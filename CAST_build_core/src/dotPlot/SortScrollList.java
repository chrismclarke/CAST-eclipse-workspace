package dotPlot;

import dataView.*;
import valueList.*;

public class SortScrollList extends ScrollValueList {
	
	private SortScrollContent sortContent;
	
	public SortScrollList(DataSet theData, XApplet applet, boolean showHeading) {
		super(theData, applet, showHeading);
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		sortContent = new SortScrollContent(theData, applet, this);
		return sortContent;
	}
	
	public ScrollValueContent getSortContent() {
		return sortContent;
	}
}
