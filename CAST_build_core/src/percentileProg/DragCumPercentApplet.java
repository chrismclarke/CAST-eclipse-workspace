package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import percentile.*;


public class DragCumPercentApplet extends DragFindPercentApplet {
	static final protected String COUNT_AXIS_PARAM = "countAxis";
	
	static final protected String kZeroOneAxis = "0 1 0.0 0.2";
	
	private MultiVertAxis vertAxis;
	
	private XChoice countPropnChoice;
	private int currentChoice = 0;
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			countPropnChoice = new XChoice(this);
			countPropnChoice.addItem(translate("Cumulative count"));
			countPropnChoice.addItem(translate("Cumulative proportion"));
		thePanel.add(countPropnChoice);
		return thePanel;
	}
	
	protected VertAxis getVertAxis() {
		vertAxis = new MultiVertAxis(this, 2);
		vertAxis.readNumLabels(kZeroOneAxis);
		vertAxis.readExtraNumLabels(getParameter(COUNT_AXIS_PARAM));
		vertAxis.setStartAlternate(1);
		vertAxis.setForeground(Color.red);
		return vertAxis;
	}
	
	protected XPanel dataDisplayPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			String longVarName = getParameter(LONG_VAR_NAME_PARAM);
			horizAxis.setAxisName((longVarName == null) ? data.getVariable("y").name : longVarName);
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = getVertAxis();
		thePanel.add("Left", vertAxis);
		
			cumView = new CumFunctDotPlotView(data, this, horizAxis, referenceData, "ref",
																															PropnRangeView.LESS_EQUAL, vertAxis);
		thePanel.add("Center", cumView);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == countPropnChoice) {
			int newChoice = countPropnChoice.getSelectedIndex();
			if (newChoice != currentChoice) {
				currentChoice = newChoice;
				vertAxis.setAlternateLabels(1 - newChoice);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}