package com.dockerKube.beans;

public class Containers {
	String name;
	String image;
	Ports ports;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public Ports getPorts() {
		return ports;
	}
	public void setPorts(Ports ports) {
		this.ports = ports;
	}
	
	
}
