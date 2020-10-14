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
import java.lang.invoke.MethodHandles;

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
import com.dockerKube.logging.LogConfig;
import com.dockerKube.service.KubeService;
import com.dockerKube.utils.DockerKubeConstants;


@RestController
public class KubeController {
	
	@Autowired
	private Environment env;
	
	@Autowired
	KubeService kubeService;
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	@RequestMapping(value = "/getSolutionZip/{solutionId}/{revisionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void getSolutionZip(@PathVariable("solutionId") String solutionId, @PathVariable("revisionId") String revisionId,HttpServletResponse response) throws Exception {
		 LogConfig.setEnteringMDCs("kubernetes-client","getSolutionZip");
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
		 
		 String dockerProxyHost=(env.getProperty(DockerKubeConstants.DOCKER_PROXY_HOST) != null) ? env.getProperty(DockerKubeConstants.DOCKER_PROXY_HOST) : "";
		 String dockerProxyPort=(env.getProperty(DockerKubeConstants.DOCKER_PROXY_PORT) != null) ? env.getProperty(DockerKubeConstants.DOCKER_PROXY_PORT) : "";
		 
		 String probeExternalPort=(env.getProperty(DockerKubeConstants.PROBE_EXTERNAL_PORT) != null) ? env.getProperty(DockerKubeConstants.PROBE_EXTERNAL_PORT) : "";
		 String probeSchemaPort=(env.getProperty(DockerKubeConstants.PROBE_SCHEMA_PORT) != null) ? env.getProperty(DockerKubeConstants.PROBE_SCHEMA_PORT) : "";
		 String nginxImageName=(env.getProperty(DockerKubeConstants.NGINX_IMAGE_NAME) != null) ? env.getProperty(DockerKubeConstants.NGINX_IMAGE_NAME) : "";
		 String nexusEndPointURL=(env.getProperty(DockerKubeConstants.NEXUS_END_POINTURL) != null) ? env.getProperty(DockerKubeConstants.NEXUS_END_POINTURL) : "";
		 
		 String logstashHost=(env.getProperty(DockerKubeConstants.LOGSTASH_HOST) != null) ? env.getProperty(DockerKubeConstants.LOGSTASH_HOST) : "";
		 String logstashIp=(env.getProperty(DockerKubeConstants.LOGSTASH_IP) != null) ? env.getProperty(DockerKubeConstants.LOGSTASH_IP) : "";
		 String logstashPort=(env.getProperty(DockerKubeConstants.LOGSTASH_PORT) != null) ? env.getProperty(DockerKubeConstants.LOGSTASH_PORT) : "";
		 
		 
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
    	 dBean.setDataBrokerNodePort(dataBrokerNodePort);
    	 dBean.setProbeModelPort(probeModelPort);
    	 dBean.setProbeNodePort(probeNodePort);
    	 dBean.setProbeApiPort(probeApiPort);
    	 dBean.setProbeTargetPort(probeTargetPort);
    	 dBean.setDockerProxyHost(dockerProxyHost);
    	 dBean.setDockerProxyPort(dockerProxyPort);
    	 dBean.setProbeExternalPort(probeExternalPort);
    	 dBean.setProbeSchemaPort(probeSchemaPort);
    	 dBean.setNginxImageName(nginxImageName);
    	 dBean.setNexusEndPointURL(nexusEndPointURL);
			 dBean.setLogstashHost(logstashHost);
			 dBean.setLogstashIp(logstashIp);
    	 dBean.setLogstashPort(logstashPort);
    	 
    	 log.debug("probeExternalPort "+probeExternalPort);
    	 log.debug("probeSchemaPort "+probeSchemaPort);
    	 log.debug("nginxImageName "+nginxImageName);
    	 log.debug("nexusEndPointURL "+nexusEndPointURL);
    	 log.debug("dockerProxyHost "+dockerProxyHost);
    	 log.debug("dockerProxyPort "+dockerProxyPort);
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
			 log.debug("logstashHost "+logstashHost);
			 log.debug("logstashIp "+logstashIp);
	   	 log.debug("logstashPort "+logstashPort);
	   	try{
	   	  String solutionToolKitType=kubeService.getSolutionCode(solutionId, cmnDataUrl, cmnDataUser, cmnDataPd);
	   	log.debug("solutionToolKitType "+solutionToolKitType);
	   	  if(solutionToolKitType!=null && !"".equals(solutionToolKitType) && "CP".equalsIgnoreCase(solutionToolKitType)){
	   		log.debug("Composite Solution Details Start");
	   		solutionZip=kubeService.compositeSolutionDetails(dBean, solutionToolKitType);
	   		log.debug("Composite Solution Deployment End");
	   	  }else{
	   		log.debug("Single Solution Details Start");
	   		String imageTag=kubeService.getSingleImageData(solutionId, revisionId, cmnDataUrl, cmnDataUser, cmnDataPd);
	   		solutionZip=kubeService.singleSolutionDetails(dBean, imageTag, singleModelPort, solutionToolKitType);
	   		log.debug("Single Solution Details End");
	   	  }
	   	response.setStatus(200);
	   	}catch(Exception e){
			log.error("getSolutionZip failed", e);
			response.setStatus(404);
			LogConfig.clearMDCDetails();
		}
	   	
	    response.setHeader("Content-Disposition", "attachment; filename=solution.zip");
	    response.getOutputStream().write(solutionZip);
	   	log.debug("End getSolutionZip");
	}
 }
