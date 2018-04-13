package corrProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.OneValueView;
import coreGraphics.*;
import utils.*;

import corr.*;


public class CorrelTransBoxApplet extends ScatterApplet {
	static final private Color kBoxFill = new Color(0xDDDDDD);
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 25, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		CorrelTransView theCorr = new CorrelTransView(data, "x", "y", CorrelationView.NO_FORMULA,
																										theHorizAxis, theVertAxis, this);
		thePanel.add(theCorr);
		
		if (data.getVariable("label") != null)
			thePanel.add(new OneValueView(data, "label", this));
			
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		BoxView xBoxView = new BoxView(data, this, theHorizAxis);
		xBoxView.setShowOutliers(false);
		xBoxView.setActiveNumVariable("x");
		xBoxView.setFillColor(kBoxFill);
		thePanel.add("BottomMargin", xBoxView);
		
		BoxView yBoxView = new BoxView(data, this, theVertAxis);
		yBoxView.setShowOutliers(false);
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
}