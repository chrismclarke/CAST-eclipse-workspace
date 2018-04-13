package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;


public class NormalInverseZApplet extends CoreNormalProbApplet {
	static final private double kEpsValue = 0.001;
	static final protected NumValue kMaxZValue = new NumValue(-9, 3);
	
	static final protected boolean DENSITY = true;
	static final protected boolean TABLE = false;
	
	static protected void insertMessageContent(MessagePanel messagePanel, boolean densityNotTable,
																																	CoreNormalProbApplet applet) {
		NumValue percent = applet.getPercent();
		switch (applet.result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Find the required value then type it into the text-edit box above.\n");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a value in the text-edit box above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(answerString(percent, densityNotTable, applet));
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Correct!\n");
				messagePanel.insertText(answerString(percent, densityNotTable, applet));
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText(closerString(percent, densityNotTable, applet));
				messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
				messagePanel.insertFormula(zToXFormula(applet));
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText(hintString(percent, densityNotTable, applet));
				messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
				messagePanel.insertFormula(zToXFormula(applet));
				break;
		}
	}
	
	static protected String closerString(NumValue percent, boolean densityNotTable,
																																		CoreNormalProbApplet applet) {
		String s;
		NumValue localPercent = new NumValue(percent);			//		cannot use passed parameter since its value field is changed
		if (densityNotTable) {
			s = "However you should be able to find the answer more accurately by typing different values"
										+ " into the red text-edit box above the normal curve until the area to the ";
			if (applet.tailType() == GREATER_THAN)
				s += "right is ";
			else
				s += "left is ";
		}
		else {
			s = "However you should be able to estimate the z-score a bit more accurately by estimating"
							+ " it from the adjacent table probabiities on each side of ";
			if (applet.tailType() == GREATER_THAN)
				localPercent.setValue(100 - localPercent.toDouble());
		}
		s += new NumValue(localPercent.toDouble() * 0.01, localPercent.decimals + 2) + ".\nThen translate this z-score to a "
																+ applet.getVarName() + " with the formula:\n";
		return s;
	}
	
	static protected String answerString(NumValue percent, boolean densityNotTable,
																																		CoreNormalProbApplet applet) {
		NumValue zQuantile =  new NumValue(applet.evaluatePercentile(percent, "z"), 3);
		NumValue xQuantile =  new NumValue(applet.evaluatePercentile(percent, "distn"),
																																applet.getMaxValue().decimals);
		String s;
		if (densityNotTable) {
			if (applet.tailType() == GREATER_THAN)
				s = "The area under the standard normal curve to the right of ";
			else
				s = "The area under the standard normal curve to the left of ";
			s += zQuantile.toString() + " is " + new NumValue(percent.toDouble() * 0.01, percent.decimals + 2);
		}
		else {
			if (applet.tailType() == GREATER_THAN) {
				NumValue rightProb = new NumValue(percent.toDouble() * 0.01, percent.decimals + 2);
				NumValue leftProb = new NumValue(1.0 - rightProb.toDouble(), rightProb.decimals);
				s = "The probability of a z-score less than " + zQuantile.toString() + " is " + leftProb
								+ " so the probability of a higher z-score is " + rightProb;
			}
			else
				s = "The probability of a z-score less than " + zQuantile.toString() + " is "
																		+ new NumValue(percent.toDouble() * 0.01, percent.decimals + 2);
		}
		s += ".\nThis corresponds to a " + applet.getVarName() + " of " + xQuantile + ".";
		
		return s;
	}
	
	static protected String hintString(NumValue percent, boolean densityNotTable,
																																		CoreNormalProbApplet applet) {
		String s;
		NumValue localPercent = new NumValue(percent);			//		cannot use passed parameter since its value field is changed
		if (densityNotTable) {
			s = "Find the z-score for which the area to the ";
			if (applet.tailType() == GREATER_THAN)
				s += "right is ";
			else
				s += "left is ";
		}
		else {
			s = "Find the z-score corresponding to the table entry ";
			if (applet.tailType() == GREATER_THAN)
				localPercent.setValue(100 - localPercent.toDouble());
		}
		s += new NumValue(localPercent.toDouble() * 0.01, localPercent.decimals + 2)
						+ ".\nThen translate the z-score into a " + applet.getVarName() + " with the formula:\n";
		return s;
	}
	
	static private MFormula zToXFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		
		MBinary scaledZ = new MBinary(MBinary.TIMES, new MText("#sigma#", stdContext), new MText("z", stdContext),
																																				stdContext);
		MBinary xFromZ = new MBinary(MBinary.PLUS, new MText("#mu#", stdContext), scaledZ, stdContext);
		return new MBinary(MBinary.EQUALS, new MText("x", stdContext), xFromZ, stdContext);
	}
	
//-----------------------------------------------------------
	
	protected NormalLookupPanel zLookupPanel;
	protected ZInverseTemplatePanel zInverseTemplate;
		
//-----------------------------------------------------------

	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			zLookupPanel = new NormalLookupPanel(data, "z", this, NormalLookupPanel.HIGH_ONLY);
			registerStatusItem("zDrag", zLookupPanel);
		thePanel.add("Center", zLookupPanel);
		
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			zInverseTemplate = new ZInverseTemplatePanel(getMaxValue(), stdContext);
			zInverseTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("zTemplate", zInverseTemplate);
		thePanel.add("South", zInverseTemplate);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		if (zLookupPanel != null)
			zLookupPanel.resetPanel(kMaxZValue);
		
		zInverseTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		data.variableChanged("z");
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		double mean = getMean().toDouble();
		double sd = getSD().toDouble();
		normalDistn.setMean(mean);
		normalDistn.setSD(sd);
		
		NormalDistnVariable zDistn = (NormalDistnVariable)data.getVariable("z");
		zDistn.setMinSelection(Double.NEGATIVE_INFINITY);
		zDistn.setMaxSelection(0.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		insertMessageContent(messagePanel, DENSITY, this);
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
			NormalDistnVariable zDistn = new NormalDistnVariable("Z");
			zDistn.setMean(0.0);
			zDistn.setSD(1.0);
		data.addVariable("z", zDistn);
		
		return data;
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = getAttempt();
			
			NumValue percent = getPercent();
			double correct = evaluatePercentile(percent, "distn");
			double sd = getSD().toDouble();
			
			if (Math.abs(correct - attempt) <= sd * kEpsValue)
				return ANS_CORRECT;
			else {
				double maxCloseError = zLookupPanel.twoPixelValue() * sd;
				
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
		NumValue correctZ = new NumValue(evaluatePercentile(percent, "z"), kMaxZValue.decimals);
		
		resultPanel.showAnswer(correct);
		
		NumValue start = new NumValue(Double.NEGATIVE_INFINITY);
		zLookupPanel.showAnswer(start, correctZ);
		
		zInverseTemplate.setValues(correctZ, getMean(), getSD());
	}
}