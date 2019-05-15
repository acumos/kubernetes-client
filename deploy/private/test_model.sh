#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
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
#. - test_model_prereqs.sh run by a user with sudo privileges
#. - Via the Acumos platform, download a solution.zip deployment package for
#.   a specific model, unzip the package into a folder, and enter that folder.
#. - For simple models, also download the two artifacts below from the Acumos
#.   portal into the same folder, and refer to them when calling test-model.sh
#.   "model name".proto, e.g. "square-9.proto"
#.   "TOSCAPROTOBUF-n.json" e.g. TOSCAPROTOBUF-9.json
#. - Install the solution via deploy.sh (see the script for details)
#. Usage:
#. - bash test-model.sh "<input>" "<host>" [.proto] [.json]
#.   input: quoted string of test data to pass to the model
#.   host: host (name or IP address) where the model is deployed
#.   .proto: for a simple model, the filename for "model name".proto
#.   .json: for a simple model, the filename "TOSCAPROTOBUF-n".json
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
    port=$(kubectl get svc -n acumos | awk '/nginx-proxy-mc/{print $5}' | cut -d '/' -f 1 | cut -d ':' -f 2)
    set -x
    echo "$input" \
      | ~/protoc/bin/protoc --encode=$firstpkg.$firstmsg \
          --proto_path=microservice/$firstms microservice/$firstms/model.proto \
      | curl -s --request POST -H "Content-Type: application/vnd.google.protobuf" \
          -H "Accept: application/vnd.google.protobuf" \
          --data-binary @- http://$host:$port/model/methods/$firstop \
      | ~/protoc/bin/protoc --decode $lastpkg.$lastmsg \
          --proto_path=microservice/$lastms microservice/$lastms/model.proto
    set +x
  else
    # Simple model
    firstms=$(jq -r ".service.listOfOperations[0].operationName" $json)
    firstop=$firstms
    firstmsg=$(jq -r ".service.listOfOperations[0].listOfInputMessages[0].inputMessageName" $json)
    firstpkg=$(jq -r ".packageName" $json)
    lastms=$firstms
    lastmsg=$(jq -r ".service.listOfOperations[0].listOfOutputMessages[0].outPutMessageName" $json)
    lastpkg=$firstpkg
    port=$(kubectl get svc -n acumos | awk '/nginx-proxy/{print $5}' | cut -d '/' -f 1 | cut -d ':' -f 2)
    set -x
    echo "$input" \
      | ~/protoc/bin/protoc --encode=$firstpkg.$firstmsg \
          --proto_path=. $proto \
      | curl -s --request POST -H "Content-Type: application/vnd.google.protobuf" \
          -H "Accept: application/vnd.google.protobuf" \
          --data-binary @- http://$host:$port/model/methods/$firstop \
      | ~/protoc/bin/protoc --decode=$lastpkg.$lastmsg \
          --proto_path=. $proto
    set +x
  fi
}

input="$1"
host=$2
proto=$3
json=$4

dist=$(grep --m 1 ID /etc/os-release | awk -F '=' '{print $2}' | sed 's/"//g')
test_model
