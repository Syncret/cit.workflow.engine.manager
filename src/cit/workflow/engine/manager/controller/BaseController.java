package cit.workflow.engine.manager.controller;

import java.util.List;





import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.server.EC2ServerController;
import cit.workflow.engine.manager.server.LocalServerController;
import cit.workflow.engine.manager.server.ServerControllerInterface;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.util.RequestGenerator;
import cit.workflow.engine.manager.views.ConsoleView;

public abstract class BaseController extends Thread{
	protected ServerControllerInterface serverController;
	protected boolean run=false;
	protected int inteval=1000*60*15;
	public static final int SERVERCONTROL_LOCAL=0;
	public static final int SERVERCONTROL_EC2=1;
	
	BaseController(IWorkbenchWindow window,int serverControllerType){
		switch(serverControllerType){
		case SERVERCONTROL_LOCAL:
			serverController=new LocalServerController(window);
			break;
		case SERVERCONTROL_EC2:
			serverController=new EC2ServerController(window);
			break;
		default:
			serverController=new LocalServerController(window);
			break;		
		}
	}
	
	public int getInteval() {
		return inteval;
	}

	public void setInteval(int inteval) {
		this.inteval = inteval;
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public void initialize(){
		this.setDaemon(true);
		this.setRun(true);
	}
	
	public void printStatics(){
		
	}
	
	protected int getCurrentSize(){
		int s=RequestAssigner.getInstance().getRequests().size();
		String msg="";
		if(s==0){
			List<ServiceAgent> services=ServerList.getEngineServices(ServiceAgent.STATE_RUNNING);
			for (ServiceAgent service : services) {
				s-=service.getVacancy();
				msg+=service.getServer().getName()+":"+(service.getRunningWorkflows())+", ";
			}
		}
		msg+="syssize:"+s;
		ConsoleView.println(msg);
		return s;
	}
	
	public void modifyServers(int requires,int serverNum){
		if(requires>0){
			ConsoleView.println(String.format("%02.3f A +%d %d", RequestGenerator.getInstance().getVirtualTime(),requires,serverNum));
			serverController.addServers(requires);
		}
		else if(requires<0){
			requires=-requires;
			requires=Math.min(requires, serverNum-1);
			ConsoleView.println(String.format("%02.3f A -%d %d", RequestGenerator.getInstance().getVirtualTime(),requires,serverNum));
			while(requires>0){
				int minRunning=Integer.MAX_VALUE;
				ServiceAgent candidate=null;
				for(ServiceAgent service:ServerList.getEngineServices(ServiceAgent.STATE_RUNNING)){
					if(service.getRunningWorkflows()==0){
						ConsoleView.println(String.format("%02.3f D %s %d", RequestGenerator.getInstance().getVirtualTime(),service.getServer().getName(),--serverNum));
						serverController.deleteServer(service.getServer());
						requires--;
						if(requires<=0)break;
					}
					else if(service.getRunningWorkflows()<minRunning&&service.getRunningWorkflows()>0){
						minRunning=service.getRunningWorkflows();
						candidate=service;
					}
				}
				if(requires>0){
					ConsoleView.println(String.format("%02.3f D %s %d (trying delete)", RequestGenerator.getInstance().getVirtualTime(),candidate.getServer().getName(),--serverNum));
					serverController.deleteServer(candidate.getServer());
					requires--;
				}
			}
		}
		else{
			ConsoleView.println(String.format("%02.3f A 0%d %d", RequestGenerator.getInstance().getVirtualTime(),requires,serverNum));
		}
	}
}
