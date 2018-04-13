package exerciseSDProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreVariables.*;
import exercise2.*;
import formula.*;

import exerciseSD.*;


public class ScaledMeanSdApplet extends ExerciseApplet {
	static final private double kMinWidthPropn = 0.8;
	
	static final private Color kMeanBackgroundColor = new Color(0x9BD8F2);
	static final private Color kSdBackgroundColor = new Color(0xFFAFAF);
	static final private Color kHeadingColor = new Color(0x880000);
	
	static final protected NumValue kNanValue = new NumValue("?");
	static final protected NumValue kOneValue = new NumValue(1, 0);
	static final protected NumValue kZeroValue = new NumValue(0, 0);
	
	private RandomNormal generator;
	
	private ResultValuePanel meanResultPanel, sdResultPanel;
	
	protected XLabel convertionFormula;
	protected HorizAxis theAxis;
	protected DataView theView;
	private ScaleXYTemplatePanel meanScaleXYTemplate, meanScaleYXTemplate, sdScaleXYTemplate, sdScaleYXTemplate;
	
	private int meanResult = ANS_UNCHECKED;
	private int sdResult = ANS_UNCHECKED;
	
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
		registerParameter("varName", "string");
		registerParameter("shortVarName", "string");
		registerParameter("xUnits", "string");
		registerParameter("yUnits", "string");
		registerParameter("direction", "int");
		registerParameter("count", "int");
		registerParameter("axis", "string");
		registerParameter("decimals", "int");
		registerParameter("maxResult", "const");
		registerParameter("intercept", "const");
		registerParameter("slope", "const");
	}
	
	protected String getVarName() {
		String unitString = (getDirection() == ScaleXYTemplatePanel.X_TO_Y) ? getXUnits()
																																				: getYUnits();
		return getStringParam("varName") + ", " + unitString;
	}
	
	private String getShortVarName() {
		return getStringParam("shortVarName");
	}
	
	private String decodeDegrees(String s) {
		if (s.equals("degreesC"))
			s = "#degrees#C";
		else if (s.equals("degreesF"))
			s = "#degrees#F";
		return MText.expandText(s);
	}
	
	protected String getXUnits() {
		return decodeDegrees(getStringParam("xUnits"));
	}
	
	protected String getYUnits() {
		return decodeDegrees(getStringParam("yUnits"));
	}
	
	protected int getDirection() {
		return getIntParam("direction");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	protected String getAxisInfo() {
		return getStringParam("axis");
	}
	
	protected int getDecimals() {
		return getIntParam("decimals");
	}
	
	protected NumValue getMaxResult() {
		return getNumValueParam("maxResult");
	}
	
	protected NumValue getIntercept() {
		return getNumValueParam("intercept");
	}
	
	protected NumValue getSlope() {
		return getNumValueParam("slope");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		thePanel.add("North", createConvertionFormula());
		
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				theAxis = new HorizAxis(this);
			displayPanel.add("Bottom", theAxis);
			
				theView = new StackMeanSdView(data, this, theAxis, "y", getDecimals());
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
			displayPanel.add("Center", theView);
		
		thePanel.add("Center", displayPanel);
		
			XPanel meanSdPanel = new XPanel();
			meanSdPanel.setLayout(new ProportionLayout(0.5, 0));
			
			meanSdPanel.add(ProportionLayout.LEFT, createMeanPanel());
			meanSdPanel.add(ProportionLayout.RIGHT, createSdPanel());
		
		thePanel.add("South", meanSdPanel);
		
		return thePanel;
	}
	
	protected XLabel createConvertionFormula() {
		convertionFormula = new XLabel("", XLabel.CENTER, this);
		convertionFormula.setFont(getBigBoldFont());
		convertionFormula.setForeground(kHeadingColor);
		return convertionFormula;
	}
	
	private XPanel createMeanPanel() {
		XPanel meanPanel = new XPanel();
		meanPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
		
			XLabel meanLabel = new XLabel(translate("Mean"), XLabel.CENTER, this);
			meanLabel.setFont(getBigBoldFont());
			meanLabel.setForeground(kHeadingColor);
		meanPanel.add(meanLabel);
			
			XPanel templatePanel = new InsetPanel(0, 4, 0, 0);
			templatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
			
				meanScaleXYTemplate = new ScaleXYTemplatePanel(this, ScaleXYTemplatePanel.X_TO_Y, getMaxResult());
				registerStatusItem("meanXYTemplate", meanScaleXYTemplate);
			templatePanel.add(meanScaleXYTemplate);
		
				meanScaleYXTemplate = new ScaleXYTemplatePanel(this, ScaleXYTemplatePanel.Y_TO_X, getMaxResult());
				registerStatusItem("meanYXTemplate", meanScaleYXTemplate);
			templatePanel.add(meanScaleYXTemplate);
			
			templatePanel.lockBackground(kMeanBackgroundColor);
		meanPanel.add(templatePanel);
			
			meanResultPanel = new ResultValuePanel(this, "Mean =", getYUnits(), 6);
			registerStatusItem("mean", meanResultPanel);
		meanPanel.add(meanResultPanel);
		return meanPanel;
	}
	
	private XPanel createSdPanel() {
		XPanel sdPanel = new XPanel();
		sdPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
		
			XLabel sdLabel = new XLabel(translate("St devn"), XLabel.CENTER, this);
			sdLabel.setFont(getBigBoldFont());
			sdLabel.setForeground(kHeadingColor);
		sdPanel.add(sdLabel);
			
			XPanel templatePanel = new InsetPanel(0, 4, 0, 0);
			templatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
			
				sdScaleXYTemplate = new ScaleXYTemplatePanel(this, ScaleXYTemplatePanel.X_TO_Y, getMaxResult());
				registerStatusItem("sdXYTemplate", sdScaleXYTemplate);
			templatePanel.add(sdScaleXYTemplate);
		
				sdScaleYXTemplate = new ScaleXYTemplatePanel(this, ScaleXYTemplatePanel.Y_TO_X, getMaxResult());
				registerStatusItem("sdYXTemplate", sdScaleYXTemplate);
			templatePanel.add(sdScaleYXTemplate);
			
			templatePanel.lockBackground(kSdBackgroundColor);
		sdPanel.add(templatePanel);
			
			sdResultPanel = new ResultValuePanel(this, translate("St devn") + " =", getYUnits(), 6);
			registerStatusItem("sd", sdResultPanel);
		sdPanel.add(sdResultPanel);
		
		return sdPanel;
	}
	
	protected void setupConvertionFormula() {
		NumValue intercept = getIntercept();
		NumValue slope = getSlope();
		String text = getShortVarName() + "(" + getYUnits() + ") = ";
		if (intercept.toDouble() != 0.0)
			text += intercept + " + ";
		text += slope + " " + getShortVarName() + "(" + getXUnits() + ")";
		convertionFormula.setText(text);
		convertionFormula.repaint();
	}
	
	protected void setDisplayForQuestion() {
		String unitString = (getDirection() == ScaleXYTemplatePanel.X_TO_Y) ? getYUnits()
																																			: getXUnits();
		meanResultPanel.changeUnits(unitString);
		meanResultPanel.clear();
		meanResultPanel.invalidate();
		sdResultPanel.changeUnits(unitString);
		sdResultPanel.clear();
		sdResultPanel.invalidate();
		
		meanScaleXYTemplate.setXYValues(kZeroValue, kZeroValue, kZeroValue);
		
		meanScaleYXTemplate.setYXValues(kZeroValue, kZeroValue, kZeroValue);
		
		sdScaleXYTemplate.setXYValues(kZeroValue, kZeroValue, kZeroValue);
		
		sdScaleYXTemplate.setYXValues(kZeroValue, kZeroValue, kZeroValue);
		
		setupConvertionFormula();
		
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		((StackMeanSdView)theView).setDecimals(getDecimals());
		
		validate();
	}
	
	private void setScaling(NumVariable baseVar, ScaledVariable yVar, double targetMin, double targetMax) {
		NumValue sortedY[] = baseVar.getSortedData();
		double dataMin = sortedY[0].toDouble();
		double dataMax = sortedY[sortedY.length - 1].toDouble();
		
		double factor = (targetMax - targetMin) / (dataMax - dataMin);
		double shift = targetMin - dataMin * factor;
		
		yVar.setScale(shift, factor, 9);
		yVar.clearSortedValues();
		
		data.variableChanged("base");
	}
	
	protected boolean rejectSample(double axisMin, double axisMax) {
		return false;			//	overridden by ScaledCumulativeApplet
	}
	
	protected void setDataForQuestion() {
		NumSampleVariable baseVar = (NumSampleVariable)data.getVariable("base");
		baseVar.setSampleSize(getCount());
		
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		
		Random uniformGenerator = new Random(nextSeed());
		double minWidth = kMinWidthPropn * (axisMax - axisMin);
		double targetMin = axisMin + uniformGenerator.nextDouble() * (axisMax - axisMin - minWidth);
		double targetMax = targetMin + minWidth + uniformGenerator.nextDouble() * (axisMax - targetMin - minWidth);
		
		do {
			baseVar.generateNextSample();
			setScaling(baseVar, (ScaledVariable)data.getVariable("y"), targetMin, targetMax);
		} while (rejectSample(axisMin, axisMax));
		
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Find the transformed mean and sd.\n(One of the two blue templates can be used to find the mean; one of the two red templates can be used to find the st devn.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The calculations to obtain the correct mean and standard deviation are shown in the templates above.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertRedText("You have given the correct mean and standard deviation.");
				break;
			case ANS_INCOMPLETE:
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertBoldText("Mean: ");
				if (meanResult == ANS_INCOMPLETE)
					messagePanel.insertRedText("You have not typed a value for the mean.");
				else if (meanResult == ANS_CORRECT)
					messagePanel.insertText("You have given the correct value for the mean.");
				else {		//		meanResult == ANS_WRONG
					messagePanel.insertRedText("You should use the formula  ");
					if (getDirection() == ScaleXYTemplatePanel.X_TO_Y)
						messagePanel.insertFormula(xToYFormula("mean"));
					else
						messagePanel.insertFormula(yToXFormula("mean"));
				}
				
				messagePanel.insertBoldText("\nSt devn: ");
				if (sdResult == ANS_INCOMPLETE)
					messagePanel.insertRedText("You have not typed a value for the standard deviation.");
				else if (sdResult == ANS_INVALID)
					messagePanel.insertRedText("The standard deviation cannot be negative.");
				else if (sdResult == ANS_CORRECT)
					messagePanel.insertText("You have given the correct standard deviation.");
				else {		//		sdResult == ANS_WRONG
					messagePanel.insertRedText("You should use the formula  ");
					if (getDirection() == ScaleXYTemplatePanel.X_TO_Y)
						messagePanel.insertFormula(xToYSdFormula());
					else
						messagePanel.insertFormula(yToXSdFormula());
				}
				break;
		}
	}
	
	protected MFormula xToYFormula(String meanString) {
		FormulaContext stdContext = new FormulaContext(null, null, this);
		NumValue interceptVal = getIntercept();
		NumValue slopeVal = getSlope();
		
		MFormula rightFormula = new MBinary(MBinary.TIMES, new MConst(slopeVal, stdContext),
												new MText(meanString + "(" + getXUnits() + ")", stdContext), stdContext);
		if (interceptVal.toDouble() != 0.0)
			rightFormula = new MBinary(MBinary.PLUS, new MConst(interceptVal, stdContext),
																																	rightFormula, stdContext);
		
		return new MBinary(MBinary.EQUALS, new MText(meanString + "(" + getYUnits() + ")", stdContext),
																																	rightFormula, stdContext);
	}
	
	protected MFormula yToXFormula(String meanString) {
		FormulaContext stdContext = new FormulaContext(null, null, this);
		NumValue interceptVal = getIntercept();
		NumValue slopeVal = getSlope();
		
		MFormula numer = new MText("mean(" + getYUnits() + ")", stdContext);
		if (interceptVal.toDouble() != 0.0)
			numer = new MBinary(MBinary.MINUS, numer, new MConst(interceptVal, stdContext),
																																				stdContext);
		
		MRatio ratio = new MRatio(numer, new MConst(slopeVal, stdContext), stdContext);
		return new MBinary(MBinary.EQUALS, new MText("mean(" + getXUnits() + ")", stdContext),
																																					ratio, stdContext);
	}
	
	private MFormula xToYSdFormula() {
		FormulaContext stdContext = new FormulaContext(null, null, this);
		NumValue slopeVal = getSlope();
		
		MFormula rightFormula = new MBinary(MBinary.TIMES, new MConst(slopeVal, stdContext),
																new MText("sd(" + getXUnits() + ")", stdContext), stdContext);
		return new MBinary(MBinary.EQUALS, new MText("sd(" + getYUnits() + ")", stdContext),
																																	rightFormula, stdContext);
	}
	
	private MFormula yToXSdFormula() {
		FormulaContext stdContext = new FormulaContext(null, null, this);
		NumValue slopeVal = getSlope();
		
		MFormula numer = new MText("sd(" + getYUnits() + ")", stdContext);
		MRatio ratio = new MRatio(numer, new MConst(slopeVal, stdContext), stdContext);
		return new MBinary(MBinary.EQUALS, new MText("sd(" + getXUnits() + ")", stdContext),
																																					ratio, stdContext);
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomNormal(10, 0.0, 1.0, 3.0);
			generator.setSeed(nextSeed());
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable("", baseVar, "base", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {			//	side effect is to set meanResult and sdResult
																			//	OK for use by getMark() because they are not used in message unless overall result changes from ANS_UNCHECKED
		NumValue sourceMeanVal = ((StackMeanSdView)theView).getMean();
		NumValue sourceSdVal = ((StackMeanSdView)theView).getSD();
		double sourceMean = sourceMeanVal.toDouble();
		double sourceSd = sourceSdVal.toDouble();
		
		NumValue interceptVal = getIntercept();
		NumValue slopeVal = getSlope();
		double intercept = interceptVal.toDouble();
		double slope = slopeVal.toDouble();
		
		int resultDecimals = getMaxResult().decimals;
		double slop = Math.pow(10.01, -resultDecimals);
		
		if (meanResultPanel.isClear())
			meanResult = ANS_INCOMPLETE;
		else {
			double attempt = meanResultPanel.getAttempt().toDouble();
			double correct = (getDirection() == ScaleXYTemplatePanel.X_TO_Y)
													? intercept + slope * sourceMean : (sourceMean - intercept) / slope;
			if (Math.abs(attempt - correct) < slop)
				meanResult = ANS_CORRECT;
			else
				meanResult = ANS_WRONG;
		}
		
		if (sdResultPanel.isClear())
			sdResult = ANS_INCOMPLETE;
		else {
			double attempt = sdResultPanel.getAttempt().toDouble();
			double correct = (getDirection() == ScaleXYTemplatePanel.X_TO_Y)
																				? slope * sourceSd : sourceSd / slope;
			if (attempt <= 0.0)
				sdResult = ANS_INVALID;
			if (Math.abs(attempt - correct) < slop)
				sdResult = ANS_CORRECT;
			else
				sdResult = ANS_WRONG;
		}
		
		return (meanResult == ANS_CORRECT && sdResult == ANS_CORRECT) ? ANS_CORRECT
									: (meanResult == ANS_INCOMPLETE || sdResult == ANS_INCOMPLETE) ? ANS_INCOMPLETE
									: ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		meanResult = sdResult = ANS_TOLD;
		
		NumValue sourceMeanVal = ((StackMeanSdView)theView).getMean();
		NumValue sourceSdVal = ((StackMeanSdView)theView).getSD();
		double sourceMean = sourceMeanVal.toDouble();
		double sourceSd = sourceSdVal.toDouble();
		
		NumValue interceptVal = getIntercept();
		NumValue slopeVal = getSlope();
		double intercept = interceptVal.toDouble();
		double slope = slopeVal.toDouble();
		
		double mean, sd;
		if (getDirection() == ScaleXYTemplatePanel.X_TO_Y) {
			mean = intercept + slope * sourceMean;
			sd = slope * sourceSd;
			
			meanScaleXYTemplate.setXYValues(sourceMeanVal, interceptVal, slopeVal);
			sdScaleXYTemplate.setXYValues(sourceSdVal, kZeroValue, slopeVal);
			
			meanScaleYXTemplate.setYXValues(kNanValue, kNanValue, kNanValue);
			sdScaleYXTemplate.setYXValues(kNanValue, kNanValue, kNanValue);
		}
		else {
			mean = (sourceMean - intercept) / slope;
			sd = sourceSd / slope;
			
			meanScaleYXTemplate.setYXValues(sourceMeanVal, interceptVal, slopeVal);
			sdScaleYXTemplate.setYXValues(sourceSdVal, kZeroValue, slopeVal);
			
			meanScaleXYTemplate.setXYValues(kNanValue, kNanValue, kNanValue);
			sdScaleXYTemplate.setXYValues(kNanValue, kNanValue, kNanValue);
		}
		
		int resultDecimals = getMaxResult().decimals;
		meanResultPanel.showAnswer(new NumValue(mean, resultDecimals));
		sdResultPanel.showAnswer(new NumValue(sd, resultDecimals));
	}
	
	protected double getMark() {
		int overallResult = assessAnswer();			//	side effect sets sdResult and meanResult
		
		return (overallResult == ANS_CORRECT) ? 1 : (meanResult == ANS_CORRECT || sdResult == ANS_CORRECT) ? 0.5 : 0;
	}
}