package cit.workflow.engine.manager.data;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import cit.workflow.engine.manager.image.ImageFactory;

public class ServiceAgent implements TreeElement{
	private String name;
	private ServerAgent server;
	private int state;
		
	public static final int STATE_STOPPED=0;
	public static final int STATE_RUNNING=1;

	public ServiceAgent(String name){
		this.setName(name);
		setState(STATE_STOPPED);
	}
	
	public ServiceAgent(ServerAgent server,String name){
		this.server=server;
		this.setName(name);
		setState(STATE_STOPPED);
	}
	
	public ServiceAgent(ServerAgent server,String name,int state){
		this.server=server;
		this.setName(name);
		this.state=state;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the server
	 */
	public ServerAgent getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(ServerAgent server) {
		this.server = server;
	}

	/**
	 * @return the state
	 */
	private int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	private void setState(int state) {
		this.state = state;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public List<TreeElement> getChildren() {
		return null;
	}

	@Override
	public TreeElement getParent() {
		return server;
	}

	@Override
	public Image getImage() {
		if(state==STATE_RUNNING)return ImageFactory.getImage(ImageFactory.RUNNING);
		if(state==STATE_STOPPED)return ImageFactory.getImage(ImageFactory.STOPPED);
		return ImageFactory.getImage(ImageFactory.SERVERS);
	}

}
