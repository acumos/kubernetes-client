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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of Operation Signature list of a Node. IMPORTANT: This itself
 * is NOT an Arraylist.
 */

public class DataBrokerMap implements Serializable {
	
	
	private static final long serialVersionUID = -5950222666895492477L;

	@JsonProperty("map_inputs")
	private ArrayList<MapInputs> mapInputs = null;

	@JsonProperty("data_broker_type")
    private String dataBrokerType = null;

	@JsonProperty("csv_file_field_separator")
    private String csvFileFieldSeparator = null;

	@JsonProperty("target_system_url")
    private String targetSystemUrl = null;

    @JsonProperty("map_outputs")
    private ArrayList<MapOutputs> mapOutputs = null;

    @JsonProperty("local_system_data_file_path")
    private String localSystemDataFilePath = null;

    @JsonProperty("first_row")
    private String firstRow = null;

    @JsonProperty("script")
    private String script = null;

    
    public DataBrokerMap(ArrayList<MapInputs> mapInputs, String dataBrokerType, String csvFileFieldSeparator,
			String targetSystemUrl, ArrayList<MapOutputs> mapOutputs, String localSystemDataFilePath, String firstRow,
			String script) {
		super();
		this.mapInputs = mapInputs;
		this.dataBrokerType = dataBrokerType;
		this.csvFileFieldSeparator = csvFileFieldSeparator;
		this.targetSystemUrl = targetSystemUrl;
		this.mapOutputs = mapOutputs;
		this.localSystemDataFilePath = localSystemDataFilePath;
		this.firstRow = firstRow;
		this.script = script;
	}

    public DataBrokerMap(){
    	
    }
	public String getDataBrokerType() {
		return dataBrokerType;
	}


	public void setDataBrokerType(String dataBrokerType) {
		this.dataBrokerType = dataBrokerType;
	}


	public String getCsvFileFieldSeparator() {
		return csvFileFieldSeparator;
	}


	public void setCsvFileFieldSeparator(String csvFileFieldSeparator) {
		this.csvFileFieldSeparator = csvFileFieldSeparator;
	}


	public String getTargetSystemUrl() {
		return targetSystemUrl;
	}


	public void setTargetSystemUrl(String targetSystemUrl) {
		this.targetSystemUrl = targetSystemUrl;
	}

	public String getLocalSystemDataFilePath() {
		return localSystemDataFilePath;
	}


	public void setLocalSystemDataFilePath(String localSystemDataFilePath) {
		this.localSystemDataFilePath = localSystemDataFilePath;
	}


	public String getFirstRow() {
		return firstRow;
	}


	public void setFirstRow(String firstRow) {
		this.firstRow = firstRow;
	}


	public String getScript() {
		return script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}


	public ArrayList<MapInputs> getMapInputs() {
		return mapInputs;
	}


	public void setMapInputs(ArrayList<MapInputs> mapInputs) {
		this.mapInputs = mapInputs;
	}


	public ArrayList<MapOutputs> getMapOutputs() {
		return mapOutputs;
	}


	public void setMapOutputs(ArrayList<MapOutputs> mapOutputs) {
		this.mapOutputs = mapOutputs;
	}	
	

	@Override
    public String toString()
    {
        return "ClassPojo [map_inputs = "+mapInputs+", data_broker_type = "+dataBrokerType+", csv_file_field_separator = "+csvFileFieldSeparator+", target_system_url = "+targetSystemUrl+", map_outputs = "+mapOutputs+", local_system_data_file_path = "+localSystemDataFilePath +", first_row = "+firstRow+", script = "+script +"]";
    }
	
}