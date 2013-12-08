package cit.workflow.engine.manager.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import cit.workflow.engine.manager.Activator;
import cit.workflow.engine.manager.image.ImageFactory;

public class ServerList implements TreeElement{
//	private static List<ServerAgent> servers=new ArrayList<ServerAgent>();
	private static Hashtable<URL, ServerAgent> servers=new Hashtable<URL,ServerAgent>();
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
		List<ServerAgent> list=new ArrayList<>();
		Enumeration<ServerAgent> e=servers.elements();
		while (e.hasMoreElements()) {
			ServerAgent serverAgent = (ServerAgent) e.nextElement();
			list.add(serverAgent);
		}
		return list;
	}
	
	public static void addServer(ServerAgent server){
		if(servers.containsKey(server.getUrl())){
			Shell shell=Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openWarning(shell, "Add Server", "This Server Already exsit");
			return;
		}
		servers.put(server.getUrl(),server);
	}
	
	public static List<ServerAgent> getServers(){
		List<ServerAgent> list=new ArrayList<>();
		Enumeration<ServerAgent> e=servers.elements();
		while (e.hasMoreElements()) {
			ServerAgent serverAgent = (ServerAgent) e.nextElement();
			list.add(serverAgent);
		}
		return list;
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
