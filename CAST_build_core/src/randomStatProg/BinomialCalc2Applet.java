package randomStatProg;

import java.awt.*;

import dataView.*;
import randomStat.*;
import qnUtils.*;
import distn.*;
import normal.*;
import formula.*;


public class BinomialCalc2Applet extends BinomialCalcApplet {
	
	protected double questionProportion() {
		return 0.4;
	}
	
	protected XPanel workingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
		thePanel.add("North", calcPanel());
		
		thePanel.add("Center", new DistnLookupPanel(DistnLookupPanel.Z_DISTN, this,
																								getBackground()));
		return thePanel;
	}
	
	private XPanel calcPanel()  {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
		thePanel.add("North", new SuccessCalcPanel(SuccessCalcPanel.MEAN, stdContext));
		thePanel.add("Center", new SuccessCalcPanel(SuccessCalcPanel.SD, stdContext));
		thePanel.add("South", new ZCalcPanel(stdContext));
		
		return thePanel;
	}
	
	protected String[] getAnswerStrings() {
//		String nString = question.getValueString(QuestionLayout.N);
//		int n = Integer.parseInt(nString);
		
//		String piString = question.getValueString(QuestionLayout.PI);
//		double pi = Double.parseDouble(piString);
		
		String xString = question.getValueString(QuestionLayout.X1_VALUE);
		int x = Integer.parseInt(xString);
		
		String adviceString;
		if (tailString.equals("LT"))
			adviceString = "where x = " + String.valueOf(x - 1) + ".5. The probability is the area below x.";
		else if (tailString.equals("LE"))
			adviceString = "where x = " + String.valueOf(x) + ".5. The probability is the area below x.";
		else if (tailString.equals("GT"))
			adviceString = "where x = " + String.valueOf(x) + ".5. The probability is the area above x. (One minus the area below x.)";
		else if (tailString.equals("GE"))
			adviceString = "where x = " + String.valueOf(x - 1) + ".5. The probability is the area above x. (One minus the area below x.)";
		else
			adviceString = "???";
		
		String solnString = "Using a normal approx, the probability is " + answer.toString();
		
		String answerString[] = new String[5];
		
		answerString[LinkedAnswerEditPanel.NONE] = "Find the probability to within "
																+ approxString + " of the correct value.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Probability is correct!  " + solnString;
		answerString[LinkedAnswerEditPanel.WRONG] = "Probability is wrong.  Find the mean = np and sd = root(np(1-p)), then z = (x - mean) / sd, " + adviceString;
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "Error! You have not typed a valid number.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Probability is close enough!  " + solnString;
		return answerString;
	}
	
	protected void setupAnswer() {
		String nString = question.getValueString(QuestionLayout.N);
		int n = Integer.parseInt(nString);
		
		String piString = question.getValueString(QuestionLayout.PI);
		double pi = Double.parseDouble(piString);
		
		String xString = question.getValueString(QuestionLayout.X1_VALUE);
		double x = Integer.parseInt(xString);
		
		if (tailString.equals("LT") || tailString.equals("GE"))
			x -= 0.5;
		else
			x += 0.5;
		
		double z = (x - n * pi) / Math.sqrt(n * pi * (1.0 - pi));
		
		double prob = 0.0;
		if (tailString.equals("LT") || tailString.equals("LE"))
			prob = NormalTable.cumulative(z);
		else
			prob = 1.0 - NormalTable.cumulative(z);
		
		answer = new NumValue(prob, 4);
	}
}