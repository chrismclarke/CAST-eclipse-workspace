package scatterProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import valueList.*;
import utils.*;
import coreVariables.*;
import coreGraphics.*;


public class ScatterLogApplet extends ScatterApplet {
	static final private String UNITS_PARAM = "units";
	
	private String logYName, logXName;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String yName = getParameter(Y_VAR_NAME_PARAM);
		NumVariable yRawVar = new NumVariable(yName);
		yRawVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("yRaw", yRawVar);
		logYName = translate("log") + "(" + yName + ")";
		data.addVariable("y", new LogVariable(logYName, data, "yRaw", 0));
		
		String xName = getParameter(X_VAR_NAME_PARAM);
		NumVariable xRawVar = new NumVariable(xName);
		xRawVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("xRaw", xRawVar);
		logXName = translate("log") + "(" + xName + ")";
		data.addVariable("x", new LogVariable(logXName, xRawVar, null));
		
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		DataView theView = super.createDataView(data, theHorizAxis, theVertAxis);
		theView.setRetainLastSelection(true);
		return theView;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		HorizAxis axis = super.createHorizAxis(data);
		if (labelAxes)
			axis.setAxisName(logXName);
		return axis;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		if (labelAxes) {
			yVariateName = new XLabel(logYName, XLabel.LEFT, this);
			yVariateName.setFont(theVertAxis.getFont());
			thePanel.add(yVariateName);
		}
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 0));
			labelPanel.add(new OneValueView(data, "label", this));
		thePanel.add("West", labelPanel);
		
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 3));
				OneValueView yView = new OneValueView(data, "xRaw", this);
				yView.addEqualsSign();
			valuePanel.add(yView);
			
				OneValueView xView = new OneValueView(data, "yRaw", this);
				xView.addEqualsSign();
			valuePanel.add(xView);
			
			String unitString = getParameter(UNITS_PARAM);
			if (unitString != null) {
				StringTokenizer st = new StringTokenizer(unitString);
				yView.setUnitsString(st.nextToken());
				xView.setUnitsString(st.nextToken());
			}
			
		thePanel.add("Center", valuePanel);
		
		return thePanel;
	}
}