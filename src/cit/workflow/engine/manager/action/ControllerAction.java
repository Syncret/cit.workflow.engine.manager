package cit.workflow.engine.manager.action;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.ICommandIds;
import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.dialog.AssignWorkflowDialog;
import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;


public class ControllerAction extends Action implements IWorkbenchAction{
	private boolean run=false;
	private FuzzyController controller;
    private final IWorkbenchWindow window;
    private NavigationView naviView;
    private final String[] TEXT={"Enable Controller","Disable Controller"};

    public ControllerAction(IWorkbenchWindow window) {
        this.window = window;
        this.setText(TEXT[0]);
        naviView=(NavigationView)window.getActivePage().findView(NavigationView.ID);
        setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.STATUS));
        setImageDescriptor(cit.workflow.engine.manager.Activator.getImageDescriptor("/icons/sample3.gif"));
    }

    public void run() {
    	if(run){
    		controller.setRun(false);
    		this.setText(TEXT[0]);
    	}
    	else{
    		controller=new FuzzyController();
    		controller.initialize();
    		controller.setRun(true);
    		controller.start();
    		this.setText(TEXT[1]);
    	}
    	run=!run;
    }
    
    private class FuzzyController extends Thread{
		private int inteval=60000;
		private boolean run=false;
		public void setRun(boolean run){this.run=run;}
		public boolean isRun(){return run;}
		
		private int size;
		private int dsize;
		private int output;
		
		//fuzzy control constant
		private int NBS=-40;
		private int NSZ=-20;
		private int PZS=20;
		private int PSB=40;
		
		private int DNBS=-20;
		private int DNSZ=-10;
		private int DPZS=10;
		private int DPSB=20;
//		private int[][] controllTable2={
//				{-3,-2,-1,-1,0},
//				{-2,-2,-1,0,1},
//				{-1,0,0,0,2},
//				{0,0,1,3,3},
//				{0,1,2,3,3}
//		};
		
		private int[][] controllTable={
				{-2,-2,-2,-1,0},
				{-2,-2,-1,0,0},
				{-1,0,0,0,2},
				{0,0,1,1,2},
				{0,0,1,2,2}
		};
		
		public FuzzyController(){
		}
		
		public void initialize(){
			size=getCurrentSize();
			dsize=0;
		}
		
		private int getCurrentSize(){
			int s=RequestAssigner.getInstance().getRequests().size();
			if(s==0){
				List<ServiceAgent> services=ServerList.getEngineServices(ServiceAgent.STATE_RUNNING);
				String msg="";
				for (ServiceAgent service : services) {
					s-=service.getVacancy();
					msg+=service.getServer().getName()+":"+(service.getRunningWorkflows())+", ";
				}
				ConsoleView.println(msg);
			}
			return s;
		}
		
		
		private int smithPredict(){
			double costAver=36;
			List<ServiceAgent> services=ServerList.getEngineServices(ServiceAgent.STATE_ACTIVATING);
			if(services.size()==0)return 0;
			int requestPerMin=0;
			for(ServiceAgent service:services){
				requestPerMin+=service.getCapacity()*60/costAver*60/service.getServer().getActiveTime();
			}
			return requestPerMin;
//			return 0;
		}
		
		@Override
		public void run(){
			while (run) {
				long now = System.currentTimeMillis();
				RequestAssigner.getInstance().printStatics();
				int preSize = size;
				size = getCurrentSize();
				dsize = size - preSize;
				int correct=smithPredict();
				int psize = size-correct;
				dsize-=correct;
				String msg="queen:"+RequestAssigner.getInstance().getRequests().size()
						+" size:" + size + " dsize:" +dsize+" psize:"+ psize;
				
				// get the fuzzy set of the error, error correction
				int eu, ecu;
				if (psize < NBS)
					eu = -2;
				else if (psize < NSZ)
					eu = -1;
				else if (psize < PZS)
					eu = 0;
				else if (psize < PSB)
					eu = 1;
				else
					eu = 2;
				if (dsize < DNBS)
					ecu = -2;
				else if (dsize < DNSZ)
					ecu = -1;
				else if (dsize < DPZS)
					ecu = 0;
				else if (dsize < DPSB)
					ecu = 1;
				else
					ecu = 2;
				// query controll table
				msg+=" e:" + eu+" ec:"+ecu;
				output = controllTable[eu+2][ecu+2];
				msg+=" output:" + output;
				ConsoleView.println("Controller:"+msg);
				/*
				if (output > 0) {
					ServiceAgent candidate = null;
					int best = 3;
					for (ServiceAgent service : ServerList.getEngineServices(ServiceAgent.STATE_AVAILABLE)) {
						int type = service.getServer().getType()+1;
						int differ = output - type;
						if (differ >= 0 && differ < best) {
							candidate = service;
							best = differ;
						}
					}
					if (candidate == null)
						ConsoleView.println("Controller:No available server to add");
					else {
						new ActivateServerThread(candidate).start();
						ConsoleView.println("Controller:Activating " + candidate.getServer().getName());
					}
				}
				if (output < 0) {
					List<ServiceAgent> services = ServerList.getEngineServices(ServiceAgent.STATE_RUNNING);
					if (services.size() <= 1) {
						ConsoleView.println("Controller:Minimun number of servers");
					}
					else{
						ServiceAgent candidate = null;
						int best = -3;
						for (ServiceAgent service : services) {
							int type = service.getServer().getType()+1;
							int differ = output + type;
							if (differ <= 0 && differ > best) {
								candidate = service;
								best = differ;
							}
						}
						if (candidate == null)
							ConsoleView.println("Controller:No suitable running server to shutdown");
						else {
							ConsoleView.println("Controller:Shutting down " + candidate.getServer().getName());
							candidate.setState(ServiceAgent.STATE_SHUTTING);
							candidate.getServer().setState(ServerAgent.STATE_SHUTTING);
							size+=candidate.getVacancy();
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									naviView.refresh();
								}
							});
						}
					}
				}*/
				try {
					Thread.sleep(now + inteval - System.currentTimeMillis());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private class ActivateServerThread extends Thread{
			private ServerAgent server;
			private ServiceAgent service;
			public ActivateServerThread(ServiceAgent service){
				this.service=service;
				this.server=service.getServer();
			}
			@Override
			public void run(){
				service.setState(ServiceAgent.STATE_ACTIVATING);
				server.setState(ServerAgent.STATE_ACTIVATING);
				Display.getDefault().syncExec(new Runnable() {
				    public void run() {naviView.refresh();}
				   }); 
				long waitTime=0;
				waitTime=server.getActiveTime()*1000;
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(service.getState()!=ServiceAgent.STATE_ACTIVATING)
					ConsoleView.println("Controller:Activate "+server.getName()+" failed");
				else {
					server.setState(ServerAgent.STATE_RUNNING);
					service.setState(ServiceAgent.STATE_RUNNING);
					ConsoleView.println("Controller:Activate "+server.getName()+" complete");
					Display.getDefault().syncExec(new Runnable() {
					    public void run() {naviView.refresh();}
					   }); 
					RequestAssigner.getInstance().assignRequestToService(service);
				}
			}
		}
	}

	@Override
	public void dispose() {
		controller.setRun(false);
	}
}