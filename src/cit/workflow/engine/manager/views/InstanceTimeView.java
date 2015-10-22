package cit.workflow.engine.manager.views;

import java.awt.Color;
import java.awt.MenuBar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.TreeElement;
import cit.workflow.engine.manager.data.ServerList.ServerPair;
import cit.workflow.engine.manager.util.ConnectionPool;
import cit.workflow.engine.manager.util.ImageFactory;

public class InstanceTimeView extends ViewPart{
	public static final String ID = "cit.workflow.engine.manager.instancetimeview"; 
	private static final int FILL_ALL=GridData.GRAB_HORIZONTAL+GridData.HORIZONTAL_ALIGN_FILL+GridData.GRAB_VERTICAL+GridData.VERTICAL_ALIGN_FILL;

	public static final int MODE_ALL=0;
	public static final int MODE_LOCATION=1;
	public static final int MODE_SERVER=2;
	
	private TimeSeries timeSeries;
	private TimeSeriesCollection dataset;
	private JFreeChart chart;
	private IMenuManager menuBar;
	private MenuManager selectWorkflowMenu;
	private ArrayList<Integer> workflowLists=new ArrayList<>();
	
	public InstanceTimeView(){
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		menuBar=getViewSite().getActionBars().getMenuManager();
		selectWorkflowMenu=new MenuManager("Select Workflow Model",null,null);
		selectWorkflowMenu.add(new SelectWorkflowAction(-1));
		menuBar.add(selectWorkflowMenu);
		 
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
    	dataset=new TimeSeriesCollection();
        chart = ChartFactory.createTimeSeriesChart(
            "Instance Execution Time",  // title
            "Time",             // x-axis label
            "Cost Time(s)",   // y-axis label
            dataset,            // data
            true,              // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        //do not show the line
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
		this.setData(MODE_LOCATION,serverLocation,"");
	}
	
	public void setData(String serverName){
		this.setData(MODE_SERVER,0,serverName);
	}
	
	public void setData(int mode, int serverLocation, String serverName){
		String title="";
		if(mode==MODE_ALL)title="Instance Execution Time on All Servers";
		else if(mode==MODE_LOCATION) title="Instance Execution Time on "+ServerAgent.LOCATIONSTRING[serverLocation];
		else if(mode==MODE_SERVER) title="Instance Execution Time on "+serverName;
		chart.setTitle(title);
		
		dataset.removeAllSeries();		
		selectWorkflowMenu.removeAll();
		selectWorkflowMenu.add(new SelectWorkflowAction(-1));
		workflowLists.clear();
		
		Connection conn=null;
		try {
			conn = ConnectionPool.getInstance().getConnection();
			long startTime=System.currentTimeMillis()-70*24*60*60*1000;
			
			PreparedStatement pst=null;
			String statement="";
			if(mode==MODE_ALL) {
				statement="select processid,starttime,endtime,workflowid from processlogs where starttime>? order by starttime";
				pst=conn.prepareStatement(statement);
				pst.setLong(1, startTime);
			}
			else if(mode==MODE_LOCATION) {
				statement="select processid,starttime,endtime,workflowid from processlogs where starttime>? and location=? order by starttime";
				pst=conn.prepareStatement(statement);
				pst.setLong(1, startTime);
				pst.setInt(2, serverLocation);						
			}
			else if(mode==MODE_SERVER){
				statement="select processid,starttime,endtime,workflowid from processlogs where starttime>? and server=? order by starttime";
				pst=conn.prepareStatement(statement);
				pst.setLong(1, startTime);
				pst.setString(2, serverName);
			}
			
			ResultSet rs=pst.executeQuery();
			long starttime,endtime;
			int workflowid;
			HashMap<Integer, TimeSeries> map=new HashMap<Integer, TimeSeries>();
			while(rs.next()){
				starttime=rs.getLong(2);
				endtime=rs.getLong(3);
				workflowid=rs.getInt(4);
				TimeSeries ts=map.get(workflowid);
				if(ts==null){
					ts=new TimeSeries("Workflow"+workflowid);
					map.put(workflowid, ts);
				}
				ts.addOrUpdate(new Second(new Date(starttime)),(int)((endtime-starttime)/1000));
			}
			pst.close();
			for(int id:map.keySet()){
				TimeSeries ts=map.get(id);
				dataset.addSeries(ts);
				workflowLists.add(id);
				selectWorkflowMenu.add(new SelectWorkflowAction(id));
			}
			menuBar.add(selectWorkflowMenu);
			SelectVisibleWorkflow(-1);//show all workflows
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally{
			ConnectionPool.getInstance().returnConnection(conn);
		}
	}
	
	private void SelectVisibleWorkflow(int workflowID){
		XYPlot plot = (XYPlot) chart.getPlot();
		XYItemRenderer r = plot.getRenderer();
		for(int i=0;i<workflowLists.size();i++){
			if(workflowLists.get(i)==workflowID||workflowID<0)
				r.setSeriesVisible(i,true);
			else
				r.setSeriesVisible(i,false);
		}
	}

	public class SelectWorkflowAction extends Action implements IWorkbenchAction{
		private int workflowID;
		public SelectWorkflowAction(int workflowID){
			super();
			this.workflowID=workflowID;
			if(workflowID==-1) this.setText("All");
			else this.setText("Workflow"+workflowID);
		}
		@Override
		public void run(){
			SelectVisibleWorkflow(workflowID);
		}
		
		@Override
		public void dispose(){
		}
	}
}