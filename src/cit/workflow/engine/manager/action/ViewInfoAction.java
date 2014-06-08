package cit.workflow.engine.manager.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.views.ServerInfoView;
import cit.workflow.engine.manager.views.WorkflowInstancesView;

public class ViewInfoAction extends Action implements IDoubleClickListener,IWorkbenchAction{
	private IWorkbenchWindow window;
	public final static String ID="cit.workflow.engine.manager.action.ViewServerAction";
	
	public ViewInfoAction(IWorkbenchWindow window){
		super("View Information");
		setText("Vew Information");
		this.window=window;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		ISelection selection=event.getSelection();
		Object element;
		if(selection instanceof TreeSelection){
			element=((TreeSelection)selection).getFirstElement();
			if(element instanceof ServerAgent){
				List<Object> servers=new ArrayList<Object>();
				servers.add(element);
				ServerInfoView infoView=(ServerInfoView)window.getActivePage().findView(ServerInfoView.ID);
				infoView.setData(servers);
				try {
					window.getActivePage().showView(ServerInfoView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
			if(element instanceof ServiceAgent){
				try {
					window.getActivePage().showView(WorkflowInstancesView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
				WorkflowInstancesView workflowInstancesView=(WorkflowInstancesView)window.getActivePage().findView(WorkflowInstancesView.ID);
				workflowInstancesView.setFlowData((ServiceAgent)element);
			}
		}
	}


	@Override
	public void dispose() {
	}
}
