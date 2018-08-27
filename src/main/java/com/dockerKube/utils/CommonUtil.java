package com.dockerKube.utils;

import java.io.BufferedReader;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
	Logger logger = LoggerFactory.getLogger(CommonUtil.class);
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
	
	public String getFileDetails(String fileDetails) throws Exception{
		String content="";
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

}
