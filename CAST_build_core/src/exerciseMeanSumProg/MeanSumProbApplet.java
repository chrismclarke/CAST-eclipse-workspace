package exerciseMeanSumProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;
import formula.*;


import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;
import exerciseNormalProg.*;
import exerciseMeanSum.*;


public class MeanSumProbApplet extends CoreNormalProbApplet {
	static final private int MEAN_VALUE = 0;
	static final private int SUM_VALUE = 1;
	static final private int SINGLE_VALUE = 2;
	
	static final private int kZDecimals = 3;
	
	static final private Color kLabelColor = new Color(0x990000);
	
//---------------------------------------------------------------------
	
	private NormalLookupPanel zLookupPanel;
	private ZTemplatePanel z1Template, z2Template;
	private MeanSumTemplatePanel sumMeanTemplate, meanSdTemplate, sumSdTemplate;
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("maxParam", "const");
		registerParameter("sumMeanType", "choice");
	}
	
	private int getMeanSumType() {
		return getIntParam("sumMeanType");
//		return questionExtraVersion;
	}

	
	public NumValue getMaxValue() {
		return new NumValue(-9, kZDecimals);
	}
	
	public NumValue getMaxParam() {
		return getNumValueParam("maxParam");
	}
	
	protected NumValue getMean() {
		int sumMeanType = getMeanSumType();
		
		NumValue popnMean = super.getMean();
		if (sumMeanType == SUM_VALUE) {
			int n = getN();
			return new NumValue(popnMean.toDouble() * n, popnMean.decimals);
		}
		else
			return popnMean;
	}
	
	protected NumValue getSD() {
		int sumMeanType = getMeanSumType();
		
		NumValue popnSd = super.getSD();
		
		if (sumMeanType == SUM_VALUE) {
			int n = getN();
			double rootN = Math.sqrt(n);
			double sd = popnSd.toDouble() * rootN;
			int decimals = getBaseDecimals(popnSd);
			
			while (n > 100) {
				decimals --;
				n /= 100;
			}
			return new NumValue(sd, decimals);
		}
		else if (sumMeanType == MEAN_VALUE) {
			int n = getN();
			double rootN = Math.sqrt(n);
			double sd = popnSd.toDouble() / rootN;
			int decimals = getBaseDecimals(popnSd);
			while (n > 100) {
				decimals ++;
				n /= 100;
			}
			return new NumValue(sd, decimals);
		}
		else
			return popnSd;
	}
	
	private int getBaseDecimals(NumValue popnSd) {
		int decimals = popnSd.decimals + 1;
		
		double temp = popnSd.toDouble() / 100;
		while (temp < 1) {
			decimals ++;
			temp *= 10;
		}
		return decimals;
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			XPanel templatePanel = new XPanel();
			templatePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
			
				XPanel parameterPanel = new XPanel();
				parameterPanel.setLayout(new BorderLayout(0, 10));
				
					XPanel meanPanel = new XPanel();
					meanPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						XLabel meanLabel = new XLabel(translate("Mean"), XLabel.LEFT, this);
						meanLabel.setFont(getBigBoldFont());
						meanLabel.setForeground(kLabelColor);
					meanPanel.add(meanLabel);
					
						FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
						sumMeanTemplate = new MeanSumTemplatePanel(MeanSumTemplatePanel.SUM_MEAN, getMaxParam(), stdContext);
						registerStatusItem("sumMeanTemplate", sumMeanTemplate);
					meanPanel.add(sumMeanTemplate);
					
				parameterPanel.add("North", meanPanel);
				
					XPanel sdPanel = new XPanel();
					sdPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						XLabel sdLabel = new XLabel(translate("St devn"), XLabel.LEFT, this);
						sdLabel.setFont(getBigBoldFont());
						sdLabel.setForeground(kLabelColor);
					sdPanel.add(sdLabel);
					
						sumSdTemplate = new MeanSumTemplatePanel(MeanSumTemplatePanel.SUM_SD, getMaxParam(), stdContext);
						registerStatusItem("sumSdTemplate", sumSdTemplate);
					sdPanel.add(sumSdTemplate);
					
						meanSdTemplate = new MeanSumTemplatePanel(MeanSumTemplatePanel.MEAN_SD, getMaxParam(), stdContext);
						registerStatusItem("meanSdTemplate", meanSdTemplate);
					sdPanel.add(meanSdTemplate);
					
				parameterPanel.add("Center", sdPanel);
			
			templatePanel.add(ProportionLayout.LEFT, parameterPanel);
			
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 7));
			
					XPanel zPanel = new InsetPanel(0, 6);
					zPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
							
						FormulaContext templateContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
						z1Template = new ZTemplatePanel(MText.expandText("z#sub1# ="), 4, templateContext);
						registerStatusItem("z1Template", z1Template);
					zPanel.add(z1Template);
							
						z2Template = new ZTemplatePanel(MText.expandText("z#sub2# ="), 4, templateContext);
						registerStatusItem("z2Template", z2Template);
					zPanel.add(z2Template);
				
					zPanel.lockBackground(kTemplateBackground);
				rightPanel.add(zPanel);
			
			templatePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		thePanel.add("North", templatePanel);
		
			zLookupPanel = new NormalLookupPanel(data, "z", this, NormalLookupPanel.HIGH_AND_LOW,
																																		NormalLookupPanel.SINGLE_DENSITY);
			registerStatusItem("zLookup", zLookupPanel);
		thePanel.add("Center", zLookupPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		zLookupPanel.resetPanel();
		
		sumMeanTemplate.setValues(kOneValue, kOneValue, kOneValue);
		sumSdTemplate.setValues(kOneValue, kOneValue, kOneValue);
		meanSdTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		NumValue maxVal = getMaxParam();
		sumMeanTemplate.changeMaxValue(maxVal);
		sumSdTemplate.changeMaxValue(maxVal);
		meanSdTemplate.changeMaxValue(maxVal);
		
		z1Template.setValues(kOneValue, kOneValue, kOneValue);
		z2Template.setValues(kOneValue, kOneValue, kOneValue);
		
		data.variableChanged("z");
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NumValue mean = getMean();
		NumValue sd = getSD();
		
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		normalDistn.setMean(mean.toDouble());
		normalDistn.setSD(sd.toDouble());
		normalDistn.setDecimals(mean.decimals, sd.decimals);
		
		NormalDistnVariable zDistn = (NormalDistnVariable)data.getVariable("z");
		zDistn.setMinSelection(-1.0);
		zDistn.setMaxSelection(1.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int sumMeanType = getMeanSumType();
//		ContinDistnVariable yDistn = (ContinDistnVariable)data.getVariable("distn");
		
		IntervalLimits limits = getLimits();
		NumValue lowLimit = limits.startVal;
		NumValue highLimit = limits.endVal;
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Find the probability.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a probability into the Answer box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Probabilities cannot be less than zero or more than one.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				switch (sumMeanType) {
					case MEAN_VALUE:
						messagePanel.insertText("The sample mean is approx normal with mean = #mu# and st devn = ");
						messagePanel.insertFormula(MStandardFormulae.sdMeanFormula(this));
						break;
					case SUM_VALUE:
						messagePanel.insertText("The sum of the n sample values is approx normal with mean = n#mu# and st devn = ");
						messagePanel.insertFormula(MStandardFormulae.sdSumFormula(this));
						break;
					case SINGLE_VALUE:
						messagePanel.insertText("The distribution of a single sampled value is the same as that of the population, so it has mean #mu# and st devn #sigma#");
						break;
				}
				if (lowLimit != null && highLimit != null)
					messagePanel.insertText(".\nThe templates on the right show how to translate the cut-off values, "
																					+ lowLimit + " and " + highLimit + ", into z-scores");
				else
					messagePanel.insertText(".\nOne template on the right shows how to translate the cut-off value, "
																					+ (lowLimit == null ? highLimit : lowLimit) + ", into a z-score");
				messagePanel.insertText(" using the mean and st devn of this distribution.");
				if (lowLimit != null && highLimit != null)
					messagePanel.insertText("\nThe probabiity is the area under the normal density between these z-scores.");
				else if (lowLimit == null)
					messagePanel.insertText("\nThe probabiity is the area under the normal density to the left of this z-score.");
				else
					messagePanel.insertText("\nThe probabiity is the area under the normal density to the right of this z-score.");
				break;
				
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have found the correct probability.");
				break;
				
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("However you should be able to find the answer correct to 4 decimal places by typing into the red text-edit boxes above the normal curve.");
				break;
				
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				switch (sumMeanType) {
					case MEAN_VALUE:
						messagePanel.insertText("Since the question asks for a probabiity about a sample ");
						messagePanel.insertBoldText("mean");
						messagePanel.insertText(", you must first find the distribution of the sample mean. It also has mean, #mu#, but its st devn is ");
						messagePanel.insertFormula(MStandardFormulae.sdMeanFormula(this));
						break;
					case SUM_VALUE:
						messagePanel.insertText("Since the question asks for a probabiity about the ");
						messagePanel.insertBoldText("sum");
						messagePanel.insertText(" of sample values, you must first find the distribution of the sample sum. Its mean is n#mu# and st devn is ");
						messagePanel.insertFormula(MStandardFormulae.sdSumFormula(this));
						break;
					case SINGLE_VALUE:
						messagePanel.insertText("The question asks for a probability about a ");
						messagePanel.insertBoldText("single value");
						messagePanel.insertText(" and its distribution is the population distribution with mean #mu# and st devn #sigma#");
						break;
				}
				if (lowLimit != null && highLimit != null)
					messagePanel.insertText(".\nNext translate the two cut-off values, "
																					+ lowLimit + " and " + highLimit + ", into z-scores");
				else
					messagePanel.insertText(".\nNext translate the cut-off value, "
																					+ (lowLimit == null ? highLimit : lowLimit) + ", into a z-score");
				messagePanel.insertText(" using ");
				messagePanel.insertFormula(MStandardFormulae.zFormula(this));
				switch (sumMeanType) {
					case MEAN_VALUE:
						messagePanel.insertText(" where #mu# and #sigma# now denote the mean and st devn of the ");
						messagePanel.insertBoldText("sample mean's");
						messagePanel.insertText(" distribution.");
						break;
					case SUM_VALUE:
						messagePanel.insertText(" where #mu# and #sigma# now denote the mean and st devn of the ");
						messagePanel.insertBoldText("sum of the sample values");
						messagePanel.insertText(".");
						break;
					case SINGLE_VALUE:
						messagePanel.insertText(".");
						break;
				}
				if (lowLimit != null && highLimit != null)
					messagePanel.insertText("\nThe probabiity is the area under the normal density between these z-scores.");
				else if (lowLimit == null)
					messagePanel.insertText("\nThe probabiity is the area under the normal density to the left of this z-score.");
				else
					messagePanel.insertText("\nThe probabiity is the area under the normal density to the right of this z-score.");
				
				break;
		}
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
	
	protected double maxExactError(NumValue startZ, NumValue endZ) {
		return kEps;
	}
	
	protected double maxCloseError(NumValue startZ, NumValue endZ) {
		return zLookupPanel.getPixError(startZ, endZ);
	}
	
	protected int assessAnswer() {
		double attempt = getAttempt();
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if(attempt < 0.0 || attempt > 1.0)
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
				
				return (Math.abs(correct - attempt) <= maxCloseError) ? ANS_CLOSE : ANS_WRONG;
			}
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		IntervalLimits limits = getLimits();
		double prob = evaluateProbability(limits);
		NumValue probValue = new NumValue(prob, 4);
		
		resultPanel.showAnswer(probValue);
		
		NumValue meanVal = getMean();
		double mean = meanVal.toDouble();
		NumValue sdVal = getSD();
		double sd = sdVal.toDouble();
		
		NumValue startZ = (limits.startVal == null) ? null : new NumValue((limits.startVal.toDouble() - mean) / sd, kZDecimals);
		NumValue endZ = (limits.endVal == null) ? null : new NumValue((limits.endVal.toDouble() - mean) / sd, kZDecimals);
		
		zLookupPanel.showAnswer(startZ, endZ);
		
		if (limits.startVal == null)
			z1Template.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
		else
			z1Template.setValues(limits.startVal, meanVal, sdVal);
		
		if (limits.endVal == null)
			z2Template.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
		else
			z2Template.setValues(limits.endVal, meanVal, sdVal);
		
		NumValue popnMeanVal = super.getMean();
		NumValue popnSdVal = super.getSD();
		int sumMeanType = getMeanSumType();
		if (sumMeanType == SUM_VALUE) {
			NumValue nVal = new NumValue(getN(), 0);
			sumMeanTemplate.setValues(nVal, popnMeanVal, popnSdVal);
			meanSdTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
			sumSdTemplate.setValues(nVal, popnMeanVal, popnSdVal);
		}
		else if (sumMeanType == MEAN_VALUE) {
			NumValue nVal = new NumValue(getN(), 0);
			sumMeanTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
			meanSdTemplate.setValues(nVal, popnMeanVal, popnSdVal);
			sumSdTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
		}
		else {
			sumMeanTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
			meanSdTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
			sumSdTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
		}
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
}