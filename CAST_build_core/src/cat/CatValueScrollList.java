package cat;

import dataView.*;
import valueList.*;

public class CatValueScrollList extends ScrollValueList {
	
	public CatValueScrollList(DataSet theData, XApplet applet,
																	boolean showHeading, CoreCreateTableView freqTableView) {
		super(theData, applet, showHeading);
		
		((ScrollCatValueContent)content).setLinkedFreqTable(freqTableView);
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		return new ScrollCatValueContent(theData, applet, this);
	}
	
	public void resetList() {
		((ScrollCatValueContent)content).resetList();
	}
	
	public void completeTable() {
		((ScrollCatValueContent)content).completeTable();
	}
	
	public int numberCompleted() {
		return ((ScrollCatValueContent)content).numberCompleted();
	}
	
	public CatSelection getValuesClicked() {
		return ((ScrollCatValueContent)content).getValuesClicked();
	}
}
