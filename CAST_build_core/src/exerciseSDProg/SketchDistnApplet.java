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


public class SketchDistnApplet extends MeanSDExerciseApplet {
//	static final private String VAR_NAME_PARAM = "varName";
	
	static final public int ANS_WRONG_AXIS = ANS_WRONG;
	static final public int ANS_WRONG_MEAN = ANS_WRONG + 1;
	static final public int ANS_WRONG_SD = ANS_WRONG + 2;
	
	static final private double kMeanSlopPropn = 0.35;
	static final private double kExactSdSlopPropn = 0.5;
													//	'exact' sd must be within kExactFactor times the legal range
	static final private double kCloseSdSlopPropn = 1.2;
													//	'close' sd must be within kCloseFactor times the legal range
	
	private RandomNormal generator;
	
	private MultiHorizAxis theAxis;
	private CoreDragView theView;
	
	private XChoice axisChoice;
	private int currentAxisIndex;
	
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
		registerParameter("correctAxis", "int");
		registerParameter("jointMeanSD", "jointMeanSD");
		registerParameter("mean", "mean");
		registerParameter("sd", "sd");
		registerParameter("count", "int");
		registerParameter("allAxes", "array");						//	not just indexed one
		registerParameter("allHistoClasses", "array");		//	not just indexed one
		registerParameter("allAxisNames", "array");				//	not just indexed one
		registerParameter("varName", "string");				//	not just indexed one
	}
	
	protected NumValue getMean() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getMean();
	}
	
	protected NumValue getSD() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getSD();
	}
	
	private StringArray getAllAxesInfo() {
		return getArrayParam("allAxes");
	}
	
	private StringArray getAllAxisNames() {
		return getArrayParam("allAxisNames");
	}
	
	public String getAxisInfo() {			//	needed by MeanSDExerciseApplet
						//		correct axis info
		return getAllAxesInfo().getValue(getIndex());
	}
	
	protected int getIndex() {
		return getIntParam("correctAxis");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	public int getDecimals() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getDecimals();
	}
	
	public String getCurrentHistoClassInfo() {
		return getArrayParam("allHistoClasses").getValue(currentAxisIndex);
	}
	
	public String getVarName() {
		return getStringParam("varName");
	}
	
	
//-----------------------------------------------------------
	
	protected CoreDragView getDataView(DataSet data, MultiHorizAxis axis) {
		if (hasOption("boxplot"))
			return new BoxplotDragView(data, this, theAxis, null);
		else if (hasOption("crosses"))
			return new CrossDragView(data, this, theAxis, "base", "y");
		else				//	histo
			return new HistoDragView(data, this, theAxis, null);
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new AxisLayout());
			
				theAxis = new MultiHorizAxis(this, 1);		//	no of alternates will be changed by setDisplayForQuestion()
				theAxis.setChangeMinMax(true);
			graphPanel.add("Bottom", theAxis);
			
				theView = getDataView(data, theAxis);
				theView.lockBackground(Color.white);
			graphPanel.add("Center", theView);
		
		thePanel.add("Center", graphPanel);
		
		if (hasOption("multipleAxes")) {
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
				axisChoice = new XChoice(this);
				registerStatusItem("axisIndex", axisChoice);
			choicePanel.add(axisChoice);
			
			registerStatusItem("dragData", theView);		//	must be restored after axisChoice since changing axis sets default params
			
			thePanel.add("South", choicePanel);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		StringArray allAxes = getAllAxesInfo();
		int nAxes = allAxes.getNoOfStrings();
		theAxis.setNoOfAlternates(nAxes);
		theAxis.readNumLabels(allAxes.getValue(0));
		for (int i=1 ; i<nAxes ; i++)
			theAxis.readExtraNumLabels(allAxes.getValue(i));
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		if (axisChoice == null) {
			currentAxisIndex = getIndex();
			theAxis.setStartAlternate(currentAxisIndex);
		}
		else {
			currentAxisIndex = 0;
			axisChoice.clearItems();
			StringArray axisNames = getAllAxisNames();
			for (int i=0 ; i<axisNames.getNoOfStrings() ; i++)
				axisChoice.addItem(axisNames.getValue(i));
			axisChoice.invalidate();
		}
		
		int decimals = getDecimals() + 1;
		theView.setMeanSdDecimals(decimals, decimals);
		if (theView instanceof HistoDragView) {
			StringTokenizer st = new StringTokenizer(getCurrentHistoClassInfo());
			double class0Start = Double.parseDouble(st.nextToken());
			double classWidth = Double.parseDouble(st.nextToken());
			((HistoDragView)theView).changeClasses(class0Start, classWidth);
		}
		else if (theView instanceof CrossDragView)
			((CrossDragView)theView).setNValues(getCount());
		
		theView.resetClasses();
		
		theView.setShow4s(false);
		theView.repaint();
	}
	
	protected void setDataForQuestion() {
	}
	
	
//-----------------------------------------------------------
	
	protected String getDisplayString() {
		return hasOption("crosses") ? "stacked dot plot" : hasOption("boxplot") ? "box plot" : "histogram";
	}
	
	protected String getDragWording() {
		if (hasOption("boxplot"))
			return "the median, quartiles and extremes of the box plot to display a distribution whose mean and standard deviation approximately match the values in the question.";
		else if (hasOption("crosses"))
			return "the crosses in the dot plot to display a distribution whose mean and standard deviation approximately match the values in the question.";
		else		//	histo
			return "the tops of the histogram bars to display a distribution whose mean and standard deviation approximately match the values in the question.";
	}
	
	protected String getToldWording() {
		return "(But note that there are other " + getDisplayString() + "s with the same mean and sd.)";
	}
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		String displayString = getDisplayString();
		switch (result) {
			case ANS_UNCHECKED:
				if (axisChoice == null)
					messagePanel.insertText("Drag ");
				else
					messagePanel.insertText("Firstly select an appropriate axis to display the distribution.\nThen drag ");
				messagePanel.insertText(getDragWording());
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The " + displayString + " is one that matches the mean and standard deviation in the question exactly.\n");
				messagePanel.insertText(getToldWording());
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("This " + displayString + " has mean and standard deviation that match the values in the question as closely as could be expected by eye.");
				break;
			case ANS_WRONG_AXIS:
				messagePanel.insertRedHeading("Wrong!\n");
				double axisMin = theAxis.minOnAxis;
				double axisMax = theAxis.maxOnAxis;
				double mean = getMean().toDouble();
				double sd = getSD().toDouble();
				double dataLow = mean - 2 * sd;
				double dataHigh = mean + 2 * sd;
				messagePanel.insertText("The mean and/or standard deviation of the " + displayString + " is wrong.\n");
				if (dataLow < axisMin || dataHigh > axisMax)
					messagePanel.insertText("The axis that you have chosen does not include values that can display ");
				else
					messagePanel.insertText("Choose an axis that can better display ");
				messagePanel.insertText("a distribution with  mean " + getMean() + " and standard deviation " + getSD() + ".");
				break;
			case ANS_WRONG_MEAN:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("The mean of your " + displayString + " is not close enough to " + getMean() + ".");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("The mean of your " + displayString + " is near " + getMean());
				messagePanel.insertText(".\nHowever, its standard deviation is ");
				double attemptSD = theView.getSDFromGraph();
				double correctSD = getSD().toDouble();
				if (attemptSD < correctSD)
					messagePanel.insertText("a bit on the low side.");
				else
					messagePanel.insertText("a bit on the high side.");
				break;
			case ANS_WRONG_SD:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("The mean of your " + displayString + " is near " + getMean());
				messagePanel.insertText(", but your should be able to make its standard deviation closer to " + getSD() + ".");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		if (hasOption("crosses")) {
				generator = new RandomNormal(10, 0.0, 1.0, 3.0);
				generator.setSeed(nextSeed());
				NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
				baseVar.generateNextSample();
			data.addVariable("base", baseVar);
			
				ScaledVariable yVar = new ScaledVariable(getVarName(), baseVar,
																																	"base", 0.0, 1.0, 9);
			data.addVariable("y", yVar);
		}
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		double correctMean = getMean().toDouble();
		double correctSD = getSD().toDouble();
		
		double attemptMean = theView.getMeanFromGraph();
		double attemptSD = theView.getSDFromGraph();
		
		double meanSlop = correctSD * kMeanSlopPropn;
		
		boolean meanOK = Math.abs(attemptMean - correctMean) <= meanSlop;
		
		double highApprox = Math.max(attemptSD, theView.getExtremeSD(true));
		double lowApprox = Math.min(attemptSD, theView.getExtremeSD(false));
		double exactSDSlop = (highApprox - lowApprox) * kExactSdSlopPropn;
		double closeSDSlop = (highApprox - lowApprox) * kCloseSdSlopPropn;
		boolean sdOK = Math.abs(attemptSD - correctSD) <= exactSDSlop;
		boolean sdClose = Math.abs(attemptSD - correctSD) <= closeSDSlop;
		
		if (meanOK && sdOK)
			return ANS_CORRECT;
		else if (meanOK && sdClose)
			return ANS_CLOSE;
		else {
			double axisMin = theAxis.minOnAxis;
			double axisMax = theAxis.maxOnAxis;
			double mean = getMean().toDouble();
			double sd = getSD().toDouble();
			double dataLow = mean - 2 * sd;
			double dataHigh = mean + 2 * sd;
			
			if (dataLow < axisMin || dataHigh > axisMax || (dataHigh - dataLow) < 0.3 * (axisMax - axisMin))
				return ANS_WRONG_AXIS;
			else 
				return (!meanOK) ? ANS_WRONG_MEAN : ANS_WRONG_SD;
		}
	}
	
	protected void giveFeedback() {
		if (result == ANS_CORRECT || result == ANS_CLOSE || result == ANS_WRONG_MEAN || result == ANS_WRONG_SD) {
			theView.setShow4s(true);
			theView.repaint();
		 } 
	}
	
	protected void showCorrectWorking() {
		int correctIndex = getIndex();
		if (axisChoice != null) {
			axisChoice.select(correctIndex);
			changeAxis(correctIndex);
		}
		theView.initialise();
		theView.setMeanSD(getMean().toDouble(), getSD().toDouble());
		theView.setShow4s(true);
		theView.repaint();
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
	
	
//-----------------------------------------------------------
	
	private void changeAxis(int axisIndex) {
		currentAxisIndex = axisIndex;
		
		theAxis.setAlternateLabels(axisIndex);
		theAxis.repaint();
		
		if (theView instanceof HistoDragView) {
			StringTokenizer st = new StringTokenizer(getCurrentHistoClassInfo());
			double class0Start = Double.parseDouble(st.nextToken());
			double classWidth = Double.parseDouble(st.nextToken());
			((HistoDragView)theView).changeClasses(class0Start, classWidth);
		}
		theView.resetClasses();
	}
	
	private boolean localAction(Object target) {
		if (target == axisChoice) {
			int newChoice = axisChoice.getSelectedIndex();
			if (newChoice != currentAxisIndex) {
				changeAxis(newChoice);
				theView.repaint();
				noteChangedWorking();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}