package cit.workflow.engine.manager.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import cit.workflow.engine.manager.controller.BaseController;
import cit.workflow.engine.manager.controller.FuzzyController;
import cit.workflow.engine.manager.controller.TestController;
import cit.workflow.engine.manager.util.ImageFactory;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.NavigationView;


public class ControllerAction extends Action implements IWorkbenchAction{
	private boolean run=false;
	private BaseController controller;
    private final IWorkbenchWindow window;
    private NavigationView naviView;
    private final String[] TEXT={"Enable Controller","Disable Controller"};

    public ControllerAction(IWorkbenchWindow window) {
        this.window = window;
        this.setText(TEXT[0]);
        naviView=(NavigationView)window.getActivePage().findView(NavigationView.ID);
        setImageDescriptor(ImageFactory.getImageDescriptor(ImageFactory.STATUS));
//        setImageDescriptor(cit.workflow.engine.manager.Activator.getImageDescriptor("/icons/sample3.gif"));
    }

    public void run() {
    	if(run){
    		controller.setRun(false);
    		this.setText(TEXT[0]);
    	}
    	else{
//    		controller=new TestController(window,BaseController.SERVERCONTROL_LOCAL);
//    		controller.initialize();
//    		controller.start();
    		ConsoleView.println("Pattern Based Controller Startup, default cloud: AWS EC2");
    		this.setText(TEXT[1]);
    	}
    	run=!run;
    }
    
	@Override
	public void dispose() {
		controller.setRun(false);
	}
}