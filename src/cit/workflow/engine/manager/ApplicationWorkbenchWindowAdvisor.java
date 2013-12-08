package cit.workflow.engine.manager;

import java.net.MalformedURLException;
import java.net.URL;
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
    
    //initial some data for test
    private void initDummyDate(){
    	URL url1 = null;
    	URL url2 = null;
		try {
			url1 = new URL("http://192.168.1.1");
			url2=new URL("http://192.168.1.2:8080");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ServerAgent server=new ServerAgent(url1,ServerAgent.STATE_RUNNING);
    	//add service
    	server.addService(new ServiceAgent(server,"Engine Service",ServiceAgent.STATE_RUNNING));
    	server.addService(new ServiceAgent(server,"Test Service",ServiceAgent.STATE_STOPPED));
    	
    	//add workflow instances
    	WorkflowInstanceAgent instance1=new WorkflowInstanceAgent("testWorkflow1", 1, server, new Date(), 100); 
    	instance1.setState(WorkflowInstanceAgent.STATE_RUNNING);
    	instance1.setRunningTime(30);
    	server.addWorkflowInstance(instance1);
    	
    	WorkflowInstanceAgent instance2=new WorkflowInstanceAgent("testWorkflow2", 1, server, new Date(), 100); 
    	instance2.setState(WorkflowInstanceAgent.STATE_FINISHED);
    	instance2.setEndTime(new Date());
    	server.addWorkflowInstance(instance2);
    	
    	WorkflowInstanceAgent instance3=new WorkflowInstanceAgent("testWorkflow3", 1, server, new Date(), 100); 
    	instance3.setState(WorkflowInstanceAgent.STATE_WAITING);
    	server.addWorkflowInstance(instance3);
    	
//    	WorkflowInstanceAgent instance4=new WorkflowInstanceAgent("testWorkflow4", 1, server, new Date(), 100); 
//    	instance4.setState(WorkflowInstanceAgent.STATE_RUNNING);
//    	instance4.setRunningTime(120);
//    	server.addWorkflowInstance(instance4);
//    	
    	WorkflowInstanceAgent instance5=new WorkflowInstanceAgent("testWorkflow5", 1, server, new Date(), 100); 
    	instance5.setState(WorkflowInstanceAgent.STATE_STOPPED);
    	instance5.setRunningTime(70);
    	server.addWorkflowInstance(instance5);
    	
    	ServerList.addServer(server);
    	ServerAgent server2=new ServerAgent(url2,ServerAgent.STATE_STOPPED);
    	ServerList.addServer(server2);
    }
    
}
