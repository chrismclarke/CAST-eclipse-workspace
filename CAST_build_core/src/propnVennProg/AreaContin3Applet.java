package propnVennProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import propnVenn.*;
import contin.*;


public class AreaContin3Applet extends AreaContin2Applet {
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String Z_LABELS_PARAM = "zLabels";
	static final protected String Z_VALUES_PARAM = "zValues";
	
	public void setupApplet() {
		ContinImages.loadLabels(this);
		
		data = readData();
		
		setLayout(new BorderLayout(0, 30));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
															getParameter(Z_LABELS_PARAM));
		return data;
	}
	
	protected AreaContinCoreView getPropnVenn(DataSet data, VertAxis vertAxis,
												HorizAxis horizAxis, boolean yMarginal) {
		return new AreaContin3View(data, this, vertAxis, horizAxis, "y", "x", "z", AreaContin2View.Y_MARGIN);
	}
	
	protected MarginConditPanel yAxisLabelPanel(DataSet data, boolean marginNotCondit) {
		yAxisLabel = null;
		return null;
	}
	
	protected MarginConditPanel xAxisLabelPanel(DataSet data, boolean marginNotCondit) {
		CoreVariable zVar = data.getVariable("z");
		CoreVariable yVar = data.getVariable("y");
		boolean isFinite = (zVar instanceof CatVariable);
		
		xAxisLabel = new SimpsonsConditPanel(zVar.name, yVar.name, marginNotCondit, isFinite, this);
		return xAxisLabel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XChoice marginChoice = new XChoice(this);
			String zName = data.getVariable("z").name;
			String yName = data.getVariable("y").name;
			
			marginChoice.addItem(translate("Group by") + " " + zName + " & " + yName);
			marginChoice.addItem(translate("Group by") + " " + zName);
		
		thePanel.add(new PickMarginPanel(xAxisLabel, yAxisLabel, theView, marginChoice));
		return thePanel;
	}
}