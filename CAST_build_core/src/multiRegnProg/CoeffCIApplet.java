package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import valueList.*;
import graphics3D.*;

import inference.*;
import multivarProg.*;
import multiRegn.*;


public class CoeffCIApplet extends RotateApplet {
	static final private String COEFF_AXIS_PARAM = "coeffAxis";
	static final private String CI_DECIMALS_PARAM = "ciDecimals";
	static final protected String MAX_COEFF_PARAM = "maxCoeff";
	static final private String OVERALL_MAX_COEFF_PARAM = "overallMaxCoeff";
	
	static final private int kCIPanelHeight = 220;
	
	protected MultiRegnDataSet data;
	protected SummaryDataSet summaryData;
	
	private Model3ResidView theView;
	private OneValueView ciValueView;
	protected D3Axis xAxis, yAxis, zAxis;
	
	private XButton spinButton;
	protected RepeatingButton sampleButton;
	private XCheckbox accumulateCheck;
	private XChoice coeffChoice;
	private int currentCoeffChoice;
	
	private String coeffKey[] = new String[3];
	protected String coeffName[] = new String[3];
	protected String xVarName[] = new String[2];
	protected String yVarName;
	
	private XPanel ciDisplayPanel;
	private CardLayout ciPanelLayout;
	
	protected NumValue maxCoeff[];
	
	private CoverageValueView coverage;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		setCoeffNames();
		
		summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		return data;
	}
	
	protected void setCoeffNames() {
		coeffName[0] = translate("Intercept");
		coeffName[1] = translate("Slope for") + " " + data.getVariable("x").name;
		coeffName[2] = translate("Slope for") + " " + data.getVariable("z").name;
		
		xVarName[0] = data.getVariable("x").name;
		xVarName[1] = data.getVariable("z").name;
		yVarName = data.getVariable("y").name;
	}
	
	protected SummaryDataSet getSummaryData(MultiRegnDataSet data) {
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error");
		
		StringTokenizer st = new StringTokenizer(getParameter(CI_DECIMALS_PARAM));
		NumVariable yVar = (NumVariable)data.getVariable("y");
		int n = yVar.noOfValues();
		
		for (int i=0 ; i<3 ; i++) {
			int decimals = Integer.parseInt(st.nextToken());
			CoeffCIVariable ciVar = new CoeffCIVariable(translate("CI") + ": " + coeffName[i], 0.95, n - 3, "ls", "y",
																												i, decimals);
			coeffKey[i] = "ciForB" + i;
			summaryData.addVariable(coeffKey[i], ciVar);
		}
		
		maxCoeff = new NumValue[3];
		st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
		for (int i=0 ; i<3 ; i++)
			maxCoeff[i] = new NumValue(st.nextToken());
		
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XLabel modelLabel = new XLabel(translate("Model") + ": ", XLabel.LEFT, this);
			modelLabel.setFont(getStandardBoldFont());
		thePanel.add(modelLabel);
		thePanel.add(new MultiLinearEqnView(data, this, "model", yVarName, xVarName, maxCoeff, maxCoeff));
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(regnData.getXAxisInfo());
			yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(regnData.getYAxisInfo());
			zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(regnData.getZAxisInfo());
			
			theView = new Model3ResidView(data, this, xAxis, yAxis, zAxis, "ls",
																		MultiRegnDataSet.xKeys, "y");
			theView.setSelectCrosses(false);
			theView.setShowSelectedArrows(false);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel rotateControlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
		thePanel.add(RotateButton.createRotationPanel(theView, this));
		
			spinButton = new XButton(translate("Spin"), this);
		thePanel.add(spinButton);
		return thePanel;
	}
	
	protected void addDescription(DataSet data, XPanel thePanel) {
		coverage = new CoverageValueView(summaryData, getCIKey(1), this, getTarget(data, 1));
		thePanel.add(coverage);
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 20));
		
		thePanel.add(rotateControlPanel());
		
			XPanel samplePanel = getSamplePanel();
		if (samplePanel != null)
			thePanel.add(samplePanel);
		
		addDescription(data, thePanel);
		
		return thePanel;
	}
	
	private String getCIKey(int i) {
		return "ciForB" + i;
	}
	
	private NumValue getTarget(DataSet data, int i) {
		MultipleRegnModel regnModel = (MultipleRegnModel)data.getVariable("model");
		return regnModel.getParameter(i);
	}
	
	protected XPanel getSamplePanel() {
		XPanel samplePanel = new XPanel();
		samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		samplePanel.add(sampleButton);
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		samplePanel.add(accumulateCheck);
		
		return samplePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(999, kCIPanelHeight));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new BorderLayout(20, 0));
			
			innerPanel.add("West", ciChoicePanel(summaryData, coeffName, coeffKey));
			innerPanel.add("Center", getCIDisplayPanel(summaryData, coeffName, coeffKey));
		
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel ciChoicePanel(SummaryDataSet summaryData, String[] coeffName,
																																String[] coeffKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 16));
			
			coeffChoice = new XChoice(this);
			for (int i=0 ; i<coeffName.length ; i++)
				coeffChoice.addItem(coeffName[i]);
			coeffChoice.select(1);
			currentCoeffChoice = 1;
		thePanel.add(coeffChoice);
		
			String maxCoeff = getParameter(OVERALL_MAX_COEFF_PARAM);
			LabelValue maxValue = new LabelValue(maxCoeff + " " + translate("to") + " " + maxCoeff);
			
			ciValueView = new OneValueView(summaryData, coeffKey[currentCoeffChoice], this, maxValue);
			ciValueView.setLabel("95% " + translate("CI") + " =");
		
		thePanel.add(ciValueView);
		return thePanel;
	}
	
	private XPanel getCIDisplayPanel(SummaryDataSet summaryData, String[] coeffName,
																																String[] coeffKey) {
		ciDisplayPanel = new XPanel();
		ciPanelLayout = new CardLayout();
		ciDisplayPanel.setLayout(ciPanelLayout);
		
		for (int i=0 ; i<coeffKey.length ; i++) {
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis ciAxis = new HorizAxis(this);
				ciAxis.readNumLabels(getParameter(COEFF_AXIS_PARAM + i));
				Variable x = (Variable)summaryData.getVariable(coeffKey[i]);
				ciAxis.setAxisName(x.name);
			innerPanel.add("Bottom", ciAxis);
				
				MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
				NumValue target = model.getParameter(i);
				IntervalView ciView = new IntervalView(summaryData, this, ciAxis, coeffKey[i], target);
				ciView.lockBackground(Color.white);
				
			innerPanel.add("Center", ciView);
			
			ciDisplayPanel.add(coeffName[i], innerPanel);
		}
		
		ciPanelLayout.show(ciDisplayPanel, coeffName[currentCoeffChoice]);
		
		return ciDisplayPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == spinButton) {
			theView.startAutoRotation();
			return true;
		}
		else if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == coeffChoice) {
			int newChoice = coeffChoice.getSelectedIndex();
			if (newChoice != currentCoeffChoice) {
				currentCoeffChoice = newChoice;
				ciValueView.setVariableKey(coeffKey[newChoice]);
				ciValueView.repaint();
				ciPanelLayout.show(ciDisplayPanel, coeffName[newChoice]);
				
				coverage.setTarget(getTarget(data, newChoice));
				coverage.setVariableKey(getCIKey(newChoice));
				coverage.redrawAll();
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