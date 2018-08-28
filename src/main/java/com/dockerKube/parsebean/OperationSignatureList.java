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

public class OperationSignatureList implements Serializable {
	private final static long serialVersionUID = -6436344519431883582L;

	@JsonProperty("operation_signature")
	private OperationSignature operationSignature = null;
	@JsonProperty("connected_to")
	private ArrayList<ConnectedTo> connectedTo = null;

	/**
	 * Standard POJO no-arg constructor
	 */
	public OperationSignatureList() {
		super();
	}

	/**
	 * Standard POJO constructor initialized with field
	 * 
	 * @param operationSignature
	 *            This is the operation signature
	 * @param connectedTo
	 *            This is the connected to for an operation signature.
	 */
	public OperationSignatureList(OperationSignature operationSignature, ArrayList<ConnectedTo> connectedTo) {
		super();
		this.operationSignature = operationSignature;
		this.connectedTo = connectedTo;
	}

	@JsonProperty("operation_signature")
	public OperationSignature getOperationSignature() {
		return operationSignature;
	}

	@JsonProperty("operation_signature")
	public void setOperationSignature(OperationSignature operationSignature) {
		this.operationSignature = operationSignature;
	}

	@JsonProperty("connected_to")
	public ArrayList<ConnectedTo> getConnectedTo() {
		return connectedTo;
	}

	@JsonProperty("connected_to")
	public void setConnectedTo(ArrayList<ConnectedTo> connectedTo) {
		this.connectedTo = connectedTo;
	}

	@Override
	public String toString() {

		return "OperationSignatureList [operationSignature=" + operationSignature + ", connectedTo=" + connectedTo
				+ "]";
	}
}
