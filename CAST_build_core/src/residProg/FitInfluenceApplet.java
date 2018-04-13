package residProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import models.*;

import resid.*;


public class FitInfluenceApplet extends XApplet {
	static final protected String X_AXIS_INFO_PARAM = "xAxis";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String FIT_CHANGE_AXIS_PARAM = "fitChangeAxis";
	static final private String DFITS_AXIS_PARAM = "dfitsAxis";
	static final private String MAX_PARAM_PARAM = "maxParam";
	
	protected DataSet data;
	
	private XChoice influenceChoice;
	private int currentInfluence = 0;
	
	protected MultiVertAxis influenceVertAxis;
	protected HiliteOneResidualView influenceView;
	
	protected NumValue maxIntercept, maxSlope;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout());
		
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
			displayPanel.add(ProportionLayout.LEFT, dataPanel(data));
			displayPanel.add(ProportionLayout.RIGHT, influencePanel(data));
		
		add("Center", displayPanel);
		add("South", controlPanel(data));
	}
	
	protected DataSet readCoreData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
			String maxParamString = getParameter(MAX_PARAM_PARAM);
			if (maxParamString != null) {
				StringTokenizer st = new StringTokenizer(maxParamString);
				maxIntercept = new NumValue(st.nextToken());
				maxSlope = new NumValue(st.nextToken());
			}
			
			LinearModel lsLine = new LinearModel("LS line", data, "x", maxIntercept, maxSlope);
			lsLine.updateLSParams("y");
		data.addVariable("ls", lsLine);
		
			LinearModel deletedLS = new LinearModel("Deleted LS", data, "x", maxIntercept, maxSlope);
			deletedLS.updateLSParams("y");
		data.addVariable("deletedLS", deletedLS);
		return data;
	}
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
			DeletedChangeVariable fitChangeVar = new DeletedChangeVariable("Change in fit", data,
															"y", "x", "ls", "deletedLS", DeletedChangeVariable.FIT, 9);
		data.addVariable("fitChange", fitChangeVar);
		
			DeletedSDVariable deletedSD = new DeletedSDVariable("Deleted sd", data, "y",
																																					"deletedLS");
		data.addVariable("deletedSD", deletedSD);
		
			ExtStudentResidVariable tResidVar = new ExtStudentResidVariable("Ext student resid", data,
																					"x", "y", "ls", "deletedLS", 9);
		data.addVariable("tResid", tResidVar);
		
			FitInfluenceVariable dfitsVar = new FitInfluenceVariable("DFITS", data, "x", "tResid",
																																										"ls", 9);
		data.addVariable("dfits", dfitsVar);
		
		return data;
	}
	
	protected boolean showFitOnDataPlot() {
		return true;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				NumVariable xVar = (NumVariable)data.getVariable("x");
				horizAxis.setAxisName(xVar.name);
			plotPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			plotPanel.add("Left", vertAxis);
			
				DeleteChangeView theView = new DeleteChangeView(data, this, horizAxis, vertAxis,
																																"x", "y", "ls", "deletedLS");
				theView.setDrawFitChange(showFitOnDataPlot());
				theView.setRetainLastSelection(true);
				theView.lockBackground(Color.white);
			plotPanel.add("Center", theView);
		
		thePanel.add("Center", plotPanel);
				
			NumVariable yVar = (NumVariable)data.getVariable("y");
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(vertAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	protected XPanel influencePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				influenceChoice = new XChoice(this);
				influenceChoice.addItem(translate("Change in fitted value"));
				influenceChoice.addItem(translate("DFITS"));
			choicePanel.add(influenceChoice);
		
		thePanel.add("North", choicePanel);
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				NumVariable xVar = (NumVariable)data.getVariable("x");
				horizAxis.setAxisName(xVar.name);
			plotPanel.add("Bottom", horizAxis);
			
				influenceVertAxis = new MultiVertAxis(this, 2);
				influenceVertAxis.setChangeMinMax(true);
				influenceVertAxis.readNumLabels(getParameter(FIT_CHANGE_AXIS_PARAM));
				influenceVertAxis.readExtraNumLabels(getParameter(DFITS_AXIS_PARAM));
			plotPanel.add("Left", influenceVertAxis);
			
				influenceView = new HiliteOneResidualView(data, this, horizAxis, influenceVertAxis, "x", "fitChange", null);
				influenceView.setRetainLastSelection(true);
				influenceView.lockBackground(Color.white);
			plotPanel.add("Center", influenceView);
		
		thePanel.add("Center", plotPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == influenceChoice) {
			int newChoice = influenceChoice.getSelectedIndex();
			if (newChoice != currentInfluence) {
				currentInfluence = newChoice;
				influenceVertAxis.setAlternateLabels(newChoice);
				influenceView.changeVariables(newChoice == 0 ? "fitChange" : "dfits", "x");
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