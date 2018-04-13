package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import valueList.*;
import coreGraphics.*;
import distn.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class CoeffSampleApplet extends RotateApplet {
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	static final private String COEFF_AXIS_PARAM = "coeffAxis";
	
	static final private int kCoeffPanelHeight = 70;
	static final private String kCoeffKey[] = {"b0", "b1", "b2"};
	
	static final private Color kLightGrey = new Color(0x999999);
	
	private CoreModelDataSet data;
	private SummaryDataSet summaryData;
	
	private XButton takeSampleButton;
	private XCheckbox accumulateCheck, theoryCheck;
	
	private SamplePlanesView theView;
	private DataPlusDistnInterface coeffView[];
	
	private NumValue maxCoeff[];
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		addTheoryDistns(data, summaryData);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
		int nCoeffs = st.countTokens();
		maxCoeff = new NumValue[nCoeffs];
		int decimals[] = new int[nCoeffs];
		for (int i=0 ; i<nCoeffs ; i++) {
			maxCoeff[i]  = new NumValue(st.nextToken());
			decimals[i] = maxCoeff[i].decimals;
		}
		
		String xKeys[] = MultiRegnDataSet.xKeys;
		summaryData.addVariable("planes", new LSCoeffVariable("Planes", "ls",
																					xKeys, "y", null, decimals));
		
		for (int i=0 ; i<nCoeffs ; i++) {
			String coeffKey = kCoeffKey[i];
			String coeffName;
			if (i == 0)
				coeffName = translate("Intercept");
			else {
				NumVariable xVar = (NumVariable)sourceData.getVariable(xKeys[i - 1]);
				coeffName = translate("Slope for") + " " + xVar.name;
			}
			summaryData.addVariable(coeffKey, new SingleLSCoeffVariable(coeffName, summaryData,
																																						"planes", i));
		}
		
		return summaryData;
	}
	
	private void addTheoryDistns(DataSet data, SummaryDataSet summaryData) {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		double sigma = model.evaluateSD().toDouble();
		double variance = sigma * sigma;
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		double bVar[] = ls.getCoeffVariances("y", true, variance);
		
		
		for (int i=0 ; i<3 ; i++) {
			double biVar = bVar[(i + 1) * (i + 2) / 2 - 1];
			double biSD = Math.sqrt(biVar);
			NumValue biMean = model.getParameter(i);
			String biTheoryKey = kCoeffKey[i] + "Theory";
			
			NormalDistnVariable biTheory = new NormalDistnVariable(biTheoryKey);
			biTheory.setMean(biMean.toDouble());
			biTheory.setSD(biSD);
			biTheory.setDecimals(biMean.decimals, biMean.decimals + 2);
			
			summaryData.addVariable(biTheoryKey, biTheory);
		}
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			theView = new SamplePlanesView(summaryData, this, xAxis, yAxis, zAxis,
																			"planes", data, "model", MultiRegnDataSet.xKeys, "y", "ls");
			theView.lockBackground(Color.white);
								
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 8));
			rotatePanel.add(RotateButton.createRotationPanel(theView, this));
			
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(rotatePanel);
		
		thePanel.add(samplingPanel());
		
			XPanel showTheoryPanel = new XPanel();
			showTheoryPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				theoryCheck = new XCheckbox(translate("Show theory"), this);
			showTheoryPanel.add(theoryCheck);
			
		thePanel.add(showTheoryPanel);
		return thePanel;
	}
	
	private XPanel samplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		takeSampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		int nCoeffs = maxCoeff.length;
		coeffView = new DataPlusDistnInterface[nCoeffs];
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(999, nCoeffs * kCoeffPanelHeight));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new BorderLayout(5, 0));
			innerPanel.add("West", coeffValuePanel(summaryData, nCoeffs));
			innerPanel.add("Center", coeffDistnPanel(summaryData, nCoeffs));
		
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel coeffValuePanel(SummaryDataSet summaryData, int noToGo) {
		if (noToGo == 1)
			return oneCoeffValuePanel(summaryData, 0);
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout((noToGo - 1.0) / noToGo, 0, ProportionLayout.VERTICAL,
																											ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.TOP, coeffValuePanel(summaryData, noToGo - 1));
		thePanel.add(ProportionLayout.BOTTOM, oneCoeffValuePanel(summaryData, noToGo - 1));
		return thePanel;
	}
	
	private XPanel oneCoeffValuePanel(SummaryDataSet summaryData, int coeffIndex) {
		String coeffKey = kCoeffKey[coeffIndex];
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 0));
			OneValueView coeffValue = new OneValueView(summaryData, coeffKey, this, maxCoeff[coeffIndex]);
		thePanel.add(coeffValue);
		return thePanel;
	}
	
	private XPanel coeffDistnPanel(SummaryDataSet summaryData, int noToGo) {
		if (noToGo == 1)
			return oneCoeffDistnPanel(summaryData, 0);
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout((noToGo - 1.0) / noToGo, 0, ProportionLayout.VERTICAL,
																											ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.TOP, coeffDistnPanel(summaryData, noToGo - 1));
		thePanel.add(ProportionLayout.BOTTOM, oneCoeffDistnPanel(summaryData, noToGo - 1));
		return thePanel;
	}
	
	private XPanel oneCoeffDistnPanel(SummaryDataSet summaryData, int coeffIndex) {
		String coeffKey = kCoeffKey[coeffIndex];
		String theoryKey = coeffKey + "Theory";
		
		XPanel distnPanel = new XPanel();
		distnPanel.setLayout(new AxisLayout());
		
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(getParameter(COEFF_AXIS_PARAM + coeffIndex));
			
		distnPanel.add("Bottom", axis);
		
			StackedPlusNormalView coeffDistnView = new StackedPlusNormalView(summaryData, this, axis, theoryKey);
			coeffDistnView.setActiveNumVariable(coeffKey);
			coeffDistnView.lockBackground(Color.white);
			coeffDistnView.setShowDensity(StackedPlusNormalView.NO_DISTN);
			coeffView[coeffIndex] = coeffDistnView;
			
			NormalDistnVariable theoryDistn = (NormalDistnVariable)summaryData.getVariable(theoryKey);
			coeffDistnView.setDistnLabel(new LabelValue("N(" + theoryDistn.getMean().toString()
																		+ ", " + theoryDistn.getSD().toString() + ")"), kLightGrey);
			
		distnPanel.add("Center", coeffDistnView);
		
		return distnPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			theView.setDrawData(true);
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == theoryCheck) {
			int nCoeffs = maxCoeff.length;
			int newShowState = theoryCheck.getState() ? StackedPlusNormalView.CONTIN_DISTN
																									: StackedPlusNormalView.NO_DISTN;
			for (int i=0 ; i<nCoeffs ; i++)
				coeffView[i].setShowDensity(newShowState);
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