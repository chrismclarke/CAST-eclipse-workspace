package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import dotPlot.*;
import valueList.OneValueView;
import coreVariables.*;


public class TwinAxisDotApplet extends XApplet {
	static final private String AXIS1_INFO_PARAM = "horizAxis1";
	static final private String AXIS2_INFO_PARAM = "horizAxis2";
	
	static final private String SCALED_NAME_PARAM = "var2Name";
	
	static final private String TRANSFORM_PARAM = "transform";
	
	private StackingDotPlotView theView;
	private XChoice jitterStackChoice;
	
	private Variable rawVariable;
	private Variable scaledVariable;
	
	public void setupApplet() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		rawVariable = (Variable)data.getVariable("y");
		
		scaledVariable = new ScaledVariable(getParameter(SCALED_NAME_PARAM), data.getNumVariable(),
																					"y", getParameter(TRANSFORM_PARAM));
		data.addVariable("scaled", scaledVariable);
		
		setLayout(new BorderLayout());
		add("North", valuePanel(data));
		add("Center", createView(data));
		add("South", createControls(data));
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		thePanel.add(new OneValueView(data, "y", this));
		OneValueView transView = new OneValueView(data, "scaled", this);
		thePanel.add(transView);
		transView.setForeground(Color.blue);
		return thePanel;
	}
	
	private XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis axis1 = new HorizAxis(this);
		axis1.setAxisName(rawVariable.name);
		String labelInfo = getParameter(AXIS1_INFO_PARAM);
		axis1.readNumLabels(labelInfo);
		thePanel.add("Bottom", axis1);
		
		HorizAxis axis2 = new HorizAxis(this);
		axis2.setAxisName(scaledVariable.name);
		labelInfo = getParameter(AXIS2_INFO_PARAM);
		axis2.readNumLabels(labelInfo);
		thePanel.add("Bottom", axis2);
		axis2.setForeground(Color.blue);
		
		DataView theView = coreView(data, axis1);
		theView.setActiveNumVariable("y");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected DataView coreView(DataSet data, NumCatAxis theHorizAxis) {
		theView = new StackingDotPlotView(data, this, theHorizAxis);
		return theView;
	}
	
	private XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		jitterStackChoice = new XChoice(this);
		jitterStackChoice.addItem(translate("Jittered"));
		jitterStackChoice.addItem(translate("Stacked"));
		jitterStackChoice.select(0);
		controlPanel.add(jitterStackChoice);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == jitterStackChoice) {
			if (jitterStackChoice.getSelectedIndex() == 0)
				theView.setFrame(0);
			else
				theView.setFinalFrame();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}