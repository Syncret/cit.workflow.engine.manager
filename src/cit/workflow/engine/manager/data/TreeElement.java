package cit.workflow.engine.manager.data;

import java.util.List;

import org.eclipse.swt.graphics.Image;

public interface TreeElement {
	public String getName();
	public boolean hasChildren();
	public List getChildren();
	public TreeElement getParent();
	public Image getImage();
}
