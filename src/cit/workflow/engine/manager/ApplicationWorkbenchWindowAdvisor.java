package cit.workflow.engine.manager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import cit.workflow.engine.manager.data.ServerList.ServerPair;
import cit.workflow.engine.manager.util.ConnectionPool;
import cit.workflow.engine.manager.util.RequestAccepter;
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
        configurer.setTitle("CIT云工作流管理平台");
        
        
        initDummyDate();
        ServerList.initialized=true;
    }
    
    @Override
    public void postWindowCreate(){
    	ConsoleView.showConsole();
    	writeDummyLog();
    	RequestAccepter.getInstance().start();
    }
    
    
    @Override
    public void postWindowClose(){
    	RequestAccepter.getInstance().close();
    	try {
			ConnectionPool.getInstance().closeConnectionPool();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    //initial some data for test
    private void initDummyDate(){

    	String url="http://localhost:8080";
    	ServerAgent ec1=new ServerAgent(url,ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO,ServerAgent.LOC_LOCAL,"");
    	ec1.setName("192.168.1.30");
    	ServiceAgent es1=new ServiceAgent(ec1, ServiceAgent.STATE_RUNNING);
    	ec1.addService(es1);
    	ServerList.addServer(ec1);
    	
//    	ServerAgent ec0=new ServerAgent("http://52.10.114.68:8080",ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,"i-8beb674d");
//    	ServiceAgent es0=new ServiceAgent(ec0, ServiceAgent.STATE_RUNNING);
//    	ec0.addService(es0);
//    	ServerList.addServer(ec0);
    	
    	ServerAgent ec2=new ServerAgent("http://52.10.114.68:8080",ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,"i-8ace192l");
    	ServiceAgent es2=new ServiceAgent(ec2, ServiceAgent.STATE_RUNNING);
    	ec2.addService(es2);
    	ServerList.addServer(ec2);
    	
    	ServerAgent ec3=new ServerAgent("http://52.10.114.68:8080",ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,"i-8mhk221x");
    	ServiceAgent es3=new ServiceAgent(ec2, ServiceAgent.STATE_RUNNING);
    	ec3.addService(es3);
    	ServerList.addServer(ec3);
////    	

//    	
//    	ServerAgent ali=new ServerAgent("http://121.42.198.139:8080",ServerAgent.STATE_AVAILABLE,ServerAgent.TYPE_MICRO,ServerAgent.LOC_ALIYUN,"i-28v6phbw6");
//    	ServiceAgent alis=new ServiceAgent(ali,ServiceAgent.STATE_RUNNING);
//    	ali.addService(alis);
//    	ServerList.addServer(ali);
    	
//    	ServerList.ServerNumberRecord.add(new ServerPair(1443343459062l, 0));
//    	ServerList.ServerNumberRecord.add(new ServerPair(1443353459062l, 1));
//    	ServerList.ServerNumberRecord.add(new ServerPair(1443353659062l, 2));
//    	ServerList.ServerNumberRecord.add(new ServerPair(1443354163777l, 3));
//    	ServerList.ServerNumberRecord.add(new ServerPair(1443356629652l, 4));
//    	ServerList.ServerNumberRecord.add(new ServerPair(1443358310852l, 3));
    	  	
    }
    
    private void writeDummyLog(){
		try {
			BufferedReader br = new BufferedReader(new FileReader("D:/data/log.txt"));
	    	String line="";
	    	StringBuffer  buffer = new StringBuffer();
	    	while((line=br.readLine())!=null){
	    		buffer.append(line);
	    		buffer.append("\n");
	    	}
	    	String fileContent = buffer.toString();
	    	
	    	ConsoleView.println(fileContent);
		} catch (FileNotFoundException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}  
    }
    
}
