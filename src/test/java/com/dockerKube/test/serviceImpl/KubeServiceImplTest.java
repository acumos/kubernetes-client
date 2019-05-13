package com.dockerKube.test.serviceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dockerKube.beans.DeploymentBean;
import com.dockerKube.serviceImpl.KubeServiceImpl;
import com.dockerKube.utils.CommonUtil;
import com.dockerKube.utils.DockerKubeConstants;



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
		dBean.setSingleNodePort("30333");
		dBean.setSingleModelPort("8556");
		dBean.setSingleTargetPort("30333");
		dBean.setDockerProxyHost("http://host");
		dBean.setDockerProxyPort("4243");
		dBean.setSingleTargetPort("30333");
		impl.getSingleSolutionYMLFile("repo/image:1", "30333", dBean);
		logger.info("getSingleSolutionYMLFileTestt End");
	}
	@Test	
	public void getSingleSolutionYMLFileTest2() throws Exception{
		logger.info("getSingleSolutionYMLFileTest2 Start");
		DeploymentBean dBean =new DeploymentBean();
		KubeServiceImpl impl=new KubeServiceImpl();
		dBean.setSingleNodePort("30333");
		dBean.setSingleModelPort("8556");
		dBean.setSingleTargetPort("30333");
		dBean.setDockerProxyHost("http://host");
		dBean.setDockerProxyPort("4243");
		dBean.setSingleTargetPort("30333");
		dBean.setSolutionId("123-abc-456-xyz-7a8b9c");
		
		// positive assertion as per the expected imageTag format
		String imageTag 
			= "acumos-aio-host:30883/face_privacy_filter_detect_123-abc-456-xyz-7a8b9c:1";
		String solutionYaml = impl.getSingleSolutionYMLFile(imageTag, "30333", dBean);
		String expectedModelName = "face-privacy-filter-detect";
		// logger.info("solutionYaml -\n" + solutionYaml);
		assertTrue("Cannot derive correct modelName from the imageTag", 
			solutionYaml.indexOf("name: " + expectedModelName) != -1);

		// negative assertion as per the un-expected imageTag format
		imageTag = "acumos-aio-host:30883/someName";
		solutionYaml = impl.getSingleSolutionYMLFile(imageTag, "30333", dBean);
		// logger.info("solutionYaml -\n" + solutionYaml);
		// solutionYaml shall have default modelName i.e. mymodel
		assertTrue("Unexpected modelName", 
			solutionYaml.indexOf("name: " + DockerKubeConstants.MYMODEL_YML) != -1);

		logger.info("getSingleSolutionYMLFileTestt End");
	}
}
