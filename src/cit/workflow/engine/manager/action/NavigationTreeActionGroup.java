package cit.workflow.engine.manager.action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ActionGroup;

import cit.workflow.Constants;
import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.TreeElement;
import cit.workflow.engine.manager.data.WorkflowInstanceAgent;
import cit.workflow.engine.manager.dialog.AssignWorkflowDialog;
import cit.workflow.engine.manager.server.AwsInstanceProxy;
import cit.workflow.engine.manager.util.ConnectionPool;
import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.InstanceTimeView;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.engine.manager.views.ServerStatusView;
import cit.workflow.engine.manager.views.WorkflowInstancesView;
import cit.workflow.webservice.AwsUtility;
import cit.workflow.webservice.RemoteDeployer;
import cit.workflow.webservice.WorkflowServerClient;

public class NavigationTreeActionGroup extends ActionGroup {
	private TreeViewer tv;
	private IWorkbenchWindow window;

	public NavigationTreeActionGroup(TreeViewer treeViewer) {
		this.tv = treeViewer;
		window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	// 生成菜单Menu，并将两个Action传入
	public void fillContextMenu(IMenuManager mgr) {
		// 加入两个Action对象到菜单管理器
		MenuManager menuManager = (MenuManager) mgr; // 类型转换
		// 生成Menu并挂在树Tree上
		Tree tree = tv.getTree();
		Menu menu = menuManager.createContextMenu(tree);
		
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				TreeElement element=getSelTreeEntry();
				if(element instanceof ServerList){
					MenuManager newInstanceMenuManger=new MenuManager("New Cloud Instance",ImageFactory.getImageDescriptor(ImageFactory.CLUSTER_ADD),null);
					newInstanceMenuManger.add(new NewCloudInstanceAction(window,ServerAgent.LOC_AWSEC2));
					newInstanceMenuManger.add(new NewCloudInstanceAction(window,ServerAgent.LOC_ALIYUN));
					manager.add(new AddServerAction(window));
					manager.add(newInstanceMenuManger);
					manager.add(new OpenServerNumberViewAction(window,"View Server Numbers"));
					manager.add(new RefreshTreeAction());
				}
				else if(element instanceof ServerAgent){
					//deploy server menus
					MenuManager deployMenuManager=new MenuManager("Deploy Service", ImageFactory.getImageDescriptor(ImageFactory.ADD), null);
					deployMenuManager.add(new DeployServiceAction(DeployServiceAction.ENGINESERVICE));
					deployMenuManager.add(new DeployServiceAction(DeployServiceAction.TESTSERVICE));
					deployMenuManager.add(new Separator());
					deployMenuManager.add(new DeployServiceAction(DeployServiceAction.OTHERSERVICE));
					manager.add(deployMenuManager);
					manager.add(new SetServerNameAction());
					manager.add(new RemoveServerAction());
					manager.add(new TestConnectionAction());
					manager.add(new CheckServiceAction());
				}
				else if(element instanceof ServiceAgent){
					manager.add(new AssignWorkflowAction());
					manager.add(new ViewPerformanceAction());
					manager.add(new ViewInstanceTimeAction());
					manager.add(new StartServiceAction());
					manager.add(new StopServiceAction());
					manager.add(new UndeployServiceAction());
					manager.add(new SetServerCapacityAction());
					MenuManager setStatusManager=new MenuManager("Set Status");
					setStatusManager.add(new SetServiceStatusAction(ServiceAgent.STATE_AVAILABLE));
					setStatusManager.add(new SetServiceStatusAction(ServiceAgent.STATE_INVALID));
					setStatusManager.add(new SetServiceStatusAction(ServiceAgent.STATE_RUNNING));
					setStatusManager.add(new SetServiceStatusAction(ServiceAgent.STATE_STOPPED));
					manager.add(setStatusManager);
					manager.add(new TestConnectionAction());
				}
			}
		});
		mgr.setRemoveAllWhenShown(true);
		tree.setMenu(menu);
	}

//	// “打开”的Action类
//	private class OpenAction extends Action {
//		public OpenAction() {
//			setText("打开");
//		}
//
//		public void run() {
//			ITreeEntry entry = getSelTreeEntry();
//			if (entry != null)
//				MessageDialog.openInformation(null, null, entry.getName());
//		}
//	}
	public class DeployServiceAction extends Action implements IWorkbenchAction{
		public static final int OTHERSERVICE=0;
		public static final int ENGINESERVICE=1;
		public static final int TESTSERVICE=2;
		
		private final String SERVICENAME[]={"other","Engine Service","Test Service"};
		private final String DEPLOYPATH[]={"other","workflow","test"};
		private final String WSDLPATH[]={"Workflow","Workflow",""};
		private final int TYPE[]={ServiceAgent.TYPE_OTHERS,ServiceAgent.TYPE_ENGINE,ServiceAgent.TYPE_OTHERS};
		private static final String ENGINESERVICEPATH="workflow.war";
		private static final String TESTSERVICEPATH="test.war";

		private int service;
		private String serviceName;
		private String warPath;
		private String deployPath;
		private String wsdlPath;
		private int type;
		private ServiceAgent serviceAgent=null;
		private boolean result;
		
		public DeployServiceAction(int service){
			super();
			setText(SERVICENAME[service]);
			this.service=service;
		}
		
		@Override
		public void run(){
			//check node valid
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServerAgent)) return;
			final ServerAgent serverAgent = (ServerAgent) element;
			
			//get war path
			switch (service) {
			case 0:
				FileDialog dialog=new FileDialog(Display.getCurrent().getActiveShell(),SWT.OPEN);
				dialog.setFilterPath(Constants.MANAGER_PATH+RemoteDeployer.SCRIPTPATH);
				dialog.setFilterExtensions(new String[]{"*.war","*.*"});
				dialog.setFilterNames(new String[]{"war Files(*.war)","All Files(*.*)"});
				warPath=dialog.open();
				break;
			case ENGINESERVICE:
				warPath=ENGINESERVICEPATH;
				break;
			case TESTSERVICE:
				warPath=TESTSERVICEPATH;
				break;
			default:
				break;
			}
			if(warPath==null)return;
			
			serviceName=SERVICENAME[service];
			deployPath=DEPLOYPATH[service];
			wsdlPath=WSDLPATH[service];
			type=TYPE[service];
			
			if(service==0){
				File warFile=new File(warPath);
				serviceName=warFile.getName();
				deployPath=serviceName.replace(" ", "_");
			}
			
			boolean confirm=MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm Deployment", 
					"Are you sure to deploy "+serviceName+" to "+serverAgent.getURL().toString()+"?");
			if(!confirm)return;
			
			//Deploy the service
			try {
				window.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException,	InterruptedException {
						monitor.beginTask("Deploy Service", 4);
						
						//test connection
						monitor.subTask("Connection to "+serverAgent.getName());
						if(!serverAgent.testConnection()){
							result=false;
							return;
						}
						monitor.worked(1);
						
						//call ant
						monitor.subTask("Call Ant");
						RemoteDeployer deployer = new RemoteDeployer();
						result = deployer.deploy(serverAgent.getURL(),deployPath, warPath);
						
						//test connection to wsdl
						if(result){
							monitor.worked(2);
							serviceAgent = new ServiceAgent(serverAgent,serviceName,deployPath,wsdlPath,ServiceAgent.STATE_RUNNING,type);
							if(!serviceAgent.testConnection()){
								result=false;
								return;
							}
							monitor.worked(1);
						}
						monitor.done();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			if (result && serviceAgent!=null) {
				serverAgent.addService(serviceAgent);
				serverAgent.setState(ServerAgent.STATE_RUNNING);
				MessageDialog.openInformation(window.getShell(), "Deploy Complete", "Deploy Complete");
				refreshTree();
			}
			else {
				ConsoleView.println("Something wrong happened, deployment maybe failed");
				MessageDialog.openError(window.getShell(), "Deploy Error", "Success flag undetected, deployment failed");
			}
		}
		
		@Override
		public void dispose() {
			
		}
	}
	
	public boolean setServiceState(ServiceAgent service,boolean state){
		WorkflowServerClient client=service.getClient();
		if(client==null){
			return false;
		}
		client.setRun(state);
		if(state)service.setState(ServiceAgent.STATE_RUNNING);
		else service.setState(ServiceAgent.STATE_STOPPED);
		return true;
	}
	
	public class StopServiceAction extends Action implements IWorkbenchAction{
		private ServiceAgent service=null;
		
		public StopServiceAction(){
			super();
			this.setText("Stop Service");
			this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.STOPPED));
		}
		
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent)) return;
			service=(ServiceAgent)element;
			setServiceState(service, false);
			refreshTree();
		}
		
		@Override
		public void dispose(){
		}
	}
	
	public class StartServiceAction extends Action implements IWorkbenchAction{
		private ServiceAgent service=null;
		
		public StartServiceAction(){
			super();
			this.setText("Start Service");
			this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.RUNNING));
		}
		
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent)) return;
			service=(ServiceAgent)element;
			setServiceState(service, true);
			refreshTree();
		}
		
		@Override
		public void dispose(){
		}
	}
	
	public class UndeployServiceAction extends Action implements IWorkbenchAction{
		private boolean result;
		
		public UndeployServiceAction(){
			super();
			this.setText("Undeploy");
			this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.DELETE));
		}
		
		@Override
		public void run(){
			//check node valid
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent)) return;
			final ServiceAgent serviceAgent = (ServiceAgent) element;
			final ServerAgent serverAgent=serviceAgent.getServer();
			
			boolean confirm=MessageDialog.openConfirm(window.getShell(), "Confirm Undeployment", 
					"Are you sure to undeploy "+serviceAgent.getName()+" from "+serverAgent.getName()+"?");
			if(!confirm)return;
			
			//Undeploy the service
			try {
				window.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException,	InterruptedException {
						monitor.beginTask("Undeploying Service from"+serverAgent.getName(), 4);
						RemoteDeployer deployer = new RemoteDeployer();
						monitor.worked(1);
						if(!setServiceState(serviceAgent, false)){
							result=false;
							return;
						}
						result = deployer.undeploy(serverAgent.getURL(),serviceAgent.getDeployPath());
						if(result)monitor.worked(3);
						monitor.done();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			if (result) {
				serverAgent.removeService(serviceAgent);
				if(serverAgent.getServices().size()==0){
					serverAgent.setState(ServerAgent.STATE_STOPPED);
				}
				MessageDialog.openInformation(window.getShell(), "Undeploy Complete", "Undeploy Complete");
				refreshTree();
			}
			else {
				ConsoleView.println("Something wrong happened, undeployment maybe failed");
				MessageDialog.openError(window.getShell(), "Undeploy Error", "Success flag undetected, undeployment failed");
			}
		}
		
		@Override
		public void dispose() {
		}
		
	}

	public class RemoveServerAction extends Action implements IWorkbenchAction{
		public RemoveServerAction(){
			super();
			this.setText("Remove Server");
			this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.DELETE));
		}
		
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServerAgent)) return;
			ServerAgent server=(ServerAgent)element;
			boolean confirm;
			if(server.getServices().size()!=0){
				confirm=MessageDialog.openConfirm(window.getShell(), "Warning", "There is still services running on the server\n"
						+ "Do you still want to remove it?");
			}
			else{
				confirm=MessageDialog.openConfirm(window.getShell(), "Confirm", "Are you sure to remove "+server.getName()+"?");
			}
			if(confirm){
				server.delete(window);
				tv.refresh();
			}
		}
		
		@Override
		public void dispose() {
		}
	}
	
	public class SetServerCapacityAction extends Action implements IWorkbenchAction{
		private ServiceAgent service=null;
		
		public SetServerCapacityAction(){
			super();
			this.setText("Set Capacity");
		}
		
		@Override
		public void run() {
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent))return;
			service=(ServiceAgent)element;
	        InputDialog inputDialog=new InputDialog(window.getShell(), "Set Capacity", "Set Capacity:", 
	        		"", new IntValidator());
	        int r=inputDialog.open();
	        if(r==Window.OK){
	        	int c=Integer.parseInt(inputDialog.getValue());
	        	service.setCapacity(c);
	        }
	    }
	    
		private class IntValidator implements IInputValidator {
			public String isValid(String newText) {
				try {
					Integer.parseInt(newText);
				} catch (Exception e) {
					return e.getMessage();
				}
				return null;
			}
		}
		
		
		@Override
		public void dispose(){
		}
	}
	
	
	public class SetServerNameAction extends Action implements IWorkbenchAction{
		private ServerAgent server=null;
		
		public SetServerNameAction(){
			super();
			this.setText("Set Name");
		}
		
		@Override
		public void run() {
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServerAgent))return;
			server=(ServerAgent)element;
	        InputDialog inputDialog=new InputDialog(window.getShell(), "Set Name", "Set Name:", 
	        		"", null);
	        int r=inputDialog.open();
	        if(r==Window.OK){
	        	String name=inputDialog.getValue();
	        	if(name!=null && name.length()>0){
					server.setName(name);
					refreshTree();
	        	}
	        }
	    }
	    
		@Override
		public void dispose(){
		}
	}
	
	public class SetServiceStatusAction extends Action implements IWorkbenchAction{
		private ServiceAgent service=null;
		private int state;
		public SetServiceStatusAction(int state){
			super();
			this.state=state;
			this.setText(ServiceAgent.getStateString(state));
		}
		
		@Override
		public void run() {
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent))return;
			service=(ServiceAgent)element;
	        service.setState(state);
	        service.getServer().setState(state);
	        refreshTree();
	    }
	    
		@Override
		public void dispose(){
		}
	}
	
	
	
	public class ViewPerformanceAction extends Action implements IWorkbenchAction{
		private ServiceAgent service=null;
		
		public ViewPerformanceAction(){
			super();
			this.setText("View Server Performance");
		}
		
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent)) return;
			service=(ServiceAgent)element;
			WorkflowServerClient client=service.getClient();
			if(client==null){
				return;
			}
			try {
				window.getActivePage().showView(ServerStatusView.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			ServerStatusView ssView=(ServerStatusView)window.getActivePage().findView(ServerStatusView.ID);
			ssView.setLabel(service.getServer().getName());
			ssView.setData(client.getCpuPerfList(), client.getMemoryPerfList());
		}
		
		@Override
		public void dispose(){
		}
	}
	
	public class ViewInstanceTimeAction extends Action implements IWorkbenchAction{
		private ServiceAgent service=null;
		public ViewInstanceTimeAction(){
			super();
			this.setText("View Instance Execution Time");
		}
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent)) return;
			service=(ServiceAgent)element;			
			try {
				window.getActivePage().showView(InstanceTimeView.ID);
				InstanceTimeView view=(InstanceTimeView)window.getActivePage().findView(InstanceTimeView.ID);
				view.setData(service.getServer().getName());			
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void dispose(){
		}
	}
	
	public class TestConnectionAction extends Action implements IWorkbenchAction{
		private boolean result=false;
		private int state;
		private URL url;
		
		public TestConnectionAction(){
			super();
			this.setText("Test connection");
			
		}
		
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null) return;
			if(element instanceof ServerAgent) url=((ServerAgent)element).getURL();
			if(element instanceof ServiceAgent) url=((ServiceAgent)element).getURL();
			try {
				window.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException,
							InterruptedException {
						monitor.beginTask("Test connetion to "+url, 4);
						try {
							monitor.worked(1);
							HttpURLConnection conn=(HttpURLConnection)url.openConnection();
							monitor.worked(1);
							result=true;
							state=conn.getResponseCode();
							conn.disconnect();
						} catch (IOException e) {
							result=false;
							return;
						}
						monitor.done();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			if(result){
				ConsoleView.println("Return code from "+url+": "+state);
				if(state==-1) ConsoleView.println("Unavailable url");
			}
			else {
				boolean confirm=MessageDialog.openConfirm(window.getShell(), "Connection Failed", "Cannot connect to "+url+"\n\nDo you want to remove it?");
				if(confirm){
					if(element instanceof ServerAgent){
						ServerAgent serverAgent=(ServerAgent)element;
						ServerList.removeServer(serverAgent);
						MessageDialog.openInformation(window.getShell(), "Server Removed", serverAgent.getName()+" has been removed");
					}
					if(element instanceof ServiceAgent) {
						ServiceAgent serviceAgent=(ServiceAgent)element;
						serviceAgent.getServer().removeService(serviceAgent);
						MessageDialog.openInformation(window.getShell(), "Service Removed", serviceAgent.getName()+" has been removed");
					}
					refreshTree();
				}
			}
		}
		
		@Override
		public void dispose(){
		}
	}
	
	public class AssignWorkflowAction extends Action implements IWorkbenchAction{
		private ServiceAgent service=null;
		private int workflowID;
		private int selectNum;
		
		
		public AssignWorkflowAction(){
			super();
			this.setText("Assign Workflow");
			this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.ADD));
		}
		
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServiceAgent)) return;
			service=(ServiceAgent)element;
			AssignWorkflowDialog awDlg=new AssignWorkflowDialog(window.getShell());
			awDlg.open();
//			workflowID=new SelectWorkflowDialog().open();
			workflowID=awDlg.getSelectWorkflow();
			selectNum=awDlg.getSelectNumber();
			if(workflowID==-1||selectNum<=0)return;
			Connection conn;
			String name="";
			int expectTime=0;
			int maxDuration=0;
			int minDuration=0;
			try {
				conn = ConnectionPool.getInstance().getConnection();
				String connstr="select * from workflowinformation where WorkflowID='"+workflowID+"'";
				PreparedStatement pst=conn.prepareStatement(connstr);
				ResultSet rs=pst.executeQuery();
				while(rs.next()){
					name=rs.getString("WorkflowName");
					expectTime=rs.getInt("MostPossibleDuration");
					maxDuration=rs.getInt("MaximalDuration");
					minDuration=rs.getInt("MinimalDuration");
				}
			} catch (SQLException e1) {
				ConsoleView.println(e1.getMessage());
				e1.printStackTrace();
				return;
			}
			for (int i = 0; i < selectNum; i++) {
				WorkflowInstanceAgent wsAgent = new WorkflowInstanceAgent(workflowID);
				wsAgent.setName(name);
				wsAgent.setExpectTime(expectTime);
				wsAgent.setMaxDuration(maxDuration);
				wsAgent.setMinDuration(minDuration);
				wsAgent.setProcessID("");
				wsAgent.setService(service);
				RequestAssigner.getInstance().acceptRequest(wsAgent);
			}
			WorkflowInstancesView workflowInstancesView = (WorkflowInstancesView) window.getActivePage().findView(
					WorkflowInstancesView.ID);
			if(workflowInstancesView.getService()==null){
				workflowInstancesView.setFlowData(service);
				try {
					window.getActivePage().showView(WorkflowInstancesView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void dispose(){
		}
		
	}
	
	public class CheckServiceAction extends Action implements IWorkbenchAction{
		private ServerAgent server=null;
		
		
		public CheckServiceAction(){
			super();
			this.setText("Check Service");
		}
		
		@Override
		public void run(){
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServerAgent)) return;
			server=(ServerAgent)element;
			ServiceAgent service=new ServiceAgent(server, ServiceAgent.STATE_RUNNING);
			if(service.testConnection()){
				ConsoleView.println("Success connect to"+service.getWsdlURL().toString());
				server.addService(service);
				refreshTree();
			}
		}
		
		@Override
		public void dispose(){
		}
	}
	
	public class RefreshTreeAction extends Action implements IWorkbenchAction{
		public RefreshTreeAction(){
			super();
			this.setText("Refresh");
		}
		@Override
		public void run(){
			NavigationView.RefreshNavigationView(window);
		}

		@Override
		public void dispose() {
			
		}
	}

	// 为共用而自定义的方法：取得当前选择的结点
	private TreeElement getSelTreeEntry() {
		IStructuredSelection selection = (IStructuredSelection) tv
				.getSelection();
		return (TreeElement) (selection.getFirstElement());
	}
	
	private void refreshTree(){
		tv.refresh();
	}
}