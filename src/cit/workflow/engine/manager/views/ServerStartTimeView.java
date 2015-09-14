package cit.workflow.engine.manager.views;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServerList.ServerPair;
import cit.workflow.engine.manager.util.ConnectionPool;

public class ServerStartTimeView extends ViewPart{
	public static final String ID = "cit.workflow.engine.manager.serverstarttimeview"; 
	private static final int FILL_ALL=GridData.GRAB_HORIZONTAL+GridData.HORIZONTAL_ALIGN_FILL+GridData.GRAB_VERTICAL+GridData.VERTICAL_ALIGN_FILL;
	private TimeSeries AWSts;
	private TimeSeries Alits;
	private JFreeChart chart;
	
	public ServerStartTimeView(){
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

    	AWSts = new TimeSeries("AWS EC2");
    	Alits=new TimeSeries("Aliyun ECS");
    	TimeSeriesCollection dataset=new TimeSeriesCollection();
    	dataset.addSeries(AWSts);
    	dataset.addSeries(Alits);
        chart = ChartFactory.createTimeSeriesChart(
            "Server Start Time",  // title
            "Time",             // x-axis label
            "Cost Time(s)",   // y-axis label
            dataset,            // data
            true,              // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis yaxis=(NumberAxis)plot.getRangeAxis();
        yaxis.setAutoRangeIncludesZero(true);
        ValueAxis xaxis=plot.getDomainAxis();
//        xaxis.setRange(System.currentTimeMillis()-7*24*60*60*1000,System.currentTimeMillis());
        
        
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
//        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setBaseLinesVisible(false);
        }
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd,HH"));
        
        return chart;
    }
	
	
	public void setData(int serverLocation){
		String title="";
		if(serverLocation==-1)title="Server Start Time";
		else title="Server Start Time on "+ServerAgent.LOCATIONSTRING[serverLocation];
		chart.setTitle(title);		
		
		AWSts.clear();
		Alits.clear();
		
		Connection conn=null;
		try {
			conn = ConnectionPool.getInstance().getConnection();
			long startTime=System.currentTimeMillis()-7*24*60*60*1000;
			//date, location, cost, name
			String statement="select * from managerserverstartrecord where date>? order by date";
			PreparedStatement pst=conn.prepareStatement(statement);
			pst.setLong(1, startTime);
			ResultSet rs=pst.executeQuery();
			long date;
			int location;
			int cost;
			while(rs.next()){
				date=rs.getLong(1);
				location=rs.getInt(2);
				cost=rs.getInt(3);

				if(location==ServerAgent.LOC_AWSEC2)
					AWSts.add(new Second(new Date(date)),cost);
				else if(location==ServerAgent.LOC_ALIYUN)
					Alits.add(new Second(new Date(date)),cost);
			}
			pst.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally{
			ConnectionPool.getInstance().returnConnection(conn);
		}
		if(serverLocation==ServerAgent.LOC_AWSEC2){
			Alits.clear();
		}
		if(serverLocation==ServerAgent.LOC_ALIYUN){
			AWSts.clear();
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