package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.OneValueView;
import coreGraphics.*;

import regnView.*;


public class DragMedianTraceApplet extends ScatterApplet {
	static final protected String BOUNDARY_PARAM = "boundary";
	
//	private DragMedianTraceView theView;
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		OneValueView label = new OneValueView(data, "label", this);
		thePanel.add(label);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		return new DragMedianTraceView(data, this, theHorizAxis, theVertAxis, "x", "y",
																																getParameter(BOUNDARY_PARAM));
	}
}