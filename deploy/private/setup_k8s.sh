#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
# What this is: script to setup a single-node generic kubernetes cluster
# in preparation for deploying Acumos models.
#
# Intended to be used as included in "solution.zip" packages as downloaded by
# the user for a "Deploy to Local" action on the Acumos platform. Unzip the
# solution.zip and as needed run this command. NOTE: if you are deplying models
# to an existing generic k8s cluster, be aware that this command will disrupt
# that cluster for a short time, as needed to configure the docker-engine on
# the host to allow use of the Acumos platforn Nexus as an insecure registry.
#
# Prerequisites:
# - Ubuntu Xenial/Bionic or Centos 7 server
#   - NOTE: Deploying models under OpenShift/Centos is not supported yet
# - solution.zip downloaded and extracted
# - 192.168.0.0/16 should not be used on your server network interface subnets
# - Docker pre-installed
# - User running this script must be assigned to the docker group and have
#   sudo permission (passwordless sudo is recommended)
# - wget installed
#
# Usage: from the folder where you extracted the solution.zip
# $ bash setup_k8s.sh
#

set -x

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

if [[ ! $(which kubectl) ]]; then
  wget https://raw.githubusercontent.com/acumos/system-integration/master/tools/setup_k8s.sh -O setup_k8s_base.sh
  bash setup_k8s_base.sh
fi

log "retrieve the hostname:port of the Acumos platform docker proxy from the solution.yaml, using the 'image' attribute of any model microservice"
dockerProxy=$(grep 'image.*\/' solution.yaml | grep -ve acumos.org | head -1 | sed 's/        image: //' | cut -d '/' -f 1)

if [[ $(sudo grep -c $dockerProxy /etc/docker/daemon.json) -eq 0 ]]; then
  log "configure the docker service to allow access to the Acumos platform docker proxy as an insecure registry."
  cat << EOF | sudo tee /etc/docker/daemon.json
{
"insecure-registries": [
  "$dockerProxy"
],
"disable-legacy-registry": true
}
EOF
  sudo systemctl daemon-reload
  sudo service docker restart
fi

log "Create logs folder as needed"
if [[ ! -e /mnt/acumos/log ]]; then
  sudo mkdir -p /mnt/acumos/log
  sudo chmod 777 /mnt/acumos/log
  sudo chown $USER:$USER /mnt/acumos/log
fi
