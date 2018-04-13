package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.OneValueView;
import utils.*;
import coreGraphics.*;



public class ScatterBoxApplet extends ScatterApplet {
	static final private Color kDarkBlue = new Color(0x000099);
	static final private Color kBoxFill = new Color(0xCCCCCC);
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		BoxView xBoxView = new BoxView(data, this, theHorizAxis);
		xBoxView.setActiveNumVariable("x");
		xBoxView.setFillColor(kBoxFill);
		thePanel.add("BottomMargin", xBoxView);
		
		BoxView yBoxView = new BoxView(data, this, theVertAxis);
		yBoxView.setActiveNumVariable("y");
		yBoxView.setFillColor(kBoxFill);
		thePanel.add("LeftMargin", yBoxView);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		DataView theView = super.createDataView(data, theHorizAxis, theVertAxis);
		theView.setRetainLastSelection(true);
		return theView;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
			OneValueView yValueView = new OneValueView(data, "y", this);
				yValueView.addEqualsSign();
//			yValueView.setFont(getBigFont());
		thePanel.add(yValueView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 8));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
				OneValueView xValueView = new OneValueView(data, "x", this);
				xValueView.addEqualsSign();
			
			topPanel.add(xValueView);
			
		thePanel.add(topPanel);
		
		if (data.getVariable("label") != null) {
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
			OneValueView labelView = new OneValueView(data, "label", this);
			labelView.setForeground(kDarkBlue);
			labelView.setFont(getStandardBoldFont());
			labelView.addEqualsSign();
			bottomPanel.add(labelView);
			thePanel.add(bottomPanel);
		}
		
		return thePanel;
	}
}