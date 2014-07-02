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

public class AddServerAction extends Action implements IWorkbenchAction{
	private IWorkbenchWindow window;
	
	public AddServerAction(IWorkbenchWindow window){
		super("Add Server");
		setText("Add Server");
		this.window=window;
//		setId(ICommandIds.CMD_ADD_SERVER);
//		setActionDefinitionId(ICommandIds.CMD_ADD_SERVER);
		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.ADDSERVER));
	}
	
    public void run() {
        InputDialog inputDialog=new InputDialog(window.getShell(), "Add Server", "Please enter the URL of the new server", 
        		"http://", new URLValidator());
        int r=inputDialog.open();
        if(r==Window.OK){
        	URL url=null;
        	try {
				url=new URL(inputDialog.getValue());
			} catch (MalformedURLException e) {
				MessageDialog.openError(window.getShell(), "Error", "URL invalid");
				return;
			}
        	ServerList.addServer(new ServerAgent(url,ServerAgent.STATE_STOPPED,ServerAgent.TYPE_MIDDLE));
        	NavigationView view=(NavigationView)window.getActivePage().findView(NavigationView.ID);
        	view.refresh();
        	view.getTree().expandToLevel(2);
        }
    }
    
	private class URLValidator implements IInputValidator {
		public String isValid(String newText) {
			URL url;
			try {
				url = new URL(newText);

				if (!url.getProtocol().equals("http")) {
					return "Unsupported protocal:" + url.getProtocol();
				}
				if (url.getHost() == null || url.getHost().equals("")) {
					return "Please input the host";
				}
			} catch (MalformedURLException e) {
				return e.getMessage();
			}
			return null;
		}
	}

	@Override
	public void dispose() {
	}
}
