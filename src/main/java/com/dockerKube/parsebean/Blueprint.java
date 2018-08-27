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

public class Blueprint implements Serializable {

	private static final long serialVersionUID = -8199926375109170778L;

	public Blueprint() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Blueprint(String name, String version, List<InputPort> inputPorts, List<Node> nodes,
			ArrayList<ProbeIndicator> probeIndicator, List<TrainingClient> trainingClients) {
		super();
		this.name = name;
		this.version = version;
		this.inputPorts = inputPorts;
		this.nodes = nodes;
		this.probeIndicator = probeIndicator;
		this.trainingClients = trainingClients;
	}
	
	

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("version")
	private String version = null;

	@JsonProperty("nodes")
	private List<Node> nodes = null;

	/*@JsonProperty("input_operation_signatures")
	private List<OperationSignature> inputs = null;

	@JsonProperty("orchestrator")
	private Orchestrator orchestrator = null;*/
	
	
	/*@JsonProperty("probeIndocator")
	private String probeIndocator = null; */
	
	@JsonProperty("probeIndicator")
	private ArrayList<ProbeIndicator> probeIndicator = null;
	
	@JsonProperty("input_ports")
	private List<InputPort> inputPorts = null;
	
	
	@JsonProperty("training_clients")
	private List<TrainingClient> trainingClients = null;
	

	/*public ArrayList<ProbeIndicator> getProbeIndocator() {
		return probeIndicator;
	}

	public void setProbeIndocator(ArrayList<ProbeIndicator> probeIndicatorLst) {
		this.probeIndicator = probeIndicatorLst;
	}*/

	public List<InputPort> getInputPorts() {
		return inputPorts;
	}

	public void setInputPorts(List<InputPort> inputPorts) {
		this.inputPorts = inputPorts;
	}

	public ArrayList<ProbeIndicator> getProbeIndicator() {
		return probeIndicator;
	}

	public void setProbeIndicator(ArrayList<ProbeIndicator> probeIndicator) {
		this.probeIndicator = probeIndicator;
	}

	/*public List<OperationSignature> getInputs() {
		return inputs;
	}

	public void setInputs(List<OperationSignature> inputs) {
		this.inputs = inputs;
	}*/

	/*public Blueprint addInput(OperationSignature os) {
		if (this.inputs == null) {
			this.inputs = new ArrayList<OperationSignature>();
		}
		this.inputs.add(os);
		return this;
	}*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/*public Orchestrator getOrchestrator() {
		return orchestrator;
	}

	public void setOrchestrator(Orchestrator orchestrator) {
		this.orchestrator = orchestrator;
	}*/

	public List<Node> getNodes() {
		return nodes;
	}

	public Node getNodebyContainer(String container) {
		for (Node node : nodes) {
			if (node.getContainerName().equalsIgnoreCase(container))
				return node;
		}
		return null;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public Blueprint addNode(Node node) {
		if (this.nodes == null) {
			this.nodes = new ArrayList<Node>();
		}
		this.nodes.add(node);
		return this;
	}

	@Override
	public String toString() {

		return "Blueprint [name=" + name + ", version=" + version + ", nodes=" + nodes + "]";
	}

}
