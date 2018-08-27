package com.dockerKube.beans;

public class ContainerBean {
	private String containerName; 
	private String nodeType;
	private String image;
	private String protoUriPath;
	private String protoUriDetails;
	
	public String getContainerName() {
		return containerName;
	}
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getProtoUriPath() {
		return protoUriPath;
	}
	public void setProtoUriPath(String protoUriPath) {
		this.protoUriPath = protoUriPath;
	}
	public String getProtoUriDetails() {
		return protoUriDetails;
	}
	public void setProtoUriDetails(String protoUriDetails) {
		this.protoUriDetails = protoUriDetails;
	}
	
	

}
