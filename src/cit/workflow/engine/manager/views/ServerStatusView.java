package cit.workflow.engine.manager.views;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

public class ServerStatusView extends ViewPart {

	public static final String ID = "cit.workflow.engine.manager.ServerStatusView"; //$NON-NLS-1$
	private static final int FILL_ALL=GridData.GRAB_HORIZONTAL+GridData.HORIZONTAL_ALIGN_FILL+GridData.GRAB_VERTICAL+GridData.VERTICAL_ALIGN_FILL;
	private Label label1;
	private TimeSeries cpuTS;
	private TimeSeries memTS;

	public ServerStatusView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite container = new Composite(parent,SWT.NULL);
		final JFreeChart cpuChart = createCPUChart();
		final JFreeChart memChart = createMemChart();
		container.setLayout(new GridLayout(1, true));
		
		label1 = new Label(container, SWT.NONE);
		label1.setText("Performance Chart");
		ChartComposite cpuFrame = new ChartComposite(container, SWT.NONE, cpuChart, true);
		cpuFrame.setLayoutData(new GridData(FILL_ALL));
        cpuFrame.setDisplayToolTips(true);
        cpuFrame.setHorizontalAxisTrace(false);
        cpuFrame.setVerticalAxisTrace(false);
        
        ChartComposite memFrame = new ChartComposite(container, SWT.NONE, memChart, true);
        memFrame.setLayoutData(new GridData(FILL_ALL));
        memFrame.setDisplayToolTips(true);
        memFrame.setHorizontalAxisTrace(false);
        memFrame.setVerticalAxisTrace(false);
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void setLabel(String server){
		label1.setText("Performance chart of "+server);
	}
	
	public void setData(LinkedList<Object[]> cpuPerfList,LinkedList<Object[]> memPerfList){
		cpuTS.addAndOrUpdate(list2ts(cpuPerfList));
		memTS.addAndOrUpdate(list2ts(memPerfList));
	}
	
	public static TimeSeries list2ts(LinkedList<Object[]> perfList) {
		TimeSeries ts = new TimeSeries("");
		try {
			for (Object[] perf : perfList) {
				ts.add(new Second(new Date((long)perf[0])), (double) perf[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ts;
	}
	
	private JFreeChart createCPUChart() {

    	cpuTS = new TimeSeries("CPU usage");
    	TimeSeriesCollection dataset=new TimeSeriesCollection(cpuTS);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "CPU usage",  // title
            "Time",             // x-axis label
            "Ratio/%",   // y-axis label
            dataset,            // data
            false,               // create legend?
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
        ValueAxis rangeAxis=plot.getRangeAxis();
		rangeAxis.setRange(0, 100);
        
        return chart;

    }
	
	private JFreeChart createMemChart() {

    	memTS = new TimeSeries("Memory usage");
    	TimeSeriesCollection dataset=new TimeSeriesCollection(memTS);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Memory usage",  // title
            "Time",             // x-axis label
            "Ratio/%",   // y-axis label
            dataset,            // data
            false,               // create legend?
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
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
        ValueAxis rangeAxis=plot.getRangeAxis();
		rangeAxis.setRange(0, 100);
        
        return chart;

    }
    
 
}
