package cit.workflow.engine.manager.server;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.webservice.AwsUtility;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;

public class AwsInstanceProxy implements InstanceProxyInterface{
	private static final AwsInstanceProxy proxy=new AwsInstanceProxy();
	public static AwsInstanceProxy getInstance(){return proxy;}
	
	private AwsUtility utility=AwsUtility.GetInstance();
	private RefreshStatusThread refreshThread=null;
	
	
	@Override
	public boolean newInstance(IWorkbenchWindow window) {
		Instance instance=utility.runInstance();
		ConsoleView.println("Start instance "+instance.getInstanceId()+" on AWS EC2");
		ServerAgent server=new ServerAgent(null,ServerAgent.STATE_ACTIVATING,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,instance.getInstanceId());
		server.setName(instance.getInstanceId());
		//ServerAgent server=new ServerAgent(null,ServerAgent.STATE_ACTIVATING,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,"aaa");
		ServerList.addServer(server);
		NavigationView.RefreshNavigationView(window);
    	startRefreshStatusThread(window);
    	return true;
	}
	
	@Override
	public boolean deleteInstance(ServerAgent server,IWorkbenchWindow window) {
		if(server.getLocation()!=ServerAgent.LOC_AWSEC2||server.getInstanceId()==null||server.getInstanceId().equals("")){
			ConsoleView.println("Invalid EC2 server");
			return false;
		}
		InstanceState state=utility.terminateInstance(server.getInstanceId());
		int stateCode=state.getCode()%128;
		if(stateCode==32||stateCode==48){
			ConsoleView.println("Success shutdown "+server.getInstanceId()+", current state:"+state.getName());
			ServerList.removeServer(server);
		}
		else{
			ConsoleView.println("Failed to shutdown "+server.getInstanceId()+", current state:"+state.getName());
			if(stateCode!=16)ServerList.removeServer(server);
		}
		NavigationView.RefreshNavigationView(window);
		return true;
	}
	
	
	public void startRefreshStatusThread(IWorkbenchWindow window){
		if(refreshThread==null||!refreshThread.isAlive()){
			refreshThread=new RefreshStatusThread(window);
			refreshThread.setDaemon(true);
			refreshThread.start();		
		}
	}
	
	

	private class RefreshStatusThread extends Thread{
		private IWorkbenchWindow window=null;
		private long inteval=60*1000;

		public RefreshStatusThread(IWorkbenchWindow window){
			this.window=window;
		}

		@Override
		public void run(){
			try {
				boolean loop=true;
				ConsoleView.println("start thread to trace servers status");
				while(loop){
					loop=false;
					Thread.sleep(inteval);
					List<Instance> instances=utility.getInstancesStatus();
					for (ServerAgent server : ServerList.getServers()) {
						if (server.getLocation() == ServerAgent.LOC_AWSEC2 && server.getState() == ServerAgent.STATE_ACTIVATING) {
							boolean found = false;
							for(Instance instance:instances){
								if(instance.getInstanceId().equals(server.getInstanceId())){
									ConsoleView.println(instance.getInstanceId()+" status:"+instance.getState().getName());
									found=true;
									if(instance.getState().getCode()%128!=AwsUtility.STATE_RUNNING){
										loop=true;
										break;
									}
									if(server.getURL()==null){
										server.setURL("http://"+instance.getPublicIpAddress()+":8080");
										ConsoleView.println("Get public IP of "+instance.getInstanceId()+": "+instance.getPublicIpAddress());
									}
									if(server.testConnection()){
										ConsoleView.println(server.getName()+" start running, ip:"+server.getURL().toString());
										ServiceAgent service=new ServiceAgent(server, ServiceAgent.STATE_INVALID);
										if(service.testConnection()){
											ConsoleView.println(service.getWsdlURL()+" connect success, service started");
											service.setState(ServiceAgent.STATE_RUNNING);											
											server.addService(service);
											server.setState(ServerAgent.STATE_RUNNING);
											RequestAssigner.getInstance().assignRequestToService(server.getEngineSerivce());
										}
										else {
											loop=true;
											ConsoleView.println(service.getWsdlURL()+" connect failed, Retry later");
										}
									}
									else {
										loop=true;
										ConsoleView.println(server.getName()+" connect failed, ip:"+server.getURL().toString()+". Retry later");
									}
									NavigationView.RefreshNavigationView(window);
								}
							}
							if(!found){
								ConsoleView.println(server.getName()+" cannot be found! Remove from Server List");
								ServerList.removeServer(server);
								break;
							}
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally{
				ConsoleView.println("thread complete");				
			}
		}
		
	}
	

}
