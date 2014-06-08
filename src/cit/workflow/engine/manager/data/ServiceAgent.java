package cit.workflow.engine.manager.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.webservice.WorkflowServerClient;

public class ServiceAgent implements TreeElement{
	private String name;
	private ServerAgent server;
	private String deployPath;
	private String namePath;
	private int type;
	private List<WorkflowInstanceAgent> workflows;
	
	private int state;
		
	public static final int STATE_STOPPED=0;
	public static final int STATE_RUNNING=1;
	public static final int STATE_INVALID=2;
	
	public static final int TYPE_ENGINE=11;
	public static final int TYPE_OTHERS=10;

	
	public ServiceAgent(ServerAgent server,String name, String deployPath,String namePath,int state,int type) {
		this.name = name;
		this.server = server;
		this.deployPath = deployPath;
		this.namePath=namePath;
		this.state = state;
		this.type=type;
		workflows=new ArrayList<>();
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the server
	 */
	public ServerAgent getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(ServerAgent server) {
		this.server = server;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public List<TreeElement> getChildren() {
		return null;
	}

	@Override
	public TreeElement getParent() {
		return server;
	}

	@Override
	public Image getImage() {
		if(state==STATE_RUNNING)return ImageFactory.getImage(ImageFactory.RUNNING);
		if(state==STATE_STOPPED)return ImageFactory.getImage(ImageFactory.STOPPED);
		return ImageFactory.getImage(ImageFactory.SERVERS);
	}
	
	@Override
	public boolean equals(Object service){
		if(service==null||!(service instanceof ServiceAgent))return false;
		return this.getURL().equals(((ServiceAgent)service).getURL());
	}

	public String getDeployPath() {
		return deployPath;
	}

	public void setDeployPath(String serverPath) {
		this.deployPath = serverPath;
	}


	public String getNamePath() {
		return this.namePath;
	}

	public URL getURL() {
		try {
			URL url1=new URL(server.getURL(),deployPath+"/");
			return new URL(url1,namePath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public URL getWsdlURL(){
		URL url1=null;
		try {
			url1 = new URL(getURL().toString()+"?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url1;
	}


	public void setNamePath(String wsdlPath) {
		this.namePath = wsdlPath;
	}


	public WorkflowServerClient getClient() {
		WorkflowServerClient client=new WorkflowServerClient(getWsdlURL());
		boolean result=client.connect();
		if(!result){
			state=STATE_INVALID;
			System.out.println("Cannot create service client from "+getWsdlURL());
			return null;
		}
		return client;
	}

	public void addWorkflowInstance(WorkflowInstanceAgent workflow){
		workflow.setService(this);
		workflows.add(workflow);
	}
	
	public List<WorkflowInstanceAgent> getWorkflowInstance(){
		return workflows;
	}
	
	public void removeWorkflowInstance(WorkflowInstanceAgent workflow){
		workflows.remove(workflow);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
