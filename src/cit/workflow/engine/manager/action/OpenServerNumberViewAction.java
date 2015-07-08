package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.views.ServerNumberView;

public class OpenServerNumberViewAction extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openservernumberviewaction";
	private final String viewID=cit.workflow.engine.manager.views.ServerNumberView.ID;
	public OpenServerNumberViewAction(IWorkbenchWindow window){
		this.window=window;
		this.setText("Server Number");
		setToolTipText("Open Server Number View");		
	}
	
	public void run(){
		if(window!=null){
			try{
				window.getActivePage().showView(viewID);
			}catch(PartInitException e){
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:"+e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
	}
}
