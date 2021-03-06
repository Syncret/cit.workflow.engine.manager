package cit.workflow.engine.manager;

import java.awt.Dialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

import cit.workflow.engine.manager.dialog.AssignWorkflowDialog;
import cit.workflow.engine.manager.util.ImageFactory;


public class TestAction extends Action {

    private final IWorkbenchWindow window;

    TestAction(String text, IWorkbenchWindow window) {
        super(text);
        this.window = window;
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_OPEN_MESSAGE);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_OPEN_MESSAGE);
        //setImageDescriptor(cit.workflow.engine.manager.Activator.getImageDescriptor("/icons/sample3.gif"));
        this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.RUNNING));
    }

    public void run() {
    	//pop a message
//        MessageDialog.openInformation(window.getShell(), "Open", "Open Message Dialog!"); 
    	AssignWorkflowDialog dl=new AssignWorkflowDialog(window.getShell());
    	dl.open();
    	System.out.println(dl.getSelectWorkflow());
    	
    }
}