package cit.workflow.engine.manager.test;

public class test {
	private class mThread extends Thread{
		public void run(){
			while(true){
				System.out.println("i'm running");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public test(){
		System.out.println("start");
		mThread mt=new mThread();
//		mt.setDaemon(true);
//		mt.start();
		new Thread(mt).run();
		System.out.println("over");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new test();
		System.out.println("all over");
	}
}
