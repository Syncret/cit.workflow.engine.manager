package cit.workflow.webservice;
import com.aliyun.api.AliyunClient;
import com.aliyun.api.DefaultAliyunClient;
import com.aliyun.api.domain.Instance;
import com.aliyun.api.domain.InstanceStatus;
import com.aliyun.api.domain.IpAddress;
import com.aliyun.api.ecs.ecs20130110.request.DescribeInstanceStatusRequest;
import com.aliyun.api.ecs.ecs20130110.response.DescribeInstanceStatusResponse;
import com.aliyun.api.ecs.ecs20140526.request.*;
import com.aliyun.api.ecs.ecs20140526.response.*;
import com.amazonaws.services.ec2.model.InstanceState;
import com.taobao.api.ApiException;
import com.taobao.api.internal.util.StringUtils;

import cit.workflow.*;

public class AliyunUtility {
	private static final AliyunUtility instance=new AliyunUtility();
	public static AliyunUtility getInstance(){return instance;}
	private static AliyunClient client;
	static {
		String serverUrl = "http://ecs.aliyuncs.com/"; 
		String accessKeyId = Constants.ALI_ACCESSKEYID;
		String accessKeySecret = Constants.ALI_ACCESSKEYSECRET;

		// 初始化一个AliyunClient
		client = new DefaultAliyunClient(serverUrl, accessKeyId, accessKeySecret);
	}
	
	
    //you can only get instanceId and errorCode with the parameters you send and requestId
	//you have to describe the Instance to get the ip and other information
    //the state of the host you create will be initially stopped
	//you have to start the instance to use it
	//it seems the default instance won't have public IP
	//you have to apply for a public IP in addition and assign the IP to instance
	public String createInstance() {
	    CreateInstanceRequest createInstanceRequest = new CreateInstanceRequest();
	    createInstanceRequest.setRegionId("cn-qingdao");
	    createInstanceRequest.setImageId("ubuntu1404_64_20G_aliaegis_20150325.vhd");
	    createInstanceRequest.setInstanceType("ecs.t1.small");
	    createInstanceRequest.setSecurityGroupId("sg-28mf6bp8q");

	    try {
	        CreateInstanceResponse createInstanceResponse = client.execute(createInstanceRequest);
	        if (StringUtils.isEmpty(createInstanceResponse.getErrorCode())) { //创建成功
	            String instanceId = createInstanceResponse.getInstanceId(); //获取实例ID
	            return instanceId;
	        } else {
	            String errCode = createInstanceResponse.getErrorCode(); //获取错误码
	            String message = createInstanceResponse.getMessage(); //获取错误信息
	            System.out.println(errCode+": "+message);
	        }
	    } catch (ApiException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	public boolean startInstance(String instanceId){
		StartInstanceRequest startInstanceRequest=new StartInstanceRequest();
		startInstanceRequest.setInstanceId(instanceId);
		
		try{
			StartInstanceResponse startInstanceResponse=client.execute(startInstanceRequest);
			if(StringUtils.isEmpty(startInstanceResponse.getErrorCode())){
				return true;
			} else {
				String errCode=startInstanceResponse.getErrorCode();
				String message=startInstanceResponse.getMessage();
				System.out.println(errCode+": "+message);
			}
		}catch(ApiException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean stopInstance(String instanceId){
		StopInstanceRequest stopRequest=new StopInstanceRequest();
		stopRequest.setInstanceId(instanceId);
		
		try{
			StopInstanceResponse stopResponse=client.execute(stopRequest);
			if(StringUtils.isEmpty(stopResponse.getErrorCode())){
				return true;
			} else {
				String errCode=stopResponse.getErrorCode();
				String message=stopResponse.getMessage();
				System.out.println(errCode+": "+message);
			}
		}catch(ApiException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public DescribeInstanceAttributeResponse describeInstanceAttribute(String instanceId) {
	    DescribeInstanceAttributeRequest describeInstanceAttributeRequest = new DescribeInstanceAttributeRequest();
	    describeInstanceAttributeRequest.setInstanceId(instanceId);
	    try {
	        DescribeInstanceAttributeResponse response = client.execute(describeInstanceAttributeRequest);
	        if (StringUtils.isEmpty(response.getErrorCode())) { //查询成功
//	        	String innerIp=response.getInnerIpAddress().get(0).toString();
	        	//it seems there is a bug in the api, the getInnerIpAddress actually returns String list instead of IpAddress
//	        	Object to=response.getInnerIpAddress().get(0);
//	        	String ip=(String)to;
//	        	String status=response.getStatus();
	        	return response;
	        } else {
	            String errCode = response.getErrorCode(); //获取错误码
	            String message = response.getMessage(); //获取错误信息
	            System.out.println(errCode+": "+message);
	        }
	    } catch (ApiException e) {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	public void describeInstances(){
		DescribeInstancesRequest request=new DescribeInstancesRequest();
		request.setRegionId("cn-qingdao");
		try{
			DescribeInstancesResponse response=client.execute(request);
			if(StringUtils.isEmpty(response.getErrorCode())){
				for(Instance instance:response.getInstances()){
					instance.getStatus();
				}
			} else {
				String errCode=response.getErrorCode();
				String message=response.getMessage();
			}
		}catch (ApiException e) {
	    }		
	}
	
	public void describeInstancesStatus(){
		DescribeInstanceStatusRequest request=new DescribeInstanceStatusRequest();
		request.setRegionId("cn-qingdao");
		try{
			DescribeInstanceStatusResponse response=client.execute(request);
			if(StringUtils.isEmpty(response.getErrorCode())){
				for(InstanceStatus status : response.getInstanceStatuses()){
					status.getInstanceId();
					status.getStatus();
				}
			} else {
				String errCode=response.getErrorCode();
				String message=response.getMessage();
			}
		}catch (ApiException e) {
	    }		
	}
	
	//you can only delete a stopped("Stopped") instance, which means you must stop a running instance before you delete it 
	//but stop instance takes time. So you
	public boolean deleteInstance(String instanceId) {
	    DeleteInstanceRequest deleteInstanceRequest =new DeleteInstanceRequest();
	    deleteInstanceRequest.setInstanceId(instanceId);
	    try {
	        DeleteInstanceResponse deleteInstanceResponse=client.execute(deleteInstanceRequest);
	        if (StringUtils.isEmpty(deleteInstanceResponse.getErrorCode())) { //删除成功
	        	return true;
	        } else { //删除失败
	            String errorCode = deleteInstanceResponse.getErrorCode(); //获取错误码
	            String message = deleteInstanceResponse.getMessage(); //获取错误信息
	            System.out.println(errorCode+": "+message);
	        }
	    } catch (ApiException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	public static void main(String[] args) {
		AliyunUtility ali=AliyunUtility.getInstance();
		String instanceId="i-28f7cflr9";
//		ali.stopInstance(instanceId);
//		System.out.println(ali.deleteInstance(instanceId));
//		ali.createInstance(); 
//		ali.startInstance(instanceId);
		ali.describeInstanceAttribute(instanceId);
//		ali.describeInstances();
//		ali.describeInstancesStatus();
	}
}
