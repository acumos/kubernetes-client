#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
#. What this is: script to setup a kubernetes cluster with calico as cni
#. 
#. Prerequisites:
#. - One or more Ubuntu Xenial or Centos 7 servers (as target k8s cluster nodes)
#. - This script downloaded to a folder on the server to be the k8s master node
#. - key-based SSH setup between the k8s master node and other nodes
#. - 192.168.0.0/16 should not be used on your server network interface subnets
#.
#. Usage: on the master node,
#. $ git clone git clone https://gerrit.acumos.org/r/kubernetes-client
#. $ cd kubernetes-client/deploy/private
#. $ bash setup_k8s.sh "[nodes]"
#.   nodes: quoted, space-separated list of k8s worker nodes. If no nodes are
#.          specified a single all-in-one (AIO) cluster will be installed.
#.

trap 'fail' ERR

function log() {
  fname=$(caller 0 | awk '{print $2}')
  fline=$(caller 0 | awk '{print $1}')
  echo; echo "$fname:$fline ($(date)) $1"
}

function setup_prereqs() {
  log "Create prerequisite setup script"
  cat <<'EOG' >~/prereqs.sh
#!/bin/bash
# Basic server pre-reqs
function wait_dpkg() {
  # TODO: workaround for "E: Could not get lock /var/lib/dpkg/lock - open (11: Resource temporarily unavailable)"
  echo; echo "waiting for dpkg to be unlocked"
  while sudo fuser /var/{lib/{dpkg,apt/lists},cache/apt/archives}/lock >/dev/null 2>&1; do
    sleep 1
  done
}
dist=$(grep --m 1 ID /etc/os-release | awk -F '=' '{print $2}' | sed 's/"//g')
if [[ $(grep -c $HOSTNAME /etc/hosts) -eq 0 ]]; then
  echo; echo "prereqs.sh: ($(date)) Add $HOSTNAME to /etc/hosts"
  # have to add "/sbin" to path of IP command for centos
  echo "$(/sbin/ip route get 8.8.8.8 | awk '{print $NF; exit}') $HOSTNAME" \
    | sudo tee -a /etc/hosts
fi
if [[ "$dist" == "ubuntu" ]]; then
  # Per https://kubernetes.io/docs/setup/independent/install-kubeadm/
  echo; echo "prereqs.sh: ($(date)) Basic prerequisites"

  wait_dpkg; sudo apt-get update
  wait_dpkg; sudo apt-get upgrade -y

  dce=$(dpkg -l | grep -c docker-ce)
  if [[ $dce -eq 0 ]]; then
    echo; echo "prereqs.sh: ($(date)) Install latest docker-ce"
    # Per https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/
    sudo apt-get remove -y docker docker-engine docker.io docker-ce
    sudo apt-get update
    sudo apt-get install -y \
      apt-transport-https \
      ca-certificates \
      curl \
      software-properties-common
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
    sudo add-apt-repository "deb [arch=amd64] \
      https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
    sudo apt-get update
    sudo apt-get install -y docker-ce
  fi

  echo; echo "prereqs.sh: ($(date)) Get k8s packages"
  export KUBE_VERSION=1.10.0
  # per https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/
  # Install kubelet, kubeadm, kubectl per https://kubernetes.io/docs/setup/independent/install-kubeadm/
  sudo apt-get update && sudo apt-get install -y apt-transport-https
  curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
  cat <<EOF | sudo tee /etc/apt/sources.list.d/kubernetes.list
deb http://apt.kubernetes.io/ kubernetes-xenial main
EOF
  sudo apt-get update
  echo; echo "prereqs.sh: ($(date)) Install kubectl, kubelet, kubeadm"
  sudo apt-get -y install --allow-downgrades kubectl=${KUBE_VERSION}-00 \
    kubelet=${KUBE_VERSION}-00 kubeadm=${KUBE_VERSION}-00
  echo; echo "prereqs.sh: ($(date)) Install jq for API output parsing"
  sudo apt-get install -y jq
  if [[ "$(sudo ufw status)" == "Status: active" ]]; then
    echo; echo "prereqs.sh: ($(date)) Set firewall rules"
    if [[ "$1" == "master" ]]; then
      sudo ufw allow 6443/tcp
      sudo ufw allow 2379:2380/tcp
      sudo ufw allow 10250/tcp
      sudo ufw allow 10251/tcp
      sudo ufw allow 10252/tcp
      sudo ufw allow 10255/tcp
    else
      sudo ufw allow 10250/tcp
      sudo ufw allow 10255/tcp
      sudo ufw allow 30000:32767/tcp
    fi
  fi
  # TODO: fix need for this workaround: disable firewall since the commands 
  # above do not appear to open the needed ports, even if ufw is inactive
  # (symptom: nodeport requests fail unless sent from within the cluster or
  # to the node IP where the pod is assigned) issue discovered ~11/16/17
  sudo ufw disable
else
  echo; echo "prereqs.sh: ($(date)) Basic prerequisites"
  sudo yum install -y epel-release
  sudo yum update -y
  sudo yum install -y wget git
  echo; echo "prereqs.sh: ($(date)) Install latest docker"
  # per https://docs.docker.com/engine/installation/linux/docker-ce/centos/#install-from-a-package
  sudo yum install -y docker
  sudo systemctl enable docker
  sudo systemctl start docker
#  wget https://download.docker.com/linux/centos/7/x86_64/stable/Packages/docker-ce-17.09.0.ce-1.el7.centos.x86_64.rpm
#  sudo yum install -y docker-ce-17.09.0.ce-1.el7.centos.x86_64.rpm
#  sudo systemctl start docker
  echo; echo "prereqs.sh: ($(date)) Install kubectl, kubelet, kubeadm"
  cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
  sudo setenforce 0
  sudo yum install -y kubelet kubeadm kubectl
  sudo systemctl enable kubelet
  sudo systemctl start kubelet
  echo; echo "prereqs.sh: ($(date)) Install jq for API output parsing"
  sudo yum install -y jq  
  echo; echo "prereqs.sh: ($(date)) Set firewall rules"
  cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
  sudo sysctl --system
fi
EOG
}

function setup_k8s_master() {
  log "Setting up kubernetes master"
  setup_prereqs

  # Install master
  bash ~/prereqs.sh master
  # per https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/
  # If the following command fails, run "kubeadm reset" before trying again
  # --pod-network-cidr=192.168.0.0/16 is required for calico; this should not 
  # conflict with your server network interface subnets
  log "Reset kubeadm in case pre-existing cluster"
  sudo kubeadm reset
  # Start cluster
  log "Workaround issue '/etc/kubernetes/manifests is not empty'"
	mkdir ~/tmp
  # workaround for [preflight] Some fatal errors occurred:
	#                /etc/kubernetes/manifests is not empty
  sudo rm -rf /etc/kubernetes/manifests/*
  log "Disable swap to workaround k8s incompatibility with swap"
  # per https://github.com/kubernetes/kubeadm/issues/610
  sudo swapoff -a
  log "Start the cluster"
  sudo kubeadm init --pod-network-cidr=192.168.0.0/16 >>~/tmp/kubeadm.out
  cat ~/tmp/kubeadm.out
  export k8s_joincmd=$(grep "kubeadm join" ~/tmp/kubeadm.out)
  log "Cluster join command for manual use if needed: $k8s_joincmd"
  mkdir -p $HOME/.kube
  sudo cp -f /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config
  export KUBECONFIG=$HOME/.kube/config
  # Deploy pod network
  log "Deploy calico as CNI"
  # Updated to deploy Calico 2.6 per the create-cluster-kubeadm guide above
  #  kubectl apply -f http://docs.projectcalico.org/v2.4/getting-started/kubernetes/installation/hosted/kubeadm/1.6/calico.yaml
  kubectl apply -f https://docs.projectcalico.org/v2.6/getting-started/kubernetes/installation/hosted/kubeadm/1.6/calico.yaml

  # TODO: document process dependency
  # Failure to wait for all calico pods to be running can cause the first worker
  # to be incompletely setup. Symptom is that node_ports cannot be routed
  # via that node (no response - incoming SYN packets are dropped). 
  log "Wait for all calico pods to be Created"
  # calico-etcd, calico-kube-controllers, calico-node
  pods=$(kubectl get pods --namespace kube-system | grep -c calico)
  while [[ $pods -lt 3 ]]; do
    log "all calico pods are not yet created. Waiting 10 seconds"
    sleep 10
    pods=$(kubectl get pods --namespace kube-system | grep -c calico)
  done
  
  log "Wait for all calico pods to be Running"
  pods=$(kubectl get pods --namespace kube-system | awk '/calico/ {print $1}')
  for pod in $pods; do
    status=$(kubectl get pods --namespace kube-system | awk "/$pod/ {print \$3}")
    while [[ "$status" != "Running" ]]; do
      log "$pod status is $status. Waiting 10 seconds"
      sleep 10
      status=$(kubectl get pods --namespace kube-system | awk "/$pod/ {print \$3}")
    done
    log "$pod status is $status"
  done

  log "Wait for kubedns to be Running"
  kubedns=$(kubectl get pods --namespace kube-system | awk '/kube-dns/ {print $3}')
  while [[ "$kubedns" != "Running" ]]; do
    log "kube-dns status is $kubedns. Waiting 60 seconds"
    sleep 60
    kubedns=$(kubectl get pods --namespace kube-system | awk '/kube-dns/ {print $3}')
  done
  log "kube-dns status is $kubedns"

  log "Allow pod scheduling on master (nodeSelector will be used to limit them)"
  kubectl taint node $HOSTNAME node-role.kubernetes.io/master:NoSchedule-

  log "Label node $HOSTNAME as 'role=master'"
  kubectl label nodes $HOSTNAME role=master

  log "Deploy kubernetes dashboard"
  wget https://raw.githubusercontent.com/kubernetes/dashboard/master/src/deploy/recommended/kubernetes-dashboard.yaml
  # Use "gobbling the entire file" approach to enable multi-line replace
  sed -i -e '1h;2,$H;$!d;g' -e 's~ports\:\n    - ~type\: NodePort\n  ports\:\n    - nodePort\: 32767\n      ~' kubernetes-dashboard.yaml
  kubectl create -f kubernetes-dashboard.yaml
  
  log "Enable default admin access to the kubernetes dashboard"
  # per https://github.com/kubernetes/dashboard/wiki/Access-control#admin-privileges
  cat <<EOF >dashboard-admin.yaml
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: kubernetes-dashboard
  labels:
    k8s-app: kubernetes-dashboard
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: kubernetes-dashboard
  namespace: kube-system
EOF
  kubectl create -f dashboard-admin.yaml
}

function setup_k8s_workers() {
  workers="$1"
  export k8s_joincmd=$(grep "kubeadm join" ~/tmp/kubeadm.out)
  log "Installing workers at $1 with joincmd: $k8s_joincmd"

# TODO: kubeadm reset below is workaround for 
# Ubuntu: "[preflight] Some fatal errors occurred: /var/lib/kubelet is not empty"
# per https://github.com/kubernetes/kubeadm/issues/1
# Centos: "Failed to start ContainerManager failed to initialize top
# level QOS containers: root container /kubepods doesn't exist"
  tee start_worker.sh <<EOF
sudo kubeadm reset
sudo $k8s_joincmd
EOF

# process below is serial for now; when workers are deployed in parallel,
# sometimes calico seems to be incompletely setup at some workers. symptoms
# similar to as noted for the "wait for calico" steps above.
  for worker in $workers; do
    host=$(ssh -x -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $USER@$worker hostname)
    log "Install worker at $worker hostname $host"
    if ! scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \
      ~/prereqs.sh $USER@$worker:/home/$USER/. ; then
      fail "Failed copying setup files to $worker"
    fi
    ssh -x -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \
      $USER@$worker bash prereqs.sh
    scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ~/k8s_env.sh \
      $USER@$worker:/home/$USER/k8s_env.sh
    scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \
      start_worker.sh $USER@$worker:/home/$USER/.
    ssh -x -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \
      $USER@$worker bash start_worker.sh

    log "checking that node $host is 'Ready'"
    status=$(kubectl get nodes | awk "/$host/ {print \$2}")
    while [[ "$status" != "Ready" ]]; do
      log "node $host is \"$status\", waiting 10 seconds"
      status=$(kubectl get nodes | awk "/$host/ {print \$2}")
      ((tries++))
      if [[ tries -gt 18 ]]; then
        log "node $host is \"$status\" after 3 minutes; resetting kubeadm"
        ssh -x -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \
          $USER@$worker bash start_worker.sh
        tries=1
      fi
      sleep 10
    done
    log "node $host is 'Ready'."
    log "Label node $host as 'worker'"
    kubectl label nodes $host role=worker
  done

  log "***** kube proxy pods *****"
  pods=$(kubectl get pods --all-namespaces | awk '/kube-proxy/ {print $2}')
  for pod in $pods; do
    echo; echo "**** $pod ****"
    kubectl describe pods --namespace kube-system $pod
    echo; echo "**** $pod logs ****"
    kubectl logs --namespace kube-system $pod
  done

  log "Cluster is ready (all nodes in 'kubectl get nodes' show as 'Ready')."
}

dist=$(grep --m 1 ID /etc/os-release | awk -F '=' '{print $2}' | sed 's/"//g')
hostip=$(ip route get 8.8.8.8 | awk '{print $NF; exit}')

setup_k8s_master

if [[ ! -z "$2" ]]; then
  setup_k8s_workers "$2"
fi

log "Setup is complete."
log "The kubernetes dashboard is at https://$hostip:32767"
