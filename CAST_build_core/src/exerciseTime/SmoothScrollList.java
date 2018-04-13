package exerciseTime;

import dataView.*;
import valueList.*;

public class SmoothScrollList extends ScrollValueList {
	
	public SmoothScrollList(DataSet theData, XApplet applet, boolean showHeading) {
		super(theData, applet, showHeading);
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		return new SmoothScrollContent(theData, applet, this);
	}
	
	
	public void setMissing(int[] missingIndex, Value[] missingText) {
		((SmoothScrollContent)content).setMissing(missingIndex, missingText);
	}
}
