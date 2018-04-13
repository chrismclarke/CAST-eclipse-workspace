package statistic2Prog;

import java.awt.*;

import dataView.*;
import qnUtils.*;
import axis.*;
import dotPlot.*;
import statistic2.SpreadCalculator;


public class CheckSDApplet extends CheckValueApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String STACK_PARAM = "stack";
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected String valueLabel() {
		return "SD  =";
	}
	
	protected NumValue evalAnswer(DataSet data, int correctDecimals) {
		SpreadCalculator sdCalc = new SpreadCalculator(SpreadCalculator.STDEV);
		double sd = sdCalc.evaluateStat(data.getNumVariable(), null);
												//	this only works for the SD. Other measures of spread require boxInfo
		
		return new NumValue(sd, correctDecimals);
	}
	
	protected double evalExactSlop(NumValue answer, DataSet data) {
		return answer.toDouble() * 0.00001;
	}
	
	protected double evalApproxSlop(NumValue answer, DataSet data) {
		return answer.toDouble() * 0.15;
	}
	
	protected String[] answerStrings(NumValue answer) {
		String[] answerString = new String[5];
		answerString[LinkedAnswerEditPanel.NONE] = "Guess the value of the standard deviation.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!!";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "You have not typed a number.";
		answerString[LinkedAnswerEditPanel.WRONG] = "Your answer is not close enough. Try the mean \u00B1 SD and mean \u00B1 2SD rules.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!! The exact value is SD = "
																														+ answer.toString() + ".";
		return answerString;
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		StackingDotPlotView theDotPlot = new StackingDotPlotView(data, this, theHorizAxis);
		thePanel.add("Center", theDotPlot);
		theDotPlot.lockBackground(Color.white);
		String stackString = getParameter(STACK_PARAM);
		if (stackString != null && stackString.equals("true"))
			theDotPlot.setFrame(StackingDotPlotView.kStackedIndex);
		
		return thePanel;
	}
}