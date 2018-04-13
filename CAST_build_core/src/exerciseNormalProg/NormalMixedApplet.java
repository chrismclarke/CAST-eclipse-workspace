package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JtableLookup.*;


public class NormalMixedApplet extends CoreNormalProbApplet {
	static final private double kEpsValue = 0.001;
	static final protected NumValue kMaxZValue = new NumValue(-9, 3);
	
	private TablePanel tableLookupPanel;
	private DiffTemplatePanel diffTemplate;
	protected ZTemplatePanel zTemplate;
	protected ZInverseTemplatePanel zInverseTemplate;
	protected ExpectedTemplatePanel expectedTemplate;
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			zTemplate = new ZTemplatePanel(stdContext);
			zTemplate.lockBackground(kTemplateBackground);
			zTemplate.setValues(kNaNValue, kNaNValue, kNaNValue);
			registerStatusItem("zTemplate", zTemplate);
		thePanel.add("North", zTemplate);
		
			tableLookupPanel = new TablePanel(data, "z", this);
			registerStatusItem("tableSelection", tableLookupPanel);
		thePanel.add("Center", tableLookupPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 10));
			
				zInverseTemplate = new ZInverseTemplatePanel(getMaxValue(), stdContext);
				zInverseTemplate.lockBackground(kTemplateBackground);
				zInverseTemplate.setValues(kNaNValue, kNaNValue, kNaNValue);
				registerStatusItem("zInverseTemplate", zInverseTemplate);
			bottomPanel.add("North", zInverseTemplate);
			
				diffTemplate = new DiffTemplatePanel(DiffTemplatePanel.BASIC, stdContext);
				diffTemplate.lockBackground(kTemplateBackground);
				diffTemplate.setValues(kNaNValue, kNaNValue);
				registerStatusItem("diffTemplate", diffTemplate);
			bottomPanel.add("Center", diffTemplate);
		
				expectedTemplate = new ExpectedTemplatePanel(stdContext);
				expectedTemplate.lockBackground(kTemplateBackground);
				expectedTemplate.setValues(kNaNValue, kNaNValue);
				registerStatusItem("expectedTemplate", expectedTemplate);
			bottomPanel.add("South", expectedTemplate);
		
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		if (tailType() == OUTSIDE)
			throw new RuntimeException("Error: NormalMixedApplet cannot use intervals outside two limits");
		
		tableLookupPanel.scrollToSelection();
		
		zTemplate.setValues(kNaNValue, kNaNValue, kNaNValue);
		
		zInverseTemplate.setValues(kNaNValue, kNaNValue, kNaNValue);
		
		diffTemplate.setValues(kNaNValue, kNaNValue);
		
		expectedTemplate.setValues(kNaNValue, kNaNValue);
		
		data.variableChanged("z");
		
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
		zDistn.setMinSelection(0.0);
		zDistn.setMaxSelection(0.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		if (result == ANS_UNCHECKED) {
			messagePanel.insertText("Find the number that answers the question then type it into the 'Answer' text-edit box above.\n");
			return;
		}
		
		if (getQuestionType() == 0 || getQuestionType() == 1)
			NormalTableProbApplet.insertTableMessageContent(messagePanel, data, this);
		else
			NormalInverseZApplet.insertMessageContent(messagePanel, NormalInverseZApplet.TABLE, this);
	}
	
/*
	private String areaCloserString(NumValue percent) {
		String s = "However you should be able to estimate the z-score a bit more accurately by estimating"
							+ "it from the adjacent table probabiities on each side of ";
		s += new NumValue(percent.toDouble() * 0.01, percent.decimals + 2) + ".\nThen translate this z-score to a "
																+ getVarName() + " with the formula:\n";
		return s;
	}
	
	private String areaAnswerString(NumValue percent) {
		NumValue zQuantile =  new NumValue(evaluatePercentile(percent, "z"), 3);
		NumValue xQuantile =  new NumValue(evaluatePercentile(percent, "distn"), getMaxValue().decimals);
		String s;
		if (tailType() == GREATER_THAN)
			s = "The probability of a z-score greater than ";
		else
			s = "The probability of a z-score less than ";
		s += zQuantile.toString() + " is " + new NumValue(percent.toDouble() * 0.01, percent.decimals + 2)
												+ ".\nThis corresponds to a " + getVarName() + " of " + xQuantile + ".";
		
		return s;
	}
	
	private String areaHintString(NumValue percent) {
		String s = "Find the z-score corresponding to the table entry ";
		s += new NumValue(percent.toDouble() * 0.01, percent.decimals + 2)
						+ ".\nThen translate the z-score into a " + getVarName() + " with the formula:\n";
		return s;
	}
*/
	
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
		return kMaxZValue;
	}
	
//-----------------------------------------------------------
	
	protected double maxExactError(NumValue startZ, NumValue endZ) {
		return tableLookupPanel.getRoundingError(startZ, endZ, 0.002);
	}
	
	protected double maxCloseError(NumValue startZ, NumValue endZ) {
		return tableLookupPanel.getRoundingError(startZ, endZ, 0.01);
	}
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = getAttempt();
			
			if (getQuestionType() == 0 || getQuestionType() == 1) {
				IntervalLimits limits = getLimits();
				if (isCountQuestionType())
					attempt /= getN();
				
				if (attempt < 0.0 || attempt > 1.0)
					return ANS_INVALID;
				else {
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
							return hasOption("interpolate") ? ANS_CLOSE : ANS_CORRECT;
						else
							return ANS_WRONG;
					}
				}
			}
			else {	//	inverse problem
				NumValue percent = getPercent();
				double correct = evaluatePercentile(percent, "distn");
				double sd = getSD().toDouble();
				
				if (Math.abs(correct - attempt) <= sd * kEpsValue)
					return ANS_CORRECT;
				else {
					double maxCloseError = tableLookupPanel.inverseError(percent.toDouble() * 0.01) * sd;
					
					if (Math.abs(correct - attempt) <= maxCloseError)
						return hasOption("interpolate") ? ANS_CLOSE : ANS_CORRECT;
					else
						return ANS_WRONG;
				}
			}
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		if (getQuestionType() == 0 || getQuestionType() == 1) {
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
		
			ContinDistnVariable distn = (ContinDistnVariable)data.getVariable("distn");
			IntervalLimits zLimits = limits.translateToZ(distn);
			
			NumValue meanVal = distn.getMean();
			NumValue sdVal = distn.getSD();
			
			tableLookupPanel.showAnswer(zLimits.startVal, zLimits.endVal);
			
			if (limits.startVal == null)
				zTemplate.setValues(limits.endVal, meanVal, sdVal);
			else
				zTemplate.setValues(limits.startVal, meanVal, sdVal);
			
			NumValue startCum = (limits.startVal == null) ? null
											: new NumValue(distn.getCumulativeProb(limits.startVal.toDouble()), 4);
			NumValue endCum = (limits.endVal == null) ? null
											: new NumValue(distn.getCumulativeProb(limits.endVal.toDouble()), 4);
			if (startCum == null)
				diffTemplate.setValues(kNaNValue, kNaNValue);
			else if (endCum == null)
				diffTemplate.setValues(kOneValue, startCum);
			else if (startCum.toDouble() < endCum.toDouble())
				diffTemplate.setValues(endCum, startCum);
//			else
//				diffTemplate.setValues(kOneValue, startCum, endCum);
			
			if (!isCountQuestionType())
				expectedTemplate.setValues(kNaNValue, kNaNValue);
			else
				expectedTemplate.setValues(new NumValue(getN(), 0), probValue);
			
			zInverseTemplate.setValues(kNaNValue, kNaNValue, kNaNValue);
		}
		else {			//	inverse problem
			NumValue percent = getPercent();
			NumValue correct = new NumValue(evaluatePercentile(percent, "distn"), getMaxValue().decimals);
			NumValue correctZ = new NumValue(evaluatePercentile(percent, "z"), kMaxZValue.decimals);
			
			resultPanel.showAnswer(correct);
			
			NumValue start = new NumValue(Double.NEGATIVE_INFINITY);
			tableLookupPanel.showAnswer(start, correctZ);
			
			zInverseTemplate.setValues(correctZ, getMean(), getSD());
			
			zTemplate.setValues(kNaNValue, kNaNValue, kNaNValue);
			expectedTemplate.setValues(kNaNValue, kNaNValue);
			diffTemplate.setValues(kNaNValue, kNaNValue);
		}
	}
	
	protected double getMark() {
		int markType = assessAnswer();
		double mark;
		if (markType == ANS_CORRECT)
			mark = 1;
		else if (markType == ANS_CLOSE) {
			if (getQuestionType() == 0 || getQuestionType() == 1)
				mark = 0.8;
			else
				mark = 0.9;
		}
		else
			mark = 0;
		return mark;
	}
}