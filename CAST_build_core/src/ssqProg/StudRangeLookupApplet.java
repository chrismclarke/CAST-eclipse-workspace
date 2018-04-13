package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import valueList.*;


public class StudRangeLookupApplet extends XApplet {
	static final private String N_LEVELS_PARAM = "nLevels";
	static final private String N_REPS_PARAM = "nReps";
	
	static final private NumValue kMinGroupsValue = new NumValue(2, 0);
	static final private NumValue kMaxGroupsValue = new NumValue(20, 0);
	
	
	static final private NumValue kMinPerGroupValue = new NumValue(2, 0);
	static final private NumValue kMaxPerGroupValue = new NumValue(40, 0);
	
	static final private NumValue kMaxResult = new NumValue(99.0, 3);
	
	static final private Color kResultBackgroundColor = new Color(0xEDF2FF);
	
	private ParameterSlider noOfGroupsSlider;
	private ParameterSlider noPerGroupSlider;
	
	private FixedValueView rangeView;
	
	public void setupApplet() {
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
	
		add(noOfGroupsPanel());
		add(noPerGroupPanel());
		add(resultPanel());
		
		setStudentisedRange();
	}
	
	private XPanel noOfGroupsPanel() {
		XPanel thePanel = new InsetPanel(100, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			String levelsString = getParameter(N_LEVELS_PARAM);
			if (levelsString == null)
				levelsString = translate("Number of groups");
			noOfGroupsSlider = new ParameterSlider(kMinGroupsValue, kMaxGroupsValue, kMinGroupsValue,
																		levelsString, this);
			noOfGroupsSlider.setFont(getStandardBoldFont());
		
		thePanel.add("Center", noOfGroupsSlider);
		
		return thePanel;
	}
	
	private XPanel noPerGroupPanel() {
		XPanel thePanel = new InsetPanel(100, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			String repsString = getParameter(N_REPS_PARAM);
			if (repsString == null)
				repsString = translate("Values per group");
			noPerGroupSlider = new ParameterSlider(kMinPerGroupValue, kMaxPerGroupValue,
																				kMinPerGroupValue, repsString, this);
			noPerGroupSlider.setFont(getStandardBoldFont());
		
		thePanel.add("Center", noPerGroupSlider);
		
		return thePanel;
	}
	
	private XPanel resultPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 5);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
				XLabel coverageLabel = new XLabel(translate("Studentised range constant"), XLabel.LEFT, this);
				coverageLabel.setFont(getStandardBoldFont());
			innerPanel.add(coverageLabel);
			
				rangeView = new FixedValueView(null, kMaxResult, 0.0, this);
				rangeView.setFont(getBigFont());
			
			innerPanel.add(rangeView);
		
			innerPanel.lockBackground(kResultBackgroundColor);
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private void setStudentisedRange() {
		int noOfGroups = (int)Math.round(noOfGroupsSlider.getParameter().toDouble());
		int nPerG = (int)Math.round(noPerGroupSlider.getParameter().toDouble());
		int residDf = noOfGroups * (nPerG - 1);
		
		double coverage = StudentizedRangeTable.quantile(0.95, residDf, noOfGroups);
		
		rangeView.setValue(coverage);
	}

	
	private boolean localAction(Object target) {
		if (target == noOfGroupsSlider) {
			setStudentisedRange();
			return true;
		}
		else if (target == noPerGroupSlider) {
			setStudentisedRange();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}