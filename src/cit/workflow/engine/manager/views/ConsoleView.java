package cit.workflow.engine.manager.views;  
  
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.ui.console.ConsolePlugin;  
import org.eclipse.ui.console.IConsole;  
import org.eclipse.ui.console.IConsoleFactory;  
import org.eclipse.ui.console.IConsoleManager;  
import org.eclipse.ui.console.MessageConsole;  
import org.eclipse.ui.console.MessageConsoleStream;
  
public class ConsoleView implements IConsoleFactory {  
  
	public static String ID="cit.workflow.engine.manager.views.consoleview";
    private static MessageConsole console = new MessageConsole("", null);  
    public static boolean exists = false;  
  
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
			System.setOut(new PrintStream(stream));
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
  
    /** 
     * 获取控制台 
     *  
     * @return 
     */  
    public static MessageConsole getConsole() {  
 
        showConsole();  
  
        return console;  
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
    
    public static void println(String message){
    	println(message,false);
    }
    
}  