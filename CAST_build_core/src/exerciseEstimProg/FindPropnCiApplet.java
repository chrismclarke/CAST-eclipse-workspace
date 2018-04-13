package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseEstim.*;


public class FindPropnCiApplet extends FindPropn95CiApplet {
	
	private TLookupPanel zLookupPanel;
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("ciLevel", "const");
	}
	
	private NumValue getCiPercent() {
		return getNumValueParam("ciLevel");
	}
	
//-----------------------------------------------------------
	
	protected boolean fullWidthTemplateHighlight() {
		return true;
	}
	
	protected int getIntervalOrientation() {
		return CiResultPanel.VERTICAL;
	}
	
	protected int getTemplateType() {
		return PropnSeTemplatePanel.SE_PLUS_Z;
	}
	
	protected XPanel getAnswerPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
			
			XPanel lookupPanel = new InsetPanel(0, 5, 0, 0);
			lookupPanel.setLayout(new BorderLayout(0, 0));
				zLookupPanel = new TLookupPanel(this, TLookupPanel.QUANTILE_LOOKUP, TLookupPanel.NORMAL_ONLY);
			lookupPanel.add(zLookupPanel);
			lookupPanel.lockBackground(kWorkingBackground);
			
		thePanel.add("Center", lookupPanel);
			
			XPanel ansPanel = new InsetPanel(20, 20);		//	height is base height + 20 pix top & bottom
			ansPanel.setLayout(new BorderLayout(0, 0));
				
			ansPanel.add("Center", getCiPanel());
			
		thePanel.add("East", ansPanel);
		return thePanel;
	}
	
	protected int getMessageHeight() {
		return 130;
	}
	
//-----------------------------------------------------------
	
	protected void setDisplayForQuestion() {
		zLookupPanel.reset();
		super.setDisplayForQuestion();
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use normal distribution to find the z-score corresponding to the confidence level in the question.");
				messagePanel.insertText("\nUse the template to evaluate the 'plus-minus' value for this confidence interval and type it into the boxes above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("A " + getCiPercent() + "% confidence interval for the sample proportion is,\n");
				insertCiFormula(messagePanel);
				messagePanel.insertText("\n");
				messagePanel.setAlignment(MessagePanel.LEFT_ALIGN);
				messagePanel.insertText("since " + getCiPercent() + "% of the standard normal distribution is between #plusMinus#" + new NumValue(getCorrectZ(), 3) + ".");
				break;
			case ANS_INCOMPLETE:
			case ANS_INVALID:
			case ANS_CLOSE:
			case ANS_WRONG:
			case ANS_CORRECT:
				super.insertMessageContent(messagePanel);
				break;
		}
	}
	
	protected void analyseMistake(MessagePanel messagePanel) {
		String templateResultString = seTemplate.getResult().toString();
		String correctString = new NumValue(evaluatePlusMinus(), maxPropn.decimals).toString();
		
		if (templateResultString.equals(correctString)) {
			messagePanel.insertText("\nYou seem to have correctly evaluated the 'plus-minus' value in the template.");
			messagePanel.insertRedText("\nThe confidence interval should be the sample proportion, p = "
								+ new NumValue(evaluateProb(), maxPropn.decimals) + ", #plusMinus# this.");
		}
		else {
			NumValue templateZ = seTemplate.getZ();
			double slop = 0.5 * Math.pow(0.1, maxPropn.decimals);
			if (Math.abs(templateZ.toDouble() - getCorrectZ()) > slop)
				messagePanel.insertRedText("\nYou have used the wrong z-value in the template. "
						+ getCiPercent() + "% of the standard normal distribution is between #plusMinus#"
						+ new NumValue(getCorrectZ(), 3) + " and this should be used at the start of the template, not "
						+ templateZ + ".");
			else
				analyseSeTemplate(messagePanel);
		}
	}
	
	private MFormula ciFormula(FormulaContext context) {
		MFormula se = seFormula(context);
		MFormula z = new MConst(new NumValue(getCorrectZ(), 3), context);
		MFormula plusMinus = new MBinary(MBinary.TIMES, z, se, context);
		MFormula p = new MText("p", context);
		return new MBinary(MBinary.PLUS_MINUS, p, plusMinus, context);
	}
	
	private void insertCiFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		MFormula ci = ciFormula(context);
		
		messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
		messagePanel.insertFormula(ci);
	}
	
//-----------------------------------------------------------
	
	private double getCorrectZ() {
		double lowTailProb = (1 + getCiPercent().toDouble() / 100) / 2;
		double z = NormalTable.quantile(lowTailProb);
		return Math.rint(z * 1000) / 1000;	//	round to 3 decimals
	}
	
	protected double evaluatePlusMinus() {
		return getCorrectZ() * evaluateSe();
	}
	
	protected void showCorrectWorking() {
		NumValue pVal = new NumValue(evaluateProb(), maxPropn.decimals);
		NumValue qVal = new NumValue(1 - pVal.toDouble(), maxPropn.decimals);
		NumValue nVal = new NumValue(evaluateN(), 0);
		NumValue z = new NumValue(getCorrectZ(), 3);
		
		seTemplate.setValues(pVal, qVal, nVal, z);
		
		double ciLevel = getCiPercent().toDouble() / 100;
		int ciDecimals = getCiPercent().decimals + 2;
		zLookupPanel.setConfidenceLevel(new NumValue(ciLevel, ciDecimals));
		
		showResult();
	}
	
	protected int assessAnswer() {
		if (getCiPercent().toString().equals("95"))
			return super.assessAnswer();			//	allows either 2 or 1.96
		
		int validityResult = checkValidAnswer();
		if (validityResult != ANS_UNCHECKED)
			return validityResult;
		
		double plusMinus = evaluatePlusMinus();
		int lowAns = checkLimit(getLowAttempt(), -plusMinus);
		int highAns = checkLimit(getHighAttempt(), plusMinus);
		
		return Math.max(lowAns, highAns);
	}
}