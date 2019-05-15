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
package com.dockerKube.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dockerKube.beans.DeploymentBean;

import java.net.InetAddress;

public class CommonUtil {
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** 
	 * Extract the model name from the given imageName and solutionId.
	 * Following format based on docker image path is expected:
	 *   host:port/modelName_solutionId:version
	 * For example,
	 *   acumos-aio-1:30883/face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
	 * 
	 * @param imageName
	 *            - image name
	 * @param solutionId
	 * 						- solutionId
	 * @return modelName.
	 *              - model name derived from the imageName
	 *      	      - returns null, if cannot derived from the expected imageName format
	 */
	public String getModelName(String imageName, String solutionId){
		logger.debug("Start-getModelName: imgName:" + imageName + ", solutionId:" + solutionId);
		String modelName = null;
		if (imageName != null) {
			// imageName=acumos-aio-host:30883/face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
			String[] imageArr = imageName.split("/");
			if (imageArr != null && imageArr.length >= 2 && imageArr[1] != null) {
				String[] imageNameArr = imageArr[1].split(":");
				if (imageNameArr != null && imageNameArr[0] != null) {
					// imageFullName derived as face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
					String imageFullName = imageNameArr[0];
					Pattern p = Pattern.compile("(.*)(" + solutionId + ")");
					Matcher m = p.matcher(imageFullName);
					if (m.matches()) {
						// modelName as face_privacy_filter_detect_
						modelName = m.group(1);
						if (modelName != null) {
							if ((modelName.endsWith("_") || modelName.endsWith("-"))) {
								modelName = modelName.substring(0, modelName.length() - 1);
							}
							// make dns-compliant i.e. replace '_' with '-'
							modelName = modelName.replaceAll("_", "-");
							logger.debug("-getModelName " + modelName);
						}
					}
				}
			}
		}
		
		logger.debug(" End-getModelName " + modelName);
		return modelName;
	}
	/** geContainerName method is used to get containerName
	 * @param imageName
	 *            - image name
	 * @return containerName.
	 *              - container name
	 */
	public String geContainerName(String imageName){
		logger.debug("Start-geContainerName "+imageName);
		String containerName="";
		String repositaryName="";
		if(imageName!=null){
			String imageArr[]=imageName.split("/");
			if(imageArr!=null && imageArr[1]!=null){
				String imageNameArr[]=imageArr[1].split(":");
				if(imageNameArr!=null && imageNameArr[0]!=null){
					containerName=imageNameArr[0];
				}
			}
		}
		
		logger.debug(" End containerName "+containerName);
		return containerName;
	  }
	public String getProxyImageName(String imageName,String dockerProxyHost,String dockerProxyPort){
		logger.debug("Start-geProxyImageName "+imageName);
		String dockerImage="";
		String image="";
		if(imageName!=null){
			String imageArr[]=imageName.split("/");
			if(imageArr!=null && imageArr[1]!=null){
				image=imageArr[1];
			}
		}
		logger.debug("image "+image);
		if(image!=null && !"".equals(image) && dockerProxyHost!=null && dockerProxyPort!=null){
			dockerImage=dockerProxyHost+":"+dockerProxyPort+"/"+image;
		}
		
		logger.debug(" end geProxyImageName dockerImage"+dockerImage);
		return dockerImage;
	  }
	/** getFileDetails method is used to get file details
	 * @param fileDetails
	 *             - file Details 
	 * @return content
	 *           - content of string
	 * @throws Exception
	 *               - exception thrown          
	 */
	public String getFileDetails(String fileDetails) throws Exception{
		String content="";
		logger.debug("fileDetails "+fileDetails);
		BufferedReader reader = new BufferedReader(new FileReader(fileDetails));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		// delete the last new line separator
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		reader.close();

		content = stringBuilder.toString();
		return content;
	}
	/** iterateImageMap method is used to get file details
	 * @param imageMap
	 *             - map of image and container
	 * @return list
	 *           - List of image names
	 */
	public ArrayList<String> iterateImageMap(HashMap<String,String> imageMap){
		logger.debug("iterateImageMap ");
		logger.debug("imageMap "+imageMap);
		ArrayList<String> list=new ArrayList<String>();
		 Iterator it = imageMap.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        logger.debug(pair.getKey() + " = " + pair.getValue());
		        if(pair.getKey()!=null){
		        	list.add((String)pair.getKey());
		        }
		    }
		logger.debug("list "+list);    
		logger.debug(" iterateImageMap End ");
		return list;
	}
	
	public String getEnvFileDetails(DeploymentBean dBean) throws Exception{
		logger.debug("getEnvFileDetails Start ");

		Map<String, String> solRevIdMap = dBean.getSolutionRevisionIdMap();
		String solId = "";
		String solRevId = "";
		String compSolId = "";
		String compRevId = "";
		if (solRevIdMap != null) {
			for (String solIdKey: solRevIdMap.keySet()) {
				if (solRevId.length() > 0) {
					solId += ",";
					solRevId += ",";
				}
				solId += solIdKey;
				solRevId += solIdKey + ":" + solRevIdMap.get(solIdKey);
			}

			// more than one entry for composite case
			if (solRevIdMap.keySet().size() > 1) {
				compSolId = dBean.getSolutionId();
				compRevId = dBean.getSolutionRevisionId();
			}
		}

		// # Single model
		//   - SOLUTION_ID=<SINGLE_MDL_SOL_ID>
		//   - SOL_REVISION_ID=<SINGLE_MDL_REVISION_ID>
		// # Composite model
		//   - SOLUTION_ID=<COMP_MDL_SOL_ID>,<MDL_1_SOL_ID>,<MDL_2_SOL_ID>,...
		//   - SOL_REVISION_ID=<COMP_MDL_SOL_ID>:<COMP_MDL_REVISION_ID>,<MDL_1_SOL_ID>:<MDL_1_REVISION_ID>,<MDL_2_SOL_ID>:<MDL_2_REVISION_ID>,...
		String setEnvDeploy = ""
				+ "#!/bin/bash \n"
				+ "export SOLUTION_ID="+solId+"\n"
				+ "export SOL_REVISION_ID="+solRevId+"\n"
				+ "export LOGSTASH_HOST="+dBean.getLogstashHost()+"\n"
				+ "export LOGSTASH_PORT="+dBean.getLogstashPort()+"\n";
		
		if (compSolId != null && compSolId.length() > 0) {
			setEnvDeploy += "export COMP_SOLUTION_ID="+compSolId+"\n"
				+ "export COMP_REVISION_ID="+compRevId+"\n";
		}

		logger.debug("getEnvFileDetails End " +setEnvDeploy);
		return setEnvDeploy;
	}
	
	/** 
	 * Parse the given image path and extract values.
	 * Following format based on docker image path is expected:
	 *   host:port/modelName_solutionId:version
	 * For example,
	 *   acumos-aio-1:30883/face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
	 * 
	 * @param imageName
	 *            - image name
	 * @return map with key/value pair for each parsed item with key as
	 * 							- DockerKubeConstants.DOCKER_HOST
	 * 							- DockerKubeConstants.DOCKER_PORT
	 * 							- DockerKubeConstants.MODEL_NAME
	 * 							- DockerKubeConstants.SOLUTION_ID
	 * 							- DockerKubeConstants.VERSION
	 *      	 - returns empty map, if cannot derived from the expected imageName format
	 */
	public static Map<String, String> parseImageToken(String imageName) throws Exception {
		logger.debug("Start-parseImageToken: imgName:" + imageName);
		Map<String, String> map = new HashMap<String, String>();
		if (imageName != null) {
			// imageName=acumos-aio-host:30883/face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
			String[] imageArr = imageName.split("/");
			if (imageArr.length >= 2) {

				// extract docker host:port info
				String[] dockerInfoArr = imageArr[0].split(":");
				if (dockerInfoArr.length > 0) {
          map.put(DockerKubeConstants.DOCKER_HOST, dockerInfoArr[0]);
          if (dockerInfoArr.length > 1) {
            map.put(DockerKubeConstants.DOCKER_PORT, dockerInfoArr[1]);
          }
        }

        // extract modelName:solutionId:version info
				String[] imageNameArr = imageArr[1].split(":");
				if (imageNameArr.length > 0) {
          if (imageNameArr.length > 1) {
            map.put(DockerKubeConstants.VERSION, imageNameArr[1]);
          }
					// modelSolTkn derived as face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5
					String modelSolTkn = imageNameArr[0];
          int underscoreLastIndex = modelSolTkn.lastIndexOf("_");
          if (underscoreLastIndex == -1) {
            throw new Exception("cannot find the solutionId match from input imagePath " + imageName);
          } else {
            String modelName = modelSolTkn.substring(0, underscoreLastIndex);
						String solutionId = modelSolTkn.substring(underscoreLastIndex + 1);
						
						// make dns-compliant i.e. replace '_' with '-'
						modelName = modelName.replaceAll("_", "-");

            map.put(DockerKubeConstants.MODEL_NAME, modelName);
            map.put(DockerKubeConstants.SOLUTION_ID, solutionId);
          }
				}
			}
		}
		
		logger.debug(" End-parseImageToken " + map.get("dockerHost") 
			+ "::" + map.get("dockerPort") + "::" + map.get("modelName") 
			+ "::" + map.get("solutionId") + "::" + map.get("version"));
		return map;
	}

}
