package cit.workflow.engine.manager.data;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.server.AliyunInstanceProxy;
import cit.workflow.engine.manager.server.AwsInstanceProxy;
import cit.workflow.engine.manager.util.ImageFactory;

public class ServerAgent implements TreeElement{
	private URL url;
	private int state;
	private int type;
	private int location;
	private String name;
	private String instanceId="";
	private List<ServiceAgent> services;
	private ServiceAgent engineSerivce=null;
	private List<WorkflowInstanceAgent> workflows;
	private long starttime;
	
	public static final int STATE_STOPPED=0;
	public static final int STATE_RUNNING=1;
	public static final int STATE_INVALID=2;
	public static final int STATE_AVAILABLE=3;
	public static final int STATE_ACTIVATING=4;
	public static final int STATE_SHUTTING=5;
	private static final String[] STATESTRING={"Stopped","Running","Invalid","Available","Activating","Shutting"};
	
	public static final int TYPE_MICRO=0;
	public static final int TYPE_SMALL=1;
	public static final int TYPE_MIDDLE=2;
	public static final int TYPE_BIG=3;
	
	public static final int LOC_LOCAL=0;
	public static final int LOC_AWSEC2=1;
	public static final int LOC_ALIYUN=2;
	public static final String[] LOCATIONSTRING={"local","AWS EC2","Aliyun ECS"};
	
	public static final int[] SERVICECAPACITY={4,20,40,60};
	public static final int[] ACTIVETIME={40,100,120};
	public int getActiveTime(){return ACTIVETIME[type];}
	
	public ServerAgent(){}
	public ServerAgent(URL server,int state,int type){
		this.url=server;
		this.state=state;
		this.setType(type);
		this.setLocation(LOC_LOCAL);
		this.setInstanceId("");
		services=new ArrayList<ServiceAgent>();
		workflows=new ArrayList<WorkflowInstanceAgent>();
	}
	
	public ServerAgent(String server,int state,int type,int location,String instanceId){
		initialize(server, state, type, location, instanceId);
	}
		
	public ServerAgent(String server,int state,int type){
		try {
			this.url=new URL(server);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.state=state;
		this.setType(type);
		this.setLocation(LOC_LOCAL);
		this.setInstanceId("");
		services=new ArrayList<ServiceAgent>();
		workflows=new ArrayList<WorkflowInstanceAgent>();
	}	
	
	private void initialize(String server,int state,int type,int location,String instanceId){
		if(server==null||server=="")this.url=null;
		else {
			try {
				this.url = new URL(server);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		this.state=state;
		this.setType(type);
		this.setLocation(location);
		this.setInstanceId(instanceId);
		this.setStarttime(System.currentTimeMillis());
		services=new ArrayList<ServiceAgent>();
		workflows=new ArrayList<WorkflowInstanceAgent>();
	}
	
	public int getLocation() {
		return location;
	}
	
	public String getLocationString(){
		return LOCATIONSTRING[location];
	}

	public void setLocation(int location) {
		this.location = location;
	}
	
	public URL getURL() {
		return url;
	}
	public void setURL(URL url) {
		this.url = url;
	}
	
	public void setURL(String url){
		try {
			this.url=new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
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
		if(name==null||name.equals("")){
			if(instanceId=="")name=url.getHost();
			else name=instanceId;
		}
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
		if(location==LOC_LOCAL){
			if(state==STATE_RUNNING) return ImageFactory.getImage(ImageFactory.SERVER_RUNNING);
			if(state==STATE_STOPPED||state==STATE_INVALID||state==STATE_SHUTTING) return ImageFactory.getImage(ImageFactory.SERVER_STOPPED);
			if(state==STATE_ACTIVATING||state==STATE_AVAILABLE) return ImageFactory.getImage(ImageFactory.SERVER);
			return ImageFactory.getImage(ImageFactory.SERVER);
		}
		else if(location==LOC_AWSEC2){
			if(state==STATE_RUNNING) return ImageFactory.getImage(ImageFactory.CLUSTER_RUNNING);
			if(state==STATE_STOPPED||state==STATE_INVALID||state==STATE_SHUTTING) return ImageFactory.getImage(ImageFactory.CLUSTER_STOPPED);
			if(state==STATE_ACTIVATING||state==STATE_AVAILABLE) return ImageFactory.getImage(ImageFactory.CLUSTER);
			return ImageFactory.getImage(ImageFactory.CLUSTER);
		}
		return ImageFactory.getImage(ImageFactory.SERVER);
	}
	
	@Override
	public boolean equals(Object other){
		if(this==other)return true;
		if(other==null)return false;
		if(!(other instanceof ServerAgent))return false;
		if(this.getName()!=((ServerAgent)other).getName())return false;
		if(this.getLocation()!=((ServerAgent)other).getLocation())return false;
		if(this.getLocation()==ServerAgent.LOC_AWSEC2)return this.getInstanceId().equals(((ServerAgent)other).getInstanceId());
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

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getStateString() {
		return STATESTRING[this.state];
	}
	
	public boolean testConnection(){
		int result=-1;
		try {
			HttpURLConnection connection=(HttpURLConnection) url.openConnection();
			result=connection.getResponseCode();
			connection.disconnect();
		} catch (IOException e) {
			return false;
		}
		if(result==200)return true;
		else return false;
	}

	//
	public double getLeftPayingTime() {
		long runningTime=System.currentTimeMillis()-starttime;
		double inhour=runningTime/1000/60/60.0;
		inhour%=1;
		return 1-inhour;
	}
	
	public double getPayingTime(){
		long runningTime=System.currentTimeMillis()-starttime;
		double inhour=runningTime/1000/60/60.0;
		return inhour;
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}
	
	public void delete(IWorkbenchWindow window){
		if(this.getLocation()==ServerAgent.LOC_AWSEC2){
			AwsInstanceProxy.getInstance().deleteInstance(this,window);
		}
		else if(this.getLocation()==ServerAgent.LOC_ALIYUN){
			AliyunInstanceProxy.getInstance().deleteInstance(this,window);
		}
		else{
			ServerList.removeServer(this);
		}
	}
}
