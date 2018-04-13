package exerciseBivarProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import exercise2.*;

import regn.*;


public class FindPredictionApplet extends FindResidualApplet {
	private LinearEquationView eqnView;
	
	
	protected String getResultName() {
		return "Prediction =";
	}
	
	protected String getResultUnits() {
		return "";
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("xValue", "const");
	}
	
	private NumValue getXValue() {
		return getNumValueParam("xValue");
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", getScatterPanel(data));
		
			XPanel eqnPanel = new InsetPanel(0, 6, 0, 0);
			eqnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				eqnView = new LinearEquationView(data, this, "ls", "", "", null, null, null, null);
			eqnPanel.add(eqnView);
		
		thePanel.add("North", eqnPanel);
		
		return thePanel;
	}
	
	protected void doSelection(int n) {
		data.variableChanged("y");
	}
	
	protected void setDisplayForQuestion() {
		theView.setPredictionX(getXValue().toDouble());
		
		super.setDisplayForQuestion();
		
		eqnView.setExplanName(getXVarName());
		eqnView.setYName(getYVarName());
		LinearModel ls = (LinearModel)data.getVariable("ls");
		NumValue intercept = ls.getIntercept();
		NumValue slope = ls.getSlope();
		eqnView.setMinMaxParams(intercept, intercept, slope, slope);		//	does invalidate()
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
//		NumVariable xVar = (NumVariable)data.getVariable("x");
		LinearModel ls = (LinearModel)data.getVariable("ls");
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the value of the prediction.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error\n");
				messagePanel.insertText("You must type a value for the prediction.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("Using the least squares equation, the predicted value of " + getYVarName() + " is\n");
				messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
				messagePanel.insertText(ls.getIntercept().toString());
				NumValue slope = ls.getSlope();
				NumValue x = getXValue();
				if (slope.toDouble() > 0)
					messagePanel.insertText(" + " + slope);
				else
					messagePanel.insertText(" - " + new NumValue(-slope.toDouble(), slope.decimals));
				messagePanel.insertText(" x " + x);
				messagePanel.insertText(" = " + new NumValue(getCorrect(), getDecimals()));
				
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have given the correct prediction.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertRedText("The value that you have given is as close as could be expected 'by eye' from the least squares line. However you should use the equation of the line to obtain the prediction more accurately.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertRedText("Use the equation of the least squares line to find the prediction.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	protected double getCloseSlop() {
		return getSlop(7);
	}
	
	protected double getExactSlop() {
		int decimals = getDecimals();
		double slop = 0.5;
		for (int i=0 ; i<decimals ; i++)
			slop *= 0.1;
		for (int i=0 ; i<-decimals ; i++)
			slop *= 10;
		return slop;
	}
	
	protected double getCorrect() {
		LinearModel ls = (LinearModel)data.getVariable("ls");
		
		double x = getXValue().toDouble();
		double prediction = ls.evaluateMean(x);
		
		return prediction;
	}
}