package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.image.ImageFactory;

public class OpenServerInfoViewAction extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openserverinfoviewaction";
	private final String viewID=cit.workflow.engine.manager.views.ServerInfoView.ID;
	public OpenServerInfoViewAction(IWorkbenchWindow window){
		this.window=window;
		this.setText("&Server Info");
		setToolTipText("Open Server Information View");
		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.SERVER));
		
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
