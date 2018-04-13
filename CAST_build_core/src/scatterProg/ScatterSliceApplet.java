package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import scatter.*;
import coreGraphics.*;


public class ScatterSliceApplet extends ScatterApplet {
	static final private String SLICE_WIDTH_PARAM = "sliceWidth";
	static final private String PROB_NAME_PARAM = "probName";
	
	static final private String kProbAxisInfo = "0.0 1.0 0.0 0.2";
	
	private VertAxis probAxis;
	
	public void setupApplet() {
		data = readData();
		
		labelAxes = true;
		
		setLayout(new BorderLayout(10, 0));
		
		XPanel scatterPanel = displayPanel(data);
		scatterPanel.add("Right", probAxis);		//		created by createDataView()
		
		add("Center", scatterPanel);
		
		add("North", topPanel(data));
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		double selectRange = Double.parseDouble(getParameter(SLICE_WIDTH_PARAM));
		probAxis = new VertAxis(this);
		probAxis.readNumLabels(kProbAxisInfo);
		return new SliceScatter2View(data, this, theHorizAxis, theVertAxis, probAxis, "x", "y", selectRange);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(theVertAxis.getFont());
		thePanel.add("West", yVariateName);
		
		XLabel probName = new XLabel(getParameter(PROB_NAME_PARAM), XLabel.RIGHT, this);
		probName.setFont(probAxis.getFont());
		thePanel.add("East", probName);
		
		return thePanel;
	}
}