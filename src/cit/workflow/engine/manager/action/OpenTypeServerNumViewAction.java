package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.views.ServerStatusView;
import cit.workflow.engine.manager.views.TypeServerNumView;

public class OpenTypeServerNumViewAction  extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.opentypeservernumviewaction";
	private final String viewID=cit.workflow.engine.manager.views.TypeServerNumView.ID;
	private int serverLocation; 
	public OpenTypeServerNumViewAction(IWorkbenchWindow window, int serverLocation){
		this.window=window;
		this.serverLocation=serverLocation;
		this.setText(ServerAgent.LOCATIONSTRING[this.serverLocation]);
	}
	
	public void run(){
		if(window!=null){
			try{
				window.getActivePage().showView(viewID);
				TypeServerNumView view=(TypeServerNumView)window.getActivePage().findView(viewID);
				view.setData(serverLocation);
			}catch(PartInitException e){
				e.printStackTrace();
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:"+e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
	}
}
