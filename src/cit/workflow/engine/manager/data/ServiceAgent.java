package cit.workflow.engine.manager.data;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.graphics.Image;

import cit.workflow.engine.manager.server.AwsInstanceProxy;
import cit.workflow.engine.manager.util.ConnectionPool;
import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.webservice.AwsUtility;
import cit.workflow.webservice.WorkflowServerClient;

public class ServiceAgent implements TreeElement{
	private String name;
	private ServerAgent server;
	private String deployPath;
	private String namePath;
	private int type;
	private int capacity=8;
	private int runningWorkflows=0;
	private boolean pendingShutDown=false;
	//if agent update the process log to database
	private boolean updateLog=false;
	private ReentrantLock insLock=new ReentrantLock();
	public int getRunningWorkflows() {
		return runningWorkflows;
	}


	private List<WorkflowInstanceAgent> workflows;
	
	private int state;
		
	public static final int STATE_STOPPED=0;
	public static final int STATE_RUNNING=1;
	public static final int STATE_INVALID=2;
	public static final int STATE_AVAILABLE=3;
	public static final int STATE_ACTIVATING=4;
	public static final int STATE_SHUTTING=5;
	private static final String[] STATESTRING={"Stopped","Running","Invalid","Available","Activating","Shutting"};
	public static String getStateString(int state){return STATESTRING[state];}
	
	
	public static final int TYPE_ENGINE=11;
	public static final int TYPE_OTHERS=10;

	public ServiceAgent(ServerAgent server,int state) {
		this(server,"Engine Service","workflow","Workflow",state,TYPE_ENGINE);
	}
	

	public ServiceAgent(ServerAgent server,String name, String deployPath,String namePath,int state,int type) {
		this.name = name;
		this.server = server;
		this.deployPath = deployPath;
		this.namePath=namePath;
		this.state = state;
		this.type=type;
		workflows=new ArrayList<>();
	}
	



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the server
	 */
	public ServerAgent getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(ServerAgent server) {
		this.server = server;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}
	
	

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
		if(this.getServer()!=null && state==STATE_RUNNING)
			this.getServer().setState(ServerAgent.STATE_RUNNING);
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public List<TreeElement> getChildren() {
		return null;
	}

	@Override
	public TreeElement getParent() {
		return server;
	}

	@Override
	public Image getImage() {
		return getImage(this.state);
	}
	
	public static Image getImage(int state) {
		if(state==STATE_ACTIVATING||state==STATE_AVAILABLE)return ImageFactory.getImage(ImageFactory.GREENCIRCLE);
		if(state==STATE_RUNNING)return ImageFactory.getImage(ImageFactory.RUNNING);
		if(state==STATE_STOPPED||state==STATE_INVALID||state==STATE_SHUTTING)return ImageFactory.getImage(ImageFactory.STOPPED);
		return ImageFactory.getImage(ImageFactory.SERVERS);
	}
	
	@Override
	public boolean equals(Object service){
		if(service==null||!(service instanceof ServiceAgent))return false;
		return this.getURL().equals(((ServiceAgent)service).getURL());
	}

	public String getDeployPath() {
		return deployPath;
	}

	public void setDeployPath(String serverPath) {
		this.deployPath = serverPath;
	}


	public String getNamePath() {
		return this.namePath;
	}

	public URL getURL() {
		try {
			URL url1=new URL(server.getURL(),deployPath+"/");
			return new URL(url1,namePath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public URL getWsdlURL(){
		URL url1=null;
		try {
			url1 = new URL(getURL().toString()+"?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url1;
	}


	public void setNamePath(String wsdlPath) {
		this.namePath = wsdlPath;
	}


	public WorkflowServerClient getClient() {
		WorkflowServerClient client=new WorkflowServerClient(getWsdlURL());
		boolean result=client.connect();
		if(!result){
			state=STATE_INVALID;
			System.out.println("Cannot create service client from "+getWsdlURL());
			return null;
		}
		return client;
	}

	public void addWorkflowInstance(WorkflowInstanceAgent workflow){
		if(this!=RequestAssigner.getWaitService()&&workflow.getService()==null)
			workflow.setService(this);
		workflows.add(workflow);
	}
	
	public List<WorkflowInstanceAgent> getWorkflowInstance(){
		return workflows;
	}
	
	public void removeWorkflowInstance(WorkflowInstanceAgent workflow){
		workflows.remove(workflow);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}



	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public int getVacancy(){
		return capacity-runningWorkflows;
	}
	
	public boolean testConnection(){
		int result=-1;
		try {
			HttpURLConnection connection=(HttpURLConnection) this.getWsdlURL().openConnection();
			result=connection.getResponseCode();
			connection.disconnect();
		} catch (IOException e) {
			return false;
		}
		if(result==200)return true;
		else return false;
	}
	
	public boolean isPendingShutDown() {
		return pendingShutDown;
	}


	public void setPendingShutDown(boolean pendingShutDown) {
		this.pendingShutDown = pendingShutDown;
	}
	
	public void shutDown(){
		if(runningWorkflows!=0){//only in case here,please do verification out of here
			ConsoleView.println(runningWorkflows+" running workflows pending shutdown");
			this.setPendingShutDown(true);
			this.setState(STATE_SHUTTING);
			server.setState(STATE_SHUTTING);
		}
		else{
			this.setState(STATE_SHUTTING);
			server.setState(STATE_SHUTTING);
			ServerList.removeServer(server);			
		}
	}


	public void assignRequest(WorkflowInstanceAgent wsAgent){
		if(wsAgent.getService()!=null && wsAgent.getService()!=this){
			ConsoleView.println("Error: Request assignned to wrong service");
			return;
		}
		if(runningWorkflows>=capacity){
			ConsoleView.println("Error: Request assignned to full load server");
			return;
		}
		if(wsAgent.getService()==null)this.addWorkflowInstance(wsAgent);
		insLock.lock();
		runningWorkflows++;
		insLock.unlock();
//		ConsoleView.println("Workflow "+wsAgent.getWorkflowID()+" assigned to "+this.getServer().getName());
		Thread thread = new WorkflowClientThread(wsAgent);
		thread.start();
	}
	
	private class WorkflowClientThread extends Thread{
		private ServiceAgent service;
		private WorkflowInstanceAgent wsAgent;
		private String processID;
		boolean result=false;
		private String processLog;
		long starttime=0;
		long endtime=0;
		long idletime=0;
		public WorkflowClientThread(WorkflowInstanceAgent wsAgent){
			this.wsAgent=wsAgent;
			this.service=wsAgent.getService();
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
					Object[] callResult = client.executeWorkflow(wsAgent.getWorkflowID());
					processID=(String)callResult[0];
					processLog = (String) callResult[1];
					if (processLog.endsWith("Failed\n")) result = false;
					else result = true;
//					starttime=(callResult[2]==null?0:(long)callResult[2]);
//					endtime = (callResult[3] == null ? 0 : (long) callResult[3]);
					endtime=System.currentTimeMillis();
					idletime = (callResult[4] == null ? 0 : (int) callResult[4]);
					wsAgent.setStartTime(starttime);
					wsAgent.setProcessID(processID);
					wsAgent.setEndTime(endtime);
					wsAgent.setBusyTime(endtime-starttime-idletime);
//					ConsoleView.println("Execute " + processID + ":" + (result ? "complete" : "failed"));
				}
			} catch (RemoteException e) {
				result=false;
				e.printStackTrace();
			}
			wsAgent.setState(result?WorkflowInstanceAgent.STATE_FINISHED:WorkflowInstanceAgent.STATE_FAILED);
			insLock.lock();
			service.runningWorkflows--;
			if(service.runningWorkflows==0)service.getWorkflowInstance().clear();
			insLock.unlock();
			if(service.runningWorkflows==0 && service.getState()==ServiceAgent.STATE_SHUTTING){
				service.setState(ServiceAgent.STATE_STOPPED);
				service.getServer().setState(ServerAgent.STATE_STOPPED);
				if(service.getServer().getLocation()==ServerAgent.LOC_AWSEC2){
					AwsInstanceProxy.getInstance().deleteInstance(server,null);
				}
				ConsoleView.println("Server "+service.getServer().getName()+" shutdown");
				
			}
//			ConsoleView.println((endtime-starttime-idletime)+"");
			RequestAssigner.getInstance().workflowComplete(wsAgent);
			if(updateLog&&processID!=null&&!processLog.equals("")){
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
