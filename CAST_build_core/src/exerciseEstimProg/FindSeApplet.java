package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;
import formula.*;


import exerciseSD.*;
import exerciseEstim.*;


public class FindSeApplet extends ExerciseApplet {
	static final private double kExactFactor = 0.001;
	static final private double kApproxFactor = 0.01;
	
	static final private NumValue kOneValue = new NumValue(1, 0);
	
	private RandomNormal generator;
	
	private HorizAxis theAxis;
	private StackMeanSdView theView;
	
	private RatioTemplatePanel ratioTemplate;
	
	private ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				XPanel ansPanel = new XPanel();
				ansPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					XPanel insetPanel = new InsetPanel(10, 2);
					insetPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
						resultPanel = new ResultValuePanel(this, translate("Standard error") + " =", "", 6);
						resultPanel.setFont(getBigFont());
						registerStatusItem("se", resultPanel);
					insetPanel.add(resultPanel);
					
					insetPanel.lockBackground(kAnswerBackground);
				ansPanel.add(insetPanel);
				
			bottomPanel.add(ansPanel);
			
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
		registerParameter("varName", "string");
		registerParameter("noOfValues", "int");
		registerParameter("axis", "string");
		registerParameter("mean", "const");
		registerParameter("sd", "const");
		registerParameter("maxSe", "const");
		registerParameter("units", "string");
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private int getNoOfValues() {
		return getIntParam("noOfValues");
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private NumValue getMean() {
		return getNumValueParam("mean");
	}
	
	private NumValue getSd() {
		return getNumValueParam("sd");
	}
	
	private NumValue getMaxSe() {
		return getNumValueParam("maxSe");
	}
	
	private String getUnits() {
		return getStringParam("units");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel dotPlotPanel = new XPanel();
			dotPlotPanel.setLayout(new AxisLayout());
			
				theAxis = new HorizAxis(this);
			dotPlotPanel.add("Bottom", theAxis);
			
				theView = new StackMeanSdView(data, this, theAxis, "y", 9);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
			dotPlotPanel.add("Center", theView);
			
		thePanel.add("Center", dotPlotPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				ratioTemplate = new RatioTemplatePanel(new NumValue(0), stdContext);
				registerStatusItem("ratio", ratioTemplate);
				
			bottomPanel.add(ratioTemplate);
				
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		int decimals = getMaxSe().decimals;
		theView.setDecimals(decimals);
		
		ratioTemplate.changeMaxValue(getMaxSe());
		ratioTemplate.setValues(kOneValue, kOneValue);
		
		resultPanel.changeUnits(getUnits());
		
		data.variableChanged("y");
	}
	
	protected void setDataForQuestion() {
		int n = getNoOfValues();
		
		generator.setMean(getMean().toDouble());
		generator.setSD(getSd().toDouble());
		
		NumSampleVariable yVar = (NumSampleVariable)data.getVariable("y");
		yVar.setSampleSize(n);
		yVar.name = getVarName();
		yVar.generateNextSample();
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the template to help find the standard error of the sample mean then type it into the box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type a value for the standard error in the box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("Standard errors can never be negative.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The standard error of the mean is the sample standard deviation divided by the square root of the sample size,\n");
				insertFormula(messagePanel);
			break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly found the standard error of the sample mean,\n");
				insertFormula(messagePanel);
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				double exact = evaluateSe();
				double attempt = getAttempt();
				if (attempt < exact)
					messagePanel.insertRedText("Your attempt is close to the correct standard error, but is a little too low.");
				else
					messagePanel.insertRedText("Your attempt is close to the correct standard error, but is a little too high.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("Your answer is not close enough to the correct standard error.");
				break;
		}
	}
	
	private void insertFormula(MessagePanel messagePanel) {
		FormulaContext stdContext = new FormulaContext(null, null, this);
		MFormula rootN = new MRoot(new MText(String.valueOf(getNoOfValues()), stdContext), stdContext);
		MFormula ratio = new MRatio(new MText(theView.getSD().toString(), stdContext), rootN, stdContext);
		
		MFormula name = new MText("se(x#bar#)", stdContext);
		
		messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
		messagePanel.insertFormula(new MBinary(MBinary.EQUALS, name, ratio, stdContext));
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomNormal(10, 0.0, 1.0, 2.0);
			generator.setSeed(nextSeed());
			NumSampleVariable yVar = new NumSampleVariable("Y", generator, 9);
			yVar.generateNextSample();
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	private double evaluateSe() {
		double s = theView.getSD().toDouble();
		int n = getNoOfValues();
		return s / Math.sqrt(n);
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		double attempt = getAttempt();
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if (attempt < 0.0)
			return ANS_INVALID;
			
		double correct = evaluateSe();
		double absError = Math.abs(attempt - correct);
		
		double exactSlop = correct * kExactFactor;
		double approxSlop = correct * kApproxFactor;
		
		return (absError < exactSlop) ? ANS_CORRECT
						: (absError < approxSlop) ? ANS_CLOSE : ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue sd = theView.getSD();
		double rootN = Math.sqrt(getNoOfValues());
		int decimals = (Math.abs(rootN - Math.rint(rootN)) < 0.00001) ? 0 : 4;
		NumValue rootNValue = new NumValue(rootN, decimals);
		
		ratioTemplate.setValues(sd, rootNValue);
		
		int seDecimals = getMaxSe().decimals;
		resultPanel.showAnswer(new NumValue(evaluateSe(), seDecimals));
	}
	
	protected double getMark() {
		int result = assessAnswer();
		
		return (result == ANS_CORRECT) ? 1.0 : (result == ANS_CLOSE) ? 0.5 : 0;
	}
}