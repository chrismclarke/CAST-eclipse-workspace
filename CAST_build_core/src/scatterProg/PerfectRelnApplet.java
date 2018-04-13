package scatterProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import valueList.OneValueView;
import coreGraphics.*;
import coreVariables.*;

import scatter.*;


public class PerfectRelnApplet extends ScatterApplet {
	static final protected String Y_TRANSFORM_PARAM = "yTransform";
	static final protected String EQN_FILE_NAME_PARAM = "eqnFile";
	
	static final private int kMarginWidth = 8;
	
	private DataView localView;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
		StringTokenizer transformParams = new StringTokenizer(getParameter(Y_TRANSFORM_PARAM));
		double power = Double.parseDouble(transformParams.nextToken());
		double const0 = Double.parseDouble(transformParams.nextToken());
		double const1 = Double.parseDouble(transformParams.nextToken());
		int decimals = Integer.parseInt(transformParams.nextToken());
		
		PowerVariable invX = new PowerVariable("power", xVar, power, 0);
		data.addVariable("inv", invX);
		ScaledVariable yVar = new ScaledVariable(getParameter(Y_VAR_NAME_PARAM), invX, "power", const0,
																								const1, decimals);
		data.addVariable("y", yVar);
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		localView.lockBackground(thePanel.getBackground());
		
		DotPlotView xDotView = new DotPlotView(data, this, theHorizAxis);
		xDotView.setActiveNumVariable("x");
		xDotView.lockBackground(Color.white);
//		xDotView.setCrossSize(DataView.LARGE_CROSS);
		xDotView.setMinDisplayWidth(kMarginWidth);
		xDotView.setViewBorder(new Insets(10, 5, 10, 5));
		xDotView.setRetainLastSelection(true);
		thePanel.add("BottomMargin", xDotView);
		
		DotPlotView yDotView = new DotPlotView(data, this, theVertAxis);
		yDotView.setActiveNumVariable("y");
		yDotView.lockBackground(Color.white);
//		yDotView.setCrossSize(DataView.LARGE_CROSS);
		yDotView.setMinDisplayWidth(kMarginWidth);
		yDotView.setViewBorder(new Insets(5, 10, 5, 10));
		xDotView.setRetainLastSelection(true);
		thePanel.add("LeftMargin", yDotView);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		String eqnFileName = getParameter(EQN_FILE_NAME_PARAM) + ".gif";
		localView = new LineView(data, this, theHorizAxis, theVertAxis, "inv", "y", "x", eqnFileName);
		localView.setForeground(Color.blue);
		return localView;
	}
	
	private OneValueView createView(DataSet data, String key, String name) {
		StringTokenizer nameParams = new StringTokenizer(name, "()");
		String varName = nameParams.nextToken();
		String units = nameParams.nextToken();
		OneValueView valueView = new OneValueView(data, key, this);
		valueView.setLabel(varName + " =");
		valueView.setUnitsString(units);
		return valueView;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
			OneValueView yValueView = createView(data, "y", getParameter(Y_VAR_NAME_PARAM));
//			yValueView.setFont(getBigFont());
		thePanel.add(yValueView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		
		OneValueView xValueView = createView(data, "x", getParameter(X_VAR_NAME_PARAM));
//		xValueView.setFont(getBigFont());
		thePanel.add(xValueView);
		
		return thePanel;
	}
}