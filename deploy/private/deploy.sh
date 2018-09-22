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
#. What this is: Deployment script for Acumos solutions under private kubernetes
#. clusters.
#. 
#. Prerequisites:
#. - Ubuntu Xenial or Centos 7 server
#. - Via the Acumos platform, download a solution.zip deployment package for
#.   a specific solution/revision, and unzip the package into a folder
#. Usage:
#. - bash deploy.sh <solution> <user> <pass> [datasource]
#.   solution: path to folder where solution.zip was unpacked
#.   user: username on the Acumos platform
#.   pass: password on the Acumos platform
#.   datasource: (optional) file path or URL of data source for databroker

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

function docker_login() {
  trap 'fail' ERR
  while ! sudo docker login $1 -u $2 -p $3 ; do
    log "Docker login failed at $1, trying again"
  done
}

function prepare_docker() {
  trap 'fail' ERR
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

  log "login to the Acumos platform docker proxy using the Acumos platform username and password provided by the user"
  docker_login https://$dockerProxy $username $password
  log "Log into LF Nexus Docker repos"
  docker_login https://nexus3.acumos.org:10004 docker docker
  docker_login https://nexus3.acumos.org:10003 docker docker
  docker_login https://nexus3.acumos.org:10002 docker docker
  sudo chown -R $USER:$USER ~/.docker
}

function update_datasource() {
  trap 'fail' ERR
  if [[ $(grep -c 'app: databroker' solution.yaml) -gt 0 ]]; then
    log "Extract databroker.json from blueprint.json"
    nodes=$(jq '.nodes | length' blueprint.json)
    node=0
    while [[ $node -lt $nodes ]] ; do
      type=$(jq --raw-output ".nodes[$node].node_type" blueprint.json)
      if [[ $type == DataBroker ]]; then
        jq -r ".nodes[$node].data_broker_map" blueprint.json >> databroker.json
      fi
      node=$[$node+1]
    done

    log "update databroker.json per the datasource selected by the user"
    if [[ $(echo $datasource | grep -c ':') -gt 0 ]]; then
      sed -i -- "s~\"target_system_url\": \".*\"~\"target_system_url\": \"$datasource\"~" databroker.json
    else
      sed -i -- 's~"local_system_data_file_path": ".*"~"local_system_data_file_path": "/var/acumos/datasource"~' databroker.json
    fi
  fi
}

function update_blueprint() {
  trap 'fail' ERR
  if [[ -d microservice ]]; then
    log "copy the subfolders under 'microservice' from the unpacked solution.zip to /var/acumos"

    sudo mkdir -p /var/acumos/log
    sudo chown -R $USER:$USER /var/acumos
    # Note: copying microservice protofiles to /var/acumos, and sharing that
    # folder with probe, is redundant with the nginx-based probe design also
    # supported below, but is retained here for eventual migration to this
    # lower-overhead design
    cp -r microservice /var/acumos/.

    log "update URL to model.proto files in blueprint.json"
    # Note: modelconnector sends these URLs to probe which retrieves the proto
    # files from the solution-embedded nginx server
    nodes=$(jq '.nodes | length' blueprint.json)
    models=$(ls microservice)
    for model in $models ; do
      node=0
      while [[ $node < $nodes ]] ; do
        name=$(jq --raw-output ".nodes[$node].container_name" blueprint.json)
        if [[ $name == $model ]]; then
          echo ".nodes[$node].proto_uri = \"http://localhost/$model/model.proto\""
          jq ".nodes[$node].proto_uri = \"http://localhost/$model/model.proto\"" blueprint.json > blueprint1.json
          mv blueprint1.json blueprint.json
        fi
        node=$[$node+1]
      done
    done
  fi
}

prepare_k8s() {
  trap 'fail' ERR
  if [[ $(kubectl get namespaces | grep -c 'acumos ') -eq 0 ]]; then
    log "create a namespace 'acumos' using kubectl"
    while ! kubectl create namespace acumos; do
      log "kubectl API is not yet ready ... waiting 10 seconds"
      sleep 10
    done
  fi

  log "Create k8s secret for image pulling from docker using ~/.docker/config.json"
   b64=$(cat ~/.docker/config.json | base64 -w 0)
  if [[ $(kubectl get secrets -n acumos | grep -c 'acumos-registry ') == 1 ]]; then
    kubectl delete secret -n acumos acumos-registry
   fi
  cat << EOF >acumos-registry.yaml
apiVersion: v1
kind: Secret
metadata:
  name: acumos-registry
  namespace: acumos
data:
  .dockerconfigjson: $b64
type: kubernetes.io/dockerconfigjson
EOF

  kubectl create -f acumos-registry.yaml
}

function deploy_solution() {
  trap 'fail' ERR
  log "invoke kubectl to deploy the services and deployments in solution.yaml"
  kubectl create -f solution.yaml

  if [[ $(grep -c 'app: databroker' solution.yaml) -gt 0 ]]; then
    log "monitor the status of the databroker service and deployment, and when they are running, send databroker.json to the databroker via its /configDB API"
    databroker=$(kubectl get pods --namespace acumos | awk '/databroker/ {print $3}')
    while [[ "$databroker" != "Running" ]]; do
      log "databroker status is $databroker. Waiting 60 seconds"
      sleep 60
      databroker=$(kubectl get pods --namespace acumos | awk '/databroker/ {print $3}')
    done
    log "databroker status is $databroker"
    log "send databroker.json to the Data Broker service via the /configDB API"
    curl -v -X PUT -H "Content-Type: application/json" \
      http://127.0.0.1:30556/configDB -d @databroker.json
  fi

  log "monitor the status of all other services and deployments, and when they are running"
  log "Wait for all pods to be Running"
  pods=$(kubectl get pods --namespace acumos | awk '/-/ {print $1}')
  for pod in $pods; do
    status=$(kubectl get pods -n acumos | awk "/$pod/ {print \$3}")
    while [[ "$status" != "Running" ]]; do
      log "$pod status is $status. Waiting 10 seconds"
      sleep 10
      status=$(kubectl get pods -n acumos | awk "/$pod/ {print \$3}")
    done
    log "$pod status is $status"
  done

  if [[ $(grep -c 'app: modelconnector' solution.yaml) -gt 0 ]]; then
    log "send dockerinfo.json to the Model Connector service via the /putDockerInfo API"
    curl -v -X PUT -H "Content-Type: application/json" \
      http://127.0.0.1:30555/putDockerInfo -d @dockerinfo.json
    log "send blueprint.json to the Model Connector service via the /putBlueprint API"
    curl -v -X PUT -H "Content-Type: application/json" \
      http://127.0.0.1:30555/putBlueprint -d @blueprint.json
  fi
}

solution=$1
username=$2
password=$3
datasource=$4

export WORK_DIR=$(pwd)
cd $solution
prepare_docker
update_datasource
update_blueprint
prepare_k8s
deploy_solution
cd $WORK_DIR
