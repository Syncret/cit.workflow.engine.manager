package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.ICommandIds;

import java.io.IOException;
import java.net.*;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.webservice.AwsUtility;

public class RunAwsEC2Instance extends Action implements IWorkbenchAction{
	private IWorkbenchWindow window;
	
	public RunAwsEC2Instance(IWorkbenchWindow window){
		super("Run a AWS EC2 instance");
		setText("Run a AWS EC2 instance");
		this.window=window;
//		setId(ICommandIds.CMD_ADD_SERVER);
//		setActionDefinitionId(ICommandIds.CMD_ADD_SERVER);
		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.CLUSTER_ADD));
	}
	
    public void run() {
        AwsUtility.GetInstance().newEC2InstanceFromWorkbench(window);
    }

	@Override
	public void dispose() {
	}
}
