package exerciseNumGraphProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import random.*;
import exercise2.*;

import exerciseNumGraph.*;


public class MatchDistnApplet extends CoreMatchApplet {
	static final private int kNDisplayedDistns = 4;
	
	static final private double kMinWidthPropn = 0.8;
	
	private DistnGenerator generator[];
	
	private HorizAxis leftAxis, rightAxis;
	private MultipleDistnView leftPlots, rightPlots;
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel localWorkingPanel = getWorkingPanels(data);		//	CoreMatchApplet has variable workingPanel
		add("Center", localWorkingPanel);
				
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
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("classLimits", "string");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	protected String getAxisInfo() {
		return getStringParam("axis");
	}
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	protected double[] getClassLimits() {
		String classLimitString = getStringParam("classLimits");
		if (classLimitString == null)
			return null;
		else {
			StringTokenizer st = new StringTokenizer(classLimitString);
			double class0Start = Double.parseDouble(st.nextToken());
			double classWidth = Double.parseDouble(st.nextToken());
			
			st = new StringTokenizer(getAxisInfo());
			st.nextToken();
			double axisMax = Double.parseDouble(st.nextToken());
			int nClasses = (int)Math.round(Math.floor((axisMax - class0Start) / classWidth));
			
			double classLimits[] = new double[nClasses + 1];
			for (int i=0 ; i<=nClasses ; i++)
				classLimits[i] = class0Start + i * classWidth;
			
			return classLimits;
		}
	}
	
	
//-----------------------------------------------------------
	
	protected int noOfItems() {
		return kNDisplayedDistns;
	}
	
	protected boolean retainFirstItems() {
		return false;
	}
	
	protected int getDragMatchHeight() {
		return leftPlots.getSize().height;
	}
	
	protected void setWorkingPanelLayout(XPanel thePanel) {
		thePanel.setLayout(new ProportionLayout(0.5, 20));
	}
	
	protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder) {
			XPanel dotPanel = new XPanel();
			dotPanel.setLayout(new AxisLayout());
			
				leftAxis = new HorizAxis(this);
			dotPanel.add("Bottom", leftAxis);
			
//				String[] yKeys = getYKeys();
				int leftDisplayType = hasOption("histogram") ? MultipleDistnView.HISTOGRAM
																										: MultipleDistnView.STACKED_DOT_PLOT;
				leftPlots = new MultipleDistnView(data, this, leftAxis, getYKeys(), leftOrder, leftDisplayType);
				leftPlots.lockBackground(Color.white);
				registerStatusItem("leftOrder", leftPlots);
			dotPanel.add("Center", leftPlots);
			
		thePanel.add(ProportionLayout.LEFT, dotPanel);
		
		return dotPanel;
	}
	
	protected XPanel addRightItems(XPanel thePanel, int[] rightOrder) {
			XPanel boxPanel = new XPanel();
			boxPanel.setLayout(new AxisLayout());
			
				rightAxis = new HorizAxis(this);
			boxPanel.add("Bottom", rightAxis);
			
				int rightDisplayType = hasOption("boxplot") ? MultipleDistnView.BOX_PLOT
																										: MultipleDistnView.CUM_DISTN;
				rightPlots = new MultipleDistnView(data, this, rightAxis, getYKeys(), rightOrder, rightDisplayType);
				rightPlots.lockBackground(Color.white);
				registerStatusItem("rightOrder", rightPlots);
			boxPanel.add("Center", rightPlots);
			
		thePanel.add(ProportionLayout.RIGHT, boxPanel);
		
		return boxPanel;
	}
	
	
//-----------------------------------------------------------
	
	private String[] getYKeys() {
		String yKeys[] = new String[kNDisplayedDistns];
		if (generator.length == kNDisplayedDistns)
			for (int i=0 ; i<kNDisplayedDistns ; i++)
				yKeys[i] = generator[i].getYKey();
		else {
			int destIndex = 0;
			Random rand = new Random();
			rand.setSeed(nextSeed());
			for (int i=0 ; i<generator.length ; i++) {
				double prob = (kNDisplayedDistns - destIndex) / (double)(generator.length - i);
				if (rand.nextDouble() < prob) {
					yKeys[destIndex ++] = generator[i].getYKey();
					if (destIndex == kNDisplayedDistns)
						break;
				}
			}
		}
		return yKeys;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		String yKeys[] = getYKeys();
		double classLimits[] = getClassLimits();
		
		leftAxis.readNumLabels(getAxisInfo());
		leftAxis.setAxisName(getVarName());
		leftAxis.invalidate();
		
		leftPlots.setYKeys(yKeys);
		leftPlots.setClassLimits(classLimits);
		leftPlots.repaint();
		
		rightAxis.readNumLabels(getAxisInfo());
		rightAxis.setAxisName(getVarName());
		rightAxis.invalidate();
		
		rightPlots.setYKeys(yKeys);
		rightPlots.setClassLimits(classLimits);
		rightPlots.repaint();
	}
	
	private void setScaling(String baseKey, String scaledKey, double targetMin, double targetMax) {
		NumVariable baseVar = (NumVariable)data.getVariable(baseKey);
		NumValue sortedY[] = baseVar.getSortedData();
		double dataMin = sortedY[0].toDouble();
		double dataMax = sortedY[sortedY.length - 1].toDouble();
		
		double factor = (targetMax - targetMin) / (dataMax - dataMin);
		double shift = targetMin - dataMin * factor;
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable(scaledKey);
		yVar.setScale(shift, factor, 9);
		yVar.clearSortedValues();
	}
	
	protected void setDataForQuestion() {
		for (int i=0 ; i<generator.length ; i++)
			generator[i].generateNextSample(data, getCount());
		
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		
		Random uniformGenerator = new Random(nextSeed());
		double minWidth = kMinWidthPropn * (axisMax - axisMin);
		double dataMin = axisMin + uniformGenerator.nextDouble() * (axisMax - axisMin - minWidth);
		double dataMax = dataMin + minWidth + uniformGenerator.nextDouble() * (axisMax - dataMin - minWidth);
		
		for (int i=0 ; i<generator.length ; i++)
			setScaling(generator[i].getBaseKey(), generator[i].getYKey(), dataMin, dataMax);
		
		super.setDataForQuestion();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		String displayString = hasOption("boxplot") ? "box plot"
														: hasOption("boxplot") ? "histogram" : "cumulative distribution";
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Reorder the " + displayString + "s (or stacked dot plots) by dragging so that each " + displayString + " corresponds to the dot plot on its left.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The correct matching of " + displayString + "s and stacked dot plots is shown.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched up the " + displayString + "s with the stacked dot plots of the data sets.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The red arrows indicate " + displayString + "s that do not correspond to the stacked dot plots on their left.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		generator = DistnGenerator.createGenerators(hasOption("easy"));
		
		for (int i=0 ; i<generator.length ; i++) {
				String baseKey = generator[i].getBaseKey();
				RandomContinuous gen = generator[i].getGenerator(this);
				gen.setSeed(nextSeed());
				NumSampleVariable baseVar = new NumSampleVariable(baseKey, gen, 9);
				baseVar.generateNextSample();
			data.addVariable(baseKey, baseVar);
			
				ScaledVariable yVar = new ScaledVariable("", baseVar, baseKey, 0.0, 1.0, 9);
				yVar.setRoundValues(true);
			data.addVariable(generator[i].getYKey(), yVar);
		}
		
		return data;
	}
	
//-----------------------------------------------------------
	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		rightPlots.repaint();
	}
	
}