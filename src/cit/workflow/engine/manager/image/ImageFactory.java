package cit.workflow.engine.manager.image;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import cit.workflow.Constants;
import cit.workflow.engine.manager.Activator;

public class ImageFactory {
	
	private ImageFactory() {
	}

	public static final String RELATIVE_PATH="icons/";
	public static final String ICON_PATH = Constants.MANAGER_PATH+RELATIVE_PATH;
	public static final String REAL_PATH = "icons/";
	
	
	public static final String RUNNING="running.gif";
	public static final String STOPPED="stopped.gif";
	public static final String SERVICE="service.gif";
	public static final String SERVER="server.gif";
	public static final String SERVER_RUNNING="server_running.gif";
	public static final String SERVER_STOPPED="server_stopped.gif";
	public static final String SERVERS="servers.gif";
	public static final String TREEELEMENT="treeelement.gif";
	public static final String GREENCIRCLE="green-circle.gif";
	public static final String FINISHED="finished.gif";
	public static final String NAVIGATIONVIEW="filenav_nav.gif";
	public static final String CONSOLEVIEW="console_view.gif";
	public static final String WORKFLOWSVIEW="workflows.gif";
	public static final String SERVERSTATUS="status.gif";
	public static final String ADDSERVER="addserver.gif";
	public static final String ADD="add.gif";
//	private static Hashtable<String, Image> htImage = new Hashtable<String, Image>();
	
	
	private static ImageRegistry ir=new ImageRegistry(); 

	public static Image getImage(String imageName){
		return getImageDescriptor(imageName).createImage();
	}
	
	public static ImageDescriptor getImageDescriptor(String imageName){
		ImageDescriptor id=Activator.getImageDescriptor(RELATIVE_PATH+imageName);
		if(id==null){
			id=ImageDescriptor.createFromFile(ImageFactory.class, imageName);
			ir.put(imageName,id);
		}
		return id;
	}
	
	
//	public static ImageDescriptor getImageDescriptor(String imageName){
//		return Activator.getImageDescriptor(RELATIVE_PATH+imageName);
//	}
//	
//	public static Image getImage(String imageName) {
//		return getImage(Display.getCurrent(),imageName);
//	}
//
//	public static Image getImage(Display display, String imageName) {
//		Image image = (Image) htImage.get(imageName.toUpperCase());
//		if (image == null) {
//			image = new Image(display, ICON_PATH + imageName);
//			htImage.put(imageName.toUpperCase(), image);
//		}
//		return image;
//	}
//	
//	
//	public static void dispose() {
//		Enumeration<Image> e = htImage.elements();
//		while (e.hasMoreElements()) {
//			Image image = (Image) e.nextElement();
//			if (!image.isDisposed())
//				image.dispose();
//		}
//	}
}