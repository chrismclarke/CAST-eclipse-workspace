package inferenceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import inference.*;
import formula.*;



public class CheckCI2Applet extends CheckCIApplet {
	static final private Color kGreenColor = new Color(0x006600);
	
	public void setupApplet() {
		setAlwaysShowSD();
		super.setupApplet();
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dotPlotPanel(data));
		
		XPanel tTablePanel = new XPanel();
			tTablePanel.setLayout(new BorderLayout());
			XPanel subPanel = new XPanel();
			subPanel.setLayout(new BorderLayout(0, 20));
				subPanel.add("Center", new TLookupPanel(this, "Working"));
				Separator sep = new Separator(0.9);
				sep.setMinWidth(150);
				subPanel.add("South", sep);
			tTablePanel.add("South", subPanel);
		thePanel.add("East", tTablePanel);
		
		XPanel statAndWorkingPanel = new XPanel();
		statAndWorkingPanel.setLayout(new BorderLayout(10, 0));
		
		XPanel statisticPanel = summaryPanel(data);
		statisticPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
		statAndWorkingPanel.add("Center", statisticPanel);
		
		FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
		statAndWorkingPanel.add("East", new CIMeanCalcPanel(bigGreenContext));
		
		thePanel.add("South", statAndWorkingPanel);
		
		return thePanel;
	}
	
	protected String[] answerStrings(NumValue answer) {
		String[] answerString = new String[5];
		answerString[LinkedAnswerEditPanel.NONE] = "Find the 95% confidence interval for the population mean.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!!";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "You have not typed a number.";
		answerString[LinkedAnswerEditPanel.WRONG] = "Your answer is not close enough. Find the t-value with (n-1) degrees of freedom. Multiply t by s, then divide by root(n).";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!! The exact value is plus or minus "
																					+ answer.toString() + ".";
		return answerString;
	}
}