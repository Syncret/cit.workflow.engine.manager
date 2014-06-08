package cit.workflow.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import cit.workflow.Constants;
import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.views.ConsoleView;

public class RemoteDeployer {
	private static final String PATH=Constants.MANAGER_PATH;
	private static final String DEFAULTXML = "scripts/tomcat.xml";
//	private static final String UNDEPLOYXML="scripts/undeploy_tomcat.xml";
	private static final String DEFAULTBAT="scripts/deploy.bat";
	public static final String TOMCATTEXT="/manager/text";
	public static final String SCRIPTPATH="scripts";
	public static final int DEPLOYTASK=0;
	public static final int UNDEPLOYTASK=1;
	
	public RemoteDeployer(){}
		
	/**
	 * deploy the war with the web service on the remote web server
	 * @param url set the url of the target server
	 * @param serverPath
	 * @param warPath
	 * @param scriptPath
	 * @return the result
	 */
	public boolean callAnt(URL url, String serverPath,String warPath,int task) {
		try {
			url = new URL(url, TOMCATTEXT);
			setParams(url, serverPath,warPath,task);
			Runtime rt = Runtime.getRuntime();
			Process p;
			boolean success = false;
			try {
//				 p=rt.exec("cmd /c start "+PATH+DEFAULTBAT);
				p = rt.exec("cmd /c "+PATH+DEFAULTBAT);
				ConsoleView.println("Invoking ant...");
//				Thread.sleep();
				p.waitFor();
				InputStream fis = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(fis);
				/*
				 * LineNumberReader input=new LineNumberReader(isr); String
				 * line; while((line=input.readLine())!=null)
				 * System.out.println(line);
				 */
				BufferedReader br = new BufferedReader(isr);
				// 直到读完为止
				String msg = null;
				while ((msg = br.readLine()) != null) {
					ConsoleView.println(msg);
					if (msg.contains("BUILD SUCCESSFUL"))
						success = true;
				}
				br.close();
				isr.close();
				fis.close();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			return success;
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		return false;
	}
	
	/**
	 * deploy war from the default path to url
	 * @param url the url to deploy, usually the url of a tomcat server with its port
	 * @param serverPath the name of war file to deploy
	 * @param warPath the path of war file to deploy
	 * @return whether deploy success or not
	 */
	public boolean deploy(URL url, String serverPath,String warPath) {
		return callAnt(url, serverPath, warPath,DEPLOYTASK);
	}
	
	/**
	 * undeploy war from the default path to url
	 * @param url the url to undeploy, usually the url of a tomcat server with its port
	 * @param serverPath the name of war file to deploy
	 * @return whether undeploy success or not
	 */
	public boolean undeploy(URL url, String serverPath) {
		return callAnt(url, serverPath, null,UNDEPLOYTASK);
	}
	
	/**
	 * 修改build.xml文件中的url属性，使ant能够将服务部署到url指定的tomcat服务器
	 * @param url 指定部署的服务器url
	 * @param serverPath the path of the server to deploy or undeploy
	 * @param warPath the path of the war
	 * @param scriptPath 指定build.xml的路径
	 */
	public void setParams(URL url, String serverPath, String warPath,int task){
		String scriptPath=PATH+DEFAULTXML;
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(scriptPath));
			Attribute attr=(Attribute)document.selectSingleNode("/project/@default");
			if(task==DEPLOYTASK)attr.setValue("deploy");
			else if(task==UNDEPLOYTASK)attr.setValue("undeploy");
			attr=(Attribute)document.selectSingleNode("/project/property[@name='url']/@value");
			attr.setValue(url.toString());
			attr=(Attribute)document.selectSingleNode("/project/property[@name='path']/@value");
			attr.setValue(serverPath);
			if(warPath!=null){
				attr=(Attribute)document.selectSingleNode("/project/property[@name='warpath']/@value");
				attr.setValue(warPath);
			}
			XMLWriter writer = new XMLWriter(new FileWriter(new File(scriptPath)));
			writer.write(document);
			writer.close();
			ConsoleView.println("Set url to "+url+" in "+scriptPath);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String args[]) throws MalformedURLException{
//		URL url=new URL("http://192.168.1.62:8080");
//		new RemoteDeployer().deploy(url,"workflow.war");
//	}
}
