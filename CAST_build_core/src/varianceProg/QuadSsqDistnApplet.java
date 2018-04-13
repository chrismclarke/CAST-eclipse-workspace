package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import models.*;

import variance.*;
import ssq.*;
import ssqProg.*;


public class QuadSsqDistnApplet extends CoreSsqDistnApplet {
	static final private String MAX_SLOPE_CURVE_PARAM = "maxSlopeCurve";
	
	static final private int kSliderSteps = 100;
	
	static final private Color kDarkBlue = new Color(0x000099);
	static final private Color kDarkRed = new Color(0x990000);
	
	private double slopeMax, curveMax;
	private XNoValueSlider linearSlopeSlider, curveSlopeSlider;
	
	private double xMean, zMean;
	
	protected CoreModelDataSet readData() {
		QuadRegnDataSet data = new QuadRegnDataSet(this);
		
		QuadComponentVariable.addComponentsToData(data, "x", "y",
																				QuadRegnDataSet.kLinLsKey, QuadRegnDataSet.kQuadLsKey);
		
		return data;
	}
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		AnovaSummaryData summaryData;
		if (maxMeanSsq != null && maxF != null)
			summaryData = new AnovaSummaryData(sourceData, "error", QuadComponentVariable.kComponentKey,
									maxSsq.decimals, kMaxRSquared.decimals, maxMeanSsq.decimals, maxF.decimals);
		else
			summaryData = new AnovaSummaryData(sourceData, "error", QuadComponentVariable.kComponentKey,
									maxSsq.decimals, kMaxRSquared.decimals);
			
			GammaDistnVariable chiSquared = new GammaDistnVariable("chiSqr");
		summaryData.addVariable("chiSquared", chiSquared);
		
			adjustDF(summaryData);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	protected void adjustDF(AnovaSummaryData summaryData) {
		CoreModelVariable model = (CoreModelVariable)data.getVariable("model");
		double errorSD = model.evaluateSD().toDouble();
		
		GammaDistnVariable chiSquared = (GammaDistnVariable)summaryData.getVariable("chiSquared");
		chiSquared.setScale(2.0 * errorSD);
		chiSquared.setShape(0.5);
		
		SsqStackedView chi2DistnView = (SsqStackedView)ssqView;
		if (chi2DistnView != null) {
			chi2DistnView.setChi2Df();
			summaryData.variableChanged("chiSquared");
		}
	}
	
	protected int componentType() {
		return QuadComponentVariable.QUADRATIC;
	}
	
	protected DataWithComponentsPanel dataPanel(DataSet data) {
		QuadraticComponentsPanel dataView = new QuadraticComponentsPanel(this);
		dataView.setupPanel(data, "x", "y", QuadRegnDataSet.kLinLsKey, QuadRegnDataSet.kQuadLsKey,
																											"model", componentType(), this);
		return dataView;
	}
	
	protected XPanel ssqAndDistnPanel(AnovaSummaryData summaryData) {
		return ssqPanel(summaryData);
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
		String explString = "anova/sigma2.gif";
		
		SsqStackedView theView = new SsqStackedView(summaryData, this,
							axis, "chiSquared", explString, kRegnMeanSsqColor);
		theView.setActiveNumVariable(QuadComponentVariable.kComponentKey[2]);	//	quadratic
		
		axis.setForeground(QuadComponentVariable.kQuadraticColor);
		axis.setAxisName(translate("Quadratic sum of squares"));
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return QuadComponentVariable.kQuadraticColor;
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = super.controlPanel(data, summaryData);
		
		if (showSsqValue())
			thePanel.add(getSsqValueView(summaryData, QuadComponentVariable.kComponentKey[2],
															"xEquals/quadraticSsq.png", QuadComponentVariable.kQuadraticColor));
		thePanel.add(getSampleSizePanel());
		
		thePanel.add(sliderPanel(data));
		thePanel.add(getSampleButton(sampleButtonText()));
		return thePanel;
	}
	
	protected String sampleButtonText() {
		return translate("Take sample");
	}
	
	protected boolean showSsqValue() {
		return true;
	}
	
	private XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
		
			StringTokenizer st = new StringTokenizer(getParameter(MAX_SLOPE_CURVE_PARAM));
			slopeMax = Double.parseDouble(st.nextToken());
			curveMax = Double.parseDouble(st.nextToken());
			linearSlopeSlider = new XNoValueSlider(translate("Zero"), null, translate("Model slope"), 0, kSliderSteps, 0, this);
			linearSlopeSlider.setForeground(kDarkBlue);
			
			curveSlopeSlider = new XNoValueSlider(translate("Zero"), null, translate("Model curvature"), 0, kSliderSteps, 0, this);
			curveSlopeSlider.setForeground(kDarkRed);
			
		thePanel.add(linearSlopeSlider);
		thePanel.add(curveSlopeSlider);
		
		NumVariable xVar = (NumVariable)data.getVariable("x");
		ValueEnumeration xe = xVar.values();
		double sx = 0.0;
		double sxx = 0.0;
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			sx += x;
			sxx += x * x;
		}
		int n = xVar.noOfValues();
		xMean = sx / n;
		zMean = sxx / n - xMean * xMean;
		QuadraticModel yDistn = (QuadraticModel)data.getVariable("model");
		yDistn.setSlope(0.0);						//	slope and curvature should already be zero from
		yDistn.setCurvature(0.0);				//	applet parameters
		
		yPivot = yDistn.evaluateMean(xPivot);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == linearSlopeSlider || target == curveSlopeSlider) {
			double newSlope = slopeMax * linearSlopeSlider.getValue() / kSliderSteps;
			double newCurve = curveMax * curveSlopeSlider.getValue() / kSliderSteps;
			
			double beta2 = newCurve;
			double beta1 = (newSlope - 2.0 * newCurve * xMean);
			double beta0 = yPivot - newSlope * xMean + newCurve * (xMean * xMean - zMean);
			QuadraticModel yDistn = (QuadraticModel)data.getVariable("model");
			yDistn.setCurvature(beta2);
			yDistn.setSlope(beta1);
			yDistn.setIntercept(beta0);
			
			data.variableChanged("model");
			summaryData.setSingleSummaryFromData();
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