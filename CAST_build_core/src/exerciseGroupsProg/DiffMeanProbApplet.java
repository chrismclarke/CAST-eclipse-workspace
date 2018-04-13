package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;

import exerciseGroups.*;


public class DiffMeanProbApplet extends CoreGroupsProbApplet {
	static final private int SINGLE_VALUE = 0;
//	static final private int MEAN_VALUE = 1;
	
//---------------------------------------------------------------------
	
	private SumDiffTemplatePanel meanTemplate, sdTemplate;
	
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("singleOrMean", "choice");
		registerParameter("n1", "int");
		registerParameter("n2", "int");
	}
	
	private boolean isSingleNotMean() {
		return getIntParam("singleOrMean") == SINGLE_VALUE;
	}
	
	protected NumValue getVarMean() {		//	returns mean for difference. Used when generating cutoffs
		double mean1 = getMean1().toDouble();
		double mean2 = getMean2().toDouble();
		int decimals = getMaxMeanParam().decimals;
		return new NumValue(swapGroups ? mean2 - mean1 : mean1 - mean2, decimals);
	}
	
	protected NumValue getVarSd() {		//	returns sd for difference. Used when generating cutoffs
		double sd1 = getSD1().toDouble();
		double sd2 = getSD2().toDouble();
		int n1 = getN1();
		int n2 = getN2();
		int decimals = getMaxSdParam().decimals;
		return new NumValue(Math.sqrt(sd1 * sd1 / n1 + sd2 * sd2 / n2), decimals);
	}
	
	private int getN1() {
		if (isSingleNotMean())
			return 1;
		else
			return getIntParam("n1");
	}
	
	private int getN2() {
		if (isSingleNotMean())
			return 1;
		else
			return getIntParam("n2");
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			XPanel templatePanel = new XPanel();
			templatePanel.setLayout(new BorderLayout(0, 10));
			
				XPanel meanSdPanel = new XPanel();
				meanSdPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL));
				
					XPanel meanPanel = new XPanel();
					meanPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
						meanTemplate = new SumDiffTemplatePanel(SumDiffTemplatePanel.DIFF_OPTION, SumDiffTemplatePanel.BASIC_OPTION, getMaxMeanParam(), stdContext);
						registerStatusItem("meanTemplate", meanTemplate);
					meanPanel.add(meanTemplate);
				
				meanSdPanel.add(ProportionLayout.LEFT, meanPanel);
			
					XPanel sdPanel = new XPanel();
					sdPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						sdTemplate = new SumDiffTemplatePanel(SumDiffTemplatePanel.SUM_OPTION, SumDiffTemplatePanel.VAR_N_OPTION, getMaxSdParam(), stdContext);
						registerStatusItem("sdTemplate", sdTemplate);
					sdPanel.add(sdTemplate);
				
				meanSdPanel.add(ProportionLayout.RIGHT, sdPanel);
					
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
		
		meanTemplate.setValues(kOneValue, kZeroValue);
		sdTemplate.setValues(kOneValue, kOneValue, kOneValue, kOneValue);
		
		NumValue maxMeanVal = getMaxMeanParam();
		NumValue maxSdVal = getMaxSdParam();
		meanTemplate.changeMaxValue(maxMeanVal);
		sdTemplate.changeMaxValue(maxSdVal);
		
		resultPanel.clear();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		GroupsIntervalLimits groupLimits = getGroupLimits();
		NumValue cutoff = groupLimits.cutoff;
		String sub1 = swapGroups ? "#sub2#" : "#sub1#";
		String sub2 = swapGroups ? "#sub1#" : "#sub2#";
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("First use the templates at the top to find the mean and standard deviation of the difference.");
				messagePanel.insertText("\nThe template underneath can be used to find a z-score for the problem; type into the box above it to find the probability from the standard normal distribution.");
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
				messagePanel.insertText("The difference is approx normal with mean = #mu#" + sub1 + " - #mu#" + sub2 + " and st devn = ");
				messagePanel.insertFormula(MSumDiffSdFormula.sumDiffSdFormula(this, isSingleNotMean() ? MSumDiffSdFormula.SINGLE_VALUES : MSumDiffSdFormula.MEANS, swapGroups));
				
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
				messagePanel.insertText("Since the question asks for a probabiity about the difference");
				messagePanel.insertText(" between two quantities, you must first find its distribution. It has mean #mu#" + sub1 + " - #mu#" + sub2 + ".");
				if (isSingleNotMean()) {
					messagePanel.insertText(" Since the question is about individual values from the groups, its st devn is ");
					messagePanel.insertFormula(MSumDiffSdFormula.sumDiffSdFormula(this, MSumDiffSdFormula.SINGLE_VALUES, swapGroups));
				}
				else {
					messagePanel.insertText(" Since the question is about sample means in the two groups, its st devn is ");
					messagePanel.insertFormula(MSumDiffSdFormula.sumDiffSdFormula(this, MSumDiffSdFormula.MEANS, swapGroups));
				}

				
				messagePanel.insertText(".\nNext translate the cut-off value, x="
																					+ cutoff + ", into a z-score");
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
		
		NumValue mean1Val = swapGroups ? getMean2() : getMean1();
		NumValue sd1Val = swapGroups ? getSD2() : getSD1();
		NumValue mean2Val = swapGroups ? getMean1() : getMean2();
		NumValue sd2Val = swapGroups ? getSD1() : getSD2();
		int n1 = swapGroups ? getN2() : getN1();
		int n2 = swapGroups ? getN1() : getN2();
		
		meanTemplate.setValues(mean1Val, mean2Val);
		
		if (isSingleNotMean())
			sdTemplate.setValues(sd1Val, sd2Val);
		else
			sdTemplate.setValues(sd1Val, sd2Val, new NumValue(n1, 0), new NumValue(n2, 0));
		
		showCorrectZ();
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
}