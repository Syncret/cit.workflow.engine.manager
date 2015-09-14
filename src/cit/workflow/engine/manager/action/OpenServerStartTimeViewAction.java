package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.views.ServerStartTimeView;

public class OpenServerStartTimeViewAction extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openserverstarttimeviewaction";
	private final String viewID=cit.workflow.engine.manager.views.ServerStartTimeView.ID;
	private int serverLocation; 
	public OpenServerStartTimeViewAction(IWorkbenchWindow window, int serverLocation){
		this.window=window;
		this.serverLocation=serverLocation;
		if(serverLocation==-1)this.setText("All");
		else this.setText(ServerAgent.LOCATIONSTRING[this.serverLocation]);
	}
	
	public void run(){
		if(window!=null){
			try{
				window.getActivePage().showView(viewID);
				ServerStartTimeView view=(ServerStartTimeView)window.getActivePage().findView(viewID);
				view.setData(serverLocation);
			}catch(PartInitException e){
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:"+e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
	}
}
