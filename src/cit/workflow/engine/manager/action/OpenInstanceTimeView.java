package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.views.InstanceTimeView;
import cit.workflow.engine.manager.views.TypeServerNumView;

public class OpenInstanceTimeView extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openinstancetimeviewaction";
	private final String viewID=cit.workflow.engine.manager.views.InstanceTimeView.ID;

	private int mode=0;//catagory mode
	private int serverLocation=0;
	private String serverName="";
	
	public OpenInstanceTimeView(IWorkbenchWindow window){
		this.window=window;
		this.mode=InstanceTimeView.MODE_ALL;
		this.setText("All");
	}
	
	public OpenInstanceTimeView(IWorkbenchWindow window, int serverLocation){
		this.window=window;
		this.mode=InstanceTimeView.MODE_LOCATION;
		this.serverLocation=serverLocation;
		this.setText(ServerAgent.LOCATIONSTRING[this.serverLocation]);
	}
	
	public OpenInstanceTimeView(IWorkbenchWindow window, String serverName){
		this.window=window;
		this.mode=InstanceTimeView.MODE_SERVER;
		this.serverName=serverName;
		this.setText("View Instance Execute Time");
	} 
	
	public void run(){
		if(window!=null){
			try{
				window.getActivePage().showView(viewID);
				InstanceTimeView view=(InstanceTimeView)window.getActivePage().findView(viewID);
				view.setData(mode, serverLocation, serverName);				
			}catch(PartInitException e){
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:"+e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
	}
}
