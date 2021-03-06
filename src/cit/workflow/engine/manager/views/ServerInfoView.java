package cit.workflow.engine.manager.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.TreeElement;
import cit.workflow.engine.manager.util.ImageFactory;

import org.eclipse.swt.widgets.Table;

public class ServerInfoView extends ViewPart {

	public static final String ID = "cit.workflow.engine.manager.ServerInfoView"; //$NON-NLS-1$
	private TableViewer table;
	private List servers;
	public static final int HOST=0;
	public static final int PORT=1;
	public static final int STATE=2;
	public static final int LOCATION=3;
	public static final int CAPACITY=4;
	public static final int RUNNINGINSTANCES=5;
	
	
	public static final String[] COLUMN_NAME={"Server","Port","State","Location","Capacity","Instances"};
	
	public ServerInfoView() {
	}
	
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
//		Composite container = new Composite(parent, SWT.NONE);
		
		table=new TableViewer(parent,SWT.FULL_SELECTION);
		for(int i=0;i<COLUMN_NAME.length;i++){
			new TableColumn(table.getTable(),SWT.LEFT).setText(COLUMN_NAME[i]);
		}
		table.getTable().setHeaderVisible(true);
		table.getTable().setLinesVisible(true);
		table.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement){
				if(inputElement instanceof List)
					return ((List)inputElement).toArray();
				return null;
			}
			public void dispose(){}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {}
			
		});
		table.setLabelProvider(new ITableLabelProvider(){

			@Override
			public void addListener(ILabelProviderListener listener) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				if(columnIndex==STATE){
					ServerAgent server = (ServerAgent) element;
					return server.getImage();
				}
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				ServerAgent server=(ServerAgent)element;
				ServiceAgent service=server.getEngineSerivce();
				if(columnIndex==HOST){
					if(server.getURL()==null)return "";
					return server.getURL().getHost();
				}
				else if(columnIndex==PORT){
					if(server.getURL()==null)return "";
					return server.getURL().getPort()+"";
				}
				else if(columnIndex==STATE)
					return server.getStateString();
				else if(columnIndex==LOCATION) return server.getLocationString();
				else if(columnIndex==CAPACITY){
					if(service==null)return "";
					else return service.getCapacity()+"";
				}
				else if(columnIndex==RUNNINGINSTANCES){
					if(service==null)return "";
					return service.getRunningWorkflows()+"";
				}
				return null;
			}
			
		});

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
	
	public void setData(List servers){
		table.setInput(servers);
		
		for(int i=0;i<COLUMN_NAME.length;i++){
			table.getTable().getColumn(i).pack();
		}
	}

}
