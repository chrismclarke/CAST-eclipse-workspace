package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import valueList.*;


public class PairwiseCoverageApplet extends XApplet {
	static final private String APPLET_TYPE_PARAM = "appletType";
	static final private String N_LEVELS_PARAM = "nLevels";
	static final private String N_REPS_PARAM = "nReps";
	
	static final private int noPerGroup[] = {3, 4, 5, 10, 20, 40};
	
	static final private NumValue kMinGroupsValue = new NumValue(2, 0);
	static final private NumValue kMaxGroupsValue = new NumValue(20, 0);
	
	static final private NumValue kMaxCoverage = new NumValue(1.0, 3);
	
	static final private Color kResultBackgroundColor = new Color(0xEDF2FF);
	
	private boolean coverageNotPvalue = true;
	
	private ParameterSlider noOfGroupsSlider;
	private XChoice noInGroupChoice;
	private int currentNoInGroupIndex = 0;
	
	private FixedValueView coverageView;
	
	public void setupApplet() {
		String appletTypeString = getParameter(APPLET_TYPE_PARAM);
		if (appletTypeString != null)
			coverageNotPvalue = !appletTypeString.equals("pValues");
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
	
		add(noOfGroupsPanel());
		add(noPerGroupPanel());
		add(resultPanel());
		
		setMultipleCoverage();
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
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			String repsString = getParameter(N_REPS_PARAM);
			if (repsString == null)
				repsString = translate("Values per group");
			XLabel nPerGroupLabel = new XLabel(repsString + " =", XLabel.LEFT, this);
			nPerGroupLabel.setFont(getStandardBoldFont());
		thePanel.add(nPerGroupLabel);
		
				noInGroupChoice = new XChoice(this);
				for (int i=0 ; i<noPerGroup.length ; i++)
					noInGroupChoice.addItem(String.valueOf(noPerGroup[i]));
		
		thePanel.add(noInGroupChoice);
		
		return thePanel;
	}
	
	private XPanel resultPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 5);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
				XLabel coverageLabel = new XLabel(coverageNotPvalue ?
											translate("Overall probability for all pairwise comparisons")
											: translate("Prob(at least one pairwise p-value < 0.05)"), XLabel.LEFT, this);
				coverageLabel.setFont(getStandardBoldFont());
			innerPanel.add(coverageLabel);
			
				coverageView = new FixedValueView(null, kMaxCoverage, 0.0, this);
				coverageView.setFont(getBigFont());
				
			innerPanel.add(coverageView);
		
			innerPanel.lockBackground(kResultBackgroundColor);
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private void setMultipleCoverage() {
		int noOfGroups = (int)Math.round(noOfGroupsSlider.getParameter().toDouble());
		int nPerG = noPerGroup[currentNoInGroupIndex];
		int residDf = noOfGroups * (nPerG - 1);
		
		double pairedIntervalWidth = TTable.quantile(0.975, residDf) * Math.sqrt(2.0);
		
		double coverage = StudentizedRangeTable.cumulative(pairedIntervalWidth, residDf,
																																						noOfGroups);
		coverageView.setValue(coverageNotPvalue ? coverage : 1.0 - coverage);
	}

	
	private boolean localAction(Object target) {
		if (target == noOfGroupsSlider) {
			setMultipleCoverage();
			return true;
		}
		else if (target == noInGroupChoice) {
			int newChoice = noInGroupChoice.getSelectedIndex();
			if (newChoice != currentNoInGroupIndex) {
				currentNoInGroupIndex = newChoice;
				setMultipleCoverage();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}