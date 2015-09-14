package cit.workflow.engine.manager.views;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServerList.ServerPair;
import cit.workflow.engine.manager.dialog.ProcessLogDialog;
import cit.workflow.engine.manager.util.ConnectionPool;

public class TypeServerNumView extends ViewPart{
	public static final String ID = "cit.workflow.engine.manager.TypeServerNumView"; //$NON-NLS-1$
	private static final int FILL_ALL=GridData.GRAB_HORIZONTAL+GridData.HORIZONTAL_ALIGN_FILL+GridData.GRAB_VERTICAL+GridData.VERTICAL_ALIGN_FILL;
	private TimeSeries serverTS;
	private JFreeChart chart;
	
	public TypeServerNumView(){
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite container = new Composite(parent,SWT.NULL);
		final JFreeChart serverChart = createServerCountChart();
		container.setLayout(new GridLayout(1, false));
		ChartComposite serNumFrame = new ChartComposite(container, SWT.NONE, serverChart, true);
		serNumFrame.setLayoutData(new GridData(FILL_ALL));
        serNumFrame.setDisplayToolTips(true);
        serNumFrame.setHorizontalAxisTrace(false);
        serNumFrame.setVerticalAxisTrace(false);
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose(){
		super.dispose();
	}
	
	
	private JFreeChart createServerCountChart() {

    	serverTS = new TimeSeries("Server Number");
    	TimeSeriesCollection dataset=new TimeSeriesCollection(serverTS);
        chart = ChartFactory.createTimeSeriesChart(
            "Server Number",  // title
            "Time",             // x-axis label
            "Number",   // y-axis label
            dataset,            // data
            false,              // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis yAxis=(NumberAxis)plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setAutoRangeIncludesZero(true);
        
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
//        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYStepRenderer renderer = new XYStepRenderer();
        renderer.setBaseShapesVisible(true);
//        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
//        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        renderer.setDefaultEntityRadius(6);
        plot.setRenderer(renderer);

        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
        
        return chart;
    }
	
	
	public void setData(int serverLocation){
		if(serverLocation==-1)chart.setTitle("Server Number");
		else chart.setTitle("Server Number on "+ServerAgent.LOCATIONSTRING[serverLocation]);
		
		String sqlword="";
		if(serverLocation==ServerAgent.LOC_AWSEC2)sqlword="aws";
		else if(serverLocation==ServerAgent.LOC_ALIYUN)sqlword="aliyun";
		else return;
		
		serverTS.clear();
		if(serverLocation==ServerAgent.LOC_AWSEC2){
			Connection conn=null;
			try {
				conn = ConnectionPool.getInstance().getConnection();
				long startTime=System.currentTimeMillis()-24*60*60*1000;
				String statement=String.format("select date,%s from managerservernumberrecord where date>? order by date", sqlword);
				PreparedStatement pst=conn.prepareStatement(statement);
				pst.setLong(1, startTime);
				ResultSet rs=pst.executeQuery();
				long date;
				int num=0;
				while(rs.next()){
					date=rs.getLong(1);
					num=rs.getInt(2);
					serverTS.add(new Second(new Date(date)),num);
				}
				num=0;
				for(ServerAgent server:ServerList.getServers()){
					if(server.getLocation()==serverLocation){
						num++;
					}
				}
				//add current num
				if(serverTS.getItemCount()==0){
					serverTS.add(new Second(new Date(startTime)),num);
				}
				serverTS.add(new Second(new Date()),num);
				pst.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}finally{
				ConnectionPool.getInstance().returnConnection(conn);
			}
		}
		else if(serverLocation==ServerAgent.LOC_ALIYUN){			
			Connection conn=null;
			try {
				conn = ConnectionPool.getInstance().getConnection();
				long startTime=System.currentTimeMillis()-24*60*60*1000;
				String statement=String.format("select date,%s from managerservernumberrecord where date>? order by date", sqlword);
				PreparedStatement pst=conn.prepareStatement(statement);
				pst.setLong(1, startTime);
				ResultSet rs=pst.executeQuery();
				long date;
				int num=0;
				while(rs.next()){
					date=rs.getLong(1);
					num=rs.getInt(2);
					serverTS.add(new Second(new Date(date)),num);
				}
				num=0;
				for(ServerAgent server:ServerList.getServers()){
					if(server.getLocation()==serverLocation){
						num++;
					}
				}
				//add current num
				if(serverTS.getItemCount()==0){
					serverTS.add(new Second(new Date(startTime)),num);
				}
				serverTS.add(new Second(new Date()),num);
				pst.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}finally{
				ConnectionPool.getInstance().returnConnection(conn);
			}
		}
	}
	
	public static TimeSeries list2ts(List<ServerPair> list) {
		TimeSeries ts = new TimeSeries("");
		try {
			for (ServerPair serverPair : list) {
				ts.add(new Second(new Date(serverPair.time)), serverPair.number);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ts;
	}
}
