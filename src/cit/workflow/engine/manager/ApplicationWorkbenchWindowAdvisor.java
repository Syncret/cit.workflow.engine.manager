package cit.workflow.engine.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.util.PrefUtil;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.WorkflowInstanceAgent;
import cit.workflow.engine.manager.util.ConnectionPool;
import cit.workflow.engine.manager.views.ConsoleView;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(600, 400));
        configurer.setShowMenuBar(true);
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
        configurer.setShowFastViewBars(true);
        configurer.setShowPerspectiveBar(true);
        configurer.setTitle("Workflow Manager");
        
        
        //set style
//        IPreferenceStore preStore=PrefUtil.getAPIPreferenceStore();
//        preStore.setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
        
        //initial data
        initDummyDate();
    }
    
    @Override
    public void postWindowCreate(){
    	ConsoleView.showConsole();
    }
    
    @Override
    public void postWindowClose(){
    	try {
			ConnectionPool.getInstance().closeConnectionPool();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    //initial some data for test
    private void initDummyDate(){

//    	ServerAgent localServer=new ServerAgent("http://localhost:8080",ServerAgent.STATE_RUNNING,ServerAgent.TYPE_SMALL);
//    	localServer.addService(new ServiceAgent(localServer, "Engine Service", "workflow", "Workflow", ServiceAgent.STATE_RUNNING,ServiceAgent.TYPE_ENGINE));
//    	ServerList.addServer(localServer);
    	
    	
    	String url="http://192.168.1.30:8080";
    	ServerAgent ec1=new ServerAgent(url,ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO);
    	ServiceAgent es2=new ServiceAgent(ec1, ServiceAgent.STATE_RUNNING);
    	ec1.addService(es2);
    	ServerList.addServer(ec1);
    	
    	ServerAgent ec2=new ServerAgent("http://52.10.114.68:8080",ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,"i-91178266");
    	ServiceAgent es1=new ServiceAgent(ec2, ServiceAgent.STATE_RUNNING);
    	ec2.addService(es1);
    	ServerList.addServer(ec2);
    	
    	ServerAgent ali=new ServerAgent("http://10.173.249.189:8080",ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO,ServerAgent.LOC_ALIYUN,"i-28hd94z1v");
    	ServerList.addServer(ali);
    	
//    	String host1="http://192.168.1.30:8080";    	
//    	for(int i=0;i<3;i++){
//    		ServerAgent small=new ServerAgent(host1,ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_SMALL);
//    		small.addService(new ServiceAgent(null, ServiceAgent.STATE_AVAILABLE));
//    		small.setName("small"+i);
//    		if(i==0){
//    			small.getEngineSerivce().setState(ServiceAgent.STATE_RUNNING);
//    			small.setState(ServiceAgent.STATE_RUNNING);
//    		}
//    		ServerList.addServer(small);
//    	}
//    	
//    	for(int i=0;i<2;i++){
//    		ServerAgent middle=new ServerAgent(host1,ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MIDDLE);
//    		middle.addService(new ServiceAgent(null, ServiceAgent.STATE_AVAILABLE));
//    		middle.setName("middle"+i);
//    		ServerList.addServer(middle);
//    	}
//    	ServerAgent server1=new ServerAgent("http://122.192.64.35:8080",ServerAgent.STATE_RUNNING,ServerAgent.TYPE_MIDDLE);
//    	server1.addService(new ServiceAgent(server1, ServiceAgent.STATE_RUNNING));
//    	server1.setName("middle2");
//    	ServerList.addServer(server1);
//    	for(int i=0;i<1;i++){
//    		ServerAgent big=new ServerAgent(host1,ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_BIG);
//    		big.addService(new ServiceAgent(null, "Engine Service", "workflow", "Workflow", ServiceAgent.STATE_AVAILABLE,ServiceAgent.TYPE_ENGINE));
//    		big.setName("big"+i);
//    		ServerList.addServer(big);
//    	}
    }
    
}
