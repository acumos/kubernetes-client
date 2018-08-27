package com.dockerKube.controller;



import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.dockerKube.utils.ParseJSON;
import com.dockerKube.utils.TestJava;
import com.fasterxml.jackson.annotation.JsonInclude.Include;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;


import com.dockerKube.beans.*;
import com.dockerKube.parsebean.*;
import com.dockerKube.service.KubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
public class KubeController {
	
	@Autowired
	private Environment env;
	
	@Autowired
	KubeService kubeService;
	
	Logger log = LoggerFactory.getLogger(KubeController.class);
	
	@RequestMapping(value = "/getSingleSolutionZip", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void getSingleSolutionZip( String solutionId, String solutionRevisionId,String imageTag,HttpServletResponse response) throws IOException {
		log.debug("Start simple solution ");
		byte[] solutionZip=null;
		DeploymentBean dBean=new DeploymentBean();
		try{
			String solutionYaml=kubeService.getSingleSolutionYMLFile(imageTag);
			dBean.setSolutionYml(solutionYaml);
			solutionZip=kubeService.createSingleSolutionZip(dBean);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		response.setHeader("Content-Disposition", "attachment; filename=solution.zip");
	    response.getOutputStream().write(solutionZip);
		log.debug("End simple solution ");
	}
	
	
	@RequestMapping(value = "/getCompositeSolutionZip", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void getCompositeSolutionZip( String solutionId, String solutionRevisionId,HttpServletResponse response) throws IOException {
	    String test = "new string test bytes";
	    byte[] solutionZip=null;
	    HashMap<String,ByteArrayOutputStream> hmap=new HashMap<String,ByteArrayOutputStream>();
	    DeploymentBean dBean=new DeploymentBean();
	    ParseJSON parseJson=new ParseJSON();
	    List<ContainerBean> contList=null;
	    
	    try{
	    	 /*String nexusUrl=(env.getProperty("nexus.url") != null) ? env.getProperty("nexus.url") : "";
	    	 String nexusUsername=(env.getProperty("nexus.username") != null) ? env.getProperty("nexus.username") : "";
	    	 String nexusPd=(env.getProperty("nexus.pd") != null) ? env.getProperty("nexus.pd") : "";
	    	 String cmnDataUrl=(env.getProperty("cmndatasvc.cmndatasvcendpoinurl") != null) ? env.getProperty("cmndatasvc.cmndatasvcendpoinurl") : "";
	    	 String cmnDataUser=(env.getProperty("cmndatasvc.cmndatasvcuser") != null) ? env.getProperty("cmndatasvc.cmndatasvcuser") : "";
	    	 String cmnDataPd=(env.getProperty("cmndatasvc.cmndatasvcpd") != null) ? env.getProperty("cmndatasvc.cmndatasvcpd") : "";*/
	    	
	    	String nexusUrl="http://cognita-nexus01:8081/repository/repo_cognita_model_maven/";
	    	String nexusUsername="cognita_model_rw";
	    	String nexusPd="not4you";
	    	String cmnDataUrl="http://cognita-dev1-vm01-core:8002/ccds";
	    	String cmnDataUser="ccds_client";
	    	String cmnDataPd="ccds_client";
	    			
	    			
	    	 
	    	log.debug("nexusUrlnexusUrl "+nexusUrl);
	    	log.debug("solutionId "+solutionId);
	    	log.debug("solutionRevisionId "+solutionRevisionId);
	    	log.debug("nexusUrl "+nexusUrl);
	    	log.debug("nexusUsername "+nexusUsername);
	    	log.debug("nexusPd "+nexusPd);
	    	log.debug("cmnDataUrl "+cmnDataUrl);
	    	log.debug("cmnDataUser "+cmnDataUser);
	    	log.debug("cmnDataPd "+cmnDataUser);
	    	 dBean.setSolutionId(solutionId);
	    	 dBean.setSolutionRevisionId(solutionRevisionId);
	    	 dBean.setNexusUrl(nexusUrl);
	    	 dBean.setNexusUserName(nexusUsername);
	    	 dBean.setNexusPd(nexusPd);
	    	 dBean.setCmnDataUrl(cmnDataUrl);
	    	 dBean.setCmnDataUser(cmnDataUser);
	    	 dBean.setCmnDataPd(cmnDataPd);
	    	 /**Blueprint.json**/
	    	 ByteArrayOutputStream byteArrayOutputStream=kubeService.getBluePrintNexus(solutionId, solutionRevisionId, cmnDataUrl, cmnDataUser, cmnDataUser, nexusUrl, nexusUsername, nexusPd);
	    	 log.debug("byteArrayOutputStream### "+byteArrayOutputStream);
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
	    	 kubeService.getSolutionYMLFile(contList, dBean,byteArrayOutputStream.toString());
	    	 log.debug("SolutionYMLFile");
	    	 /**Deploy.sh **/
	    	 //
	    	 /**Create Zip**/
	    	 solutionZip=kubeService.createCompositeSolutionZip(dBean);
	    	 log.debug("CompositeSolutionZip");
	    	 
	    	/* ByteArrayOutputStream blueprintOutput =TestJava.getBlueprintFile();
	    	 hmap.put("Blueprint.json", blueprintOutput);
	    	 ByteArrayOutputStream solutionOutput =TestJava.getSolutionFile();
	    	 hmap.put("solution.yml", solutionOutput);
			 ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
			 bOutput.write("test deploy file".getBytes()); 
			 hmap.put("deploy.sh", bOutput);
			 
			 bb=TestJava.zipFiles("solution.zip",hmap);*/
			}catch(Exception e){
				e.printStackTrace();
			}
	    response.setHeader("Content-Disposition", "attachment; filename=solution.zip");
	    response.getOutputStream().write(solutionZip);
	}

}
