package exercisePercentProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreVariables.*;
import exercise2.*;

import exerciseNumGraph.*;
import exercisePercent.*;


public class DrawCumulativeApplet extends ExerciseApplet {
	static final private double kMinWidthPropn = 0.9;
	
	private RandomContinuous generator[];
	
	private HorizAxis theAxis;
	private DragCumHistoView theView;
	
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
		registerParameter("classLimits", "string");
		registerParameter("varName", "string");
		registerParameter("distnType", "choice");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private String getClassLimits() {
		String s = getStringParam("classLimits");
		return (s == null || s.length() == 0) ? null : s;
			
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private int getDistnType() {
		return getIntParam("distnType");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theAxis = new HorizAxis(this);
		thePanel.add("Bottom", theAxis);
		
			theView = new DragCumHistoView(data, this, "y", theAxis);
			theView.setCrossSize(DataView.LARGE_CROSS);
			theView.lockBackground(Color.white);
			registerStatusItem("cumCounts", theView);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		String classLimitString = getClassLimits();
		if (classLimitString == null)
			theView.changeExactCumulative();
		else {
			StringTokenizer st = new StringTokenizer(classLimitString);
			double class0Start = Double.parseDouble(st.nextToken());
			double classWidth = Double.parseDouble(st.nextToken());
			theView.changeClasses(class0Start, classWidth);
		}
		
		validate();
	}
	
	private void setScaling(NumVariable baseVar, ScaledVariable yVar, double targetMin, double targetMax) {
		NumValue sortedY[] = baseVar.getSortedData();
		double dataMin = sortedY[0].toDouble();
		double dataMax = sortedY[sortedY.length - 1].toDouble();
		
		double factor = (targetMax - targetMin) / (dataMax - dataMin);
		double shift = targetMin - dataMin * factor;
		
		yVar.setScale(shift, factor, 9);
		yVar.clearSortedValues();
		
		data.variableChanged("base");
	}
	
	protected void setDataForQuestion() {
		NumSampleVariable baseVar = (NumSampleVariable)data.getVariable("base");
		baseVar.setGenerator(generator[getDistnType()]);
		baseVar.setSampleSize(getCount());
		
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		double inset = (axisMax - axisMin) * 0.05;
		axisMin += inset;				//		no values within 1/20 of ends. This allows histo class boundaries to be away from ends.
		axisMax -= inset;
		
		Random uniformGenerator = new Random(nextSeed());
		double minWidth = kMinWidthPropn * (axisMax - axisMin);
		double targetMin = axisMin + uniformGenerator.nextDouble() * (axisMax - axisMin - minWidth);
		double targetMax = targetMin + minWidth + uniformGenerator.nextDouble() * (axisMax - targetMin - minWidth);
		
		baseVar.generateNextSample();
		setScaling(baseVar, (ScaledVariable)data.getVariable("y"), targetMin, targetMax);
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag to draw the cumulative distribution.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				if (getClassLimits() == null)
					messagePanel.insertText("The function should increase by 1/n at each data value.");
				else
					messagePanel.insertText("At the end of each class, the cumulative function increases by the proportion of values in the class.");
				messagePanel.insertText("\n(Click green circles to check that the correct proportion of values are less.)");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("This is the correct cumulative distribution function.");
				break;
			case ANS_WRONG:
				boolean isClose = theView.isCloseToCorrect();
				messagePanel.insertRedHeading(isClose ? "Close!\n" : "Wrong!\n");
				messagePanel.insertRedText("The red circles indicate wrong cumulative proportions.");
				if (getClassLimits() == null)
					messagePanel.insertRedText("\nThe red crosses indicate values where the cumulative proportion does not increases by 1/n.");
				else
					messagePanel.insertRedText("\nThe yellow rectangles in the histogram indicate classes where the cumulative proportion does not increase by the proportion in that class.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 130;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			
			generator = new RandomContinuous[4];
			generator[0] = new DistnGenerator(DistnGenerator.NORMAL, 0.2).getGenerator(this);
			generator[1] = new DistnGenerator(DistnGenerator.TWO_CLUSTER, 0.2).getGenerator(this);
			generator[2] = new DistnGenerator(DistnGenerator.RIGHT_SKEW, 0.2).getGenerator(this);
			generator[3] = new DistnGenerator(DistnGenerator.LEFT_SKEW, 0.2).getGenerator(this);
			
		for (int i=0 ; i<4 ; i++)
			generator[i].setSeed(nextSeed());
			
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator[0], 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable("", baseVar, "base", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		for (int i=0 ; i<generator.length ; i++)
			generator[i].setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		return theView.checkCumulative() ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		theView.showErrors();
		theView.repaint();
	}
	
	protected void showCorrectWorking() {
		theView.setCorrectCumulative();
		theView.repaint();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
}