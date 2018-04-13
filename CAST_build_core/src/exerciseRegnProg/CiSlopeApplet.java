package exerciseRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;
import formula.*;
import coreVariables.*;
import models.*;
import valueList.*;

import linMod.*;
import regn.*;
import exerciseEstim.*;
import exerciseBivar.*;
import exerciseGroupsProg.*;
import exerciseRegn.*;


public class CiSlopeApplet extends CoreCiApplet {
	static final protected String SHOW_SUMMARY_TABLE_OPTION = "showSummaryTable";
	
	private RandomNormal xGenerator, yGenerator;
	
	private XLabel yNameLabel;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	protected ResidPredictScatterView theView;
	
	private LinearEquationView eqnView;
	private FixedValueView seView;
	private ParameterSummaryPanel parameterSummaries;
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("xVarName", "string");
		registerParameter("xAxis", "string");
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "string");
		registerParameter("count", "int");
		registerParameter("corr", "const");
		registerParameter("interceptDecimals", "int");
		registerParameter("slopeDecimals", "int");
		registerParameter("maxParam", "const");
		registerParameter("interceptName", "string");
		registerParameter("slopeName", "string");
	}
	
//	--------------------------------------------------
	
	protected String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private double getXMean() {
		StringTokenizer st = new StringTokenizer(getXAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		return (max + min) / 2;
	}
	
	private double getXSD() {
		StringTokenizer st = new StringTokenizer(getXAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		return (max - min) / 5;
	}
	
	private String getXAxisInfo() {
		return getStringParam("xAxis");
	}
	
	protected String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private double getYMean() {
		StringTokenizer st = new StringTokenizer(getYAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		return (max + min) / 2;
	}
	
	private double getYSD() {
		StringTokenizer st = new StringTokenizer(getYAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		return (max - min) / 6;
	}
	
	private String getYAxisInfo() {
		return getStringParam("yAxis");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	private NumValue getCorr() {
		return getNumValueParam("corr");
	}
	
	private int getInterceptDecimals() {
		if (getObjectParam("interceptDecimals") == null)
			return 0;
		else
			return getIntParam("interceptDecimals");
	}
	
	private int getSlopeDecimals() {
		if (getObjectParam("slopeDecimals") == null)
			return 0;
		else
			return getIntParam("slopeDecimals");
	}
	
	private NumValue getMaxParam() {
		return getNumValueParam("maxParam");
	}
	
	private String getInterceptName() {
		String s = getStringParam("interceptName");
		return (s == null) ? "Intercept" : s;
	}
	
	private String getSlopeName() {
		String s = getStringParam("slopeName");
		return (s == null) ? "Slope" : s;
	}
	
//	--------------------------------------------------

	protected DataSet getData() {
		DataSet data = new DataSet();
		
			xGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xBaseVar = new NumSampleVariable("ZX", xGenerator, 9);
			xBaseVar.generateNextSample();
		data.addVariable("xBase", xBaseVar);
		
			ScaledVariable xVar = new ScaledVariable("", xBaseVar, "xBase", 0.0, 1.0, 9);
			xVar.setRoundValues(true);
		data.addVariable("x", xVar);
		
			yGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			yGenerator.setSeed(nextSeed());
			NumSampleVariable yBaseVar = new NumSampleVariable("ZY", yGenerator, 9);
			yBaseVar.generateNextSample();
		data.addVariable("yBase", yBaseVar);
			
			CorrelatedVariable yVar = new CorrelatedVariable("", data, "xBase", "yBase", 9);
		data.addVariable("y", yVar);
		
			LinearModel ls = new LinearModel("Least sqrs", data, "x");
		data.addVariable("ls", ls);
		
		return data;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.65, 0, ProportionLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(30, 0));
			
			topPanel.add("Center", getScatterPanel(data));
			topPanel.add("East", getParameterSummaryPanel(data));
			
		thePanel.add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(50, 0));
			
				tLookupPanel = new TLookupPanel(this, TLookupPanel.QUANTILE_LOOKUP, TLookupPanel.T_ONLY);
				registerStatusItem("tLookupPanel", tLookupPanel);
			bottomPanel.add("Center", tLookupPanel);
			
			FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
				tseTemplate = new PlusMinusCalcPanel(getMaxSe(), bigGreenContext);
				registerStatusItem("tseTemplate", tseTemplate);
			bottomPanel.add("East", tseTemplate);
			
			bottomPanel.lockBackground(kWorkingBackground);
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		return thePanel;
	}
	
	private XPanel getParameterSummaryPanel(DataSet data) {
		XPanel eqnPanel = new InsetPanel(0, 6, 0, 0);
		eqnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		if (hasOption(SHOW_SUMMARY_TABLE_OPTION)) {
			parameterSummaries = new ParameterSummaryPanel(data, "ls", this);
			eqnPanel.add(parameterSummaries);
		}
		else {
				eqnView = new LinearEquationView(data, this, "ls", "", "", null, null, null, null);
				eqnView.setFont(getBigFont());
			eqnPanel.add(eqnView);
			
				seView = new FixedValueView("se(slope) =", getMaxSe(), 0.0, this);
				seView.setFont(getBigFont());
			eqnPanel.add(seView);
		}
		return eqnPanel;
	}
	
	protected XPanel getScatterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			yNameLabel = new XLabel("", XLabel.LEFT, this);
		thePanel.add("North", yNameLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				xAxis = new HorizAxis(this);
			displayPanel.add("Bottom", xAxis);
			
				yAxis = new VertAxis(this);
			displayPanel.add("Left", yAxis);
			
				theView = new ResidPredictScatterView(data, this, xAxis, yAxis, "x", "y", "ls");
				theView.lockBackground(Color.white);
			displayPanel.add("Center", theView);
			
		thePanel.add("Center", displayPanel);
		
		return thePanel;
	}
	
	protected String ciLabelString() {
		return "The regression slope is";
	}
	
	protected void setDataForQuestion() {
		int n = getCount();
		NumSampleVariable xCoreVar = (NumSampleVariable)data.getVariable("xBase");
		xCoreVar.setSampleSize(n);
		xCoreVar.generateNextSample();
		
		NumSampleVariable yCoreVar = (NumSampleVariable)data.getVariable("yBase");
		yCoreVar.setSampleSize(n);
		yCoreVar.generateNextSample();
		
		double sx = 0, sxx = 0;
		for (int i=0 ; i<n ; i++) {
			double x = xCoreVar.doubleValueAt(i);
			sx += x;
			sxx += x * x;
		}
		double xCoreBar = sx / n;
		double xCoreSd = Math.sqrt((sxx - sx * xCoreBar) / (n - 1));
		
		ScaledVariable xVar = (ScaledVariable)data.getVariable("x");
		double scale = getXSD() / xCoreSd;		//	scale so that xVar has exactly specified mean and sd
																					//	This makes slope and intercept neat values for FindPredictionApplet
		xVar.setScale(getXMean() - xCoreBar * scale, scale, 9);
		xVar.name = getXVarName();
		
		CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable("y");
		yVar.setMeanSdCorr(getYMean(), getYSD(), getCorr().toDouble(), 0);		//	zero decimals for Y since its values are not printed
		yVar.name = getYVarName();
		
		LinearModel ls = (LinearModel)data.getVariable("ls");
		ls.setLSParams("y", getInterceptDecimals(), getSlopeDecimals(), 0);
		
		data.variableChanged("y");
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		xAxis.readNumLabels(getXAxisInfo());
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		yAxis.readNumLabels(getYAxisInfo());
		yNameLabel.setText(getYVarName());
		yAxis.invalidate();
		
		if (hasOption(SHOW_SUMMARY_TABLE_OPTION)) {
			parameterSummaries.updateForNewData(new LabelValue(getInterceptName()), new LabelValue(getSlopeName()), getMaxParam(), getMaxSe());
//			parameterSummaries.revalidate();
		}
		else {
			eqnView.setExplanName(getXVarName());
			eqnView.setYName(getYVarName());
			LinearModel ls = (LinearModel)data.getVariable("ls");
			NumValue intercept = ls.getIntercept();
			NumValue slope = ls.getSlope();
			eqnView.setMinMaxParams(intercept, intercept, slope, slope);		//	does invalidate()
			
			seView.setValue(new NumValue(evaluateCorrectSe(), getMaxSe().decimals));
			seView.setMaxValue(getMaxSe());		//	does revalidate()
		}
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the t distribution (adjusting the degrees of freedom if necessary)");
				messagePanel.insertText(" to help find a " + getCiPercent() + "% CI for the difference between the means, typing it into the answer boxes.\n");
				messagePanel.insertText("(Type a proportion into the edit box in the middle of the distribution to specify the red central area.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error\n");
				messagePanel.insertRedText("You must type values for both ends of the confidence interval in the two boxes above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Invalid!\n");
				messagePanel.insertRedText("The lower limit of the interval must be less than the upper limit.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(getCiPercent() + "% of the ");
				messagePanel.insertText("t distribution with " + getDf() + " df");
				messagePanel.insertText(" lies between #plusMinus# " + new NumValue(getCorrectT(), 3) + ".\n");
				messagePanel.insertText("The confidence interval is therefore ");
				insertIntervalFormula(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly found the end-points of the confidence interval.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertRedText("The end-points of your confidence interval are close to the correct ones.\n");
				insertTHints(messagePanel);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The end-points of your confidence interval are not close enough to the correct ones.\n");
				insertTHints(messagePanel);
				break;
		}
	}
	
	private void insertIntervalFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		
		MFormula t = new MConst(new NumValue(getCorrectT(), 3), context);
		MFormula se = new MText("se(slope)", context);
		MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
		
		MFormula slope = new MText("slope", context);
		
		MFormula ci1 = new MBinary(MBinary.PLUS_MINUS, slope, tse, context);
		
		double slopeVal = evaluateEstimate();
		MFormula mean = new MConst(new NumValue(slopeVal, getSlopeDecimals()), context);
		MFormula tseVal = new MConst(tseTemplate.getResult(), context);
		
		MFormula ci2 = new MBinary(MBinary.PLUS_MINUS, mean, tseVal, context);
		
		MFormula f = new MBinary(MBinary.EQUALS, ci1, ci2, context);
		
		messagePanel.insertFormula(f);
	}
	
	private void insertTHints(MessagePanel messagePanel) {
		int df = getDf();
		if (!tLookupPanel.isCorrectDf(df))
			messagePanel.insertText(MText.expandText("Firstly use a t distribution with n - 2 = "  + df + " df."));
		else {
			messagePanel.insertText("You have used the correct degrees of freedom for the t distribution.\n");
			
			NumValue ciPercent = getCiPercent();
			NumValue ciLevel = new NumValue(ciPercent.toDouble() / 100, ciPercent.decimals + 2);
			if (!tLookupPanel.isCorrectConfidenceLevel(ciLevel))
				messagePanel.insertText("Next find the t-value for which " + ciPercent + "% of the area is between #plusMinus#t. (The red centre area in the distribution should be " + ciLevel + ".)");
			else {
				messagePanel.insertText("You have found the correct t-value for a " + ciPercent + "% confidence interval.");
				
				NumValue tInTemplate = tseTemplate.getT();
				boolean copiedT = effectivelySame(tInTemplate.toDouble(), getCorrectT(), tInTemplate.decimals);
				
				NumValue seInTemplate = tseTemplate.getSe();
				boolean correctSe = effectivelySame(seInTemplate.toDouble(), evaluateCorrectSe(), seInTemplate.decimals);		//	when used to find the plus-minus value
				if (!copiedT || !correctSe) {
					if (!copiedT)
						messagePanel.insertText(" However you do not seem to have used this value in the template for the \"#plusMinus#\" value.");
					if (!correctSe)
						messagePanel.insertText("\nYou have not used the correct standard error in the template for the \"#plusMinus#\" value.");
				}
				else {
					messagePanel.insertText(" And you seem to have found the \"#plusMinus#\" value correctly in the template.\nThe confidence interval should be between (the least squares slope - this value) to (the least squares slope + this value),\n");
					messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
					
					FormulaContext context = new FormulaContext(null, null, this);
					MFormula se = new MText("se(slope)", context);
					MFormula t = new MText("t", context);
					MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
					
					MFormula xBar = new MText("slope", context);
					MFormula ci = new MBinary(MBinary.PLUS_MINUS, xBar, tse, context);
					
					messagePanel.insertFormula(ci);
				}
			}
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected double evaluateCorrectSe() {
		LinearModel ls = (LinearModel)data.getVariable("ls");
		return ls.getSeSlope();
	}
	
	protected double evaluateEstimate() {
		LinearModel ls = (LinearModel)data.getVariable("ls");
		return ls.getSlope().toDouble();
	}
	
	protected int getDf() {
		return getCount() - 2;
	}
	
	
//-----------------------------------------------------------

	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
	}
}