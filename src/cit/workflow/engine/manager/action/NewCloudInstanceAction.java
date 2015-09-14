package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.ICommandIds;
import cit.workflow.engine.manager.StaticSettings;

import java.io.IOException;
import java.net.*;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.server.AliyunInstanceProxy;
import cit.workflow.engine.manager.server.AwsInstanceProxy;
import cit.workflow.engine.manager.server.DummyInstanceProxy;
import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.webservice.AwsUtility;

public class NewCloudInstanceAction extends Action implements IWorkbenchAction{
	private IWorkbenchWindow window;
	public static final int AWSEC2=0;
	public static final int ALIYUN=1;
	private int type=0;
	public static final String[] TYPENAME={"AWS EC2","Aliyun ECS"};
	
	public NewCloudInstanceAction(IWorkbenchWindow window,int cloudType){		
		this.window=window;
		this.type=cloudType;
		String title="%s instance";
		title=String.format(title,TYPENAME[type]);
		this.setText(title);
//		setId(ICommandIds.CMD_ADD_SERVER);
//		setActionDefinitionId(ICommandIds.CMD_ADD_SERVER);
//		this.setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.CLUSTER_ADD));
	}
	
    public void run() {
    	if(StaticSettings.DUMMYSERVERMODE){
    		new DummyInstanceProxy(type).newInstance(window);
    	}
    	else {
	    	switch(type){
		    	case AWSEC2:	    		
		    		AwsInstanceProxy.getInstance().newInstance(window);
		    		break;
		    	case ALIYUN:
		    		AliyunInstanceProxy.getInstance().newInstance(window);
		    		break;
		    	default:
		    		ConsoleView.println("Unsupported Cloud Type "+type);
	    	}
    	}
    }

	@Override
	public void dispose() {
	}
}
