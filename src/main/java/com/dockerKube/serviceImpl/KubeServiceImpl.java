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
package com.dockerKube.serviceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.nexus.client.NexusArtifactClient;
import org.acumos.nexus.client.RepositoryLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dockerKube.beans.ContainerBean;
import com.dockerKube.beans.Containers;
import com.dockerKube.beans.DeploymentBean;
import com.dockerKube.beans.DeploymentYml;
import com.dockerKube.beans.DockerInfo;
import com.dockerKube.beans.MetaData;
import com.dockerKube.beans.Ports;
import com.dockerKube.beans.Specification;
import com.dockerKube.controller.KubeController;
import com.dockerKube.parsebean.Blueprint;
import com.dockerKube.parsebean.DataBrokerBean;
import com.dockerKube.parsebean.Node;
import com.dockerKube.service.KubeService;
import com.dockerKube.utils.CommonUtil;
import com.dockerKube.utils.DockerKubeConstants;
import com.dockerKube.utils.ParseJSON;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

@Service
public class KubeServiceImpl implements KubeService  {
	
	Logger logger = LoggerFactory.getLogger(KubeController.class);
	public CommonDataServiceRestClientImpl getClient(String datasource,String userName,String dataPd) {
		logger.debug("getClient start");
		CommonDataServiceRestClientImpl client = new CommonDataServiceRestClientImpl(datasource, userName, dataPd,null);
		logger.debug("getClient End");
		return client;
	}
	public NexusArtifactClient nexusArtifactClient(String nexusUrl, String nexusUserName,String nexusPd) {
		logger.debug("nexusArtifactClient start");
		RepositoryLocation repositoryLocation = new RepositoryLocation();
		repositoryLocation.setId("1");
		repositoryLocation.setUrl(nexusUrl);
		repositoryLocation.setUsername(nexusUserName);
		repositoryLocation.setPassword(nexusPd);
		NexusArtifactClient nexusArtifactClient = new NexusArtifactClient(repositoryLocation);
		logger.debug("nexusArtifactClient End");
		return nexusArtifactClient;
	}
	@Override
	public ByteArrayOutputStream getBluePrintNexus(String solutionId, String revisionId,String datasource,String userName,String dataPd,
			String nexusUrl,String nexusUserName,String nexusPd) throws  Exception{
		  logger.debug(" getBluePrintNexus Start");
		  logger.debug("solutionId "+solutionId);
		  logger.debug("revisionId "+revisionId);
		  List<MLPSolutionRevision> mlpSolutionRevisionList;
		  String solutionRevisionId = revisionId;
		  List<MLPArtifact> mlpArtifactList;
		  String nexusURI = "";
		  String bluePrintStr="";
		  ByteArrayOutputStream byteArrayOutputStream = null;
		  CommonDataServiceRestClientImpl cmnDataService=getClient(datasource,userName,dataPd);
			if (null != solutionRevisionId) {
				// 3. Get the list of Artifiact for the SolutionId and SolutionRevisionId.
				mlpArtifactList = cmnDataService.getSolutionRevisionArtifacts(solutionId, solutionRevisionId);
				if (null != mlpArtifactList && !mlpArtifactList.isEmpty()) {
					nexusURI = mlpArtifactList.stream()
							.filter(mlpArt -> mlpArt.getArtifactTypeCode().equalsIgnoreCase(DockerKubeConstants.ARTIFACT_TYPE_BLUEPRINT)).findFirst()
							.get().getUri();
					logger.debug(" Nexus URI : " + nexusURI );
					if (null != nexusURI) {
						NexusArtifactClient nexusArtifactClient=nexusArtifactClient(nexusUrl,nexusUserName,nexusPd);
						byteArrayOutputStream = nexusArtifactClient.getArtifact(nexusURI);
						
					}
				}
			}	
			logger.debug("getBluePrintNexus End byteArrayOutputStream "+byteArrayOutputStream);	
		return byteArrayOutputStream;	
	  }
	public ByteArrayOutputStream getNexusUrlFile(String nexusUrl, String nexusUserName,String nexusPassword,String nexusURI)throws Exception {
		   logger.debug("getNexusUrlFile start");
		    ByteArrayOutputStream byteArrayOutputStream=null;
			NexusArtifactClient nexusArtifactClient=nexusArtifactClient(nexusUrl, 
					nexusUserName, nexusPassword);
			 byteArrayOutputStream = nexusArtifactClient.getArtifact(nexusURI);
			 logger.debug("byteArrayOutputStream "+byteArrayOutputStream);
			 logger.debug("getNexusUrlFile ");
			return byteArrayOutputStream;
}	
  public List<ContainerBean> getprotoDetails(List<ContainerBean> contList,DeploymentBean dBean) throws Exception{
	if(contList!=null){
		int j = 0;
		while (contList.size() > j) {
			ContainerBean contbean=contList.get(j);
			if(contbean!=null && contbean.getContainerName()!=null && !"".equals(contbean.getContainerName())
					&& contbean.getProtoUriPath()!=null && !"".equals(contbean.getProtoUriPath())){
				ByteArrayOutputStream byteArrayOutputStream=getNexusUrlFile(dBean.getNexusUrl(), dBean.getNexusUserName(),
						dBean.getNexusPd(), contbean.getProtoUriPath());
				logger.debug(contbean.getProtoUriPath() +"byteArrayOutputStream "+byteArrayOutputStream);
				contbean.setProtoUriDetails(byteArrayOutputStream.toString());
			}
			j++;
		}
	}
	return contList;
  }
  
  public void getDataBrokerFile(List<ContainerBean> contList,DeploymentBean dBean,String jsonString)throws Exception{
	  ParseJSON parseJson=new ParseJSON();
	  DataBrokerBean dataBrokerBean=parseJson.getDataBrokerContainer(jsonString);
		 if(dataBrokerBean!=null){
				if(dataBrokerBean!=null){
					ByteArrayOutputStream byteArrayOutputStream=getNexusUrlFile(dBean.getNexusUrl(),dBean.getNexusUserName(), dBean.getNexusPd(),
							dataBrokerBean.getProtobufFile());
					logger.debug("byteArrayOutputStream "+byteArrayOutputStream);
					if(byteArrayOutputStream!=null){
						dataBrokerBean.setProtobufFile(byteArrayOutputStream.toString());
						dBean.setDataBrokerJson(byteArrayOutputStream.toString());	
					}else{
						dataBrokerBean.setProtobufFile("");
						dBean.setDataBrokerJson("");	
					}
					
				 }
				
		  }
	  
  }
  public void getSolutionYMLFile(List<ContainerBean> contList,DeploymentBean dBean,String jsonString)throws Exception{
	    Blueprint bluePrintProbe=null;
		DataBrokerBean dataBrokerBean=null;
		ParseJSON parseJson=new ParseJSON();
		String solutionYaml=null;
		
		List<DockerInfo> dockerInfoList=new ArrayList<DockerInfo>();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		dataBrokerBean=parseJson.getDataBrokerContainer(jsonString);
		bluePrintProbe=parseJson.jsonFileToObject(jsonString,dataBrokerBean);
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
		String ip=dBean.getKubeIP();
		if(ip==null || "".equals(ip)){
			ip="localhost";
		}
		List<Node> node=bluePrintProbe.getNodes();
		Iterator itr=node.iterator();
		int contPort=8080;
		List<Containers> containers=new ArrayList<Containers>();
		while(itr.hasNext()){
			Node nodevar =(Node)itr.next();
			Containers container=new Containers();
			DockerInfo dockerInfo=new DockerInfo();
	  		container.setName(nodevar.getContainerName());
	  		container.setImage(nodevar.getImage());
	  		Ports ports=new Ports();
	  		ports.setContainerPort(String.valueOf(contPort));
	  		container.setPorts(ports);
	  		containers.add(container);
	  		
	  		dockerInfo.setContainer(nodevar.getContainerName());
	  		dockerInfo.setIpAddress(ip);
	  		dockerInfo.setPort(String.valueOf(contPort));
	  		dockerInfoList.add(dockerInfo);
	  		contPort++;
		}
		
		Containers containerBP=new Containers();
		containerBP.setName(DockerKubeConstants.BLUEPRINT_CONTAINER);
		containerBP.setImage(dBean.getBluePrintImage());
		Ports portsBP=new Ports();
		portsBP.setContainerPort(String.valueOf(dBean.getBluePrintPort()));
		containerBP.setPorts(portsBP);
		containers.add(containerBP);
		
		DockerInfo dockerBluePrintInfo=new DockerInfo();
		dockerBluePrintInfo.setContainer(DockerKubeConstants.BLUEPRINT_CONTAINER);
		dockerBluePrintInfo.setIpAddress(ip);
		dockerBluePrintInfo.setPort(String.valueOf(dBean.getBluePrintPort()));
  		dockerInfoList.add(dockerBluePrintInfo);
		
		
		Specification sp=new Specification();
		sp.setContainers(containers);
		
		MetaData meta=new MetaData();
		meta.setName(bluePrintProbe.getName());
		DeploymentYml dep=new DeploymentYml();
		dep.setApiVersion(DockerKubeConstants.KUBE_VERSION);
		dep.setKind(DockerKubeConstants.KUBE_KIND);
		dep.setMetadata(meta);
		dep.setSpec(sp);
		mapper.setSerializationInclusion(Include.NON_NULL);
		solutionYaml = mapper.writeValueAsString(dep);
		if(solutionYaml!=null){
			dBean.setSolutionYml(solutionYaml);
		}
		if(dockerInfoList!=null && dockerInfoList.size() > 0){
			ObjectMapper objMapper = new ObjectMapper();
			String dockerJson=objMapper.writeValueAsString(dockerInfoList);
			logger.debug("dockerJson "+dockerJson);
			dBean.setDockerInfoJson(dockerJson);
		}
		
  }
  
  public byte[] createCompositeSolutionZip(DeploymentBean dBean)throws Exception{
	  
	    byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos =null;
		HashMap<String,ByteArrayOutputStream> hmap=new HashMap<String,ByteArrayOutputStream>();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		CommonUtil util=new CommonUtil();
			if(dBean!=null){
				 String installScript=util.getFileDetails(DockerKubeConstants.KUBE_PATH_K8S_SH);
				 if(installScript!=null && !"".equals(installScript)){
					 bOutput.write(installScript.getBytes());
					 hmap.put(DockerKubeConstants.KUBE_K8S_SH, bOutput);
					 logger.debug(DockerKubeConstants.KUBE_K8S_SH+" "+bOutput);
				 }
				 
				 bOutput = new ByteArrayOutputStream(12);
				 String deployScript=util.getFileDetails(DockerKubeConstants.KUBE_PATH_DEPLOY_SH);
				 if(installScript!=null && !"".equals(installScript)){
					 bOutput.write(deployScript.getBytes());
					 hmap.put(DockerKubeConstants.KUBE_DEPLOY_SH, bOutput);
					 logger.debug(DockerKubeConstants.KUBE_DEPLOY_SH+" "+bOutput);
				 }
				if(dBean.getBluePrintjson()!=null && !"".equals(dBean.getBluePrintjson())){
					bOutput = new ByteArrayOutputStream(12);
					bOutput.write(dBean.getBluePrintjson().getBytes());
					hmap.put(DockerKubeConstants.KUBE_BLUEPRINT_JSON, bOutput);
					logger.debug(DockerKubeConstants.KUBE_BLUEPRINT_JSON+" "+bOutput);
				}
				
				if(dBean.getDockerInfoJson()!=null && !"".equals(dBean.getDockerInfoJson())){
					bOutput = new ByteArrayOutputStream(12);
					bOutput.write(dBean.getDockerInfoJson().getBytes());
					hmap.put(DockerKubeConstants.KUBE_DOCKERINFO_JSON, bOutput);
					logger.debug(DockerKubeConstants.KUBE_DOCKERINFO_JSON+"  "+bOutput);
				}
				if(dBean.getSolutionYml()!=null && !"".equals(dBean.getSolutionYml())){
					bOutput = new ByteArrayOutputStream(12);
					bOutput.write(dBean.getSolutionYml().getBytes());
					hmap.put(DockerKubeConstants.KUBE_SOLUTION_YML, bOutput);
					logger.debug(DockerKubeConstants.KUBE_SOLUTION_YML+"  "+bOutput);
				}
				if(dBean.getDataBrokerJson()!=null && !"".equals(dBean.getDataBrokerJson())){
					bOutput = new ByteArrayOutputStream(12);
					bOutput.write(dBean.getDataBrokerJson().getBytes());
					hmap.put(DockerKubeConstants.KUBE_DATABROKER_JSON, bOutput);
					logger.debug(DockerKubeConstants.KUBE_DATABROKER_JSON+"  "+bOutput);
				}
				if(dBean.getContainerBeanList()!=null && dBean.getContainerBeanList().size() > 0){
					List<ContainerBean> contList=dBean.getContainerBeanList();
					if(contList!=null){
						int j = 0;
						while (contList.size() > j) {
							ContainerBean contbean=contList.get(j);
							if(contbean!=null && contbean.getProtoUriPath()!=null && !"".equals(contbean.getProtoUriPath())
									&& contbean.getProtoUriDetails()!=null && !"".equals(contbean.getProtoUriDetails())){
							    int index = contbean.getProtoUriPath().lastIndexOf("/");
				        	    String protoFileName=contbean.getProtoUriPath().substring(index+1);
				        	    bOutput = new ByteArrayOutputStream(12);
								bOutput.write(contbean.getProtoUriDetails().getBytes());
				        	    hmap.put(protoFileName, bOutput);
				        	    logger.debug(protoFileName+" "+bOutput);
							}
							j++;
						}
					}
				}
				
				
			}
			
			baos= new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			Iterator it = hmap.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        String fileName=(String)pair.getKey();
		        ByteArrayOutputStream ba=(ByteArrayOutputStream)pair.getValue();
		        
		        ZipEntry ze = new ZipEntry(fileName);
				zos.putNextEntry(ze);
				InputStream in = new ByteArrayInputStream(ba.toByteArray());
				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				in.close();
		    }
			
			zos.closeEntry();
			zos.close();
	    	return baos.toByteArray();
  }
  
  public String getSingleSolutionYMLFile(String imageTag)throws Exception{
	    //String imageContainerName="SingleImageContainer";
		String solutionYaml=null;
		CommonUtil cmUtil=new CommonUtil();
		List<DockerInfo> dockerInfoList=new ArrayList<DockerInfo>();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
		//String containerName=cmUtil.geContainerName(imageTag);
		/*if(containerName!=null && !"".equals(containerName.trim())){
			imageContainerName=containerName;
		}*/
		int contPort=Integer.parseInt(DockerKubeConstants.KUBE_CONT_PORT);
		List<Containers> containers=new ArrayList<Containers>();
		Containers containerBP=new Containers();
		containerBP.setName(DockerKubeConstants.SINGLE_CONTAINER_NAME);
		containerBP.setImage(imageTag);
		Ports portsBP=new Ports();
		portsBP.setContainerPort(String.valueOf(contPort));
		containerBP.setPorts(portsBP);
		containers.add(containerBP);
		
		Specification sp=new Specification();
		sp.setContainers(containers);
		MetaData meta=new MetaData();
		meta.setName(DockerKubeConstants.SINGLE_CONTAINER_NAME);
		DeploymentYml dep=new DeploymentYml();
		dep.setApiVersion(DockerKubeConstants.KUBE_VERSION);
		dep.setKind(DockerKubeConstants.KUBE_KIND);
		dep.setMetadata(meta);
		dep.setSpec(sp);
		mapper.setSerializationInclusion(Include.NON_NULL);
		solutionYaml = mapper.writeValueAsString(dep);
		logger.debug("solutionYaml  "+solutionYaml);
		return solutionYaml;
  }

  public byte[] createSingleSolutionZip(DeploymentBean dBean)throws Exception{
	  
	    byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos =null;
		HashMap<String,ByteArrayOutputStream> hmap=new HashMap<String,ByteArrayOutputStream>();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		CommonUtil util=new CommonUtil();
			if(dBean!=null){
					 bOutput = new ByteArrayOutputStream(12);
					 String installScript=util.getFileDetails(DockerKubeConstants.KUBE_PATH_K8S_SH);
					 if(installScript!=null && !"".equals(installScript)){
						 bOutput.write(installScript.getBytes());
						 hmap.put(DockerKubeConstants.KUBE_K8S_SH, bOutput);
						 logger.debug(DockerKubeConstants.KUBE_K8S_SH+" "+bOutput);
					 }
					 
					 bOutput = new ByteArrayOutputStream(12);
					 String deployScript=util.getFileDetails(DockerKubeConstants.KUBE_PATH_DEPLOY_SH);
					 if(installScript!=null && !"".equals(installScript)){
						 bOutput.write(deployScript.getBytes());
						 hmap.put(DockerKubeConstants.KUBE_DEPLOY_SH, bOutput);
						 logger.debug(DockerKubeConstants.KUBE_DEPLOY_SH+"   "+bOutput);
					 }
					 
				
				
				
				if(dBean.getSolutionYml()!=null && !"".equals(dBean.getSolutionYml())){
					bOutput = new ByteArrayOutputStream(12);
					bOutput.write(dBean.getSolutionYml().getBytes());
					hmap.put(DockerKubeConstants.KUBE_SOLUTION_YML, bOutput);
					logger.debug(DockerKubeConstants.KUBE_SOLUTION_YML+"  "+bOutput);
				}
				
			}
			
			baos= new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			Iterator it = hmap.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        String fileName=(String)pair.getKey();
		        ByteArrayOutputStream ba=(ByteArrayOutputStream)pair.getValue();
		        
		        ZipEntry ze = new ZipEntry(fileName);
				zos.putNextEntry(ze);
				InputStream in = new ByteArrayInputStream(ba.toByteArray());
				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				in.close();
		    }
			
			zos.closeEntry();
			zos.close();
			logger.debug("Done");
	 
		return baos.toByteArray();
   }

}
