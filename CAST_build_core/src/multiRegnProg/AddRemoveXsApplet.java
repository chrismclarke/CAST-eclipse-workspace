package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import multiRegn.*;

public class AddRemoveXsApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final protected String MAX_TABLE_ENTRIES_PARAM = "maxTableEntries";
//	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final private String kXStart = "x";
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	
	private DataSet data;
	
	private String xKeys[];
	
	private String paramName[];
	private NumValue maxParam[];
	private int paramDecimals[];
	
	private CoefficientView coeffView[];
	private XCheckbox includeParamCheck[];
	private double constraints[] = null;
													//	allows some coefficients to be constrained to zero
	
	public void setupApplet() {
		data = readData();
		setDefaultConstraints();
		
		setLayout(new BorderLayout(10, 0));
		
		add("Center", coefficientPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
		int nx = 0;
		while (getParameter(kXStart + nx + kXNameEnd) != null)
			nx ++;
		xKeys = new String[nx];
		
		paramName = new String[nx + 1];
		paramName[0] = "intercept";
		StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
		maxParam = new NumValue[nx + 1];
		paramDecimals = new int[nx + 1];
		maxParam[0] = new NumValue(st.nextToken());
		paramDecimals[0] = maxParam[0].decimals;
		
		for (int i=0 ; i<nx ; i++) {
			paramName[i+1] = getParameter(kXStart + i + kXNameEnd);
			String values = getParameter(kXStart + i + kXValuesEnd);
			xKeys[i] = kXStart + i;
			data.addNumVariable(xKeys[i], paramName[i+1], values);
			maxParam[i+1] = new NumValue(st.nextToken());
			paramDecimals[i+1] = maxParam[i+1].decimals;
		}
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, xKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	private void setDefaultConstraints() {
		constraints = new double[paramName.length];
		for (int i=0 ; i<2 ; i++)
			constraints[i] = Double.NaN;
		for (int i=2 ; i<constraints.length ; i++)
			constraints[i] = 0.0;
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("ls");
		model.updateLSParams("y", constraints);
	}
	
	public void setConstraint(int paramIndex, boolean constrainedZero) {
		constraints[paramIndex] = constrainedZero ? 0.0 : Double.NaN;
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("ls");
		model.updateLSParams("y", constraints);
	}
	
	private XPanel coefficientPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 25));
		
			XPanel yPanel = new XPanel();
			yPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 0, 1));
			
				String yEquals = data.getVariable("y").name + " = ";
				CoefficientView interceptView = new CoefficientView(data, this, "ls", maxParam[0],
																																						0, yEquals, null);
			yPanel.add(interceptView);
			
				XPanel dummyPanel = new XPanel();		//	add panel with invisible XCheckbox to match spacing of parameters
				CardLayout dummyLayout = new CardLayout();
				dummyPanel.setLayout(dummyLayout);
				
				dummyPanel.add("blank", new XPanel());
				
				dummyPanel.add("dummyCheck", new XCheckbox("", this));
				
				dummyLayout.show(dummyPanel, "blank");
				
			yPanel.add(dummyPanel);
		
		thePanel.add(yPanel);
		
		includeParamCheck = new XCheckbox[xKeys.length];
		coeffView = new CoefficientView[xKeys.length];
		for (int i=0 ; i<xKeys.length ; i++) {
			XPanel paramPanel = new XPanel();
			paramPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 0, 1));
			
				String xName = data.getVariable(xKeys[i]).name;
				coeffView[i] = new CoefficientView(data, this, "ls", maxParam[i + 1],
																																						i + 1, " + ", xName);
				coeffView[i].setForeground(i == 0 ? Color.black : Color.gray);
			paramPanel.add(coeffView[i]);
			
				includeParamCheck[i] = new XCheckbox("", this);
				includeParamCheck[i].setState(i == 0);
			
			paramPanel.add(includeParamCheck[i]);
			
			thePanel.add(paramPanel);
		}
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		for (int i = 0 ; i<includeParamCheck.length ; i++)
			if (target == includeParamCheck[i]) {
				setConstraint(i + 1, !includeParamCheck[i].getState());
				coeffView[i].setForeground(includeParamCheck[i].getState() ? Color.black : Color.gray);
				data.variableChanged("ls");
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}