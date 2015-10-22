package cit.workflow.engine.manager.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class RequestGeneratorWindow {
	private static final int FROMFILE=0;
	private static final int CONSTANTS=1;
	private static Shell shell;
	private Text text;
	private Label textLabel;
	private Generator generator;
	
	public void createContent(){		
		shell.setSize(522, 495);
		shell.setText("Workflow Request Generator");
		shell.setLayout(new GridLayout(1, false));
		
		Menu menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		
		
		MenuItem fileMenu = new MenuItem(menubar, SWT.CASCADE);
		fileMenu.setText("File");
		MenuItem generatorMenu=new MenuItem(menubar,SWT.CASCADE);
		generatorMenu.setText("Generate");
		
		//file menu
		Menu fileMenuCascade = new Menu(fileMenu);
		fileMenu.setMenu(fileMenuCascade);
		//restartMenu
		MenuItem restartMenu=new MenuItem(fileMenuCascade,SWT.NONE);
		restartMenu.setText("Restart");
		restartMenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if(!GeneratePreviousCheck()) return;
				generator.setRun(true);
				Thread thread=new Thread(generator);
				thread.setDaemon(true);
				thread.start();
			}
		});
		//stopMenu
		MenuItem stopMenu=new MenuItem(fileMenuCascade,SWT.NONE);
		stopMenu.setText("Stop");
		stopMenu.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				if(generator==null){
					MessageBox messageBox = new MessageBox(shell, SWT.OK|SWT.ICON_ERROR);
					messageBox.setMessage("Please specify a generate model");
					messageBox.open();
					return;
				}
				if(generator!=null&&generator.isRun()){
					generator.setRun(false);
					log("Stop current Generator");
				}
			}
		});
		//exitMenu
		MenuItem exitMenu = new MenuItem(fileMenuCascade, SWT.NONE);
		exitMenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		exitMenu.setText("Exit");
		
		//generator menu
		Menu generatorMenuCascade=new Menu(generatorMenu);
		generatorMenu.setMenu(generatorMenuCascade);
		
		MenuItem fromFileGeneratorMenu=new MenuItem(generatorMenuCascade,SWT.NONE);		
		fromFileGeneratorMenu.addSelectionListener(new FromFileGeneratorSelectionAdapter());
		fromFileGeneratorMenu.setText("From File");
		
		MenuItem constantsGeneratorMenu=new MenuItem(generatorMenuCascade,SWT.NONE);		
		constantsGeneratorMenu.setText("Constants");
		
		MenuItem GaussianGeneratorMenu=new MenuItem(generatorMenuCascade,SWT.NONE);		
		GaussianGeneratorMenu.setText("Gaussian");
		
		MenuItem PoissonGeneratorMenu=new MenuItem(generatorMenuCascade,SWT.NONE);		
		PoissonGeneratorMenu.setText("Poisson");
		
		//label
		textLabel = new Label(shell, SWT.NONE);
		textLabel.setText("Generate Log");
		textLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		//text
		text = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		shell.open();
		shell.layout();		
	}
	
	private void log(String message){		
		final String msg=message;
    	Display.getDefault().syncExec(new Runnable() {
    	    public void run() {
    	    	SimpleDateFormat s=new SimpleDateFormat("HH:mm:ss");
    	    	String now=s.format(new Date());
    	    	text.append(String.format("%s - %s\n", now,msg));
    		}
    	});
	}
	
	private boolean GeneratePreviousCheck(){
		if(generator!=null && generator.isRun()){
			MessageBox messageBox = new MessageBox(shell, SWT.OK|SWT.ICON_ERROR);
			messageBox.setMessage("Please wait until current generator completes");
			messageBox.open();
			return false;
		}
		return true;
	}
	
	private class FromFileGeneratorSelectionAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			if(!GeneratePreviousCheck()) return;
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setFileName("");
			dialog.setFilterNames(new String[] { "文本文件 (*.txt)", "所有文件(*.*)" });//设置扩展名
			dialog.setFilterExtensions(new String[] { "*.txt", "*.*" });//设置文件扩展名
			String fileName = dialog.open();
			if(fileName!=null && fileName.isEmpty())return;
			textLabel.setText("Generating requests from "+fileName);
			generator=new FromFileGenerator(fileName);
			generator.setRun(true);		
			Thread thread=new Thread(generator);
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	private class Generator implements Runnable{
		protected boolean run=true;
		public boolean isRun() {
			return run;
		}
		public void setRun(boolean run) {
			this.run = run;
		}
		@Override
		public void run(){}
	}

	private class FromFileGenerator extends Generator{
		private String file;
		public FromFileGenerator(String fileName){
			this.file=fileName; 
		}
		@Override
		public void run(){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				float now=0;
				long nextclock=System.currentTimeMillis();
				while(run && (line=reader.readLine())!=null){
					String[] ss=line.split(" ");
					float date=Float.parseFloat(ss[0]);
					int workflowid=Integer.parseInt(ss[1]);
					log(String.format("Next Request: %.2f minutes later", (date-now)*60));
					nextclock+=(date-now)*60*60*1000;
					while(run && System.currentTimeMillis()<nextclock){
//						log( (nextclock-System.currentTimeMillis())+"");
						if(nextclock-System.currentTimeMillis()>5000)Thread.sleep(5000);
						else Thread.sleep(nextclock-System.currentTimeMillis());						
					}
					if(!run)break;
					Connection conn=null;
					try{
						conn=ConnectionPool.getInstance().getConnection();
						String statement="INSERT INTO managerrequests(date,workflowid) VALUES(?,?)";
						PreparedStatement pst=conn.prepareStatement(statement);
						pst.setLong(1, System.currentTimeMillis());
						pst.setInt(2, workflowid);
						pst.executeUpdate();
						pst.close();
						log("Generate Request: Workflow "+workflowid);
					}catch (SQLException e1) {
						e1.printStackTrace();
						break;
					} finally{
						ConnectionPool.getInstance().returnConnection(conn);
					}
				}
				log("Generate Complete");
				run=false;
			} catch (Exception e){
				log(e.getMessage());
				return;
			}finally{
				Display.getDefault().syncExec(new Runnable() {
					public void run(){
						textLabel.setText(String.format("Generate requests complete from %s", file));
					}
				});
				run=false;
			}
		}
	}
	
	public static void main(String[] args) {
		Display display = Display.getDefault();
		shell = new Shell();
		RequestGeneratorWindow mainWindow=new RequestGeneratorWindow();
		mainWindow.createContent();

		shell.layout(true, true);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
