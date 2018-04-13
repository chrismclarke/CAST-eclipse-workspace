package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;


public class NormalZProbApplet extends CoreNormalProbApplet {
	static final private int kZDecimals = 3;
	
	static final protected boolean DENSITY = true;
	static final protected boolean TABLE = false;
	
	static protected void insertDensityMessageContent(MessagePanel messagePanel, DataSet data,
																																CoreNormalProbApplet applet) {
		ContinDistnVariable xDistn = (ContinDistnVariable)data.getVariable("distn");
		
		IntervalLimits limits = applet.getLimits();
		IntervalLimits zLimits = limits.translateToZ(xDistn);
		
		
		if (applet.isCountQuestionType()) {
			int n = applet.getN();
			switch (applet.result) {
				case ANS_UNCHECKED:
					messagePanel.insertText("Find the expected number then type it into the text-edit box above.\n");
					break;
				case ANS_INCOMPLETE:
					messagePanel.insertRedHeading("Error!\n");
					messagePanel.insertText("You must type a value in the Answer box above.");
					break;
				case ANS_INVALID:
					messagePanel.insertRedHeading("Wrong!\n");
					messagePanel.insertText("The expected number cannot be less than zero or more than the total.");
					break;
				case ANS_TOLD:
					messagePanel.insertRedHeading("Answer\n");
					messagePanel.insertText("The template shows how to find a z-score. ");
					messagePanel.insertText("The expected value is " + n + " times the area under the standard normal curve " + zLimits.areaAnswerString() + ".");
					break;
				case ANS_CORRECT:
					messagePanel.insertRedHeading("Correct!\n");
					messagePanel.insertText("The expected value is " + n + " times the area under the standard normal curve " + zLimits.areaAnswerString() + ".");
					break;
				case ANS_CLOSE:
					messagePanel.insertRedHeading("Good!\n");
					messagePanel.insertText("However you should be able to find the answer more accurately by typing into the red text-edit boxes above the normal curves.");
					break;
				case ANS_WRONG:
					addWrongMessage(messagePanel, limits, DENSITY, applet);
					messagePanel.insertText(" Finally use the template to multiply by " + n + ".");
					break;
			}
		}
		else
			switch (applet.result) {
				case ANS_UNCHECKED:
					messagePanel.insertText("Find the probability then type it into the text-edit box above.\n");
					break;
				case ANS_INCOMPLETE:
					messagePanel.insertRedHeading("Error!\n");
					messagePanel.insertText("You must type a probability in the Answer box above.");
					break;
				case ANS_INVALID:
					messagePanel.insertRedHeading("Wrong!\n");
					messagePanel.insertText("Probabilities cannot be less than zero or more than one.");
					break;
				case ANS_TOLD:
					messagePanel.insertRedHeading("Answer\n");
					messagePanel.insertText("The template shows how to find a z-score. ");
					messagePanel.insertText("The answer is the area under the standard normal curve " + zLimits.areaAnswerString() + ".");
					break;
				case ANS_CORRECT:
					messagePanel.insertRedHeading("Correct!\n");
					messagePanel.insertText("This probability is the area under the standard normal curve " + zLimits.areaAnswerString() + ".");
					break;
				case ANS_CLOSE:
					messagePanel.insertRedHeading("Good!\n");
					messagePanel.insertText("However you should be able to find the answer correct to 4 decimal places by typing z-scores into the red text-edit boxes above the normal curves.");
					break;
				case ANS_WRONG:
					addWrongMessage(messagePanel, limits, DENSITY, applet);
					break;
			}
	}
	
	static protected void addWrongMessage(MessagePanel messagePanel, IntervalLimits limits,
																					boolean densityNotTable, CoreNormalProbApplet applet) {
		messagePanel.insertRedHeading("Not close enough!\n");
		if (limits.startVal == null)
			messagePanel.insertText("To find the probability that x < " + limits.endVal.toString() + ", translate it to a z-score with ");
		else if (limits.endVal == null)
			messagePanel.insertText("To find the probability that x > " + limits.startVal.toString() + ", translate it to a z-score with ");
		else
			messagePanel.insertText("Translate both limits, x#sub1# = " + limits.startVal.toString() + " and x#sub2# = " + limits.endVal.toString() + ", to z-scores with ");
		
		messagePanel.insertFormula(MStandardFormulae.zFormula(applet));
		messagePanel.insertText(".");
		
		if (densityNotTable) {
			if (limits.startVal == null || limits.endVal == null)
				messagePanel.insertText(" Then use z to find the area under the normal curve.");
			else
				messagePanel.insertText(" Then use the two z-scores to find the area under the normal curve.");
		}
		else {
			if (limits.startVal == null)
				messagePanel.insertText(" Then look up z in the table to find the required probability.");
			else if (limits.endVal == null)
				messagePanel.insertText(" Then look up z in the table. The probability is one minus this.");
			else {
				messagePanel.insertText(" Then look up both z-scores in the table.");
				if (limits.startVal.toDouble() < limits.endVal.toDouble())
					messagePanel.insertText(" The probability is the difference between these values.");
				else
					messagePanel.insertText(" The probability is one minus the difference between these values.");
			}
		}
	}
	
//-----------------------------------------------------------
	
	protected NormalLookupPanel zLookupPanel;
	protected ZTemplatePanel zTemplate;
	protected ExpectedTemplatePanel expectedTemplate;
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			zTemplate = new ZTemplatePanel(stdContext);
			zTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("zTemplate", zTemplate);
		thePanel.add("North", zTemplate);
		
			zLookupPanel = new NormalLookupPanel(data, "z", this, NormalLookupPanel.HIGH_AND_LOW);
			registerStatusItem("zDrag", zLookupPanel);
		thePanel.add("Center", zLookupPanel);
		
		if (hasOption("nTemplate")) {
			expectedTemplate = new ExpectedTemplatePanel(stdContext);
			expectedTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("expectedTemplate", expectedTemplate);
			thePanel.add("South", expectedTemplate);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		if (zLookupPanel !=  null)
			zLookupPanel.resetPanel();
		
		zTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		if (expectedTemplate != null)
			expectedTemplate.setValues(kOneValue, kOneValue);
		
//		data.variableChanged("z");
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		NumValue mean = getMean();
		NumValue sd = getSD();
		normalDistn.setMean(mean.toDouble());
		normalDistn.setSD(sd.toDouble());
		normalDistn.setDecimals(mean.decimals, sd.decimals);
		
		NormalDistnVariable zDistn = (NormalDistnVariable)data.getVariable("z");
		setDefaultSelection(zDistn);
	}
	
	protected void setDefaultSelection(NormalDistnVariable zDistn) {
		zDistn.setMinSelection(-1.0);
		zDistn.setMaxSelection(1.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		insertDensityMessageContent(messagePanel, data, this);
	}
	
	protected int getMessageHeight() {
		return 120;
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

	
	public NumValue getMaxValue() {
		return new NumValue(-9, kZDecimals);
	}
	
//-----------------------------------------------------------
	
	protected double maxExactError(NumValue startZ, NumValue endZ) {
		return kEps;
	}
	
	protected double maxCloseError(NumValue startZ, NumValue endZ) {
		return zLookupPanel.getPixError(startZ, endZ);
	}
	
	protected int assessAnswer() {
		double attempt = getAttempt();
		if (isCountQuestionType())
			attempt /= getN();
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if (attempt < 0.0 || attempt > 1.0)
			return ANS_INVALID;
		else {
			IntervalLimits limits = getLimits();
			double correct = evaluateProbability(limits);
			
			double mean = getMean().toDouble();
			double sd = getSD().toDouble();
			NumValue startZ = (limits.startVal == null) ? null : new NumValue((limits.startVal.toDouble() - mean) / sd);
			NumValue endZ = (limits.endVal == null) ? null : new NumValue((limits.endVal.toDouble() - mean) / sd);
			
			if (Math.abs(correct - attempt) <= maxExactError(startZ, endZ))
				return ANS_CORRECT;
			else {
				double maxCloseError = maxCloseError(startZ, endZ);
				
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
		IntervalLimits limits = getLimits();
		double prob = evaluateProbability(limits);
		NumValue probValue = new NumValue(prob, 4);
		
		if (!isCountQuestionType())
			resultPanel.showAnswer(probValue);
		else {
			int n = getN();
			int decimals = 4;
			while (n > 10) {
				n /= 10;
				decimals --;
			}
			resultPanel.showAnswer(new NumValue(prob * getN(), decimals));
		}
		
		NumValue meanVal = getMean();
		double mean = meanVal.toDouble();
		NumValue sdVal = getSD();
		double sd = sdVal.toDouble();
		
		NumValue startZ = (limits.startVal == null) ? null : new NumValue((limits.startVal.toDouble() - mean) / sd, kZDecimals);
		NumValue endZ = (limits.endVal == null) ? null : new NumValue((limits.endVal.toDouble() - mean) / sd, kZDecimals);
		
		zLookupPanel.showAnswer(startZ, endZ);
		
		if (limits.startVal == null)
			zTemplate.setValues(limits.endVal, meanVal, sdVal);
		else
			zTemplate.setValues(limits.startVal, meanVal, sdVal);
		
		if (expectedTemplate != null)
			if (!isCountQuestionType())
				expectedTemplate.setValues(kNaNValue, kNaNValue);
			else
				expectedTemplate.setValues(new NumValue(getN(), 0), probValue);
	}
}