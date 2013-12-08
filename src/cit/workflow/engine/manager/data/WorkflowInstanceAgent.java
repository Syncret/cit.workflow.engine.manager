package cit.workflow.engine.manager.data;

import java.util.Date;

import org.eclipse.swt.graphics.Image;

import cit.workflow.engine.manager.image.ImageFactory;

public class WorkflowInstanceAgent {
	private String name;
	private int workflowID;
	private ServerAgent server;
	private double expectTime;
	private double runningTime;
	private double costTime;
	private int state;
	private Date startTime;
	private Date endTime;
	
	public static final int STATE_RUNNING=0;
	public static final int STATE_FINISHED=1;
	public static final int STATE_STOPPED=2;
	public static final int STATE_ABORTTED=3;
	public static final int STATE_WAITING=4;
	
	private static final String[] states={"Running","Finished","Stopped","Abortted","Waiting"};
	public WorkflowInstanceAgent(String name, int workflowID,
			ServerAgent server, Date startTime, double expectTime) {
		super();
		this.name = name;
		this.workflowID = workflowID;
		this.server = server;
		this.startTime = startTime;
		this.expectTime = expectTime;
		this.state=STATE_WAITING;
		this.runningTime=0;
	}
	public ServerAgent getServer() {
		return server;
	}
	public void setServer(ServerAgent server) {
		this.server = server;
	}
	public double getExpectTime() {
		return expectTime;
	}
	public void setExpectTime(double expectTime) {
		this.expectTime = expectTime;
	}
	public double getCostTime() {
		return costTime;
	}
	public void setCostTime(double costTime) {
		this.costTime = costTime;
	}
	public int getState() {
		return state;
	}
	public String getStateString(){
		return states[state];
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getWorkflowID() {
		return workflowID;
	}
	public void setWorkflowID(int workflowID) {
		this.workflowID = workflowID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getRunningTime() {
		return runningTime;
	}
	public void setRunningTime(double runningTime) {
		this.runningTime = runningTime;
	}
	public double getProgress(){
		if(state==STATE_FINISHED) return 1.00;
		return runningTime/expectTime;
	}
	public Image getImage() {
		if(state==STATE_WAITING) return ImageFactory.getImage(ImageFactory.GREENCIRCLE);
		if(state==STATE_RUNNING) return ImageFactory.getImage(ImageFactory.RUNNING);
		if(state==STATE_STOPPED||state==STATE_ABORTTED) return ImageFactory.getImage(ImageFactory.STOPPED);
		if(state==STATE_FINISHED) return ImageFactory.getImage(ImageFactory.FINISHED);
		return ImageFactory.getImage(ImageFactory.GREENCIRCLE);
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public boolean overtime(){
		return runningTime>expectTime;
	}
}
