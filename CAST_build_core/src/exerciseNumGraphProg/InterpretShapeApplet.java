package exerciseNumGraphProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreVariables.*;
import exercise2.*;
import coreGraphics.*;

import exerciseNumGraph.*;


public class InterpretShapeApplet extends ExerciseApplet {
	static final public int NORMAL = 0;
	static final public int SKEW_POS = 1;
	static final public int OUTLIER_POS = 2;
	static final public int CLUSTERS1 = 3;
	static final public int SKEW_NEG = 4;
	static final public int OUTLIER_NEG = 5;
	static final public int CLUSTERS2 = 6;
	
	static final private String[] kShapeString = {"normal", "skewPos", "outlierPos", "clusters1", "skewNeg", "outlierNeg", "clusters2"};
	
	static final private double kMinWidthPropn = 0.8;
	
	private RandomContinuous generator;
	
	private HorizAxis theAxis;
	private StackedDotPlotView theView;
	
	private ShapeChoicePanel shapeChoicePanel;
	
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
		registerParameter("count", "int");
		registerParameter("axis", "string");
		registerParameter("varName", "string");
//		registerParameter("shapeIndex", "choice");
		registerParameter("shapeValue", "int");
		registerParameter("optionText", "array");
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private int getShapeCode() {
		return getIntParam("shapeValue");
	}
	
	private String[] getOptionText() {
		return getArrayParam("optionText").getStrings();
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel dotPlotPanel = new XPanel();
			dotPlotPanel.setLayout(new AxisLayout());
			
				theAxis = new HorizAxis(this);
			dotPlotPanel.add("Bottom", theAxis);
			
				theView = new StackedDotPlotView(data, this, theAxis);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
			dotPlotPanel.add("Center", theView);
			
		thePanel.add("Center", dotPlotPanel);
		
			shapeChoicePanel = new ShapeChoicePanel(this, getShapeCode(), getOptionText());
			registerStatusItem("shapeChoice", shapeChoicePanel);
		thePanel.add("South", shapeChoicePanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		String yKey = kShapeString[getShapeCode()];
		theView.setActiveNumVariable(yKey);
		data.variableChanged(yKey);		//	clears selection
		
		shapeChoicePanel.changeOptions(getShapeCode(), getOptionText());
		shapeChoicePanel.clearRadioButtons();
		shapeChoicePanel.invalidate();
	}
	
	protected void setDataForQuestion() {
		int n = getCount();
		int shapeIndex = getShapeCode();
		
		NumSampleVariable zVar = (NumSampleVariable)data.getVariable("base_" + kShapeString[shapeIndex]);
		zVar.setSampleSize(n);
		zVar.generateNextSample();
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable(kShapeString[shapeIndex]);
		yVar.name = getVarName();
		
		StringTokenizer axisT = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(axisT.nextToken());
		double axisMax = Double.parseDouble(axisT.nextToken());
		
		Random uniformGenerator = new Random(nextSeed());
		double dataRange = (kMinWidthPropn + uniformGenerator.nextDouble() * (1 - kMinWidthPropn))
																																						* (axisMax - axisMin);
		double side = axisMax - axisMin - dataRange;
		double dataCenter = axisMin + side * uniformGenerator.nextDouble() + dataRange / 2;
		
		setScale(yVar, zVar, dataCenter, dataRange);
	}
	
	private void setScale(ScaledVariable yVar, NumVariable zVar, double center, double range) {
		double zMin = Double.POSITIVE_INFINITY;
		double zMax = Double.NEGATIVE_INFINITY;
		ValueEnumeration ze = zVar.values();
		while (ze.hasMoreValues()) {
			double z = ze.nextDouble();
			zMin = Math.min(zMin, z);
			zMax = Math.max(zMax, z);
		}
		
		double factor = range / (zMax - zMin);
		double shift = center - range / 2 - zMin * factor;
		
		yVar.setScale(shift, factor, 9);
		yVar.clearSortedValues();
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Select one of the options by clicking on a radio button.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("You must select an option by clicking a radio button.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				switch (getShapeCode()) {
					case NORMAL:
						messagePanel.insertText("The distribution is fairly symmetrical.");
						break;
					case SKEW_POS:
						messagePanel.insertText("The distribution is skew with a long tail to the right.");
						break;
					case SKEW_NEG:
						messagePanel.insertText("The distribution is skew with a long tail to the left.");
						break;
					case OUTLIER_POS:
					case OUTLIER_NEG:
						messagePanel.insertText("The distribution is reasonably symmetric, apart from one value that seems an outlier.");
						break;
					case CLUSTERS1:
					case CLUSTERS2:
						messagePanel.insertText("There seem to be two distinct clusters of values in the distribution.");
						break;
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your answer is correct.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("No. " + shapeChoicePanel.getCorrectOptionMessage());
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		for (int i=0 ; i<kShapeString.length ; i++) {
				String yKey = kShapeString[i];
				String baseKey = "base_" + yKey;
				generator = getGenerator(i);
				generator.setSeed(nextSeed());
				NumSampleVariable zVar = new NumSampleVariable("Normal", generator, 9);
				zVar.generateNextSample();
			data.addVariable(baseKey, zVar);
				
				ScaledVariable yVar = new ScaledVariable("", zVar, baseKey, 0.0, 1.0, 9);
			data.addVariable(yKey, yVar);
		}
		
		return data;
	}
	
	private RandomContinuous getGenerator(int shapeIndex) {
		RandomContinuous generator;
		switch (shapeIndex) {
			case NORMAL:
				generator = new RandomNormal(10, 0.0, 1.0, 2.5);
				generator.setNeatening(0.3);
				break;
			case SKEW_POS:
				generator = new RandomGamma(10, 2.0, 1.0, 10.0);
				generator.setNeatening(0.3);
				break;
			case SKEW_NEG:
				generator = new RandomGamma(10, 2.0, -1.0, -10.0);
				generator.setNeatening(0.3);
				break;
			case OUTLIER_POS:
				RandomContinuous mainGen = new RandomNormal(10, 0.0, 1.0, 2.5);
				mainGen.setNeatening(0.3);
				RandomContinuous outlierGen = new RandomRectangular(1, 4.5, 5.5);
				generator = new RandomMixture(mainGen, outlierGen);
				break;
			case OUTLIER_NEG:
				mainGen = new RandomNormal(10, 0.0, 1.0, 2.5);
				mainGen.setNeatening(0.3);
				outlierGen = new RandomRectangular(1, -5.5, -4.5);
				generator = new RandomMixture(mainGen, outlierGen);
				break;
			case CLUSTERS1:
				RandomContinuous leftGen = new RandomNormal(10, -2.5, 1.0, 2.5);
				leftGen.setNeatening(0.3);
				RandomContinuous rightGen = new RandomNormal(10, 2.5, 1.0, 2.5);
				rightGen.setNeatening(0.3);
				generator = new RandomMixture(10, leftGen, rightGen, 0.5);
				break;
			default:
			case CLUSTERS2:
				leftGen = new RandomNormal(10, -2.5, 1.0, 2.5);
				leftGen.setNeatening(0.3);
				rightGen = new RandomNormal(10, 2.5, 1.0, 2.5);
				rightGen.setNeatening(0.3);
				generator = new RandomMixture(10, leftGen, rightGen, 0.7);
				break;
		}
		return generator;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		return shapeChoicePanel.checkCorrect();
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		shapeChoicePanel.showAnswer();
	}
	
	protected double getMark() {
		return (shapeChoicePanel.checkCorrect() == ANS_CORRECT) ? 1 : 0;
	}
}