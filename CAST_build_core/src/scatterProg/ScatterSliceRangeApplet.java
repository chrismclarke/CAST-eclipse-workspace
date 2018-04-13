package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import scatter.*;


public class ScatterSliceRangeApplet extends ScatterApplet {
	static final private String SLICE_WIDTH_PARAM = "sliceWidth";
	
	public void setupApplet() {
		data = readData();
		
		labelAxes = true;
		
		setLayout(new BorderLayout(10, 0));
		add("Center", displayPanel(data));
		add("North", topPanel(data));
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		double selectRange = Double.parseDouble(getParameter(SLICE_WIDTH_PARAM));
		return new SliceScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", selectRange);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(theVertAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
}