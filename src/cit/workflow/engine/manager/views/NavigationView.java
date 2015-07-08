package cit.workflow.engine.manager.views;

import java.awt.Window;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import cit.workflow.engine.manager.action.NavigationTreeActionGroup;
import cit.workflow.engine.manager.action.ViewInfoAction;
import cit.workflow.engine.manager.action.ViewInfoAction;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.TreeElement;
import cit.workflow.engine.manager.util.ImageFactory;

public class NavigationView extends ViewPart {
	public NavigationView() {
	}
	public static final String ID = "cit.workflow.engine.manager.navigationView";
	
	private TreeViewer viewer;
	private List<TreeElement> treeRoot;
	 

	private class TreeRoot implements TreeElement{

		private List<TreeElement> list;

		public TreeRoot() {
			list = new ArrayList<TreeElement>();
			list.add(ServerList.getInstance());
		}
		@Override
		public String getName() {
			return "Root";
		}

		@Override
		public boolean hasChildren() {
			return true;
		}

		@Override
		public List<TreeElement> getChildren() {
			return list;
		}

		@Override
		public TreeElement getParent() {
			return null;
		}

		@Override
		public Image getImage() {
			return null;
		}
		
	}
	

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
        
		public void dispose() {
		}
        
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}
        
		public Object getParent(Object child) {
			if(child instanceof ServerList) return treeRoot;
			else if (child instanceof TreeElement) {
				return ((TreeElement)child).getParent();
			}
			return null;
		}
        
		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeElement) {
				return ((TreeElement) parent).getChildren().toArray();
			}
			return new Object[0];
		}

        public boolean hasChildren(Object parent) {
			if (parent instanceof TreeElement)
				return ((TreeElement)parent).hasChildren();
			return false;
		}
	}
	
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			if(obj instanceof TreeElement)
				return ((TreeElement)obj).getName();
			else return null;
		}
		public Image getImage(Object obj){
			if(obj instanceof TreeElement){
				return ((TreeElement)obj).getImage();
			}
			else return ImageFactory.getImage(ImageFactory.TREEELEMENT);
		}
	}
    
    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		treeRoot=new ArrayList<TreeElement>();
    	treeRoot.add(ServerList.getInstance());
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(new TreeRoot());
		viewer.addDoubleClickListener(new ViewInfoAction(getSite().getWorkbenchWindow()));
		viewer.expandToLevel(2);
		NavigationTreeActionGroup actionGroup=new NavigationTreeActionGroup(viewer);
		actionGroup.fillContextMenu(new MenuManager());
		
	}
	
	public static void RefreshNavigationView(IWorkbenchWindow window){
		if(window==null)return;
		final NavigationView view=(NavigationView)window.getActivePage().findView(NavigationView.ID);
		if(view==null)return;
		window.getShell().getDisplay().asyncExec(new Runnable() {
		    public void run() {view.refresh();}
		}); 
	}

	
	public void refresh(){
		if(viewer!=null)viewer.refresh();
	}

	public TreeViewer getTree(){
		return viewer;
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}