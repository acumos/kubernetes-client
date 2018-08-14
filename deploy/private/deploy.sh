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
#.   a specific solution/revision, and unzip the package into a folder.
#. Usage:
#. - bash deploy.sh <user> <pass> <datasource>
#.   user: username on the Acumos platform
#.   pass: password on the Acummos platform
#.   datasource: file path or URL of data source for databroker


