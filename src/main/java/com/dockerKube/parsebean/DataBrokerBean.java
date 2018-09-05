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

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataBrokerBean {
	
	@JsonProperty("data_broker_map")
    private DataBrokerMap dataBrokerMap = null;
	
	@JsonProperty("userName")
	private String userName=null;
	
	@JsonProperty("password")
	private String password=null;
	
	@JsonProperty("host")
	private String host=null;
	
	@JsonProperty("port")
	private String port=null;
	
	@JsonProperty("protobufFile")
	private String protobufFile=null;

	public DataBrokerMap getDataBrokerMap() {
		return dataBrokerMap;
	}

	public void setDataBrokerMap(DataBrokerMap dataBrokerMap) {
		this.dataBrokerMap = dataBrokerMap;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getProtobufFile() {
		return protobufFile;
	}

	public void setProtobufFile(String protobufFile) {
		this.protobufFile = protobufFile;
	}
	
	
	
	

}
