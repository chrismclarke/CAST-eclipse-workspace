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


public class GuessCorrApplet extends MeanSDExerciseApplet {
//	static final private double kExactFactor = 0.3;
													//	'exact' sd must be within kExactFactor times the legal range
//	static final private double kCloseFactor = 1.0;
													//	'close' sd must be within kCloseFactor times the legal range
	
	private RandomNormal xGenerator, yGenerator;
	
	private XLabel yVarNameLabel;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	private ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, translate("Correlation coefficient") + ", r =", 6);
				registerStatusItem("corr", resultPanel);
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
		registerParameter("count", "int");
		registerParameter("xAxis", "string");
		registerParameter("xVarName", "string");
		registerParameter("yAxis", "string");
		registerParameter("yVarName", "string");
		registerParameter("yMeanSD", "jointMeanSD");		//	must generate mean and sd together
		registerParameter("corr", "const");
	}
	
	protected NumValue getMean() {
		return null;				//	not used since y-mean is not shown in question
	}
	
	protected NumValue getSD() {
		return null;				//	not used since y-sd is not shown in question
	}
	
	public String getAxisInfo() {
		return getYAxisInfo();			//	MeanSD is only used for Y variable
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private String getXAxisInfo() {
		return getStringParam("xAxis");
	}
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private NumValue getXMean() {
		StringTokenizer st = new StringTokenizer(getXAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		return new NumValue((min + max) / 2, 9);
	}
	
	private NumValue getXSD() {
		StringTokenizer st = new StringTokenizer(getXAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		return new NumValue((max - min) / 6, 9);
	}
	
	private String getYAxisInfo() {
		return getStringParam("yAxis");
	}
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private NumValue getYMean() {
		return ((MeanSD)getObjectParam("yMeanSD")).getMean();
	}
	
	private NumValue getYSD() {
		return ((MeanSD)getObjectParam("yMeanSD")).getSD();
	}
	
	private NumValue getCorr() {
		return getNumValueParam("corr");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			yVarNameLabel = new XLabel("", XLabel.LEFT, this);
		thePanel.add("North", yVarNameLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				yAxis = new VertAxis(this);
			displayPanel.add("Left", yAxis);
			
				xAxis = new HorizAxis(this);
			displayPanel.add("Bottom", xAxis);
			
				ScatterView theView = new ScatterView(data, this, xAxis, yAxis, "x", "y");
				theView.lockBackground(Color.white);
			displayPanel.add("Center", theView);
		
		thePanel.add("Center", displayPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		yVarNameLabel.setText(getYVarName());
		
		yAxis.readNumLabels(getYAxisInfo());
		yAxis.invalidate();
		
		xAxis.readNumLabels(getXAxisInfo());
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		double xMean = getXMean().toDouble();
		double xSd = getXSD().toDouble();
		double yMean = getYMean().toDouble();
		double ySd = getYSD().toDouble();
		double corr = getCorr().toDouble();
		
		int n = getCount();
		NumSampleVariable xCoreVar = (NumSampleVariable)data.getVariable("xBase");
		xCoreVar.setSampleSize(n);
		xCoreVar.generateNextSample();
		
		NumSampleVariable yCoreVar = (NumSampleVariable)data.getVariable("yBase");
		yCoreVar.setSampleSize(n);
		yCoreVar.generateNextSample();
		
		ScaledVariable xVar = (ScaledVariable)data.getVariable("x");
		xVar.setScale(xMean, xSd, 9);
		
		CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable("y");
		yVar.setMeanSdCorr(yMean, ySd, corr, 9);
		
		data.variableChanged("x");
		data.variableChanged("y");
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
//		String dataType = hasOption("normal") ? "normal distribution" : "data set";
		double exact = getCorr().toDouble();
		double attempt = getAttempt();
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Estimate the correlation coefficient by eye from the scatterplot then type it into the text-edit box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You have not typed a value for the correlation coefficient.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The correlation coefficient can never be greater than 1 or less than -1.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(toldString(exact));
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your guess is as close as could be expected by eye.\n");
				messagePanel.insertText("(The exact correlation coefficient is " + getCorr() + ".)");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText(closeHintString(exact, attempt));
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText(wrongHintString(exact, attempt));
				break;
		}
	}
	
	private String getBetweenString() {
		String yName = getYVarName();
		int commaIndex = yName.indexOf(',');
		if (commaIndex >= 0)
			yName = yName.substring(0, commaIndex);		//	delete units
		
		String xName = getXVarName();
		commaIndex = xName.indexOf(',');
		if (commaIndex >= 0)
			xName = xName.substring(0, commaIndex);		//	delete units
		
		String betweenString = "between " + yName + " and " + xName;
		return betweenString;
	}
	
	private String closeHintString(double exact, double attempt) {
		String betweenString = getBetweenString();
		if (exact > 0 && attempt < 0)
			return "There is positive association " + betweenString + " (cloud of points from bottom left to top right) but your guess at r is negative.";
		else if (exact < 0 && attempt > 0)
			return "There is negative association " + betweenString + " (cloud of points from top left to bottom right) but your guess at r is positive.";
		else if (Math.abs(exact) < Math.abs(attempt))
			return "The correlation coefficient " + betweenString + " is nearer to zero (linear relationship is weaker) than " + attempt + ".";
		else
			return "The correlation coefficient " + betweenString + " is further from zero (linear relationship is stronger) than " + attempt + ".";
	}
	
	private String wrongHintString(double exact, double attempt) {
		if ((exact > 0) != (attempt > 0))
			return "If the cloud of points goes from bottom left to top right, r is positive.\nIf the cloud goes from top left to bottom right, r is negative.";
		else
			return "The closer the points to a sloping straight line, the further r is from zero.\nThe closer the cloud of points to a circle or horizontal ellipse, the closer r is to zero.";
	}
	
	private String toldString(double exact) {
		String betweenString = getBetweenString();
		if (Math.abs(exact) < 0.001)
			return "There is no linear association " + betweenString + ".";
		else if (Math.abs(exact) < 0.2)
			return "There is virtually no linear association " + betweenString + ".";
		String s = (exact > 0)
					? "There is positive association " + betweenString + " (cloud of points from bottom left to top right) so r is positive"
					: "There is negative association " + betweenString + " (cloud of points from top left to bottom right) so r is negative";
		s += ". The relationship is ";
		if (Math.abs(exact) < 0.6)
			s += "weak.";
		else if (Math.abs(exact) < 0.85)
			s += "only moderately strong.";
		else
			s += "very strong.";
		return s;
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			xGenerator = new RandomNormal(10, 0.0, 1.0, 2.5);		//	+/- 2.5 SD
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xBaseVar = new NumSampleVariable("ZX", xGenerator, 9);
			xBaseVar.generateNextSample();
		data.addVariable("xBase", xBaseVar);
		
			ScaledVariable xVar = new ScaledVariable("", xBaseVar, "xBase", 0.0, 1.0, 9);
			xVar.setRoundValues(true);
		data.addVariable("x", xVar);
		
			yGenerator = new RandomNormal(10, 0.0, 1.0, 2.5);		//	+/- 2.5 SD
			yGenerator.setSeed(nextSeed());
			NumSampleVariable yBaseVar = new NumSampleVariable("ZY", yGenerator, 9);
			yBaseVar.generateNextSample();
		data.addVariable("yBase", yBaseVar);
			
			CorrelatedVariable yVar = new CorrelatedVariable("", data, "xBase", "yBase", 9);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	
//-----------------------------------------------------------
	
	private double evalSlop(double correctCorr, double accuracyParam) {
		return 0.1 * (2.0 + accuracyParam) * (1.0 - Math.pow(Math.abs(correctCorr), accuracyParam));
	}
	
	protected int assessAnswer() {
		double correct = getCorr().toDouble();
		double lowExact = (correct < 0) ? correct - evalSlop(correct, 1.5) : correct - evalSlop(correct, 2.5);
		double highExact = (correct < 0) ? correct + evalSlop(correct, 2.5) : correct + evalSlop(correct, 1.5);
		
		double lowClose = (correct < 0) ? correct - evalSlop(correct, 2.5) : correct - evalSlop(correct, 4);
		double highClose = (correct < 0) ? correct + evalSlop(correct, 4) : correct + evalSlop(correct, 2.5);
		
		double attempt = getAttempt();
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if(attempt > 1.0 || attempt < -1.0)
			return ANS_INVALID;
		else
			return (attempt >= lowExact && attempt <= highExact) ? ANS_CORRECT
									: (attempt >= lowClose && attempt <= highClose) ? ANS_CLOSE : ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		resultPanel.showAnswer(getCorr());
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : (assessAnswer() == ANS_CLOSE) ? 0.8 : 0;
	}
}