package com.dockerKube.beans;

public class Template {
	TemplateMetaData metadata;
	TemplateSpecification spec;
	public TemplateMetaData getMetadata() {
		return metadata;
	}
	public void setMetadata(TemplateMetaData metadata) {
		this.metadata = metadata;
	}
	public TemplateSpecification getSpec() {
		return spec;
	}
	public void setSpec(TemplateSpecification spec) {
		this.spec = spec;
	}
	
	
}
