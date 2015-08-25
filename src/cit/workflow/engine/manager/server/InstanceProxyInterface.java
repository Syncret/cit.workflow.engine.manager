package cit.workflow.engine.manager.server;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;

public interface InstanceProxyInterface {
	public boolean newInstance(IWorkbenchWindow window);
	public boolean deleteInstance(ServerAgent server, IWorkbenchWindow window);

}
