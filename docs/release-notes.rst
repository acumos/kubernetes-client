.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
.. http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================

===============================
Kubernetes Client Release Notes
===============================

...........................
Kubernetes-Client Component
...........................
-------------------------------
Version 2.0.0, 28 January 2019
---------------------------------
* update CDS 2.0.4 for kubernetes-client (`ACUMOS-2412 <https://jira.acumos.org/browse/ACUMOS-2412>`_)

-----------------------------
Version 1.1.0, 12 October 2018
-----------------------------
* ACUMOS-1884: Download solution package for deploy to local does not work for simple solution in K8S <https://jira.acumos.org/browse/ACUMOS-1884>'

-----------------------------
Version 1.0.0, 5 October 2018
-----------------------------

Released version as of Release Candidate 0 (RC0).

* `ACUMOS-1792: K8S Client Fixes needed in solution.yaml <https://jira.acumos.org/browse/ACUMOS-1792>`_

  * `update version for greater than 1 <https://gerrit.acumos.org/r/#/c/3071/>`_
  * `Add double quates in value filed <https://gerrit.acumos.org/r/#/c/3012/>`_
  * `Add Nginx detail in solution yaml file <https://gerrit.acumos.org/r/#/c/2971/>`_

* `ACUMOS-1783: K8S client dockerinfo is missing docker_info_list wrapper element <https://jira.acumos.org/browse/ACUMOS-1783>`_

  * `Add dockerproxy and dockerport for kubernetes <https://gerrit.acumos.org/r/#/c/2952/>`_

* `ACUMOS-1782: K8S client Replace nexus docker host:port with docker-proxy host:port <https://jira.acumos.org/browse/ACUMOS-1782>`_

  * `changes in dockerinfo file <https://gerrit.acumos.org/r/#/c/2953/>`_

* `ACUMOS-1382: Create k8s (Kubernetes) template from Blueprint <https://jira.acumos.org/browse/ACUMOS-1382>`_

  * `Updated CDS 1.18.2 version <https://gerrit.acumos.org/r/#/c/2930/>`_

* `ACUMOS-1289: Deploy model to Kubernetes Environment <https://jira.acumos.org/browse/ACUMOS-1289>`_

  * `Update release notes <https://gerrit.acumos.org/r/3039>`_
  * `Update release notes  <https://gerrit.acumos.org/r/#/c/3076/>`_

--------------------------------
Version 0.0.9, 24 September 2018
--------------------------------

Released version as of code freeze (M4).

Initial release of the kubernetes-client component, per the design for
`Acumos Solution Deployment in Private Kubernetes Cluster <https://docs.acumos.org/en/latest/submodules/kubernetes-client/docs/deploy-in-private-k8s.html>`_.

* `ACUMOS-1289: Deploy model to Kubernetes Environment <https://jira.acumos.org/browse/ACUMOS-1289>`_

  * `Add probe details in kubernetes-client <https://gerrit.acumos.org/r/#/c/2905/>`_
  * `Add condition for single and composite sol <https://gerrit.acumos.org/r/#/c/2893/>`_
  * `Create getsolutionZip api for single and composite <https://gerrit.acumos.org/r/#/c/2888/>`_
  * `Add solution yml in project <https://gerrit.acumos.org/r/#/c/2867/>`_
  * `Set path for environment variables <https://gerrit.acumos.org/r/#/c/2827/>`_
  * `Add maven path in kubernetes-client <https://gerrit.acumos.org/r/#/c/2823/>`_
  * `Added path of file used in kubernetes-client <https://gerrit.acumos.org/r/#/c/2770/>`_

* `Add Kubernetes-client code for private kubernetes <https://gerrit.acumos.org/r/#/c/2674/>`_

  * `ACUMOS-1383 <https://jira.acumos.org/browse/ACUMOS-1383>`_
  * Baseline of kubernetes-client component

...........................................
Private-Kubernetes-Deployment Support Tools
...........................................

------------------------------
Version 1.0.0, 11 October 2018
------------------------------

* `ACUMOS-1893: test-model.sh doesn't work with simple model <https://jira.acumos.org/browse/ACUMOS-1893>`_

  * `Fix test-model.sh for simple models <https://gerrit.acumos.org/r/#/c/3156/>`_

    * Fix test-model.sh for simple models
    * Fix deployment and testing in Centos 7 hosts


* `ACUMOS-1289: Deploy model to Kubernetes Environment <https://jira.acumos.org/browse/ACUMOS-1289>`_

  * `Fix doc link format <https://gerrit.acumos.org/r/#/c/3126/>`_
  * `Update user guide per RC0 version <https://gerrit.acumos.org/r/#/c/3099/>`_

---------------------------
Version 0.2, 4 October 2018
---------------------------

Released version as of Release Candidate 0 (RC0).

* `ACUMOS-1289: Deploy model to Kubernetes Environment <https://jira.acumos.org/browse/ACUMOS-1289>`_

  * `Update release notes <https://gerrit.acumos.org/r/3039>`_
  * `Align design doc with current implementation <https://gerrit.acumos.org/r/#/c/2940/>`_

    * Patch dockerinfo.json as workaround for `ACUMOS-1791 <https://jira.acumos.org/browse/ACUMOS-1791>`_

------------------------------
Version 0.1, 22 September 2018
------------------------------

Released version as of code freeze (M4).

Initial release of support tools per the design for
`Acumos Solution Deployment in Private Kubernetes Cluster <https://docs.acumos.org/en/latest/submodules/kubernetes-client/docs/deploy-in-private-k8s.html>`_.

* `ACUMOS-1289: Deploy model to Kubernetes Environment <https://jira.acumos.org/browse/ACUMOS-1289>`_

  * `Add release notes for support tools <https://gerrit.acumos.org/r/#/c/2921/>`_

  * `Aligned with initial kubernetes-client version <https://gerrit.acumos.org/r/#/c/2918/>`_

    * Add Helm, Prometheus, Grafana setup tools
    * Add sample Grafana dashboards
    * Add Data Broker support
    * Align design document with final code freeze version

  * `Updates for deploy testing <https://gerrit.acumos.org/r/#/c/2596/>`_

    * Fix code block rendering
    * Fix and further explain solution.yaml examples
    * Add modelconnector API calls
    * Add test-model.sh

  * `Fix readme <https://gerrit.acumos.org/r/#/c/2670/>`_
  * `Fix firstop parameter use <https://gerrit.acumos.org/r/#/c/2655/>`_
  * `Baseline of private-k8s-deployment <https://gerrit.acumos.org/r/#/c/2537/>`_

    * deploy.sh: main deployment script
    * setup_k8s.sh: kubernetes cluster setup script
    * deploy-in-private-k8s.rst: design document
