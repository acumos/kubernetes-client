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
	private List<ContainerBean> containerBeanList;
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
