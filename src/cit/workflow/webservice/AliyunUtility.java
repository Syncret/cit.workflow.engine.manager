package cit.workflow.webservice;
import com.aliyun.api.AliyunClient;
import com.aliyun.api.DefaultAliyunClient;
import com.aliyun.api.ecs.ecs20140526.*;
import com.aliyun.api.ecs.ecs20140526.request.*;
import com.aliyun.api.ecs.ecs20140526.response.*;
import com.taobao.api.ApiException;
import com.taobao.api.internal.util.StringUtils;

import cit.workflow.*;

public class AliyunUtility {
	private static final AliyunUtility instance=new AliyunUtility();
	public static AliyunUtility getInstance(){return instance;}
	private static AliyunClient client;
	static {
		String serverUrl = "http://ecs.aliyuncs.com/"; // 例如: http://ecs.aliyuncs.com/
		String accessKeyId = Constants.ALI_ACCESSKEYID;
		String accessKeySecret = Constants.ALI_ACCESSKEYSECRET;

		// 初始化一个AliyunClient
		client = new DefaultAliyunClient(serverUrl, accessKeyId, accessKeySecret);
	}
	
	public void createInstance() {
	    CreateInstanceRequest createInstanceRequest = new CreateInstanceRequest();
	    createInstanceRequest.setRegionId("cn-qingdao");
	    createInstanceRequest.setImageId("ubuntu1404_64_20G_aliaegis_20150325.vhd");
	    createInstanceRequest.setInstanceType("ecs.t1.small");
	    createInstanceRequest.setSecurityGroupId("sg-28mf6bp8q");

	    try {
	        CreateInstanceResponse createInstanceResponse = client.execute(createInstanceRequest);
	        if (StringUtils.isEmpty(createInstanceResponse.getErrorCode())) { //创建成功
	            String instanceId = createInstanceResponse.getInstanceId(); //获取实例ID
	        } else {
	            String errCode = createInstanceResponse.getErrorCode(); //获取错误码
	            String message = createInstanceResponse.getMessage(); //获取错误信息
	        }
	    } catch (ApiException e) {
	        // TODO: handle exception
	    }
	}
	
	public void startInstance(String instanceId){
		StartInstanceRequest startInstanceRequest=new StartInstanceRequest();
		startInstanceRequest.setInstanceId(instanceId);
		
		try{
			StartInstanceResponse startInstanceResponse=client.execute(startInstanceRequest);
			if(StringUtils.isEmpty(startInstanceResponse.getErrorCode())){
				
			} else {
				String errCode=startInstanceResponse.getErrorCode();
				String message=startInstanceResponse.getMessage();
			}
		}catch(ApiException e){
			
		}
	}
	
	public void stopInstance(String instanceId){
		StopInstanceRequest stopRequest=new StopInstanceRequest();
		stopRequest.setInstanceId(instanceId);
		
		try{
			StopInstanceResponse stopResponse=client.execute(stopRequest);
			if(StringUtils.isEmpty(stopResponse.getErrorCode())){
				
			} else {
				String errCode=stopResponse.getErrorCode();
				String message=stopResponse.getMessage();
			}
		}catch(ApiException e){
			
		}
	}
	
	public void describeInstanceAttribute(String instanceId) {
	    DescribeInstanceAttributeRequest describeInstanceAttributeRequest = new DescribeInstanceAttributeRequest();
	    describeInstanceAttributeRequest.setInstanceId(instanceId);
	    try {
	        DescribeInstanceAttributeResponse describeInstanceAttributeResponse = client.execute(describeInstanceAttributeRequest);
	        if (StringUtils.isEmpty(describeInstanceAttributeResponse.getErrorCode())) { //查询成功
	        	describeInstanceAttributeResponse.getClusterId();
	            //查看实例信息的代码
	            //......
	        } else {
	            String errCode = describeInstanceAttributeResponse.getErrorCode(); //获取错误码
	            String message = describeInstanceAttributeResponse.getMessage(); //获取错误信息
	        }
	    } catch (ApiException e) {
	        // TODO: handle exception
	    }
	}
	
	public void deleteInstance(String instanceId) {
	    DeleteInstanceRequest deleteInstanceRequest =new DeleteInstanceRequest();
	    deleteInstanceRequest.setInstanceId(instanceId);
	    try {
	        DeleteInstanceResponse deleteInstanceResponse=client.execute(deleteInstanceRequest);
	        if (StringUtils.isEmpty(deleteInstanceResponse.getErrorCode())) { //删除成功

	        } else { //删除失败
	            String errorCode = deleteInstanceResponse.getErrorCode(); //获取错误码
	            String message = deleteInstanceResponse.getMessage(); //获取错误信息
	        }
	    } catch (ApiException e) {
	        // TODO: handle exception
	    }
	}

	public static void main(String[] args) {
		AliyunUtility ali=AliyunUtility.getInstance();
//		ali.stopInstance("i-28dfb7k39");
//		ali.deleteInstance("i-28dfb7k39");
//		ali.createInstance(); 
		//ali.describeInstanceAttribute("i-28dfb7k39");
	}
}
