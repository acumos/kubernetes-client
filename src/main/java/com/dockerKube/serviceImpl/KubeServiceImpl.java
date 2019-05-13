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
import java.lang.invoke.MethodHandles;
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
import com.dockerKube.beans.DeploymentBean;
import com.dockerKube.beans.DeploymentKubeBean;
import com.dockerKube.beans.DockerInfo;
import com.dockerKube.beans.DockerInfoBean;
import com.dockerKube.beans.MLSolutionBean;
import com.dockerKube.controller.KubeController;
import com.dockerKube.parsebean.Blueprint;
import com.dockerKube.parsebean.DataBrokerBean;
import com.dockerKube.parsebean.ProbeIndicator;
import com.dockerKube.service.KubeService;
import com.dockerKube.utils.CommonUtil;
import com.dockerKube.utils.DockerKubeConstants;
import com.dockerKube.utils.ParseJSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.CodeNameType;
import org.acumos.cds.domain.MLPCodeNamePair;

@Service
public class KubeServiceImpl implements KubeService  {
	
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	    * getClient is used geeting connection from database
	    * @param datasource 
	    *           - dataSource for Model 
	    * @param userName
	    *           - userName for database 
	    * @param dataPd
	    *           - dataPd for database
	    * @return client
	    *          - client object          
	    */
	public CommonDataServiceRestClientImpl getClient(String datasource,String userName,String dataPd) {
		logger.debug("getClient start");
		CommonDataServiceRestClientImpl client = new CommonDataServiceRestClientImpl(datasource, userName, dataPd,null);
		logger.debug("getClient End");
		return client;
	}
	public byte[] singleSolutionDetails(DeploymentBean dBean,String imageTag,String singleModelPort)throws Exception{
		logger.debug("singleSolutionDetails start");
		logger.debug("imageTag "+imageTag +" singleModelPort "+singleModelPort);
		byte[] solutionZip=null;
		String solutionYaml=getSingleSolutionYMLFile(imageTag,singleModelPort,dBean);
		dBean.setSolutionYml(solutionYaml);
		logger.debug("solutionYaml "+solutionYaml);
		solutionZip=createSingleSolutionZip(dBean);
		logger.debug("singleSolutionDetails End");
		return solutionZip;
	}
	public byte[] compositeSolutionDetails(DeploymentBean dBean)throws Exception{
		logger.debug("compositeSolutionDetails start");
		byte[] solutionZip=null;
		List<ContainerBean> contList=null;
		ParseJSON parseJson=new ParseJSON();
		/**Blueprint.json**/
	   	 ByteArrayOutputStream byteArrayOutputStream=getBluePrintNexus(dBean.getSolutionId(), dBean.getSolutionRevisionId(), dBean.getCmnDataUrl(), 
	   			dBean.getCmnDataUser(), dBean.getCmnDataPd(), dBean.getNexusUrl(), dBean.getNexusUserName(), dBean.getNexusPd());
	   	 logger.debug("byteArrayOutputStream "+byteArrayOutputStream);
	   	 dBean.setBluePrintjson(byteArrayOutputStream.toString());
	   	 /**Proto file code**/
	   	 contList=parseJson.getProtoDetails(byteArrayOutputStream.toString());
	   	 logger.debug("contList "+contList);
	   	 dBean.setContainerBeanList(contList);
	   	 getprotoDetails(dBean.getContainerBeanList(),dBean);
	   	 logger.debug("Proto Details");
	   	 /**DataBroker**/
	   	 getDataBrokerFile(dBean.getContainerBeanList(), dBean,byteArrayOutputStream.toString());
	   	 logger.debug("DataBrokerFile");
	   	 /**SolutionYml and Datainfo json**/
	   	 getSolutionYMLFile(dBean,byteArrayOutputStream.toString());
	   	 logger.debug("SolutionYMLFile");
	   	 /**Deploy.sh **/
	   	 /**Create Zip**/
	   	 solutionZip=createCompositeSolutionZip(dBean);
		 logger.debug("compositeSolutionDetails End");
		return solutionZip;
	}
	public String getSingleImageData(String solutionId,String revisionId,String datasource,String userName,String dataPd)throws Exception{
		logger.debug("Start getSingleImageData");
		String imageTag="";
		CommonDataServiceRestClientImpl cmnDataService=getClient(datasource,userName,dataPd);
		List<MLPArtifact> mlpSolutionRevisions = null;
		mlpSolutionRevisions = cmnDataService.getSolutionRevisionArtifacts(solutionId, revisionId);
		if(mlpSolutionRevisions != null) {
			for (MLPArtifact artifact : mlpSolutionRevisions) {
				String[] st = artifact.getUri().split("/");
				String name = st[st.length-1];
				artifact.setName(name);
				logger.debug("ArtifactTypeCode" +artifact.getArtifactTypeCode());
				logger.debug("URI" +artifact.getUri());
				if(artifact.getArtifactTypeCode()!=null && artifact.getArtifactTypeCode().equalsIgnoreCase("DI")){
					imageTag=artifact.getUri();
				}
			}
		}
		 
		logger.debug("End getSingleImageData imageTag"+imageTag);
		return imageTag;
	}
	public String getSolutionCode(String solutionId,String datasource,String userName,String dataPd){
		logger.debug("getSolution start");
		String toolKitTypeCode="";
		try{
		CommonDataServiceRestClientImpl cmnDataService=getClient(datasource,userName,dataPd);
		MLPSolution mlpSolution = cmnDataService.getSolution(solutionId);
			if (mlpSolution != null) {
				logger.debug("mlpSolution.getToolkitTypeCode() "+mlpSolution.getToolkitTypeCode());
				toolKitTypeCode=mlpSolution.getToolkitTypeCode();
			}
		}catch(Exception e){
			logger.error("Error in get solution "+e.getMessage());
			toolKitTypeCode="";
		}
		logger.debug("getSolution End toolKitTypeCode " +toolKitTypeCode);	
	  return toolKitTypeCode;
	 }	
	/** nexusArtifactClient method is used to get connection from nexus
	 * @param nexusUrl
	 *             - url of nexus
	 * @param nexusUserName
	 *             - nexus userName
	 * @param nexusPd
	 *             - nexus pd
	 * @return nexusArtifactClient
	 *                          - nexus arifact client obj
	 */
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
	/** getBluePrintNexus method is used to get file from nexus
	 * @param solutionId
	 *                - solutionId for model
	 * @param revisionId
	 *                - revisionId for model
	 * @param datasource
	 *               - datasource name  
	 * @param userName
	 *                - userName for Database
	 * @param dataPd
	 *              - pd for database
	 * @param nexusUrl
	 *               - url of nexus
	 * @param nexusUserName
	 *                  - nexus userName
	 * @param nexusPd
	 *              - nexus pd
	 * @throws Exception
	 *               - exception for method
	 * @return byteArrayOutputStream
	 *                            - String return in for of byte
	 */
	
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
	/** getNexusUrlFile method is used to get file from nexus
	 * @param nexusUrl
	 *             - url of nexus
	 * @param nexusUserName
	 *               - nexus userName
	 * @param nexusPassword
	 *                - nexus password
	 * @param nexusURI
	 *              - url of nexus
	 * @throws Exception
	 *              - exception for method
	 * @return byteArrayOutputStream
	 *                   - String return in for of byte
	 */	
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
	

/** getprotoDetails method is used to get probe details
 * @param contList
 *            - List of containers
 * @param dBean
 *           - Deployment mean obj
 * @throws Exception
 *             - exception for method
 * @return contList
 *              - List of containers
 */
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
/** getDataBrokerFile method is used to get databroker details
 * @param contList
 *            - List of containers
 * @param dBean
 *            - Deployment bean obj
 * @param jsonString
 *              - Json String 
 * @throws Exception
 *             - exception for method
 */
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
  /** getSolutionYMLFile method is used to get solution file 
   * @param dBean
   *            - Deployment bean obj
   * @param jsonString
   *             - Json String
   * @throws Exception
   *             - exception for method
   */
  public void getSolutionYMLFile(DeploymentBean dBean,String jsonString)throws Exception{
	    logger.debug("Start getSolutionYMLFile");
		DataBrokerBean dataBrokerBean=null;
		ParseJSON parseJson=new ParseJSON();
		CommonUtil cutil=new CommonUtil();
		String solutionYml="";
		DockerInfo dockerInfo=new DockerInfo();
		List<DockerInfoBean> dockerInfoBeanList=new ArrayList<DockerInfoBean>();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		dataBrokerBean=parseJson.getDataBrokerContainer(jsonString);
		//ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
		//ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		List<DeploymentKubeBean> deploymentKubeBeanList=parseJson.parseJsonFileImageMap(jsonString);
		boolean probeIndicator=parseJson.checkProbeIndicator(jsonString);
		if (probeIndicator) {
			logger.debug("probeIndicator "+probeIndicator);
			DeploymentKubeBean probeNginxBean=new DeploymentKubeBean();
			probeNginxBean.setContainerName(DockerKubeConstants.PROBE_CONTAINER_NAME);
			probeNginxBean.setImage(dBean.getProbeImage());
			probeNginxBean.setImagePort(dBean.getProbePort());
			probeNginxBean.setNodeType(DockerKubeConstants.PROBE_CONTAINER_NAME);
			deploymentKubeBeanList.add(probeNginxBean);
		}
		DeploymentKubeBean depBlueprintBean=new DeploymentKubeBean();
		depBlueprintBean.setContainerName(DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		depBlueprintBean.setImage(dBean.getBluePrintImage());
		depBlueprintBean.setImagePort(dBean.getBluePrintPort());
		depBlueprintBean.setNodeType(DockerKubeConstants.BLUEPRINT_CONTAINER_NAME);
		deploymentKubeBeanList.add(depBlueprintBean);
		
		
		
		int contPort=Integer.parseInt(dBean.getIncrementPort());
		DockerInfo dockerBluePrintInfo=new DockerInfo();
		
		Iterator itr=deploymentKubeBeanList.iterator();
		while(itr.hasNext()){
			String portDockerInfo="";
			DeploymentKubeBean depBen=(DeploymentKubeBean)itr.next();
			if(depBen!=null && depBen.getContainerName()!=null && !"".equals(depBen.getContainerName())
					        && depBen.getImage()!=null && !"".equals(depBen.getImage())
					        && depBen.getNodeType()!=null && !"".equals(depBen.getNodeType())){
				
				String imagePort="";
				if(depBen.getNodeType()!=null){
					if(depBen.getNodeType().equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER) 
							|| depBen.getNodeType().equalsIgnoreCase(DockerKubeConstants.DATABROKER_NAME) 
							|| depBen.getNodeType().equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
						imagePort="";
					}else{
						imagePort=String.valueOf(contPort);
						contPort++;
					}
				}else{
					imagePort=String.valueOf(contPort);
					contPort++;
				}
				
				logger.debug("imagePort "+imagePort);
				String serviceYml=getCompositeSolutionService(depBen.getContainerName(),imagePort,depBen.getNodeType(),dBean);
				String deploymentYml=getCompositeSolutionDeployment(depBen.getImage(),depBen.getContainerName(),
						imagePort,depBen.getNodeType(),dBean);
				
				if(depBen.getNodeType()!=null && depBen.getNodeType().equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
					portDockerInfo=dBean.getBluePrintPort();
				}else if(depBen.getNodeType()!=null && depBen.getNodeType().equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
					portDockerInfo=dBean.getDataBrokerTargetPort();
				}else if(depBen.getNodeType()!=null && depBen.getNodeType().equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
					portDockerInfo=dBean.getProbeTargetPort();
				}else{
					portDockerInfo=imagePort;
				}
				logger.debug("depBen.getNodeType() "+depBen.getNodeType());
				logger.debug("portDockerInfo "+portDockerInfo);
				logger.debug("serviceYml "+serviceYml);
				logger.debug("deploymentYml "+deploymentYml);
				solutionYml=solutionYml+serviceYml;
				solutionYml=solutionYml+deploymentYml;
				logger.debug("solutionYml "+solutionYml);
				
				DockerInfoBean dockerInfoBean=new DockerInfoBean();
				dockerInfoBean.setContainer(depBen.getContainerName());
				dockerInfoBean.setIpAddress(depBen.getContainerName());
				dockerInfoBean.setPort(portDockerInfo);
				dockerInfoBeanList.add(dockerInfoBean);
			}
			
		}
		logger.debug("Final solutionYml "+solutionYml);
		dBean.setSolutionYml(solutionYml);
		dockerInfo.setDockerInfolist(dockerInfoBeanList);
		if(dockerInfo!=null){
			ObjectMapper objMapper = new ObjectMapper();
			String dockerJson=objMapper.writeValueAsString(dockerInfo);
			logger.debug("dockerJson "+dockerJson);
			dBean.setDockerInfoJson(dockerJson);
		}
		if(dataBrokerBean!=null){
			ObjectMapper dataBrokerMapper = new ObjectMapper();
			String dataBrokerJson=dataBrokerMapper.writeValueAsString(dataBrokerBean);
			logger.debug("dataBrokerJson "+dataBrokerJson);
			dBean.setDataBrokerJson(dataBrokerJson);
		}
	logger.debug("End getSolutionYMLFile");
  }
  /** createCompositeSolutionZip method is used to get zip file 
   * @param dBean
   *           - Deployment bean obj
   * @return baos
   *          - String return in for of byte
   * @throws Exception
   *           - exception for method
   */
  public byte[] createCompositeSolutionZip(DeploymentBean dBean)throws Exception{
	  
	    byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos =null;
		HashMap<String,ByteArrayOutputStream> hmap=new HashMap<String,ByteArrayOutputStream>();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		CommonUtil util=new CommonUtil();
			if(dBean!=null){
				 String kubernetesFile=dBean.getFolderPath()+"/"+DockerKubeConstants.KUBE_K8S_SH;
				 String installScript=util.getFileDetails(kubernetesFile);
				 if(installScript!=null && !"".equals(installScript)){
					 bOutput.write(installScript.getBytes());
					 hmap.put(DockerKubeConstants.KUBE_K8S_SH, bOutput);
					 logger.debug(DockerKubeConstants.KUBE_K8S_SH+" "+bOutput);
				 }
				 
				 bOutput = new ByteArrayOutputStream(12);
				 String deployFile=dBean.getFolderPath()+"/"+DockerKubeConstants.KUBE_DEPLOY_SH;
				 String deployScript=util.getFileDetails(deployFile);
				 if(installScript!=null && !"".equals(installScript)){
					 bOutput.write(deployScript.getBytes());
					 hmap.put(DockerKubeConstants.KUBE_DEPLOY_SH, bOutput);
					 logger.debug(DockerKubeConstants.KUBE_DEPLOY_SH+" "+bOutput);
				 }
				 //Env file
				 bOutput = new ByteArrayOutputStream(12);
				 String deployEnvScript=util.getEnvFileDetails(dBean);
				 if(deployEnvScript!=null && !"".equals(deployEnvScript)){
					 bOutput.write(deployEnvScript.getBytes());
					 hmap.put(DockerKubeConstants.KUBE_DEPLOY_ENV_SH, bOutput);
					 logger.debug(DockerKubeConstants.KUBE_DEPLOY_ENV_SH+" "+bOutput);
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
								String protoFolderName="microservice"+"/"+contbean.getContainerName()+"/"+"model.proto";
				        	    logger.debug(protoFolderName+"  "+protoFolderName);
				        	    hmap.put(protoFolderName, bOutput);
				        	    logger.debug(contbean.getProtoUriPath()+" "+bOutput);
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
  
  /** getSingleSolutionYMLFile method is used to get yml file 
   * @param imageTag
   *              - image name for deployment
   * @param singleModelPort
   *               - port of model
   * @param dBean
   *               - DeploymentBean
   * @return solutionYaml
   *               - yml string 
   * @throws Exception
   *              - exception for method
   */
public String getSingleSolutionYMLFile(String imageTag,String singleModelPort,DeploymentBean dBean)throws Exception{
	    logger.debug("getSingleSolutionYMLFile Start");
		String solutionYaml = "";
		String solutionId = dBean.getSolutionId();
		CommonUtil cutil = new CommonUtil();
		String modelName = cutil.getModelName(imageTag, solutionId);
		String serviceYml = getSingleSolutionService(singleModelPort, dBean, modelName);
		String deploymentYml = getSingleSolutionDeployment(imageTag, dBean, modelName);
		solutionYaml = serviceYml;
		solutionYaml = solutionYaml+deploymentYml;
		logger.debug("solutionYaml "+solutionYaml);
		logger.debug("getSingleSolutionYMLFile End");
		return solutionYaml;
  }
/** getSingleSolutionService method is used to get service details 
 * @param modelPort
 *              - port of model
 * @param dBean
 *           - DeploymentBean
 * @param modelName
 *           - modelName
 * @return serviceYml
 *              - yml string 
 * @throws Exception
 *               - exception for method
 */
  public String getSingleSolutionService(String modelPort, DeploymentBean dBean, String modelName)throws Exception{
	  logger.debug("getSingleSolutionService Start");
	    String serviceYml="";
	    ObjectMapper objectMapper = new ObjectMapper();
	    YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
	    ObjectNode apiRootNode = objectMapper.createObjectNode();
	    apiRootNode.put(DockerKubeConstants.APIVERSION_YML,DockerKubeConstants.V_YML);
	   	    
	    /*ObjectNode kindDataNode = objectMapper.createObjectNode();
	    kindDataNode.put(DockerKubeConstants.KIND_YML, DockerKubeConstants.SERVICE_YML);*/
	    apiRootNode.put(DockerKubeConstants.KIND_YML, DockerKubeConstants.SERVICE_YML);
		
		String modelNameYml = (modelName != null) ? modelName : DockerKubeConstants.MYMODEL_YML;

	  ObjectNode metadataNode = objectMapper.createObjectNode();
		metadataNode.put(DockerKubeConstants.NAMESPACE_YML, DockerKubeConstants.ACUMOS_YML);
		metadataNode.put(DockerKubeConstants.NAME_YML, modelNameYml);
		apiRootNode.set(DockerKubeConstants.METADATA_YML, metadataNode);
		
		ObjectNode specNode = objectMapper.createObjectNode();
		
		ObjectNode selectorNode = objectMapper.createObjectNode();
		selectorNode.put(DockerKubeConstants.APP_YML, modelNameYml);
		specNode.set(DockerKubeConstants.SELECTOR_YML, selectorNode);
		specNode.put(DockerKubeConstants.TYPE_YML, DockerKubeConstants.NODE_TYPE_PORT_YML);
		
		ArrayNode portsArrayNode = specNode.arrayNode();
		ObjectNode portsNode = objectMapper.createObjectNode();
		
		portsNode.put(DockerKubeConstants.NAME_YML, DockerKubeConstants.PROTOBUF_API_DEP_YML);
		portsNode.put(DockerKubeConstants.NODEPORT_YML, dBean.getSingleNodePort());
		portsNode.put(DockerKubeConstants.PORT_YML, dBean.getSingleModelPort());
		portsNode.put(DockerKubeConstants.TARGETPORT_YML, dBean.getSingleTargetPort());
		portsArrayNode.add(portsNode);
		specNode.set(DockerKubeConstants.PORTS_YML, portsArrayNode);
		
		apiRootNode.set(DockerKubeConstants.SPEC_YML, specNode);
		
		serviceYml = yamlMapper.writeValueAsString(apiRootNode);
		 logger.debug("solutionDeployment "+serviceYml);
	  return serviceYml;
  }
  /** getSingleSolutionDeployment method is used to get deployment details 
   * @param imageTag
   *             - image name
   * @param dBean
   *             - DeploymentBean            
   * @param modelName
   *           - modelName
   * @return modelPort
   *                - model port
   * @throws Exception
   *               - exception for method
   */
  public String getSingleSolutionDeployment(String imageTag,DeploymentBean dBean, String modelName)throws Exception{
	    logger.debug("getSingleSolutionDeployment Start");
	    ObjectMapper objectMapper = new ObjectMapper();
	    CommonUtil cutil=new CommonUtil();
	    YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
	    ObjectNode kindRootNode = objectMapper.createObjectNode();
	    kindRootNode.put(DockerKubeConstants.APIVERSION_DEP_YML, DockerKubeConstants.APPS_V1_DEP_YML);
	    kindRootNode.put(DockerKubeConstants.KIND_DEP_YML, DockerKubeConstants.DEPLOYMENT_DEP_YML);
	    
	    ObjectNode metadataNode = objectMapper.createObjectNode();
		metadataNode.put(DockerKubeConstants.NAMESPACE_DEP_YML, DockerKubeConstants.ACUMOS_DEP_YML);
		String modelNameYml = (modelName != null) ? modelName : DockerKubeConstants.MYMODEL_DEP_YML;
		metadataNode.put(DockerKubeConstants.NAME_DEP_YML, modelNameYml);
		
		ObjectNode labelsNode = objectMapper.createObjectNode();
		labelsNode.put(DockerKubeConstants.APP_DEP_YML, modelNameYml);
		metadataNode.put(DockerKubeConstants.LABELS_DEP_YML, labelsNode);
		
		kindRootNode.set(DockerKubeConstants.METADATA_DEP_YML, metadataNode);
		
		ObjectNode specNode = objectMapper.createObjectNode();
		specNode.put(DockerKubeConstants.REPLICAS_DEP_YML, 1);
		
		ObjectNode selectorNode = objectMapper.createObjectNode();
		ObjectNode matchLabelsNode = objectMapper.createObjectNode();
		matchLabelsNode.put(DockerKubeConstants.APP_DEP_YML, modelNameYml);
		selectorNode.set(DockerKubeConstants.MATCHLABELS_DEP_YML, matchLabelsNode);
		
		specNode.set(DockerKubeConstants.SELECTOR_DEP_YML, selectorNode);
	    
		ObjectNode templateNode = objectMapper.createObjectNode();
		ObjectNode metadataTemplateNode = objectMapper.createObjectNode();
		ObjectNode labelsTemplateNode = objectMapper.createObjectNode();
		labelsTemplateNode.put(DockerKubeConstants.APP_DEP_YML, modelNameYml);
		metadataTemplateNode.set(DockerKubeConstants.LABELS_DEP_YML, labelsTemplateNode);
		
		
		ObjectNode specTempNode = objectMapper.createObjectNode();
		ArrayNode containerArrayNode = templateNode.arrayNode();
		ObjectNode containerNode  = objectMapper.createObjectNode();
		containerNode.put(DockerKubeConstants.NAME_DEP_YML, modelNameYml);
		containerNode.put(DockerKubeConstants.IMAGE_DEP_YML, 
				cutil.getProxyImageName(imageTag, dBean.getDockerProxyHost(), dBean.getDockerProxyPort()));
		
		ArrayNode portsArrayNode = containerNode.arrayNode();
		ObjectNode portsNode  = objectMapper.createObjectNode();
		portsNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.PROTOBUF_API_DEP_YML);
		portsNode.put(DockerKubeConstants.CONTAINERPORT_DEP_YML, dBean.getSingleTargetPort());
		
		portsArrayNode.add(portsNode);
		containerArrayNode.add(containerNode);
		containerNode.set(DockerKubeConstants.PORTS_DEP_YML, portsArrayNode);
		
		ObjectNode imagePullSecretsNode = objectMapper.createObjectNode();
		ArrayNode imageSecretArrayNode = containerNode.arrayNode();
		imagePullSecretsNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.ACUMOS_REGISTRY_DEP_YML);
		imageSecretArrayNode.add(imagePullSecretsNode);
		specTempNode.set(DockerKubeConstants.IMAGEPULLSECRETS_DEP_YML, imageSecretArrayNode);
		
		specTempNode.set(DockerKubeConstants.CONTAINERS_DEP_YML, containerArrayNode);
		
		templateNode.set(DockerKubeConstants.METADATA_DEP_YML, metadataTemplateNode);
		templateNode.set(DockerKubeConstants.SPEC_DEP_YML, specTempNode);
		specNode.set(DockerKubeConstants.TEMPLATE_DEP_YML, templateNode);
		
		kindRootNode.put(DockerKubeConstants.SPEC_DEP_YML, specNode);
		
		String solutionDeployment = yamlMapper.writeValueAsString(kindRootNode);
		logger.debug("solutionDeployment "+solutionDeployment);
		
	    
	  return solutionDeployment;
  }
  
 
  /** getCompositeSolutionService method is used to get service details 
   * @param containerName
   *             - Container name
   * @param imagePort
   *              - image port
   * @param nodeType 
   *            - type of node
   * @param dBen
   *            - DeploymentBean            
   * @return serviceYml
   *               - yml string 
   * @throws Exception
   *               - exception for method
   */
  public String getCompositeSolutionService(String containerName,String imagePort,String nodeType,DeploymentBean dBen)throws Exception{
	  logger.debug("getSingleSolutionService Start");
	    String serviceYml="";
	    
	    ObjectMapper objectMapper = new ObjectMapper();
	    YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
	    ObjectNode apiRootNode = objectMapper.createObjectNode();
	    apiRootNode.put(DockerKubeConstants.APIVERSION_YML, DockerKubeConstants.V_YML);
	    apiRootNode.put(DockerKubeConstants.KIND_YML, DockerKubeConstants.SERVICE_YML);
	    
	    ObjectNode metadataNode = objectMapper.createObjectNode();
		metadataNode.put(DockerKubeConstants.NAMESPACE_YML, DockerKubeConstants.ACUMOS_YML);
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
		    metadataNode.put(DockerKubeConstants.NAME_YML, DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){  
			metadataNode.put(DockerKubeConstants.NAME_YML, DockerKubeConstants.DATABROKER_NAME_YML);
		}else{
			metadataNode.put(DockerKubeConstants.NAME_YML, containerName);
		}
		apiRootNode.set(DockerKubeConstants.METADATA_YML, metadataNode);
		
		ObjectNode specNode = objectMapper.createObjectNode();
		
		ObjectNode selectorNode = objectMapper.createObjectNode();
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
		   selectorNode.put(DockerKubeConstants.APP_YML, DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){ 
			selectorNode.put(DockerKubeConstants.APP_YML, DockerKubeConstants.DATABROKER_NAME_YML);
		}else{
			selectorNode.put(DockerKubeConstants.APP_YML, containerName);
		}
		specNode.set(DockerKubeConstants.SELECTOR_YML, selectorNode);
		if(nodeType!=null && (nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER) 
				|| nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER) || nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME))){
		  specNode.put(DockerKubeConstants.TYPE_YML, DockerKubeConstants.NODE_TYPE_PORT_YML);
		}else{
			specNode.put(DockerKubeConstants.TYPE_YML, DockerKubeConstants.CLUSTERIP_YML);
		}
		ArrayNode portsArrayNode = specNode.arrayNode();
		ObjectNode portsNode = objectMapper.createObjectNode();
		
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
		  portsNode.put(DockerKubeConstants.NAME_YML, DockerKubeConstants.NAME_MCAPI_YML);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){ 
			portsNode.put(DockerKubeConstants.NAME_YML, DockerKubeConstants.NAME_DATABROKER_YML);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){ 
			//NA
		}else{
			portsNode.put(DockerKubeConstants.NAME_YML, DockerKubeConstants.PROTOBUF_API_DEP_YML);
		}
		
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
		 portsNode.put(DockerKubeConstants.NODEPORT_YML, dBen.getBluePrintNodePort());
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
			portsNode.put(DockerKubeConstants.NODEPORT_YML, dBen.getDataBrokerNodePort());
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){	
			portsNode.put(DockerKubeConstants.NODEPORT_YML, dBen.getProbeNodePort());
		}
		
		
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			portsNode.put(DockerKubeConstants.PORT_YML, dBen.getBluePrintPort());
		    portsNode.put(DockerKubeConstants.TARGETPORT_YML, dBen.getBluePrintPort());
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
			portsNode.put(DockerKubeConstants.PORT_YML, dBen.getDataBrokerModelPort());
			portsNode.put(DockerKubeConstants.TARGETPORT_YML, dBen.getDataBrokerTargetPort());
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			portsNode.put(DockerKubeConstants.PORT_YML, dBen.getProbeModelPort());
			portsNode.put(DockerKubeConstants.TARGETPORT_YML, dBen.getProbeTargetPort());
		}else{
			portsNode.put(DockerKubeConstants.PORT_YML, imagePort);
			portsNode.put(DockerKubeConstants.TARGETPORT_YML, dBen.getMlTargetPort());
		}
		portsArrayNode.add(portsNode);
		specNode.set(DockerKubeConstants.PORTS_YML, portsArrayNode);
		apiRootNode.set(DockerKubeConstants.SPEC_YML, specNode);
		serviceYml = yamlMapper.writeValueAsString(apiRootNode);
		logger.debug("solutionDeployment "+serviceYml);
	  return serviceYml;
  }
  /** getCompositeSolutionDeployment method is used to get service details 
   * @param imageTag
   *             - image tag name
   * @param containerName
   *              - Name of container
   * @param imagePort
   *              - image port
   * @param nodeType
   *              - node type of model
   *  @param dBean
   *               - DeploymentBean            
   * @return solutionDeployment
   *             - obj of solution deployment
   * @throws Exception
   *               - exception for method
   */
  public String getCompositeSolutionDeployment(String imageTag,String containerName,String imagePort,String nodeType,DeploymentBean dBean)throws Exception{
	    logger.debug("getSingleSolutionDeployment Start");
	    ObjectMapper objectMapper = new ObjectMapper();
	    YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
	    CommonUtil cutil=new CommonUtil();
	    ObjectNode kindRootNode = objectMapper.createObjectNode();
	    kindRootNode.put(DockerKubeConstants.APIVERSION_DEP_YML, DockerKubeConstants.APPS_V1_DEP_YML);
	    kindRootNode.put(DockerKubeConstants.KIND_DEP_YML, DockerKubeConstants.DEPLOYMENT_DEP_YML);
	    
	    
	    ObjectNode metadataNode = objectMapper.createObjectNode();
		metadataNode.put(DockerKubeConstants.NAMESPACE_DEP_YML, DockerKubeConstants.ACUMOS_DEP_YML);
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			metadataNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){  
			metadataNode.put(DockerKubeConstants.NAME_YML, DockerKubeConstants.DATABROKER_NAME_YML);
		}else{
			metadataNode.put(DockerKubeConstants.NAME_DEP_YML, containerName);
		}
		
		
		ObjectNode labelsNode = objectMapper.createObjectNode();
		
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			labelsNode.put(DockerKubeConstants.APP_DEP_YML, DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){  
			labelsNode.put(DockerKubeConstants.APP_DEP_YML, DockerKubeConstants.DATABROKER_NAME_YML);	
		}else{
			labelsNode.put(DockerKubeConstants.APP_DEP_YML, containerName);
		}
		metadataNode.put(DockerKubeConstants.LABELS_DEP_YML, labelsNode);
		
		kindRootNode.set(DockerKubeConstants.METADATA_DEP_YML, metadataNode);
		
		ObjectNode specNode = objectMapper.createObjectNode();
		specNode.put(DockerKubeConstants.REPLICAS_DEP_YML, 1);
		
		ObjectNode selectorNode = objectMapper.createObjectNode();
		ObjectNode matchLabelsNode = objectMapper.createObjectNode();
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			matchLabelsNode.put(DockerKubeConstants.APP_DEP_YML, DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){ 
			matchLabelsNode.put(DockerKubeConstants.APP_DEP_YML, DockerKubeConstants.DATABROKER_NAME_YML);
		}else{
			matchLabelsNode.put(DockerKubeConstants.APP_DEP_YML, containerName);
		}
		
		selectorNode.set(DockerKubeConstants.MATCHLABELS_DEP_YML, matchLabelsNode);
		
		specNode.set(DockerKubeConstants.SELECTOR_DEP_YML, selectorNode);
	    
		ObjectNode templateNode = objectMapper.createObjectNode();
		ObjectNode metadataTemplateNode = objectMapper.createObjectNode();
		ObjectNode labelsTemplateNode = objectMapper.createObjectNode();
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			labelsTemplateNode.put(DockerKubeConstants.APP_DEP_YML,DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
			labelsTemplateNode.put(DockerKubeConstants.APP_DEP_YML,DockerKubeConstants.DATABROKER_NAME_YML);
		}else{
			labelsTemplateNode.put(DockerKubeConstants.APP_DEP_YML, containerName);
		}
		
		metadataTemplateNode.set(DockerKubeConstants.LABELS_DEP_YML, labelsTemplateNode);
		
		
		ObjectNode specTempNode = objectMapper.createObjectNode();
		ArrayNode containerArrayNode = templateNode.arrayNode();
		ObjectNode containerNode  = objectMapper.createObjectNode();
		ObjectNode containerNodeNginx  = objectMapper.createObjectNode();
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			containerNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.BLUEPRINT_MODELCONNECTOR_NAME);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
			containerNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.DATABROKER_NAME_YML);
		}else{
			containerNode.put(DockerKubeConstants.NAME_DEP_YML, containerName);
		}
		
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			containerNode.put(DockerKubeConstants.IMAGE_DEP_YML,imageTag);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
			containerNode.put(DockerKubeConstants.IMAGE_DEP_YML, 
					cutil.getProxyImageName(imageTag, dBean.getDockerProxyHost(), dBean.getDockerProxyPort()));
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			containerNode.put(DockerKubeConstants.IMAGE_DEP_YML,imageTag);
		}else{
			containerNode.put(DockerKubeConstants.IMAGE_DEP_YML, 
					cutil.getProxyImageName(imageTag, dBean.getDockerProxyHost(), dBean.getDockerProxyPort()));
		}
		
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			ArrayNode envArrayNode = containerNode.arrayNode();
			ObjectNode envNode  = objectMapper.createObjectNode();
			envNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.NEXUSENDPOINT_URL);
			envNode.put(DockerKubeConstants.VALUE, dBean.getNexusEndPointURL());
			ObjectNode envNodeExternal  = objectMapper.createObjectNode();
			envNodeExternal.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.ACUMOS_PROBE_EXTERNAL_PORT);
			envNodeExternal.put(DockerKubeConstants.VALUE, "\""+dBean.getProbeExternalPort()+"\"");
			envArrayNode.add(envNode);
			envArrayNode.add(envNodeExternal);
			containerNode.set(DockerKubeConstants.ENV, envArrayNode);
		}
		
		
		ArrayNode portsArrayNode = containerNode.arrayNode();
		ObjectNode portsNode  = objectMapper.createObjectNode();
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			portsNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.NAME_MCAPI_YML);
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
			//NA
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			portsNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.PROBEAPI_NAME);
		}else{
			portsNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.PROTOBUF_API_DEP_YML);
		}
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
		   portsNode.put(DockerKubeConstants.CONTAINERPORT_DEP_YML, dBean.getBluePrintPort());
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATA_BROKER)){
			portsNode.put(DockerKubeConstants.CONTAINERPORT_DEP_YML, dBean.getDataBrokerTargetPort());
		}else if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			portsNode.put(DockerKubeConstants.CONTAINERPORT_DEP_YML, dBean.getProbeApiPort());
		}else{
			portsNode.put(DockerKubeConstants.CONTAINERPORT_DEP_YML, dBean.getMlTargetPort());
		}
		
		portsArrayNode.add(portsNode);
		/*if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			ObjectNode portsNode2  = objectMapper.createObjectNode();
			portsNode2.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.PROBEAPI_NAME);
			portsNode2.put(DockerKubeConstants.CONTAINERPORT_DEP_YML, dBean.getProbeApiPort());
			portsArrayNode.add(portsNode2);
		}*/
		
		containerNode.set(DockerKubeConstants.PORTS_DEP_YML, portsArrayNode);
		//for Nginx
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			containerNodeNginx.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.NGINX_CONTAINER_NAME);
			containerNodeNginx.put(DockerKubeConstants.IMAGE_DEP_YML, dBean.getNginxImageName());
			ArrayNode portSchemaArrayNode = containerNodeNginx.arrayNode();
			ObjectNode portSchemaNode  = objectMapper.createObjectNode();
			portSchemaNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.PROBE_SCHEMA_YML);
			portSchemaNode.put(DockerKubeConstants.CONTAINERPORT_DEP_YML, dBean.getProbeSchemaPort());
			portSchemaArrayNode.add(portSchemaNode);
			containerNodeNginx.set(DockerKubeConstants.PORTS_DEP_YML, portSchemaArrayNode);
			
		}
		//BLUEPRINT or DataBroker
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			ArrayNode volmeMountArrayNode = containerNode.arrayNode();
			ObjectNode volumeMountNode  = objectMapper.createObjectNode();
			volumeMountNode.put(DockerKubeConstants.MOUNTPATH_DEP_YML, DockerKubeConstants.PATHLOGS_DEP_YML);
			volumeMountNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.LOGS_DEP_YML);
			volmeMountArrayNode.add(volumeMountNode);
			containerNode.set(DockerKubeConstants.VOLUMEMOUNTS_DEP_YML, volmeMountArrayNode);
		}
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATABROKER_NAME)){
			ArrayNode volmeMountArrayNode = containerNode.arrayNode();
			ObjectNode volumeMountNode  = objectMapper.createObjectNode();
			volumeMountNode.put(DockerKubeConstants.MOUNTPATH_DEP_YML, DockerKubeConstants.DATABROKER_PATHLOG_DEP_YML);
			volumeMountNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.DATABROKER_LOGNAME);
			volmeMountArrayNode.add(volumeMountNode);
			containerNode.set(DockerKubeConstants.VOLUMEMOUNTS_DEP_YML, volmeMountArrayNode);
		}
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			ArrayNode volmeMountArrayNode = containerNodeNginx.arrayNode();
			ObjectNode volumeMountNode  = objectMapper.createObjectNode();
			volumeMountNode.put(DockerKubeConstants.MOUNTPATH_DEP_YML, DockerKubeConstants.PROBE_MOUNTPATH_DEP_YML);
			volumeMountNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.VOLUME_PROTO_YML);
			volmeMountArrayNode.add(volumeMountNode);
			containerNodeNginx.set(DockerKubeConstants.VOLUMEMOUNTS_DEP_YML, volmeMountArrayNode);
		}
		//Finish
		
		
		ObjectNode imagePullSecretsNode = objectMapper.createObjectNode();
		ArrayNode imageSecretArrayNode = containerNode.arrayNode();
		imagePullSecretsNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.ACUMOS_REGISTRY_DEP_YML);
		imageSecretArrayNode.add(imagePullSecretsNode);
		specTempNode.set(DockerKubeConstants.IMAGEPULLSECRETS_DEP_YML, imageSecretArrayNode);
		containerArrayNode.add(containerNode);
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
		    containerArrayNode.add(containerNodeNginx);
		}
		specTempNode.set(DockerKubeConstants.CONTAINERS_DEP_YML, containerArrayNode);
		//BLUEPRINT or DataBroker
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.BLUEPRINT_CONTAINER)){
			ArrayNode volumeArrNode = templateNode.arrayNode();
			ObjectNode volumeNode  = objectMapper.createObjectNode();
			volumeNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.LOGS_DEP_YML);
			volumeArrNode.add(volumeNode);
			ObjectNode hostPathNode  = objectMapper.createObjectNode();
			hostPathNode.put(DockerKubeConstants.PATH_DEP_YML, DockerKubeConstants.ACUMOSPATHLOG_DEP_YML);
			volumeNode.put(DockerKubeConstants.HOSTPATH_DEP_YML, hostPathNode);
			specTempNode.put(DockerKubeConstants.RESTARTPOLICY_DEP_YML, DockerKubeConstants.ALWAYS_DEP_YML);
			specTempNode.set(DockerKubeConstants.VOLUMES_DEP_YML, volumeArrNode);
		}
		if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.DATABROKER_NAME)){
			
			ArrayNode volumeArrNode = templateNode.arrayNode();
			ObjectNode volumeNode  = objectMapper.createObjectNode();
			volumeNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.DATABROKER_LOGNAME);
			volumeArrNode.add(volumeNode);
			ObjectNode hostPathNode  = objectMapper.createObjectNode();
			hostPathNode.put(DockerKubeConstants.PATH_DEP_YML, DockerKubeConstants.DATABROKER_PATHLOG_DEP_YML);
			volumeNode.put(DockerKubeConstants.HOSTPATH_DEP_YML, hostPathNode);
			specTempNode.put(DockerKubeConstants.RESTARTPOLICY_DEP_YML, DockerKubeConstants.ALWAYS_DEP_YML);
			specTempNode.set(DockerKubeConstants.VOLUMES_DEP_YML, volumeArrNode);
		}
       if(nodeType!=null && nodeType.equalsIgnoreCase(DockerKubeConstants.PROBE_CONTAINER_NAME)){
			
			ArrayNode volumeArrNode = templateNode.arrayNode();
			ObjectNode volumeNode  = objectMapper.createObjectNode();
			volumeNode.put(DockerKubeConstants.NAME_DEP_YML, DockerKubeConstants.VOLUME_PROTO_YML);
			volumeArrNode.add(volumeNode);
			ObjectNode hostPathNode  = objectMapper.createObjectNode();
			hostPathNode.put(DockerKubeConstants.PATH_DEP_YML, DockerKubeConstants.PROBE_PATHLOG_DEP_YML);
			volumeNode.put(DockerKubeConstants.HOSTPATH_DEP_YML, hostPathNode);
			specTempNode.put(DockerKubeConstants.RESTARTPOLICY_DEP_YML, DockerKubeConstants.ALWAYS_DEP_YML);
			specTempNode.set(DockerKubeConstants.VOLUMES_DEP_YML, volumeArrNode);
		}
		//Finish
		
		templateNode.set(DockerKubeConstants.METADATA_DEP_YML, metadataTemplateNode);
		templateNode.set(DockerKubeConstants.SPEC_DEP_YML, specTempNode);
		specNode.set(DockerKubeConstants.TEMPLATE_DEP_YML, templateNode);
		
		kindRootNode.put(DockerKubeConstants.SPEC_DEP_YML, specNode);
		
		String solutionDeployment = yamlMapper.writeValueAsString(kindRootNode);
		logger.debug("before "+solutionDeployment);
		solutionDeployment=solutionDeployment.replace("'", "");
		logger.debug("After "+solutionDeployment);
	    
	  return solutionDeployment;
}
  /** createSingleSolutionZip method is used to get create zip 
   * @param dBean
   *            - object of deployment bean 
   * @return baos
   *           - byte string
   * @throws Exception
   *             - exception for method
   */
  public byte[] createSingleSolutionZip(DeploymentBean dBean)throws Exception{
	  
	    byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos =null;
		HashMap<String,ByteArrayOutputStream> hmap=new HashMap<String,ByteArrayOutputStream>();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		CommonUtil util=new CommonUtil();
			if(dBean!=null){
					 bOutput = new ByteArrayOutputStream(12);
					 String kubernetesFile=dBean.getFolderPath()+"/"+DockerKubeConstants.KUBE_K8S_SH;
					 String installScript=util.getFileDetails(kubernetesFile);
					 if(installScript!=null && !"".equals(installScript)){
						 bOutput.write(installScript.getBytes());
						 hmap.put(DockerKubeConstants.KUBE_K8S_SH, bOutput);
						 logger.debug(DockerKubeConstants.KUBE_K8S_SH+" "+bOutput);
					 }
					 
					 bOutput = new ByteArrayOutputStream(12);
					 String deployFile=dBean.getFolderPath()+"/"+DockerKubeConstants.KUBE_DEPLOY_SH;
					 String deployScript=util.getFileDetails(deployFile);
					 if(installScript!=null && !"".equals(installScript)){
						 bOutput.write(deployScript.getBytes());
						 hmap.put(DockerKubeConstants.KUBE_DEPLOY_SH, bOutput);
						 logger.debug(DockerKubeConstants.KUBE_DEPLOY_SH+"   "+bOutput);
					 }
					 bOutput = new ByteArrayOutputStream(12);
					 String deployEnvScript=util.getEnvFileDetails(dBean);
					 if(deployEnvScript!=null && !"".equals(deployEnvScript)){
						 bOutput.write(deployEnvScript.getBytes());
						 hmap.put(DockerKubeConstants.KUBE_DEPLOY_ENV_SH, bOutput);
						 logger.debug(DockerKubeConstants.KUBE_DEPLOY_ENV_SH+" "+bOutput);
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
