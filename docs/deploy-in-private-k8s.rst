..
  ===============LICENSE_START=======================================================
  Acumos CC-BY-4.0
  ===================================================================================
  Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
  ===================================================================================
  This Acumos documentation file is distributed by AT&T and Tech Mahindra
  under the Creative Commons Attribution 4.0 International License (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
..
  http://creativecommons.org/licenses/by/4.0
..
  This file is distributed on an "AS IS" BASIS,
  See the License for the specific language governing permissions and
  limitations under the License.
  ===============LICENSE_END=========================================================

========================================================
Acumos Solution Deployment in Private Kubernetes Cluster
========================================================

This document describes the design for the Acumos platform support of deploying
Acumos machine-learning models into private kubernetes (k8s) clusters, as simple
(single model) or composite (multi-model) solutions. This document is published
as part of the Acumos kubernetes-client repository. The private kubernetes
deployment capabilities and design are collectively referred to in this document
as the "private-k8s-deployment" feature.

-----
Scope
-----

"Private" as used here means a k8s cluster deployed in an environment (e.g.
VM(s) or bare metal machine(s)) for which the model user has ability to use the
kubectl CLI tool on the k8s cluster master ("k8s master") to manage apps in the
cluster. This is typically only available to users when they have deployed the
cluster for their own purposes, e.g. to develop/test k8s apps.

Other designs under the Acumos kubernetes-client repo will address deployment in
other k8s environments, e.g. public clouds, or using other more generic methods
that do not depend upon direct access to the k8s master node. There is expected
to be much in common across these designs, so this design is intended to provide
an initial baseline for direct reuse in other environments where possible.

Initially however, this design makes some simplifying assumptions/choices, which
over time can be relaxed or modified, to support other types of k8s environments
(e.g. other types of private and public k8s clusters, other host machines):

* deployment process split into two steps:

  * installation of a k8s cluster, as needed: a downloadable script is provided
    for this purpose, which the user must run prior to deploying the solution
  * user manual invocation of the deployment process on the k8s master, using a
    downloadable solution package including:

    * a deployment script which the user can run to start the deployment
    * a k8s template for the solution
    * a model blueprint file as created today by the Acumos Design Studio

* the k8s cluster is deployed (or will be) on a linux variant host OS. Ubuntu
  and Centos 7 will be specifically tested/supported.

............................
Previously Released Features
............................

This is the first release of private-k8s-deployment.

........................
Current Release Features
........................

The private-k8s-deployment features planned for delivery in the current release
("Athena") are:

* a utility script (k8s-cluster.sh) enabling the user to prepare a k8s cluster
  that includes the prerequisites of the solution deployment process
* a templated deployment shell script (deploy.sh) that executes the deployment
  when invoked by the user on the k8s master
* a new Acumos platform component, the "k8s-client", that collects solution
  artifacts, creates a k8s template for the solution, and prepares a
  downloadable solution package as "solution.zip"

private-k8s-deployment depends upon these related features to be delivered in
the Athena release:

* Acumos portal-marketplace support for a new "deploy to private kubernetes"
  option for a solution
* A new Acumos component, the "docker-proxy", which provides a user's kubernetes
  cluster to pull docker images from an Acumos platform Nexus repository

------------
Architecture
------------

The following diagram illustrates the functional components, interfaces, and
interaction of the components for a typical private-k8s-deployment process:

.. image:: images/private-k8s-client-arch.png

A summary of the process steps:

#. At the Acumos platform, the user selects "deploy to private k8s cloud", and
   follows this optional procedure to setup a private k8s cluster

   * A: the user selects a link to "download a k8s cluster setup script"
   * B: the user saves the script on a host where the k8s cluster will be installed
   * C: the user executes the setup script and a k8s cluster is installed

#. The user requests to download the deployable solution

   * A: the user selects a link to "download the solution and deployment script"
   * B: the portal-marketplace calls the /getSolutionZip API of the k8s-client
     service
   * C: the k8s-client calls the Solution Controller APIs of the
     common-data-service to obtain the URIs of the artifacts to be included
   * D: the k8s-client calls the Maven artifact API of nexus to retrieve the
     artifacts, prepares the solution package, and returns it to the
     portal-marketplace, which downloads it to the user's machine

#. The user prepares the solution package for deployment

   * E: the user uploads the downloaded solution package to the k8s master host
   * F: the user unpacks the package, which includes 

      * deploy.sh: deployment script
      * solution.yaml: k8s template for deploying the set of model microservices
        included in the solution, plus the Data Broker, Model Connector, and
        Probe services
      * databroker.json: Data Broker model data source to target mapping info
      * blueprint.json: solution blueprint as created by the Design Studio
      * a "microservice" subfolder, containing a subfolder named for each
        model microservice container in the solution, within which is the
        "model.proto" artifact for the microservice

#. The user kicks off the deployment, which runs automatically from this point

   * A: the user invokes deploy.sh, including parameters

     * the data source (file or URL) that the Data Broker should use
     * the user's credentials on the Acumos platform, as needed to authorize the
       user's docker client to pull solution microservice images during deployment

   * B: deploy.sh logs into the Acumos docker registry via the docker-proxy,
     using the provided user credentials
   * C: the docker-proxy calls the /api/auth/jwtToken API of the
     portal-marketplace to verify that the user is registered on the platform,
     and confirms login success to the docker client.
   * D: deploy.sh logs into the Acumos project docker registry, using the
     Acumos project credentials
   * E: deploy.sh copies the model.proto files for the solution to a host-shared
     folder where the probe service can access them, and initiates deployment of
     the solution via kubectl, using the solution.yaml template. kubectl deploys
     all the services defined in the template.
   * F: using the cached authentication for the Acumos docker registry and
     the Acumos project docker registry, k8s pulls the docker images for all
     solution microservices and Acumos project components, and deplpys them.
   * G: the docker-proxy validates the active login of the user, and pulls the
     requested image from the Acumos platform docker registry.
   * H: When the Data Broker service is active (determined by monitoring its
     status through kubectl), deploy.sh

     * updates databroker.json with the data source (file or URL) selected by
       the user
     * invokes the Data Broker /configDB API to configure Data Broker with model
       data source to target mapping info using databroker.json

   * I: The Data Broker begins retrieving the solution input data, and waits for
     a /pullData API request from the Model Connector
   * J: When all of the microservices are active (determined by monitoring their
     status through kubectl), deploy.sh

     * creates a dockerinfo file with microservice name to IP/Port mapping info
     * invokes the Model Connector /putBlueprint API with blueprint.json
     * invokes the Model Connector /putDockerInfo API with the generated
       dockerinfo file

   * K: Once /putDockerInfo is processed by the Model Connector, it calls the
     Data Broker /pullData API to start retrieval of test/training data, and
     solution operation proceeds from there, with data being routed by the
     Model Connector through the microservice forwarding graph defined for the
     solution

.....................
Functional Components
.....................

The private-k8s-deployment feature will depend upon two new Acumoscomponent
microservices:

* kubernetes-client: packages solution artifacts and deployment tools into the
  "solution.zip" package
* docker-proxy: provides an authentication proxy for the platform docker repo

Other Acumos component dependencies, with related impacts in this release:

* portal-marketplace: provides the user with a download link to the
  "setup_k8s.sh" script, and a "deploy to private kubernetes" dialog that allows
  the user to download the solution.zip package

Other Acumos component dependencies, used as-is:

* common-data-svc: provides information about solution artifacts to be retrieved
* nexus: provides access to the maven artifact repository
* docker repository: as provided by the Acumos nexus service or another docker
  repository service, provides access to the microservice docker images as
  they are deployed by the k8s cluster

Other dependencies:

* a kubernetes cluster, deployed via the "setup_k8s.sh" script, or otherwise

..........
Interfaces
..........

************
Exposed APIs
************

+++++++++++++++++
Solution Download
+++++++++++++++++

The k8s-client service exposes the following API for the portal-marketplace to
obtain a downloadable package of solution artifacts and deployment script,
for a specific solution revision.

The base URL for this API is: http://<k8s-client-service-host>:<port>, where
'k8s-client-service-host' is the routable address of the verification service
in the Acumos platform deployment, and port is the assigned port where the
service is listening for API requests.

* URL resource: /getSolutionZip/{solutionId}/{revisionId}

  * {solutionId}: ID of a solution present in the CDS 
  * {revisionId}: ID of a version for a solution present in the CDS 

* Supported HTTP operations

  * GET

    * Response

      * 200 OK

        * meaning: request successful
        * body: solution package (solution.zip)

      * 404 NOT FOUND

        * meaning: solution/revision not found, details in JSON body. NOTE: this
          response is only expected in race conditions, e.g. in which a deploy
          request was initiated when at the same time, the solution was deleted
          by another user
        * body: JSON object as below

          * status: "invalid solutionId"|"invalid revisionId"

++++++++++++
Docker Login
++++++++++++

The Acumos platform docker-proxy will expose the docker login API.

+++++++++++
Docker Pull
+++++++++++

The Acumos platform docker-proxy will expose the docker pull API.

*************
Consumed APIs
*************

++++++++++++
Docker Login
++++++++++++

Via the local docker CLI client on the host machine, deploy.sh will call the
login API of:

* the Acumos platform docker-proxy, to verify that the user is authorized to
  access docker images in the Acumos platform docker registry
* the Acumos project Nexus docker API, to enable pull of the Acumos project
  docker images to be deployed as part of the solution

+++++++++++
Docker Pull
+++++++++++

Via the local docker CLI client on the host machine, kubectl will call the
docker pull API of:

* the Acumos platform docker-proxy, to pull the model microservice images to be
  deployed as part of the solution
* the Acumos project Nexus docker API, to pull the Acumos project docker images
  to be deployed as part of the solution

++++++++++++++++++++++++++
Portal User Authentication
++++++++++++++++++++++++++

The docker-proxy service will call the portal-marketplace /api/auth/jwtToken API
to verify that the user running the deploy.sh script is an actual registered
user of the Acumos platform, thus is allowed to access docker images from the
docker registry configured for the Acumos platform.

+++++++++++++++++++
Solution Controller
+++++++++++++++++++

The k8s-client service will call the Solution Controller APIs of the
common-data-svc to obtain the following solution/revision-related data:

* nexus URI of the model.proto artifact
* nexus URI of the blueprint.json artifact (if any)


----------------
Component Design
----------------

..........
k8s-client
..........

Upon a request to the /getSolutionZip API, the k8s-client will perform the
following high-level actions to prepare the downloadable solution deployment
package:

* get the following artifacts if existing from Nexus, by querying the CDS for
  the set of solution/revision artifacts

  * blueprint.json
  * databroker.json

* if a blueprint.json artifact was found, this is a composite solution and the
  following actions are taken

  * get the model.proto artifact for each solution model microservice, for the
    model revision included in the solution
  * create a kubernetes service+deployment template as solution.yaml including
    all the solution components included in blueprint.json. See below for an
    example.

* if a blueprint.json artifact was not found, this is a simple solution and a
  kubernetes service+deployment template is created, as solution.yaml. See below
  for an example.
* In the generated solution.yaml, specify for each model microservice the
  hostname:port for the Acumos platform docker proxy, e.g.
  "acumos.example.com:35001" in the examples below
* retrieve the current deploy.sh script from the Acumos githib mirror, at
  https://raw.githubusercontent.com/acumos/kubernetes-client/master/deploy/private/deploy.sh
* create a zip archive as solution.zip containing:

  * deploy.sh
  * solution.yaml
  * for a composite solution:

    * databroker.json
    * blueprint.json
    * a "microservice" subfolder, with subfolders named for each model
      microservice, containing the model.proto for that model

* return the solution.zip as /getSolutionZip API response

Design notes for the solution.yaml structure:

* to support distribution of solution microservices and other Acumos components
  (databroker, modelconnector, probe) across nodes in multi-node kubernetes
  clusters, each microservice and the Acumos components are deployed using
  a specific service and related deployment spec.
* services which require external exposure on the cluster are provided nodePort
  assignments. These include:
  * simple solution microservices, to expose its protobuf API
  * for composite solutions, the databroker (for its API) and probe (for its UI)

Example of the generated solution.yaml template for a simple solution:

.. code-block:: yaml

  apiVersion: v1
  kind: Service
  metadata:
    namespace: acumos
    name: mymodel-service
  spec:
    selector:
      app: mymodel
    type: nodePort
    ports:
    - name: protobuf-api
      nodePort: 33330
      port: 3330
      targetPort: 3330
  ---
  kind: Deployment
  metadata:
    namespace: acumos
    name: mymodel
    labels:
      app: mymodel
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: mymodel
    template:
      metadata:
        labels:
          app: mymodel
      spec:
        containers:
        - name: mymodel
          image: acumos.example.com:35001/mymodel
          ports:
          - name: protobuf-api
            containerPort: 3330

Example of the generated solution.yaml template for a complex (composite)
solution with two model microservices, databroker, modelconnector, and probe:


.. code-block:: yaml

  apiVersion: v1
  kind: Service
  metadata:
    namespace: acumos
    name: databroker
  spec:
    selector:
      app: databroker
    type: nodePort
    ports:
    - name: databroker-api
      nodePort: 33330
      port: 8556
      targetPort: 8556
  ---
  kind: Deployment
  metadata:
    namespace: acumos
    name: databroker
    labels:
      app: databroker
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: databroker
    template:
      metadata:
        labels:
          app: databroker
      spec:
        imagePullSecrets:
        - name: acumos-registry
        containers:
        - name: databroker
          image: nexus3.acumos.org:10004/csvdatabroker
          ports:
          - containerPort: 8556
          volumeMounts:
          - mountPath: /var/acumos/datasource
            name: datasource
        restartPolicy: Always
        volumes:
        - name: datasource
          hostPath:
            path: /var/acumos/datasource
  ---
  kind: Service
  metadata:
    namespace: acumos
    name: probe-ui
  spec:
    selector:
      app: probe
    type: nodePort
    ports:
    - nodePort: 30800
      port: 8000
      targetPort: 8000
  ---
  kind: Deployment
  metadata:
    namespace: acumos
    name: probe
    labels:
      app: probe
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: probe
    template:
      metadata:
        labels:
          app: probe
      spec:
        imagePullSecrets:
        - name: acumos-registry
        containers:
        - name: probe
          image: nexus3.acumos.org:10004/acumos-proto-viewer
          ports:
          - name: probe-ui
            containerPort: 8000
          - name: probe-api
            containerPort: 5006
  ---
  kind: Service
  metadata:
    namespace: acumos
    name: modelconnector
  spec:
    selector:
      app: modelconnector
    type: ClusterIP
    ports:
    - name: modelconnector-api
    - port: 8555
      targetPort: 8555
  ---
  kind: Deployment
  metadata:
    namespace: acumos
    name: modelconnector
    labels:
      app: modelconnector
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: modelconnector
    template:
      metadata:
        labels:
          app: modelconnector
      spec:
        imagePullSecrets:
        - name: acumos-registry
        containers:
        - name: modelconnector
          image: nexus3.acumos.org:10004/blueprint-orchestrator
          ports:
          - name: modelconnector-api
            containerPort: 8555
          volumeMounts:
          - mountPath: /var/acumos/
            name: proto-files
        restartPolicy: Always
        volumes:
        - name: proto-files
          hostPath:
            path: /var/acumos/
  ---
  kind: Service
  metadata:
    namespace: acumos
    name: mymodel1
  spec:
    selector:
      app: mymodel1
    type: ClusterIP
    ports:
    - port: 8557
      targetPort: 8557
  ---
  kind: Deployment
  metadata:
    namespace: acumos
    name: mymodel1
    labels:
      app: mymodel1
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: mymodel1
    template:
      metadata:
        labels:
          app: mymodel1
      spec:
        imagePullSecrets:
        - name: acumos-registry
        containers:
        - name: mymodel1
          image: acumos.example.com:35001/mymodel1
          ports:
          - containerPort: 8557
        restartPolicy: Always
  ---
  kind: Service
  metadata:
    namespace: acumos
    name: mymodel2
  spec:
    selector:
      app: mymodel2
    type: ClusterIP
    ports:
    - port: 8558
      targetPort: 8558
  ---
  kind: Deployment
  metadata:
    namespace: acumos
    name: mymodel2
    labels:
      app: mymodel2
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: mymodel2
    template:
      metadata:
        labels:
          app: mymodel2
      spec:
        imagePullSecrets:
        - name: acumos-registry
        containers:
        - name: mymodel2
          image: acumos.example.com:35001/mymodel2
          ports:
          - containerPort: 8558
        restartPolicy: Always

............
docker-proxy
............

The docker-proxy service of the Acumos platform will provide a simple
user-authenticating frontend (reverse proxy) for the docker registry configured
as part of the Acumos platform. The docker-proxy service may be based upon nginx
as described at https://docs.docker.com/v17.09/registry/recipes/nginx/.

The docker-proxy will provide only a docker login service and image download
service for docker pull requests, as below:

* upon a docker login request, invoke the auth/jwtToken API of the Acumos
  portal, with the username and password provided in the docker login request
* if the auth/jwtToken API returns success, accept the user login and return
  an authentication token for the user, otherwise return an authentication error
* upon a docker pull request, if there is a valid authentication token, attempt
  to retrieve the requested image from the Acumos platform docker registry, and
  return the result to the requester

To support the use of self-signed certificates for the docker-proxy, deploy.sh
will configure docker on the kubernetes master to support the specified
docker proxy as an insecure registry, i.e. one in which a self-signed
certificate will be accepted, if provided.

..............
setup_k8s.sh
..............

setup_k8s.sh is a tool allowing the user to install a basic single-or-multinode
kubernetes cluster. It will install kubernetes prerequisites and core services
via the following actions:

* install the latest docker-ce version
* install the latest stable kubernetes version (currently 1.10.0)
* initialize the kubernetes master node
* install calico as CNI
* setup kubernetes worker nodes if the user selected more than one target node

As future needs arise, the kubernetes cluster setup will be extended with
helm as deployment tool, and persistent volume support via ceph.

.........
deploy.sh
.........

After the user has transferred solution.zip to the deployment host and unzipped
it, deploy.sh will be invoked by the user from a shell session on the deployment
host, using the example command:

.. code-block:: shell

  bash deploy.sh <acumos username> <acumos password> <datasource>

where:

* <acumos username> is the user's account username on the Acumos platform
* <acumos password> is the user's account password on the Acumos platform
* <datasource> is where the databroker will be instructed to obtain data to
  feed into the solution, and can be a file path or a URL

deploy.sh will then take the following actions to deploy the solution:

* retrieve the hostname:port of the Acumos platform docker proxy from the
  solution.yaml, using the "image" attribute of any model microservice
* if not already configured, configure the docker service to allow access to the
  Acumos platform docker proxy as an insecure registry.
* login to the Acumos platform docker proxy using the Acumos platform username
  and password provided by the user
* login to the Acumos project docker registry (current credentials are provided
  as default values in deploy.sh)
* copy the subfolders under "microservice" from the unpacked solution.zip to
  /var/acumos
* update databroker.json per the datasource selected by the user

  * if the user provided a file path as datasource, replace the hostpath
    attribute of the databroker deployment in solution.yaml with the
    user-provided file path, replace the "local_system_data_file_path" attribute
    in databroker.json with the path "/var/acumos/datasource", and set the
    "target_system_url" attribute to "" 
  * if the user provided a URL as datasource, set the "target_system_url"
    attribute in databroker.json to the URL, and set the
    "local_system_data_file_path" attribute to ""

* create a namespace "acumos" using kubectl
* create a secret "acumos-registry" using ~/.docker/config.json
* invoke kubectl to deploy the services and deployments in solution.yaml
* monitor the status of the databroker service and deployment, and when they are
  running, send databroker.json to the databroker via its /configDB API
* monitor the status of all other services and deployments, and when they are
  running

  * create dockerinfo.json with the service name, assigned IP address, and
    port of each service defined in solution.yaml
  * send dockerinfo.json to the modelconnector service via the /putDockerInfo
    API
  * send blueprint.json to the modelconnector service via the /putBlueprint API
