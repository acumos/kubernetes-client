package com.dockerKube.beans;

import java.util.List;

public class Specification {
	Selector selector;
	String replicas;
	Template template;
	List<Containers> containers;
	public Selector getSelector() {
		return selector;
	}
	public void setSelector(Selector selector) {
		this.selector = selector;
	}
	public String getReplicas() {
		return replicas;
	}
	public void setReplicas(String replicas) {
		this.replicas = replicas;
	}
	public Template getTemplate() {
		return template;
	}
	public void setTemplate(Template template) {
		this.template = template;
	}
	public List<Containers> getContainers() {
		return containers;
	}
	public void setContainers(List<Containers> containers) {
		this.containers = containers;
	}
	
	

}
