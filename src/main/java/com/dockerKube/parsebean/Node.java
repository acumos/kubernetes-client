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

package com.dockerKube.parsebean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Node implements Serializable {

	@JsonProperty("container_name")
	private String container = null;
	@JsonProperty("node_type")
	private String nodeType = null;
	@JsonProperty("image")
	private String image = null;
	@JsonProperty("proto_uri")
	private String protoUri = null;
	@JsonProperty("operation_signature_list")
	private ArrayList<OperationSignatureList> operationSignatureList = null; // OperationSignatureList itself is NOT a
																				// Arraylist.
	@JsonProperty("script")
	private String script = null;
	@JsonProperty("mapping_table")
	private MappingTable mappingTable = null;
	@JsonProperty("data_sources")
	private List<DataSource> dataSources = null;
	
	@JsonProperty("data_broker_map")
	private DataBrokerMap dataBrokerMap;

	/**
	 * Standard POJO no-arg constructor
	 */
	public Node() {
		super();
	}

	public Node(String container, String nodeType, String image, String protoUri,
			ArrayList<OperationSignatureList> operationSignatureList, String script, MappingTable mappingTable,
			List<DataSource> dataSources,DataBrokerMap dataBrokerMap) {
		super();
		this.container = container;
		this.nodeType = nodeType;
		this.image = image;
		this.protoUri = protoUri;
		this.operationSignatureList = operationSignatureList;
		this.script = script;
		this.mappingTable = mappingTable;
		this.dataSources = dataSources;
		this.dataBrokerMap = dataBrokerMap;
	}

	@JsonProperty("container_name")
	public String getContainerName() {
		return container;
	}

	@JsonProperty("container_name")
	public void setContainerName(String container) {
		this.container = container;
	}

	@JsonProperty("node_type")
	public String getNodeType() {
		return nodeType;
	}

	@JsonProperty("node_type")
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	@JsonProperty("image")
	public String getImage() {
		return image;
	}

	@JsonProperty("image")
	public void setImage(String image) {
		this.image = image;
	}

	@JsonProperty("proto_uri")
	public String getProtoUri() {
		return protoUri;
	}

	@JsonProperty("proto_uri")
	public void setProtoUri(String protoUri) {
		this.protoUri = protoUri;
	}

	@JsonProperty("operation_signature_list")
	public ArrayList<OperationSignatureList> getOperationSignatureList() {
		return operationSignatureList;
	}

	@JsonProperty("operation_signature_list")
	public void setOperationSignatureList(ArrayList<OperationSignatureList> operationSignatureList) {
		this.operationSignatureList = operationSignatureList;
	}

	@JsonProperty("script")
	public String getScript() {
		return script;
	}

	@JsonProperty("script")
	public void setScript(String script) {
		this.script = script;
	}

	@JsonProperty("mapping_table")
	public MappingTable getMappingTable() {
		return mappingTable;
	}

	@JsonProperty("mapping_table")
	public void setMappingTable(MappingTable mappingTable) {
		this.mappingTable = mappingTable;
	}

	@JsonProperty("data_sources")
	public List<DataSource> getDataSources() {
		return dataSources;
	}

	@JsonProperty("data_sources")
	public void setDataSources(List<DataSource> dataSources) {
		this.dataSources = dataSources;
	}
	@JsonProperty("data_broker_map")
	public DataBrokerMap getDataBrokerMap() {
		return dataBrokerMap;
	}

	@JsonProperty("data_broker_map")
	public void setDataBrokerMap(DataBrokerMap dataBrokerMap) {
		this.dataBrokerMap = dataBrokerMap;
	}

	@Override
	public String toString() {
		return "Node [container=" + container + ", image=" + image + ", protoUri=" + protoUri + ", nodeType=" + nodeType
				+ "]";
	}

}