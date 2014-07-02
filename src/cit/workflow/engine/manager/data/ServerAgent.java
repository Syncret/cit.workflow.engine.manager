package cit.workflow.engine.manager.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import cit.workflow.engine.manager.util.ImageFactory;

public class ServerAgent implements TreeElement{
	private URL url;
	private int state;
	private int type;
	private String name;
	private List<ServiceAgent> services;
	private ServiceAgent engineSerivce=null;
	private List<WorkflowInstanceAgent> workflows;
	
	public static final int STATE_STOPPED=0;
	public static final int STATE_RUNNING=1;
	public static final int STATE_INVALID=2;
	public static final int STATE_AVAILABLE=3;
	public static final int STATE_ACTIVATING=4;
	public static final int STATE_SHUTTING=5;
	
	public static final int TYPE_SMALL=0;
	public static final int TYPE_MIDDLE=1;
	public static final int TYPE_BIG=2;
	public static final int[] SERVICECAPACITY={20,40,60};
	public static final int[] ACTIVETIME={40,100,120};
	public int getActiveTime(){return ACTIVETIME[type];}
	
	public ServerAgent(){}
	public ServerAgent(URL server,int state,int type){
		this.url=server;
		this.state=state;
		this.setType(type);
		services=new ArrayList<ServiceAgent>();
		workflows=new ArrayList<WorkflowInstanceAgent>();
	}
	
	

	
	public ServerAgent(String server,int state,int type){
		try {
			this.url=new URL(server);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.state=state;
		this.setType(type);
		services=new ArrayList<ServiceAgent>();
		workflows=new ArrayList<WorkflowInstanceAgent>();
	}	
	
	public URL getURL() {
		return url;
	}
	public void setURL(URL url) {
		this.url = url;
	}
	public URL getServer(){
		return url;
	}
	public void setServer(URL server){
		this.url=server;
	}
	public int getPort(){
		return url.getPort();
	}

	public int getState(){
		return state;
	}
	public void setState(int state){
		this.state=state;
	}

	@Override
	public String getName() {
		if(name==null)name=url.getHost();
		return name;
	}
	@Override
	public boolean hasChildren() {
		if(services!=null&&services.size()>0)return true;
		return false;
	}
	@Override
	public List<ServiceAgent> getChildren() {
		return services;
	}
	
	public void addService(ServiceAgent service){
		if(services==null)services=new ArrayList<ServiceAgent>();
		service.setServer(this);
		if(!services.contains(service))
			services.add(service);
		if(service.getState()==ServiceAgent.STATE_RUNNING)
			this.setState(STATE_RUNNING);
		if(service.getType()==ServiceAgent.TYPE_ENGINE){
			this.engineSerivce=service;
			service.setCapacity(SERVICECAPACITY[type]);
		}
	}
	
	public void removeService(ServiceAgent service){
		services.remove(service);
	}
	
	public List<ServiceAgent> getServices(){
		return services;
	}
	
	public void addWorkflowInstance(WorkflowInstanceAgent workflow){
		workflows.add(workflow);
	}
	
	public List<WorkflowInstanceAgent> getWorkflowInstance(){
		return workflows;
	}
	@Override
	public TreeElement getParent() {
		return ServerList.getInstance();
	}
	@Override
	public Image getImage() {
		if(state==STATE_RUNNING) return ImageFactory.getImage(ImageFactory.SERVER_RUNNING);
		if(state==STATE_STOPPED||state==STATE_INVALID||state==STATE_SHUTTING) return ImageFactory.getImage(ImageFactory.SERVER_STOPPED);
		if(state==STATE_ACTIVATING||state==STATE_AVAILABLE) return ImageFactory.getImage(ImageFactory.GREENCIRCLE);
		return ImageFactory.getImage(ImageFactory.SERVER);
	}
	
	@Override
	public boolean equals(Object other){
		if(this==other)return true;
		if(other==null)return false;
		if(!(other instanceof ServerAgent))return false;
		return this.getURL().equals(((ServerAgent)other).getURL());
	}
	public ServiceAgent getEngineSerivce() {
		return engineSerivce;
	}
	public void setEngineSerivce(ServiceAgent engineSerivce) {
		this.engineSerivce = engineSerivce;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setName(String name) {
		this.name = name;
	}

}
