package cit.workflow.engine.manager.server;

import cit.workflow.engine.manager.data.ServerAgent;

public interface ServerControllerInterface {
	public void addServers(int num);
	public void deleteServer(ServerAgent server);
}
