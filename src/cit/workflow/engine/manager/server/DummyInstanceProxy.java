package cit.workflow.engine.manager.server;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;

public class DummyInstanceProxy implements InstanceProxyInterface{
	private static int Number=0;
	private RefreshStatusThread refreshThread=null;
	private int location;
	
	public DummyInstanceProxy(int location){
		this.location=location;
	}

	@Override
	public boolean newInstance(IWorkbenchWindow window) {
		String instanceId="dummyserver"+Number;
		Number++;
		ConsoleView.println("Start instance "+instanceId+" on "+ServerAgent.LOCATIONSTRING[location]);
		ServerAgent server=new ServerAgent(null,ServerAgent.STATE_ACTIVATING,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,instanceId);
		server.setName(instanceId);
		ServerList.addServer(server);
		NavigationView.RefreshNavigationView(window);
		startRefreshStatusThread(server, window);
		return true;
	}

	@Override
	public boolean deleteInstance(ServerAgent server, IWorkbenchWindow window) {
		if(server.getLocation()!=this.location){
			ConsoleView.println("Invalid server location");
			return false;
		}
		ConsoleView.println("Success shutdown "+server.getInstanceId());
		ServerList.removeServer(server);
		NavigationView.RefreshNavigationView(window);
		return true;
	}
	
	public void startRefreshStatusThread(ServerAgent server ,IWorkbenchWindow window){
		RefreshStatusThread refreshThread=new RefreshStatusThread(server, window);
		refreshThread.setDaemon(true);
		refreshThread.start();		
	}

	private class RefreshStatusThread extends Thread{
		private IWorkbenchWindow window=null;
		private ServerAgent server;
		private long inteval=60*1000;

		public RefreshStatusThread(ServerAgent server,IWorkbenchWindow window){
			this.window=window;
			this.server=server;
		}

		@Override
		public void run(){
			inteval=(int)(Math.random()*10+10)*1000;
			ConsoleView.println("start thread to trace servers status");
			try {
				Thread.sleep(inteval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			server.setURL("http://localhost:8080");
			ConsoleView.println(server.getName()+" start running, ip:"+server.getURL().toString());
			ServiceAgent service=new ServiceAgent(server, ServiceAgent.STATE_INVALID);
			service.setState(ServiceAgent.STATE_RUNNING);											
			server.addService(service);
			server.setState(ServerAgent.STATE_RUNNING);
			server.recordStartTime();
			RequestAssigner.getInstance().assignRequestToService(server.getEngineSerivce());
			NavigationView.RefreshNavigationView(window);				
		}		
	}	
}
