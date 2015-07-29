package cit.workflow.webservice;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import cit.workflow.Constants;
import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.util.RequestAssigner;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class AwsUtility {
	private static final AwsUtility AwsUtilityInstance=new AwsUtility();
	public static AwsUtility GetInstance(){return AwsUtilityInstance;}
	//instance state(low byte) 0:pending  16:running  32:shutting-down  48:terminated  64:stopping  80:stopped
	public static int STATE_PENDING=0;
	public static int STATE_RUNNING=16;
	public static int STATE_SHUTTINGDOWN=32;
	public static int STATE_TERMINATED=48;
	public static int STATE_STOPPING=64;
	public static int STATE_STOPPED=80;
	private boolean test=false;  
	public void set(boolean test){
		this.test=test;
	}
	AWSCredentials credentials;
	AmazonEC2 ec2;
	public AwsUtility(){
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = new AmazonEC2Client(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        ec2.setRegion(usWest2);
	}
	
	private void startRefreshStatusThread(IWorkbenchWindow window){
		RefreshStatusThread thread=new RefreshStatusThread(window);
		thread.setDaemon(true);
		thread.start();
	}
	
	public Instance runInstance(){
		RunInstancesRequest runInstancesRequest=new RunInstancesRequest();
		runInstancesRequest.withImageId(Constants.EC2_IMAGEID).withInstanceType(InstanceType.T2Micro).withMinCount(1)
				.withMaxCount(1).withKeyName(Constants.EC2_KEY).withSecurityGroups(Constants.EC2_SECUREGROUP);
		RunInstancesResult result=ec2.runInstances(runInstancesRequest);
		Reservation re=result.getReservation();
		return re.getInstances().get(0);
	}
	
	public boolean newEC2InstanceFromWorkbench(IWorkbenchWindow window){
		Instance instance=runInstance();
		ConsoleView.println("Start instance "+instance.getInstanceId()+" on AWS EC2");
		ServerAgent server=new ServerAgent(null,ServerAgent.STATE_ACTIVATING,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,instance.getInstanceId());
		server.setName(instance.getInstanceId());
		//ServerAgent server=new ServerAgent(null,ServerAgent.STATE_ACTIVATING,ServerAgent.TYPE_MICRO,ServerAgent.LOC_AWSEC2,"aaa");
		ServerList.addServer(server);
		NavigationView.RefreshNavigationView(window);
    	startRefreshStatusThread(window);
    	return true;
	}
	
	public boolean deleteEC2InstanceFromWorkbench(ServerAgent server){
		if(server.getLocation()!=ServerAgent.LOC_AWSEC2||server.getInstanceId()==null||server.getInstanceId().equals("")){
			ConsoleView.println("Invalid EC2 server");
			return false;
		}
		InstanceState state=terminateInstance(server.getInstanceId());
		int stateCode=state.getCode()%128;
		if(stateCode==32||stateCode==48){
			ConsoleView.println("Success shutdown "+server.getInstanceId()+", current state:"+state.getName());
			ServerList.removeServer(server);
			return true;
		}
		else{
			ConsoleView.println("Failed to shutdown "+server.getInstanceId()+", current state:"+state.getName());
			if(stateCode!=16)ServerList.removeServer(server);
			return false;
		}
	}
	
	public InstanceState terminateInstance(String instanceId){
		List<String> list=new ArrayList<String>();
		list.add(instanceId);
		TerminateInstancesRequest terminateInstancesRequest=new TerminateInstancesRequest(list);
		TerminateInstancesResult result = ec2.terminateInstances(terminateInstancesRequest);
		InstanceStateChange instance=result.getTerminatingInstances().get(0);
		return instance.getCurrentState();
	}
	
	public List<Instance> getInstancesStatus(){
		ArrayList<Instance> instances=new ArrayList<>();
		DescribeInstancesResult describeInstancesResult=ec2.describeInstances();
		List<Reservation> reservations=describeInstancesResult.getReservations();
		
		for(Reservation reservation:reservations){
			instances.add(reservation.getInstances().get(0));
		}
		
		return instances;
	}
	
	public ArrayList<String> findIPs() {

		ArrayList<String> myArr = new ArrayList<String>();

		DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
		List<Reservation> reservations = describeInstancesRequest.getReservations();

		for (Reservation reservation : reservations) {
			Instance instance=reservation.getInstances().get(0);
			System.out.println(instance.getInstanceId()+":"+instance.getState().getName()+":"+instance.getPublicIpAddress());
			if (instance.getPublicIpAddress() != null) {

				myArr.add(reservation.getInstances().get(0).getPublicIpAddress());

			}
		}
		return myArr;
	}
	
	private class RefreshStatusThread extends Thread{
		private IWorkbenchWindow window=null;
		private long inteval=60*1000;
		public long getInteval() {
			return inteval;
		}

		public void setInteval(long inteval) {
			this.inteval = inteval;
		}

		public RefreshStatusThread(IWorkbenchWindow window){
			this.window=window;
		}

		@Override
		public void run(){
			try {
				boolean loop=true;
				ConsoleView.println("start thread to trace servers status");
				while(loop){
					loop=false;
					Thread.sleep(inteval);
					List<Instance> instances=AwsUtility.GetInstance().getInstancesStatus();
					for (ServerAgent server : ServerList.getServers()) {
						if (server.getLocation() == ServerAgent.LOC_AWSEC2 && server.getState() == ServerAgent.STATE_ACTIVATING) {
							boolean found = false;
							for(Instance instance:instances){
								if(instance.getInstanceId().equals(server.getInstanceId())){
									ConsoleView.println(instance.getInstanceId()+" status:"+instance.getState().getName());
									found=true;
									if(instance.getState().getCode()%128!=STATE_RUNNING){
										loop=true;
										break;
									}
									if(server.getURL()==null){
										server.setURL("http://"+instance.getPublicIpAddress()+":8080");
										ConsoleView.println("Get public IP of "+instance.getInstanceId()+": "+instance.getPublicIpAddress());
									}
									if(server.testConnection()){
										ConsoleView.println(server.getName()+" start running, ip:"+server.getURL().toString());
										ServiceAgent service=new ServiceAgent(server, ServiceAgent.STATE_INVALID);
										if(service.testConnection()){
											ConsoleView.println(service.getWsdlURL()+" connect success, service started");
											service.setState(ServiceAgent.STATE_RUNNING);											
											server.addService(service);
											server.setState(ServerAgent.STATE_RUNNING);
											RequestAssigner.getInstance().assignRequestToService(server.getEngineSerivce());
										}
										else {
											loop=true;
											ConsoleView.println(service.getWsdlURL()+" connect failed, Retry later");
										}
									}
									else {
										loop=true;
										ConsoleView.println(server.getName()+" connect failed, ip:"+server.getURL().toString()+". Retry later");
									}
									NavigationView.RefreshNavigationView(window);
								}
							}
							if(!found){
								ConsoleView.println(server.getName()+" cannot be found! Remove from Server List");
								ServerList.removeServer(server);
								break;
							}
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally{
				ConsoleView.println("thread complete");				
			}
		}
		
	}
	
	public static void main(String[] args) {
		List<Instance> instances=AwsUtility.GetInstance().getInstancesStatus();
//		AwsUtility.GetInstance().newEC2InstanceFromWorkbench(null);
	}

}
