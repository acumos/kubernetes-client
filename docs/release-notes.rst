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

--------------------------------
Version 0.0.7, 21 September 2018
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
Version 0.1, 22 September 2018
------------------------------

Released version as of code freeze (M4).

Initial release of support tools per the design for
`Acumos Solution Deployment in Private Kubernetes Cluster <https://docs.acumos.org/en/latest/submodules/kubernetes-client/docs/deploy-in-private-k8s.html>`_.

* `ACUMOS-1289: Deploy model to Kubernetes Environment <https://jira.acumos.org/browse/ACUMOS-1289>`_

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