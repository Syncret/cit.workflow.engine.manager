package cit.workflow.engine.manager;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import cit.workflow.engine.manager.data.ServerAgent;
import cit.workflow.engine.manager.views.ConsoleView;
import cit.workflow.engine.manager.views.InstanceTimeView;
import cit.workflow.engine.manager.views.ServerInfoView;
import cit.workflow.engine.manager.views.ServerNumberView;
import cit.workflow.engine.manager.views.ServerStartTimeView;
import cit.workflow.engine.manager.views.ServerStatusView;
import cit.workflow.engine.manager.views.NavigationView;
import cit.workflow.engine.manager.views.TypeServerNumView;
import cit.workflow.engine.manager.views.View;
import cit.workflow.engine.manager.views.WorkflowInstancesView;

public class DefaultPerspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */
	public static final String ID = "cit.workflow.engine.manager.perspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		layout.addPerspectiveShortcut(DefaultPerspective.ID);
		
		layout.addView(NavigationView.ID, IPageLayout.LEFT, 0.2f, editorArea);

		// console
		ConsoleView consoleView = new ConsoleView();
		layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM,0.7f, editorArea);
		consoleView.openConsole();
//		IFolderLayout bottomViewsFolder=layout.createFolder("BottomViewsFolder",IPageLayout.BOTTOM,0.75f,editorArea);
//		bottomViewsFolder.addView(ConsoleView.ID);
		
		
		IFolderLayout rightViewsFolder = layout.createFolder("RightViewsFolder", IPageLayout.RIGHT, 0.7f, editorArea);
//		rightViewsFolder.addPlaceholder(View.ID + ":*");
		rightViewsFolder.addView(ServerInfoView.ID);
		
		IFolderLayout centerViewsFolder = layout.createFolder("CenterViewsFolder", IPageLayout.TOP, 0.75f, editorArea);
		centerViewsFolder.addView(WorkflowInstancesView.ID);
		centerViewsFolder.addView(ServerStatusView.ID);
		centerViewsFolder.addView(ServerNumberView.ID);
		centerViewsFolder.addView(TypeServerNumView.ID);
		centerViewsFolder.addView(InstanceTimeView.ID);
		centerViewsFolder.addView(ServerStartTimeView.ID);
		

		
	}
}
