package exerciseTimeProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import expression.*;
import exercise2.*;
import valueList.*;
import coreVariables.*;
import random.*;
import models.*;
import formula.*;

import time.*;
import curveInteract.*;


public class FindForecastApplet extends ExerciseApplet {
	static final private String[] kXKeys = {"codedYear"};
	
	
	private RandomNormal errorGenerator;
	private ScrollValueList valueList;
	
	private VertAxis yAxis;
	private IndexTimeAxis timeAxis;
	private TimeView theView;
	private XLabel varName;
	
	private ParamTestsView regnTable;
	
	private ExpressionResultPanel regnResultPanel, esResultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("index2", "int");
		registerParameter("varName", "string");
		registerParameter("firstYear", "int");
		registerParameter("lastYear", "int");
		registerParameter("esConst", "const");
		registerParameter("yDecimals", "int");
		registerParameter("paramDecimals", "string");
		registerParameter("forecastDecimals", "int");
		registerParameter("lowMean", "const");
		registerParameter("highMean", "const");
		registerParameter("errorSd", "const");
		registerParameter("yAxis", "string");
		registerParameter("autoCorrel", "const");
		registerParameter("forecastYear", "int");
		registerParameter("yearLabelInfo", "string");
		registerParameter("baseYear", "int");
		registerParameter("maxEstSe", "string");
	}
	
	private int getCount() {
		return getIntParam("lastYear") - getIntParam("firstYear") + 1;
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private int getFirstYear() {
		return getIntParam("firstYear");
	}
	
	private NumValue getEsConst() {
		return getNumValueParam("esConst");
	}
	
	private int getForecastDecimals() {
		return getIntParam("forecastDecimals");
	}
	
	private int getYDecimals() {
		return getIntParam("yDecimals");
	}
	
	private NumValue getIntercept() {
		return new NumValue(getDoubleParam("lowMean"), getInterceptDecimals());
	}
	
	private NumValue getSlope() {
		return new NumValue((getDoubleParam("highMean") - getDoubleParam("lowMean")) / getCount(), getSlopeDecimals());
	}
	
	private NumValue getErrorSd() {
		return getNumValueParam("errorSd");
	}
	
	private int getInterceptDecimals() {
		StringTokenizer st = new StringTokenizer(getStringParam("paramDecimals"));
		return Integer.parseInt(st.nextToken());
	}
	
	private int getSlopeDecimals() {
		StringTokenizer st = new StringTokenizer(getStringParam("paramDecimals"));
		st.nextToken();
		return Integer.parseInt(st.nextToken());
	}
	
	private String getYAxisInfo() {
		return getStringParam("yAxis");
	}
	
	private String getYearLabelInfo() {
		return getStringParam("yearLabelInfo");	//	index of first label, label index step, firstPrintVal, printLabelStep
	}
	
	private double getAutoCorrel() {
		return getDoubleParam("autoCorrel");
	}
	
	private int getForecastYear() {
		return getIntParam("forecastYear");
	}
	
	private int getBaseYear() {
		return getIntParam("baseYear");
	}
	
	private NumValue getMaxEst() {
		StringTokenizer st = new StringTokenizer(getStringParam("maxEstSe"));
		return new NumValue(st.nextToken());
	}
	
	private NumValue getMaxSe() {
		StringTokenizer st = new StringTokenizer(getStringParam("maxEstSe"));
		st.nextToken();
		return new NumValue(st.nextToken());
	}
	
	
//-----------------------------------------------------------

	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 6));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new BorderLayout(10, 0));
			
			dataPanel.add("Center", plotPanel(data));
			dataPanel.add("East", listPanel(data));
			dataPanel.add("South", tablePanel(data));
			
		thePanel.add("Center", dataPanel);	
		
		thePanel.add("South", resultPanels());	
		
		return thePanel;
	}
	
	private XPanel plotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			varName = new XLabel("", XLabel.LEFT, this);
		thePanel.add("North", varName);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				yAxis = new VertAxis(this);
			innerPanel.add("Left", yAxis);
			
				timeAxis = new IndexTimeAxis(this, 9);
			innerPanel.add("Bottom", timeAxis);
			
				theView = new TimeView(data, this, timeAxis, yAxis);
				theView.setActiveNumVariable("y");
				theView.setSmoothedVariable("y");
				theView.addSmoothedVariable("es");
				theView.addSmoothedVariable("fit");
			
			innerPanel.add("Center", theView);
			theView.lockBackground(Color.white);
		
		thePanel.add("Center", innerPanel);
		return thePanel;
	}
	
	private XPanel listPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new CenterFillLayout(CenterFillLayout.FILL_VERT));
		
			valueList = new ScrollValueList(data, this, true);
			valueList.addVariableToList("year", ScrollValueList.RAW_VALUE);
			valueList.addVariableToList("y", ScrollValueList.RAW_VALUE);
			valueList.addVariableToList("es", ScrollValueList.RAW_VALUE);
		thePanel.add(valueList);
		
		return thePanel;
	}
	
	private XPanel tablePanel(DataSet data) {
		XPanel thePanel = new InsetPanel(20, 7);
		thePanel.setLayout(new BorderLayout(0, 0));
			String[] paramName = {"intercept", "coded year"};
			NumValue maxT = new NumValue(-99.999, 3);
			regnTable = new ParamTestsView(data, this, "ls", "y", paramName, null, null, null, maxT);
		thePanel.add("Center", regnTable);
		
		return thePanel;
	}

	private XPanel resultPanels() {
		XPanel eqnsPanel = new InsetPanel(2, 2);
		eqnsPanel.setLayout(new ProportionLayout(0.5, 4, ProportionLayout.VERTICAL));
		
			regnResultPanel = new ExpressionResultPanel(null, 1, 30, "Regression forecast =", 6,
																													ExpressionResultPanel.HORIZONTAL, this);
			registerStatusItem("regn", regnResultPanel);
		eqnsPanel.add(ProportionLayout.TOP, regnResultPanel);
		
			esResultPanel = new ExpressionResultPanel(null, 1, 30, "Exp smoothing forecast =", 6,
																													ExpressionResultPanel.HORIZONTAL, this);
			registerStatusItem("es", esResultPanel);
		eqnsPanel.add(ProportionLayout.BOTTOM, esResultPanel);
		
		eqnsPanel.lockBackground(kWorkingBackground);
		
		return eqnsPanel;
	}
	
	protected void setDisplayForQuestion() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		varName.setText(yVar.name);
		int resultDecimals = getForecastDecimals();
		
		regnResultPanel.clear();
		regnResultPanel.setResultDecimals(resultDecimals);
		esResultPanel.clear();
		esResultPanel.setResultDecimals(resultDecimals);
		
		timeAxis.setNoOfVals(getForecastYear() - getFirstYear() + 1);
		timeAxis.setTimeScale(getYearLabelInfo());
		timeAxis.invalidate();
		
		yAxis.readNumLabels(getYAxisInfo());
		yAxis.invalidate();
		
		valueList.resetVariables();
		valueList.invalidate();
		valueList.repaint();
		
		valueList.scrollToEnd();
		
		regnTable.setMaxParams(getMaxEst(), getMaxSe());
		
		validate();
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			IndexVariable indexVar = new IndexVariable("Index", 9);
		data.addVariable("index", indexVar);
		
			ScaledVariable codedYearVar = new ScaledVariable("Coded year", indexVar, "index", 0, 1, 0);
		data.addVariable("codedYear", codedYearVar);
		
			ScaledVariable yearVar = new ScaledVariable("Year", indexVar, "index", 1900, 1.0, 0);
		data.addVariable("year", yearVar);
		
			errorGenerator = new RandomNormal(1, 0.0, 1.0, 2.5);
			errorGenerator.setSeed(nextSeed());
		
			NumSampleVariable acErrorVar = new NumSampleVariable("AcError", errorGenerator, 9);
			acErrorVar.generateNextSample();
		data.addVariable("acError", acErrorVar);
		
			ARVariable acVar = new ARVariable("AutoCorr", data, "acError");
		data.addVariable("ac", acVar);
		
			LinearModel generateModel = new LinearModel("Model", data, "index");
		data.addVariable("model", generateModel);
			
			ResponseVariable yVar = new ResponseVariable("Y", data, "index", "ac", "model", 9);
			yVar.setRoundValues(true);
		data.addVariable("y", yVar);
		
		
			ExpSmoothVariable esVar = new ExpSmoothVariable("ES", data, "y");
		data.addVariable("es", esVar);
		
			MultipleRegnModel lsModel = new MultipleRegnModel("LS", data, kXKeys);
		data.addVariable("ls", lsModel);
		
			FittedValueVariable fit = new FittedValueVariable("Regn fit", data, "codedYear", "ls", 9);
		data.addVariable("fit", fit);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		errorGenerator.setSeed(nextSeed());
	}

	
	protected void setDataForQuestion() {
		int n = getCount();
		
		NumSampleVariable errorVar = (NumSampleVariable)data.getVariable("acError");
		errorVar.setSampleSize(n);
		errorVar.generateNextSample();
		
		ARVariable acVar = (ARVariable)data.getVariable("ac");
		acVar.setSerialCorr(getAutoCorrel());
		
		LinearModel generateModel = (LinearModel)data.getVariable("model");
		generateModel.setIntercept(getIntercept());
		generateModel.setSlope(getSlope());
		generateModel.setSD(getErrorSd().toDouble());
		
		data.variableChanged("acError");
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.setDecimals(getYDecimals());
		
		IndexVariable indexVar = (IndexVariable)data.getVariable("index");
		indexVar.setNoOfValues(n);
		
		ScaledVariable yearVar = (ScaledVariable)data.getVariable("year");
		yearVar.setParam(0, getFirstYear() - 1);
		
		ScaledVariable codedYearVar = (ScaledVariable)data.getVariable("codedYear");
		codedYearVar.setParam(0, getFirstYear() - getBaseYear());
		
		ExpSmoothVariable esVar = (ExpSmoothVariable)data.getVariable("es");
		NumValue esConst = getEsConst() ;
		esVar.name = "ES(" + esConst + ")";
		esVar.setSmoothConst(esConst.toDouble());
		esVar.setExtraDecimals(getForecastDecimals() - getYDecimals());
		
		MultipleRegnModel lsModel = (MultipleRegnModel)data.getVariable("ls");
		int decimals[] = {getInterceptDecimals(), getSlopeDecimals()};
		lsModel.setLSParams("y", decimals, 0);
		
			FittedValueVariable fit = (FittedValueVariable)data.getVariable("fit");
		fit.setDecimals(esVar.getMaxDecimals());
		
	}
	
	
//-----------------------------------------------------------
	
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int esYear = getForecastYear();
//		NumValue esConst = getEsConst();
//		NumValue negEsConst = new NumValue(1 - esConst.toDouble(), esConst.decimals);
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the two forecasts in the edit boxes on the right above.\n(You can use the two boxes to their left to evaluate expressions.)\n");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type values for both of the missing values in the boxes on the right above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer");
				insertCorrectEsMessage(messagePanel);
				insertCorrectRegnMessage(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!");
				insertCorrectEsMessage(messagePanel);
				insertCorrectRegnMessage(messagePanel);
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Only one forecast is correct!\n");
				if (esCorrect())
					messagePanel.insertText("Your exponential smoothing forecast is correct but you have incorrectly calculated the regression forecast.");
				else
					messagePanel.insertText("Your regression forecast is correct but you have incorrectly found the exponential smoothing forecast.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Your exponential smoothing forecast and regression forecast for " + esYear + " are both incorrect (or inaccurately calculated).");
				break;
		}
	}
	
	private void insertCorrectEsMessage(MessagePanel messagePanel) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		int forecastYear = getForecastYear();
		int lastYear = getFirstYear() + yVar.noOfValues() - 1;
		messagePanel.insertText("\nThe exponential smoothing forecast for " + forecastYear
									+ " is the most recent exponentially smoothed value (for " + lastYear + ").");
	}
	
	private void insertCorrectRegnMessage(MessagePanel messagePanel) {
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		NumValue intercept = ls.getParameter(0);
		NumValue slope = ls.getParameter(1);
		int forecastYear = getForecastYear();
		int baseYear = getBaseYear();
		messagePanel.insertText(MText.expandText("\nThe year " + forecastYear + " is coded as \""
												+ (forecastYear - baseYear) + "\", so the forecast is " + intercept
												+ " + " + slope + " #times# " + (forecastYear - baseYear) + "."));
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	private boolean esCorrect() {
		NumValue esAttempt = esResultPanel.getAttempt();
		NumVariable esVar = (NumVariable)data.getVariable("es");
		NumValue esCorrect = ((NumValue)esVar.valueAt(esVar.noOfValues() - 1));
		
		double maxExactError = 2 * Math.pow(10.0, -esCorrect.decimals);
		double esError = Math.abs(esAttempt.toDouble() - esCorrect.toDouble());
		
		return esError < maxExactError;
	}
	
	private boolean regnCorrect() {
		NumValue regnAttempt = regnResultPanel.getAttempt();
		int forecastIndex = getForecastYear() - getBaseYear();
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		double x[] = {forecastIndex};
		double regnCorrect = ls.evaluateMean(x);
		
		double maxExactError = 3 * Math.pow(10.0, -getForecastDecimals());
		double regnError = Math.abs(regnAttempt.toDouble() - regnCorrect);
		
		return regnError < maxExactError;
	}
	
	protected int assessAnswer() {
		if (regnResultPanel.isClear() || esResultPanel.isClear())
			return ANS_INCOMPLETE;
		
		boolean esCorrect = esCorrect();
		boolean regnCorrect = regnCorrect();
		
		if (esCorrect && regnCorrect)
			return ANS_CORRECT;
		
		if (esCorrect || regnCorrect)
			return ANS_CLOSE;
		
		return ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumVariable esVar = (NumVariable)data.getVariable("es");
		NumValue esCorrect = ((NumValue)esVar.valueAt(esVar.noOfValues() - 1));
		esResultPanel.showAnswer(esCorrect, "");
		
		int forecastIndex = getForecastYear() - getBaseYear();
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		double x[] = {forecastIndex};
		double forecast = ls.evaluateMean(x);
		
		
		NumValue regnCorrect = new NumValue(forecast, getForecastDecimals());
		NumValue intercept = ls.getParameter(0);
		NumValue slope = ls.getParameter(1);
		
		String lsFormula = intercept + " + " + slope + " * " + forecastIndex;
		regnResultPanel.showAnswer(regnCorrect, lsFormula);
	}
	
	protected double getMark() {
		double mark = 0.0;
		if (!esResultPanel.isClear() && esCorrect())
			mark += 0.5;
		if (!regnResultPanel.isClear() && regnCorrect())
			mark += 0.5;
		return mark;
	}
}
