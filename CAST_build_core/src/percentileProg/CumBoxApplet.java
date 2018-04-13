package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import formula.*;


import percentile.*;


public class CumBoxApplet extends DragCumPercentApplet {
	
	static final private String kZeroOneAxis2 = "0 1 0.00 0.25";
	
	private XPanel propnDisplayPanel;
	private CardLayout propnDisplayLayout;
	private XCheckbox allowDragCheck;
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			XLabel propnLabel = new XLabel(translate("Cumulative proportion"), XLabel.LEFT, this);
			propnLabel.setForeground(Color.red);
		thePanel.add(propnLabel);
		return thePanel;
	}
	
	protected XPanel dataDisplayPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			String longVarName = getParameter(LONG_VAR_NAME_PARAM);
			horizAxis.setAxisName((longVarName == null) ? data.getVariable("y").name : longVarName);
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(kZeroOneAxis2);
			vertAxis.setForeground(Color.red);
		thePanel.add("Left", vertAxis);
		
			cumView = new CumFunctBoxPlotView(data, this, horizAxis, referenceData, "ref",
																													PropnRangeView.LESS_THAN, vertAxis);
			cumView.setAllowDrag(false);
		thePanel.add("Center", cumView);
		return thePanel;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				allowDragCheck = new XCheckbox(translate("Allow drag"), this);
			checkPanel.add(allowDragCheck);
			
		thePanel.add("West", checkPanel);
		
		
			propnDisplayPanel = new XPanel();
			propnDisplayLayout = new CardLayout();
			propnDisplayPanel.setLayout(propnDisplayLayout);
			
			propnDisplayPanel.add("blank", new XPanel());
		
			NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
			FormulaContext boldContext = new FormulaContext(Color.black, getStandardBoldFont(), this);
			formula = new SimplePropnFormulaPanel(data, "y", "ref", referenceData, maxY,
														PropnRangeView.LESS_THAN, boldContext);
			propnDisplayPanel.add("propn", formula);
			
		thePanel.add("Center", propnDisplayPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == allowDragCheck) {
			propnDisplayLayout.show(propnDisplayPanel, allowDragCheck.getState()
																													? "propn" : "blank");
			cumView.setAllowDrag(allowDragCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}