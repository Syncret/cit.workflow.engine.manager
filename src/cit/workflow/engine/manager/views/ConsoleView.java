package cit.workflow.engine.manager.views;  
  
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.ui.console.ConsolePlugin;  
import org.eclipse.ui.console.IConsole;  
import org.eclipse.ui.console.IConsoleFactory;  
import org.eclipse.ui.console.IConsoleManager;  
import org.eclipse.ui.console.MessageConsole;  
import org.eclipse.ui.console.MessageConsoleStream;
  
public class ConsoleView implements IConsoleFactory {  
  
	public static String ID="cit.workflow.engine.manager.views.consoleview";
	//comment next line to run test
    //private static MessageConsole console = new MessageConsole("", null);
	private static MessageConsole console = null;
    public static boolean exists = false;  
    public static boolean logFile=false;
    public static final int LOG_ERROR=0;
    public static final int LOG_WARNING=1;
    public static final int LOG_INFO=2;
    public static final int LOG_DEBUG=3;
    public static final int LOG_VERBOSE=4;
    public static int logLevel=3;
    public static String logPath="d:/data/workflowloglog.txt";
    /** 
     * 获取控制台 
     *  
     * @return 
     */  
    public static MessageConsole getConsole() {  
 
    	
        showConsole();  
  
        return console;  
    }  
    /** 
     * 描述:打开控制台 
     * */  
    public void openConsole() {  
        showConsole();  
    }  
  
    /** */  
    /** 
     * 描述:显示控制台 
     * */  
	public static void showConsole() {
		if(console==null)console = new MessageConsole("", null);
		if (console != null) {
			// 得到默认控制台管理器
			IConsoleManager manager = ConsolePlugin.getDefault()
					.getConsoleManager();

			// 得到所有的控制台实例
			IConsole[] existing = manager.getConsoles();
			exists = false;
			// 新创建的MessageConsole实例不存在就加入到控制台管理器，并显示出来
			for (int i = 0; i < existing.length; i++) {
				if (console == existing[i])
					exists = true;
			}
			if (!exists) {
				manager.addConsoles(new IConsole[] { console });
			}
			manager.showConsoleView(console);
			exists=true;
			//redirect system.out
			MessageConsoleStream stream = console.newMessageStream();
			//redirect the sysout to console, comment next line to let sysout print to eclipse console
			//System.setOut(new PrintStream(stream));
			// console.activate();

		}
	}

    /** */  
    /** 
     * 描述:关闭控制台 
     * */  
    public static void closeConsole() {  
        IConsoleManager manager = ConsolePlugin.getDefault()  
                .getConsoleManager();  
        if (console != null) {  
            manager.removeConsoles(new IConsole[] { console });  
        }  
    }  
  

    
	public static void print(String message, boolean activate) {
		if(!exists) {
			System.out.print(message);
			return;
		}
		MessageConsoleStream printer = ConsoleView.getConsole()
				.newMessageStream();
		printer.setActivateOnWrite(activate);
		printer.print(message);
		try {
			printer.flush();
			printer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void println(String message, boolean activate) {
		if(!exists) {
			System.out.println(message);
			return;
		}MessageConsoleStream printer = ConsoleView.getConsole()
				.newMessageStream();
		printer.setActivateOnWrite(activate);
		printer.println(message);
		try {
			printer.flush();
			printer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    public static void print(String message) {  
        print(message, false);
    } 
    
    public static final ReentrantLock fileLock=new ReentrantLock();
    public static void println(String message){
    	SimpleDateFormat s=new SimpleDateFormat("HH:mm:ss - ");
    	String now=s.format(new Date());
    	println(now+message,false);
    	if(logFile){
    		fileLock.lock();
    		FileWriter writer;
			try {
				writer = new FileWriter(logPath, true);
				writer.write(now+message+"\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				fileLock.unlock();
			}
    	}
    }
    
    public static void println(int level,String message){
    	if(level<=logLevel)println(message);
    }
    
}  