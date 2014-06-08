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
    	URL url1 = null;
    	URL url2 = null;
    	URL localURL =null;
		try {
			url1 = new URL("http://192.168.1.34:8080");
			url2=new URL("http://192.168.1.2:8080");
			localURL=new URL("http://localhost:8080");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
    	ServerAgent server=new ServerAgent(url1,ServerAgent.STATE_RUNNING);
    	//add service
    	ServiceAgent sa1=new ServiceAgent(server,"Engine Service","workflow", "Workflow", ServiceAgent.STATE_RUNNING,ServiceAgent.TYPE_ENGINE);
    	server.addService(sa1);
//    	server.addService(new ServiceAgent(server,"Test Service","","",ServiceAgent.STATE_STOPPED));
    	
    	//add workflow instances
    	
    	WorkflowInstanceAgent instance4=new WorkflowInstanceAgent("testflow1", 1001, sa1, System.currentTimeMillis(), 30);
    	instance4.setState(WorkflowInstanceAgent.STATE_RUNNING);
    	sa1.addWorkflowInstance(instance4);

    	WorkflowInstanceAgent instance5=new WorkflowInstanceAgent("testWorkflow", 1, sa1, System.currentTimeMillis(), 100); 
    	instance5.setState(WorkflowInstanceAgent.STATE_FINISHED);
    	instance5.setProcessID("7ab0a981-73d3-48e1-8467-cdf27a631aad");
    	sa1.addWorkflowInstance(instance5);
    	
    	ServerAgent server2=new ServerAgent(url2,ServerAgent.STATE_STOPPED);
    	
    	ServerAgent localServer=new ServerAgent(localURL,ServerAgent.STATE_RUNNING);
    	localServer.addService(new ServiceAgent(localServer, "Engine Service", "workflow", "Workflow", ServiceAgent.STATE_RUNNING,ServiceAgent.TYPE_ENGINE));
    	ServerList.addServer(server);
//    	ServerList.addServer(server2);
    	ServerList.addServer(localServer);
    }
    
}
