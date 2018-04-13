package bivarCatProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import bivarCat.*;
import cat.CountPropnAxis;


public class BarTimeApplet extends Bar2WayApplet {
	static final private String TIME_INFO_PARAM = "timeAxis";
	static final private String BAR_WIDTH_PARAM = "barWidth";
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable xCatVariable = new CatVariable(getParameter(X_VAR_NAME_PARAM),
																							Variable.USES_REPEATS);
		xCatVariable.readLabels(getParameter(X_LABELS_PARAM));
		xCatVariable.readValues(getParameter(X_VALUES_PARAM));
		xVariable = xCatVariable;
		data.addVariable("x", xVariable);
		
		NumVariable yNumVariable = new NumVariable(getParameter(Y_VAR_NAME_PARAM), Variable.USES_REPEATS);
		yNumVariable.readValues(getParameter(Y_VALUES_PARAM));
		yVariable = yNumVariable;
		data.addVariable("y", yVariable);
		
		return data;
	}
	
	protected int getBarWidth() {
		return Integer.parseInt(getParameter(BAR_WIDTH_PARAM));
	}
	
	protected XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		CountPropnAxis vertAxis = createCountPropnAxis();
		thePanel.add("Left", vertAxis);
		
		IndexTimeAxis timeAxis = new IndexTimeAxis(this, yVariable.getNoOfGroups());
		timeAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
		horizAxis = timeAxis;
		thePanel.add("Bottom", timeAxis);
		
		barView = new BarTimeView(data, this, vertAxis, horizAxis, "x", "y", getBarWidth());
		barView.lockBackground(Color.white);
		thePanel.add("Center", barView);
		
		return thePanel;
	}
	
	protected XPanel bottomPanel(DataSet data) {
		return super.controlPanel(data);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel keyPanel = new XPanel();
		keyPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																					VerticalLayout.VERT_CENTER, 0));
		keyPanel.add(new CatKey2View(data, this, "x", ContinTableView.OUTER));
		
		return keyPanel;
	}
}