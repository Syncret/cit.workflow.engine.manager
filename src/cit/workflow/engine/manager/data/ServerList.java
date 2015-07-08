package cit.workflow.engine.manager.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import cit.workflow.engine.manager.Activator;
import cit.workflow.engine.manager.util.ImageFactory;

public class ServerList implements TreeElement{
	public static final ReadWriteLock lock = new ReentrantReadWriteLock(false);
	private static List<ServerAgent> servers=new ArrayList<ServerAgent>();
	private static ServerList allServers=new ServerList();
	public static List<ServerPair> ServerNumberRecord=new ArrayList<ServerPair>();
	public static class ServerPair{
		public int number;
		public long time;
		public ServerPair(long time,int number){
			this.number=number;
			this.time=time;
		}
	};
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
//		if(servers.contains(server)){
//			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Add Server", server.getURL()+" Already exsit");
//			return;
//		}
		lock.writeLock().lock();
		servers.add(server);
		lock.writeLock().unlock();
		ServerNumberRecord.add(new ServerPair(System.currentTimeMillis(), servers.size()));
	}
	
	public static void removeServer(ServerAgent server){
		lock.writeLock().lock();
		if(!servers.contains(server)){
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Remove Server", server.getURL()+" is not in the list");
			return;
		}
//		if(server.getState()!=ServerAgent.STATE_STOPPED){
//			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Remove Server", server.getURL()+" is still running");
//			return;
//		}
		servers.remove(server);
		lock.writeLock().unlock();
		ServerNumberRecord.add(new ServerPair(System.currentTimeMillis(), servers.size()));
	}
	
	
	public static List<ServerAgent> getServers(){
		return servers;
	}
	
	public static int getRunningServerNum(){
		lock.readLock().lock();
		int num= getEngineServices(ServerAgent.STATE_RUNNING).size();
		lock.readLock().unlock();
		return num;
	}
	
	/**
	 * get those running engine services
	 * @return the list contains the running engine services
	 */
	public static List<ServiceAgent> getEngineServices(int serviceState){
		ArrayList<ServiceAgent> services=new ArrayList<>();
		for(ServerAgent server:servers){
			ServiceAgent service=server.getEngineSerivce();
			if(service!=null && service.getState()==serviceState){
				services.add(service);
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
