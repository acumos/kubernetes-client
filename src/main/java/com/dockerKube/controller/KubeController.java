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
package com.dockerKube.controller;


import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dockerKube.beans.DeploymentBean;
import com.dockerKube.service.KubeService;
import com.dockerKube.utils.DockerKubeConstants;


@RestController
public class KubeController {
	
	@Autowired
	private Environment env;
	
	@Autowired
	KubeService kubeService;
	
	Logger log = LoggerFactory.getLogger(KubeController.class);
	
	
	@RequestMapping(value = "/getSolutionZip/{solutionId}/{revisionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void getSolutionZip(@PathVariable("solutionId") String solutionId, @PathVariable("revisionId") String revisionId,HttpServletResponse response) throws IOException {
		 log.debug("Start getSolutionZip solutionId"+solutionId+" revisionId "+revisionId);
		 DeploymentBean dBean=new DeploymentBean();
		 byte[] solutionZip=null;
		 String singleModelPort=(env.getProperty(DockerKubeConstants.SINGLE_MODEL_PORT) != null) ? env.getProperty(DockerKubeConstants.SINGLE_MODEL_PORT) : "";
		 String singleNodePort=(env.getProperty(DockerKubeConstants.SINGLE_NODE_PORT) != null) ? env.getProperty(DockerKubeConstants.SINGLE_NODE_PORT) : "";
		 String singleTargetPort=(env.getProperty(DockerKubeConstants.SINGLE_TARGET_PORT) != null) ? env.getProperty(DockerKubeConstants.SINGLE_TARGET_PORT) : "";
		 String nexusUrl=(env.getProperty(DockerKubeConstants.NEXUS_URL_PROP) != null) ? env.getProperty(DockerKubeConstants.NEXUS_URL_PROP) : "";
	   	 String nexusUsername=(env.getProperty(DockerKubeConstants.NEXUS_USERNAME_PROP) != null) ? env.getProperty(DockerKubeConstants.NEXUS_USERNAME_PROP) : "";
	   	 String nexusPd=(env.getProperty(DockerKubeConstants.NEXUS_PD_PROP) != null) ? env.getProperty(DockerKubeConstants.NEXUS_PD_PROP) : "";
	   	 String cmnDataUrl=(env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCENDPOINTURL_PROP) != null) ? env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCENDPOINTURL_PROP) : "";
	   	 String cmnDataUser=(env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCUSER_PROP) != null) ? env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCUSER_PROP) : "";
	   	 String cmnDataPd=(env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCPD_PROP) != null) ? env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCPD_PROP) : "";
	   	 String bluePrintImage=(env.getProperty(DockerKubeConstants.BLUEPRINT_IMAGENAME_PROP)!= null) ?env.getProperty(DockerKubeConstants.BLUEPRINT_IMAGENAME_PROP):"";
	   	 String bluePrintName=(env.getProperty(DockerKubeConstants.BLUEPRINT_NAME_PROP)!= null) ?env.getProperty(DockerKubeConstants.BLUEPRINT_NAME_PROP):"";
	   	 String bluePrintPort=(env.getProperty(DockerKubeConstants.BLUEPRINT_PORT_PROP)!= null) ?env.getProperty(DockerKubeConstants.BLUEPRINT_PORT_PROP):"";
	   	 String kubeIP=(env.getProperty(DockerKubeConstants.KUBE_IP)!= null) ?env.getProperty(DockerKubeConstants.KUBE_IP):"";
	   	 String probeImage=(env.getProperty(DockerKubeConstants.PROBEIMAGE_NAME)!= null) ?env.getProperty(DockerKubeConstants.PROBEIMAGE_NAME):"";
	   	 String probePort=(env.getProperty(DockerKubeConstants.PROBEIMAGE_PORT)!= null) ?env.getProperty(DockerKubeConstants.PROBEIMAGE_PORT):"";
	   	 String incrementPort=(env.getProperty(DockerKubeConstants.INCREMENT_PORT)!= null) ?env.getProperty(DockerKubeConstants.INCREMENT_PORT):"";
	   	 String folderPath=(env.getProperty(DockerKubeConstants.FOLDERPATH) != null) ? env.getProperty(DockerKubeConstants.FOLDERPATH) : "";
	   	 String bluePrintNodePort=(env.getProperty(DockerKubeConstants.BLUEPRINT_NODEPORT_PROP)!= null) ?env.getProperty(DockerKubeConstants.BLUEPRINT_NODEPORT_PROP):"";
	   	 String mlTargetPort=(env.getProperty(DockerKubeConstants.ML_TARGET_PORT)!= null) ?env.getProperty(DockerKubeConstants.ML_TARGET_PORT):"";
	   	 String dataBrokerModelPort=(env.getProperty(DockerKubeConstants.DATABROKER_MODEL_PORT) != null) ? env.getProperty(DockerKubeConstants.DATABROKER_MODEL_PORT) : "";
		 String dataBrokerNodePort=(env.getProperty(DockerKubeConstants.DATABROKER_NODE_PORT) != null) ? env.getProperty(DockerKubeConstants.DATABROKER_NODE_PORT) : "";
		 String dataBrokerTargetPort=(env.getProperty(DockerKubeConstants.DATABROKER_TARGET_PORT) != null) ? env.getProperty(DockerKubeConstants.DATABROKER_TARGET_PORT) : "";
		 
		 String probeModelPort=(env.getProperty(DockerKubeConstants.PROBE_MODEL_PORT) != null) ? env.getProperty(DockerKubeConstants.PROBE_MODEL_PORT) : "";
		 String probeNodePort=(env.getProperty(DockerKubeConstants.PROBE_NODE_PORT) != null) ? env.getProperty(DockerKubeConstants.PROBE_NODE_PORT) : "";
		 String probeTargetPort=(env.getProperty(DockerKubeConstants.PROBE_TARGET_PORT) != null) ? env.getProperty(DockerKubeConstants.PROBE_TARGET_PORT) : "";
		 String probeApiPort=(env.getProperty(DockerKubeConstants.PROBE_API_PORT) != null) ? env.getProperty(DockerKubeConstants.PROBE_API_PORT) : "";
		 
	   	 dBean.setSolutionId(solutionId);
    	 dBean.setSolutionRevisionId(revisionId);
    	 dBean.setNexusUrl(nexusUrl);
    	 dBean.setNexusUserName(nexusUsername);
    	 dBean.setNexusPd(nexusPd);
    	 dBean.setCmnDataUrl(cmnDataUrl);
    	 dBean.setCmnDataUser(cmnDataUser);
    	 dBean.setCmnDataPd(cmnDataPd);
    	 dBean.setBluePrintImage(bluePrintImage);
    	 dBean.setBluePrintName(bluePrintName);
    	 dBean.setBluePrintPort(bluePrintPort);
    	 dBean.setKubeIP(kubeIP);
    	 dBean.setProbeImage(probeImage);
    	 dBean.setProbePort(probePort);
    	 dBean.setIncrementPort(incrementPort);
    	 dBean.setFolderPath(folderPath);
    	 dBean.setSingleModelPort(singleModelPort);
    	 dBean.setSingleNodePort(singleNodePort);
    	 dBean.setSingleTargetPort(singleTargetPort);
    	 dBean.setBluePrintNodePort(bluePrintNodePort);
    	 dBean.setMlTargetPort(mlTargetPort);
    	 dBean.setDataBrokerModelPort(dataBrokerModelPort);
    	 dBean.setBluePrintNodePort(bluePrintNodePort);
    	 dBean.setDataBrokerTargetPort(dataBrokerTargetPort);
    	 dBean.setProbeModelPort(probeModelPort);
    	 dBean.setProbeNodePort(probeNodePort);
    	 dBean.setDataBrokerTargetPort(dataBrokerTargetPort);
    	 dBean.setProbeApiPort(probeApiPort);
    	 log.debug("probeModelPort "+probeModelPort);
    	 log.debug("probeNodePort "+probeNodePort);
    	 log.debug("probeTargetPort "+probeTargetPort);
    	 log.debug("probeApiPort "+probeApiPort);
    	 log.debug("dataBrokerModelPort "+dataBrokerModelPort);
    	 log.debug("dataBrokerNodePort "+dataBrokerNodePort);
    	 log.debug("dataBrokerTargetPort "+dataBrokerTargetPort);
    	 log.debug("bluePrintNodePort "+bluePrintNodePort);
	   	 log.debug("nexusUrlnexusUrl "+nexusUrl);
	   	 log.debug("solutionId "+solutionId);
	   	 log.debug("revisionId "+revisionId);
	   	 log.debug("nexusUrl "+nexusUrl);
	   	 log.debug("nexusUsername "+nexusUsername);
	   	 log.debug("nexusPd "+nexusPd);
	   	 log.debug("cmnDataUrl "+cmnDataUrl);
	   	 log.debug("cmnDataUser "+cmnDataUser);
	   	 log.debug("cmnDataPd "+cmnDataPd);
	   	 log.debug("bluePrintImage "+bluePrintImage);
	   	 log.debug("bluePrintName "+bluePrintName);
	   	 log.debug("bluePrintPort "+bluePrintPort);
	   	 log.debug("KUBE_IP "+kubeIP);
	   	 log.debug("probeImage "+probeImage);
	   	 log.debug("probePort "+probePort);
	   	 log.debug("incrementPort "+incrementPort);
	   	 log.debug("folderPath "+folderPath);
	   	 log.debug("singleModelPort "+singleModelPort);
	   	 log.debug("singleNodePort "+singleNodePort);
	   	 log.debug("singleTargetPort "+singleTargetPort);
	   	try{
	   	  String solutionToolKitType=kubeService.getSolutionCode(solutionId, cmnDataUrl, cmnDataUser, cmnDataPd);
	   	  if(solutionToolKitType!=null && !"".equals(solutionToolKitType) && !"CP".equalsIgnoreCase(solutionToolKitType)){
	   		log.debug("Single Solution Details Start");
	   		String imageTag=kubeService.getSingleImageData(solutionId, revisionId, cmnDataUrl, cmnDataUser, cmnDataPd);
	   		solutionZip=kubeService.singleSolutionDetails(dBean, imageTag, singleModelPort);
	   		log.debug("Single Solution Details End");
	   	  }else{
	   		log.debug("Composite Solution Details Start");
	   		solutionZip=kubeService.compositeSolutionDetails(dBean);
	   		log.debug("Composite Solution Deployment End");
	   	  }
	   	}catch(Exception e){
			log.error("getSolutionZip failed", e);
		}
	    response.setHeader("Content-Disposition", "attachment; filename=solution.zip");
	    response.getOutputStream().write(solutionZip);
	   	log.debug("End getSolutionZip");
	}
 }
