package cit.workflow.engine.manager.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.jfree.data.time.Second;

import cit.workflow.engine.manager.data.WorkflowInstanceAgent;

public class RequestAccepter {
	private long inteval=5*1000;
	private ListenThread listener;
	private static final RequestAccepter instance=new RequestAccepter();
	public static RequestAccepter getInstance(){return instance;}
	private RequestAccepter(){
		listener=new ListenThread();
		listener.setDaemon(true);		
	}
	public void start(){
		clearRequestsTable();
		listener.start();
	}
	public void setInteval(long inteval) {
		this.inteval = inteval;
	}
	public void close(){
		listener.setRun(false);
	}
	
	public void clearRequestsTable(){
		Connection conn=null;
		try{
			conn=ConnectionPool.getInstance().getConnection();
			String statement="TRUNCATE TABLE managerrequests";
			PreparedStatement pst=conn.prepareStatement(statement);
			pst.executeQuery();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	private class ListenThread extends Thread{
		private boolean run=true;		
		private int cur;
		public ListenThread(){			
		}
		
		@Override
		public void run(){
			cur=0;
			while(run){
				long tosleep=System.currentTimeMillis()+inteval;
				Connection conn=null;
				try {
					conn = ConnectionPool.getInstance().getConnection();
					String statement="SELECT id, date, workflowid from managerrequests where id>?";
					PreparedStatement pst=conn.prepareStatement(statement);
					pst.setLong(1, cur);
					ResultSet rs=pst.executeQuery();
					long date;
					int workflowId;
					while(rs.next()){
						cur=rs.getInt(1);
						date=rs.getLong(2);
						workflowId=rs.getInt(3);
						WorkflowInstanceAgent wsAgent = new WorkflowInstanceAgent(workflowId);
						wsAgent.setName("TestWorfklow");
						wsAgent.setExpectTime(workflowId);
						wsAgent.setMaxDuration(workflowId);
						wsAgent.setMinDuration(workflowId);
						wsAgent.setProcessID("");
						RequestAssigner.getInstance().acceptRequest(wsAgent);
					}
					pst.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				} finally{
					ConnectionPool.getInstance().returnConnection(conn);
				}
				
				try {
					Thread.sleep(System.currentTimeMillis()>=tosleep?0:tosleep-System.currentTimeMillis());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}		
		}
		public void setRun(boolean run) {
			this.run = run;
		}
	}

}
