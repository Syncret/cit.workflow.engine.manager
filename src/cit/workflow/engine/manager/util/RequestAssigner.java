package cit.workflow.engine.manager.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.WorkflowInstanceAgent;
import cit.workflow.engine.manager.views.ConsoleView;

public class RequestAssigner {
	private static final RequestAssigner rc=new RequestAssigner();
	//the waitService is a abstract service to put the services which haven'n assigned to any service
	private static final ServiceAgent waitService=
			new ServiceAgent(null, "wait service", "", "", ServiceAgent.STATE_RUNNING, ServiceAgent.TYPE_ENGINE);
	public static final int RANDOM_ASSIGN=1;
	

	private ReentrantLock reqLock=new ReentrantLock();
	private int policy=1;
	private LinkedList<WorkflowInstanceAgent> requests=new LinkedList<WorkflowInstanceAgent>();
	public LinkedList<WorkflowInstanceAgent> getRequests(){return requests;}
	public static RequestAssigner getInstance(){
		return rc;
	}
	
	public RequestAssigner(){
	}
	
	public static ServiceAgent getWaitService(){
		return waitService;
	}
	
	
	public int getPolicy(){return policy;}
	public void setPolicy(int policy){this.policy=policy;}
	

	
	public void acceptRequest(WorkflowInstanceAgent wsAgent){
		requests.add(wsAgent);
		if(wsAgent.getService()!=null){
			wsAgent.getService().addWorkflowInstance(wsAgent);
		}
		waitService.addWorkflowInstance(wsAgent);
		assignRequests();
	}
	
	private void assignRequests(){
		if(requests.size()==0) return;
		List<ServiceAgent> services=ServerList.getEngineServices(ServiceAgent.STATE_RUNNING);
		for (ServiceAgent service : services) {
			assignRequestToService(service);
		}
	}

	
//	public void assignRequestToService(ServiceAgent service){
//		reqLock.lock();
//		ArrayList<WorkflowInstanceAgent> toRemove=new ArrayList<>();
//		for(WorkflowInstanceAgent request:requests){
//			if(service.getVacancy()<=0)break;
//			if(request.getService()!=waitService && request.getService()!=service)continue;
//			service.assignRequest(request);
//			toRemove.add(request);
//		}
//		for(WorkflowInstanceAgent request:toRemove){
//			requests.remove(request);
//		}
//		reqLock.unlock();
//	}
	
	
	public void assignRequestToService(ServiceAgent service){
		reqLock.lock();
		while(service.getVacancy()>0&&requests.size()>0){
			WorkflowInstanceAgent request=requests.pop();
			service.assignRequest(request);
		}
		reqLock.unlock();
	}
	
	
	private int sumOfWorkflow=0;
	private int allSpentTime=0;
	private int periodOfWorkflow=0;
	private int periodSpentTime=0;
	private ReentrantLock reqLock2=new ReentrantLock();
	
	public void workflowComplete(WorkflowInstanceAgent workflow){
		reqLock2.lock();
		sumOfWorkflow++;
		allSpentTime+=workflow.getSpentTime();
		periodOfWorkflow++;
		periodSpentTime+=workflow.getSpentTime();
		reqLock2.unlock();
		assignRequestToService(workflow.getService());
	}
	
	public void printStatics(){
		reqLock2.lock();
		String msg=String.format("Statics: worklfows:%d, aver:%d; period:%d, aver:%d", 
				sumOfWorkflow,sumOfWorkflow==0?0:allSpentTime/sumOfWorkflow,
				periodOfWorkflow,periodOfWorkflow==0?0:periodSpentTime/periodOfWorkflow);
		periodOfWorkflow=0;
		periodSpentTime=0;
		reqLock2.unlock();
		ConsoleView.println(msg);
	}
	

	
//	public void assignRequest2(WorkflowInstanceAgent wsAgent){
//		//if no server assigned, here assign a server, if assigned, directly run the service
//		if(wsAgent.getService()==null){
//			switch(policy){
//				case RANDOM_ASSIGN:
//					wsAgent.setService(randomAssign());
//					break;
//				default:
//					System.out.println("No policy assigned for assigner.\nUse random assigner.");
//					wsAgent.setService(randomAssign());
//					return;
//			}
//			ConsoleView.println("Workflow "+wsAgent.getWorkflowID()+" assigned to "+wsAgent.getService().getServer().getURL().toString());
//		}
//		wsAgent.getService().assignRequest(wsAgent);
//	}
	
//	private void checkAvailableService(){
//		services.clear();
//		for(ServerAgent server:ServerList.getInstance().getChildren()){
//			if(server.getState()==ServerAgent.STATE_RUNNING){
//				for(ServiceAgent service:server.getServices()){
//					if(service.getName()=="Engine Service" && service.getState()==ServiceAgent.STATE_RUNNING){
//						services.add(service);
//						break;
//					}
//				}
//			}
//		}
//	}
//	
//	
//	public ServiceAgent randomAssign(){
//		List<ServiceAgent> services=ServerList.getEngineServices();
//		int r=(int) (Math.random()*services.size());
//		return services.get(r);
//	}
	
	
}
