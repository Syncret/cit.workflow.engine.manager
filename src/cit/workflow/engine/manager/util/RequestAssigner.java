package cit.workflow.engine.manager.util;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.WorkflowInstanceAgent;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.webservice.WorkflowServerClient;

public class RequestAssigner {
	private static final RequestAssigner rc=new RequestAssigner();
	public static final int RANDOM_ASSIGN=1;
	
	private int policy;
	private ArrayList<ServiceAgent> services=new ArrayList();
	public static RequestAssigner getInstance(){
		return rc;
	}
	public RequestAssigner(){
		this.policy=RANDOM_ASSIGN;
	}
	public int getPolicy(){return policy;}
	public void setPolicy(){this.policy=policy;}
	
	private void checkAvailableService(){
		services.clear();
		for(ServerAgent server:ServerList.getInstance().getChildren()){
			if(server.getState()==ServerAgent.STATE_RUNNING){
				for(ServiceAgent service:server.getServices()){
					if(service.getName()=="Engine Service" && service.getState()==ServiceAgent.STATE_RUNNING){
						services.add(service);
						break;
					}
				}
			}
		}
	}
	
	public void assignRequest(WorkflowInstanceAgent wsAgent){
		//if no server assigned, here assign a server, if assigned, directly run the service
		if(wsAgent.getService()==null){
			switch(policy){
				case RANDOM_ASSIGN:
					wsAgent.setService(randomAssign());
					break;
				default:
					System.out.println("No policy assigned for assigner.\nUse random assigner.");
					wsAgent.setService(randomAssign());
					return;
			}
			ConsoleView.println("Workflow "+wsAgent.getWorkflowID()+" assigned to "+wsAgent.getService().getServer().getURL().toString());
		}
		wsAgent.getService().addWorkflowInstance(wsAgent);
		Thread thread = new WorkflowClientThread(wsAgent);
		thread.start();
	}
	
	
	public ServiceAgent randomAssign(){
		List<ServiceAgent> services=ServerList.getInstance().getEngineServices();
		int r=(int) (Math.random()*services.size());
		ServiceAgent service=services.get(r);
		return services.get(r);
	}
	
	private class WorkflowClientThread extends Thread{
		private ServiceAgent service;
		private WorkflowInstanceAgent wsAgent;
//		private WorkflowInstancesView view;
		private String processID;
		boolean result=false;
		private String processLog;
		long starttime=0;
		long endtime=0;
		long idletime=0;
		public WorkflowClientThread(WorkflowInstanceAgent wsAgent){
			this.wsAgent=wsAgent;
			this.service=wsAgent.getService();
//			this.view=view;
		}
		
		@Override
		public void run(){
			try {
				WorkflowServerClient client=service.getClient();
				if(client==null){
					result=false;
				} else {
					starttime= System.currentTimeMillis();
					wsAgent.setStartTime(starttime);
					wsAgent.setState(WorkflowInstanceAgent.STATE_RUNNING);
//					processID = client.instantiateWorkflow(wsAgent.getWorkflowID());
					Object[] callResult = client.executeWorkflow(wsAgent.getWorkflowID());
					processID=(String)callResult[0];
					processLog = (String) callResult[1];
					if (processLog.endsWith("Failed\n")) result = false;
					else result = true;
					// starttime=(callResult[2]==null?0:(long)callResult[2]);
					endtime = (callResult[3] == null ? 0 : (long) callResult[3]);
					idletime = (callResult[4] == null ? 0 : (long) callResult[4]);
					// wsAgent.setStartTime(starttime);
					wsAgent.setProcessID(processID);
					wsAgent.setEndTime(endtime);
					wsAgent.setBusyTime(idletime);
					ConsoleView.println("Execute " + processID + ":" + (result ? "complete" : "failed"));
				}
			} catch (RemoteException e) {
				result=false;
				e.printStackTrace();
			}
			wsAgent.setState(result?WorkflowInstanceAgent.STATE_FINISHED:WorkflowInstanceAgent.STATE_FAILED);
			if(processID!=null){
				Connection conn=null;
				PreparedStatement pst=null;
				try {
					conn=ConnectionPool.getInstance().getConnection();
					String sql="INSERT INTO processlogs(ProcessID, log, starttime,endtime, idletime) VALUES (?,?,?,?,?)";
					pst=conn.prepareStatement(sql);
					pst.setString(1, processID);
					pst.setString(2, processLog);
					pst.setLong(3, starttime);
					pst.setLong(4, endtime);
					pst.setLong(5, idletime);
					int rs=pst.executeUpdate();
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally{
					if(pst!=null){
						try {pst.close();}
						catch (SQLException e) {e.printStackTrace();}
					}
					ConnectionPool.getInstance().returnConnection(conn);
				}
			}
		}
	}
}
