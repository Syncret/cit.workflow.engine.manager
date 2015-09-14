package cit.workflow.engine.manager.test;

import cit.workflow.engine.manager.StaticSettings;
import cit.workflow.engine.manager.controller.BaseController;
import cit.workflow.engine.manager.controller.PIDController;
import cit.workflow.engine.manager.controller.PatternBasedController;
import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.util.RequestGenerator;
import cit.workflow.engine.manager.views.ConsoleView;

public class ControllerTest {
	public static void main(String[] args) {
		StaticSettings.LOGTOSQL=false;
		main1();
	}
	
	public static void main1(){
		ConsoleView.logFile=true;
		ConsoleView.logPath="d:/0725pb";
		ConsoleView.println("writing to "+ConsoleView.logPath);
    	initialServers();
    	RequestGenerator generator=RequestGenerator.getInstance();
    	generator.setPatternType(RequestGenerator.PATTERN_TYPE_SINGLE);
    	generator.setStartTime(1);
    	generator.start();
    	BaseController controller=new PatternBasedController(null,BaseController.SERVERCONTROL_LOCAL);
    	controller.initialize();
    	controller.start();
	}
	
	public static void main2(){
		ConsoleView.logFile=true;
		ConsoleView.logPath="d:/testlogtest.txt";
    	RequestGenerator generator=RequestGenerator.getInstance();
    	generator.setStartTime(1);
    	BaseController controller=new PIDController(null,BaseController.SERVERCONTROL_LOCAL);
    	controller.initialize();
    	controller.setDaemon(false);
    	controller.start();
	}
	
	public static void initialServers(int num){
		for(int i=0;i<num;i++)
			initialServers();
	}
	
	public static void initialServers(){
		ServerAgent cit1=generateServer();
		cit1.getEngineSerivce().setState(ServiceAgent.STATE_RUNNING);
    	ServerList.addServer(cit1);
    	
//    	ServerAgent cit2=generateServer();
//    	cit2.getEngineSerivce().setState(ServiceAgent.STATE_RUNNING);
//    	ServerList.addServer(cit2);
	}
	
	private static int serverCount=0;
	public static ServerAgent generateServer(){
		serverCount++;
		ServerAgent server=new ServerAgent("http://192.168.1.30:8080",ServerAgent.STATE_ACTIVATING,
				ServerAgent.TYPE_MICRO,ServerAgent.LOC_LOCAL,String.format("cit%02d", serverCount));
		ServiceAgent service=new ServiceAgent(server, ServiceAgent.STATE_ACTIVATING);
		server.addService(service);
		return server;
	}
}
