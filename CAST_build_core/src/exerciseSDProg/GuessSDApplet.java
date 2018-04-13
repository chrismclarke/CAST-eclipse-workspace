package exerciseSDProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreVariables.*;
import exercise2.*;

import exerciseSD.*;


public class GuessSDApplet extends MeanSDExerciseApplet {
	static final private int DOT_PLOT = 0;
	static final private int HISTO = 1;
	static final private int BOX_PLOT = 2;
	
	static final private double kExactFactor = 0.3;
													//	'exact' sd must be within kExactFactor times the legal range
	static final private double kCloseFactor = 1.0;
													//	'close' sd must be within kCloseFactor times the legal range
	
	static final private String kDisplayString[] = {"crosses", "histo", "box"};
	
	private RandomNormal generator;
	
	private XPanel displayPanel;
	private CardLayout displayPanelLayout;
	
	private AlmostRandomInteger displayGenerator;
	private int currentDisplayType = 0;
	
	private CoreDragView[] theView;
	private HorizAxis[] theAxis;
	
	protected ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, translate("Standard deviation") + " =", 6);
				registerStatusItem("sd", resultPanel);
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
		registerParameter("jointMeanSD", "jointMeanSD");
		registerParameter("mean", "mean");
		registerParameter("sd", "sd");
		registerParameter("count", "int");
		registerParameter("axis", "string");
		registerParameter("histoClasses", "string");
		registerParameter("varName", "string");
	}
	
	protected NumValue getMean() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getMean();
	}
	
	protected NumValue getSD() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getSD();
	}
	
	public String getAxisInfo() {
		return getStringParam("axis");
	}
	
	public int getDecimals() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getDecimals();
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	public String getHistoClassInfo() {
		return getStringParam("histoClasses");
	}
	
	public String getVarName() {
		return getStringParam("varName");
	}
	
	
//-----------------------------------------------------------
	
	private XPanel getOneDisplayPanel(DataSet data, int displayType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theAxis[displayType] = new HorizAxis(this);
		thePanel.add("Bottom", theAxis[displayType]);
		
		switch (displayType) {
			case DOT_PLOT:
				theView[displayType] = new CrossDragView(data, this, theAxis[displayType], "y");
				break;
			case HISTO:
				theView[displayType] = new HistoDragView(data, this, theAxis[displayType], "y");
				break;
			case BOX_PLOT:
				theView[displayType] = new BoxplotDragView(data, this, theAxis[displayType], "y");
				break;
		}
		theView[displayType].lockBackground(Color.white);
		thePanel.add("Center", theView[displayType]);
		
		return thePanel;
	}
	
	public boolean hasOption(String optionName) {		//	 returns true for histo when no options have been set
		if (optionName.equals("histo") && !super.hasOption("crosses") && !super.hasOption("boxPlot"))
			return true;
		else
			return super.hasOption(optionName);
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		displayPanel = new XPanel();
		displayPanelLayout = new CardLayout();
		displayPanel.setLayout(displayPanelLayout);
		
		theView = new CoreDragView[3];
		theAxis = new HorizAxis[3];
		
		if (hasOption("crosses"))
			displayPanel.add(kDisplayString[DOT_PLOT], getOneDisplayPanel(data, DOT_PLOT));
														//	Note that the crosses option only works for exercises, not tests
														//	In tests, it fails because it tries to initialise the classes before
														//	the applet is laid out -- i.e. when the axis length is zero
		if (hasOption("histo"))
			displayPanel.add(kDisplayString[HISTO], getOneDisplayPanel(data, HISTO));
		if (hasOption("boxPlot"))
			displayPanel.add(kDisplayString[BOX_PLOT], getOneDisplayPanel(data, BOX_PLOT));
		
		displayGenerator = new AlmostRandomInteger(0, kDisplayString.length - 1, nextSeed());
		
		return displayPanel;
	}
	
	protected CoreDragView getCurrentView() {
		return theView[currentDisplayType];
	}
	
	private int countUsedClasses(double class0Start, double classWidth) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		double minY = ye.nextDouble();
		double maxY = minY;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
		}
		int class0 =(int)Math.floor((minY - class0Start) / classWidth);
		int class1 =(int)Math.ceil((maxY - class0Start) / classWidth);
		return class1 - class0;
	}
	
	protected void setDisplayForQuestion() {
		do {
			currentDisplayType = displayGenerator.generateOne();
		} while (!hasOption(kDisplayString[currentDisplayType]));
		
		theAxis[currentDisplayType].readNumLabels(getAxisInfo());
		theAxis[currentDisplayType].setAxisName(getVarName());	
		int decimals = getDecimals();
		getCurrentView().setMeanSdDecimals(decimals, decimals);
		if (getCurrentView() instanceof HistoDragView) {
			StringTokenizer st = new StringTokenizer(getHistoClassInfo());
			double class0Start = Double.parseDouble(st.nextToken());
			double classWidth = Double.parseDouble(st.nextToken());
			if (countUsedClasses(class0Start, classWidth) <= 5)
				classWidth *= 0.5;
			((HistoDragView)getCurrentView()).changeClasses(class0Start, classWidth);
		}
		theView[currentDisplayType].setShow4s(false);
		theView[currentDisplayType].resetClasses();
		
		theAxis[currentDisplayType].invalidate();
		theView[currentDisplayType].repaint();
		
		displayPanelLayout.show(displayPanel, kDisplayString[currentDisplayType]);
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NumValue mean = getMean();
		NumValue sd = getSD();
		
		int n = getCount();
		NumSampleVariable coreVar = (NumSampleVariable)data.getVariable("base");
		coreVar.setSampleSize(n);
		coreVar.generateNextSample();
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		yVar.setScale(mean.toDouble(), sd.toDouble(), mean.decimals);
		yVar.clearSortedValues();
	}
	
	
//-----------------------------------------------------------
	
	protected String distnOrDataString() {
		return "data set";
	}
	
	protected String sdString() {
		return "s";
	}
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		String dataType = distnOrDataString();
		double exact, attempt;
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Estimate the standard deviation of this " + dataType
														+ " by eye then type it into the text-edit box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a value for the standard deviation.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Standard deviations can never be negative.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				insertHints(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have guessed the standard deviation as closely as could be expected from the graph.\n");
				messagePanel.insertText("(The exact standard deviation of the " + dataType
												+ " is " + sdString() + " = " + new NumValue(evaluateSD(), getDecimals()) + ".)");
				break;
			case ANS_CLOSE:
				exact = evaluateSD();
				attempt = getAttempt();
				if (attempt < exact)
					messagePanel.insertRedHeading("Close, but too low!\n");
				else
					messagePanel.insertRedHeading("Close, but too high!\n");
				messagePanel.insertText("Use the following as a guide.\n");
				
				insertHints(messagePanel);
				break;
			case ANS_WRONG:
				exact = evaluateSD();
				attempt = getAttempt();
				if (attempt < exact)
					messagePanel.insertRedHeading("Much too low!\n");
				else
					messagePanel.insertRedHeading("Much too high!\n");
				messagePanel.insertText("Use the following as a guide.\n");
				insertHints(messagePanel);
				break;
		}
	}
	
	protected void insertHints(MessagePanel messagePanel) {
		boolean isHisto = (currentDisplayType == HISTO);
//		boolean isCrosses = (currentDisplayType == DOT_PLOT);
		boolean isBoxplot = (currentDisplayType == BOX_PLOT);
//		String valueString = isHisto ? "histogram area" : "values";
		if (isBoxplot) {
			messagePanel.insertText("The middle half of the data usually span between 1.5s and 2s.\n");
			messagePanel.insertText("The range of the data is usually between 5s and 6s.");
		}
		else if (isHisto) {
			messagePanel.insertText("The middle two thirds of the histogram area usually spans about 2s (i.e. x#bar# #plusMinus# s).\n");
			messagePanel.insertText("The middle 95% of the histogram area usually spans about 4s (i.e. x#bar# #plusMinus# 2s).\n");
			messagePanel.insertText("The whole histogram usually spans between 5s and 6s (i.e. x#bar# #plusMinus# 2.5s to x#bar# #plusMinus# 3s).");
		}
		else {		//	crosses
			messagePanel.insertText("An interval that is 2s wide usually covers about the middle two thirds of the crosses (i.e. x#bar# #plusMinus# s).\n");
			messagePanel.insertText("An interval that is 4s wide usually covers about the middle 95% of the crosses (i.e. x#bar# #plusMinus# 2s).\n");
			messagePanel.insertText("The range of the data is usually between 5s and 6s (i.e. x#bar# #plusMinus# 2.5s to x#bar# #plusMinus# 3s).");
		}
	}
	
	protected int getMessageHeight() {
		return 180;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomNormal(10, 0.0, 1.0, 3.0);
			generator.setSeed(nextSeed());
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
//				baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable(getVarName(), baseVar,
																																"base", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected double evaluateSD() {
		return getCurrentView().getSDFromGraph();
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			getCurrentView().setShow4s(false);
			getCurrentView().repaint();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		double correct = evaluateSD();
		double highApprox = Math.max(correct, getCurrentView().getExtremeSD(true));
		double lowApprox = Math.min(correct, getCurrentView().getExtremeSD(false));
		
//		System.out.println("correct = " + correct + ", highApprox = " + highApprox + ", lowApprox = " + lowApprox);
		
		double attempt = getAttempt();
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if (attempt < 0.0)
			return ANS_INVALID;
		else {
			double exactSlop = (highApprox - lowApprox) * kExactFactor;
			if (Math.abs(attempt - correct) <= exactSlop)
				return ANS_CORRECT;
			else {
				double closeSlop = (highApprox - lowApprox) * kCloseFactor;
				if (Math.abs(attempt - correct) <= closeSlop)
					return ANS_CLOSE;
				else
					return ANS_WRONG;
			}
		}
	}
	
	protected void giveFeedback() {
		if (result == ANS_CORRECT || result == ANS_WRONG) {
			getCurrentView().setShow4s(true);
			getCurrentView().repaint();
		}
	}
	
	protected void showCorrectWorking() {
		NumValue correctSD = new NumValue(evaluateSD(), getDecimals());
		resultPanel.showAnswer(correctSD);
		
		getCurrentView().setShow4s(true);
		getCurrentView().repaint();
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
}