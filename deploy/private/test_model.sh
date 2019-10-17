#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2018-2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
# ===================================================================================
# This Acumos software file is distributed by AT&T and Tech Mahindra
# under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# This file is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ===============LICENSE_END=========================================================
#
#. What this is: Script to aid in testing an Acumos solution/model as deployed
#. by deploy.sh
#.
#. Prerequisites:
#. - Solution deployed via "Deploy to Local" or "Deploy to k8s" options
#. - For simple models, download the .proto file for the model and put it into
#.   the same folder as this script.
#. - For complex models, download the solution.zip for the solution via the
#.   "Deploy to local" option, and unpack it. It will contain the component
#.   microservice details (.proto files) in a set of subfolders under
#.   "microservice". Those .proto files and the blueprint.json for the solution
#.   are used by this script.
#. Usage:
#. - bash test-model.sh "<input>" "<url>" [.proto]
#.   input: quoted string of test data to pass to the model
#.   url: URL for model data ingress
#.   .proto: for a simple model, the filename for "model name".proto
#.

trap 'fail' ERR

function fail() {
  log "$1"
  exit 1
}

function log() {
  fname=$(caller 0 | awk '{print $2}')
  fline=$(caller 0 | awk '{print $1}')
  echo; echo "$fname:$fline ($(date)) $1"
}

function find_node() {
  nodes=$(jq '.nodes | length' blueprint.json)
  n=0; inputnode=''
  while [[ $n -lt $nodes ]] ; do
    name=$(jq -r ".nodes[$n].container_name" blueprint.json)
    if [[ $name == $1 ]]; then
      node=$n
    fi
    n=$[$n+1]
  done
}

function test_model() {
  if [[ -f blueprint.json ]]; then
    # Composite model
    firstms=$(jq -r '.input_ports[0].container_name' blueprint.json)
    find_node $firstms
    if [[ $(jq -r ".nodes[$node].node_type" blueprint.json) == DataBroker ]]; then
      firstms=$(jq -r ".nodes[$node].operation_signature_list[0].connected_to[0].container_name" blueprint.json)
      find_node $firstms
    fi

    firstop=$(jq -r ".nodes[$node].operation_signature_list[0].operation_signature.operation_name" blueprint.json)
    firstmsg=$(jq -r ".nodes[$node].operation_signature_list[0].operation_signature.input_message_name" blueprint.json)
    firstpkg=$(grep package microservice/$firstms/model.proto | cut -d ' ' -f 2 | sed 's/;//')
    nextms=$(jq -r ".nodes[$node].operation_signature_list[0].connected_to[0].container_name" blueprint.json)
    while [[ $nextms != null ]] ; do
      find_node $nextms
      lastms=$(jq -r ".nodes[$node].container_name" blueprint.json)
      lastmsg=$(jq -r ".nodes[$node].operation_signature_list[0].operation_signature.output_message_name" blueprint.json)
      lastpkg=$(grep package microservice/$lastms/model.proto | cut -d ' ' -f 2 | sed 's/;//')
      nextms=$(jq -r ".nodes[$node].operation_signature_list[0].connected_to[0].container_name" blueprint.json)
    done
    set -x
    echo "$input" \
      | ~/protoc/bin/protoc --encode=$firstpkg.$firstmsg \
          --proto_path=microservice/$firstms microservice/$firstms/model.proto \
      | curl -k -s --request POST -H "Content-Type: application/vnd.google.protobuf" \
          -H "Accept: application/vnd.google.protobuf" \
          --data-binary @- $url/model/methods/$firstop \
      | ~/protoc/bin/protoc --decode $lastpkg.$lastmsg \
          --proto_path=microservice/$lastms microservice/$lastms/model.proto
    set +x
  else
    # Simple model
    firstms=$(awk '/rpc /{print $2}' $proto)
    lastms=$firstms
    firstop=$firstms
    firstmsg=$(awk '/message /{print $2}' $proto | head -1)
    lastmsg=$(awk '/message /{print $2}' $proto | tail -n1)
    firstpkg=$(awk '/package /{print $2}' $proto | sed 's/;//')
    lastpkg=$firstpkg
    set -x
    echo "$input" \
      | ~/protoc/bin/protoc --encode=$firstpkg.$firstmsg \
          --proto_path=. $proto \
      | curl -k -s --request POST -H "Content-Type: application/vnd.google.protobuf" \
          -H "Accept: application/vnd.google.protobuf" \
          --data-binary @- $url/model/methods/$firstop \
      | ~/protoc/bin/protoc --decode=$lastpkg.$lastmsg \
          --proto_path=. $proto
    set +x
  fi
}

input="$1"
url=$2
proto=$3

dist=$(grep --m 1 ID /etc/os-release | awk -F '=' '{print $2}' | sed 's/"//g')
test_model
