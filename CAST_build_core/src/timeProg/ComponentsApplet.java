package timeProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import imageUtils.*;

import time.*;


public class ComponentsApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	static final protected String SEASON_PARAM = "seasonName";
	static final private String SEASON_INFO_PARAM = "seasonInfo";
	static final protected String TIME_NAME_PARAM = "timeAxisName";
	static final protected String TIME_SEQUENCE_PARAM = "timeSequence";
	static final private String DECIMALS_PARAM = "decimals";
//	static final private String SHOW_PREDICTION_PARAM = "showPrediction";
	static final private String USE_SMALL_FONT_PARAM = "smallAxisFont";
	static final private String RESID_CHECK_PARAM = "showResidComponent";
	static final private String CYCLIC_CHECK_PARAM = "showCyclicComponent";
	
	static final private int SEASONAL = 0;
	static final private int TREND = 1;
	static final private int CYCLIC = 2;
	static final private int RESIDUAL = 3;
	
	static final private String[] kComponentKeys = {"seasonalEffect", "trend", "cyclic", "resid"};
	
	static final private Color kCheckBackground = new Color(0xEEEEEE);
	
	protected DataSet data;
	private ComponentSumVariable topComponentVar, bottomComponentVar;
	protected int decimals;
	
	protected SeasonTimeAxis oneTimeAxis = null;
	
	private XCheckbox componentCheck[] = new XCheckbox[4];
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 10));
			
			topPanel.add("Center", timeSeriesPanel(data, "y", "topComponents"));
			topPanel.add("South", componentCheckPanel());
			
		add("Top", topPanel);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 10));
			
			bottomPanel.add("Center", timeSeriesPanel(data, null, "bottomComponents"));
			bottomPanel.add("South", valuePanel(data));
			
		add("Bottom", bottomPanel);
	}
	
	protected DataSet readRawData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected String getYSourceKey() {
		return "y";
	}
	
	protected DataSet readData() {
		DataSet data = readRawData();
		String yKey = getYSourceKey();
//		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		
		decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		
		StringTokenizer st = new StringTokenizer(getParameter(SEASON_PARAM));
		int noOfSeasons = Integer.parseInt(st.nextToken());
		
		boolean showEnds = false;
		MeanMedianVariable smoothedVariable = new MeanMedianVariable("Running mean", data, yKey,
																																										showEnds);
		smoothedVariable.setMeanRun(noOfSeasons);
		data.addVariable("runningMean", smoothedVariable);
		
		data.addVariable("roughDeseasonalised", new SumDiffVariable(translate("Residual"), data, yKey, "runningMean", SumDiffVariable.DIFF));
		
		SeasonalEffectVariable seasonalEffect = new SeasonalEffectVariable("Seasonal effect",
																			data, "roughDeseasonalised", noOfSeasons, decimals);
		data.addVariable("seasonalEffect", seasonalEffect);
		
		data.addVariable("deseasonalised", new SumDiffVariable("Seasonally adj", data, yKey, "seasonalEffect", SumDiffVariable.DIFF));
		
		NumVariable timeVar = new NumVariable(getParameter(TIME_NAME_PARAM));
		timeVar.readSequence(getParameter(TIME_SEQUENCE_PARAM));
		data.addVariable("time", timeVar);
		
		QuadraticModel trendModel = new QuadraticModel("Quadratic trend model", data, "time");
		trendModel.setLSParams("deseasonalised", 0, 0, 0, 0);
		data.addVariable("trendModel", trendModel);
		
		FittedValueVariable fittedTrend = new FittedValueVariable("Fitted trend", data, "time",
																																		"trendModel", decimals);
		data.addVariable("trend", fittedTrend);
		
		ResidValueVariable detrended = new ResidValueVariable("Detrended", data, "time", "deseasonalised",
																																		"trendModel", decimals);
		data.addVariable("detrended", detrended);
		
		data.addVariable("lagged", new LaggedVariable("Lagged Detrended", detrended, 1));
		
		LinearModel ar1Model = new LinearModel("AR1Model", data, "lagged");
		ar1Model.updateLSParams("detrended");
		data.addVariable("ar1Model", ar1Model);
		
		AR1Variable ar1Var = new AR1Variable(translate("Smoothed"), data, "detrended", "ar1Model");
		data.addVariable("cyclic", ar1Var);
		
		data.addVariable("resid", new SumDiffVariable(translate("Residual"), data, "detrended", "cyclic", SumDiffVariable.DIFF));
		
		double yMean = getMean(data, yKey);
		topComponentVar = new ComponentSumVariable("Top components", data,
																														kComponentKeys, yMean, true);
		data.addVariable("topComponents", topComponentVar);
		bottomComponentVar = new ComponentSumVariable("Bottom components", data,
																														kComponentKeys, yMean, false);
		data.addVariable("bottomComponents", bottomComponentVar);
		
		return data;
	}
	
	private double getMean(DataSet data, String yKey) {
		NumVariable variable = (NumVariable)data.getVariable(yKey);
		
		double sum = 0.0;
		int nVals = 0;
		
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			if (!Double.isNaN(nextVal)) {
				sum += nextVal;
				nVals ++;
			}
		}
		return sum / nVals;
	}
	
	protected XPanel addTitle(XPanel normalPanel, DataSet data, String yKey) {
		String title = (yKey == null) ? translate("Sum of selected components") : translate("Data minus sum of selected components");
		
		XPanel superPanel = new XPanel();
		superPanel.setLayout(new BorderLayout());
			
			XLabel theLabel = new XLabel(title, XLabel.LEFT, this);
			theLabel.setFont(getStandardBoldFont());
			
		superPanel.add("North", theLabel);
		superPanel.add("Center", normalPanel);
		
		return superPanel;
	}
	
	protected XPanel timeSeriesPanel(DataSet data, String yKey, String componentSumKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis yAxis = getVertAxis();
		thePanel.add("Left", yAxis);
		
		VertAxis rightVertAxis = getVertAxis2();
		if (rightVertAxis != null)
			thePanel.add("Right", rightVertAxis);
		
			SeasonTimeAxis timeAxis = new SeasonTimeAxis(this, data.getNumVariable().noOfValues());
			timeAxis.setTimeScale(getParameter(SEASON_PARAM), getParameter(SEASON_INFO_PARAM));
			
			String smallFontString = getParameter(USE_SMALL_FONT_PARAM);
			boolean smallFont = (smallFontString != null) && smallFontString.equals("true");
			if (smallFont)
				timeAxis.setFont(getSmallFont());
			
			if (oneTimeAxis == null)
				oneTimeAxis = timeAxis;
		thePanel.add("Bottom", timeAxis);
			
			TimeView theView = new TimeView(data, this, timeAxis, yAxis);
			theView.setActiveNumVariable("y");						//*********		yKey or ...
			theView.setSmoothedVariable(componentSumKey);
			theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		
		thePanel = addTitle(thePanel, data, yKey);
		return thePanel;
	}
	
	protected VertAxis getVertAxis() {
		VertAxis yAxis = new VertAxis(this);
		yAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		return yAxis;
	}
	
	protected VertAxis getVertAxis2() {
		return null;
	}
	
	protected XPanel coreCheckPanel() {
		XPanel checkPanel = new XPanel();
		checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
			
			componentCheck[SEASONAL] = new XCheckbox(translate("Seasonal"), this);
			componentCheck[TREND] = new XCheckbox(translate("Trend"), this);
			
			String cyclicCheckString = getParameter(CYCLIC_CHECK_PARAM);
			if (cyclicCheckString == null || cyclicCheckString.equals("true"))
				componentCheck[CYCLIC] = new XCheckbox(translate("Cyclical"), this);
			
			String residCheckString = getParameter(RESID_CHECK_PARAM);
			if (residCheckString == null || residCheckString.equals("true"))
				componentCheck[RESIDUAL] = new XCheckbox(translate("Residual"), this);
			
		for (int i=0 ; i<4 ; i++)
			if (componentCheck[i] != null) {
				componentCheck[i].setForeground(Color.red);
				checkPanel.add(componentCheck[i]);
			}
			
		return checkPanel;
	}
	
	protected XPanel componentCheckPanel() {
		XPanel thePanel = new InsetPanel(0, 10);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(20, 10);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				XLabel theLabel = new XLabel(translate("Components") + ":", XLabel.LEFT, this);
//				theLabel.setForeground(Color.red);
				theLabel.setFont(getStandardBoldFont());
			innerPanel.add(theLabel);
			
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
				rightPanel.add(new ImageCanvas("redLine.gif", this));
				rightPanel.add(coreCheckPanel());
				rightPanel.add(new ImageCanvas("flatArrow.png", this));
				
			innerPanel.add(rightPanel);
		
			innerPanel.lockBackground(kCheckBackground);
		thePanel.add(innerPanel);
			
		return thePanel;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		thePanel.add(new SeasonYearValueView(data, this, oneTimeAxis, getParameter(TIME_NAME_PARAM)));
		
		thePanel.add(new FitPredictValueView(data, "bottomComponents", "y", this, translate("Sum of components"), translate("Prediction")));
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		for (int i=0 ; i<4 ; i++)
			if (target == componentCheck[i]) {
				topComponentVar.setComponentShow(i, !componentCheck[i].getState());
				bottomComponentVar.setComponentShow(i, componentCheck[i].getState());
				data.variableChanged("topComponents");
				data.variableChanged("bottomComponents");
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}