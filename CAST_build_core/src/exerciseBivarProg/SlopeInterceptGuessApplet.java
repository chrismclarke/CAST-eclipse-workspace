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

import exerciseBivar.*;


public class SlopeInterceptGuessApplet extends ExerciseApplet {
	static final protected NumValue kZeroValue = new NumValue(0.0, 0);
	
	protected HorizAxis xAxis;
	private VertAxis yAxis;
	protected LinePointsView theView;
	private LinearEquationPanel eqnView;
	
	private XLabel yVarLabel;
	
	protected int slopeResult = ANS_UNCHECKED;
	protected int interceptResult = ANS_UNCHECKED;
	
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
		registerParameter("xVarName", "string");
		registerParameter("xAxis", "string");
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "string");							//	list for all possible axes
		registerParameter("yDecimals", "int");
		registerParameter("yPropnAtLowX", "const");						//	min and max Y-propn at xAxis.minOnAxis
		registerParameter("yPropnAtHighX", "const");					//	min and max Y-propn at xAxis.maxOnAxis
		registerParameter("interceptDecimals", "int");
		registerParameter("slopeDecimals", "int");
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
	
	private int getYDecimals() {
		return getIntParam("yDecimals");
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
	
	protected XPanel getTemplatePanel(FormulaContext stdContext) {
		return null;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new AxisLayout());
			
				xAxis = new HorizAxis(this);
			graphPanel.add("Bottom", xAxis);
			
				yAxis = new VertAxis(this);
			graphPanel.add("Left", yAxis);
			
				theView = new LinePointsView(data, this, xAxis, yAxis, "x01", "y01", "line");
				if (hasOption("showData"))
					theView.setDataKeys("xData", "yData");
				theView.lockBackground(Color.white);
			graphPanel.add("Center", theView);
		
		thePanel.add("Center", graphPanel);
		
			yVarLabel = new XLabel("", XLabel.LEFT, this);
		thePanel.add("North", yVarLabel);
		
			XPanel eqnPanel = new XPanel();
			eqnPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 6));
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				
				XPanel templatePanel = getTemplatePanel(stdContext);
				if (templatePanel != null)
					eqnPanel.add(templatePanel);
				
				eqnView = new LinearEquationPanel(stdContext);
				registerStatusItem("slopeIntercept", eqnView);
			eqnPanel.add(eqnView);
		thePanel.add("South", eqnPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		xAxis.readNumLabels(getXAxisInfo());
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		yAxis.readNumLabels(getYAxisInfo());
		yAxis.invalidate();
		yVarLabel.setText(getYVarName());
		
		theView.setShowSlope(false);
		
		eqnView.setVarNames(getYVarName(), getXVarName());
		eqnView.setValues(kZeroValue, kZeroValue);
		
	}
	
	private NumValue round(double y, int decimals) {
		for (int i=0 ; i<decimals ; i++)
			y *= 10.0;
		y = Math.rint(y);
		for (int i=0 ; i<decimals ; i++)
			y /= 10.0;
		return new NumValue(y, decimals);
	}
	
	protected void setSampleData(double xMin, double xMax, double yMin, double yMax, LinearModel lineVar,
																																									NumValue slope) {
	}
	
	protected void setDataForQuestion() {
//		String axisInfo = getYAxisInfo();
		StringTokenizer st = new StringTokenizer(getYAxisInfo());
		double yMin = Double.parseDouble(st.nextToken());
		double yMax = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(getXAxisInfo());
		double xMin = Double.parseDouble(st.nextToken());
		double xMax = Double.parseDouble(st.nextToken());
		
		int interceptDecimals = getInterceptDecimals();
		int slopeDecimals = getSlopeDecimals();
		LinearModel lineVar = (LinearModel)data.getVariable("line");
		double y0 = yMin + (yMax - yMin) * getYPropnAtLowX().toDouble();
		double y1 = yMin + (yMax - yMin) * getYPropnAtHighX().toDouble();
		
		NumValue slope = round((y1 - y0) / (xMax - xMin), slopeDecimals);
		NumValue intercept = round(y0 - slope.toDouble() * xMin, interceptDecimals);
		lineVar.setParameters(intercept, slope);
		
		ScaledVariable y01Var = (ScaledVariable)data.getVariable("y01");
		y01Var.setScale(intercept.toDouble(), slope.toDouble(), getYDecimals());
		
		setSampleData(xMin, xMax, yMin, yMax, lineVar, slope);
		
		data.variableChanged("line");
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the intercept and slope into the equation for the displayed line.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The slope is the change in " + getYVarName() + " when "
																														+ getXVarName() + " increases by 1.\n");
				messagePanel.insertText("The intercept is the value of " + getYVarName() + " when "
																															+ getXVarName() + " is 0.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have found the correct slope and intercept.");
				break;
			case ANS_CLOSE:
			case ANS_WRONG:
//				messagePanel.insertRedHeading(result == ANS_CLOSE ? "Close!" : "Not close enough!");
				messagePanel.insertBoldText("Slope: ");
				if (slopeResult == ANS_CORRECT)
					messagePanel.insertRedText("Correct.");
				else {
					messagePanel.insertText("The slope is the change in " + getYVarName() + " when " + getXVarName() + " increased by 1. ");
					messagePanel.insertText("Your value is ");
					if (slopeResult == ANS_CLOSE)
						messagePanel.insertRedText("close to the correct value.");
					else
						messagePanel.insertRedText("not close enough.");
				}
				messagePanel.insertBoldText("\nIntercept: ");
				if (interceptResult == ANS_CORRECT)
					messagePanel.insertRedText("Correct.");
				else {
					messagePanel.insertText("The intercept is the value of " + getYVarName() + " when " + getXVarName() + " is 0. ");
					messagePanel.insertText("Your value is ");
					if (interceptResult == ANS_CLOSE)
						messagePanel.insertRedText("close to the correct value.");
					else
						messagePanel.insertRedText("not close enough.");
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 140;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable xVar = new NumVariable("X");
			xVar.readValues("0 1");
		data.addVariable("x01", xVar);
		
			ScaledVariable yVar = new ScaledVariable("Y", xVar, "x01", 0.0, 1.0, 9);
		data.addVariable("y01", yVar);
			
			LinearModel lineVar = new LinearModel("CorrectLine", data, "x01", new NumValue(0, 0), new NumValue(1, 0));
		data.addVariable("line", lineVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		theView.setShowSlope(false);
		return super.noteChangedWorking();
	}
	
	private int checkOneParam(NumValue attempt, NumValue correct, int decimals) {
		double error = Math.abs(attempt.toDouble() - correct.toDouble());
		
		double exactSlop = Math.pow(0.1, decimals) * 0.5;
		double closeSlop = exactSlop * 19;
		
		return (error < exactSlop) ? ANS_CORRECT : (error < closeSlop) ? ANS_CLOSE : ANS_WRONG;
	}
	
	protected int assessAnswer() {
		NumValue slopeAttempt = eqnView.getSlope();
		NumValue interceptAttempt = eqnView.getIntercept();
		
		LinearModel line = (LinearModel)data.getVariable("line");
		NumValue slopeCorrect = line.getSlope();
		NumValue interceptCorrect = line.getIntercept();
		
		slopeResult = checkOneParam(slopeAttempt, slopeCorrect, getSlopeDecimals());
		interceptResult = checkOneParam(interceptAttempt, interceptCorrect, getInterceptDecimals());
		
		return Math.max(slopeResult, interceptResult);
	}
	
	protected void giveFeedback() {
		theView.setShowSlope(true);
	}
	
	protected void showCorrectWorking() {
		LinearModel correctLine = (LinearModel)data.getVariable("line");
		NumValue intercept = correctLine.getIntercept();
		NumValue slope = correctLine.getSlope();
		
		eqnView.setValues(intercept, slope);
		
		theView.setShowSlope(true);
	}
	
	protected double getMark() {
		assessAnswer();		//	side effect of setting slopeResult and interceptResult
		double mark = (slopeResult == ANS_CORRECT) ? 0.6 : (slopeResult == ANS_CLOSE) ? 0.4 : 0;
		mark += (interceptResult == ANS_CORRECT) ? 0.4 : (interceptResult == ANS_CLOSE) ? 0.3 : 0;
		return mark;
	}
}