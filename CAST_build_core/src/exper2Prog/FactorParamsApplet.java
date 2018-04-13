package exper2Prog;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import formula.*;

import exper2.*;


public class FactorParamsApplet extends XApplet {
	static final private String Y_NAME_PARAM = "yName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_NAME_PARAM = "xName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String MAX_PARAM_PARAM = "maxParam";
	
	private DataSet data;
	
	private NumValue maxParam;
	
	private XCheckbox baselineCheck;
	private XButton lsButton;
	private XChoice baselineChoice;
	private int currentBaseline = 0;
	
	private DragParamsView theView;
	private FactorEstimatesView paramView;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 6));
			
		add("Center", displayPanel(data));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			bottomPanel.add("East", controlPanel(data));
			
				paramView = new FactorEstimatesView(data, this, "x", "ls", maxParam);
			
			bottomPanel.add("Center", paramView);
			
		add("South", bottomPanel);
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(Y_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
			
			CatVariable xVar = new CatVariable(getParameter(X_NAME_PARAM));
			xVar.readLabels(getParameter(X_LABELS_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
				
		data.addVariable("x", xVar);
		
			String[] keys = {"x"};
			maxParam = new NumValue(getParameter(MAX_PARAM_PARAM));
			int nLevels = xVar.noOfCategories();
			int[] decimals = new int[nLevels];
			for (int i=0 ; i<nLevels ; i++)
				decimals[i] = maxParam.decimals;
			MultipleRegnModel ls = new MultipleRegnModel("Model", data, keys);
			ls.setLSParams("y", decimals, 9);
		data.addVariable("ls", ls);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				NumVariable yVar = (NumVariable)data.getVariable("y");
				CatVariable xVar = (CatVariable)data.getVariable("x");
				
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.setCatLabels(xVar);
				xAxis.setAxisName(xVar.name);
				
				scatterPanel.add("Bottom", xAxis);
				
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
				yAxis.readNumLabels(labelInfo);
				yAxis.setAxisName(yVar.name);
				
				scatterPanel.add("Left", yAxis);
				
				theView = new DragParamsView(data, this, xAxis, yAxis, "x", "y", "ls",
																				MText.expandText("#mu#"), MText.expandText("#beta#"));
				theView.lockBackground(Color.white);
				theView.setFont(getBigFont());
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 40));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
			
				baselineCheck = new XCheckbox(translate("Baseline and offets"), this);
			choicePanel.add(baselineCheck);
			
				baselineChoice = new XChoice(translate("Baseline") + ":", XChoice.HORIZONTAL, this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
				for (int i=0 ; i<xVar.noOfCategories() ; i++)
					baselineChoice.addItem(xVar.getLabel(i).toString());
				
				baselineChoice.disable();
			
			choicePanel.add(baselineChoice);
			
		thePanel.add(choicePanel);
			
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == baselineCheck) {
			if (baselineCheck.getState()) {
				baselineChoice.enable();
				theView.setBaselineIndex(currentBaseline);
				paramView.setBaselineIndex(currentBaseline);
			}
			else {
				baselineChoice.disable();
				theView.setBaselineIndex(-1);
				paramView.setBaselineIndex(-1);
			}
			return true;
		}
		else if (target == lsButton) {
			MultipleRegnModel lsFit = (MultipleRegnModel)data.getVariable("ls");
			lsFit.updateLSParams("y");
			data.variableChanged("ls");
			return true;
		}
		else if (target == baselineChoice) {
			int newChoice = baselineChoice.getSelectedIndex();
			if (newChoice != currentBaseline) {
				currentBaseline = newChoice;
				theView.setBaselineIndex(newChoice);
				paramView.setBaselineIndex(newChoice);
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