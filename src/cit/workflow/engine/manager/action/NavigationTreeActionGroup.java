package cit.workflow.engine.manager.action;

import java.net.MalformedURLException;
import java.net.URL;

import javax.security.auth.Refreshable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.Constants;
import cit.workflow.engine.manager.Activator;
import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.data.ServerList;
import cit.workflow.engine.manager.data.ServiceAgent;
import cit.workflow.engine.manager.data.TreeElement;
import cit.workflow.engine.manager.image.ImageFactory;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.webservice.RemoteDeployer;

public class NavigationTreeActionGroup extends ActionGroup {
	private TreeViewer tv;

	public NavigationTreeActionGroup(TreeViewer treeViewer) {
		this.tv = treeViewer;
	}

	// 生成菜单Menu，并将两个Action传入
	public void fillContextMenu(IMenuManager mgr) {
		// 加入两个Action对象到菜单管理器
		MenuManager menuManager = (MenuManager) mgr; // 类型转换
//		menuManager.add(new DeployServiceAction());
//		menuManager.add(new RefreshAction());
//		menuManager.add(new ExpandAction());
//		menuManager.add(new CollapseAction());
//		menuManager.add(new AddEntryAction());
//		menuManager.add(new RemoveEntryAction());
//		menuManager.add(new ModifyEntryAction());
		// 生成Menu并挂在树Tree上
		Tree tree = tv.getTree();
		Menu menu = menuManager.createContextMenu(tree);
		
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				TreeElement element=getSelTreeEntry();
				if(element instanceof ServerList){
					manager.add(new AddServerAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
				}
				else if(element instanceof ServerAgent){
					MenuManager deployMenuManager=new MenuManager("Deply Service", ImageFactory.getImageDescriptor(ImageFactory.ADD), null);
					deployMenuManager.add(new DeployServiceAction(DeployServiceAction.ENGINESERVICE));
					deployMenuManager.add(new DeployServiceAction(DeployServiceAction.TESTSERVICE));
					deployMenuManager.add(new Separator());
					deployMenuManager.add(new DeployServiceAction(DeployServiceAction.OTHERSERVICE));
					manager.add(deployMenuManager);
				}
				else if(element instanceof ServiceAgent){
					
				}
			}
		});
		mgr.setRemoveAllWhenShown(true);
		tree.setMenu(menu);
	}

//	// “打开”的Action类
//	private class OpenAction extends Action {
//		public OpenAction() {
//			setText("打开");
//		}
//
//		public void run() {
//			ITreeEntry entry = getSelTreeEntry();
//			if (entry != null)
//				MessageDialog.openInformation(null, null, entry.getName());
//		}
//	}
	public class DeployServiceAction extends Action implements IWorkbenchAction{
		public static final int OTHERSERVICE=0;
		public static final int ENGINESERVICE=1;
		public static final int TESTSERVICE=2;
		
		private final String SERVICENAME[]={"other","Engine Service","Test Service"};
		private final String DEPLOYPATH[]={"other","workflow","test"};
		private static final String ENGINESERVICEPATH="workflow.war";
		private static final String TESTSERVICEPATH="test.war";

		private int service;
		
		public DeployServiceAction(int service){
			super();
			setText(SERVICENAME[service]);
			this.service=service;
		}
		
		public void run(){
			//check node valid
			TreeElement element=getSelTreeEntry();
			if(element==null||!(element instanceof ServerAgent)) return;
			ServerAgent serverAgent = (ServerAgent) element;
			
			//get war path
			String warPath=null;
			switch (service) {
			case 0:
				FileDialog dialog=new FileDialog(Display.getCurrent().getActiveShell(),SWT.OPEN);
				dialog.setFilterPath(Constants.MANAGER_PATH+RemoteDeployer.SCRIPTPATH);
				dialog.setFilterExtensions(new String[]{"*.war","*.*"});
				dialog.setFilterNames(new String[]{"war Files(*.war)","All Files(*.*)"});
				warPath=dialog.open();
				break;
			case ENGINESERVICE:
				warPath=ENGINESERVICEPATH;
				break;
			case TESTSERVICE:
				warPath=TESTSERVICEPATH;
				break;
			default:
				break;
			}
			if(warPath==null)return;
			
			boolean confirm=MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm Deployment", 
					"Are you sure to deploy "+warPath+" to "+serverAgent.getUrl().toString()+"?");
			if(!confirm)return;
			RemoteDeployer deployer = new RemoteDeployer();
			boolean result = deployer.deploy(serverAgent.getUrl(),DEPLOYPATH[service], warPath);
			if (result) {
				ServiceAgent serviceAgent = new ServiceAgent(serverAgent,"test service", ServiceAgent.STATE_RUNNING);
				serverAgent.addService(serviceAgent);
				serverAgent.setState(ServerAgent.STATE_RUNNING);
				refreshTree();
			}
		}
		
		
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		

	}
//
//	// 刷新的Action类
//	private class RefreshAction extends Action {
//		public RefreshAction() {
//			setText("刷新");
//		}
//
//		// 如果在删除结点时，不同时从数据模型里删除结点。则执行本刷新后，该结点又会从
//		// 数据模型中取出并显示在界面上
//		public void run() {
//			tv.refresh();
//		}
//	}
//
//	// 展开当前结点的Action类
//	private class ExpandAction extends Action {
//		public ExpandAction() {
//			setText("展开");
//		}
//
//		public void run() {
//			ITreeEntry entry = getSelTreeEntry();
//			if (entry != null)
//				tv.expandToLevel(entry, 1); // 参数2是展开的层数
//		}
//	}
//
//	// 收缩当前结点的Action类
//	private class CollapseAction extends Action {
//		public CollapseAction() {
//			setText("收缩");
//		}
//
//		public void run() {
//			ITreeEntry entry = getSelTreeEntry();
//			if (entry != null)
//				tv.collapseToLevel(entry, -1); // 参数2是收缩的层数，-1指收缩所有子结点
//		}
//	}
//
//	// 给当前结点增加一个子结点的Action类
//	private class AddEntryAction extends Action {
//		public AddEntryAction() {
//			setText("增加");
//		}
//
//		public void run() {
//			ITreeEntry entry = getSelTreeEntry();
//			if (entry == null || entry instanceof PeopleEntity)
//				return;// 不能给“人”结点增加子结点
//			InputDialog dialog = new InputDialog(null, "增加子结点", "输入名称：", "a",
//					null);
//			if (dialog.open() == InputDialog.OK) {// 如果单击OK按钮
//				String entryName = dialog.getValue(); // 得到Dialog输入值
//				// 根据单击结点的不同类型生成相应的子结点
//				ITreeEntry newEntry = null;
//				if (entry instanceof CountryEntity)
//					newEntry = new CityEntity(entryName);
//				else if (entry instanceof CityEntity)
//					newEntry = new PeopleEntity(entryName);
//				entry.getChildren().add(newEntry); // 新结点增加到数据模型中
//				tv.add(entry, newEntry);// newEntry结点增加到entry之下，或用tv.refresh(entry,true)也行
//				if (!tv.getExpandedState(entry)) // 如果entry是未展开结点则展开它
//					tv.expandToLevel(entry, 1);
//			}
//		}
//	}
//
//	// 删除结点的Action类
//	private class RemoveEntryAction extends Action {
//		public RemoveEntryAction() {
//			setText("删除");
//		}
//
//		public void run() {
//			ITreeEntry entry = getSelTreeEntry();
//			if (entry == null)
//				return;
//			// 从数据模型里删除结点
//			List list = (List) tv.getInput();
//			remove(list, entry);
//			// 从界面删除结点，放在从模型删除之后，
//			// 否则在删除某个结点的最后一个子结点时会出问题
//			tv.remove(entry);
//		}
//
//		private void remove(List list, ITreeEntry entry) {
//			if (list == null)
//				return;
//			for (Iterator it = list.iterator(); it.hasNext();) {
//				ITreeEntry o = (ITreeEntry) it.next();
//				if (o.getName().equals(entry.getName())) {
//					it.remove();
//					return;
//				} else {
//					remove(o.getChildren(), entry);
//				}
//			}
//		}
//	}
//
//	// 修改结点名称的Action类
//	private class ModifyEntryAction extends Action {
//		public ModifyEntryAction() {
//			setText("修改");
//		}
//
//		public void run() {
//			ITreeEntry entry = getSelTreeEntry();
//			if (entry == null)
//				return;
//			InputDialog dialog = new InputDialog(null, "修改结点", "输入新名称",
//					entry.getName(), null);
//			if (dialog.open() == InputDialog.OK) {
//				String entryName = dialog.getValue();
//				entry.setName(entryName);// 修改数据模型
//				tv.refresh(entry); // 刷新结点,等效于tv.update(entry,null);
//			}
//		}
//	}

	// 为共用而自定义的方法：取得当前选择的结点
	private TreeElement getSelTreeEntry() {
		IStructuredSelection selection = (IStructuredSelection) tv
				.getSelection();
		return (TreeElement) (selection.getFirstElement());
	}
	
	private void refreshTree(){
		tv.refresh();
	}
}