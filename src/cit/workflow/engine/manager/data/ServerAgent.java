package cit.workflow.engine.manager.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import cit.workflow.engine.manager.image.ImageFactory;

public class ServerAgent implements TreeElement{
	private URL url;
	private int state;
	private List<ServiceAgent> services;
	private List<WorkflowInstanceAgent> workflows;
	
	public static final int STATE_STOPPED=0;
	public static final int STATE_RUNNING=1;
	
	public ServerAgent(){}
	public ServerAgent(URL server,int state){
		this.url=server;
		this.state=state;
		services=new ArrayList<ServiceAgent>();
		workflows=new ArrayList<WorkflowInstanceAgent>();
	}
	
	
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
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
		return url.getHost();
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
		services.add(service);
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
		if(state==STATE_STOPPED) return ImageFactory.getImage(ImageFactory.SERVER_STOPPED);
		return ImageFactory.getImage(ImageFactory.SERVER);
	}

}
