package com.dockerKube.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dockerKube.beans.ContainerBean;
import com.dockerKube.beans.DeploymentBean;



public interface KubeService {
	
	public ByteArrayOutputStream getBluePrintNexus(String solutionId, String revisionId,String datasource,String userName,String dataPd,
			String nexusUrl,String nexusUserName,String nexusPd) throws  Exception;
	public List<ContainerBean> getprotoDetails(List<ContainerBean> contList,DeploymentBean dBean)throws  Exception;
	public ByteArrayOutputStream getNexusUrlFile(String nexusUrl, String nexusUserName,String nexusPassword,String nexusURI)throws Exception;
	public void getDataBrokerFile(List<ContainerBean> contList,DeploymentBean dBean,String jsonString) throws Exception;
	public void getSolutionYMLFile(List<ContainerBean> contList,DeploymentBean dBean,String jsonString)throws Exception;
	public byte[] createCompositeSolutionZip(DeploymentBean dBean);
	public byte[] createSingleSolutionZip(DeploymentBean dBean);
	public String getSingleSolutionYMLFile(String imageTag)throws Exception;
}
