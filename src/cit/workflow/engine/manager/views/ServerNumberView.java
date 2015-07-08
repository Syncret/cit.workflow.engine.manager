package cit.workflow.engine.manager.views;

import java.awt.Color;
import java.text.SimpleDateFormat;
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
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServerList.ServerPair;

public class ServerNumberView extends ViewPart{
	public static final String ID = "cit.workflow.engine.manager.ServerNumberView"; //$NON-NLS-1$
	private static final int FILL_ALL=GridData.GRAB_HORIZONTAL+GridData.HORIZONTAL_ALIGN_FILL+GridData.GRAB_VERTICAL+GridData.VERTICAL_ALIGN_FILL;
	private TimeSeries serverTS;
	private RefreshChartThread refreshThread;
	
	public ServerNumberView(){
		refreshThread=new RefreshChartThread(Display.getCurrent());
		refreshThread.setDaemon(true);
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
        setData();
        refreshThread.start();
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose(){
		super.dispose();
		refreshThread.setRun(false);
	}
	
	
	
	private JFreeChart createServerCountChart() {

    	serverTS = new TimeSeries("Server Number");
    	TimeSeriesCollection dataset=new TimeSeriesCollection(serverTS);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
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
        
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
//        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
//        XYItemRenderer r = plot.getRenderer();
//        if (r instanceof XYLineAndShapeRenderer) {
//            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
//            renderer.setBaseShapesVisible(true);
//            renderer.setBaseShapesFilled(true);
//        }
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
        
        return chart;
    }
	
	public void setData(){
		serverTS.addAndOrUpdate(list2ts(ServerList.ServerNumberRecord));
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
	
	private class RefreshChartThread extends Thread{
		private Display display;
		private boolean run;
		private long inteval=1000;
		public boolean isRun() {
			return run;
		}

		public void setRun(boolean run) {
			this.run = run;
		}

		public RefreshChartThread(Display display){
			this.display=display;
			this.setDaemon(true);
			run=true;
		}
		
		public void run(){
			while(run&&!display.isDisposed()){				
				try {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							serverTS.add(new Second(),ServerList.getServers().size());
						}
					});
				} finally {
				}
				try {
					Thread.sleep(inteval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
