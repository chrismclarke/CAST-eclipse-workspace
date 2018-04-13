package percentileProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import percentile.*;


public class ChangeCumFunctApplet extends DragPercentileApplet {
	static final private String DATA_NAMES_PARAM = "dataNames";
	
	protected XChoice dataChoice;
	protected int currentDataSet = 0;
	
	private XCheckbox smoothCheck;
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		String dataNameString = getParameter(DATA_NAMES_PARAM);
		if (dataNameString != null) {
			StringTokenizer st = new StringTokenizer(dataNameString, "#");
			XPanel choicePanel = new InsetPanel(0, 0, 0, 10);
			choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			dataChoice = new XChoice(translate("Data set"), XChoice.HORIZONTAL, this);
			while (st.hasMoreTokens())
				dataChoice.addItem(st.nextToken());
			choicePanel.add(dataChoice);
			
			thePanel.add("North", choicePanel);
		}
		
			XPanel titlePanel = new XPanel();
			titlePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_BOTTOM, 0));
			titlePanel.add(super.topPanel(data));
		
		thePanel.add("West", titlePanel);
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				smoothCheck = new XCheckbox(translate("Smooth function"), this);
			checkPanel.add(smoothCheck);
			
		thePanel.add("Center", checkPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == smoothCheck) {
			int cumEvaluateType = smoothCheck.getState() ? PercentileInfo.SMOOTH
																															: PercentileInfo.STEP;
			((CumFunctDotPlotView)cumView).setCumEvaluateType(cumEvaluateType);
			formula.setCumEvaluateType(cumEvaluateType);
			formula2.setCumEvaluateType(cumEvaluateType);
		}
		else if (target == dataChoice) {
			int newChoice = dataChoice.getSelectedIndex();
			if (newChoice != currentDataSet) {
				currentDataSet = newChoice;
				NumVariable yVar = (NumVariable)data.getVariable("y");
				String suffix = (newChoice == 0) ? "" : String.valueOf(newChoice + 1);
				yVar.readValues(getParameter(VALUES_PARAM + suffix));
				
				String longVarName = getParameter(LONG_VAR_NAME_PARAM + suffix);
				horizAxis.setAxisName((longVarName == null) ? data.getVariable("y").name : longVarName);
				
				data.variableChanged("y");
				horizAxis.invalidate();
				validate();
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