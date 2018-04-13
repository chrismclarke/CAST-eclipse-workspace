package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreGraphics.*;
import coreVariables.*;
import exercise2.*;
import formula.*;
import models.*;

import regn.*;
import exerciseBivar.*;


public class TransformPredictApplet extends ExerciseApplet {
	static final private int IDENTITY_TRANS = 0;
	static final private int LOG_TRANS = 1;
	
	static final private NumValue kMaxLnValue = new NumValue(-9.0, 3);
	static final private NumValue kOneValue = new NumValue(1, 0);
	static final private NumValue kZeroValue = new NumValue(0, 0);
	
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
	
	private RandomRectangular xGenerator;
	private RandomNormal zGenerator;
	
	private XLabel yVarNameLabel, yTransVarNameLabel;
	private HorizAxis xAxis, xTransAxis;
	private VertAxis yAxis, yTransAxis;
	private ScatterView xyView, xyTransView;
	
	private LinearEquationView lsEqn;
	
	private FunctionEvalTemplatePanel lnTemplate, expTemplate;
	private FittedValTemplatePanel linearTemplate;
	
	protected ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, "Prediction =", 6);
				registerStatusItem("prediction", resultPanel);
			bottomPanel.add(resultPanel);
			
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
//		registerParameter("index", "int");					//	always registered
		registerParameter("xVarName", "string");
		registerParameter("xAxis", "string");
		registerParameter("lnxAxis", "string");
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "string");
		registerParameter("lnyAxis", "string");
		registerParameter("count", "int");
		registerParameter("corr", "const");
		registerParameter("transformType", "choice");		//	0 = (x,y)  1 = (x,lny)  2 = (lnx,y)  3 = (lnx,lny)
		registerParameter("xValue", "const");
		registerParameter("maxIntercept", "const");
		registerParameter("maxSlope", "const");
		registerParameter("maxExp", "const");
		registerParameter("accuracy", "const");
	}
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private String getXAxis() {
		return getStringParam("xAxis");
	}
	
	private String getLnXAxis() {
		return getStringParam("lnxAxis");
	}
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private String getYAxis() {
		return getStringParam("yAxis");
	}
	
	private String getLnYAxis() {
		return getStringParam("lnyAxis");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private NumValue getCorr() {
		return getNumValueParam("corr");
	}
	
	private int getXTransformType() {
		return getIntParam("transformType") / 2;
	}
	
	private int getYTransformType() {
		return getIntParam("transformType") % 2;
	}
	
	private NumValue getXValue() {
		return getNumValueParam("xValue");
	}
	
	private NumValue getMaxIntercept() {
		return getNumValueParam("maxIntercept");
	}
	
	private NumValue getMaxSlope() {
		return getNumValueParam("maxSlope");
	}
	
	private NumValue getMaxExpValue() {
		return getNumValueParam("maxExp");
	}
	
	private NumValue getAccuracy() {
		return getNumValueParam("accuracy");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
		thePanel.add("Center", getDataPanels(data));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		
				XPanel eqnPanel = new XPanel();
				eqnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					lsEqn = new LinearEquationView(data, this, "ls", "", "", null, null, null, null);
				
				eqnPanel.add(lsEqn);
				
			bottomPanel.add(eqnPanel);
			
			bottomPanel.add(calculationPanel());
		
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	private XPanel calculationPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 5);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 10));
			
				XPanel templatePanel = new InsetPanel(-40, 0);		//	to compensate for 40 gap in FlowLayout
				templatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
				
					FormulaContext context = new FormulaContext(kTemplateColor, getStandardFont(), this);
					lnTemplate = new FunctionEvalTemplatePanel(Function.LN_FUNCTION, kMaxLnValue, context);
					registerStatusItem("lnTemplate", lnTemplate);
				templatePanel.add(lnTemplate);
		
					expTemplate = new FunctionEvalTemplatePanel(Function.EXP_FUNCTION, getMaxExpValue(), context);
					registerStatusItem("expTemplate", expTemplate);
				templatePanel.add(expTemplate);
			
			innerPanel.add(templatePanel);
			
				XPanel linearPanel = new XPanel();
				linearPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					linearTemplate = new FittedValTemplatePanel(context);
					registerStatusItem("linearTemplate", linearTemplate);
					
				linearPanel.add(linearTemplate);
			
			innerPanel.add(linearPanel);
		
			innerPanel.lockBackground(kTemplateBackground);
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private XPanel getDataPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0));
			
			yVarNameLabel = new XLabel("", XLabel.LEFT, this);
			yAxis = new VertAxis(this);
			xAxis = new HorizAxis(this);
			xyView = new ScatterView(data, this, xAxis, yAxis, "x", "y");
		
		thePanel.add(ProportionLayout.LEFT, getScatterPanel(xAxis, yAxis, xyView, yVarNameLabel));
			
			yTransVarNameLabel = new XLabel("", XLabel.LEFT, this);
			yTransAxis = new VertAxis(this);
			xTransAxis = new HorizAxis(this);
			xyTransView = new ScatterAndLineView(data, this, xTransAxis, yTransAxis, "x", "y", "ls");
		
		thePanel.add(ProportionLayout.RIGHT, getScatterPanel(xTransAxis, yTransAxis, xyTransView,
																																						yTransVarNameLabel));
		
		return thePanel;
	}
	
	private XPanel getScatterPanel(HorizAxis xAxis, VertAxis yAxis, ScatterView xyView, XLabel yVarNameLabel) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
		thePanel.add("North", yVarNameLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
			displayPanel.add("Left", yAxis);
			
			displayPanel.add("Bottom", xAxis);
			
				xyView.lockBackground(Color.white);
			displayPanel.add("Center", xyView);
		
		thePanel.add("Center", displayPanel);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		int xTransform = getXTransformType();
		int yTransform = getYTransformType();
		
		String xKey = (xTransform == IDENTITY_TRANS) ? "x" : "exp(x)";
		String yKey = (yTransform == IDENTITY_TRANS) ? "y" : "exp(y)";
		
		xyView.changeVariables(yKey, xKey);
		
		xAxis.readNumLabels(getXAxis());
		yAxis.readNumLabels(getYAxis());
		
		yVarNameLabel.setText(getYVarName());
		
		xAxis.setAxisName(getXVarName());
	
		data.variableChanged(xKey);
		
		xTransAxis.readNumLabels(xTransform == IDENTITY_TRANS ? getXAxis() : getLnXAxis());
		yTransAxis.readNumLabels(yTransform == IDENTITY_TRANS ? getYAxis() : getLnYAxis());
		
		yTransVarNameLabel.setText(getResponseName());
		xTransAxis.setAxisName(getExplanName());
	
		data.variableChanged("x");
		xTransAxis.invalidate();
		yTransAxis.invalidate();
		
		lsEqn.setExplanName(getExplanName());
		lsEqn.setYName(getResponseName());
		lsEqn.setMinMaxParams(getMaxIntercept(), getMaxIntercept(), getMaxSlope(), getMaxSlope());
																							//		does invalidate()
		lsEqn.invalidate();
		lsEqn.repaint();
		
		lnTemplate.setXValue(kOneValue);
		
		expTemplate.setXValue(kZeroValue);
		
		linearTemplate.setValues(kZeroValue, kZeroValue, kZeroValue);
		
		resultPanel.clear();
	}
	
	private String getExplanName() {
		return (getXTransformType() == IDENTITY_TRANS) ? getXVarName() : ("ln (" + getXVarName() + ")");
	}
	
	private String getResponseName() {
		return (getYTransformType() == IDENTITY_TRANS) ? getYVarName() : ("ln (" + getYVarName() + ")");
	}
	
	protected void setDataForQuestion() {
		int xTransform = getXTransformType();
		int yTransform = getYTransformType();
		int n = getCount();
		
		StringTokenizer st = new StringTokenizer(getXAxis());
		double xMin = Double.parseDouble(st.nextToken());
		double xMax = Double.parseDouble(st.nextToken());
		if (xTransform == LOG_TRANS) {
			xMin = Math.log(xMin);
			xMax = Math.log(xMax);
			st = new StringTokenizer(getLnXAxis());
			if (Double.isNaN(xMin) || Double.isInfinite(xMin))
				xMin = Double.parseDouble(st.nextToken());
			else
				xMin = Math.max(xMin, Double.parseDouble(st.nextToken()));
			xMax = Math.min(xMax, Double.parseDouble(st.nextToken()));
		}
		
		NumSampleVariable xVar = (NumSampleVariable)data.getVariable("x");
		RandomRectangular xGenerator = (RandomRectangular)xVar.getGenerator();
		xGenerator.setMinMax(xMin, xMax);
		
		xVar.setSampleSize(n);
		xVar.generateNextSample();
		
		NumSampleVariable zVar = (NumSampleVariable)data.getVariable("z");
		zVar.setSampleSize(n);
		zVar.generateNextSample();
		
		CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable("y");
		
		st = new StringTokenizer(getYAxis());
		double yMin = Double.parseDouble(st.nextToken());
		double yMax = Double.parseDouble(st.nextToken());
		if (yTransform == LOG_TRANS) {
			yMin = Math.log(yMin);
			yMax = Math.log(yMax);
			st = new StringTokenizer(getLnYAxis());
			if (Double.isNaN(yMin) || Double.isInfinite(yMin))
				yMin = Double.parseDouble(st.nextToken());
			else
				yMin = Math.max(yMin, Double.parseDouble(st.nextToken()));
			yMax = Math.min(yMax, Double.parseDouble(st.nextToken()));
		}
		
		Random random01 = new Random(nextSeed());
		double targetMin = yMin + (yMax - yMin) * 0.05 * random01.nextDouble();
		double targetMax = yMax - (yMax - yMin) * 0.05 * random01.nextDouble();
		
		yVar.setMinMaxCorr(targetMin, targetMax, getCorr().toDouble(), 9);
		
		LinearModel lsModel = (LinearModel)data.getVariable("ls");
//		String modelXKey = (xTransform == LOG_TRANS) ? "exp(x)" : "x";
//		String modelYKey = (yTransform == LOG_TRANS) ? "exp(y)" : "y";
//		lsModel.setXKey(modelXKey);
//		lsModel.setLSParams(modelYKey, getMaxIntercept().decimals, getMaxSlope().decimals, 9);
		lsModel.setLSParams("y", getMaxIntercept().decimals, getMaxSlope().decimals, 9);
	}
	
/*
	private void generateData(NumSampleVariable sampleVar) {
		int n = getCount();
		sampleVar.setSampleSize(n);
		sampleVar.generateNextSample();
	}
*/
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the equation of the least squares line to predict the " + getYVarName() + " then type it into the text-edid box above.");
				messagePanel.insertText("\n(You should find that the templates with pale background help to perform the calculations.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You have not typed a value for the prediction.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				if (getXTransformType() == IDENTITY_TRANS)
					messagePanel.insertText("Since the explanatory variable in the LS equation is "
																	+ getXVarName() + ", there is no need to transform the value "
																	+ getXValue() + " before putting its value into the equation.");
				else
					messagePanel.insertText("Since the explanatory variable in the LS equation is ln("
																	+ getXVarName() + "), the value ln(" + getXValue()
																	+ ") must be used in the equation.");
				
				messagePanel.insertText("\nFrom this, the LS equation gives the value "
																													+ linearTemplate.getResult() + ". ");
				
				if (getYTransformType() == IDENTITY_TRANS)
					messagePanel.insertText("Since the response appears untransformed in the LS equation, this is our final prediction.");
				else
					messagePanel.insertText("Since the response in this LS equation is ln(" + getYVarName()
								+ "), the predicted " + getYVarName() + " should be exp(" + linearTemplate.getResult() + ").");
				
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly evaluated the prediction.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertRedText("Use the templates to evaluate your prediction to within " + getAccuracy() + " of the correct value.");
				insertHelpMessage(messagePanel);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("You have not correctly predicted " + getYVarName() + ".");
				insertHelpMessage(messagePanel);
				break;
		}
	}
	
	private void insertHelpMessage(MessagePanel messagePanel) {
		if (getXTransformType() == IDENTITY_TRANS && getYTransformType() == IDENTITY_TRANS) {
			messagePanel.insertText("\nMake sure that you did ");
			messagePanel.insertBoldText("not");
			messagePanel.insertText(" do a ln() transformation of " + getXVarName()
											+ " at the start or an exp() transformation of the linear prediction at the end.");
		}
		else if (getXTransformType() == IDENTITY_TRANS && getYTransformType() == LOG_TRANS) {
			messagePanel.insertText("\nMake sure that you did ");
			messagePanel.insertBoldText("not");
			messagePanel.insertText(" do a ln() transformation of " + getXVarName() + " at the start but ");
			messagePanel.insertBoldText("did");
			messagePanel.insertText(" an exp() transformation of the linear prediction at the end.");
		}
		else if (getXTransformType() == LOG_TRANS && getYTransformType() == IDENTITY_TRANS) {
			messagePanel.insertText("\nMake sure that you ");
			messagePanel.insertBoldText("did");
			messagePanel.insertText(" a ln() transformation of " + getXVarName() + " at the start but did ");
			messagePanel.insertBoldText("not");
			messagePanel.insertText(" do an exp() transformation of the linear prediction at the end.");
		}
		else if (getXTransformType() == LOG_TRANS && getYTransformType() == LOG_TRANS) {
			messagePanel.insertText("\nMake sure that you ");
			messagePanel.insertBoldText("both");
			messagePanel.insertText(" did a ln() transformation of " + getXVarName() + " at the start and ");
			messagePanel.insertBoldText("also");
			messagePanel.insertText(" did an exp() transformation of the linear prediction at the end.");
		}
	}
	
	protected int getMessageHeight() {
		return 140;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			xGenerator = new RandomRectangular(10, 0.0, 1.0);
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xVar = new NumSampleVariable("", xGenerator, 9);
		data.addVariable("x", xVar);
		
			zGenerator = new RandomNormal(10, 0.0, 1.0, 2.5);		//	+/- 2.5 SD
			zGenerator.setSeed(nextSeed());
			NumSampleVariable zVar = new NumSampleVariable("", zGenerator, 9);
		data.addVariable("z", zVar);
		
			CorrelatedVariable yVar = new CorrelatedVariable("", data, "x", "z", 9);
		data.addVariable("y", yVar);
		
			LogVariable expXVar = new LogVariable("", data, "x", 9);
			expXVar.setNaturalLogs(true);
			expXVar.setInverse();		//		exp() instead of ln()
		data.addVariable("exp(x)", expXVar);
		
			LogVariable expYVar = new LogVariable("", data, "y", 9);
			expYVar.setNaturalLogs(true);
			expYVar.setInverse();		//		exp() instead of ln()
		data.addVariable("exp(y)", expYVar);
		
			LinearModel lsModel = new LinearModel("lsModel", data, "x");
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	private double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	private double getExact(boolean doRounding) {		//	not really exact, but what the templates would display
		LinearModel lsModel = (LinearModel)data.getVariable("ls");
		double intercept = Double.parseDouble(lsModel.getIntercept().toString());
		double slope = Double.parseDouble(lsModel.getSlope().toString());
		
		double x = getXValue().toDouble();
		if (getXTransformType() == LOG_TRANS) {
			x = Math.log(x);
			if (doRounding)
				x = roundToDecimals(x, 3);		//	accuracy displayed in template
		}
		
		double prediction = intercept + slope * x;
		if (doRounding)
			prediction = roundToDecimals(prediction, 3);		//	accuracy displayed in template
		
		if (getYTransformType() == LOG_TRANS) {
			prediction = Math.exp(prediction);
			if (doRounding)
				prediction = roundToDecimals(prediction, getMaxExpValue().decimals);		//	accuracy displayed in template
		}
		
		return prediction;
	}
	
	private double roundToDecimals(double x, int decimals) {
		double roundFactor = Math.pow(10, decimals);
		return Math.rint(x * roundFactor) / roundFactor;
	}
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double absError0 = Math.abs(getAttempt() - getExact(false));	//	as evaluated on calculator
			double absError1 = Math.abs(getAttempt() - getExact(true));		//	as evaluated with templates
			if (absError0 < getAccuracy().toDouble() || absError1 < getAccuracy().toDouble())
				return ANS_CORRECT;
			else if (absError0 < getAccuracy().toDouble() * 10 || absError1 < getAccuracy().toDouble() * 10)
				return ANS_CLOSE;
			else
				return ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue xValue = getXValue();
		if (getXTransformType() == IDENTITY_TRANS)
			lnTemplate.setXValue(NumValue.NAN_VALUE);
		else {
			lnTemplate.setXValue(xValue);
			xValue = lnTemplate.getResult();
		}
		
		LinearModel lsModel = (LinearModel)data.getVariable("ls");
		linearTemplate.setValues(lsModel.getIntercept(), lsModel.getSlope(), xValue);
		
		NumValue prediction = linearTemplate.getResult();
		if (getYTransformType() == IDENTITY_TRANS)
			expTemplate.setXValue(NumValue.NAN_VALUE);
		else {
			expTemplate.setXValue(prediction);
			prediction = expTemplate.getResult();
		}
		
		resultPanel.showAnswer(prediction);
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
	
}