package com.dockerKube.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.dockerKube.beans.Containers;
import com.dockerKube.beans.DeploymentYml;
import com.dockerKube.beans.MetaData;
import com.dockerKube.beans.Ports;
import com.dockerKube.beans.Specification;
import com.dockerKube.parsebean.AzureClientConstants;
import com.dockerKube.parsebean.Blueprint;
import com.dockerKube.parsebean.DataBrokerBean;
import com.dockerKube.parsebean.Node;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
public class TestJava {
	
	
	public static byte[] zipFiles(String zipFileName,HashMap<String,ByteArrayOutputStream> hmap) {
		 
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos =null;
	 
		try {
	 
			//FileOutputStream fos = new FileOutputStream(destZipFile);
			baos= new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
	 
			System.out.println("Creating Zip Archive : " + zipFileName);
			
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
			System.out.println("Done");
	 
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return baos.toByteArray();
	}
	
	public static ByteArrayOutputStream getSolutionFile()throws Exception{
		Blueprint bluePrintProbe=null;
		DataBrokerBean dataBrokerBean=null;
		ParseJSON parseJson=new ParseJSON();
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		dataBrokerBean=parseJson.getDataBrokerContainer(AzureClientConstants.JSON_FILE_NAME);
		bluePrintProbe=parseJson.jsonFileToObject(AzureClientConstants.JSON_FILE_NAME,dataBrokerBean);
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
		
		List<Node> node=bluePrintProbe.getNodes();
		Iterator itr=node.iterator();
		int contPort=8080;
		List<Containers> containers=new ArrayList<Containers>();
		while(itr.hasNext()){
			Node nodevar =(Node)itr.next();
			Containers container=new Containers();
    		container.setName(nodevar.getContainerName());
    		container.setImage(nodevar.getImage());
    		Ports ports=new Ports();
    		
    		ports.setContainerPort(String.valueOf(contPort));
    		container.setPorts(ports);
    		containers.add(container);
    		contPort++;
		}
		
		Containers containerBP=new Containers();
		containerBP.setName("BluePrintContainer");
		containerBP.setImage("nexus3.acumos.org:10003/blueprint-orchestrator:2.0.5-SNAPSHOT");
		Ports portsBP=new Ports();
		
		portsBP.setContainerPort(String.valueOf("8555"));
		containerBP.setPorts(portsBP);
		containers.add(containerBP);
		
		
		
		Specification sp=new Specification();
		sp.setContainers(containers);
		
		MetaData meta=new MetaData();
		meta.setName(bluePrintProbe.getName());
		DeploymentYml dep=new DeploymentYml();
		dep.setApiVersion("apps/v1beta2");
		dep.setKind("Deployment");
		dep.setMetadata(meta);
		dep.setSpec(sp);
		mapper.setSerializationInclusion(Include.NON_NULL);
		String yaml = mapper.writeValueAsString(dep);
		System.out.println(yaml);
		bOutput.write(yaml.getBytes());
		
		return bOutput;
	}
	
	public static ByteArrayOutputStream getBlueprintFile()throws Exception{
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		String data = "";
	    data = new String(Files.readAllBytes(Paths.get(AzureClientConstants.JSON_FILE_NAME)));
	    System.out.println(data);
	    bOutput.write(data.getBytes());
	    return bOutput;
	  }
	
	/*public static void main(String[] args) {
		try{
		 OutputStream outputStream = new FileOutputStream("a.txt");
		 ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		 bOutput.write("hello".getBytes()); 
		 zipFiles("a.txt","b.zip",bOutput);
		 
		}catch(Exception e){
			e.printStackTrace();
		}
		File file = new File("sitemap.xml");
        String zipFileName = "sitemap.zip";
        List<File> fileList=new ArrayList<File>();
        fileList.add(file);
        zipFiles(fileList, zipFileName);
		
		
	}*/
	
	

}
