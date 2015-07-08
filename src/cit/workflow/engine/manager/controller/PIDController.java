package cit.workflow.engine.manager.controller;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.server.LocalServerController;
import cit.workflow.engine.manager.server.ServerControllerInterface;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.util.RequestGenerator;
import cit.workflow.engine.manager.views.ConsoleView;

public class PIDController extends BaseController{
	public void setRun(boolean run){this.run=run;}
	public boolean isRun(){return run;}
	
	public int serverAbility;
	public int executeTime;//ms
	
	

	public PIDController(IWorkbenchWindow window,int serverControllerType){
		super(window, serverControllerType);
		serverAbility=ServerAgent.SERVICECAPACITY[ServerAgent.TYPE_MICRO];
		executeTime=RequestGenerator.getInstance().getDefaultWorkflowId()*1000;
	}
	
	
	
	@Override
	public void run(){		
		while (run) {
			long now = System.currentTimeMillis();
			int correct=getCurrentSize();
			if(correct==1||correct==2)correct=0;//sometimes it's because the request just generated which hasn't been put into serve
			int requires=(int)Math.ceil((double)correct*executeTime/inteval/serverAbility);			
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