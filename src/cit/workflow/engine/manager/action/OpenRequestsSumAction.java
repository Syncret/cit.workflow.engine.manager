package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.views.RequestsSumView;

public class OpenRequestsSumAction  extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openrequestssumaction";
	private final String viewID=cit.workflow.engine.manager.views.RequestsSumView.ID;
	public OpenRequestsSumAction(IWorkbenchWindow window){
		this.window=window;
		this.setText("View Requests Statics");
	}
	
	public void run(){
		if(window!=null){
			try{
				window.getActivePage().showView(viewID);
				RequestsSumView view=(RequestsSumView)window.getActivePage().findView(viewID);
				view.setData();
			}catch(PartInitException e){
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:"+e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
	}
}
