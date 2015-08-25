package cit.workflow.engine.manager.server;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.webservice.AwsUtility;

public class EC2ServerController implements ServerControllerInterface{
	private IWorkbenchWindow window;
	private AwsInstanceProxy aws;
	private static final int ActiveTime=1000*60*2;//2 minute
	public EC2ServerController(IWorkbenchWindow window){
		this.aws=AwsInstanceProxy.getInstance();
		this.window=window;
	}

	@Override
	public void addServers(int num) {
		for(int i=0;i<num;i++){
			aws.newInstance(null);
		}
	}

	@Override
	public void deleteServer(ServerAgent server) {
		aws.deleteInstance(server,window);
		NavigationView.RefreshNavigationView(window); 
	}

}
