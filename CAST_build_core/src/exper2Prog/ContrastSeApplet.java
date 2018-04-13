package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import exper2.*;


public class ContrastSeApplet extends XApplet {
	static final private String CONTROL_CONTRAST_PARAM = "controlContrast";
	static final private String CONTROL_NAME_PARAM = "controlContrastName";
	static final private String OTHER_CONTRAST_PARAM = "otherContrast";
	static final private String OTHER_NAME_PARAM = "otherContrastName";
	static final private String N_UNITS_PARAM = "nUnits";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String MAX_SE_PARAM = "maxSe";
	
	static final private Color kContrastGroupColor = new Color(0x000099);
	
	private DataSet data;
	
	private ParameterSlider controlRepsSlider;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		add("North", controlPanel(data));
			
		add("Center", displayPanel(data));
		
		setRepsFromSlider();
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readLabels(getParameter(X_LABELS_PARAM));
		
		data.addVariable("x", xVar);
		
			String xKeys[] = {"x"};
			MultipleRegnModel model = new MultipleRegnModel("Model", data, xKeys);
		
		data.addVariable("model", model);
		
		return data;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(50, 0));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				int nValues = Integer.parseInt(getParameter(N_UNITS_PARAM));
				
				CatVariable xVar = (CatVariable)data.getVariable("x");
				int nTreats = xVar.noOfCategories();
				
				int maxNonControl = (nValues - 1) / (nTreats - 1);
				int minControl = nValues - (nTreats - 1) * maxNonControl;
				int maxControl = nValues - (nTreats - 1);
				int startControl = nValues / nTreats;
				int controlSteps = (maxControl - minControl) / (nTreats - 1);
				
				controlRepsSlider = new ParameterSlider(new NumValue(minControl, 0), new NumValue(maxControl, 0),
																		new NumValue(startControl, 0), controlSteps, translate("Reps for control"),
																		ParameterSlider.NO_SHOW_MIN_MAX, this);
			sliderPanel.add(controlRepsSlider);
			
		thePanel.add("Center", sliderPanel);
		
			XPanel repsPanel = new XPanel();
			repsPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
			RepsTableView repsTable = new RepsTableView(data, this, "x");
			repsTable.setFont(getBigFont());
			repsPanel.add(repsTable);
		
		thePanel.add("East", repsPanel);
		
		return thePanel;
	}
	
	private XPanel contrastPanel(DataSet data, String groupNameParam, String contrastParamCore,
																																						NumValue maxSe) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
			XLabel groupLabel = new XLabel(getParameter(groupNameParam), XLabel.CENTER, this);
			groupLabel.setFont(getBigBoldFont());
			groupLabel.setForeground(kContrastGroupColor);
		thePanel.add(groupLabel);
		
			XPanel sePanel = new XPanel();
			sePanel.setLayout(new FlexGridLayout(2, 20, 3));
			
				XLabel col1Label = new XLabel(translate("Contrast"), XLabel.CENTER, this);
				col1Label.setFont(getStandardBoldFont());
			sePanel.add(col1Label);
			
				XLabel col2Label = new XLabel(translate("SE(contrast)"), XLabel.CENTER, this);
				col2Label.setFont(getStandardBoldFont());
			sePanel.add(col2Label);
				
			
			int index = 0;
			while (true) {
				String contrastString = getParameter(contrastParamCore + (index++));
				if (contrastString == null)
					break;
				StringTokenizer st = new StringTokenizer(contrastString, "#");
				String contrastName = st.nextToken();
				
				XLabel contrastLabel = new XLabel(contrastName, XLabel.CENTER, this);
				sePanel.add(contrastLabel);
				
				StringTokenizer st2 = new StringTokenizer(st.nextToken());
				double contrast[] = new double[st2.countTokens()];
				for (int i=0 ; i<contrast.length ; i++)
					contrast[i] = Double.parseDouble(st2.nextToken());
				
				ContrastSeView contrastSe = new ContrastSeView(data, "model", contrast,
																												null, maxSe, this);
				
				sePanel.add(contrastSe);
			}
		
		thePanel.add(sePanel);
		
		return thePanel;
	
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		thePanel.setLayout(new EqualSpacingLayout(EqualSpacingLayout.HORIZONTAL, 20));
		
		NumValue maxSe = new NumValue(getParameter(MAX_SE_PARAM));
		
		thePanel.add(contrastPanel(data, CONTROL_NAME_PARAM, CONTROL_CONTRAST_PARAM, maxSe));
		
		thePanel.add(contrastPanel(data, OTHER_NAME_PARAM, OTHER_CONTRAST_PARAM, maxSe));
		
		return thePanel;
	}
	
	private void setRepsFromSlider() {
		int controlReps = (int)Math.round(controlRepsSlider.getParameter().toDouble());
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int nValues = Integer.parseInt(getParameter(N_UNITS_PARAM));
		int nTreats = xVar.noOfCategories();
		int otherReps = (nValues - controlReps) / (nTreats - 1);
		
		String repString = controlReps + "@0";
		for (int i=1 ; i<nTreats ; i++)
			repString += " " + otherReps + "@" + i;
			
		xVar.readValues(repString);
		data.variableChanged("x");
	}
	
	private boolean localAction(Object target) {
		if (target == controlRepsSlider) {
			setRepsFromSlider();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}