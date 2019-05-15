#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
# Modifications Copyright (C) 2019 Nordix Foundation.
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
#. - Ubuntu Xenial/Bionic or Centos 7 server
#. - User running this script is part of the "docker" group
#. - Via the Acumos platform, download a solution.zip deployment package for
#.   a specific solution/revision, and unzip the package into a folder
#. - In the same folder where you unzipped the solution.zip, clone the Acumos
#.   kubernetes-client repo (this script refers to templates from that repo)
#.     git clone https://gerrit.acumos.org/r/kubernetes-client
#.   and apply any patches you need to test with, if desired
#.
#. Usage: run this script from the folder where you unpacked solution.zip
#. - bash deploy.sh <user> <pass> <namespace> [datasource]
#.   user: username on the Acumos platform
#.   pass: password on the Acumos platform
#.   namespace: Kubernetes namespace to deploy the solution under
#.   datasource: (optional) file path or URL of data source for databroker
#.
#. - To stop the solution and redeploy, run these commands before deploy.sh
#.     kubectl delete -f solution.yaml
#.     watch kubectl get pods -n acumos
#.   When you see "No resources found." you can re-run deploy.sh

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
  while ! docker login $1 -u $2 -p $3 ; do
    sleep 10
    log "Docker login failed at $1, trying again"
  done
}

function prepare_docker() {
  trap 'fail' ERR
  log "login to the Acumos platform docker proxy using the Acumos platform username and password provided by the user"
  docker_login https://$dockerProxy $USER_ID $password
  log "Log into LF Nexus Docker repos"
  docker_login https://nexus3.acumos.org:10004 docker docker
  docker_login https://nexus3.acumos.org:10003 docker docker
  docker_login https://nexus3.acumos.org:10002 docker docker
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

    # Note: copying microservice protofiles to /var/acumos, and sharing that
    # folder with probe, is redundant with the nginx-based probe design also
    # supported below, but is retained here for eventual migration to this
    # lower-overhead design
    sudo mkdir -p /var/acumos/
    sudo chown -R $USER:$USER /var/acumos
    chmod -R 777  /var/acumos
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
  if [[ $(kubectl get namespaces | grep -c "$NAMESPACE ") -eq 0 ]]; then
    log "create a namespace '$NAMESPACE' using kubectl"
    while ! kubectl create namespace $NAMESPACE; do
      log "kubectl API is not yet ready ... waiting 10 seconds"
      sleep 10
    done
  fi

  if [[ -d deploy ]]; then
    for f in $(ls deploy); do
      if kubectl delete -f deploy/$f; then echo "deploy/$f deleted"; fi
      rm deploy/$f
    done
    sol=$(grep "app:" solution.yaml | awk '{print $2}' | uniq | sed ':a;N;$!ba;s/\n/ /g')
    proxies=$(grep -m 1 "app: nginx-proxy" deploy/nginx-proxy*.yaml | awk '{print $2}' | sed ':a;N;$!ba;s/\n/ /g')
    apps="modelconnector filebeat $sol $proxies"
    for app in $apps; do
      while [[ "$(kubectl get pods -n $NAMESPACE -l app=$app)" != "" ]]; do
        log "Waiting for $app pod to terminate"
        sleep 10
      done
    done
  else
    mkdir deploy
  fi

  log "Create k8s secret for image pulling from docker using ~/.docker/config.json"
  b64=$(cat ~/.docker/config.json | base64 -w 0)

  if [[ $(kubectl get secrets -n $NAMESPACE | grep -c 'acumos-registry ') == 1 ]]; then
    kubectl delete secret -n $NAMESPACE acumos-registry
  fi
  cat << EOF >deploy/acumos-registry.yaml
apiVersion: v1
kind: Secret
metadata:
  name: acumos-registry
  namespace: $NAMESPACE
data:
  .dockerconfigjson: $b64
type: kubernetes.io/dockerconfigjson
EOF

  kubectl create -f deploy/acumos-registry.yaml
  cp solution.yaml deploy/.
  log "Update solution.yaml to use namespace $NAMESPACE"
  sed -i -- "s/namespace: acumos/namespace: $NAMESPACE/g" deploy/solution.yaml
}

function deploy_solution() {
  trap 'fail' ERR
  log "invoke kubectl to deploy the services and deployments in solution.yaml"
  kubectl create -f deploy/solution.yaml

  if [[ $(grep -c 'app: databroker' solution.yaml) -gt 0 ]]; then
    log "monitor the status of the databroker service and deployment, and when they are running, send databroker.json to the databroker via its /configDB API"
    databroker=$(kubectl get pods -n $NAMESPACE | awk '/databroker/ {print $3}')
    while [[ "$databroker" != "Running" ]]; do
      log "databroker status is $databroker. Waiting 60 seconds"
      sleep 60
      databroker=$(kubectl get pods -n $NAMESPACE | awk '/databroker/ {print $3}')
    done
    log "databroker status is $databroker"
    log "send databroker.json to the Data Broker service via the /configDB API"
    curl -v -X PUT -H "Content-Type: application/json" \
      http://127.0.0.1:30556/configDB -d @databroker.json
  fi

  log "Wait for all pods to be Running"
  pods=$(kubectl get pods -n $NAMESPACE | awk '/-/ {print $1}')
  while [[ "$pods" == "No resources found." ]]; do
    log "pods are not yet created, waiting 10 seconds"
    pods=$(kubectl get pods -n $NAMESPACE | awk '/-/ {print $1}')
  done

  for pod in $pods; do
    status=$(kubectl get pods -n $NAMESPACE | awk "/$pod/ {print \$3}")
    while [[ "$status" != "Running" ]]; do
      log "$pod status is $status. Waiting 10 seconds"
      sleep 10
      status=$(kubectl get pods -n $NAMESPACE | awk "/$pod/ {print \$3}")
    done
    log "$pod status is $status"
  done

  if [[ $(grep -c 'app: modelconnector' solution.yaml) -gt 0 ]]; then
    log "Patch dockerinfo.json as workaround for https://jira.acumos.org/browse/ACUMOS-1791"
    sed -i -- 's/"container_name":"probe"/"container_name":"Probe"/' dockerinfo.json

    log "Update blueprint.json and dockerinfo.json for use of logging components"
    sol=$(grep "app:" solution.yaml | awk '{print $2}' | grep -v 'modelconnector' | uniq | sed ':a;N;$!ba;s/\n/ /g')
    apps="$sol"
    cp blueprint.json deploy/.
    cp dockerinfo.json deploy/.
    for app in $apps; do
       sed -i -- "s/$app/nginx-proxy-$app/g" deploy/dockerinfo.json
       sed -i -- "s/\"container_name\": \"$app\"/\"container_name\": \"nginx-proxy-$app\"/g" deploy/blueprint.json
    done
    # update to nginx-proxy service port
    sed -i -- "s/\"port\":\"8556\"/\"port\":\"8550\"/g" deploy/dockerinfo.json

    log "send dockerinfo.json to the Model Connector service via the /putDockerInfo API"
    while ! curl -v -X PUT -H "Content-Type: application/json" \
      http://127.0.0.1:30555/putDockerInfo -d @deploy/dockerinfo.json; do
        log "wait for Model Connector service via the /putDockerInfo API"
        sleep 10
    done
    log "send blueprint.json to the Model Connector service via the /putBlueprint API"
    curl -v -X PUT -H "Content-Type: application/json" \
      http://127.0.0.1:30555/putBlueprint -d @deploy/blueprint.json
  fi
}

function replace_env() {
  trap 'fail' ERR
  local files; local vars; local v; local vv
  log "Set variable values in k8s templates at $1"
  set +x
  if [[ -f $1 ]]; then files="$1";
  else files="$1/*.yaml"; fi
  vars=$(grep -Rho '<[^<.]*>' $files | sed 's/<//' | sed 's/>//' | sort | uniq)
  for f in $files; do
    for v in $vars ; do
      eval vv=\$$v
      sed -i -- "s~<$v>~$vv~g" $f
    done
  done
  set -x
}

function get_model_env() {
  trap 'fail' ERR
  echo "App" $1;
#  awk "/Service/{f=1}/name:/ && f{print \$2; exit}"

  export DOCKER_IMAGE_TAG=$(awk "/name: $1/{f=1}/image:/ && f{print \$2; exit}" solution.yaml)

  export DOCKER_IMAGE=$(python3 -c "import sys, os; text=os.environ.get('DOCKER_IMAGE_TAG'); \
    firstSlash =text.index('/');\
    lastUnderScore = text.rindex(':');\
    print(text[firstSlash+1:lastUnderScore]);")

  export DOCKER_IMAGE_VERSION=$(python3 -c "import sys, os; text=os.environ.get('DOCKER_IMAGE_TAG'); \
    lastColon =text.rindex(':');\
    lengthOfImage = len(text);\
    print(text[lastColon+1:lengthOfImage]);")

  # Determine solution id for each docker image - to determine if composite case works
  export SOLUTION_ID=$(python3 -c "import sys, os; text=os.environ.get('DOCKER_IMAGE'); \
    lastUnderscore =text.rindex('_');\
    print(text[lastUnderscore+1:len(text)]);")
  
  # determine the revisionId based on the solutionId
  revId=$SOL_REVISION_ID
  regex=".*${SOLUTION_ID}:([a-z0-9-]*),*"
  # echo "regex $regex"
  [[ $revId =~ $regex ]];
  export REVISION_ID=${BASH_REMATCH[1]}
  echo "solutionId:${SOLUTION_ID} , revisionId:${REVISION_ID}"

  echo "image" $image;
  # is using micro service standard
  dockerProxyHttp=${dockerProxy/30883/30882}
  export MODEL_RUNNER_STANDARD=$(curl -k $dockerProxyHttp/v2/$DOCKER_IMAGE/manifests/$DOCKER_IMAGE_VERSION | \
    python3 -c "import sys, json; \
    manifest = json.load(sys.stdin); \
    compatibility = manifest['history'][0]['v1Compatibility']; \
    labels = json.loads(compatibility)['config']['Labels']; \
    print(int('micro-service-rest-api-version' in labels));"  | tr -d '\r')
 }


function gitclone_kubernetes_client() {
  if [ ! -d "./kubernetes-client" ]
  then
    log "Clone kubernetes repo"
    git clone https://gerrit.acumos.org/r/kubernetes-client
  fi
}

function deploy_logging() {
  trap 'fail' ERR
  cp kubernetes-client/deploy/private/templates/filebeat*.yaml deploy/.
  replace_env deploy
  kubectl create -f deploy/filebeat-configmap.yaml
  kubectl create -f deploy/filebeat-rbac.yaml
  kubectl create -f deploy/filebeat-daemonset.yaml
  if [[ -d microservice ]]; then
    # Composite model
    sol=$(grep "app:" solution.yaml | awk '{print $2}' | grep -v 'modelconnector' | uniq | sed ':a;N;$!ba;s/\n/ /g')
    apps="$sol"
    modelrunnerversion="v1";
    for app in $apps; do
      export MODEL_NAME=$app
      get_model_env $app
      modelrunnerversion="v1";
      if [[ $MODEL_RUNNER_STANDARD -eq 1 ]]; then
        modelrunnerversion="v2";
      fi
      cp kubernetes-client/deploy/private/templates/nginx-configmap-$modelrunnerversion.yaml deploy/$app-nginx-configmap-$modelrunnerversion.yaml
      cp kubernetes-client/deploy/private/templates/nginx-service-composite.yaml deploy/$app-nginx-service-composite.yaml
      cp kubernetes-client/deploy/private/templates/nginx-deployment.yaml deploy/$app-nginx-deployment.yaml
      # copy k8s conf for modelconnector nginx-proxy
      cp kubernetes-client/deploy/private/templates/nginx-mc-configmap-$modelrunnerversion.yaml deploy/.
      cp kubernetes-client/deploy/private/templates/nginx-mc-service.yaml deploy/.
      cp kubernetes-client/deploy/private/templates/nginx-mc-deployment.yaml deploy/.

      replace_env deploy
      kubectl create -f deploy/$app-nginx-configmap-$modelrunnerversion.yaml
      kubectl create -f deploy/$app-nginx-service-composite.yaml
      kubectl create -f deploy/$app-nginx-deployment.yaml

    done

    # create nginx-proxy for model-connector
    kubectl create -f deploy/nginx-mc-configmap-$modelrunnerversion.yaml
    kubectl create -f deploy/nginx-mc-service.yaml
    kubectl create -f deploy/nginx-mc-deployment.yaml

  else
    # Simple model
    app=$(grep "app:" solution.yaml | awk '{print $2}' | grep -v 'modelconnector' | uniq)
    export MODEL_NAME=$(grep -m 1 "name:" solution.yaml | awk '{print $2}')
    get_model_env $app
    modelrunnerversion="v1";
    if [[ $MODEL_RUNNER_STANDARD -eq 1 ]]; then
      modelrunnerversion="v2";
    fi
    cp kubernetes-client/deploy/private/templates/nginx-configmap-$modelrunnerversion.yaml deploy/$app-nginx-configmap-$modelrunnerversion.yaml
    cp kubernetes-client/deploy/private/templates/nginx-service.yaml deploy/$app-nginx-service.yaml
    cp kubernetes-client/deploy/private/templates/nginx-deployment.yaml deploy/$app-nginx-deployment.yaml
    
    replace_env deploy
    kubectl create -f deploy/$app-nginx-configmap-$modelrunnerversion.yaml
    kubectl create -f deploy/$app-nginx-service.yaml
    kubectl create -f deploy/$app-nginx-deployment.yaml
  fi
}

if [[ $# -lt 3 ]]; then
  if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then grep '#\. ' $0 | sed 's/#.//g'; fi
  echo "All parameters not provided"
  exit 1
fi

set -x
source ./deploy_env.sh
[ -z "$LOGSTASH_HOST" ] && echo "Need to set LOGSTASH_HOST" && exit 1;
[ -z "$LOGSTASH_PORT" ] && echo "Need to set LOGSTASH_PORT" && exit 1;
export USER_ID=$1
password=$2
export NAMESPACE=$3
datasource=$4
gitclone_kubernetes_client

dist=$(grep --m 1 ID /etc/os-release | awk -F '=' '{print $2}' | sed 's/"//g')
export WORK_DIR=$(pwd)
log "retrieve the hostname:port of the Acumos platform docker proxy from the solution.yaml, using the 'image' attribute of any model microservice"
dockerProxy=$(grep 'image.*\/' solution.yaml | grep -ve acumos.org | head -1 | sed 's/        image: //' | cut -d '/' -f 1)
prepare_docker
update_datasource
update_blueprint
prepare_k8s
deploy_solution
deploy_logging
cd $WORK_DIR

log "Use this command to get info from Kubernetes"
log "kubectl get pods,svc -n  $NAMESPACE";
kubectl get pods,svc -n  $NAMESPACE
