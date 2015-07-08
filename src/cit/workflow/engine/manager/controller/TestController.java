package cit.workflow.engine.manager.controller;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.server.LocalServerController;
import cit.workflow.engine.manager.util.RequestGenerator;

public class TestController extends BaseController{
	public void setRun(boolean run){this.run=run;}
	public boolean isRun(){return run;}
	
	public static final int[] serverChange={1,-1,1,-1};
	
	public int serverAbility;
	public int executeTime;//ms
	
	

	public TestController(IWorkbenchWindow window,int serverControllerType){
		super(window, serverControllerType);
		serverAbility=ServerAgent.SERVICECAPACITY[ServerAgent.TYPE_MICRO];
		executeTime=RequestGenerator.getInstance().getDefaultWorkflowId()*1000;
		this.setInteval(60*1000);
	}
	
	
	
	@Override
	public void run(){		
		int index=0;
		while (run) {
			long now = System.currentTimeMillis();
			int requires=serverChange[index++%4];
			int serverNum=ServerList.getEngineServices(ServiceAgent.STATE_RUNNING).size();
			
			modifyServers(requires, serverNum);
			
			try {
				long toSleep=now + inteval - System.currentTimeMillis();
				if(toSleep<0)toSleep=0;
				Thread.sleep(toSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}