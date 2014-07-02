package cit.workflow.engine.manager.dialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import cit.workflow.engine.manager.util.ConnectionPool;

import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

public class AssignWorkflowDialog extends Dialog {
	private static final int MAXINSTACES=500;
	Connection conn = null;
	private int result=-1;
	private int selectWorkflowID=-1;
	private int selectNum=0;
	private ListViewer listViewer;
	private TableViewer tableViewer;
	private Spinner spinner;
	private String[] COLUMN_NAME={"ID","Name","Description"};
	private int CI_ID=0;
	private int CI_NAME=1;
	private int CI_DESCRIPTION=2;
	private AssignWorkflowDialog self=null;
	private Shell parentShell;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AssignWorkflowDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell=parentShell;
		setShellStyle(SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		self=this;
		try {
			conn = ConnectionPool.getInstance().getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Select Workflow");
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		FillLayout fl_container = new FillLayout(SWT.HORIZONTAL);
		fl_container.marginWidth = 10;
		fl_container.marginHeight = 5;
		container.setLayout(fl_container);
	
		SashForm sashForm = new SashForm(container, SWT.NONE);
		//left part
		Composite leftComposite = new Composite(sashForm, SWT.NONE);
		Label lblPackages = new Label(leftComposite, SWT.NONE);
		lblPackages.setText("Packages:");
		listViewer = new ListViewer(leftComposite,  SWT.V_SCROLL|SWT.BORDER);
		//right part
		Composite rightComposite=new Composite(sashForm, SWT.NONE);
		Label lblWorkflow = new Label(rightComposite, SWT.NONE);
		lblWorkflow.setText("Workflow:");
		tableViewer = new TableViewer(rightComposite,  SWT.FULL_SELECTION|SWT.BORDER);
		Label lblSpinner=new Label(rightComposite,SWT.NONE);
		lblSpinner.setText("Assign Instance Numbers:");
		spinner = new Spinner(rightComposite, SWT.BORDER);
		spinner.setSelection(1);
		spinner.setMaximum(MAXINSTACES);
		
		//listViewer
		
		listViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
			@Override
			public void dispose() {}
			@Override
			public Object[] getElements(Object inputElement) {
				return ((ArrayList<PackageD>)inputElement).toArray();
			}
		});
		listViewer.setLabelProvider(new ILabelProvider() {
			@Override
			public void removeListener(ILabelProviderListener listener) {}
			@Override
			public boolean isLabelProperty(Object element, String property) {return false;}
			@Override
			public void dispose() {}
			@Override
			public void addListener(ILabelProviderListener listener) {}
			@Override
			public String getText(Object element) {
				return ((PackageD)element).name;
			}
			@Override
			public Image getImage(Object element) {	return null;}
		});
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				PackageD pcg=(PackageD)((IStructuredSelection)event.getSelection()).getFirstElement();
				if(pcg==null) return;
				try {
					String statement= "select WorkflowID,WorkflowName,Description from WorkflowInformation "
							+ "where PackageID="+pcg.ID+" order by WorkflowID asc";
					PreparedStatement pst= conn.prepareStatement(statement);
					ResultSet rs = pst.executeQuery();
					ArrayList<WorkflowD> workflows=new ArrayList<>();
					while(rs.next()){
						workflows.add(new WorkflowD(rs.getInt(1), rs.getString(2), rs.getString(3)));
					}
					tableViewer.setInput(workflows);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		
		//tableViewer
		for(int i=0;i<COLUMN_NAME.length;i++){
			new TableColumn(tableViewer.getTable(),SWT.LEFT).setText(COLUMN_NAME[i]);
		}
		tableViewer.getTable().getColumn(CI_ID).setWidth(30);
		tableViewer.getTable().getColumn(CI_NAME).setWidth(130);
		tableViewer.getTable().getColumn(CI_DESCRIPTION).setWidth(120);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement){
				return ((ArrayList<WorkflowD>)inputElement).toArray();
			}
			@Override
			public void dispose(){}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput,Object newInput) {}
		});
		tableViewer.setLabelProvider(new ITableLabelProvider(){
			@Override
			public void addListener(ILabelProviderListener listener) {}
			@Override
			public void dispose() {}
			@Override
			public boolean isLabelProperty(Object element, String property) {return false;}
			@Override
			public void removeListener(ILabelProviderListener listener) {}
			@Override
			public Image getColumnImage(Object element, int columnIndex) {return null;}
			@Override
			public String getColumnText(Object element, int columnIndex) {
				WorkflowD workflow=(WorkflowD)element;
				if(columnIndex==CI_ID)return workflow.ID+"";
				if(columnIndex==CI_NAME)return workflow.name;
				if(columnIndex==CI_DESCRIPTION)return workflow.description;
				return null;
			}
		});
		tableViewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				WorkflowD wkf=(WorkflowD)((IStructuredSelection)event.getSelection()).getFirstElement();
				result=wkf.ID;
				self.close();
			}
		});
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection=((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
				if(selection instanceof WorkflowD)selectWorkflowID=((WorkflowD)selection).ID;
			}
		});
		
		sashForm.setWeights(new int[] {30, 70});
		leftComposite.setLayout(new GridLayout(1, false));
		rightComposite.setLayout(new GridLayout(2, false));
		lblPackages.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
		listViewer.getList().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		lblWorkflow.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false,2,1));
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		lblSpinner.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
		spinner.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		
		
		
		try {
			String statement= "select PackageID, PackageName from PackageInformation order by PackageID asc";
			PreparedStatement pst= conn.prepareStatement(statement);
			ResultSet rs = pst.executeQuery();
			ArrayList<PackageD> packages=new ArrayList<>();
			while(rs.next()){
				packages.add(new PackageD(rs.getInt(1), rs.getString(2)));
			}
			listViewer.setInput(packages);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				IStructuredSelection selection=(IStructuredSelection)tableViewer.getSelection();
//				Object selectWorkflow=selection.getFirstElement();
//				if(selectWorkflow==null)MessageDialog.openInformation(parentShell, "No selection", "You have not chosen any workflow yet.");
//				else{
//					result=((WorkflowD)selectWorkflow).ID;
//					self.close();
//				}
				result=selectWorkflowID;
				
			}
		});
		Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result=-1;
			}
		});
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 375);
	}
	
	@Override
	public boolean close(){
		selectNum=spinner.getSelection();
		ConnectionPool.getInstance().returnConnection(conn);
		return super.close();
	}
	
	public int getSelectWorkflow(){
		return result;
	}
	
	public int getSelectNumber(){
		return selectNum;
	}
	
	private class PackageD{
		public int ID;
		public String name;
		public PackageD(int ID, String name) {
			super();
			this.ID = ID;
			this.name = name;
		}
	}
	private class WorkflowD{
		public int ID;
		public String name;
		public String description;
		public WorkflowD(int ID, String name, String description) {
			super();
			this.ID = ID;
			this.name = name;
			this.description = description;
		}
	}
}
