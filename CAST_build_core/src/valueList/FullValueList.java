package valueList;

import java.awt.*;

import dataView.*;

public class FullValueList extends XPanel {
	static public final boolean NO_HEADING = false;
	static public final boolean HEADING = true;
	
	protected ScrollValueContent content;
	
	public FullValueList(DataSet theData, XApplet applet, boolean showHeading) {
		setLayout(new BorderLayout());
		
			Font listFont = applet.getStandardFont();
			content = new ScrollValueContent(theData, applet, null);
			content.setFont(listFont);
		
		add("Center", content);
		
		if (showHeading) {
			ListHeading headings = new ListHeading(content);
			headings.setFont(listFont);
			add("North", headings);
		}
	}
	
	public void addVariableToList(String varKey) {
		content.addVariableToList(varKey, ScrollValueList.RAW_VALUE);
	}
	
	public void setRetainLastSelection(boolean retainLastSelection) {
		content.setRetainLastSelection(retainLastSelection);
	}
	
	public void setCanSelectRows(boolean canSelectRows) {
		content.setCanSelectRows(canSelectRows);
	}
}
