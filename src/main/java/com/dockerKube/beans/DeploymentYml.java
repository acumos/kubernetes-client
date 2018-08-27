package com.dockerKube.beans;

public class DeploymentYml {
	String apiVersion;
	String kind;
	MetaData metadata;
	Specification spec;
	public String getApiVersion() {
		return apiVersion;
	}
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public MetaData getMetadata() {
		return metadata;
	}
	public void setMetadata(MetaData metadata) {
		this.metadata = metadata;
	}
	public Specification getSpec() {
		return spec;
	}
	public void setSpec(Specification spec) {
		this.spec = spec;
	}
	

}
