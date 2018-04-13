package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import dotPlot.*;
import valueList.OneValueView;


public class DotLabelApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	
	protected StackingDotPlotView theView;
	private XChoice jitterStackChoice;
	
	public void setupApplet() {
		DataSet data = createData();
		
		setLayout(new BorderLayout());
		add("North", topPanel(data));
		add("Center", createView(data));
		add("South", bottomPanel(data));
	}
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		return data;
	}
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		thePanel.add(new OneValueView(data, "label", this));
		addStackJitterChoice(thePanel);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));
		return controlPanel;
	}
	
	protected void addStackJitterChoice(XPanel thePanel) {
		jitterStackChoice = new XChoice(this);
		jitterStackChoice.addItem(translate("Jittered"));
		jitterStackChoice.addItem(translate("Stacked"));
		jitterStackChoice.select(0);
		thePanel.add(jitterStackChoice);
	}
	
	protected XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis axis = new HorizAxis(this);
		axis.setAxisName(data.getVariable("y").name);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		thePanel.add("Bottom", axis);
		
		DataView theView = coreView(data, axis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected DataView coreView(DataSet data, NumCatAxis theHorizAxis) {
		theView = new StackingDotPlotView(data, this, theHorizAxis);
		return theView;
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