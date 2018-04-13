package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;

import exerciseGroups.*;


public class SumDiffProbApplet extends CoreGroupsProbApplet {

	private SumDiffTemplatePanel sumMeanTemplate, sumSdTemplate, diffMeanTemplate, diffSdTemplate;
	
	
//-----------------------------------------------------------
		
	protected NumValue getVarMean() {		//	returns mean for sum or difference. Used when generating cutoffs
		double mean1 = getMean1().toDouble();
		double mean2 = getMean2().toDouble();
		int decimals = getMaxMeanParam().decimals;
		if (sumNotDiff)
			return new NumValue(mean1 + mean2, decimals);
		else 
			return new NumValue(mean1 - mean2, decimals);
	}
	
	protected NumValue getVarSd() {		//	returns mean for sum or difference. Used when generating cutoffs
		double sd1 = getSD1().toDouble();
		double sd2 = getSD2().toDouble();
		int decimals = getMaxSdParam().decimals;
		return new NumValue(Math.sqrt(sd1 * sd1 + sd2 * sd2), decimals);
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			XPanel templatePanel = new XPanel();
			templatePanel.setLayout(new BorderLayout(0, 10));
			
				XPanel meanSdPanel = new XPanel();
				meanSdPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL));
				
					XPanel sumPanel = new XPanel();
					sumPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
						sumMeanTemplate = new SumDiffTemplatePanel(SumDiffTemplatePanel.SUM_OPTION, SumDiffTemplatePanel.BASIC_OPTION, getMaxMeanParam(), stdContext);
						registerStatusItem("sumMeanTemplate", sumMeanTemplate);
					sumPanel.add(sumMeanTemplate);
					
						sumSdTemplate = new SumDiffTemplatePanel(SumDiffTemplatePanel.SUM_OPTION, SumDiffTemplatePanel.SIMPLE_VAR_OPTION, getMaxSdParam(), stdContext);
						registerStatusItem("sumSdTemplate", sumSdTemplate);
					sumPanel.add(sumSdTemplate);
				
				meanSdPanel.add(ProportionLayout.LEFT, sumPanel);
			
					XPanel diffPanel = new XPanel();
					diffPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						diffMeanTemplate = new SumDiffTemplatePanel(SumDiffTemplatePanel.DIFF_OPTION, SumDiffTemplatePanel.BASIC_OPTION, getMaxMeanParam(), stdContext);
						registerStatusItem("diffMeanTemplate", diffMeanTemplate);
					diffPanel.add(diffMeanTemplate);
					
						diffSdTemplate = new SumDiffTemplatePanel(SumDiffTemplatePanel.DIFF_OPTION, SumDiffTemplatePanel.SIMPLE_VAR_OPTION, getMaxSdParam(), stdContext);
						registerStatusItem("diffSdTemplate", diffSdTemplate);
					diffPanel.add(diffSdTemplate);
				
				meanSdPanel.add(ProportionLayout.RIGHT, diffPanel);
					
			templatePanel.add("North", meanSdPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 7));
			
					XPanel zPanel = new InsetPanel(0, 6);
					zPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
					
					zPanel.add(createZTemplate());
				
					zPanel.lockBackground(kTemplateBackground);
				bottomPanel.add(zPanel);
			
			templatePanel.add("Center", bottomPanel);
		
		thePanel.add("North", templatePanel);
		
		thePanel.add("Center", createZLookupPanel());
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		resetZ();
		
		sumMeanTemplate.setValues(kOneValue, kZeroValue);
		sumSdTemplate.setValues(kOneValue, kOneValue);
		diffMeanTemplate.setValues(kOneValue, kZeroValue);
		diffSdTemplate.setValues(kOneValue, kOneValue);
		
		NumValue maxMeanVal = getMaxMeanParam();
		NumValue maxSdVal = getMaxSdParam();
		sumMeanTemplate.changeMaxValue(maxMeanVal);
		sumSdTemplate.changeMaxValue(maxSdVal);
		diffMeanTemplate.changeMaxValue(maxMeanVal);
		diffSdTemplate.changeMaxValue(maxSdVal);
		
		resultPanel.clear();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		GroupsIntervalLimits groupLimits = getGroupLimits();
		NumValue cutoff = groupLimits.cutoff;
		String sub1 = swapGroups ? "#sub2#" : "#sub1#";
		String sub2 = swapGroups ? "#sub1#" : "#sub2#";
		String sumDiffSign = sumNotDiff ? "+" : "-";
		String sumDiffText = sumNotDiff ? "sum" : "difference";
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("First use the templates at the top to find the mean and standard deviation of the sum or difference of the variables.");
				messagePanel.insertText("\nThe template underneath can be used to find a z-score for the problem; type into the \"low\" and/or \"high\" boxes to find the probability from the standard normal distribution.");
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
				messagePanel.insertText("The " + sumDiffText + " is approx normal with mean = #mu#" + sub1 + " " + sumDiffSign + " #mu#" + sub2 + " and st devn = ");
				messagePanel.insertFormula(MSumDiffSdFormula.sumDiffSdFormula(this, MSumDiffSdFormula.SINGLE_VALUES, swapGroups));
				
				messagePanel.insertText(".\nThe bottom template shows how to translate the cut-off value, "
																						+ cutoff + ", into a z-score");
				messagePanel.insertText(" using the mean and st devn of this distribution.");
				if (groupLimits.questionType == WITHIN_OF_ZERO || groupLimits.questionType == OUTSIDE_OF_ZERO)
					messagePanel.insertText(" The lower cut-off can be found in the same way using -" + cutoff + ".");
				addDensityComment(messagePanel);
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
				messagePanel.insertText("Since the question asks for a probabiity about the ");
				messagePanel.insertBoldText(sumDiffText);
				messagePanel.insertText(" of two variables, you must first find the distribution of the sum. Its mean is #mu#" + sub1 + " " + sumDiffSign + " #mu#" + sub2 + " and its st devn is ");
				messagePanel.insertFormula(MSumDiffSdFormula.sumDiffSdFormula(this, MSumDiffSdFormula.SINGLE_VALUES, swapGroups));
				
				messagePanel.insertText(".\nNext translate the cut-off value, x=" + cutoff + ", into a z-score");
				messagePanel.insertText(" using ");
				messagePanel.insertFormula(MStandardFormulae.zFormula(this));
				
				messagePanel.insertText(" where #mu# and #sigma# denote the mean and st devn of the distribution you just found.");
				
				addDensityComment(messagePanel);
				
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 180;
	}
	
//-----------------------------------------------------------
	
	protected void showCorrectWorking() {
		double prob = evaluateProbability();
		NumValue probValue = new NumValue(prob, 4);
		
		resultPanel.showAnswer(probValue);
		
//		GroupsIntervalLimits groupLimits = getGroupLimits();
		
		resultPanel.showAnswer(probValue);
		
		NumValue mean1Val = getMean1();
		NumValue sd1Val = getSD1();
		NumValue mean2Val = getMean2();
		NumValue sd2Val = getSD2();
		
		showCorrectZ();
		
		if (sumNotDiff) {
			sumMeanTemplate.setValues(mean1Val, mean2Val);
			diffMeanTemplate.setValues(kZeroValue, kZeroValue);
		}
		else {
			diffMeanTemplate.setValues(mean1Val, mean2Val);
			sumMeanTemplate.setValues(kZeroValue, kZeroValue);
		}
		sumSdTemplate.setValues(sd1Val, sd2Val);
		diffSdTemplate.setValues(kZeroValue, kZeroValue);
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
}