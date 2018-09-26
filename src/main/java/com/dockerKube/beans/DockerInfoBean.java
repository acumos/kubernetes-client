package com.dockerKube.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DockerInfoBean {

	@JsonProperty("container_name")
	private String container = null; 
	
	@JsonProperty("ip_address")
	private String ipAddress = null;
	
	@JsonProperty("port")
	private String port = null;
	
	/*@JsonProperty("node_type")
	private String nodeType = null;*/

	/**
	 * Constructor class
	 * 
	 * @param container
	 *            Container
	 * @param ipAddress
	 *            IP address
	 * @param port
	 *            Port number
	 */

	public DockerInfoBean(String container, String ipAddress, String port) {
		super();
		this.container = container;
		this.ipAddress = ipAddress;
		this.port = port;
		//this.nodeType = nodeType;
	}

	/**
	 * Standard POJO no-arg constructor
	 */
	public DockerInfoBean() {
		super();
	}
    
	
	
	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "DockerInfo [container=" + container + ", ipAddress=" + ipAddress + ", port=" + port + "]";
	}
}
