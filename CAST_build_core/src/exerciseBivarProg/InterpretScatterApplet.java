package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import random.*;
import exercise2.*;
import coreGraphics.*;
import imageUtils.*;

import exerciseBivar.*;



public class InterpretScatterApplet extends ExerciseApplet {
	static final private int UNCORREL = 0;
	static final private int POS_CORREL = 1;
	static final private int NEG_CORREL = 2;
	static final private int OUTLIER_BOTH = 3;
	static final private int OUTLIER_XY = 4;
	static final private int TWO_GROUP_POS = 5;
	static final private int TWO_GROUP_NEG = 6;
	
	static final protected String kXKeys[] = new String[7];
	static final protected String kYKeys[] = new String[7];
	static {
		for (int i=0 ; i<7 ; i++) {
			kXKeys[i] = "x[" + i + "]";
			kYKeys[i] = "y[" + i + "]";
		}
	}
	
	protected Drag4LabelPanel dragPanel;
	private HorizAxis xAxis[] = new HorizAxis[4];
	private VertAxis yAxis[] = new VertAxis[4];
	private ScatterView theView[] = new ScatterView[4];
	
	protected String xAxisInfo[];
	protected String yAxisInfo[];
	
	protected int type[];
	protected RandomInteger typePermGenerator;
	protected int messagePermutation[];
	protected RandomInteger messagePermGenerator;
	
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
		registerParameter("xVarName", "string");
		registerParameter("xAxis", "array");
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "array");
		registerParameter("count", "int");
		registerParameter("uncorrel", "string");
		registerParameter("posCorrel", "string");
		registerParameter("negCorrel", "string");
		registerParameter("outlierBoth", "string");
		registerParameter("outlierXY", "string");
		registerParameter("twoGroupPos", "string");
		registerParameter("twoGroupNeg", "string");
	}
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	protected StringArray getXAxes() {
		return getArrayParam("xAxis");
	}
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	protected StringArray getYAxes() {
		return getArrayParam("yAxis");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private String getRelnGifName(int index) {
		String name = (index == 0) ? "uncorrel"
									: (index == 1) ? "posCorrel"
									: (index == 2) ? "negCorrel"
									: (index == 3) ? "outlierBoth"
									: (index == 4) ? "outlierXY"
									: (index == 5) ? "twoGroupPos"
									: "twoGroupNeg";
		return getStringParam(name);
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		dragPanel = new Drag4LabelPanel(10, 4, 0, this);
		registerStatusItem("messagePerm", dragPanel);
		
		type = new int[7];
		for (int i=0 ; i<7 ; i++)
			type[i] = i;
		typePermGenerator = new RandomInteger(0, 6, 7);
		typePermGenerator.setSeed(nextSeed());
		
		messagePermutation = new int[4];
		for (int i=0 ; i<4 ; i++)
			messagePermutation[i] = i;
		messagePermGenerator = new RandomInteger(0, 3, 4);
		messagePermGenerator.setSeed(nextSeed());
		
		for (int i=0 ; i<4 ; i++)
			dragPanel.add(new ImageCanvas(null, 0, 0, this), Drag4LabelPanel.LABEL_COMPONENT, i);	//	z-order front
		
		for (int i=0 ; i<4 ; i++)
			dragPanel.add(Drag4LabelPanel.ITEM_COMPONENT, scatterPanel(data, type[i], i));
		
		return dragPanel;
	}
	
	protected XPanel scatterPanel(DataSet data, int type, int panelIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				xAxis[panelIndex] = new HorizAxis(this);
			displayPanel.add("Bottom", xAxis[panelIndex]);
			
				yAxis[panelIndex] = new VertAxis(this);
			displayPanel.add("Left", yAxis[panelIndex]);
			
				theView[panelIndex] = new ScatterView(data, this, xAxis[panelIndex], yAxis[panelIndex],
																				kXKeys[type], kYKeys[type]);
				theView[panelIndex].lockBackground(Color.white);
			displayPanel.add("Center", theView[panelIndex]);
			
		thePanel.add("Center", displayPanel);
		return thePanel;
	}
	
	protected void permute(int[] index, RandomInteger permGenerator) {
		int[] swap = permGenerator.generate();
		for (int i=0 ; i<index.length ; i++)
			if (i != swap[i]) {
				int temp = index[i];
				index[i] = index[swap[i]];
				index[swap[i]] = temp;
			}
	}
	
	protected void setupLabels() {
		String messageGif[] = new String[4];
		for (int i=0 ; i<4 ; i++) {
			int displayIndex = type[i];
			messageGif[i] = getRelnGifName(displayIndex);
		}
		
		dragPanel.setGifMessages(messageGif, messagePermutation);
	}
	
	protected void setDisplayForQuestion() {
		permute(type, typePermGenerator);
		
		permute(messagePermutation, messagePermGenerator);
//			for (int i=0 ; i<4 ; i++)
//				messagePermutation[i] = i;
		
		for (int i=0 ; i<4 ; i++) {
			int displayIndex = type[i];
			
			xAxis[i].readNumLabels(xAxisInfo[displayIndex]);
			xAxis[i].invalidate();
			
			yAxis[i].readNumLabels(yAxisInfo[displayIndex]);
			yAxis[i].invalidate();
			
			theView[i].changeVariables(kYKeys[displayIndex], kXKeys[displayIndex]);
		}
		
		setupLabels();
	}
	
	private void resampleOneCluster(int type, int n, double xMin, double xMax, double yMin, double yMax) {
		double r = 0.0;
		if (type != UNCORREL) {
			RandomRectangular generator = new RandomRectangular(1, 0.8, 0.95);
			generator.setSeed(nextSeed());
			r = generator.generateOne();
			if (type == NEG_CORREL)
				r = -r;
		}
		double xMean = (xMin + xMax) / 2;
		double xSd = (xMax - xMin) / 6;
		double yMean = (yMin + yMax) / 2;
		double ySd = (yMax - yMin) / 6;
		
		String xKey = kXKeys[type];
		String yKey = kYKeys[type];
		NumSampleVariable xCoreVar = (NumSampleVariable)data.getVariable(xKey + "_Base");
		NumSampleVariable yCoreVar = (NumSampleVariable)data.getVariable(yKey + "_Base");
		
		xCoreVar.setSampleSize(n);
		xCoreVar.generateNextSample();
		
		yCoreVar.setSampleSize(n);
		yCoreVar.generateNextSample();
		
		ScaledVariable xVar = (ScaledVariable)data.getVariable(xKey);
		xVar.setScale(xMean, xSd, 9);
		xVar.name = getXVarName();
		
		CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable(yKey);
		yVar.setMeanSdCorr(yMean, ySd, r, 9);
		yVar.name = getYVarName();
		
		data.variableChanged(xKey);
		data.variableChanged(yKey);
	}
	
	protected void resampleTwoClusters(String xKey, String yKey, int[] n, double[] xMean, double[] xSd,
																												double[] yMean, double[] ySd, double[] r) {
		for (int i=0 ; i<2 ; i++) {
			NumSampleVariable xCoreVar = (NumSampleVariable)data.getVariable(xKey + "_Base" + i);
			NumSampleVariable yCoreVar = (NumSampleVariable)data.getVariable(yKey + "_Base" + i);
			
			xCoreVar.setSampleSize(n[i]);
			xCoreVar.generateNextSample();
			
			yCoreVar.setSampleSize(n[i]);
			yCoreVar.generateNextSample();
			
			ScaledVariable xVar = (ScaledVariable)data.getVariable(xKey + i);
			xVar.setScale(xMean[i], xSd[i], 9);
			
			CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable(yKey + i);
			yVar.setMeanSdCorr(yMean[i], ySd[i], r[i], 9);
		}
		
		StackedNumVariable xVar = (StackedNumVariable)data.getVariable(xKey);
		xVar.name = getXVarName();
		StackedNumVariable yVar = (StackedNumVariable)data.getVariable(yKey);
		yVar.name = getYVarName();
		
		data.variableChanged(xKey);
		data.variableChanged(yKey);
	}
	
	private void resampleTwoClusters(int type, int nTotal, double xMin, double xMax, double yMin, double yMax) {
		int n[] = new int[2];
		double xMean[] = new double[2];
		double xSd[] = new double[2];
		double yMean[] = new double[2];
		double ySd[] = new double[2];
		double r[] = new double[2];
		
		String xKey = kXKeys[type];
		String yKey = kYKeys[type];
		
		switch (type) {
			case OUTLIER_BOTH: {
				n[0] = nTotal - 1;
				n[1] = 1;
				
				RandomRectangular generator = new RandomRectangular(1, 0.65, 0.85);
				generator.setSeed(nextSeed());
				r[0] = generator.generateOne();
				
				xMean[0] = (2.5 * xMin + xMax) / 3.5;
				xSd[0] = (xMax - xMin) / 8;
				yMean[0] = (2.5 * yMin + yMax) / 3.5;
				ySd[0] = (yMax - yMin) / 8;
				
				
				r[1] = 0.0;
				xMean[1] = (xMin + 9 * xMax) / 10;
				xSd[1] = (xMax - xMin) / 20;
				yMean[1] = (yMin + 9 * yMax) / 10;
				ySd[1] = (yMax - yMin) / 20;
				break;
				}
			case OUTLIER_XY: {
				n[0] = nTotal - 1;
				n[1] = 1;
				
				RandomRectangular generator = new RandomRectangular(1, 0.9, 0.95);
				generator.setSeed(nextSeed());
				r[0] = generator.generateOne();
				
				xMean[0] = (xMin + xMax) / 2;
				yMean[0] = (yMin + yMax) / 2;
				xSd[0] = (xMax - xMin) / 6;
				ySd[0] = (yMax - yMin) / 6;
				
				r[1] = 0.0;
				boolean topLeft = new Random(nextSeed()).nextBoolean();
				if (topLeft) {
					xMean[1] = (xMin + 3 * xMax) / 4;
					yMean[1] = (3 * yMin + yMax) / 4;
				}
				else {
					xMean[1] = (3 * xMin + xMax) / 4;
					yMean[1] = (yMin + 3 * yMax) / 4;
				}
				xSd[1] = (xMax - xMin) / 20;
				ySd[1] = (yMax - yMin) / 20;
				break;
				}
			case TWO_GROUP_POS:
			case TWO_GROUP_NEG: {
				n[0] = nTotal / 2;
				n[1] = nTotal - n[0];
				
				RandomRectangular generator = new RandomRectangular(1, -0.4, 0.4);
				generator.setSeed(nextSeed());
				double rr = generator.generateOne();
				
				xMean[0] = (3 * xMin + xMax) / 4;
				xMean[1] = (xMin + 3 * xMax) / 4;
				
				if (type == TWO_GROUP_POS) {
					yMean[0] = (3 * yMin + yMax) / 4;
					yMean[1] = (yMin + 3 * yMax) / 4;
				}
				else {
					yMean[0] = (yMin + 3 * yMax) / 4;
					yMean[1] = (3 * yMin + yMax) / 4;
				}
				
				for (int i=0 ; i<2 ; i++) {
					xSd[i] = (xMax - xMin) / 10;
					ySd[i] = (yMax - yMin) / 10;
					r[i] = rr;
				}
				break;
				}
		}
		resampleTwoClusters(xKey, yKey, n, xMean, xSd, yMean, ySd, r);
	}
	
	protected String[] setupAxis(StringArray axisList) {
		int nAxes = axisList.getNoOfStrings();
		
		String axisInfo[] = new String[7];
		RandomInteger axisGenerator = new RandomInteger(0, nAxes - 1, 7);
		axisGenerator.setSeed(nextSeed());
		int[] index = axisGenerator.generate();
		
		for (int i=0 ; i<7 ; i++)
			axisInfo[i] = axisList.getValue(index[i]);
		
		return axisInfo;
	}
	
	protected void setDataForQuestion() {
		xAxisInfo = setupAxis(getXAxes());
		yAxisInfo = setupAxis(getYAxes());
		
		int nTotal = getCount();
		
		for (int i=0 ; i<7 ; i++) {
			StringTokenizer st = new StringTokenizer(xAxisInfo[i]);
			double xMin = Double.parseDouble(st.nextToken());
			double xMax = Double.parseDouble(st.nextToken());
			st = new StringTokenizer(yAxisInfo[i]);
			double yMin = Double.parseDouble(st.nextToken());
			double yMax = Double.parseDouble(st.nextToken());
			
			if (i < 3)
				resampleOneCluster(i, nTotal, xMin, xMax, yMin, yMax);
			else
				resampleTwoClusters(i, nTotal, xMin, xMax, yMin, yMax);
		}
	}
	
//-----------------------------------------------------------
	
	protected void addOneCluster(String xKey, String yKey, DataSet data) {
			RandomNormal xGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			xGenerator.setNeatening(0.3);
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xBaseVar = new NumSampleVariable("Z_" + xKey, xGenerator, 9);
			xBaseVar.generateNextSample();
			String xBaseKey = xKey + "_Base";
		data.addVariable(xBaseKey, xBaseVar);
		
			ScaledVariable xVar = new ScaledVariable(xKey, xBaseVar, xBaseKey, 0.0, 1.0, 9);
		data.addVariable(xKey, xVar);
		
			RandomNormal yGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			yGenerator.setNeatening(0.3);
			yGenerator.setSeed(nextSeed());
			NumSampleVariable yBaseVar = new NumSampleVariable("Z_" + yKey, yGenerator, 9);
			yBaseVar.generateNextSample();
			String yBaseKey = yKey + "_Base";
		data.addVariable(yBaseKey, yBaseVar);
			
			CorrelatedVariable yVar = new CorrelatedVariable(yKey, data, xBaseKey, yBaseKey, 9);
		data.addVariable(yKey, yVar);
	}
	
	protected void addTwoClusters(String xKey, String yKey, DataSet data) {
		String xKeys[] = new String[2];
		String yKeys[] = new String[2];
		
		for (int i=0 ; i<2 ; i++) {
				RandomNormal xGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
				xGenerator.setNeatening(0.3);
				xGenerator.setSeed(nextSeed());
				NumSampleVariable xBaseVar = new NumSampleVariable("Z_" + xKey + i, xGenerator, 9);
				xBaseVar.generateNextSample();
				String xBaseKey = xKey + "_Base" + i;
			data.addVariable(xBaseKey, xBaseVar);
			
				ScaledVariable xiVar = new ScaledVariable(xKey + i, xBaseVar, xBaseKey, 0.0, 1.0, 9);
				xKeys[i] = xKey + i;
			data.addVariable(xKeys[i], xiVar);
			
				RandomNormal yGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
				yGenerator.setNeatening(0.3);
				yGenerator.setSeed(nextSeed());
				NumSampleVariable yBaseVar = new NumSampleVariable("Z_" + yKey + i, yGenerator, 9);
				yBaseVar.generateNextSample();
				String yBaseKey = yKey + "_Base" + i;
			data.addVariable(yBaseKey, yBaseVar);
				
				CorrelatedVariable yiVar = new CorrelatedVariable(yKey + i, data, xBaseKey, yBaseKey, 9);
				yKeys[i] = yKey + i;
			data.addVariable(yKeys[i], yiVar);
		}
		
			StackedNumVariable xVar = new StackedNumVariable("", data, xKeys);
		data.addVariable(xKey, xVar);
		
			StackedNumVariable yVar = new StackedNumVariable("", data, yKeys);
		data.addVariable(yKey, yVar);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		for (int i=0 ; i<3 ; i++)
			addOneCluster(kXKeys[i], kYKeys[i], data);
		
		for (int i=3 ; i<7 ; i++)
			addTwoClusters(kXKeys[i], kYKeys[i], data);
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		typePermGenerator.setSeed(nextSeed());
		messagePermGenerator.setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the four statements onto the scatterplots that they best describe.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer");
				boolean allWrong[] = {false, false, false, false};		//	to show all messages
				addScatterDescriptions(allWrong, messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched the statements to the scatterplots.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The statements outlined in red are not the best descriptions of the scatterplots above them.");
				boolean correct[] = dragPanel.checkCorrectMessages();
				addScatterDescriptions(correct, messagePanel);
				
				break;
		}
	}
	
	private void addScatterDescriptions(boolean[] correct, MessagePanel messagePanel) {
		boolean doneOutlier = false;
		boolean doneClusters = false;
		for (int i=0 ; i<4 ; i++)
			if (!correct[i])
				switch (type[i]) {
					case UNCORREL:
						messagePanel.insertText("\nIf there is no upward or downward trend in the scatterplot, the variables are not related.");
						break;
					case POS_CORREL:
						messagePanel.insertText("\nIf there is an upward trend in the scatterplot, high values of one variable are associated with high values of the other.");
						break;
					case NEG_CORREL:
						messagePanel.insertText("\nIf there is a downward trend in the scatterplot, high values of one variable are associated with low values of the other, and vice versa.");
						break;
					case OUTLIER_BOTH:
					case OUTLIER_XY:
						if (!doneOutlier) {
							messagePanel.insertText("\nA point far from the main cloud of crosses is an outlier. It may either follow the same trend as the main cloud or not.");
							doneOutlier = true;
						}
						break;
					case TWO_GROUP_POS:
					case TWO_GROUP_NEG:
						if (!doneClusters) {
							messagePanel.insertText("\nIf there are two distinct clusters of points, they could corresond to different types of item.");
							doneClusters = true;
						}
						break;
				}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		boolean correct[] = dragPanel.checkCorrectMessages();
		boolean allCorrect = true;
		for (int i=0 ; i<4 ; i++)
			allCorrect = allCorrect && correct[i];
		
		return allCorrect ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		dragPanel.highlightCorrectMessages();
	}
	
	protected void showCorrectWorking() {
		dragPanel.showCorrectMessages();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
}