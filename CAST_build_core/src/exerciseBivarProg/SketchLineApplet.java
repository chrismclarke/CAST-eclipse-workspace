package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import models.*;
import exercise2.*;
import formula.*;

import regn.*;
import exerciseBivar.*;


public class SketchLineApplet extends ExerciseApplet {
	static final public int ANS_WRONG_AXIS = ANS_WRONG;
	static final public int ANS_WRONG_LINE = ANS_WRONG + 1;
	
	static final private String kZeroOneAxisInfo = "0 1 2 1";
	static final private NumValue kZeroValue = new NumValue(0.0, 0);
	
	private MultiHorizAxis xAxis;
	private MultiVertAxis yAxis;
	private LineDragView theView;
	private LinearEquationView eqnView;
	
	private XChoice yAxisChoice;
	private int currentYAxisIndex;
	
	private XLabel yVarLabel;
	
	private FittedValTemplatePanel lowFitTemplate, highFitTemplate;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 10));
			
				XPanel eqnPanel = new XPanel();
				eqnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					eqnView = new LinearEquationView(data, this, "correctLine",
							getYVarName(), getXVarName(), null, null, null, null);
				eqnPanel.add(eqnView);
			
			mainPanel.add("North", eqnPanel);
			
			mainPanel.add("Center", getWorkingPanels(data));
		
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
		registerParameter("xVarName", "string");
		registerParameter("xAxis", "string");							//	single axes
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "array");							//	list for all possible axes
		registerParameter("axisLabels", "array");					//	to appear in XChoice related to axis
		registerParameter("correctYAxis", "int");
		registerParameter("yPropnAtLowX", "const");						//	min and max Y-propn at xAxis.minOnAxis
		registerParameter("yPropnAtHighX", "const");						//	min and max Y-propn at xAxis.maxOnAxis
		registerParameter("interceptDecimals", "int");
		registerParameter("slopeDecimals", "int");
	}
	
/*
	private String[] parseStringArray(String allStrings, String delimiter) {
		StringTokenizer st = new StringTokenizer(allStrings, delimiter);
		String[] result = new String[st.countTokens()];
		int index = 0;
		while (st.hasMoreTokens())
			result[index ++] = st.nextToken();
		return result;
	}
*/
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private String getXAxisInfo() {
		return getStringParam("xAxis");
	}
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private String[] getYAxisInfo() {
		return getArrayParam("yAxis").getStrings();
//		return parseStringArray(getStringParam("yAxis"), "*");
	}
	
	private String[] getYAxisLabels() {
		return getArrayParam("axisLabels").getStrings();
//		return parseStringArray(getStringParam("axisLabels"), "*");
	}
	
	private int getCorrectYAxis() {
		return getIntParam("correctYAxis");
	}
	
	protected NumValue getYPropnAtLowX() {
		return getNumValueParam("yPropnAtLowX");
	}
	
	protected NumValue getYPropnAtHighX() {
		return getNumValueParam("yPropnAtHighX");
	}
	
	private int getInterceptDecimals() {
		return getIntParam("interceptDecimals");
	}
	
	private int getSlopeDecimals() {
		return getIntParam("slopeDecimals");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel graphPanel = new InsetPanel(40, 0);
			graphPanel.setLayout(new AxisLayout());
			
				xAxis = new MultiHorizAxis(this, 1);
				xAxis.setChangeMinMax(false);
			graphPanel.add("Bottom", xAxis);
			
				yAxis = new MultiVertAxis(this, 1);		//	no of alternates will be changed by setupDisplay()
				yAxis.setChangeMinMax(false);
			graphPanel.add("Left", yAxis);
			
				theView = new LineDragView(data, this, xAxis, yAxis, "x01", "y01", "x", "y", "line01");
				theView.lockBackground(Color.white);
				registerStatusItem("drag", theView);
			graphPanel.add("Center", theView);
		
		thePanel.add("Center", graphPanel);
		
		if (hasOption("multipleAxes")) {
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
				yAxisChoice = new XChoice("", XChoice.HORIZONTAL, this);
			registerStatusItem("axisChoice", yAxisChoice);
			choicePanel.add(yAxisChoice);
			
			thePanel.add("North", choicePanel);
		}
		else {
			yVarLabel = new XLabel("", XLabel.LEFT, this);
			thePanel.add("North", yVarLabel);
		}
		
			XPanel fitPanel = new XPanel();
			fitPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 2));
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				lowFitTemplate = new FittedValTemplatePanel(stdContext);
				registerStatusItem("lowTemplate", lowFitTemplate);
			fitPanel.add(lowFitTemplate);
				highFitTemplate = new FittedValTemplatePanel(stdContext);
				registerStatusItem("highTemplate", highFitTemplate);
			fitPanel.add(highFitTemplate);
		thePanel.add("South", fitPanel);
		
		return thePanel;
	}
	
	private NumValue maxEditValue(String axisInfo) {
		StringTokenizer st = new StringTokenizer(axisInfo);
		st.nextToken();
		NumValue maxVal = new NumValue(st.nextToken());
		
		maxVal.decimals += 2;
		maxVal.setValue(maxVal.toDouble() * 10);
		return maxVal;
	}
	
	protected void setDisplayForQuestion() {
		xAxis.setNoOfAlternates(2);
		xAxis.readNumLabels(kZeroOneAxisInfo);
		xAxis.readExtraNumLabels(getXAxisInfo());
		xAxis.setStartAlternate(1);
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		String[] yAxisInfo = getYAxisInfo();
		yAxis.setNoOfAlternates(yAxisInfo.length + 1);
		yAxis.readNumLabels(kZeroOneAxisInfo);
		for (int i=0 ; i<yAxisInfo.length ; i++)
			yAxis.readExtraNumLabels(yAxisInfo[i]);
		
		if (yAxisChoice != null) {
			yAxis.setStartAlternate(1);
			yAxisChoice.clearItems();
			String[] yAxisNames = getYAxisLabels();
			for (int i=0 ; i<yAxisNames.length ; i++)
				yAxisChoice.addItem(yAxisNames[i]);
			currentYAxisIndex = 0;
			yAxisChoice.changeLabel(getYVarName());		//	does invalidate()
		}
		else {
			yAxis.setStartAlternate(getCorrectYAxis() + 1);
			changeAxis(getCorrectYAxis());
			yVarLabel.setText(getYVarName());
		}
		
		theView.setSelectedIndex(-1);
		
		eqnView.setExplanName(getXVarName());
		eqnView.setYName(getYVarName());
		LinearModel correctLine = (LinearModel)data.getVariable("correctLine");
		NumValue intercept = correctLine.getIntercept();
		NumValue slope = correctLine.getSlope();
		eqnView.setMinMaxParams(intercept, intercept, slope, slope);		//	does invalidate()
		
		int correctYAxisIndex = getCorrectYAxis();
		String correctYAxisInfo = getYAxisInfo()[correctYAxisIndex];
		NumValue maxY = maxEditValue(correctYAxisInfo);
		
		lowFitTemplate.changeMaxValue(maxY);
		lowFitTemplate.setValues(kZeroValue, kZeroValue, kZeroValue);
		
		highFitTemplate.changeMaxValue(maxY);
		highFitTemplate.setValues(kZeroValue, kZeroValue, kZeroValue);
	}
	
	private void rescaleForNewAxis(String axisInfo, String key) {
		StringTokenizer st = new StringTokenizer(axisInfo);
		NumValue minVal = new NumValue(st.nextToken());
		NumValue maxVal = new NumValue(st.nextToken());
		double min = minVal.toDouble();
		double max = maxVal.toDouble();
		int decimals = Math.max(minVal.decimals, maxVal.decimals) + 2;
		
		ScaledVariable var = (ScaledVariable)data.getVariable(key);
		var.setScale(min, max - min, decimals);
	}
	
	private NumValue round(double y, int decimals) {
		for (int i=0 ; i<decimals ; i++)
			y *= 10.0;
		y = Math.rint(y);
		for (int i=0 ; i<decimals ; i++)
			y /= 10.0;
		return new NumValue(y, decimals);
	}
	
	protected void setDataForQuestion() {
		NumVariable x01Var = (NumVariable)data.getVariable("x01");
		x01Var.readValues("0.1 0.9");
		
		NumVariable y01Var = (NumVariable)data.getVariable("y01");
		y01Var.readValues("0.1 0.9");
		
		LinearModel line01Var = (LinearModel)data.getVariable("line01");
		line01Var.updateLSParams("y01");			//	don't worry about decimals
		
		String startYAxisInfo = getYAxisInfo()[0];
		rescaleForNewAxis(startYAxisInfo, "y");
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		yVar.name = getYVarName();
		
		rescaleForNewAxis(getXAxisInfo(), "x");
		ScaledVariable xVar = (ScaledVariable)data.getVariable("x");
		xVar.name = getXVarName();
		
		int correctYAxisIndex = getCorrectYAxis();
		String correctYAxisInfo = getYAxisInfo()[correctYAxisIndex];
		StringTokenizer st = new StringTokenizer(correctYAxisInfo);
		NumValue minVal = new NumValue(st.nextToken());
		NumValue maxVal = new NumValue(st.nextToken());
		double yCorrectMin = minVal.toDouble();
		double yCorrectMax = maxVal.toDouble();
		
		st = new StringTokenizer(getXAxisInfo());
		double xMin = Double.parseDouble(st.nextToken());
		double xMax = Double.parseDouble(st.nextToken());
		
		int interceptDecimals = getInterceptDecimals();
		int slopeDecimals = getSlopeDecimals();
		LinearModel correctLineVar = (LinearModel)data.getVariable("correctLine");
		double y0 = yCorrectMin + (yCorrectMax - yCorrectMin) * getYPropnAtLowX().toDouble();
		double y1 = yCorrectMin + (yCorrectMax - yCorrectMin) * getYPropnAtHighX().toDouble();
		
		NumValue slope = round((y1 - y0) / (xMax - xMin), slopeDecimals);
		NumValue intercept = round(y0 - slope.toDouble() * xMin, interceptDecimals);
		correctLineVar.setParameters(intercept, slope);
		correctLineVar.name = getYVarName();
		
		data.variableChanged("y01");
		data.variableChanged("correctLine");
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				if (hasOption("multipleAxes"))
					messagePanel.insertText("Use the pop-up menu to select an axis that can display the line, then drag");
				else
					messagePanel.insertText("Drag");
				messagePanel.insertText(" the two red circles to position the line.\n(The two templates under the diagram can be used to evaluate points on the line.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The templates shows how to find " + getYVarName() + " corresponding to "
												+ getXVarName() + " at both ends of the horizontal axis. The red circles on the line have been moved to these positions.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have positioned the line correctly.");
				break;
			case ANS_WRONG_AXIS:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Your vertical axis cannot display the " + getYVarName()
														+ " corresponding to both low and high " + getXVarName()
														+ " (values at both ends of the horizontal axis).\nUse the templates to evaluate these values of "
														+ getYVarName() + " and ensure that the axis can display them.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("Your line is close to its correct position, but you should be able to get closer. (Use the templates to find two points on the line. Then click on the red circles and use the arrow keys on the keyboard for fine adjustment.)");
				break;
			case ANS_WRONG_LINE:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("Use the templates to find the " + getYVarName() + " corresponding to values of "
												+ getXVarName() + " near the two ends of the horizontal axis then drag the red dots to ensure that the line passes through these points.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable x01Var = new NumVariable("X01");
			x01Var.readValues("0.1 0.9");
		data.addVariable("x01", x01Var);
			
			ScaledVariable xVar = new ScaledVariable("X", x01Var, "x01", 0.0, 1.0, 9);
		data.addVariable("x", xVar);
		
			NumVariable y01Var = new NumVariable("Y01");
			y01Var.readValues("0.1 0.9");
		data.addVariable("y01", y01Var);
			
			ScaledVariable yVar = new ScaledVariable("Y", y01Var, "y01", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		
			LinearModel line01Var = new LinearModel("Line01", data, "x01", new NumValue(0, 0), new NumValue(1, 0));
		data.addVariable("line01", line01Var);
		
			LinearModel correctLineVar = new LinearModel("CorrectLine", data, "x", new NumValue(0, 0), new NumValue(1, 0));
		data.addVariable("correctLine", correctLineVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (currentYAxisIndex != getCorrectYAxis())
			return ANS_WRONG_AXIS;
		else {
			double y0 = getYPropnAtLowX().toDouble();
			double y1 = getYPropnAtHighX().toDouble();
			double slope01 = y1 - y0;
			double intercept01 = y0;
			
			return theView.checkLinePosition(intercept01, slope01);
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		if (yAxisChoice != null) {
			currentYAxisIndex = getCorrectYAxis();
			changeAxis(currentYAxisIndex);
			yAxisChoice.select(currentYAxisIndex);
		}
		
		LinearModel correctLine = (LinearModel)data.getVariable("correctLine");
		NumValue intercept = correctLine.getIntercept();
		NumValue slope = correctLine.getSlope();
		
		int correctYAxisIndex = getCorrectYAxis();
		String correctYAxisInfo = getYAxisInfo()[correctYAxisIndex];
		StringTokenizer st = new StringTokenizer(correctYAxisInfo);
		double yMin = Double.parseDouble(st.nextToken());
		double yMax = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(getXAxisInfo());
		double xMin = Double.parseDouble(st.nextToken());
		double xMax = Double.parseDouble(st.nextToken());
		
		LinearModel line01 = (LinearModel)data.getVariable("line01");
		double intercept01 = (intercept.toDouble() - yMin + slope.toDouble() * xMin) / (yMax - yMin);
		double slope01 = slope.toDouble() * (xMax - xMin) / (yMax - yMin);
		line01.setIntercept(intercept01);
		line01.setSlope(slope01);
		
		NumVariable xVar01 = (NumVariable)data.getVariable("x01");
		NumVariable yVar01 = (NumVariable)data.getVariable("y01");
		
		NumValue x01Low = (NumValue)xVar01.valueAt(0);
		x01Low.setValue(0.0);
		NumValue x01High = (NumValue)xVar01.valueAt(1);
		x01High.setValue(1.0);
		
		NumValue y01Low = (NumValue)yVar01.valueAt(0);
		y01Low.setValue(line01.evaluateMean(x01Low.toDouble()));
		
		NumValue y01High = (NumValue)yVar01.valueAt(1);
		y01High.setValue(line01.evaluateMean(x01High.toDouble()));
		
		ScaledVariable xVar = (ScaledVariable)data.getVariable("x");
		lowFitTemplate.setValues(intercept, slope, (NumValue)xVar.valueAt(0));
		highFitTemplate.setValues(intercept, slope, (NumValue)xVar.valueAt(1));
		
		theView.setSelectedIndex(0);
		
		data.variableChanged("line01");
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.5 : 0;
	}
	
	
//-----------------------------------------------------------
	
	private void changeAxis(int axisIndex) {
		currentYAxisIndex = axisIndex;
		
		yAxis.setAlternateLabels(axisIndex + 1);			//	zero is for 0-1 axis
		yAxis.repaint();
		
		String startYAxisInfo = getYAxisInfo()[axisIndex];
		rescaleForNewAxis(startYAxisInfo, "y");
		
		theView.repaint();
	}
	
	private boolean localAction(Object target) {
		if (target == yAxisChoice) {
			int newChoice = yAxisChoice.getSelectedIndex();
			if (newChoice != currentYAxisIndex) {
				changeAxis(newChoice);
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