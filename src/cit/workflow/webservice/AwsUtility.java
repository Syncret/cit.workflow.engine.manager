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

	
	public Instance runInstance(){
		RunInstancesRequest runInstancesRequest=new RunInstancesRequest();
		runInstancesRequest.withImageId(Constants.EC2_IMAGEID).withInstanceType(InstanceType.T2Micro).withMinCount(1)
				.withMaxCount(1).withKeyName(Constants.EC2_KEY).withSecurityGroups(Constants.EC2_SECUREGROUP);
		RunInstancesResult result=ec2.runInstances(runInstancesRequest);
		Reservation re=result.getReservation();
		return re.getInstances().get(0);
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
	
	public static void main(String[] args) {
		List<Instance> instances=AwsUtility.GetInstance().getInstancesStatus();
	}

}
