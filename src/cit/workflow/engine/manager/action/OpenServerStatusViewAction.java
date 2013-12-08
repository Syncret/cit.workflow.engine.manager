package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.image.ImageFactory;

public class OpenServerStatusViewAction extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openserverstatusviewaction";
	private final String viewID=cit.workflow.engine.manager.views.ServerStatusView.ID;
	public OpenServerStatusViewAction(IWorkbenchWindow window){
		this.window=window;
		this.setText("&Server Status");
		setToolTipText("Open Server Status View");
		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.SERVERSTATUS));
		
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
