package cit.workflow.engine.manager.controller;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.server.EC2ServerController;
import cit.workflow.engine.manager.server.LocalServerController;
import cit.workflow.engine.manager.server.ServerControllerInterface;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.util.RequestGenerator;
import cit.workflow.engine.manager.views.ConsoleView;

public class PatternBasedController extends BaseController{
	public void setRun(boolean run){this.run=run;}
	public boolean isRun(){return run;}
	
	public int serverAbility;
	public int executeTime;//ms
	public double predictAccuracy=0.5;
	
	public static final int[][] HistoryDayRequest={
		{0,0,0,0,0,0,0,2,2,8,11,4,0,0,0,0,0,2,0,0,0,0,4,0,0,0,3,7,18,16,18,30,16,10,10,2,6,4,2,6,0,4,4,2,0,4,4,2,2,0,15,11,18,14,16,10,6,3,9,3,4,2,2,6,0,0,0,0,0,0,2,5,0,0,0,0,0,0,4,0,0,0,2,2,6,0,0,0,0,0,0,0,0,0,8,2},
		{2,0,2,0,0,2,0,2,0,10,16,6,2,0,0,0,0,2,2,0,6,0,2,0,2,0,2,2,33,18,26,30,14,10,14,6,8,2,6,6,8,2,0,0,4,0,2,6,6,8,8,14,14,6,10,6,10,15,12,2,4,8,8,0,2,3,2,0,0,4,2,6,0,4,4,0,0,4,2,2,2,2,0,2,2,0,4,0,0,2,0,0,0,0,2,2},
		{0,0,0,0,0,2,0,4,0,2,6,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,5,18,19,31,29,28,10,10,4,8,2,0,7,0,0,4,12,10,11,15,4,8,2,6,23,15,15,4,14,19,4,8,9,0,6,2,4,2,2,6,4,4,0,0,0,2,6,2,0,0,2,2,0,0,4,2,2,0,2,0,2,0,0,0,0,0,0,2,0},
	  //{0,0,0,0,0,4,0,6,0,7,16,6,0,0,0,0,0,0,0,4,0,0,2,0,0,2,6,6,35,21,26,32,14,5,9,4,10,2,10,2,12,0,8,6,4,2,10,2,6,4,12,8,14,6,10,6,10,13,11,11,6,8,2,3,9,2,4,2,0,2,0,0,2,0,2,2,2,2,4,2,4,2,2,0,2,0,0,0,0,2,4,0,2,2,4,0},
	};
	public static final int[][] HistoryDayPattern={
		{0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,0,0,0,0,0,0,1,1,1,1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,-1,-1,-1,-1,-1,-1,0,0,-1,-1,-1,-1,1,1,1,1,1,1,1,1,-1,-1,-1,-1,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,-1,-1,-1,0,0,0,0},
		{0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,-1,-1,-1,-1,1,1,1,1,0,0,0,0,-1,-1,-1,0,0,0,0,0,0,0,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
	};
	
	private int[] historyRequest={0,0,0,0,0,4,0,6,0,7,16,6,0,0,0,0,0,0,0,4,0,0,2,0,0,2,6,6,35,21,26,32,14,5,9,4,10,2,10,2,12,0,8,6,4,2,10,2,6,4,12,8,14,6,10,6,10,13,11,11,6,8,2,3,9,2,4,2,0,2,0,0,2,0,2,2,2,2,4,2,4,2,2,0,2,0,0,0,0,2,4,0,2,2,4,0};
		

	public PatternBasedController(IWorkbenchWindow window,int serverControllerType){
		super(window, serverControllerType);
		serverAbility=ServerAgent.SERVICECAPACITY[ServerAgent.TYPE_MICRO];
		executeTime=RequestGenerator.getInstance().getDefaultWorkflowId()*1000;
	}

	
	
	@Override
	public void run(){
		List<Integer> pendingReduceServer=new ArrayList<Integer>();
		int predictRequest=0,predictIncrease=0,predictPattern=0;
//		double virtualTime=1;
		while (run) {
			long now = System.currentTimeMillis();			
			int serverNum=ServerList.getEngineServices(ServiceAgent.STATE_RUNNING).size();
			
			double virtualTime=RequestGenerator.getInstance().getVirtualTime();
			//virtualTime+=0.25;
			
			int timeIndex=(int)Math.round(virtualTime/0.25);
			
			double minDiffSum=Integer.MAX_VALUE;
			double diffSum=0;
			int numSum=0;
			int lastTimeIndex=timeIndex-1;
			int resultDay = 0,resultTimeIndex = 0;
			for(int i=0;i<HistoryDayRequest.length;i++){
				for(int j=-3;j<4;j++){
					if(lastTimeIndex+j<0||lastTimeIndex+j>=HistoryDayRequest[0].length)continue;
					diffSum=numSum=0;
					for(int k=0;k<5;k++){
						if(lastTimeIndex+j-k<0||lastTimeIndex-k<0)continue;
						diffSum+=Math.pow(historyRequest[lastTimeIndex-k]-HistoryDayRequest[i][lastTimeIndex+j-k], 2);
						numSum++;
					}
					diffSum/=numSum;
					if(diffSum<minDiffSum){
						minDiffSum=diffSum;
						resultDay=i;
						resultTimeIndex=lastTimeIndex+j;
					}
				}
			}
			int lastPredictRequest=predictRequest;
			int lastActureRequest=historyRequest[lastTimeIndex];
			predictRequest=HistoryDayRequest[resultDay][resultTimeIndex+1];
			predictIncrease=HistoryDayRequest[resultDay][resultTimeIndex+1]-HistoryDayRequest[resultDay][resultTimeIndex];
			if(HistoryDayPattern[resultDay][resultTimeIndex]<0&&HistoryDayPattern[resultDay][resultTimeIndex+1]>0)predictPattern=0;
			else predictPattern=HistoryDayPattern[resultDay][resultTimeIndex];
			//this accuracy only take affect when the request is increasing
			predictAccuracy=Math.abs(((double)lastActureRequest)/lastPredictRequest);
			if(predictAccuracy<0.3)predictAccuracy=0.7;//accurate
			else if(lastActureRequest>=lastPredictRequest)predictAccuracy=0.7;//increase more then predict, high accuracy to make server quick increase
			else if(Math.abs(lastActureRequest-lastPredictRequest)<=4)predictAccuracy=0.5;//still accurate 
			else predictAccuracy=0.3;
			predictAccuracy=0.8;//seems it maybe better to keep a high accuracy....
			ConsoleView.println(String.format("%02.3f S %02d, %02d, %02d(resultDay,resultTimeIndex,lastTimeIndex)", virtualTime,resultDay,resultTimeIndex,lastTimeIndex));
			ConsoleView.println(String.format("%02.3f S %02d, %02d, %f (lPRqs,lARqs,PAcc)", virtualTime,lastPredictRequest,lastActureRequest,predictAccuracy));
			ConsoleView.println(String.format("%02.3f S %02d, %02d, %d (PRqs,PInc,PPtn)", virtualTime,predictRequest,predictIncrease,predictPattern));
			
			
			int correct=getCurrentSize();
			if(correct==1||correct==2)correct=0;//sometimes it's because the request just generated which hasn't been put into serve
			int requires=0;

			if(predictPattern!=0||correct>=0)pendingReduceServer.clear();
			if(predictPattern==-1){
				requires=(int)Math.ceil((double)correct*executeTime/inteval/serverAbility);
			}
			else if(predictPattern==0){
				int nextPattern=HistoryDayPattern[resultDay][resultTimeIndex+1];
				if(nextPattern==1){
					requires=(int)Math.ceil((double)(correct+predictIncrease*predictAccuracy)*executeTime/inteval/serverAbility);
					if(requires<0){
						pendingReduceServer.add(requires);
						requires=0;
					}					
				}
				else if(correct>=0){
					requires=(int)Math.ceil((double)correct*executeTime/inteval/serverAbility);
				}
				else {
					requires=(int)Math.ceil((double)correct*executeTime/inteval/serverAbility);
					pendingReduceServer.add(requires);
					if(pendingReduceServer.size()>=3){
						int max=Integer.MIN_VALUE;
						for(int temp:pendingReduceServer){
							if(temp>max)max=temp;
						}
						requires=max;
					}
					else requires=0;
				}
			}
			else if(predictPattern==1){
				requires=(int)Math.ceil((double)(correct+predictIncrease*predictAccuracy)*executeTime/inteval/serverAbility);
				if(requires<0)requires=0;
			}
			
			if(serverNum==0&&requires<=0)requires=1;

			
			modifyServers(requires, serverNum);
			
			try {
				long toSleep=now + inteval - System.currentTimeMillis();
				if(toSleep<0)toSleep=0;
				Thread.sleep(toSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	

}