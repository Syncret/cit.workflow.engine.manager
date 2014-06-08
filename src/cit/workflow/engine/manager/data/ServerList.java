package cit.workflow.engine.manager.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import cit.workflow.engine.manager.Activator;
import cit.workflow.engine.manager.util.ImageFactory;

public class ServerList implements TreeElement{
//	private static List<ServerAgent> servers=new ArrayList<ServerAgent>();
	private static List<ServerAgent> servers=new ArrayList<ServerAgent>();
	public static ServerList allServers=new ServerList();
	
	public static ServerList getInstance(){
		return allServers;
	}

	public ServerList(){}
	@Override
	public String getName() {
		return "Servers";
	}

	@Override
	public boolean hasChildren() {
		if(servers!=null&&servers.size()>0)return true;
		return false;
	}

	@Override
	public List<ServerAgent> getChildren() {
		return getServers();
	}
	
	public static void addServer(ServerAgent server){
		if(servers.contains(server)){
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Add Server", server.getURL()+" Already exsit");
			return;
		}
		servers.add(server);
	}
	
	public static void removeServer(ServerAgent server){
		if(!servers.contains(server)){
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Remove Server", server.getURL()+" is not in the list");
			return;
		}
//		if(server.getState()!=ServerAgent.STATE_STOPPED){
//			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Remove Server", server.getURL()+" is still running");
//			return;
//		}
		servers.remove(server);
	}
	
	
	public static List<ServerAgent> getServers(){
		return servers;
	}
	
	/**
	 * get those running engine services
	 * @return the list contains the running engine services
	 */
	public static List<ServiceAgent> getEngineServices(){
		ArrayList<ServiceAgent> services=new ArrayList<>();
		for(ServerAgent server:servers){
			if(server.getState()==ServerAgent.STATE_RUNNING ){
				ServiceAgent service=server.getEngineSerivce();
				if(service!=null && service.getState()==ServiceAgent.STATE_RUNNING){
					services.add(service);
				}
			}
		}
		return services;
	}
	

	@Override
	public TreeElement getParent() {
		return null;
	}
	@Override
	public Image getImage() {
		return ImageFactory.getImage(ImageFactory.SERVERS);
	}


}
