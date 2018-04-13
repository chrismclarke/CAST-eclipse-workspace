package exerciseNormalProg;

import dataView.*;
import distn.*;
import exercise2.*;

import exerciseNormal.JdistnAreaLookup.*;


public class NormalInverseApplet extends CoreNormalProbApplet {
	static final private double kEpsValue = 0.001;
	
	protected NormalLookupPanel normalLookupPanel;
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		normalLookupPanel = new NormalLookupPanel(data, "distn", this, NormalLookupPanel.HIGH_ONLY);
		registerStatusItem("drag", normalLookupPanel);
		return normalLookupPanel;
	}
	
	protected void setDisplayForQuestion() {
		normalLookupPanel.resetPanel();
		
		data.variableChanged("distn");
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		double mean = getMean().toDouble();
		double sd = getSD().toDouble();
		normalDistn.setMean(mean);
		normalDistn.setSD(sd);
		normalDistn.setMinSelection(Double.NEGATIVE_INFINITY);
		normalDistn.setMaxSelection(mean);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		NumValue percent = getPercent();
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Find the required value then type it into the text-edit box above.\n");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a value in the Answer box above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(areaAnswerString(percent));
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Correct!\n");
				messagePanel.insertText(areaAnswerString(percent));
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText(areaCloserString(percent));
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText(areaHintString(percent));
				break;
		}
	}
	
	private String areaCloserString(NumValue percent) {
		String s = "However you should be able to find the answer more accurately by typing different values"
										+ " into the red text-edit box above the normal curve until the area to the ";
		if (tailType() == GREATER_THAN)
			s += "right";
		else
			s += "left";
		return s + " is " + new NumValue(percent.toDouble() * 0.01, percent.decimals + 2) + ".";
	}
	
	private String areaAnswerString(NumValue percent) {
		NumValue quantile =  new NumValue(evaluatePercentile(percent, "distn"), getMaxValue().decimals);
		String s;
		if (tailType() == GREATER_THAN)
			s = "The area under the normal curve to the right of ";
		else
			s = "The area under the normal curve to the left of ";
		return s + quantile.toString() + " is " + new NumValue(percent.toDouble() * 0.01, percent.decimals + 2) + ".";
	}
	
	private String areaHintString(NumValue percent) {
		String s = "Find the value for which the area to the ";
		if (tailType() == GREATER_THAN)
			s += "right";
		else
			s += "left";
		return s + " is " + new NumValue(percent.toDouble() * 0.01, percent.decimals + 2);
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = getAttempt();
			
			NumValue percent = getPercent();
			double correct = evaluatePercentile(percent, "distn");
			
			if (Math.abs(correct - attempt) <= getSD().toDouble() * kEpsValue)
				return ANS_CORRECT;
			else {
				double maxCloseError = normalLookupPanel.twoPixelValue();
				
				if (Math.abs(correct - attempt) <= maxCloseError)
					return ANS_CLOSE;
				else
					return ANS_WRONG;
			}
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue percent = getPercent();
		NumValue correct = new NumValue(evaluatePercentile(percent, "distn"), getMaxValue().decimals);
		
		resultPanel.showAnswer(correct);
		
		NumValue start = new NumValue(Double.NEGATIVE_INFINITY);
		normalLookupPanel.showAnswer(start, correct);
	}
}