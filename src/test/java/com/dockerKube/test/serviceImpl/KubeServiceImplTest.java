package com.dockerKube.test.serviceImpl;


import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dockerKube.beans.DeploymentBean;
import com.dockerKube.serviceImpl.KubeServiceImpl;
import com.dockerKube.utils.CommonUtil;



public class KubeServiceImplTest {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Test	
	public void getSolutionYMLFileTest() throws Exception{
		logger.info("getSolutionYMLFileTest Start");
		CommonUtil cutil=new CommonUtil();
		KubeServiceImpl impl=new KubeServiceImpl();
		String json=cutil.getFileDetails("blueprint.json");
		DeploymentBean dBean =new DeploymentBean();
		dBean.setProbeImage("repo/probeImage:1");
		dBean.setProbePort("5006");
		dBean.setBluePrintImage("repo/blueprintImage:1");
		dBean.setBluePrintPort("8555");
		dBean.setIncrementPort("8557");
		
		impl.getSolutionYMLFile(dBean, json);
		assertNotNull(dBean.getSolutionYml());
		logger.info("getSolutionYMLFileTest End");
		
	}
	@Test	
	public void createCompositeSolutionZipTest() throws Exception{
		logger.info("createCompositeSolutionZipTest Start");
		byte[] byteArray =null;
		DeploymentBean dBean =new DeploymentBean();
		KubeServiceImpl impl=new KubeServiceImpl();
		dBean.setFolderPath("deploy/private");
		dBean.setBluePrintjson("blueprit.json");
		dBean.setDockerInfoJson("dockerinfo.json");
		dBean.setSolutionYml("solution.yml");
		dBean.setDataBrokerJson("dataBroker.json");
		byteArray=impl.createCompositeSolutionZip(dBean);
		assertNotNull(byteArray);
		logger.info("createCompositeSolutionZipTest End");
		
	}
	@Test	
	public void getSingleSolutionYMLFileTest() throws Exception{
		logger.info("getSingleSolutionYMLFileTest Start");
		DeploymentBean dBean =new DeploymentBean();
		KubeServiceImpl impl=new KubeServiceImpl();
		dBean.setSingleModelPort("8556");
		dBean.setSingleTargetPort("3330");
		dBean.setDockerProxyHost("http://host");
		dBean.setDockerProxyPort("4243");
		impl.getSingleSolutionYMLFile("repo/image:1", "30333", dBean);
		logger.info("getSingleSolutionYMLFileTestt End");
	}
}
