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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dockerKube.beans.ContainerBean;
import com.dockerKube.beans.DeploymentBean;
import com.dockerKube.service.KubeService;
import com.dockerKube.utils.DockerKubeConstants;
import com.dockerKube.utils.ParseJSON;


@RestController
public class KubeController {
	
	@Autowired
	private Environment env;
	
	@Autowired
	KubeService kubeService;
	
	Logger log = LoggerFactory.getLogger(KubeController.class);
	
	/**
	    * getSingleSolutionZip is used for single model deployment 
	    * @param solutionId 
	    *           - solutionId for Model 
	    * @param solutionRevisionId
	    *           - solutionRevisionId for Model 
	    * @param imageTag
	    *              - image name                
	    * @param response
	    *           - response return file
	    * @throws IOException
	    *             exception thrown
	    */
	
	@RequestMapping(value = "/getSingleSolutionZip", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void getSingleSolutionZip( String solutionId, String solutionRevisionId,String imageTag,HttpServletResponse response) throws IOException {
		log.debug("Start simple solution ");
		byte[] solutionZip=null;
		DeploymentBean dBean=new DeploymentBean();
		try{
			String singleModelPort=(env.getProperty(DockerKubeConstants.SINGLE_MODEL_PORT) != null) ? env.getProperty(DockerKubeConstants.SINGLE_MODEL_PORT) : "";
			String folderPath=(env.getProperty(DockerKubeConstants.FOLDERPATH) != null) ? env.getProperty(DockerKubeConstants.FOLDERPATH) : "";
			log.debug("singleModelPort "+singleModelPort);
			log.debug("folderPath "+folderPath);
			String solutionYaml=kubeService.getSingleSolutionYMLFile(imageTag,singleModelPort);
			dBean.setSolutionYml(solutionYaml);
			dBean.setFolderPath(folderPath);
			solutionZip=kubeService.createSingleSolutionZip(dBean);
			
		}catch(Exception e){
			log.error("getSingleSolutionZip failed", e);
		}
		response.setHeader("Content-Disposition", "attachment; filename=solution.zip");
	    response.getOutputStream().write(solutionZip);
		log.debug("End simple solution ");
	}
	
	/**
	    * getCompositeSolutionZip is used for composite model
	    * @param solutionId 
	    *           - solutionId for Model 
	    * @param solutionRevisionId
	    *           - solutionRevisionId for Model 
	    * @param response
	    *           - response return file
	    * @throws IOException
	    *             exception thrown
	    */
	@RequestMapping(value = "/getCompositeSolutionZip", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void getCompositeSolutionZip( String solutionId, String solutionRevisionId,HttpServletResponse response) throws IOException {
		log.debug("getCompositeSolutionZip Start ");
		byte[] solutionZip=null;
	    HashMap<String,ByteArrayOutputStream> hmap=new HashMap<String,ByteArrayOutputStream>();
	    DeploymentBean dBean=new DeploymentBean();
	    ParseJSON parseJson=new ParseJSON();
	    List<ContainerBean> contList=null;
	    
	    try{
	    	 String nexusUrl=(env.getProperty(DockerKubeConstants.NEXUS_URL_PROP) != null) ? env.getProperty(DockerKubeConstants.NEXUS_URL_PROP) : "";
	    	 String nexusUsername=(env.getProperty(DockerKubeConstants.NEXUS_USERNAME_PROP) != null) ? env.getProperty(DockerKubeConstants.NEXUS_USERNAME_PROP) : "";
	    	 String nexusPd=(env.getProperty(DockerKubeConstants.NEXUS_PD_PROP) != null) ? env.getProperty(DockerKubeConstants.NEXUS_PD_PROP) : "";
	    	 String cmnDataUrl=(env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCENDPOINURL_PROP) != null) ? env.getProperty(DockerKubeConstants.CMNDATASVC_CMNDATASVCENDPOINURL_PROP) : "";
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
	    	 
	    	 log.debug("nexusUrlnexusUrl "+nexusUrl);
	    	 log.debug("solutionId "+solutionId);
	    	 log.debug("solutionRevisionId "+solutionRevisionId);
	    	 log.debug("nexusUrl "+nexusUrl);
	    	 log.debug("nexusUsername "+nexusUsername);
	    	 log.debug("nexusPd "+nexusPd);
	    	 log.debug("cmnDataUrl "+cmnDataUrl);
	    	 log.debug("cmnDataUser "+cmnDataUser);
	    	 log.debug("cmnDataPd "+cmnDataUser);
	    	 log.debug("bluePrintImage "+bluePrintImage);
	    	 log.debug("bluePrintName "+bluePrintName);
	    	 log.debug("bluePrintPort "+bluePrintPort);
	    	 log.debug("KUBE_IP "+kubeIP);
	    	 log.debug("probeImage "+probeImage);
	    	 log.debug("probePort "+probePort);
	    	 log.debug("incrementPort "+incrementPort);
	    	 log.debug("folderPath "+folderPath);
	    	 dBean.setSolutionId(solutionId);
	    	 dBean.setSolutionRevisionId(solutionRevisionId);
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
	    	 /**Blueprint.json**/
	    	 ByteArrayOutputStream byteArrayOutputStream=kubeService.getBluePrintNexus(solutionId, solutionRevisionId, cmnDataUrl, cmnDataUser, cmnDataUser, nexusUrl, nexusUsername, nexusPd);
	    	 log.debug("byteArrayOutputStream "+byteArrayOutputStream);
	    	 dBean.setBluePrintjson(byteArrayOutputStream.toString());
	    	 /**Proto file code**/
	    	 contList=parseJson.getProtoDetails(byteArrayOutputStream.toString());
	    	 log.debug("contList "+contList);
	    	 dBean.setContainerBeanList(contList);
	    	 kubeService.getprotoDetails(dBean.getContainerBeanList(),dBean);
	    	 log.debug("Proto Details");
	    	 /**DataBroker**/
	    	 kubeService.getDataBrokerFile(dBean.getContainerBeanList(), dBean,byteArrayOutputStream.toString());
	    	 log.debug("DataBrokerFile");
	    	 /**SolutionYml and Datainfo json**/
	    	 kubeService.getSolutionYMLFile(dBean,byteArrayOutputStream.toString());
	    	 log.debug("SolutionYMLFile");
	    	 /**Deploy.sh **/
	    	 /**Create Zip**/
	    	 solutionZip=kubeService.createCompositeSolutionZip(dBean);
	    	 log.debug("CompositeSolutionZip");
			}catch(Exception e){
				log.error("getCompositeSolutionZip failed", e);
			}
	    log.debug("getCompositeSolutionZip End ");
	    response.setHeader("Content-Disposition", "attachment; filename=solution.zip");
	    response.getOutputStream().write(solutionZip);
	}

}
