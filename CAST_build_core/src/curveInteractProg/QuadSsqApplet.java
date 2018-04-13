package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import formula.*;
import graphics3D.*;

import multivarProg.*;
import ssq.*;
import curveInteract.*;


public class QuadSsqApplet extends RotateApplet {
	static final private String COMP_NAME_PARAM = "compName";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final protected String kQuadXKeys[] = {"x", "z", "xx", "zz"};
	static final protected String kLSKeys[] = {"lsMean", "lsXZ", "lsX2Z"};
	static final protected int kModelParams[] = {1, 3, 4};
	
	static final private int kParamDecimals[] = {9, 9, 9, 9, 9};
	
	static final private double kMeanConstraints[] = {Double.NaN, 0.0, 0.0, 0.0, 0.0};
	static final private double kXZConstraints[] = {Double.NaN, Double.NaN, Double.NaN, 0.0, 0.0};
	static final private double kX2ZConstraints[] = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0.0};
//	static final private double kXZ2Constraints[] = {Double.NaN, Double.NaN, Double.NaN, 0.0, Double.NaN};
	
	static final protected Color kMeanModelColor = new Color(0xCCCCCC);
	static final protected Color kLinXZModelColor = new Color(0x66CCFF);
	static final protected Color kQuadModelColor = new Color(0xFF6666);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private NumValue maxSsq;
	
	private ComponentEqnPanel theEquation;
	
	private XChoice componentChoice;
	private int currentComponent = 0;
	
	protected DataSet readData() {
		data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
			NumVariable zVar = new NumVariable(getParameter(Z_VAR_NAME_PARAM));
			zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		
		data.addVariable("xx", new QuadraticVariable(xVar.name + "-sqr", xVar, 0.0, 0.0, 1.0, 9));
		data.addVariable("zz", new QuadraticVariable(zVar.name + "-sqr", zVar, 0.0, 0.0, 1.0, 9));
		
		MultipleRegnModel lsMean = new MultipleRegnModel("model", data, kQuadXKeys);
		lsMean.setLSParams("y", kMeanConstraints, kParamDecimals, 9);
		data.addVariable("lsMean", lsMean);
		
		MultipleRegnModel lsXZ = new MultipleRegnModel("model", data, kQuadXKeys);
		lsXZ.setLSParams("y", kXZConstraints, kParamDecimals, 9);
		data.addVariable("lsXZ", lsXZ);
		
		MultipleRegnModel lsX2Z = new MultipleRegnModel("model", data, kQuadXKeys);
		lsX2Z.setLSParams("y", kX2ZConstraints, kParamDecimals, 9);
		data.addVariable("lsX2Z", lsX2Z);
		
//		MultipleRegnModel lsXZ2 = new MultipleRegnModel("model", data, kQuadXKeys);
//		lsXZ2.setLSParams("y", kXZ2Constraints, kParamDecimals, 9);
//		data.addVariable("lsXZ2", lsXZ2);
		
		StringTokenizer st = new StringTokenizer(getParameter(COMP_NAME_PARAM), "#");
		String compName[] = new String[4];
		for (int i=0 ; i<4 ; i++)
			compName[i] = st.nextToken();
		SsqDiffComponentVariable.addComponentsToData(data, kQuadXKeys,
																				"y", kLSKeys, compName, kModelParams, 9);
		
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		summaryData = new AnovaSummaryData(data, "error",
													SsqDiffComponentVariable.getComponentKeys(4), maxSsq.decimals, 4);
		summaryData.setSingleSummaryFromData();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		
			componentChoice = new XChoice(this);
			componentChoice.addItem(translate("Total"));
			componentChoice.addItem(translate("Explained by linear X & Z"));
			componentChoice.addItem(translate("Explained by quadratic X"));
			componentChoice.addItem(translate("Residual"));
			
		thePanel.add(componentChoice);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(data.getVariable("x").name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(data.getVariable("y").name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(data.getVariable("z").name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			theView = new SeqQuadPlanesView(data, this, xAxis, yAxis, zAxis, "lsMean", "lsXZ", "lsX2Z", kMeanModelColor,
								kLinXZModelColor, kQuadModelColor, SsqDiffComponentVariable.getComponentColors(4));
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
			
			AnovaImages.loadQuadXZImages(this);
			
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			theEquation = new ComponentEqnPanel(summaryData,
								SsqDiffComponentVariable.getComponentKeys(4), maxSsq, AnovaImages.quadXZSsqs,
								SsqDiffComponentVariable.getComponentColors(4), AnovaImages.kQuadXZSsqWidth,
								AnovaImages.kQuadXZSsqHeight, bigContext);
			
			thePanel.add(theEquation);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == componentChoice) {
			int newChoice = componentChoice.getSelectedIndex();
			if (newChoice != currentComponent) {
				currentComponent = newChoice;
				if (newChoice == 0)
					((SeqQuadPlanesView)theView).setComponentType(SeqQuadPlanesView.DATA_TO_M0);
				else if (newChoice == 1)
					((SeqQuadPlanesView)theView).setComponentType(SeqQuadPlanesView.M0_TO_M1);
				else if (newChoice == 2)
					((SeqQuadPlanesView)theView).setComponentType(SeqQuadPlanesView.M1_TO_M2);
				else
					((SeqQuadPlanesView)theView).setComponentType(SeqQuadPlanesView.DATA_TO_M2);
				theView.repaint();
				theEquation.highlightComponent(newChoice);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}