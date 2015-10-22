package cit.workflow.engine.manager.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.jfree.data.time.Second;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.util.ConnectionPool;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.SpotPlacement;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class test {
	public static void main(String[] args) {
		insertServerRecode();
		insertWorkflowRecord();
		insertServerStartRecode();
	}
	
	
	public static void sqlTest(){
		Connection conn=null;
		PreparedStatement pst=null;
		try {
			conn=ConnectionPool.getInstance().getConnection();
			String sql="INSERT INTO managerservernumberrecord(date, local, aws, aliyun) VALUES (?,?,?,?)";
			pst=conn.prepareStatement(sql);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(pst!=null){
				try {pst.close();}
				catch (SQLException e) {e.printStackTrace();}
			}
			ConnectionPool.getInstance().returnConnection(conn);
		}
	}
	
	public static void insertServerRecode(){
		try {
			String files="D:/data/servers.txt";
//			String files[]={"D:/0622pid","D:/0627pid20-"};
			String line=null;
			long now=System.currentTimeMillis()-142*60*60*100;
			BufferedReader reader = new BufferedReader(new FileReader(files));
			while((line=reader.readLine())!=null){
				String[] ss=line.split(" ");
				Connection conn=null;
				PreparedStatement pst=null;
				try {
					conn=ConnectionPool.getInstance().getConnection();
					String sql="INSERT INTO managerservernumberrecord(date, local, aws, aliyun) VALUES (?,?,?,?)";
					pst=conn.prepareStatement(sql);
					pst.setLong(1, now+Math.round(Double.parseDouble(ss[0])*60*60*1000));
					pst.setInt(2, Integer.parseInt(ss[1]));
					pst.setInt(3, Integer.parseInt(ss[2]));
					pst.setInt(4, Integer.parseInt(ss[3]));
					pst.executeUpdate();
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally{
					if(pst!=null){
						try {pst.close();}
						catch (SQLException e) {e.printStackTrace();}
					}
					ConnectionPool.getInstance().returnConnection(conn);
				}
			}
			reader.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}			
	}
	
	
	public static void insertServerStartRecode(){
		try {
			String files="D:/data/serverstarts.txt";
//			String files[]={"D:/0622pid","D:/0627pid20-"};
			String line=null;
			long now=System.currentTimeMillis()-142*60*60*100;
			BufferedReader reader = new BufferedReader(new FileReader(files));
			while((line=reader.readLine())!=null){
				String[] ss=line.split(" ");
				Connection conn=null;
				PreparedStatement pst=null;
				try {
					conn=ConnectionPool.getInstance().getConnection();
					String sql="INSERT INTO managerserverstartrecord(date, location, cost, name) VALUES (?,?,?,?)";
					pst=conn.prepareStatement(sql);
					pst.setLong(1, now+Math.round(Double.parseDouble(ss[0])*60*60*1000));
					pst.setInt(2, Integer.parseInt(ss[1]));
					pst.setInt(3, Integer.parseInt(ss[2]));
					pst.setString(4, ss[3]);				
					pst.executeUpdate();
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally{
					if(pst!=null){
						try {pst.close();}
						catch (SQLException e) {e.printStackTrace();}
					}
					ConnectionPool.getInstance().returnConnection(conn);
				}
			}
			reader.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}			
	}
	
	
	public static void insertWorkflowRecord(){
		try {
			String files="D:/data/workflows.txt";
//			String files[]={"D:/0622pid","D:/0627pid20-"};
			String line=null;
			long now=System.currentTimeMillis()-142*60*60*100;
			BufferedReader reader = new BufferedReader(new FileReader(files));
			while((line=reader.readLine())!=null){
				String[] ss=line.split(" ");
				Connection conn=null;
				PreparedStatement pst=null;
				try {
					conn=ConnectionPool.getInstance().getConnection();
					String sql="INSERT INTO processlogs(ProcessID, log, starttime,endtime, idletime, workflowid, server, location) VALUES (?,?,?,?,?,?,?,?)";
					pst=conn.prepareStatement(sql);
					pst.setString(1, now+ss[1]);
					pst.setString(2, "");
					pst.setLong(3, now+Math.round(Double.parseDouble(ss[0])*60*60*1000)-Math.round(Double.parseDouble(ss[2])*1000));
					pst.setLong(4, now+Math.round(Double.parseDouble(ss[0])*60*60*1000));
					pst.setLong(5, 0);
					pst.setInt(6, Integer.parseInt(ss[3]));
					pst.setString(7, "");
					if(System.currentTimeMillis()%4==0)pst.setInt(8, ServerAgent.LOC_LOCAL);
					else pst.setInt(8, ServerAgent.LOC_AWSEC2);
					int rs=pst.executeUpdate();
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally{
					if(pst!=null){
						try {pst.close();}
						catch (SQLException e) {e.printStackTrace();}
					}
					ConnectionPool.getInstance().returnConnection(conn);
				}
			}
			reader.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}			
	}
}
