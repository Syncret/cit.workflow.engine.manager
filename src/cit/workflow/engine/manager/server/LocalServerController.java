package cit.workflow.engine.manager.server;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.test.ControllerTest;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.util.RequestGenerator;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;

public class LocalServerController implements ServerControllerInterface{
	private IWorkbenchWindow window;
	private static final int ActiveTime=1000*60*2;//2 minute
	public LocalServerController(IWorkbenchWindow window){
		this.window=window;
	}

	@Override
	public void addServers(int num) {
		for(int i=0;i<num;i++){
			new ActivateServerThread().start();
		}
	}

	@Override
	public void deleteServer(ServerAgent server) {
		server.getEngineSerivce().shutDown();		
		NavigationView.RefreshNavigationView(window); 
	}
	
	private class ActivateServerThread extends Thread{
		public ActivateServerThread(){
		}
		@Override
		public void run(){
			ServerAgent server=generateServer();
			ServerList.addServer(server);
			NavigationView.RefreshNavigationView(window);
			try {
				Thread.sleep(ActiveTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			server.getEngineSerivce().setState(ServiceAgent.STATE_RUNNING);
			ConsoleView.println(ConsoleView.LOG_VERBOSE,String.format("%2.3f R %s %d", RequestGenerator.getInstance().getVirtualTime(),server.getName(),ServerList.getRunningServerNum()));
			NavigationView.RefreshNavigationView(window); 
			RequestAssigner.getInstance().assignRequestToService(server.getEngineSerivce());
		}
	}
	
	private static int serverCount=0;
	public static ServerAgent generateServer(){
		serverCount++;
		ServerAgent server=new ServerAgent("http://192.168.1.30:8080",ServerAgent.STATE_ACTIVATING,
				ServerAgent.TYPE_MICRO,ServerAgent.LOC_LOCAL,String.format("cit%02d", serverCount));
		ServiceAgent service=new ServiceAgent(server, ServiceAgent.STATE_ACTIVATING);
		server.addService(service);
		return server;
	}

}
