package statisticProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;

import statistic.*;


public class NormalDistnsApplet extends XApplet {
	static final private String NO_OF_VARIABLES_PARAM = "noOfVariables";
	static final private String GROUP_NAMES_PARAM = "groupNames";
	static final private String VARIABLE_INFO_PARAM = "variableInfo";		//	name#axisInfo#mean1 sd1#mean2 sd2
	
	private XChoice dataSetChoice;
	private int currentDataSet = 0;
	
	private DataSet data;
	
	private HorizAxis axis;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 8));
		
		add("Center", displayDisplay(data));
		add("South", controlPanel());
		
		changeDistns();
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		StringTokenizer st = new StringTokenizer(getParameter(GROUP_NAMES_PARAM), "*");
		
			NormalDistnVariable y1Var = new NormalDistnVariable(st.nextToken());
		data.addVariable("y1", y1Var);
		
			NormalDistnVariable y2Var = new NormalDistnVariable(st.nextToken());
		data.addVariable("y2", y2Var);
		
		return data;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
			dataSetChoice = new XChoice(translate("Variable") + ":", XChoice.HORIZONTAL, this);
			int noOfVariables = Integer.parseInt(getParameter(NO_OF_VARIABLES_PARAM));
			for (int i=0 ; i<noOfVariables ; i++) {
				StringTokenizer st = new StringTokenizer(getParameter(VARIABLE_INFO_PARAM + i), "#");
				dataSetChoice.addItem(st.nextToken());
			}
		
		thePanel.add(dataSetChoice);
		
		return thePanel;
	}
	
	private XPanel displayDisplay(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(100, 180));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				axis = new HorizAxis(this);
			mainPanel.add("Bottom", axis);
			
				TwoNormalView theView = new TwoNormalView(data, this, axis, "y1", "y2");
				theView.setFont(getBigFont());
				theView.lockBackground(Color.white);
				theView.setRetainLastSelection(true);
				theView.setCrossSize(DataView.LARGE_CROSS);
			mainPanel.add("Center", theView);
		
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	private void changeDistns() {
		StringTokenizer st = new StringTokenizer(getParameter(VARIABLE_INFO_PARAM + currentDataSet), "#");
		
		st.nextToken();
		String axisInfo = st.nextToken();
		String meanSd1 = st.nextToken();
		String meanSd2 = st.nextToken();
		
		NormalDistnVariable y1Var = (NormalDistnVariable)data.getVariable("y1");
		y1Var.setParams(meanSd1);
		NormalDistnVariable y2Var = (NormalDistnVariable)data.getVariable("y2");
		y2Var.setParams(meanSd2);
		
		data.variableChanged("y1");
		axis.readNumLabels(axisInfo);
		axis.invalidate();
		validate();
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != currentDataSet) {
				currentDataSet = newChoice;
				changeDistns();
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