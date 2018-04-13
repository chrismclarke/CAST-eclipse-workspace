package exerciseBivarProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import random.*;
import exercise2.*;
import valueList.*;

import exerciseNumGraph.*;
import exerciseBivar.*;


public class DragBuildScatterApplet extends ExerciseApplet {
	private RandomNormal xGenerator, yGenerator;
	
	protected ValueUsage valuesUsed = new ValueUsage();
	
	protected ScrollValueList theList;
	private XLabel yNameLabel;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	protected DragScatterView theView;
	
//================================================
	
	protected void createDisplay() {
		valuesUsed = new ValueUsage();
		registerStatusItem("usage", valuesUsed);
		
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
		
		valuesUsed.setListAndView(data, theList, theView);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("xVarName", "string");
		registerParameter("xMean", "const");
		registerParameter("xSd", "const");
		registerParameter("xDecimals", "int");
		registerParameter("xAxis", "string");
		registerParameter("yVarName", "string");
		registerParameter("yMean", "const");
		registerParameter("ySd", "const");
		registerParameter("yDecimals", "int");
		registerParameter("yAxis", "string");
		registerParameter("count", "int");
		registerParameter("corr", "const");
	}
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private NumValue getXMean() {
		return getNumValueParam("xMean");
	}
	
	private NumValue getXSD() {
		return getNumValueParam("xSd");
	}
	
	private int getXDecimals() {
		return getIntParam("xDecimals");
	}
	
	private String getXAxisInfo() {
		return getStringParam("xAxis");
	}
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private NumValue getYMean() {
		return getNumValueParam("yMean");
	}
	
	private NumValue getYSD() {
		return getNumValueParam("ySd");
	}
	
	private int getYDecimals() {
		return getIntParam("yDecimals");
	}
	
	private String getYAxisInfo() {
		return getStringParam("yAxis");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private NumValue getCorr() {
		return getNumValueParam("corr");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			theList = new UsedValueList(data, this, UsedValueList.HEADING);
			theList.addVariableToList("x", UsedValueList.RAW_VALUE);
			theList.addVariableToList("y", UsedValueList.RAW_VALUE);
		thePanel.add("West", theList);
		
		thePanel.add("Center", scatterPanel(data));
		
		return thePanel;
	}
	
	protected DragScatterView getScatterView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		return new DragScatterView(data, this, xAxis, yAxis, "x", "y");
	}
	
	private XPanel scatterPanel(DataSet data) {
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
			
				theView = getScatterView(data, xAxis, yAxis);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
				registerStatusItem("crossPositions", (DragScatterView)theView);
			displayPanel.add("Center", theView);
			
		thePanel.add("Center", displayPanel);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		valuesUsed.initialise();
		
		boolean[] alreadyUsed = valuesUsed.getUsage();
		
		((UsedValueList)theList).setAlreadyUsed(alreadyUsed);
		theList.invalidate();
		
		xAxis.readNumLabels(getXAxisInfo());
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		yAxis.readNumLabels(getYAxisInfo());
		yNameLabel.setText(getYVarName());
		yAxis.invalidate();
		
		theView.setAlreadyUsed(alreadyUsed);
	}
	
	protected void setDataForQuestion() {
		int n = getCount();
		NumSampleVariable xCoreVar = (NumSampleVariable)data.getVariable("xBase");
		xCoreVar.setSampleSize(n);
		xCoreVar.generateNextSample();
		
		NumSampleVariable yCoreVar = (NumSampleVariable)data.getVariable("yBase");
		yCoreVar.setSampleSize(n);
		yCoreVar.generateNextSample();
		
		ScaledVariable xVar = (ScaledVariable)data.getVariable("x");
		xVar.setScale(getXMean().toDouble(), getXSD().toDouble(), getXDecimals());
		xVar.name = getXVarName();
		
		CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable("y");
		yVar.setMeanSdCorr(getYMean().toDouble(), getYSD().toDouble(), getCorr().toDouble(),
																																				getYDecimals());
		yVar.name = getYVarName();
		
		data.variableChanged("x");
		data.variableChanged("y");
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
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Click on each value in the list to display it as a cross; then drag the cross to move it to its correct position on a scatterplot.");
				messagePanel.insertText("\n(The arrow keys can be used to fine-tune the position of a selected cross after dragging to its rough position.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Incomplete!\n");
				messagePanel.insertText("You have not added crosses for all values in the list.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This scatterplot describes the data. (Click any cross to see its value on the list.)");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your crosses are all in the correct places (or very close).");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("The highlighted crosses are close to their correct positions but need to be moved a little.");
				messagePanel.insertText("\n(The arrow keys can be used to fine-tune the position of a selected cross.)");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The highlighted crosses are far from their correct places.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	protected boolean completedCrosses() {
		boolean[] alreadyUsed = valuesUsed.getUsage();
		for (int i=0 ; i<alreadyUsed.length ; i++)
			if (!alreadyUsed[i])
				return false;
		return true;
	}
	
	protected int assessAnswer() {
		if (!completedCrosses())
			return ANS_INCOMPLETE;
		else {
			boolean wrongCross[] = theView.getErrors(false);
			
			if (wrongCross != null)
				return ANS_WRONG;
			else {
				wrongCross = theView.getErrors(true);
				return (wrongCross == null) ? ANS_CORRECT : ANS_CLOSE;
			}
		}
	}
	
	protected void giveFeedback() {
		if (result == ANS_CORRECT)
			data.clearSelection();
		else if (result == ANS_WRONG) {
			boolean wrongCross[] = theView.getErrors(false);
			data.setSelection(wrongCross);
		}
		else if (result == ANS_CLOSE) {
			boolean wrongCross[] = theView.getErrors(true);
			data.setSelection(wrongCross);
		}
	}
	
	protected void showCorrectWorking() {
		data.clearSelection();
		
		valuesUsed.setAllUsed();
		theView.showCorrectCrosses();
		
		data.variableChanged("xBase");
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
}