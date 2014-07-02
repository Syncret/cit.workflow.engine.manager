package cit.workflow.engine.manager.action;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.data.WorkflowInstanceAgent;
import cit.workflow.engine.manager.dialog.AssignWorkflowDialog;
import cit.workflow.engine.manager.util.ConnectionPool;
import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.WorkflowInstancesView;

public class GenRequestAction extends Action implements IWorkbenchAction{
	private IWorkbenchWindow window;
	private RequestGenerateThread generator=null;
	private boolean run=false;
	private String[] text={"Generate Requests","Stop"};
	
	
	public GenRequestAction(IWorkbenchWindow window){
		super();
		setText(text[0]);
		this.window=window;
		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.GENREQUEST));
	}
	
//    public void run() {
//		AssignWorkflowDialog awDlg=new AssignWorkflowDialog(window.getShell());
//		awDlg.open();
//		int workflowID=awDlg.getSelectWorkflow();
//		int selectNum=awDlg.getSelectNumber();
//		if(workflowID==-1||selectNum<=0)return;
//		Connection conn;
//		String name="";
//		int expectTime=0;
//		int maxDuration=0;
//		int minDuration=0;
//		try {
//			conn = ConnectionPool.getInstance().getConnection();
//			String connstr="select * from workflowinformation where WorkflowID='"+workflowID+"'";
//			PreparedStatement pst=conn.prepareStatement(connstr);
//			ResultSet rs=pst.executeQuery();
//			while(rs.next()){
//				name=rs.getString("WorkflowName");
//				expectTime=rs.getInt("MostPossibleDuration");
//				maxDuration=rs.getInt("MaximalDuration");
//				minDuration=rs.getInt("MinimalDuration");
//			}
//		} catch (SQLException e1) {
//			ConsoleView.println(e1.getMessage());
//			e1.printStackTrace();
//			return;
//		}
//		for (int i = 0; i < selectNum; i++) {
//			WorkflowInstanceAgent wsAgent = new WorkflowInstanceAgent(workflowID);
//			wsAgent.setName(name);
//			wsAgent.setExpectTime(expectTime);
//			wsAgent.setMaxDuration(maxDuration);
//			wsAgent.setMinDuration(minDuration);
//			wsAgent.setState(WorkflowInstanceAgent.STATE_STOPPED);
//			wsAgent.setProcessID("");
//			RequestAssigner.getInstance().acceptRequest(wsAgent);
//		}
//		try {
//			window.getActivePage().showView(WorkflowInstancesView.ID);
//		} catch (PartInitException e) {
//			e.printStackTrace();
//		}
//		WorkflowInstancesView workflowInstancesView=(WorkflowInstancesView)window.getActivePage().findView(WorkflowInstancesView.ID);
//		workflowInstancesView.setFlowData(RequestAssigner.getWaitService());
//    }
    
    
    public void run(){
    	if(run){
    		generator.run=false;
    		setText(text[0]);
    		return;
    	}
		try {
			window.getActivePage().showView(WorkflowInstancesView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		WorkflowInstancesView workflowInstancesView=(WorkflowInstancesView)window.getActivePage().findView(WorkflowInstancesView.ID);
		workflowInstancesView.setFlowData(RequestAssigner.getWaitService());
		generator=new RequestGenerateThread();
		generator.start();
		setText(text[1]);
    }
    
    
	@Override
	public void dispose() {
	}
	
	private class RequestGenerateThread extends Thread{
		public boolean run=true;
		public void run() {
			int[] pattern={10,15,20,25,30,35,40,45,50,50,45,40,35,30,25,20,15,10,15,20,25,30,35,40,45,50,50,45,40,35,30,25,20,15,10};
	    	for(int j=0;j<pattern.length;j++){
	    		int num=pattern[j];
	    		long now=System.currentTimeMillis();	    		
	    		ConsoleView.println("Generator:"+pattern[j]+" requests");
				for (int i = 0; i < num; i++) {
					WorkflowInstanceAgent wsAgent = new WorkflowInstanceAgent(21);
					wsAgent.setName("test");
					wsAgent.setExpectTime(35);
					wsAgent.setMaxDuration(35);
					wsAgent.setMinDuration(35);
					wsAgent.setProcessID("");
					RequestAssigner.getInstance().acceptRequest(wsAgent);
				}
	    		try {
					Thread.sleep(now-System.currentTimeMillis()+30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    		if(!run)break;
	    	}
	    	ConsoleView.println("Generator:Complete");
	    }
	}
}
