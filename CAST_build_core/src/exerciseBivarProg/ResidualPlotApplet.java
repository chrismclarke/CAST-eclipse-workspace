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


public class ResidualPlotApplet extends ExerciseApplet {
	static final private String kResidKey[] = {"residMean", "residLin", "residQuad", "residNegMean", "residNegLin", "residNegQuad"};
	
	private Random random01;
	private RandomRectangular xGenerator;
	private RandomNormal errorGenerator;
	
	private XLabel yNameLabel;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	protected ScatterAndLineView theView;
	
	private ResidPlotChoicePanel residPlotChoice;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 8));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.33, 0, ProportionLayout.VERTICAL,
																															ProportionLayout.TOTAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new ProportionLayout(0.5, 25, ProportionLayout.HORIZONTAL,
																																ProportionLayout.TOTAL));
						
//					XPanel innerPanel = new XPanel();
//					innerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
//					
//						questionPanel = new QuestionPanel(this);
//					innerPanel.add(questionPanel);
				
					XPanel innerPanel = new InsetPanel(0, 0, 0, 40);
					innerPanel.setLayout(new BorderLayout(0, 0));
					
						questionPanel = new QuestionPanel(this);
					innerPanel.add("Center", questionPanel);
					
				topPanel.add(ProportionLayout.LEFT, innerPanel);
				
				topPanel.add(ProportionLayout.RIGHT, getScatterPanel(data));
			
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
			mainPanel.add(ProportionLayout.BOTTOM, getWorkingPanels(data));
			
		add("Center", mainPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(ALLOW_HINTS));
			
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
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "string");
		registerParameter("residAxis", "string");
		registerParameter("count", "int");
	}
	
	protected String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private String getXAxisInfo() {
		return getStringParam("xAxis");
	}
	
	protected String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private String getYAxisInfo() {
		return getStringParam("yAxis");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private String getResidAxisInfo() {
		return getStringParam("residAxis");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		residPlotChoice = new ResidPlotChoicePanel(this, data, "x",
																kResidKey, getResidAxisInfo(), getXAxisInfo());
		registerStatusItem("residPlotChoice", residPlotChoice);
		
		return residPlotChoice;
	}
	
	protected XPanel getScatterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			yNameLabel = new XLabel("", XLabel.LEFT, this);
			yNameLabel.setFont(getSmallFont());
		thePanel.add("North", yNameLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				xAxis = new HorizAxis(this);
				xAxis.setFont(getSmallFont());
			displayPanel.add("Bottom", xAxis);
			
				yAxis = new VertAxis(this);
				yAxis.setFont(getSmallFont());
			displayPanel.add("Left", yAxis);
			
				theView = new ScatterAndLineView(data, this, xAxis, yAxis, "x", "y", "lsLin");
				theView.lockBackground(Color.white);
			displayPanel.add("Center", theView);
			
		thePanel.add("Center", displayPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		xAxis.readNumLabels(getXAxisInfo());
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		yAxis.readNumLabels(getYAxisInfo());
		yNameLabel.setText(getYVarName());
		yAxis.invalidate();
		
		residPlotChoice.changeOptions(getResidAxisInfo(), getXAxisInfo());
		residPlotChoice.clearRadioButtons();
		
		data.variableChanged("x");
	}
	
	protected void setDataForQuestion() {
		NumSampleVariable xVar = (NumSampleVariable)data.getVariable("x");
		RandomRectangular xGenerator = (RandomRectangular)xVar.getGenerator();
		StringTokenizer st = new StringTokenizer(getXAxisInfo());
		double xMin = Double.parseDouble(st.nextToken());
		double xMax = Double.parseDouble(st.nextToken());
		xGenerator.setMinMax(xMin, xMax);
		xVar.name = getXVarName();
		
		int n = getCount();
		BiSampleVariable random = (BiSampleVariable)data.getVariable("random");
		random.setSampleSize(n);
		random.generateNextSample();
		
		NumVariable xDummyVar = (NumVariable)data.getVariable("xDummy");
		xDummyVar.clearData();
		xDummyVar.addValue(new NumValue(xMin));
		xDummyVar.addValue(new NumValue((xMin + xMax) / 2));
		xDummyVar.addValue(new NumValue(xMax));
		
		st = new StringTokenizer(getYAxisInfo());
		double yMin = Double.parseDouble(st.nextToken());
		double yMax = Double.parseDouble(st.nextToken());
		
		if (random01 == null)
			random01 = new Random(nextSeed());
		double errorSD = (yMax - yMin) / 8;
		errorSD = 0.2 * errorSD + 0.8 * errorSD * random01.nextDouble();
		yMin += 2 * errorSD;
		yMax -= 2 * errorSD;
		
		double y[] = {yMin, 0.0, yMax};
		if (random01.nextDouble() < 0.5) {
			y[1] = 0.8 * yMin + 0.2 * yMax;
			if (random01.nextDouble() < 0.5) {
				double temp = y[0];
				y[0] = y[1];
				y[1] = temp;
			}
		}
		else {
			y[1] = 0.2 * yMin + 0.8 * yMax;
			if (random01.nextDouble() < 0.5) {
				double temp = y[2];
				y[2] = y[1];
				y[1] = temp;
			}
		}
		
		NumVariable yDummyVar = (NumVariable)data.getVariable("yDummy");
		yDummyVar.clearData();
		for (int i=0 ; i<3 ; i++)
			yDummyVar.addValue(new NumValue(y[i]));
		
		QuadraticModel modelVar = (QuadraticModel)data.getVariable("model");
		modelVar.setLSParams("yDummy", 9, 9, 9, 9);
		modelVar.setSD(errorSD);
		
		MeanOnlyModel lsMean = (MeanOnlyModel)data.getVariable("lsMean");
		lsMean.setLSParams("y", 9, 0);
		
		LinearModel lsLin = (LinearModel)data.getVariable("lsLin");
		lsLin.setLSParams("y", 9, 9, 0);
		
		QuadraticModel lsQuad = (QuadraticModel)data.getVariable("lsQuad");
		lsQuad.setLSParams("y", 9, 9, 9, 0);
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			xGenerator = new RandomRectangular(10, 0.0, 1.0);
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xVar = new NumSampleVariable("", xGenerator, 9);
			xVar.generateNextSample();
		data.addVariable("x", xVar);
		
			errorGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			errorGenerator.setSeed(nextSeed());
			NumSampleVariable errorVar = new NumSampleVariable(translate("Error"), errorGenerator, 9);
			errorVar.generateNextSample();
		data.addVariable("error", errorVar);
		
			BiSampleVariable random = new BiSampleVariable(data, "x", "error");
		data.addVariable("random", random);
		
			NumVariable xDummyVar = new NumVariable("xDummy");
		data.addVariable("xDummy", xDummyVar);
		
			NumVariable yDummyVar = new NumVariable("yDummy");
		data.addVariable("yDummy", yDummyVar);
		
			QuadraticModel modelVar = new QuadraticModel("Model", data, "xDummy");
		data.addVariable("model", modelVar);
		
			ResponseVariable yVar = new ResponseVariable("", data, "x", "error", "model", 9);
		data.addVariable("y", yVar);
		
			MeanOnlyModel lsMean = new MeanOnlyModel("Mean only", data);
		data.addVariable("lsMean", lsMean);
		
			LinearModel lsLin = new LinearModel(translate("Linear model"), data, "x");
		data.addVariable("lsLin", lsLin);
		
			QuadraticModel lsQuad = new QuadraticModel("Quad model", data, "x");
		data.addVariable("lsQuad", lsQuad);
		
			ResidValueVariable residMean = new ResidValueVariable(translate("Residual"), data, new String[0], "y",
																			"lsMean", 9);
		data.addVariable(kResidKey[0], residMean);
		
			ResidValueVariable residLin = new ResidValueVariable(translate("Residual"), data, "x", "y",
																			"lsLin", 9);
		data.addVariable(kResidKey[1], residLin);
		
			ResidValueVariable residQuad = new ResidValueVariable(translate("Residual"), data, "x", "y",
																			"lsQuad", 9);
		data.addVariable(kResidKey[2], residQuad);
		
			ScaledVariable residNegMean = new ScaledVariable(translate("Residual"), residMean, "residMean", 0.0,
															-1.0, 9);
		data.addVariable(kResidKey[3], residNegMean);
		
			ScaledVariable residNegLin = new ScaledVariable(translate("Residual"), residLin, "residLin", 0.0,
															-1.0, 9);
		data.addVariable(kResidKey[4], residNegLin);
		
			ScaledVariable residNegQuad = new ScaledVariable(translate("Residual"), residQuad, "residQuad", 0.0,
															-1.0, 9);
		data.addVariable(kResidKey[5], residNegQuad);
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		if (random01 != null)
			random01.setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("One of the four red scatterplots is the correct residual plot after a linear model is fitted to the data. Use the radio buttons to select it.");
				if (hasHints)
					messagePanel.insertRedText("\nHint: Click any cross to highlight it (and its residual) on all plots.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error\n");
				messagePanel.insertText("You must select one of the options given for the residual plot. ");
				messagePanel.insertBoldText("(Click a radio button.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("There should be no linear trend in the residual plot but the curvature evident in the data should also be present.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have picked the correct residual plot.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText(residPlotChoice.getSelectedOptionMessage());
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
	
//-----------------------------------------------------------
	
/*
	public boolean noteChangedWorking() {
		if (super.noteChangedWorking()) {
			theView.setShowResult(false);
			theView.repaint();
			
			return true;
		}
		else
			return false;
	}
*/
	
	protected int assessAnswer() {
		return residPlotChoice.checkCorrect();
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		residPlotChoice.showAnswer();
	}
	
	protected double getMark() {
		return (residPlotChoice.checkCorrect() == ANS_CORRECT) ? 1 : 0;
	}
	
	
	public void showHints(boolean hasHints) {
		super.showHints(hasHints);
		data.clearSelection();
		residPlotChoice.setShowHints(hasHints);
		theView.setShowHints(hasHints);
		message.changeContent();
	}
	
}