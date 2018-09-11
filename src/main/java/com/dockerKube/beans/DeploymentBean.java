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

import java.util.List;

public class DeploymentBean {

	private String solutionId;
	private String solutionRevisionId;
	private String bluePrintjson;
	private String nexusUrl;
	private String nexusUserName;
	private String nexusPd;
	private String cmnDataUrl;
	private String cmnDataUser;
	private String cmnDataPd;
	private String dataBrokerJson;
	private String dockerInfoJson;
	private String solutionYml;
	private String bluePrintImage;
	private String bluePrintName;
	private String bluePrintPort;
	private String kubeIP;
	private String probeImage;
	private String probePort;
	private String nginxImage;
	private String nginxPort;
	private String incrementPort;
	private String folderPath;
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	private List<ContainerBean> containerBeanList;
	
	
	public String getIncrementPort() {
		return incrementPort;
	}
	public void setIncrementPort(String incrementPort) {
		this.incrementPort = incrementPort;
	}
	public String getProbeImage() {
		return probeImage;
	}
	public void setProbeImage(String probeImage) {
		this.probeImage = probeImage;
	}
	public String getProbePort() {
		return probePort;
	}
	public void setProbePort(String probePort) {
		this.probePort = probePort;
	}
	public String getNginxImage() {
		return nginxImage;
	}
	public void setNginxImage(String nginxImage) {
		this.nginxImage = nginxImage;
	}
	public String getNginxPort() {
		return nginxPort;
	}
	public void setNginxPort(String nginxPort) {
		this.nginxPort = nginxPort;
	}
	public String getBluePrintImage() {
		return bluePrintImage;
	}
	public void setBluePrintImage(String bluePrintImage) {
		this.bluePrintImage = bluePrintImage;
	}
	public String getBluePrintName() {
		return bluePrintName;
	}
	public void setBluePrintName(String bluePrintName) {
		this.bluePrintName = bluePrintName;
	}
	public String getBluePrintPort() {
		return bluePrintPort;
	}
	public void setBluePrintPort(String bluePrintPort) {
		this.bluePrintPort = bluePrintPort;
	}
	public String getKubeIP() {
		return kubeIP;
	}
	public void setKubeIP(String kubeIP) {
		this.kubeIP = kubeIP;
	}
	public String getSolutionId() {
		return solutionId;
	}
	public void setSolutionId(String solutionId) {
		this.solutionId = solutionId;
	}
	public String getSolutionRevisionId() {
		return solutionRevisionId;
	}
	public void setSolutionRevisionId(String solutionRevisionId) {
		this.solutionRevisionId = solutionRevisionId;
	}
	public String getBluePrintjson() {
		return bluePrintjson;
	}
	public void setBluePrintjson(String bluePrintjson) {
		this.bluePrintjson = bluePrintjson;
	}
	
	public String getNexusUrl() {
		return nexusUrl;
	}
	public void setNexusUrl(String nexusUrl) {
		this.nexusUrl = nexusUrl;
	}
	public String getNexusUserName() {
		return nexusUserName;
	}
	public void setNexusUserName(String nexusUserName) {
		this.nexusUserName = nexusUserName;
	}
	public String getNexusPd() {
		return nexusPd;
	}
	public void setNexusPd(String nexusPd) {
		this.nexusPd = nexusPd;
	}
	public String getCmnDataUrl() {
		return cmnDataUrl;
	}
	public void setCmnDataUrl(String cmnDataUrl) {
		this.cmnDataUrl = cmnDataUrl;
	}
	public String getCmnDataUser() {
		return cmnDataUser;
	}
	public void setCmnDataUser(String cmnDataUser) {
		this.cmnDataUser = cmnDataUser;
	}
	public String getCmnDataPd() {
		return cmnDataPd;
	}
	public void setCmnDataPd(String cmnDataPd) {
		this.cmnDataPd = cmnDataPd;
	}
	public List<ContainerBean> getContainerBeanList() {
		return containerBeanList;
	}
	public void setContainerBeanList(List<ContainerBean> containerBeanList) {
		this.containerBeanList = containerBeanList;
	}
	public String getDataBrokerJson() {
		return dataBrokerJson;
	}
	public void setDataBrokerJson(String dataBrokerJson) {
		this.dataBrokerJson = dataBrokerJson;
	}
	public String getDockerInfoJson() {
		return dockerInfoJson;
	}
	public void setDockerInfoJson(String dockerInfoJson) {
		this.dockerInfoJson = dockerInfoJson;
	}
	public String getSolutionYml() {
		return solutionYml;
	}
	public void setSolutionYml(String solutionYml) {
		this.solutionYml = solutionYml;
	}
	
	

}
