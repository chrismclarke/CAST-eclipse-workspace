package exerciseNumGraphProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import random.*;
import exercise2.*;
import valueList.*;
import distn.*;

import exerciseNumGraph.*;


public class BuildBoxPlotApplet extends ExerciseApplet {
	static final private Color kBoxFillColor = new Color(0xDDDDDD);
	
	static final private double kLowTailProb = 0.005;
	static final private double kMinWidthPropn = 0.6;
	
	private RandomGamma generator;
	
	protected ScrollValueList theList;
	protected HorizAxis valAxis;
	private DragBoxPlotView theView;
	
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
//		registerParameter("index", "int");					//	always registered
		registerParameter("shape", "const");
		registerParameter("count", "int");
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("decimals", "int");
	}
	
	protected double getShapeValue() {
		return getDoubleParam("shape");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	protected String getAxisInfo() {
		return getStringParam("axis");
	}
	
	protected int getDecimals() {
		return getIntParam("decimals");
	}
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
			theList.addVariableToList("y", ScrollValueList.RANK);
			theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
			theList.sortByVariable("y", ScrollValueList.SMALL_FIRST);
			theList.setCanSelectRows(false);
		thePanel.add("West", theList);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new AxisLayout());
			
				valAxis = new HorizAxis(this);
			rightPanel.add("Bottom", valAxis);
			
				theView = new DragBoxPlotView(data, this, valAxis, "y");
				theView.setShowOutliers(hasOption("showOutliers"));
				theView.setFillColor(kBoxFillColor);
				theView.lockBackground(Color.white);
				registerStatusItem("boxPos", theView);
			rightPanel.add("Center", theView);
		thePanel.add("Center", rightPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		theList.resetVariables();
		theList.invalidate();
		
		valAxis.readNumLabels(getAxisInfo());
		valAxis.setAxisName(getVarName());
		valAxis.invalidate();
		
		if (theView != null) {
			theView.setDefaultBox();
			theView.reset();
		}
	}
	
	
	protected void setDataForQuestion() {
		NumSampleVariable coreVar = (NumSampleVariable)data.getVariable("base");
		RandomGamma gammaGenerator = (RandomGamma)coreVar.getGenerator();
		
		double shape = getShapeValue();
		double absShape = Math.abs(shape);
		gammaGenerator.setShape(absShape);
		
		double lowQuantile = GammaDistnVariable.gammaQuant(kLowTailProb, absShape);
		double highQuantile = GammaDistnVariable.gammaQuant(1.0 - kLowTailProb, absShape);
		gammaGenerator.setTruncation(lowQuantile, highQuantile);
		
		int n = getCount();
		coreVar.setSampleSize(n);
		
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		
		Random uniformGenerator = new Random(nextSeed());
		double minWidth = kMinWidthPropn * (axisMax - axisMin);
		double dataMin = axisMin + uniformGenerator.nextDouble() * (axisMax - axisMin - minWidth);
		double dataMax = dataMin + minWidth + uniformGenerator.nextDouble() * (axisMax - dataMin - minWidth);
		
		if (shape < 0.0) {
			double temp = dataMax;
			dataMax = dataMin;
			dataMin = temp;
		}
		
		coreVar.generateNextSample();
		
		NumValue sortedCore[] = coreVar.getSortedData();
		double coreMin = sortedCore[0].toDouble();
		double coreMax = sortedCore[sortedCore.length - 1].toDouble();
		
		double factor = (dataMax - dataMin) / (coreMax - coreMin);
		double shift = dataMin - coreMin * factor;
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.setScale(shift, factor, getDecimals());
		yVar.clearSortedValues();
		
		data.variableChanged("base");
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the vertical lines to draw a box plot of the data.");
				if (hasOption("showOutliers"))
					messagePanel.insertText("\nMake sure that the \"whiskers\" extend no more than 1.5IQR beyond the quartiles.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This box plot describes the data.\n(Other definitions of the quartiles would result in slightly different box plots.)");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("This is a valid box plot for the data.\n(But note that other definitions of the quartiles would result in slightly different box plots.)");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("The red lines show incorrectly positioned parts of the box plot.\n");
				if (hasOption("showOutliers")) {
					if (theView.whiskersTooLong()) 
						messagePanel.insertText("The \"whiskers\" should be no longer than 1.5 times the interquartile range (length of central box).");
					messagePanel.insertText("Hint: Find the median and quartiles and draw the central box first, then draw the \"whiskers\".");
				}
				else
					messagePanel.insertText("For fine adjustment of any part of the box plot, click to select it, then use the arrow keys to move it left or right.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomGamma(10, 1.0, 1.0, 3.0);
			generator.setSeed(nextSeed());
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable("", baseVar, "base", 0.0, 1.0, 9);
			yVar.setRoundValues(true);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		boolean correct[] = theView.correctQuartiles();
		boolean allOK = true;
		for (int i=0 ; i<correct.length ; i++)
			allOK = allOK && correct[i];
		
		return allOK ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_WRONG) {
			boolean correct[] = theView.correctQuartiles();
			theView.showWrongQuartiles(correct);
			theView.repaint();
		}
	}
	
	protected void showCorrectWorking() {
		theView.setCorrectBox();
		theView.repaint();
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : 0;
	}
	
	public void showHints(boolean hasHints) {
		super.showHints(hasHints);
		
	}
}