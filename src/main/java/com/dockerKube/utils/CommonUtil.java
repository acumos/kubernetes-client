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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
	Logger logger = LoggerFactory.getLogger(CommonUtil.class);
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

}
