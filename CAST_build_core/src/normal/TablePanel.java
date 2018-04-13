package normal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;
import utils.*;
import distn.*;


public class TablePanel extends XPanel implements AdjustmentListener {
	private TableLayout theLayout;
	
	private NormalTableView theTable;
	private HorizHeading hHeading;
	private VerticalHeading vHeading;
	private JScrollBar hScroll, vScroll;
	
	public TablePanel(XApplet applet) {
		DataSet data = new DataSet();
		NormalDistnVariable y = new NormalDistnVariable("Z");
		y.setParams("0 1");
		data.addVariable("distn", y);
		data.setSelection("distn", Double.NEGATIVE_INFINITY, 0.0);
		
		theLayout = new TableLayout();
		setLayout(theLayout);
		Font headingFont = applet.getStandardBoldFont();
		
		XLabel zLabel = new XLabel("z", XLabel.CENTER, applet);
		zLabel.setFont(headingFont);
		add(TableLayout.TOP_LEFT, zLabel);
		
		hScroll = new JScrollBar(JScrollBar.HORIZONTAL);
		hScroll.addAdjustmentListener(this);
		add(TableLayout.HORIZ_SCROLL, hScroll);
		
		vScroll = new JScrollBar(JScrollBar.VERTICAL);
		vScroll.addAdjustmentListener(this);
		add(TableLayout.VERT_SCROLL, vScroll);
		
		theTable = new NormalTableView(data, applet, "distn");
		theTable.setFont(applet.getStandardFont());
		theTable.lockBackground(Color.white);
		add(TableLayout.VALUES, theTable);
		
		hHeading = new HorizHeading(theTable, applet);
		hHeading.setFont(headingFont);
		add(TableLayout.HORIZ_HEADING, hHeading);
		
		vHeading = new VerticalHeading(theTable, applet);
		vHeading.setFont(headingFont);
		add(TableLayout.VERT_HEADING, vHeading);
		
		theTable.setHeadings(hHeading, vHeading);
	}
	
	public Insets insets() {
		return new Insets(3, 3, 3, 3);
	}
	
	public void adjustmentValueChanged(AdjustmentEvent e) {
		int newOrgX = -hScroll.getValue() * TableLayout.kPixelShift;
		int newOrgY = -vScroll.getValue() * TableLayout.kPixelShift;
		theTable.setOrigin(newOrgX, newOrgY, hHeading, vHeading);
	}

/*	
	public boolean handleEvent(Event evt) {
		switch (evt.id) {
			case Event.SCROLL_LINE_UP:
			case Event.SCROLL_LINE_DOWN:
			case Event.SCROLL_PAGE_UP:
			case Event.SCROLL_PAGE_DOWN:
			case Event.SCROLL_ABSOLUTE:
				int newOrgX = -hScroll.getValue() * theLayout.kPixelShift;
				int newOrgY = -vScroll.getValue() * theLayout.kPixelShift;
				theTable.setOrigin(newOrgX, newOrgY, hHeading, vHeading);
				return true;
		}
		return super.handleEvent(evt);
	}
*/
}