package exerciseNumGraph;

import dataView.*;
import valueList.*;

public class UsedValueList extends ScrollValueList {
	
	public UsedValueList(DataSet theData, XApplet applet, boolean showHeading) {
		super(theData, applet, showHeading);
		setRetainLastSelection(true);
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		return new UsedValueContent(theData, applet, this);
	}
	
	public void setAlreadyUsed(boolean[] alreadyUsed) {
		((UsedValueContent)content).setAlreadyUsed(alreadyUsed);
		content.repaint();
	}
	
	public void setDragCount(int numberToDrag) {
		((UsedValueContent)content).setDragCount(numberToDrag);
	}
}
