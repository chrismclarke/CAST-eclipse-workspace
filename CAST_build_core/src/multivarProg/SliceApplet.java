package multivarProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import regnProg.*;
import multivar.*;


public class SliceApplet extends MultipleScatterApplet {
	static final protected String Z_AXIS_INFO_PARAM = "zAxis";
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String Z_VALUES_PARAM = "zValues";
	static final protected String INIT_SELECT_PARAM = "select";
	
	private VertAxis zAxis;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM));
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.75, 10, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
		thePanel.add("Left", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
		thePanel.add("Right", slicePanel(data));
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																				int plotIndex) {
		ScatterSliceView theView = new ScatterSliceView(data, this, theHorizAxis, theVertAxis, "x", "y");
		theView.setForeground(Color.red);
		return theView;
	}
	
	private XPanel slicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dotPlotPanel(data));
		XLabel zName = new XLabel(getParameter(Z_VAR_NAME_PARAM), XLabel.LEFT, this);
		zName.setFont(zAxis.getFont());
		thePanel.add("North", zName);
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		zAxis = new VertAxis(this);
		zAxis.readNumLabels(getParameter(Z_AXIS_INFO_PARAM));
		thePanel.add("Left", zAxis);
		
		double minSel = 0.0;
		double maxSel = 1.0;
		StringTokenizer theLabels = new StringTokenizer(getParameter(INIT_SELECT_PARAM));
		try {
			minSel = Double.parseDouble(theLabels.nextToken());
			maxSel = Double.parseDouble(theLabels.nextToken());
		} catch (Exception e) {
		}
//		boolean changed = data.setSelection("z", minSel, maxSel);
		
		SliceDotPlotView sliceView = new SliceDotPlotView(data, this, zAxis, 1.0, minSel, maxSel, "z");
		sliceView.lockBackground(Color.white);
		thePanel.add("Center", sliceView);
		
		return thePanel;
	}
}