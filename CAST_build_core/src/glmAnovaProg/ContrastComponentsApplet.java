package glmAnovaProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import glmAnova.*;


public class ContrastComponentsApplet extends XApplet {
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_VALUES_PARAM = "yValues";
	
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final protected String X2_VAR_NAME_PARAM = "x2VarName";
	static final protected String X2_VALUES_PARAM = "x2Values";
	static final protected String X2_LABELS_PARAM = "x2Labels";
	
	static final protected String GROUP1_COUNT_PARAM = "group1Count";
	static final protected String COMPONENT_NAMES_PARAM = "componentNames";
	
	private DataSet data;
	
	private VertAxis yAxis;
	private ContrastFactorView theView;
	
	private XChoice componentChoice;
	private int currentComponent = 0;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("North", yNamePanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
			CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readLabels(getParameter(X_LABELS_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
			GroupsModelVariable lsFactorModel = new GroupsModelVariable("Factor LS", data, "x");
			lsFactorModel.updateLSParams("y");
		data.addVariable("ls2", lsFactorModel);
		
			CatVariable x2Var = new CatVariable(getParameter(X2_VAR_NAME_PARAM));
			x2Var.readLabels(getParameter(X2_LABELS_PARAM));
			x2Var.readValues(getParameter(X2_VALUES_PARAM));
		data.addVariable("xGroup", x2Var);
		
			GroupsModelVariable lsGroupedModel = new GroupsModelVariable("Group LS", data, "xGroup");
			lsGroupedModel.updateLSParams("y");
		data.addVariable("ls1", lsGroupedModel);
		
		return data;
	}
	
	protected XPanel yNamePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			CatVariable xVar = (CatVariable)data.getVariable("x");
			xAxis.setCatLabels(xVar);
		thePanel.add("Bottom", xAxis);
		
			yAxis = new VertAxis(this);
			String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
			yAxis.readNumLabels(labelInfo);;
		thePanel.add("Left", yAxis);
		
			int levelsInGroup0 = Integer.parseInt(getParameter(GROUP1_COUNT_PARAM));
			theView = new ContrastFactorView(data, this, xAxis, yAxis, "y", "x", "xGroup", "ls1", "ls2", levelsInGroup0);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
			componentChoice = new XChoice(this);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAMES_PARAM), "#");
			for (int i=0 ; i<3 ; i++)
				componentChoice.addItem(st.nextToken());		//	Factor, group contrast, within groups
		
		thePanel.add(componentChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == componentChoice) {
			int newChoice = componentChoice.getSelectedIndex();
			if (newChoice != currentComponent) {
				currentComponent = newChoice;
				theView.setDisplayType((newChoice == 0) ? ContrastFactorView.FACTOR
																		: (newChoice == 1) ? ContrastFactorView.BETWEEN_GROUPS
																		: ContrastFactorView.WITHIN_GROUPS);
				theView.repaint();
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