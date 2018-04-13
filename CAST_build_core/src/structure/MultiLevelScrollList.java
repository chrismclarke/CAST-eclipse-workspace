package structure;

import dataView.*;
import valueList.*;


public class MultiLevelScrollList extends ScrollValueList {
	
	public MultiLevelScrollList(DataSet theData, XApplet applet, boolean showHeading) {
		super(theData, applet, showHeading);
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		return new MultiLevelScrollContent(theData, applet, this);
	}
}
