package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import models.*;

import linMod.*;


public class PredictionErrorApplet extends SampleLSApplet {
	static final protected String ERROR_AXIS_PARAM = "errorAxis";
	static final private String X_LIMITS_PARAM = "xLimits";
//	static final private String PREDICTION_DECIMALS_PARAM = "predictionDecimals";
	
	static final private int kExtraSDDecimals = 3;
	
	static final private Color kPaleBlue = new Color(0x99CCFF);
	
	private JitterPlusNormalView errorView;
	private PredictionEqnView dataPrediction;
	private XValueSlider xSlider;
	
	private NumValue xMax, predictionMax;
	
	public void setupApplet() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(MAX_PARAM));
		paramLimits.nextToken();		//		First 2 parameters will be read later by SampleLSApplet
		paramLimits.nextToken();
		xMax = new NumValue(paramLimits.nextToken());
		predictionMax = new NumValue(paramLimits.nextToken());
		
		StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
		NumValue minX = new NumValue(theParams.nextToken());
		NumValue maxX = new NumValue(theParams.nextToken());
		NumValue xStep = new NumValue(theParams.nextToken());
		NumValue startX = new NumValue(theParams.nextToken());
		xSlider = new XValueSlider(minX, maxX, xStep, startX, this);
		xSlider.setForeground(Color.red);
		super.setupApplet();
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		PredictionVariable prediction = new PredictionVariable("prediction", summaryData,
													"intercept", "slope", xSlider, predictionMax.decimals);
		summaryData.addVariable("prediction", prediction);
		
		PredictionErrorVariable predictionError = new PredictionErrorVariable(translate("Difference from popn mean at x"),
					summaryData, "prediction", sourceData, "model", xSlider, predictionMax.decimals);
		summaryData.addVariable("error", predictionError);
		
		NormalDistnVariable errorDistn = new NormalDistnVariable("error distn");
		summaryData.addVariable("errorDistn", errorDistn);
		
		setTheoryParams(sourceData, summaryData);
		
		return summaryData;
	}
	
	protected void setTheoryParams(DataSet sourceData, SummaryDataSet summaryData) {
		LinearModel theory = (LinearModel)sourceData.getVariable("model");
//		NumValue modelSlope = theory.getSlope();
		NumValue modelIntercept = theory.getIntercept();
		double modelSD = theory.evaluateSD().toDouble();
		NumValue x0 = xSlider.getNumValue();
		
		NumVariable xVar = (NumVariable)sourceData.getVariable("x");
		ValueEnumeration xe = xVar.values();
		double sxx = 0.0;
		double sx = 0.0;
		int n = 0;
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			sx += x;
			sxx += x * x;
			n++;
		}
		double xMean = sx / n;
		sxx -= sx * xMean;
		double xOffset = x0.toDouble() - xMean;
		NumValue errorSD = new NumValue(Math.sqrt(1.0 / n + xOffset * xOffset / sxx) * modelSD,
																	modelIntercept.decimals + kExtraSDDecimals);
		
		NormalDistnVariable errorDistn = (NormalDistnVariable)summaryData.getVariable("errorDistn");
		errorDistn.setParams("0.0 " + errorSD.toString());
	}
	
	protected XPanel equationPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		return thePanel;
	}
	
	private XPanel jitteredPanel(DataSet data, String errorKey, String theoryKey,
															String axisParam, int densityDisplayType) {
		XPanel paramPanel = new XPanel();
		paramPanel.setLayout(new AxisLayout());
			
		HorizAxis horizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisParam);
		horizAxis.readNumLabels(labelInfo);
		NumVariable error = (NumVariable)data.getVariable(errorKey);
		horizAxis.setAxisName(error.name);
		paramPanel.add("Bottom", horizAxis);
		
		errorView = new JitterPlusNormalView(data, this, horizAxis, theoryKey, 1.0);
		errorView.lockBackground(Color.white);
		errorView.setActiveNumVariable(errorKey);
		errorView.setShowDensity(densityDisplayType);
		errorView.setDensityColor(kPaleBlue);
		paramPanel.add("Center", errorView);
		
		return paramPanel;
	}
	
	protected int initialTheoryDisplay() {
		return DataPlusDistnInterface.NO_DISTN;
	}
	
	protected XPanel bivarDisplayPanel(DataSet data, int viewType) {
		if (viewType == SAMPLE)
			return super.bivarDisplayPanel(data, viewType);
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(0, 20));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																				VerticalLayout.VERT_CENTER, 10));
			
				topPanel.add(xSlider);
				PredictionEqnView modelPrediction = new PredictionEqnView(this.data, this,
									null, null, "model", intMax, slopeMax, xMax, predictionMax, xSlider);
				topPanel.add(modelPrediction);
				
				dataPrediction = new PredictionEqnView(data, this,
									"intercept", "slope", null, intMax, slopeMax, xMax, predictionMax, xSlider);
				dataPrediction.setForeground(Color.blue);
				topPanel.add(dataPrediction);
			
			thePanel.add("North", topPanel);
			
			thePanel.add("Center", jitteredPanel(data, "error", "errorDistn",
															ERROR_AXIS_PARAM, initialTheoryDisplay()));
			return thePanel;
		}
	}
	
	protected DataView getSampleView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		LinePredictionView theView = new LinePredictionView(data, this, xAxis, yAxis, "x", "y", "model", xSlider);
		theView.setShowData(true);
		return theView;
	}
	
	public void setTheoryShow(boolean theoryShow) {
		errorView.setShowDensity(theoryShow ? DataPlusDistnInterface.CONTIN_DISTN
																			: DataPlusDistnInterface.NO_DISTN);
	}
	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			setTheoryParams(data, summaryData);
			errorView.repaint();
			dataPrediction.repaint();
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what)) {
			return true;
		}
		else
			return localAction(evt.target);
	}
}