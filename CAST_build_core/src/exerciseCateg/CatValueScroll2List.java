package exerciseCateg;

import dataView.*;
import utils.*;
import valueList.*;


public class CatValueScroll2List extends ScrollValueList implements StatusInterface {
	
	public CatValueScroll2List(DataSet theData, XApplet applet, boolean showHeading) {
		super(theData, applet, showHeading);
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		return new ScrollCatValue2Content(theData, applet, this);
	}
	
	public String getStatus() {
		int nClicked = ((ScrollCatValue2Content)content).numberCompleted();
		return String.valueOf(nClicked);
	}
	
	public void setStatus(String statusString) {
		resetList();
		int nClicked = Integer.parseInt(statusString);
		for (int i=0 ; i<nClicked ; i++)
			selectNextValue();
	}
	
	public void resetList() {
		((ScrollCatValue2Content)content).resetList();
	}
	
	public void completeTable() {
		((ScrollCatValue2Content)content).completeTable();
	}
	
	public void selectNextValue() {
		((ScrollCatValue2Content)content).selectNextValue();
	}
	
	public void selectPreviousValue() {
		((ScrollCatValue2Content)content).selectPreviousValue();
	}
	
	public int numberCompleted() {
		return ((ScrollCatValue2Content)content).numberCompleted();
	}
}
