package exerciseNumGraph;

import dataView.*;
import valueList.*;

public class UsedStemLeafList extends ScrollValueList {
	
	public UsedStemLeafList(DataSet theData, XApplet applet, boolean showHeading) {
		super(theData, applet, showHeading);
		setRetainLastSelection(true);
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		return new ScrollStemLeafContent(theData, applet, this);
	}
	
	public void setAlreadyUsed(boolean[] alreadyUsed) {
		((ScrollStemLeafContent)content).setAlreadyUsed(alreadyUsed);
		content.repaint();
	}
	
	public void setLeafPosition(int decimals, int stemPower) {
		((ScrollStemLeafContent)content).setLeafPosition(decimals, stemPower);
	}
}
