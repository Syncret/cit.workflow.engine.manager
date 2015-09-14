package cit.workflow.engine.manager.server;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.webservice.AliyunUtility;
import cit.workflow.webservice.AwsUtility;

import com.aliyun.api.ecs.ecs20140526.response.DescribeInstanceAttributeResponse;
import com.amazonaws.services.ec2.model.Instance;


public class AliyunInstanceProxy implements InstanceProxyInterface{
	private static final AliyunInstanceProxy proxy=new AliyunInstanceProxy();
	public static AliyunInstanceProxy getInstance(){return proxy;}
	
	private AliyunUtility utility=AliyunUtility.getInstance();
	
	
	@Override
	public boolean newInstance(IWorkbenchWindow window) {
		String instanceId=utility.createInstance();
		boolean result=false;
		if(!instanceId.isEmpty()){
			result=utility.startInstance(instanceId);
		}
		if(instanceId.isEmpty()||!result){
			ConsoleView.println("Start instance on Aliyun Failed");
			return false;
		}
		ServerAgent server=new ServerAgent(null,ServerAgent.STATE_ACTIVATING,ServerAgent.TYPE_MICRO,ServerAgent.LOC_ALIYUN,instanceId);
		server.setName(instanceId);
		ServerList.addServer(server);
		ConsoleView.println("New instance "+instanceId+" on Aliyun Success, starting");
		NavigationView.RefreshNavigationView(window);
    	startRefreshStatusThread(server,RefreshStatusThread.START,window);
    	return true;
	}
	
	@Override
	public boolean deleteInstance(ServerAgent server,IWorkbenchWindow window) {
		if(server.getLocation()!=ServerAgent.LOC_ALIYUN||server.getInstanceId()==null||server.getInstanceId().isEmpty()){
			ConsoleView.println("Invalid Aliyun server");
			return false;
		}
		boolean result=false;
		result=utility.stopInstance(server.getInstanceId());
		server.setState(ServerAgent.STATE_SHUTTING);
		NavigationView.RefreshNavigationView(window);
		if(result){
			ConsoleView.println("Stopping instance "+server.getInstanceId()+" on Aliyun");
			startRefreshStatusThread(server, RefreshStatusThread.DELETE,window);
		}
		else {
			ConsoleView.println("Stop instance "+server.getInstanceId()+" failed");
		}
		return result;
	}
	
	
	public void startRefreshStatusThread(ServerAgent server,int target,IWorkbenchWindow window){
		RefreshStatusThread refreshThread=new RefreshStatusThread(server, target,window);
		refreshThread.setDaemon(true);
		refreshThread.start();		
	}
	
	

	private class RefreshStatusThread extends Thread{
		private IWorkbenchWindow window=null;
		private ServerAgent server;
		private long inteval=30*1000;		
		public static final int START=0;
		public static final int DELETE=1;
		private int target=0;

		public RefreshStatusThread(ServerAgent server, int target,IWorkbenchWindow window){
			this.server=server;
			this.target=target;
			this.window=window;
		}

		@Override
		public void run(){
			try {
				boolean loop=true;
				String instanceId=server.getInstanceId();
				if(instanceId.isEmpty()){
					ConsoleView.println("Invalid InstanceId");
					return;
				}
				ConsoleView.println("start thread to trace server status");
				while(loop){
					loop=true;
					Thread.sleep(inteval);
					DescribeInstanceAttributeResponse response=utility.describeInstanceAttribute(instanceId);
					String status=response.getStatus();
					ConsoleView.println(server.getName()+" status:"+status);
					if(target==START){
						if(status.equals("Running")){
							if(server.getURL()==null){
//								String ip=response.getInnerIpAddress().get(0).getIpAddress();//inner ip
								Object to=response.getInnerIpAddress().get(0);
								String ip=(String)to;
	//							String ip=response.getPublicIpAddress().get(0).getIpAddress();//public ip
								server.setURL("http://"+ip+":8080");
								ConsoleView.println("Get IP of "+instanceId+": "+ip);							
							}
							server.setState(ServerAgent.STATE_RUNNING);
							ConsoleView.println(server.getName()+" start running, ip:"+server.getURL().toString());
							server.recordStartTime();
							NavigationView.RefreshNavigationView(window);
							loop=false;
						}
					}
					else if(target==DELETE){
						if(status.equals("Stopped")){
							ConsoleView.println(server.getName()+" stopped, delete it");
							utility.deleteInstance(instanceId);
							ServerList.removeServer(server);
							NavigationView.RefreshNavigationView(window);
							loop=false;
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
