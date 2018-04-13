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


public class MatchTransformApplet extends CoreMatchApplet {
	static final private String[] kTransformName = {"Square", "Square root", "Log", "Identity"};
	static final private String[] kTransformKeys = {"square", "root", "log", "identity"};
	static final private int kNTransformDistns = kTransformKeys.length;
	
	static final private double kMinWidthPropn = 0.9;
	
	static final private String kDefaultAxis = "1 2 3 1";
	
	private RandomContinuous generator[];
	
	private MultipleDistnView dotPlots, namesDisplay;
	
	
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
		registerParameter("count", "int");
		registerParameter("baseShape", "choice");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	protected int getBaseShape() {
		return getIntParam("baseShape");
	}
	
	
//-----------------------------------------------------------
	
	protected int noOfItems() {
		return kNTransformDistns;
	}
	
	protected boolean retainFirstItems() {
		return true;
	}
	
	protected int getDragMatchHeight() {
		return dotPlots.getSize().height;
	}
	
	protected void setWorkingPanelLayout(XPanel thePanel) {
		thePanel.setLayout(new BorderLayout(40, 0));
	}
	
	protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder) {
			XPanel dotPanel = new XPanel();
			dotPanel.setLayout(new AxisLayout());
			
				HorizAxis dotAxis = new HorizAxis(this);
				dotAxis.readNumLabels(kDefaultAxis);
			dotPanel.add("Bottom", dotAxis);
			
				dotPlots = new MultipleDistnView(data, this, dotAxis, kTransformKeys,
																					leftOrder, MultipleDistnView.STACKED_DOT_PLOT);
				dotPlots.lockBackground(Color.white);
				dotPlots.setFixedTopDistn(true);
				registerStatusItem("leftOrder", dotPlots);
			dotPanel.add("Center", dotPlots);
			
		thePanel.add("Center", dotPanel);
		
		return dotPanel;
	}
	
	protected XPanel addRightItems(XPanel thePanel, int[] rightOrder) {
			XPanel namesPanel = new InsetPanel(0, 0, 0, 1);
			namesPanel.setLayout(new BorderLayout(0, 0));
			
				namesDisplay = new MultipleDistnView(data, this, null, kTransformKeys,
																										rightOrder, MultipleDistnView.DISTN_NAMES);
				namesDisplay.setDrawGrid(false);
				namesDisplay.setFixedTopDistn(true);
				registerStatusItem("rightOrder", namesDisplay);
				
			namesPanel.add("Center", namesDisplay);
				
		thePanel.add("East", namesPanel);
		
		return namesPanel;
	}
	
	
//-----------------------------------------------------------

	
	private void setScaling(NumVariable baseVar, ScaledVariable yVar, double targetMin, double targetMax) {
		NumValue sortedY[] = baseVar.getSortedData();
		double dataMin = sortedY[0].toDouble();
		double dataMax = sortedY[sortedY.length - 1].toDouble();
		
		double factor = (targetMax - targetMin) / (dataMax - dataMin);
		double shift = targetMin - dataMin * factor;
		
		yVar.setScale(shift, factor, 9);
		yVar.clearSortedValues();
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		dotPlots.repaint();
		
		namesDisplay.repaint();
	}
	
	protected void setDataForQuestion() {
		RandomContinuous gen = generator[getBaseShape()];
		NumSampleVariable baseVar = (NumSampleVariable)data.getVariable("base");
		baseVar.setGenerator(gen);
		baseVar.setSampleSize(getCount());
		baseVar.generateNextSample();
		
		StringTokenizer st = new StringTokenizer(kDefaultAxis);
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		
		Random uniformGenerator = new Random(nextSeed());
		double minWidth = kMinWidthPropn * (axisMax - axisMin);
		double targetMin = axisMin + uniformGenerator.nextDouble() * (axisMax - axisMin - minWidth);
		double targetMax = targetMin + minWidth + uniformGenerator.nextDouble() * (axisMax - targetMin - minWidth);
		
		for (int i=0 ; i<kNTransformDistns ; i++) {
			NumVariable baseTransformVar = (NumVariable)data.getVariable("base" + i);
			baseTransformVar.clearSortedValues();
			setScaling(baseTransformVar, (ScaledVariable)data.getVariable(kTransformKeys[i]),
																																				targetMin, targetMax);
		}
		
		super.setDataForQuestion();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Reorder the stacked dot plots (or the transformation names) by dragging so that each dot plot shows the distribution that results from the named transformation of the top data set.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The correct matching of stacked dot plots and transformations is shown.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched up the stacked dot plots with the named transformations of the top data set.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The red arrows indicate errors.");
				messagePanel.insertText("\nSquare root and -log transformations reduce the size of the right tail and increase the left tail. Squaring values does the opposite.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		generator = new RandomContinuous[4];
		
		generator[0] = new RandomNormal(1, 3.0, 1.0, 2.5);
			generator[0].setNeatening(0.3);
			generator[0].setSeed(nextSeed());
		generator[1] = new RandomGamma(1, 2.0, 1.0, 15);
			generator[1].setNeatening(0.3);
			generator[1].setSeed(nextSeed());
//		generator[2] = new RandomGamma(1, 2.0, -1.0, -15);
//			generator[2].setNeatening(0.3);
			
			RandomNormal lowClusterGen = new RandomNormal(1, 5, 1.7, 2.5);
			lowClusterGen.setSeed(nextSeed());
			lowClusterGen.setNeatening(0.3);
			RandomNormal highClusterGen = new RandomNormal(1, 9, 0.8, 2.5);
			highClusterGen.setSeed(nextSeed());
			highClusterGen.setNeatening(0.3);
		generator[2] = new RandomMixture(1, lowClusterGen, highClusterGen, 0.35);
			generator[2].setSeed(nextSeed());
		
			lowClusterGen = new RandomNormal(1, 3, 1.0, 2.5);
			lowClusterGen.setSeed(nextSeed());
			lowClusterGen.setNeatening(0.3);
			highClusterGen = new RandomNormal(1, 8, 1.0, 2.5);
			highClusterGen.setSeed(nextSeed());
			highClusterGen.setNeatening(0.3);
		generator[3] = new RandomMixture(1, lowClusterGen, highClusterGen, 0.5);
			generator[3].setSeed(nextSeed());
		
		NumSampleVariable baseVar = new NumSampleVariable("Base", generator[0], 9);
		baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
		PowerVariable basePower[] = new PowerVariable[4];
		basePower[0] = new PowerVariable("BaseRoot", baseVar, 2.0, 9);		//	square
		basePower[1] = new PowerVariable("BaseRoot", baseVar, 0.5, 9);		//	root
		basePower[2] = new PowerVariable("BaseRoot", baseVar, 0.0, 9);		//	log
		basePower[3] = new PowerVariable("BaseRoot", baseVar, 1.0, 9);		//	identity
		
		for (int i=0 ; i<4 ; i++) {
			data.addVariable("base" + i, basePower[i]);
			ScaledVariable yVar = new ScaledVariable(kTransformName[i], basePower[i], "base" + i, 0.0, 1.0, 9);
			data.addVariable(kTransformKeys[i], yVar);
		}
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		for (int i=0 ; i<generator.length ; i++)
			generator[i].setSeed(nextSeed());
	}
	
//-----------------------------------------------------------
	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		namesDisplay.repaint();
	}
	
}