package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import coreVariables.*;
import random.*;
import exercise2.*;
import formula.*;

import exerciseBivar.*;


public class MatchXYTransformApplet extends CoreMatchApplet {
	static final private int SQR = 0;
	static final private int ROOT = 1;
	static final private int LOG = 2;
	static final private int IDENTITY = 3;
	
	
	static final private String[] kDefaultXKeys = {"xSqr", "xRoot", "xLog", "x"};
	static final private String[] kDefaultYKeys = {"ySqr", "yRoot", "yLog", "y"};
	static final private int kNTransformDistns = kDefaultXKeys.length;
	
	private RandomRectangular xGenerator;
	private RandomNormal zGenerator;
	
	private MultipleScatterView scatterPlots, namesDisplay;
	
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.3, 10));
			
				XPanel leftPanel = new XPanel();
				leftPanel.setLayout(new ProportionLayout(0.6, 0, ProportionLayout.VERTICAL));
			
					questionPanel = new QuestionPanel(this);
				leftPanel.add(ProportionLayout.TOP, questionPanel);
				leftPanel.add(ProportionLayout.BOTTOM, new XPanel());
				
			mainPanel.add(ProportionLayout.LEFT, leftPanel);
			
			mainPanel.add(ProportionLayout.RIGHT, getWorkingPanels(data));
			
		add("Center", mainPanel);
				
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
		registerParameter("count", "int");
		registerParameter("baseXPower", "const");
		registerParameter("baseYPower", "const");
		registerParameter("ordersOfMagnitude", "const");
		registerParameter("corr", "const");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	protected double getBaseXPower() {
		return getDoubleParam("baseXPower");
	}
	
	protected double getBaseYPower() {
		return getDoubleParam("baseYPower");
	}
	
	protected double getOrdersOfMagnitude() {
		return getDoubleParam("ordersOfMagnitude");
	}
	
	protected double getCorr() {
		return getDoubleParam("corr");
	}
	
	
//-----------------------------------------------------------
	
	protected int noOfItems() {
		return kNTransformDistns;
	}
	
	protected boolean retainFirstItems() {
		return true;
	}
	
	protected int getDragMatchHeight() {
		return scatterPlots.getSize().height;
	}
	
	protected void setWorkingPanelLayout(XPanel thePanel) {
		thePanel.setLayout(new BorderLayout(40, 0));
	}
	
	protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder) {
			XPanel namesPanel = new XPanel();
			namesPanel.setLayout(new BorderLayout(0, 0));
			
				namesDisplay = new MultipleScatterView(data, this, kDefaultXKeys, kDefaultYKeys,
																			leftOrder, MultipleScatterView.VARIABLE_NAMES);
				namesDisplay.setFixedTopDistn(true);
				registerStatusItem("namesPerm", namesDisplay);
				
			namesPanel.add("Center", namesDisplay);
				
		thePanel.add("West", namesPanel);
		
		return namesPanel;
	}
	
	protected XPanel addRightItems(XPanel thePanel, int[] rightOrder) {
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new BorderLayout(0, 0));
			
				scatterPlots = new MultipleScatterView(data, this, kDefaultXKeys, kDefaultYKeys,
																	rightOrder, MultipleScatterView.SCATTER_PLOTS);
				scatterPlots.lockBackground(Color.white);
				scatterPlots.setFixedTopDistn(true);
				registerStatusItem("plotsPerm", scatterPlots);
				
			scatterPanel.add("Center", scatterPlots);
				
		thePanel.add("Center", scatterPanel);
		
		return scatterPanel;
	}
	
	
//-----------------------------------------------------------
	
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
	
		double xPower = getBaseXPower();
		int xCorrect = (xPower == 1) ? IDENTITY : (xPower < 1) ? SQR : ROOT;
		double yPower = getBaseYPower();
		int yCorrect = (yPower == 1) ? IDENTITY : (xPower < 1) ? SQR : ROOT;
		
		int xTransform[] = {SQR, ROOT, LOG, IDENTITY};
		int yTransform[] = {SQR, ROOT, LOG, IDENTITY};
		Random rand = new Random(nextSeed());
		
		if (xCorrect == IDENTITY && yCorrect == IDENTITY) {
			permute(xTransform, 3, rand);
			permute(yTransform, 3, rand);
		}
		else {
			if (xCorrect == IDENTITY) {
				swap(yTransform, yCorrect, 2);		//	yTransform[2] becomes symmetric
				xTransform[2] = IDENTITY;					//	xTransform[2] is symmetric, others are 2 from wrongs
			}
			else if (yCorrect == IDENTITY) {
				swap(xTransform, xCorrect, 2);		//	xTransform[2] becomes symmetric
				yTransform[2] = IDENTITY;					//	yTransform[2] is symmetric, others are 2 from wrongs
			}
			else {
				swap(xTransform, xCorrect, 2);		//	xTransform[2] becomes symmetric
				swap(yTransform, yCorrect, 2);		//	yTransform[2] becomes symmetric
			}
			if (rand.nextDouble() < 0.5)
				swap(xTransform, 0, 1);
			if (rand.nextDouble() < 0.5)
				swap(yTransform, 0, 1);
		}
			
		String xKeys[] = new String[4];
		String yKeys[] = new String[4];
		for (int i=0 ; i<4 ; i++) {
			xKeys[i] = kDefaultXKeys[xTransform[i]];
			yKeys[i] = kDefaultYKeys[yTransform[i]];
		}
		
		scatterPlots.setXYKeys(xKeys, yKeys);
		scatterPlots.repaint();
		
		namesDisplay.setXYKeys(xKeys, yKeys);
		namesDisplay.invalidate();
		namesDisplay.repaint();
	}
	
	private void swap(int[] transform, int source, int dest) {
		int temp = transform[source];
		transform[source] = transform[dest];
		transform[dest] = temp;
	}
	
	protected void setDataForQuestion() {
		NumSampleVariable xBaseVar = (NumSampleVariable)data.getVariable("xBase");
		xBaseVar.setSampleSize(getCount());
		RandomRectangular xGenerator = (RandomRectangular)xBaseVar.getGenerator();
		double lowValue = Math.pow(0.1, getOrdersOfMagnitude());
		xGenerator.setMinMax(lowValue, 1.0);
		xBaseVar.generateNextSample();
		
		NumSampleVariable zVar = (NumSampleVariable)data.getVariable("z");
		zVar.setSampleSize(getCount());
		zVar.generateNextSample();
		((NumValue)zVar.valueAt(0)).setValue(lowValue);
		
		CorrelatedVariable yBaseVar = (CorrelatedVariable)data.getVariable("yBase");
		yBaseVar.setMinMaxCorr(lowValue, 1.0, getCorr(), 9);
		
		PowerVariable xVar = (PowerVariable)data.getVariable("x");
		xVar.setPower(getBaseXPower(), 9);
		
		PowerVariable yVar = (PowerVariable)data.getVariable("y");
		yVar.setPower(getBaseYPower(), 9);
		
		super.setDataForQuestion();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Reorder the scatterplots (or the transformation names) by dragging so that each scatterplot describes the transformed variables on its left.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The correct matching of scatterplots and transformations is shown.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched up the scatterplots with the named transformations of the top data set.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The red arrows indicate errors.");
				messagePanel.insertText("\nSquare root and log transformations of X and Y push the middle crosses towards the right and top. Squaring X or Y does the opposite.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			xGenerator = new RandomRectangular(10, 0.0, 1.0);
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xBaseVar = new NumSampleVariable("X", xGenerator, 9);
		data.addVariable("xBase", xBaseVar);
		
			zGenerator = new RandomNormal(10, 0.0, 1.0, 2.5);		//	+/- 2.5 SD
			zGenerator.setSeed(nextSeed());
			NumSampleVariable zBaseVar = new NumSampleVariable("Z", zGenerator, 9);
		data.addVariable("z", zBaseVar);
			
			CorrelatedVariable yBaseVar = new CorrelatedVariable("Y", data, "xBase", "z", 9);
		data.addVariable("yBase", yBaseVar);
		
			PowerVariable xVar = new PowerVariable("X", xBaseVar, 2.0, 9);
		data.addVariable("x", xVar);
		
			PowerVariable yVar = new PowerVariable("Y", yBaseVar, 2.0, 9);
		data.addVariable("y", yVar);
		
		//----------
		
			LogVariable xLogVar = new LogVariable("log(X)", data, "x", 9);
		data.addVariable("xLog", xLogVar);
		
			LogVariable yLogVar = new LogVariable("log(Y)", data, "y", 9);
		data.addVariable("yLog", yLogVar);
		
			PowerVariable xRootVar = new PowerVariable(MText.expandText("#sqrt#X"), xVar, 0.5, 9);
		data.addVariable("xRoot", xRootVar);
		
			PowerVariable yRootVar = new PowerVariable(MText.expandText("#sqrt#Y"), yVar, 0.5, 9);
		data.addVariable("yRoot", yRootVar);
		
			PowerVariable xSqrVar = new PowerVariable(MText.expandText("X#sup2#"), xVar, 2.0, 9);
		data.addVariable("xSqr", xSqrVar);
		
			PowerVariable ySqrVar = new PowerVariable(MText.expandText("Y#sup2#"), yVar, 2.0, 9);
		data.addVariable("ySqr", ySqrVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		scatterPlots.repaint();
	}
	
}