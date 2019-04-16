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
#. by deploy.sh. This part is for a host Admin to prepare the host configuration
#. as needed by normal (non-sudo) users to test models.
#.
#. Prerequisites:
#. - Ubuntu Xenial/Bionic or Centos 7 server
#. - Download this script from the Acumos kubernetes-client repo
#.
#. Usage:
#. - bash test_model_prereqs.sh
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

function setup_prereqs() {
  if [[ "$dist" == "ubuntu" ]]; then
    sudo apt install -y jq golang-go unzip
  else
    sudo yum install -y jq golang-go unzip
  fi

  if [[ "$(ls ~/protoc/bin/protoc)" == "" ]]; then
    log "Download and install protobuf 3 (protoc) in ~/protoc"
    # Per https://github.com/protocolbuffers/protobuf/releases
    wget https://github.com/protocolbuffers/protobuf/releases/download/v3.6.1/protoc-3.6.1-linux-x86_64.zip
    unzip protoc-3.6.1-linux-x86_64.zip -d ~/protoc
  fi
}

dist=$(grep --m 1 ID /etc/os-release | awk -F '=' '{print $2}' | sed 's/"//g')
setup_prereqs
test_model
