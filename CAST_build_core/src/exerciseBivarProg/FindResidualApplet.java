package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import models.*;
import random.*;
import exercise2.*;

import exerciseBivar.*;


public class FindResidualApplet extends ExerciseApplet {
	private RandomInteger selectionGenerator;
	private RandomNormal xGenerator, yGenerator;
	
	private XLabel yNameLabel;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	protected ResidPredictScatterView theView;
	
//	private XNumberEditPanel residEdit;
//	private XChoice moreLessChoice;
	
	protected ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 8));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
//			XPanel mainPanel = new XPanel();
			XPanel mainPanel = new InsetPanel(getWorkingInset(), 0);
			mainPanel.setLayout(new BorderLayout(10, 0));
			mainPanel.add("Center", getWorkingPanels(data));
				
//			mainPanel.add("East", getResultPanel());
			
		add("Center", mainPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, getResultName(), getResultUnits(), 6);
				registerStatusItem("answer", resultPanel);
			bottomPanel.add(resultPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
	protected String getResultName() {
		return translate("Residual") + " =";
	}
	
	protected String getResultUnits() {
		return null;
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("xVarName", "string");
		registerParameter("xAxis", "string");
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "string");
		registerParameter("count", "int");
		registerParameter("corr", "const");
		registerParameter("decimals", "int");
		registerParameter("interceptDecimals", "int");
		registerParameter("slopeDecimals", "int");
	}
	
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
	
	protected int getDecimals() {
		return getIntParam("decimals");
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
	
	
//-----------------------------------------------------------
	
	protected int getWorkingInset() {		//	left and right
		return 60;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", getScatterPanel(data));
		
		return thePanel;
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
			
				theView = createScatterView(data, xAxis, yAxis, "x", "y", "ls");
				theView.lockBackground(Color.white);
			displayPanel.add("Center", theView);
			
		thePanel.add("Center", displayPanel);
		
		return thePanel;
	}
	
	protected ResidPredictScatterView createScatterView(DataSet data, HorizAxis xAxis, VertAxis yAxis,
																									String xKey, String yKey, String lsKey) {
		return new ResidPredictScatterView(data, this, xAxis, yAxis, xKey, yKey, lsKey);
	}

/*	
	private XPanel getResultPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 30));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				XLabel yValueText = new XLabel("The value for Y is", XLabel.LEFT, this);
			topPanel.add(yValueText);
			
				XPanel residPanel = new XPanel();
				residPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
					residEdit = new XNumberEditPanel(null, 6, this);
				residPanel.add(residEdit);
				
					moreLessChoice = new XChoice(this);
					moreLessChoice.addItem("more");
					moreLessChoice.addItem("less");
				residPanel.add(moreLessChoice);
			topPanel.add(residPanel);
			
			topPanel.add(new XLabel("than predicted by the", XLabel.LEFT, this));
			topPanel.add(new XLabel("least squares line.", XLabel.LEFT, this));
			
		thePanel.add(topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				XLabel residText = new XLabel("Therefore the residual is", XLabel.LEFT, this);
			bottomPanel.add(residText);
			
				resultPanel = new ResultValuePanel(this, null, 6);
			bottomPanel.add(resultPanel);
			
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
*/
	
	protected void setDisplayForQuestion() {
		theView.setShowResult(false);
		
		xAxis.readNumLabels(getXAxisInfo());
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		yAxis.readNumLabels(getYAxisInfo());
		yNameLabel.setText(getYVarName());
		yAxis.invalidate();
		
		resultPanel.clear();
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
		yVar.setMeanSdCorr(getYMean(), getYSD(), getCorr().toDouble(), getDecimals());
		yVar.name = getYVarName();
		
		LinearModel ls = (LinearModel)data.getVariable("ls");
		ls.setLSParams("y", getInterceptDecimals(), getSlopeDecimals(), 0);
		
		doSelection(n);
	}
	
	protected void doSelection(int n) {
		if (selectionGenerator == null)
			selectionGenerator = new RandomInteger(0, n - 1, 1, nextSeed());
		else
			selectionGenerator.setMinMax(1, n - 1);
		
		data.variableChanged("y", selectionGenerator.generateOne());
	}
	
//-----------------------------------------------------------
	
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
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		if (selectionGenerator != null)
			selectionGenerator.setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		NumVariable xVar = (NumVariable)data.getVariable("x");
		LinearModel ls = (LinearModel)data.getVariable("ls");
		int selectedIndex = data.getSelection().findSingleSetFlag();
		
		NumValue y = new NumValue(yVar.doubleValueAt(selectedIndex), getDecimals());
		double x = xVar.doubleValueAt(selectedIndex);
		NumValue fit = new NumValue(ls.evaluateMean(x), getDecimals());
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the approximate value of the residual for the selected point.\n(Estimate it by eye.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error\n");
				messagePanel.insertText("You must type a value for the residual.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The actual " + getYVarName() + " is " + y + " and the least squares line predicts it to be " + fit + ". The residual is the difference between these values.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				NumValue resid = new NumValue(y.toDouble() - fit.toDouble(), getDecimals());
				messagePanel.insertText("This is close enough to the correct residual.\n(The exact value is " + y + " - " + fit + " = " + resid + ".)");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertRedText("However you should be able to estimate the value of the residual a bit more accurately.\n");
				messagePanel.insertText("Hint:");
				messagePanel.insertRedText("The actual " + getYVarName() + " is " + y + " and the fitted value is " + fit + ".");
				break;
			case ANS_WRONG:
				double correct = y.toDouble() - fit.toDouble();
				double attempt = getAttempt();
				if (correct > 0 && attempt < 0) {
					messagePanel.insertRedHeading("Wrong!\n");
					messagePanel.insertRedText("The cross is above the least squares line but you have given a negative residual.");
				}
				else if (correct < 0 && attempt > 0) {
					messagePanel.insertRedHeading("Wrong!\n");
					messagePanel.insertRedText("The cross is below the least squares line but you have given a positive residual.");
				}
				else {
					messagePanel.insertRedHeading("Not close enough!\n");
					messagePanel.insertRedText("The residual is the vertical distance from the cross to the least squares line.");
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		if (super.noteChangedWorking()) {
			theView.setShowResult(false);
			theView.repaint();
			
			return true;
		}
		else
			return false;
	}
	
	protected double getSlop(int slopPix) {
		int axisPix = yAxis.axisLength;
		return (yAxis.maxOnAxis - yAxis.minOnAxis) * slopPix / axisPix;
	}
	
	protected double getExactSlop() {
		return getSlop(7);
	}
	
	protected double getCloseSlop() {
		return getSlop(15);
	}
	
	private double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected double getCorrect() {
		int selectedIndex = data.getSelection().findSingleSetFlag();
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		NumVariable xVar = (NumVariable)data.getVariable("x");
		LinearModel ls = (LinearModel)data.getVariable("ls");
		
		double x = xVar.doubleValueAt(selectedIndex);
		double y = yVar.doubleValueAt(selectedIndex);
		double fit = ls.evaluateMean(x);
		
		return y - fit;
	}
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double exactSlop = getExactSlop();
			double closeSlop = getCloseSlop();
			
			double correct = getCorrect();
			double attempt = getAttempt();
			double error = Math.abs(attempt - correct);
			
			return (error <= exactSlop) ? ANS_CORRECT : (error <= closeSlop) ? ANS_CLOSE : ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
		if (result != ANS_INCOMPLETE) {
			theView.setShowResult(true);
			theView.repaint();
		}
	}
	
	protected void showCorrectWorking() {
		resultPanel.showAnswer(new NumValue(getCorrect(), getDecimals()));
		
		theView.setShowResult(true);
		theView.repaint();
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
}