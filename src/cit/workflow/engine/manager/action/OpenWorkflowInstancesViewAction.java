package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.image.ImageFactory;

public class OpenWorkflowInstancesViewAction extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openworkflowinstancesviewaction";
	private final String viewID=cit.workflow.engine.manager.views.WorkflowInstancesView.ID;
	public OpenWorkflowInstancesViewAction(IWorkbenchWindow window){
		this.window=window;
		this.setText("&Workflow Instances");
		setToolTipText("Open Workflow Instances View");
		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.WORKFLOWSVIEW));
		
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
