/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package com.dockerKube.beans;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DockerInfo implements Serializable {

	private static final long serialVersionUID = 6384817901582893495L;
 
	@JsonProperty("docker_info_list")
	private List<DockerInfoBean> dockerInfolist;

	public List<DockerInfoBean> getDockerInfolist() {
		return dockerInfolist;
	}

	public void setDockerInfolist(List<DockerInfoBean> dockerInfolist) {
		this.dockerInfolist = dockerInfolist;
	}
	
}


