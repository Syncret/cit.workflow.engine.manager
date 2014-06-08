package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.views.ConsoleView;

public class OpenConsoleViewAction extends Action implements IWorkbenchAction{
	private final IWorkbenchWindow window;
	public static final String ID="cit.workflow.engine.manager.action.openconsoleviewaction";
	private final String viewID=cit.workflow.engine.manager.views.ConsoleView.ID;
	public OpenConsoleViewAction(IWorkbenchWindow window){
		this.window=window;
		this.setText("&Console");
		setToolTipText("Open Console View");
		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.CONSOLEVIEW));
		
	}
	
	public void run(){
		if(window!=null){
			try{
				new ConsoleView().openConsole();
//				ConsoleView.activate();window.getActivePage().
			}catch(Exception e){
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:"+e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
	}
}
