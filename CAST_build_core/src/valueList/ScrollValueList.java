package valueList;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;


public class ScrollValueList extends XPanel implements AdjustmentListener {
	static public final boolean NO_HEADING = false;
	static public final boolean HEADING = true;
	
	static public final int RAW_VALUE = 0;
	static public final int RANK = 1;
	static public final int INVERSE_RANK = 2;
	
	static public final boolean SMALL_LAST = false;
	static public final boolean SMALL_FIRST = true;
	
	private Font listFont;
	
	private DataSet theData;
	private XApplet applet;
	
	protected ScrollValueContent content;
	private ListHeading headings;
	private ListTotal totals;
	private JScrollBar scrollbar;
	
	public ScrollValueList(DataSet theData, XApplet applet, boolean showHeading) {
		this.theData = theData;
		this.applet = applet;
		setLayout(new BorderLayout());
		listFont = applet.getStandardFont();
		add("Center", listPanel(theData, applet));
		if (showHeading) {
			headings = new ListHeading(content);
//			headings.lockBackground(applet.getBackground());
			add("North", headings);
			headings.setFont(listFont);
		}
	}
	
	public void addVariableToList(String varKey, int displayType) {
		content.addVariableToList(varKey, displayType);
	}
	
	public void addTotals(boolean[] displayed) {
		totals = new ListTotal(content, displayed, theData, applet);
		add("South", totals);
	}
	
	public void setRetainLastSelection(boolean retainLastSelection) {
		content.setRetainLastSelection(retainLastSelection);
	}
	
	public void sortByVariable(String varKey, boolean smallFirst) {
		content.sortByVariable(varKey, smallFirst);
	}
	
	public void setSelectedCols(int selectedCol1, int selectedCol2) {
		content.setSelectedCols(selectedCol1, selectedCol2);
	}
	
	public void setSelectedCols(String selectedKey1, String selectedKey2) {
		content.setSelectedCols(selectedKey1, selectedKey2);
	}
	
	public void setCanSelectRows(boolean canSelectRows) {
		content.setCanSelectRows(canSelectRows);
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (content != null)
			content.setFont(f);
		if (headings != null)
			headings.setFont(f);
		if (totals != null)
			totals.setFont(f);
	}
	
	public void resetVariables() {
		if (headings != null)
			headings.resetVariables();
		content.resetColumns();
	}
	
	public void setSelectionColors(Color selectedRowColor, Color selectedColColor,
																				Color selectedRowColColor) {
		content.setSelectionColors(selectedRowColor, selectedColColor, selectedRowColColor);
	}
	
	public void scrollToEnd() {
		content.scrollToEnd();
	}
	
	private XPanel listPanel(DataSet theData, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		content = createContent(theData, applet);
		thePanel.add("Center", content);
		content.setFont(listFont);
		
		scrollbar = new JScrollBar(Scrollbar.VERTICAL);
		scrollbar.addAdjustmentListener(this);
		thePanel.add("East", scrollbar);
		return thePanel;
	}
	
	protected ScrollValueContent createContent(DataSet theData, XApplet applet) {
		return new ScrollValueContent(theData, applet, this);
	}
	
	public void adjustmentValueChanged(AdjustmentEvent e) {
//		int topIndex = scrollbar.getValue();
		content.setTopIndex(scrollbar.getValue());
	}
	
	protected void scrollToIndex(int index) {
		scrollbar.setValue(index);
		content.setTopIndex(index);
	}
	
	public void initialiseScrollbar(int noOfValues, int noVisible, int topIndex) {
		scrollbar.setValues(topIndex, noVisible, 0, noOfValues);
		scrollbar.setBlockIncrement(noVisible - 10);
	}
}
