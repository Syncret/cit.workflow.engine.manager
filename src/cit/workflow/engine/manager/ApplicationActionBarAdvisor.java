package cit.workflow.engine.manager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import cit.workflow.engine.manager.action.AddServerAction;
import cit.workflow.engine.manager.action.ControllerAction;
import cit.workflow.engine.manager.action.GenRequestAction;
import cit.workflow.engine.manager.action.OpenConsoleViewAction;
import cit.workflow.engine.manager.action.OpenInstanceTimeView;
import cit.workflow.engine.manager.action.OpenNavigationViewAction;
import cit.workflow.engine.manager.action.OpenRequestsSumAction;
import cit.workflow.engine.manager.action.OpenServerInfoViewAction;
import cit.workflow.engine.manager.action.OpenServerNumberViewAction;
import cit.workflow.engine.manager.action.OpenServerStartTimeViewAction;
import cit.workflow.engine.manager.action.OpenServerStatusViewAction;
import cit.workflow.engine.manager.action.OpenTypeServerNumViewAction;
import cit.workflow.engine.manager.action.OpenWorkflowInstancesViewAction;
import cit.workflow.engine.manager.action.NewCloudInstanceAction;
import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.views.View;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the
 * actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use them
    // in the fill methods.  This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;
    private IWorkbenchAction aboutAction;
    private IWorkbenchAction newWindowAction;
    private OpenViewAction openViewAction;
    private Action messagePopupAction;
    private IWorkbenchAction controllerAction;
    
    //actions under server menu
    private IWorkbenchAction addServerAction;
    private IWorkbenchAction genRequestAction;
    private IWorkbenchAction addAWSEC2InstanceAction;
    private IWorkbenchAction addAliyunInstanceAction;
    private IWorkbenchAction viewAllServerNumAction;
    private IWorkbenchAction viewAliServerNumAction;
    private IWorkbenchAction viewAWSServerNumAction;
    private IWorkbenchAction viewAllInstanceTimeAction;
    private IWorkbenchAction viewAliInstanceTimeAction;
    private IWorkbenchAction viewAWSInstanceTimeAction;
    private IWorkbenchAction viewAllStartTimeAction;
    private IWorkbenchAction viewAliStartTimeAction;
    private IWorkbenchAction viewAWSStartTimeAction;
    
    
    //actions under window menu
    private IWorkbenchAction openConsoleViewAction;
    private IWorkbenchAction openNavigationViewAction;
    private IWorkbenchAction openServerInfoViewAction;
    private IWorkbenchAction openServerStatusViewAction;
    private IWorkbenchAction openWorkflowInstancesViewAction;
    private IWorkbenchAction openServerNumberViewAction;
    private IWorkbenchAction openRequestsSumViewAction;
    private IContributionItem showViewListAction;
    private IWorkbenchAction perspectiveAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }
    
    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
        
        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        register(newWindowAction);
        
        perspectiveAction=ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(window);
        
        openViewAction = new OpenViewAction(window, "Open Another Message View", View.ID);
        register(openViewAction);
        
        messagePopupAction = new TestAction("test button", window);
        register(messagePopupAction);
        
        controllerAction=new ControllerAction(window);
        
        //under server menu
        addServerAction=new AddServerAction(window);
        addAWSEC2InstanceAction=new NewCloudInstanceAction(window,ServerAgent.LOC_AWSEC2);
        addAliyunInstanceAction=new NewCloudInstanceAction(window, ServerAgent.LOC_ALIYUN);
        viewAllServerNumAction=new OpenServerNumberViewAction(window, "All");
        viewAWSServerNumAction=new OpenTypeServerNumViewAction(window, ServerAgent.LOC_AWSEC2);
        viewAliServerNumAction=new OpenTypeServerNumViewAction(window, ServerAgent.LOC_ALIYUN);
        viewAllInstanceTimeAction=new OpenInstanceTimeView(window);
        viewAWSInstanceTimeAction=new OpenInstanceTimeView(window, ServerAgent.LOC_AWSEC2);
        viewAliInstanceTimeAction=new OpenInstanceTimeView(window, ServerAgent.LOC_ALIYUN);
        viewAllStartTimeAction=new OpenServerStartTimeViewAction(window, -1);
        viewAWSStartTimeAction=new OpenServerStartTimeViewAction(window, ServerAgent.LOC_AWSEC2);
        viewAliStartTimeAction=new OpenServerStartTimeViewAction(window, ServerAgent.LOC_ALIYUN);
        
        genRequestAction=new GenRequestAction(window);
        
//        register(addServerAction);
        
        //under window menu
        openConsoleViewAction=new OpenConsoleViewAction(window);
        openNavigationViewAction=new OpenNavigationViewAction(window);
        openServerInfoViewAction=new OpenServerInfoViewAction(window);
        openServerStatusViewAction=new OpenServerStatusViewAction(window);        
        openWorkflowInstancesViewAction=new OpenWorkflowInstancesViewAction(window);
        openServerNumberViewAction=new OpenServerNumberViewAction(window,"");
        openRequestsSumViewAction=new OpenRequestsSumAction(window);
//        register(openConsoleViewAction);
        showViewListAction=ContributionItemFactory.VIEWS_SHORTLIST.create(window);
        
    }
    
    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager serverMenu = new MenuManager("&Server");
        MenuManager staticsMenu=new MenuManager("Statics");
        MenuManager windowMenu=new MenuManager("&Window",IWorkbenchActionConstants.M_WINDOW);        
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        
        
        menuBar.add(fileMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(serverMenu);
        menuBar.add(windowMenu);
        menuBar.add(staticsMenu);
        menuBar.add(helpMenu);
        
        // File
        fileMenu.add(newWindowAction);
        fileMenu.add(new Separator());
        fileMenu.add(messagePopupAction);
        fileMenu.add(openViewAction);
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);
        
        //Server
        serverMenu.add(addServerAction);
        MenuManager addInstanceMenu=new MenuManager("Add Cloud Instance");        
        addInstanceMenu.add(addAWSEC2InstanceAction);
        addInstanceMenu.add(addAliyunInstanceAction);
        serverMenu.add(addInstanceMenu);        
        
        //Statics
        staticsMenu.add(openRequestsSumViewAction);
        MenuManager viewNumMenu=new MenuManager("View Server Numbers");
        viewNumMenu.add(viewAllServerNumAction);
        viewNumMenu.add(viewAWSServerNumAction);
        viewNumMenu.add(viewAliServerNumAction);
        staticsMenu.add(viewNumMenu);
        MenuManager viewInstanceTimeMenu=new MenuManager("View Instance Time");
        viewInstanceTimeMenu.add(viewAllInstanceTimeAction);
        viewInstanceTimeMenu.add(viewAWSInstanceTimeAction);
        viewInstanceTimeMenu.add(viewAliInstanceTimeAction);
        staticsMenu.add(viewInstanceTimeMenu);
        MenuManager viewStartTimeMenu=new MenuManager("View Server Start Time");
        viewStartTimeMenu.add(viewAllStartTimeAction);
        viewStartTimeMenu.add(viewAWSStartTimeAction);
        viewStartTimeMenu.add(viewAliStartTimeAction);
        staticsMenu.add(viewStartTimeMenu);
        
        // Window
        MenuManager showViewMenu=new MenuManager("&Show View","show view");
        showViewMenu.add(openNavigationViewAction);
        showViewMenu.add(openConsoleViewAction);
        showViewMenu.add(openWorkflowInstancesViewAction);
        showViewMenu.add(openServerInfoViewAction);
        showViewMenu.add(openServerStatusViewAction);
        showViewMenu.add(openServerNumberViewAction);
        showViewMenu.add(showViewListAction);
        windowMenu.add(showViewMenu);
        windowMenu.add(perspectiveAction);
        
        // Help
        helpMenu.add(aboutAction);
    }
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "main"));   
        toolbar.add(addServerAction);
        toolbar.add(controllerAction);
        toolbar.add(genRequestAction);
        toolbar.add(messagePopupAction);
    }
    
    protected void fillStatusLine(IStatusLineManager statusLine){
    	super.fillStatusLine(statusLine);
    	final StatusLineContributionItem statusItem=new StatusLineContributionItem("");
    	statusLine.getProgressMonitor();
    	statusItem.setText("Status Messags");
    	statusLine.add(statusItem);
    }
    
}
