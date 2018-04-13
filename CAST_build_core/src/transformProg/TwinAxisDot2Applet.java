package transformProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.OneValueView;
import coreVariables.*;

import transform.*;


public class TwinAxisDot2Applet extends XApplet {
	static final private String AXIS1_INFO_PARAM = "horizAxis1";
	static final private String AXIS2_INFO_PARAM = "horizAxis2";
	
	static final private String SCALED_NAME_PARAM = "var2Name";
	static final private String INDEX_NAME_PARAM = "indexName";
	
	static final private String TRANSFORM_PARAM = "transform";
	
	static final private String RAW_LABEL_PARAM = "rawLabel";
	static final private String RAW_UNITS_PARAM = "rawUnits";
	static final private String SCALED_LABEL_PARAM = "transformLabel";
	static final private String SCALED_UNITS_PARAM = "transformUnits";
	
	private Variable rawVariable;
	private Variable scaledVariable;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
		add("North", valuePanel(data));
		add("Center", createView(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		rawVariable = (Variable)data.getVariable("y");
		
		scaledVariable = new ScaledVariable(getParameter(SCALED_NAME_PARAM), data.getNumVariable(),
																					"y", getParameter(TRANSFORM_PARAM));
		data.addVariable("scaled", scaledVariable);
		
		data.addVariable("index", new IndexVariable(getParameter(INDEX_NAME_PARAM), rawVariable.noOfValues()));
		return data;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
		thePanel.add(new OneValueView(data, "index", this));
		
		OneValueView rawView = new OneValueView(data, "y", this);
		rawView.setLabel(getParameter(RAW_LABEL_PARAM));
		rawView.setUnitsString(getParameter(RAW_UNITS_PARAM));
		thePanel.add(rawView);
		
		OneValueView transView = new OneValueView(data, "scaled", this);
		transView.setLabel(getParameter(SCALED_LABEL_PARAM));
		transView.setUnitsString(getParameter(SCALED_UNITS_PARAM));
//		transView.setForeground(Color.blue);
		thePanel.add(transView);
		return thePanel;
	}
	
	private XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis axis1 = new HorizAxis(this);
		axis1.setAxisName(rawVariable.name);
		String labelInfo = getParameter(AXIS1_INFO_PARAM);
		axis1.readNumLabels(labelInfo);
		axis1.setForeground(Color.blue);
		thePanel.add("Top", axis1);
		
		HorizAxis axis2 = new HorizAxis(this);
		axis2.setAxisName(scaledVariable.name);
		labelInfo = getParameter(AXIS2_INFO_PARAM);
		axis2.readNumLabels(labelInfo);
		axis2.setForeground(Color.blue);
		thePanel.add("Bottom", axis2);
		
		DataView theView = coreView(data, axis1);
		theView.setActiveNumVariable("y");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected DataView coreView(DataSet data, NumCatAxis theHorizAxis) {
		return new TwinAxisDotPlotView(data, this, theHorizAxis);
	}
}