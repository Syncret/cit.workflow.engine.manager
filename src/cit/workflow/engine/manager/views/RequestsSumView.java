package cit.workflow.engine.manager.views;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.util.ConnectionPool;

public class RequestsSumView extends ViewPart{
	public static final String ID = "cit.workflow.engine.manager.requestssumview"; //$NON-NLS-1$
	private static final int FILL_ALL=GridData.GRAB_HORIZONTAL+GridData.HORIZONTAL_ALIGN_FILL+GridData.GRAB_VERTICAL+GridData.VERTICAL_ALIGN_FILL;
	private TimeSeries serverTS;
	private JFreeChart chart;
	
	public RequestsSumView(){
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
	}
	
	@Override
	public void dispose(){
		super.dispose();
	}
	
	
	private JFreeChart createServerCountChart() {

    	serverTS = new TimeSeries("Requests Statics");
    	TimeSeriesCollection dataset=new TimeSeriesCollection(serverTS);
        chart = ChartFactory.createTimeSeriesChart(
            "Requests",  // title
            "Time",             // x-axis label
            "Request Number",   // y-axis label
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
	
	
	public void setData(){
		serverTS.clear();
		
		Connection conn=null;
		try {
			conn = ConnectionPool.getInstance().getConnection();
			long inteval=60*60*1000;
			long startTime=System.currentTimeMillis()-24*60*60*1000;
			startTime-=startTime%inteval;
			String statement="SELECT timegroup, COUNT(*) FROM "
				+"(SELECT starttime-starttime%(?) AS timegroup FROM processlogs WHERE starttime>? ) AS timegrouptable "
				+"GROUP BY timegroup ORDER BY timegroup";
			PreparedStatement pst=conn.prepareStatement(statement);
			pst.setLong(1, inteval);
			pst.setLong(2, startTime);
			ResultSet rs=pst.executeQuery();
			long date;
			long tTime=Long.MAX_VALUE;
			int num=0;
			while(rs.next()){
				date=rs.getLong(1);
				while(tTime<date){
					serverTS.add(new Second(new Date(tTime)),0);
					tTime+=inteval;
				}
				tTime=date+inteval;
				num=rs.getInt(2);
				serverTS.add(new Second(new Date(date)),num);
			}
			if(tTime==Long.MAX_VALUE){
				serverTS.add(new Second(new Date(startTime)),0);
				startTime=System.currentTimeMillis();
				serverTS.add(new Second(new Date(startTime)),0);
			}
			pst.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally{
			ConnectionPool.getInstance().returnConnection(conn);
		}
		
	}
}