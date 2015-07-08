package cit.workflow.engine.manager.util;

import cit.workflow.engine.manager.data.WorkflowInstanceAgent;
import cit.workflow.engine.manager.views.ConsoleView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class RequestGenerator extends Thread{
	private static RequestGenerator generator=new RequestGenerator();
	public static RequestGenerator getInstance(){return generator;}
	private RequestGenerator(){
		starttime=System.currentTimeMillis();
	};
	private long starttime=0;
	private int inteval=30000;
	private boolean run=true;
	private int patternType=1;
	public static int PATTERN_TYPE_SUM=0;
	public static int PATTERN_TYPE_SINGLE=1;
//	private int[] sumpattern={10,15,20,25,30,35,40,45,50,50,45,40,35,30,25,20,15,10,15,20,25,30,35,40,45,50,50,45,40,35,30,25,20,15,10};
	private int[] sumpattern={1,4,5,5,5,5,5,5,5,5};
	//private double[] singlePattern={1.28, 1.29, 1.48, 1.48, 1.77, 1.77, 1.85, 1.85, 1.99, 1.99, 2.37, 2.37, 2.44, 2.45, 2.47, 2.49, 2.50, 2.52, 2.53, 2.56, 2.59, 2.60, 2.62, 2.63, 2.64, 2.67, 2.68, 2.68, 2.69, 2.72, 2.73, 2.74, 2.75, 2.76, 2.77, 2.78, 2.79, 2.80, 2.82, 4.75, 4.75, 4.76, 4.76, 5.55, 5.56, 6.49, 6.50, 6.61, 6.61, 6.66, 6.66, 6.68, 6.69, 6.87, 6.87, 6.96, 6.96, 6.99, 7.00, 7.04, 7.04, 7.06, 7.07, 7.07, 7.07, 7.08, 7.08, 7.08, 7.09, 7.09, 7.12, 7.12, 7.12, 7.12, 7.15, 7.15, 7.15, 7.15, 7.18, 7.18, 7.18, 7.18, 7.18, 7.19, 7.20, 7.21, 7.21, 7.22, 7.23, 7.24, 7.24, 7.24, 7.25, 7.25, 7.26, 7.27, 7.27, 7.32, 7.33, 7.35, 7.35, 7.40, 7.41, 7.44, 7.44, 7.47, 7.48, 7.48, 7.48, 7.48, 7.48, 7.48, 7.49, 7.49, 7.49, 7.51, 7.51, 7.51, 7.51, 7.53, 7.53, 7.56, 7.56, 7.61, 7.61, 7.64, 7.65, 7.65, 7.65, 7.66, 7.66, 7.66, 7.67, 7.70, 7.70, 7.72, 7.72, 7.73, 7.73, 7.73, 7.73, 7.78, 7.78, 7.79, 7.79, 7.80, 7.81, 7.83, 7.84, 7.84, 7.84, 7.87, 7.87, 7.87, 7.87, 7.88, 7.88, 7.89, 7.90, 7.92, 7.92, 7.93, 7.93, 7.93, 7.93, 7.95, 7.96, 7.96, 7.96, 7.96, 7.97, 7.98, 7.99, 8.06, 8.06, 8.08, 8.08, 8.08, 8.09, 8.09, 8.09, 8.12, 8.12, 8.13, 8.14, 8.18, 8.18, 8.32, 8.32, 8.32, 8.33, 8.50, 8.50, 8.53, 8.53, 8.56, 8.56, 8.66, 8.66, 8.66, 8.66, 8.84, 8.85, 8.99, 8.99, 9.01, 9.01, 9.01, 9.01, 9.05, 9.05, 9.11, 9.11, 9.21, 9.21, 9.28, 9.28, 9.51, 9.51, 9.52, 9.52, 9.58, 9.58, 9.59, 9.59, 9.72, 9.73, 9.76, 9.77, 10.04, 10.05, 10.11, 10.11, 10.13, 10.13, 10.22, 10.22, 10.23, 10.23, 10.23, 10.23, 10.63, 10.64, 10.65, 10.65, 10.66, 10.66, 10.71, 10.71, 10.78, 10.78, 10.79, 10.79, 10.98, 10.98, 11.08, 11.08, 11.17, 11.17, 11.48, 11.48, 11.52, 11.52, 11.59, 11.59, 11.60, 11.61, 11.66, 11.66, 11.69, 11.70, 11.95, 11.95, 12.03, 12.03, 12.05, 12.05, 12.13, 12.13, 12.30, 12.30, 12.32, 12.32, 12.57, 12.57, 12.64, 12.64, 12.65, 12.65, 12.66, 12.67, 12.74, 12.74, 12.75, 12.75, 12.78, 12.78, 12.83, 12.83, 12.87, 12.88, 12.94, 12.94, 13.03, 13.03, 13.06, 13.06, 13.07, 13.07, 13.11, 13.11, 13.12, 13.12, 13.15, 13.15, 13.22, 13.22, 13.33, 13.33, 13.41, 13.41, 13.41, 13.42, 13.50, 13.50, 13.52, 13.52, 13.52, 13.53, 13.71, 13.71, 13.71, 13.71, 13.78, 13.78, 13.80, 13.80, 13.91, 13.91, 14.00, 14.00, 14.00, 14.00, 14.05, 14.05, 14.06, 14.06, 14.20, 14.20, 14.25, 14.25, 14.31, 14.31, 14.37, 14.37, 14.39, 14.39, 14.39, 14.39, 14.40, 14.40, 14.43, 14.52, 14.52, 14.53, 14.55, 14.55, 14.55, 14.55, 14.57, 14.57, 14.65, 14.65, 14.78, 14.78, 14.83, 14.88, 14.88, 14.92, 14.93, 14.93, 14.93, 14.95, 14.95, 15.15, 15.15, 15.16, 15.16, 15.22, 15.22, 15.30, 15.30, 15.34, 15.34, 15.39, 15.39, 15.41, 15.41, 15.69, 15.69, 15.91, 15.91, 16.00, 16.00, 16.07, 16.07, 16.17, 16.17, 16.20, 16.20, 16.23, 16.23, 16.40, 16.40, 16.50, 16.51, 16.59, 16.59, 16.78, 16.78, 17.40, 17.40, 18.14, 18.14, 18.74, 18.74, 18.98, 18.99, 19.12, 19.12, 19.35, 19.35, 19.54, 19.54, 19.65, 19.65, 19.77, 19.77, 20.08, 20.08, 20.10, 20.10, 20.34, 20.34, 20.56, 20.56, 21.06, 21.06, 22.43, 22.43, 22.52, 22.52, 22.66, 22.66, 23.12, 23.12, 23.49, 23.49, 23.65, 23.66, 23.68, 23.68};
	//private double[] singlePattern={1.25, 1.29, 1.48, 1.48, 1.77, 1.77, 1.85, 1.85, 1.99, 1.99, 2.37, 2.37, 2.44, 2.45, 2.47, 2.49, 2.50, 2.52, 2.53, 2.56, 2.59, 2.60, 2.62, 2.63, 2.64, 2.67, 2.68, 2.68, 2.69, 2.72, 2.73, 2.74, 2.75, 2.76, 2.77, 2.78, 2.79, 2.80, 2.82, 4.75, 4.75, 4.76, 4.76, 5.55, 5.56, };
	private double[] singlePattern={6.00, 6.50, 6.61, 6.61, 6.66, 6.66, 6.68, 6.69, 6.87, 6.87, 6.96, 6.96, 6.99, 7.00, 7.04, 7.04, 7.06, 7.07, 7.07, 7.07, 7.08, 7.08, 7.08, 7.09, 7.09, 7.12, 7.12, 7.12, 7.12, 7.15, 7.15, 7.15, 7.15, 7.18, 7.18, 7.18, 7.18, 7.18, 7.19, 7.20, 7.21, 7.21, 7.22, 7.23, 7.24, 7.24, 7.24, 7.25, 7.25, 7.26, 7.27, 7.27, 7.32, 7.33, 7.35, 7.35, 7.40, 7.41, 7.44, 7.44, 7.47, 7.48, 7.48, 7.48, 7.48, 7.48, 7.48, 7.49, 7.49, 7.49, 7.51, 7.51, 7.51, 7.51, 7.53, 7.53, 7.56, 7.56, 7.61, 7.61, 7.64, 7.65, 7.65, 7.65, 7.66, 7.66, 7.66, 7.67, 7.70, 7.70, 7.72, 7.72, 7.73, 7.73, 7.73, 7.73, 7.78, 7.78, 7.79, 7.79, 7.80, 7.81, 7.83, 7.84, 7.84, 7.84, 7.87, 7.87, 7.87, 7.87, 7.88, 7.88, 7.89, 7.90, 7.92, 7.92, 7.93, 7.93, 7.93, 7.93, 7.95, 7.96, 7.96, 7.96, 7.96, 7.97, 7.98, 7.99, 8.06, 8.06, 8.08, 8.08, 8.08, 8.09, 8.09, 8.09, 8.12, 8.12, 8.13, 8.14, 8.18, 8.18, 8.32, 8.32, 8.32, 8.33, 8.50, 8.50, 8.53, 8.53, 8.56, 8.56, 8.66, 8.66, 8.66, 8.66, 8.84, 8.85, 8.99, 8.99, 9.01, 9.01, 9.01, 9.01, 9.05, 9.05, 9.11, 9.11, 9.21, 9.21, 9.28, 9.28, 9.51, 9.51, 9.52, 9.52, 9.58, 9.58, 9.59, 9.59, 9.72, 9.73, 9.76, 9.77, 10.04, 10.05, 10.11, 10.11, 10.13, 10.13, 10.22, 10.22, 10.23, 10.23, 10.23, 10.23, 10.63, 10.64, 10.65, 10.65, 10.66, 10.66, 10.71, 10.71, 10.78, 10.78, 10.79, 10.79, 10.98, 10.98, 11.08, 11.08, 11.17, 11.17, 11.48, 11.48, 11.52, 11.52, 11.59, 11.59, 11.60, 11.61, 11.66, 11.66, 11.69, 11.70, 11.95, 11.95, 12.03, 12.03, 12.05, 12.05, 12.13, 12.13, 12.30, 12.30, 12.32, 12.32, 12.57, 12.57, 12.64, 12.64, 12.65, 12.65, 12.66, 12.67, 12.74, 12.74, 12.75, 12.75, 12.78, 12.78, 12.83, 12.83, 12.87, 12.88, 12.94, 12.94, 13.03, 13.03, 13.06, 13.06, 13.07, 13.07, 13.11, 13.11, 13.12, 13.12, 13.15, 13.15, 13.22, 13.22, 13.33, 13.33, 13.41, 13.41, 13.41, 13.42, 13.50, 13.50, 13.52, 13.52, 13.52, 13.53, 13.71, 13.71, 13.71, 13.71, 13.78, 13.78, 13.80, 13.80, 13.91, 13.91, 14.00, 14.00, 14.00, 14.00, 14.05, 14.05, 14.06, 14.06, 14.20, 14.20, 14.25, 14.25, 14.31, 14.31, 14.37, 14.37, 14.39, 14.39, 14.39, 14.39, 14.40, 14.40, 14.43, 14.52, 14.52, 14.53, 14.55, 14.55, 14.55, 14.55, 14.57, 14.57, 14.65, 14.65, 14.78, 14.78, 14.83, 14.88, 14.88, 14.92, 14.93, 14.93, 14.93, 14.95, 14.95, 15.15, 15.15, 15.16, 15.16, 15.22, 15.22, 15.30, 15.30, 15.34, 15.34, 15.39, 15.39, 15.41, 15.41, 15.69, 15.69, 15.91, 15.91, 16.00, 16.00, 16.07, 16.07, 16.17, 16.17, 16.20, 16.20, 16.23, 16.23, 16.40, 16.40, 16.50, 16.51, 16.59, 16.59, 16.78, 16.78, 17.40, 17.40, 18.14, 18.14, 18.74, 18.74, 18.98, 18.99, 19.12, 19.12, 19.35, 19.35, 19.54, 19.54, 19.65, 19.65, 19.77, 19.77 };
	
	//private double[] singlePattern={20.00, 20.08, 20.10, 20.10, 20.34, 20.34, 20.56, 20.56, 21.06, 21.06, 22.43, 22.43, 22.52, 22.52, 22.66, 22.66, 23.12, 23.12, 23.49, 23.49, 23.65, 23.66, 23.68, 23.68};
	
	private ReentrantLock requestLock=new ReentrantLock();
	private int requestNum=0;
	private List<Integer> historyRequestNum=new ArrayList<Integer>();
	
	private int defaultWorkflowId=60*15;//second
	
	
	public void run() {
    	if(patternType==PATTERN_TYPE_SUM)sumRequestsPattern();
    	if(patternType==PATTERN_TYPE_SINGLE)singleRequestsPattern();
    }
	
	private void singleRequestsPattern(){
		long now=System.currentTimeMillis();
		if(starttime==0) starttime=now-(long)(singlePattern[0]*60*60*1000);
		for(int j=0;j<singlePattern.length;j++){
			//wait until next request come
			now=System.currentTimeMillis();
			long tosleep=(long)(singlePattern[j]*60*60*1000)+starttime-now;
			if(tosleep<0)tosleep=0;
			try {
				Thread.sleep(tosleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//handle request
			WorkflowInstanceAgent wsAgent = new WorkflowInstanceAgent(defaultWorkflowId);
			wsAgent.setName(String.format("test%03d", j));
			wsAgent.setExpectTime(defaultWorkflowId);
			wsAgent.setMaxDuration(defaultWorkflowId);
			wsAgent.setMinDuration(defaultWorkflowId);
			wsAgent.setProcessID("");
			RequestAssigner.getInstance().acceptRequest(wsAgent);
			requestLock.lock();
			requestNum++;
			requestLock.unlock();
			ConsoleView.println(ConsoleView.LOG_VERBOSE,String.format("%02.3f G %s ", getVirtualTime(),wsAgent.getName()));
		}
		RequestAssigner.getInstance().setEnd(true);
	}
	
	public List<Integer> getHistoryRequestNum(){
		requestLock.lock();
		historyRequestNum.add(requestNum);
		requestNum=0;
		requestLock.unlock();
		return historyRequestNum;
	}
	
	public double getVirtualTime(){
		return (double)(System.currentTimeMillis()-starttime)/60/60/1000;
	}
	
	public double getVirtualTime(long time){
		return (double)(time-starttime)/60/60/1000;
	}
	
	private void sumRequestsPattern(){
		for(int j=0;j<sumpattern.length;j++){
    		int num=sumpattern[j];
    		long now=System.currentTimeMillis();	    		
    		ConsoleView.println("Generator:"+num+" requests");
			for (int i = 0; i < num; i++) {
				WorkflowInstanceAgent wsAgent = new WorkflowInstanceAgent(defaultWorkflowId);
				wsAgent.setName("test");
				wsAgent.setExpectTime(35);
				wsAgent.setMaxDuration(35);
				wsAgent.setMinDuration(35);
				wsAgent.setProcessID("");
				RequestAssigner.getInstance().acceptRequest(wsAgent);
			}
    		try {
				Thread.sleep(now-System.currentTimeMillis()+30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		if(!run)break;
    	}
    	ConsoleView.println("Generator:Complete");
	}
	public boolean isRun() {
		return run;
	}
	public void setRun(boolean run) {
		this.run = run;
	}
	public int getInteval() {
		return inteval;
	}
	public void setInteval(int inteval) {
		this.inteval = inteval;
	}
	public int[] getPattern() {
		return sumpattern;
	}
	public void setPattern(int[] pattern) {
		this.sumpattern = pattern;
	}
	public int getDefaultWorkflowId() {
		return defaultWorkflowId;
	}
	public void setDefaultWorkflowId(int defaultWorkflowId) {
		this.defaultWorkflowId = defaultWorkflowId;
	}
	public int getPatternType() {
		return patternType;
	}
	public void setPatternType(int patternType) {
		this.patternType = patternType;
	}
	public void setStartTime(long startTime){
		this.starttime=System.currentTimeMillis()-(long)(startTime*60*60*1000);
	}
}