package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;

import regn.*;
import regnView.*;


public class GroupLinesApplet extends XApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	
	static final private String SHORT_NAMES_PARAM = "shortNames";
	static final private String MAX_PARAMS_PARAM = "maxParams";
	
	static final private String X_AXIS_INFO_PARAM = "horizAxis";
	static final private String Y_AXIS_INFO_PARAM = "vertAxis";
	
	static final private Color kGroupColor[] = {Color.black, Color.red, Color.blue, Color.green};
	
	static final private Color kEqnBackground = new Color(0xFFEEBB);
	static final private Color kGroupLabelColor = new Color(0x000066);
	
	private NumValue maxIntercept, maxSlope;
	
	private ScatterGroupsView theView;
	
	private XChoice modelChoice;
	private int currentModelIndex = 0;
	
	private String[] groupModelKey;
	private LinearEquationView groupEqn[];
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(10, 3));
		add("Center", displayPanel(data));
		add("South", equationPanel(data));
	}
	
	private DataSet readData() {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAMS_PARAM));
		maxIntercept = new NumValue(st.nextToken());
		maxSlope = new NumValue(st.nextToken());
		
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
			CatVariable zVar = new CatVariable(getParameter(Z_VAR_NAME_PARAM));
			zVar.readLabels(getParameter(Z_LABELS_PARAM));
			zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		
			int interceptDecimals = maxIntercept.decimals;
			int slopeDecimals = maxSlope.decimals;
		
			LinearModel model = new LinearModel("model", data, "x");
			model.setLSParams("y", interceptDecimals, slopeDecimals, 9);
		data.addVariable("model", model);
		
		groupModelKey = new String[zVar.noOfCategories()];
		for (int i=0 ; i<groupModelKey.length ; i++) {
				String yGroupKey = "y" + i;
				FilterNumVariable yGroupVar = new FilterNumVariable(yGroupKey, data, "y", "z");
				yGroupVar.setFilterIndex(i);
			data.addVariable(yGroupKey, yGroupVar);
			
				groupModelKey[i] = "model" + i;
				model = new LinearModel("model", data, "x");
				model.setLSParams(yGroupKey, interceptDecimals, slopeDecimals, 9);
			data.addVariable(groupModelKey[i], model);
		}
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				horizAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
			scatterPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			scatterPanel.add("Left", vertAxis);
			
				theView = new ScatterGroupsView(data, this,horizAxis, vertAxis, "x", "y");
				theView.setColouredCats(false);
				theView.setModels("model", groupModelKey);
				theView.lockBackground(Color.white);
			scatterPanel.add("Center", theView);
		
		thePanel.add("Center", scatterPanel);
		
			XLabel yNameLabel = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yNameLabel.setFont(horizAxis.getFont());
		
		thePanel.add("North", yNameLabel);
		
		return thePanel;
	}
	
	private XPanel equationPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 12));
			
			modelChoice = new XChoice(this);
			modelChoice.addItem(translate("Common line"));
			modelChoice.addItem(translate("Separate lines"));
			
		thePanel.add(modelChoice);
		
			XPanel eqnPanel = new InsetPanel(10, 5);
			eqnPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 10));
			
			StringTokenizer st = new StringTokenizer(getParameter(SHORT_NAMES_PARAM));
			String yName = st.nextToken();
			String xName = st.nextToken();
			
			CatVariable groupVar = (CatVariable)data.getVariable("z");
			groupEqn = new LinearEquationView[groupModelKey.length];
			for (int i=0 ; i<groupModelKey.length ; i++) {
				XPanel groupPanel = new XPanel();
				groupPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				
					XLabel groupLabel = new XLabel(groupVar.getLabel(i).toString(), XLabel.LEFT, this);
					groupLabel.setFont(getBigBoldFont());
					groupLabel.setForeground(kGroupLabelColor);
				
				groupPanel.add(groupLabel);
				
					groupEqn[i] = new LinearEquationView(data, this, "model", yName, xName,
																		maxIntercept, maxIntercept, maxSlope, maxSlope);
					groupEqn[i].setFont(getBigFont());
					Color groupColor = kGroupColor[i % kGroupColor.length];
					groupEqn[i].setForeground(groupColor);
				
				groupPanel.add(groupEqn[i]);
				
				eqnPanel.add(groupPanel);
			}
		
			eqnPanel.lockBackground(kEqnBackground);
			
		thePanel.add(eqnPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int newChoice = modelChoice.getSelectedIndex();
			if (newChoice != currentModelIndex) {
				currentModelIndex = newChoice;
				
				for (int i=0 ; i<groupModelKey.length ; i++) {
					String modelKey = (newChoice == 0) ? "model" : groupModelKey[i];
					groupEqn[i].setModelKey(modelKey);
					groupEqn[i].repaint();
				}
				
				theView.setColouredCats(newChoice != 0);
				theView.repaint();
				
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}