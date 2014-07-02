package cit.workflow.engine.manager.views;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Table;

import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.WorkflowInstanceAgent;
import cit.workflow.engine.manager.dialog.ProcessLogDialog;
import cit.workflow.engine.manager.util.ConnectionPool;
import cit.workflow.engine.manager.util.ImageFactory;

public class WorkflowInstancesView extends ViewPart {

	public static final String ID = "cit.workflow.engine.manager.WorkflowInstancesView"; //$NON-NLS-1$
	private Table table;
	private String[] columns={"","Name","ID","State","Progress","Start Time","End Time","Busy Time","Server","Process ID"};
	private int[] columnWidth={20,90,50,65,100,130,130,80,100,150};
	private ServiceAgent serviceAgent=null;
	public ServiceAgent getService() {
		return serviceAgent;
	}

	private ArrayList<TableItemData> itemMap=new ArrayList<>();
	private static final int TABLEINDEX_ICON=0;
	private static final int TABLEINDEX_NAME=1;
	private static final int TABLEINDEX_WORKFLOWID=2;
	private static final int TABLEINDEX_STATE=3;
	private static final int TABLEINDEX_PROGRESS=4;
	private static final int TABLEINDEX_STARTTIME=5;
	private static final int TABLEINDEX_ENDTIME=6;
	private static final int TABLEINDEX_BUSYTIME=7;
	private static final int TABLEINDEX_SERVER=8;
	private static final int TABLEINDEX_PROCESSID=9;
	private RefreshProgressThread rpt;
	private Composite parent;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public WorkflowInstancesView() {
		rpt=new RefreshProgressThread(Display.getCurrent());
		rpt.start();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
//		this.parent=new Composite(parent,SWT.None);
		
		table = new Table(parent,  SWT.FULL_SELECTION);
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {}
			@Override
			public void mouseDown(MouseEvent e) {}
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] items=table.getSelection();
				String state=items[0].getText(TABLEINDEX_STATE);
				String processID=items[0].getText(TABLEINDEX_PROCESSID);
				if(processID==null||processID.equals(""))return;
				if(state.equals("Running")||state.equals("Stopped"))return;
				Connection conn=null;
				try {
					conn = ConnectionPool.getInstance().getConnection();
					PreparedStatement pst=conn.prepareStatement("select log from processlogs where ProcessID='"+processID+"'");
					ResultSet rs=pst.executeQuery();
					String log=null;
					while(rs.next())log=rs.getString(1);
					if(log!=null){
						new ProcessLogDialog(getSite().getShell(),"Process Log of "+processID,log).open();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}finally{
					ConnectionPool.getInstance().returnConnection(conn);
				}
			}
		});
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		
		for(int i=0;i<columns.length;i++){
			TableColumn column=new TableColumn(table,SWT.NONE);
			column.setMoveable(true);
			table.getColumn(i).setText(columns[i]);
			table.getColumn(i).setWidth(columnWidth[i]);
		}
		

		createActions();
		initializeToolBar();
		initializeMenu();
		initialTable();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolbarManager.add(new ClearInstanceAction(this));
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	public void initialTable(){
		
	}
	
	
	
	public void clearTable(){
		for(TableItemData instance:itemMap){
			instance.bar.dispose();
			instance.editor.dispose();
			instance.workflow.setShow(false);
		}
		itemMap.clear();
		table.removeAll();
	}
	
	public void setFlowData(ServiceAgent service){
		if(service==null) return;
		if(service!=this.serviceAgent){
			clearTable();
			this.serviceAgent=service;
		}
		for (WorkflowInstanceAgent workflow : service.getWorkflowInstance()) {
			if (workflow.isShow())
				continue;
			addFlowData(workflow);
			workflow.setShow(true);
		}
		
	}
	
	public void addFlowData(WorkflowInstanceAgent workflow){
//		lock.lock();
		try {
			TableItem item = new TableItem(table, SWT.NONE);
			String[] text = new String[columns.length];
			text[TABLEINDEX_ICON] = "";
			item.setImage(TABLEINDEX_ICON, workflow.getImage());
			text[TABLEINDEX_NAME] = workflow.getName();
			text[TABLEINDEX_WORKFLOWID] = Integer.toString(workflow.getWorkflowID());
			text[TABLEINDEX_STATE] = workflow.getStateString();
			double progress = workflow.getProgress();
			text[TABLEINDEX_PROGRESS] = Double.toString(progress);
			long date = workflow.getStartTime();
			if (date>0)
				text[TABLEINDEX_STARTTIME] = sdf.format(new Date(date));
			else
				text[TABLEINDEX_STARTTIME] = "";
			date = workflow.getEndTime();
			if (date>0)
				text[TABLEINDEX_ENDTIME] = sdf.format(new Date(date));
			else
				text[TABLEINDEX_ENDTIME] = "";
			double busyTime=workflow.getBusyTime();
			if(busyTime!=0)
				text[TABLEINDEX_BUSYTIME]=busyTime/1000+"s";
			else
				text[TABLEINDEX_BUSYTIME]="";
			text[TABLEINDEX_SERVER]=workflow.getServerName();
			text[TABLEINDEX_PROCESSID] = workflow.getProcessID();
			item.setText(text);
			// insert progress bar
			ProgressBar bar = new ProgressBar(table, SWT.NONE);
			bar.setMaximum(100);
			bar.setMinimum(0);
			bar.setSelection((int) (progress * 100));
			// if(progress>1||workflow.getState()==WorkflowInstanceAgent.STATE_STOPPED)bar.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			TableEditor editor = new TableEditor(table);
			editor.grabHorizontal = true;
			editor.grabVertical = true;
			editor.setEditor(bar, item, TABLEINDEX_PROGRESS);
			itemMap.add(new TableItemData(item, editor, bar, workflow,workflow.getState()));
		} finally {
//			lock.unlock();
		}
	}
	
	public void refresh(){
		if(serviceAgent==null)return;
		setFlowData(serviceAgent);
	}
	
	private class RefreshProgressThread extends Thread{
		private Display display;
		public RefreshProgressThread(Display display){
			this.display=display;
			this.setDaemon(true);
		}
		
		public void run(){
			while(!display.isDisposed()){
//				lock.lock();
				try {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							for (TableItemData itemData:itemMap) {
								if (!itemData.item.isDisposed()&&
										(itemData.state==WorkflowInstanceAgent.STATE_RUNNING||
										itemData.state==WorkflowInstanceAgent.STATE_WAITING||
										itemData.state==WorkflowInstanceAgent.STATE_STOPPED)) {
									if(itemData.state==WorkflowInstanceAgent.STATE_RUNNING)
										itemData.bar.setSelection((int) (itemData.workflow.getProgress() * 100));
									//check if the state has changed
									if(!itemData.item.getText(TABLEINDEX_STATE).equals(itemData.workflow.getStateString()))	{
										itemData.bar.setSelection((int) (itemData.workflow.getProgress() * 100));
										itemData.item.setImage(TABLEINDEX_ICON, itemData.workflow.getImage());
										itemData.item.setText(TABLEINDEX_STATE,itemData.workflow.getStateString());
										itemData.item.setText(TABLEINDEX_PROCESSID,itemData.workflow.getProcessID());
										itemData.item.setText(TABLEINDEX_SERVER,itemData.workflow.getServerName());
										if(itemData.workflow.getStartTime()>0)
											itemData.item.setText(TABLEINDEX_STARTTIME,sdf.format(itemData.workflow.getStartTime()));
										if(itemData.workflow.getEndTime()>0)
											itemData.item.setText(TABLEINDEX_ENDTIME,sdf.format(itemData.workflow.getEndTime()));
										if(itemData.workflow.getBusyTime()>0)
											itemData.item.setText(TABLEINDEX_BUSYTIME,(double)itemData.workflow.getBusyTime()/1000+"s");
										itemData.state=itemData.workflow.getState();
									}
								}
							}
							setFlowData(serviceAgent);
						}
					});
				} finally {
//					lock.unlock();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class TableItemData{
		public TableItem item;
		public TableEditor editor;
		public ProgressBar bar;
		public WorkflowInstanceAgent workflow;
		public int state;
		public TableItemData(TableItem item, TableEditor editor, ProgressBar bar, WorkflowInstanceAgent workflow,int state) {
			super();
			this.item = item;
			this.editor = editor;
			this.bar = bar;
			this.workflow = workflow;
			this.state=state;
		}
	}
	
	public class ClearInstanceAction extends Action{
		private WorkflowInstancesView view;
		public ClearInstanceAction(WorkflowInstancesView view){
			setText("&Clear");
			setToolTipText("Clear all instances");
			setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.CLEARTEXT));
			this.view=view;
		}
		
		public void run(){
			view.clearTable();
			ArrayList<WorkflowInstanceAgent> toRemove=new ArrayList<>();
			for(WorkflowInstanceAgent workflow:serviceAgent.getWorkflowInstance()){
				if(workflow.getState()==WorkflowInstanceAgent.STATE_FINISHED
						||workflow.getState()==WorkflowInstanceAgent.STATE_FAILED
						||workflow.getState()==WorkflowInstanceAgent.STATE_ABORTTED)
					toRemove.add(workflow);
			}
			for(WorkflowInstanceAgent workflow:toRemove){
				serviceAgent.getWorkflowInstance().remove(workflow);
			}
		}
	}
}



