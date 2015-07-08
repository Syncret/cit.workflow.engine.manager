package cit.workflow.engine.manager.data;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.graphics.Image;

import cit.workflow.engine.manager.util.ImageFactory;

public class WorkflowInstanceAgent {
	private String name;
	private int workflowID;
	private String processID;
	private ServiceAgent service;
	private double expectTime;
	private int maxDuration;
	private int minDuration;
	private double costTime;
	private int state;
	private long startTime=0;
	private long endTime=0;
	private long busyTime=0;
	private long acceptTime=0;
	//for test, meaning we need to print statics for previous period
	private boolean staticsflag=false;
	private double staticsTimeline; 

	private boolean show=false;
	
	public static final int STATE_RUNNING=0;
	public static final int STATE_FINISHED=1;
	public static final int STATE_STOPPED=2;
	public static final int STATE_ABORTTED=3;
	public static final int STATE_WAITING=4;
	public static final int STATE_FAILED=5;
	
	private static final String[] states={"Running","Finished","Stopped","Abortted","Waiting","Failed"};
//	public WorkflowInstanceAgent(String name, int workflowID,
//			ServiceAgent service, long startTime, double expectTime) {
//		super();
//		this.name = name;
//		this.workflowID = workflowID;
//		this.service = service;
//		this.startTime = startTime;
//		this.expectTime = expectTime;
//		this.state=STATE_WAITING;
//	}
	
	public WorkflowInstanceAgent(int workflowID) {
		super();
		this.workflowID = workflowID;
		this.state=STATE_WAITING;
		this.acceptTime=System.currentTimeMillis();
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
	/**
	 * get the state string according to the input state rather than the actual state of the workflow instance
	 * @param state the state to get string
	 * @return
	 */
	public static String getStateString(int state){
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
	public long getRunningTime() {
		if(startTime==0)return 0;
		return (System.currentTimeMillis()-startTime)/1000;
	}
	public double getProgress(){
		if(state==STATE_FINISHED) return 1.00;
		double progress=getRunningTime()/expectTime;
		if(progress>0.9)return 0.9;
		else return progress;
	}
	public Image getImage() {
		if(state==STATE_WAITING) return ImageFactory.getImage(ImageFactory.GREENCIRCLE);
		if(state==STATE_RUNNING) return ImageFactory.getImage(ImageFactory.RUNNING);
		if(state==STATE_STOPPED||state==STATE_ABORTTED) return ImageFactory.getImage(ImageFactory.STOPPED);
		if(state==STATE_FINISHED) return ImageFactory.getImage(ImageFactory.FINISHED);
		if(state==STATE_FAILED) return ImageFactory.getImage(ImageFactory.DELETE);
		return ImageFactory.getImage(ImageFactory.GREENCIRCLE);
	}
	
	public String getServerName(){
		ServiceAgent service=this.getService();
		if(service==null)return "";
		ServerAgent server=service.getServer();
		if(server==null)return "";
		return server.getName();
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getAcceptTime() {
		return acceptTime;
	}

	public boolean overtime(){
		return getRunningTime()>expectTime;
	}
	public String getProcessID() {
		if(processID==null)return "";
		else return processID;
	}
	public void setProcessID(String processID) {
		this.processID = processID;
	}
	public int getMaxDuration() {
		return maxDuration;
	}
	public void setMaxDuration(int maxDuration) {
		this.maxDuration = maxDuration;
	}
	public int getMinDuration() {
		return minDuration;
	}
	public void setMinDuration(int minDuration) {
		this.minDuration = minDuration;
	}

	public ServiceAgent getService() {
		return service;
	}

	public void setService(ServiceAgent service) {
		this.service = service;
	}

	public long getBusyTime() {
		return busyTime;
	}

	public void setBusyTime(long busyTime) {
		this.busyTime = busyTime;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public long getWaitTime() {
		if(startTime>0)return startTime-acceptTime; 
		else return System.currentTimeMillis()-acceptTime;
	}
	
	public long getSpentTime() {
		if(endTime>0)return endTime-acceptTime; 
		else return System.currentTimeMillis()-acceptTime;
	}

	public void setStaticsflag(boolean staticsflag) {
		this.staticsflag = staticsflag;
	}	
	
	public boolean isStaticsflag() {
		return staticsflag;
	}

	public double getStaticsTimeline() {
		return staticsTimeline;
	}

	public void setStaticsTimeline(double staticsTimeline) {
		this.staticsTimeline = staticsTimeline;
	}

}
