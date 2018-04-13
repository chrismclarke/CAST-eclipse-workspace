package dotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import coreGraphics.*;
import valueList.OneValueView;

//import dotPlot.StackingDotPlotView;


public class StackDotPlot2Applet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final private String SHOW_VAR_NAME_PARAM = "showVarName";
	
	protected DataSet data;
	
	protected HorizAxis yAxis;
	
	public void setupApplet() {
		data = readLabelledData();
		
		setLayout(new BorderLayout());
		
		add("Center", dotPlotPanels(data));
		add("North", valuePanel(data));
	}
	
	protected boolean showingVarName() {
		String showVarNameString = getParameter(SHOW_VAR_NAME_PARAM);
		return showVarNameString != null && showVarNameString.equals("true");
	}
	
	protected XPanel oneStackedDotPlot(DataSet data, int crossSize) {
		XPanel stackedDotPlot = new XPanel();
		stackedDotPlot.setLayout(new AxisLayout());
		
		yAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		yAxis.readNumLabels(labelInfo);
		if (showingVarName())
			yAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		stackedDotPlot.add("Bottom", yAxis);
		
//		StackingDotPlotView theDotPlot = new StackingDotPlotView(data, this, yAxis);
		StackedDotPlotView theDotPlot = new StackedDotPlotView(data, this, yAxis);
		stackedDotPlot.add("Center", theDotPlot);
		theDotPlot.setCrossSize(crossSize);
//		theDotPlot.initialiseToFinalFrame();
		theDotPlot.lockBackground(Color.white);
		theDotPlot.setRetainLastSelection(true);
		
		return stackedDotPlot;
	}
	
	protected DataSet readLabelledData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		return data;
	}
	
	private XPanel dotPlotPanels(DataSet data) {
		XPanel thePlots = new XPanel();
		thePlots.setLayout(new GridLayout(2, 1));
		thePlots.add(oneStackedDotPlot(data, DataView.SMALL_CROSS));
		thePlots.add(oneStackedDotPlot(data, DataView.LARGE_CROSS));
		return thePlots;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel theLabels = new XPanel();
		theLabels.setLayout(new FlowLayout(FlowLayout.CENTER));
		theLabels.add(new OneValueView(data, "y", this));
		return theLabels;
	}
}