package cit.workflow.engine.manager.views;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Table;

import cit.workflow.engine.manager.data.WorkflowInstanceAgent;

public class WorkflowInstancesView extends ViewPart {

	public static final String ID = "cit.workflow.engine.manager.WorkflowInstancesView"; //$NON-NLS-1$
	private Table table;
	private String[] columns={"","Name","ID","State","Progress","Start Time","End Time"};
	private static final int TABLEINDEX_ICON=0;
	private static final int TABLEINDEX_NAME=1;
	private static final int TABLEINDEX_ID=2;
	private static final int TABLEINDEX_STATE=3;
	private static final int TABLEINDEX_PROGRESS=4;
	private static final int TABLEINDEX_STARTTIME=5;
	private static final int TABLEINDEX_ENDTIME=6;
	private Composite parent;
	public WorkflowInstancesView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent=new Composite(parent,SWT.None);
		

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
	
	public void setData(List<WorkflowInstanceAgent> workflows){
		if(table!=null) table.dispose();
		
		table = new Table(parent,  SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		
		for(int i=0;i<columns.length;i++){
			TableColumn column=new TableColumn(table,SWT.NONE);
			column.setMoveable(true);
			table.getColumn(i).setText(columns[i]);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(WorkflowInstanceAgent workflow:workflows){
			//set text
			TableItem item=new TableItem(table,SWT.NONE);
			String[] text=new String[columns.length];
			text[TABLEINDEX_ICON]="";
			item.setImage(TABLEINDEX_ICON, workflow.getImage());
			text[TABLEINDEX_NAME]=workflow.getName();
			text[TABLEINDEX_ID]=Integer.toString(workflow.getWorkflowID());
			text[TABLEINDEX_STATE]=workflow.getStateString();
			double progress=workflow.getProgress();
			text[TABLEINDEX_PROGRESS]=Double.toString(progress);
			Date date=workflow.getStartTime();
			if(date!=null)text[TABLEINDEX_STARTTIME]=sdf.format(date);
			else text[TABLEINDEX_STARTTIME]="";
			date=workflow.getEndTime();
			if(date!=null)text[TABLEINDEX_ENDTIME]=sdf.format(date);
			else text[TABLEINDEX_ENDTIME]="";
			item.setText(text);
			//insert progress bar
			ProgressBar bar=new ProgressBar(table,SWT.NONE);
			bar.setMaximum(100);
			bar.setMinimum(0);
			bar.setSelection((int)(progress*100));
//			if(progress>1||workflow.getState()==WorkflowInstanceAgent.STATE_STOPPED)bar.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			TableEditor editor=new TableEditor(table);
			editor.grabHorizontal=true;
			editor.grabVertical=true;
			editor.setEditor(bar,item,TABLEINDEX_PROGRESS);
		}
		for(int i=0;i<table.getColumnCount();i++){
			if(i!=TABLEINDEX_PROGRESS) table.getColumn(i).pack();
		}
		table.getColumn(TABLEINDEX_PROGRESS).setWidth(100);
		table.pack();
	}
}
