package cit.workflow.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedList;

import javax.jws.WebMethod;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jfree.data.time.TimeSeries;

import cit.workflow.engine.manager.views.ConsoleView;


public class WorkflowServerClient {
	
	private WorkflowServerInterface client=null;
	private URL url;
	
	public WorkflowServerClient(String url){
		try {
			this.url=new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public WorkflowServerClient(URL url){
		this.url=url;
	}
	
	public boolean connect() {
		try {
//			URL url = new URL(
//					"http://localhost:8080/axis2/services/WorkflowServerImpl?wsdl");
//			QName qname = new QName("http://webservice.workflow.cit/",
//					"WorkflowServerImpl");
//			Service service = Service.create(url, qname);
//			client = service.getPort(new QName(
//					"http://webservice.workflow.cit/",
//					"WorkflowServerImplServiceHttpSoap12Endpoint"),
//					WorkflowServerInterface.class);
			QName qname = new QName("http://webservice.workflow.cit/",
					"WorkflowServerImplService");
			Service service = Service.create(url, qname);
			client = service.getPort(
					new QName("http://webservice.workflow.cit/",
							"WorkflowServerImplPort"),
					WorkflowServerInterface.class);
//			((BindingProvider)client).getRequestContext().put(
//				     BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wnsaddr);
			return true;
		} catch (Exception e1) {
			ConsoleView.println(e1.getMessage());
			return false;
		}
//		//servercomment System.out.println("connect successfully");
	}

	public String instantiateWorkflow(int workflowID) throws RemoteException{
		return client.instantiateWorkflow(workflowID);
	}

	public Object[] startProcess(String processID) 
			throws RemoteException{
		return client.startProcess(processID);
	}
	
	public LinkedList<Object[]> getCpuPerfList(){
		return client.getCpuPerfList();
	}
	
	public LinkedList<Object[]> getMemoryPerfList(){
		return client.getMemoryPerfList();
	}
	
	public void setMonitorInteval(long inteval){
		client.setMonitorInteval(inteval);
	}
	
	public void setRun(boolean run){
		client.setRun(run);
	}
	
	public Object[] executeWorkflow(int workflowID)
			throws RemoteException{
		return client.executeWorkflow(workflowID);
	}
	
	public static void main(String[] args){
		WorkflowServerClient client=new WorkflowServerClient("http://localhost:8080/workflow/Workflow?wsdl");
		client.connect();
		try {
			client.executeWorkflow(10);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
