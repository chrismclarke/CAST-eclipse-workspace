package exerciseNormalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;


import exerciseNormal.*;


public class NormalApproxApplet extends ExerciseApplet {
		static final private NumValue kOne = new NumValue(1, 0);
	static final private NumValue kHalf = new NumValue(0.5, 1);
	
	static final private double kMeanSlopPropn = 0.001;
	static final private double kSdSlopPropn = 0.001;
	
	private BinomTemplatePanel xMeanTemplate, xSdTemplate, pSdTemplate;
	
	private XLabel approxDistnLabel;
	private ResultValuePanel meanResultPanel, sdResultPanel;
	
	private int meanResult, sdResult;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 8));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add(questionPanel);
			
			topPanel.add(getWorkingPanels(null));
				
			topPanel.add(getResultPanel());
				
			topPanel.add(createMarkingPanel(NO_HINTS));
				
		add("North", topPanel);
		
			XPanel messagePanel = new XPanel();
			messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
				
				message = new ExerciseMessagePanel(this);
			messagePanel.add(message);
		
		add("Center", messagePanel);
		
		repaint();
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "choice");					//	always registered
		registerParameter("successes", "string");
		registerParameter("failures", "string");
		registerParameter("trials", "string");
		registerParameter("nTrials", "int");
		registerParameter("pSuccess", "const");
		registerParameter("succ-fail", "string");
		registerParameter("propn-count", "string");
		registerParameter("maxTemplateValues", "string");
	}
	
	private String getSuccessesName() {
		return getStringParam("successes");
	}
	
	private String getFailuresName() {
		return getStringParam("failures");
	}
	
/*
	private String getTrialsName() {
		return getStringParam("trials");
	}
*/
	
	private int getNTrials() {
		return getIntParam("nTrials");
	}
	
	private NumValue getPSuccess() {
		NumValue p = getNumValueParam("pSuccess");
		if (isSuccNotFail())
			return p;
		else
			return new NumValue(1 - p.toDouble(), p.decimals);
	}
	
	private boolean isSuccNotFail() {
		String succFailName = getStringParam("succ-fail");
		return succFailName.equals(getSuccessesName());
	}
	
	private boolean isPropnNotCount() {
		String succFailName = getStringParam("propn-count");
		return succFailName.equals("proportion");
	}
	
	private NumValue[] getMaxTemplateValues() {
		NumValue[] result = new NumValue[3];
		StringTokenizer st = new StringTokenizer(getStringParam("maxTemplateValues"), " ");
		for (int i=0 ; i<3 ; i++)
			result[i] = new NumValue(st.nextToken());
		return result;
	}
	
//-----------------------------------------------------------

	private XPanel getResultPanel() {
		XPanel thePanel = new InsetPanel(8, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel resultPanel = new InsetPanel(20, 6);
			resultPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 3));
	
				approxDistnLabel = new XLabel("", XLabel.CENTER, this);
				approxDistnLabel.setFont(getBigFont());
			resultPanel.add(approxDistnLabel);
			
				XPanel valuePanel = new XPanel();
				valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
				
					meanResultPanel = new ResultValuePanel(this, MText.expandText("#mu# ="), 6);
					meanResultPanel.setFont(getBigFont());
					registerStatusItem("mean", meanResultPanel);
				valuePanel.add(meanResultPanel);
				
					sdResultPanel = new ResultValuePanel(this, MText.expandText("and #sigma# ="), 6);
					sdResultPanel.setFont(getBigFont());
					registerStatusItem("sd", sdResultPanel);
				valuePanel.add(sdResultPanel);
				
			resultPanel.add(valuePanel);
		
			resultPanel.lockBackground(kWorkingBackground);
		thePanel.add(resultPanel);
		
		return thePanel;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 6);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 10));
		
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
			xMeanTemplate = new BinomTemplatePanel(BinomTemplatePanel.X_MEAN, kOne, stdContext);
			registerStatusItem("xMeanTemplate", xMeanTemplate);
		thePanel.add(xMeanTemplate);
		
			xSdTemplate = new BinomTemplatePanel(BinomTemplatePanel.X_SD, kOne, stdContext);
			registerStatusItem("xSdTemplate", xSdTemplate);
		thePanel.add(xSdTemplate);
		
			pSdTemplate = new BinomTemplatePanel(BinomTemplatePanel.P_SD, kOne, stdContext);
			registerStatusItem("pSdTemplate", pSdTemplate);
		thePanel.add(pSdTemplate);
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		NumValue[] maxTemplateValues = getMaxTemplateValues();
		
		xMeanTemplate.setValues(kOne, kHalf, kHalf);
		xMeanTemplate.changeMaxValue(maxTemplateValues[0]);
		
		xSdTemplate.setValues(kOne, kHalf, kHalf);
		xSdTemplate.changeMaxValue(maxTemplateValues[1]);
		
		pSdTemplate.setValues(kOne, kHalf, kHalf);
		pSdTemplate.changeMaxValue(maxTemplateValues[2]);
		
		approxDistnLabel.setText("The " + (isPropnNotCount() ? "propn" : "number") + " of "
												+ (isSuccNotFail() ? getSuccessesName() : getFailuresName()) + " is approx normal with");
		approxDistnLabel.invalidate();
		approxDistnLabel.repaint();
		
		meanResultPanel.clear();
		
		sdResultPanel.clear();
	}
	
	protected DataSet getData() {
		return null;
	}
	
	protected void setDataForQuestion() {
	}
	
//-----------------------------------------------------------

	
	protected void insertMessageContent(MessagePanel messagePanel) {
		boolean succNotFail = isSuccNotFail();
		boolean propnNotCount = isPropnNotCount();
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the parameters of the best normal approximation into the two text-edit boxes.");
				messagePanel.insertText("\n(One or two of the templates above may help with the calculations.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!");
				messagePanel.insertText("\nYou must type values for the mean and standard deviation into the boxes above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Error!");
				if (sdResult == ANS_INVALID)
					messagePanel.insertText("\nThe standard deviation cannot be negative.");
				if (meanResult == ANS_INVALID) {
					if (isPropnNotCount())
						messagePanel.insertText("\nThe mean proportion cannot be less than zero or more than one.");
					else
						messagePanel.insertText("\nThe mean count cannot be less than zero or more than " + getNTrials() + ".");
				}
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				String succName = succNotFail ? getSuccessesName() : getFailuresName();
				if (propnNotCount)
					messagePanel.insertText("Since we are interested in the proportion of " + succName + ", not the number, the mean is #mu# = " + (succNotFail ? "#pi#" : "(1 - #pi#)") + ".");
				else
					messagePanel.insertText("Since we are interested in the number of " + succName + ", not the proportion, the mean is #mu# = n" + (succNotFail ? "#pi#" : "(1 - #pi#)") + ".");
				
				messagePanel.insertText("\nThe standard deviation of this " + (propnNotCount ? "proportion" : "number") + " is ");
				if (propnNotCount)
					messagePanel.insertFormula(MStandardFormulae.pSdFormula(this));
				else
					messagePanel.insertFormula(MStandardFormulae.xSdFormula(this));
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your mean and standard deviation are close enough to the correct values.");
				break;
			case ANS_CLOSE:
			case ANS_WRONG:
				messagePanel.insertRedHeading(result == ANS_CLOSE ? "Close!" : "Not close enough!");
				messagePanel.insertBoldText("\nMean, #mu#:  ");
				if (meanResult == ANS_CORRECT)
					messagePanel.insertText("Close enough to the correct mean.");
				else {
					double attemptPropn = getAttemptMean();
					if (!propnNotCount)
						attemptPropn /= getNTrials();
					int failMeanResult = assessAttemptPropn(1 - attemptPropn);
					if (failMeanResult == ANS_CORRECT) {
						messagePanel.insertText("You have given the mean " + (propnNotCount ? "proportion" : "number") + " of '");
						String sName = succNotFail ? getSuccessesName() : getFailuresName();
						String fName = succNotFail ? getFailuresName() : getSuccessesName();
						messagePanel.insertText(fName + "' not '" + sName + "'.");
					}
					else if (meanResult == ANS_CLOSE)
						messagePanel.insertText("Close to the correct mean but try to get another digit correct.");
					else if (meanResult == ANS_WRONG)
						messagePanel.insertText("The value that you have given is wrong.");
				}
				
				messagePanel.insertBoldText("\nSt devn, #sigma#:  ");
				if (sdResult == ANS_CORRECT)
					messagePanel.insertText("Close enough to the correct st devn.");
				else if (sdResult == ANS_CLOSE)
					messagePanel.insertText("Close to the correct st devn but try to get another digit correct.");
				else if (sdResult == ANS_WRONG)
					messagePanel.insertText("The value that you have given is wrong.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	private double getCorrectMean() {
		double p = getPSuccess().toDouble();
		if (isPropnNotCount())
			return p;
		else
			return p * getNTrials();
	}
	
	private double getCorrectSd() {
		double p = getPSuccess().toDouble();
		int n = getNTrials();
		if (isPropnNotCount())
			return Math.sqrt(p * (1 - p) / n);
		else
			return Math.sqrt(p * (1 - p) * n);
	}
	
	private double getAttemptMean() {
		return meanResultPanel.getAttempt().toDouble();
	}
	
	private double getAttemptSd() {
		return sdResultPanel.getAttempt().toDouble();
	}
	
/*
	private double getMeanSlop(double correctMean) {
		if (isPropnNotCount())
			return kMeanSlopPropn * Math.min(correctMean, 1 - correctMean);
		else
			return kMeanSlopPropn * Math.min(correctMean, getNTrials() - correctMean);
	}
*/
	
	private int assessAttemptPropn(double attemptPropn) {
		double correctPropn = getCorrectMean();
		if (!isPropnNotCount())
			correctPropn /= getNTrials();
		double propnSlop = kMeanSlopPropn * Math.min(correctPropn, 1 - correctPropn);
		if (Math.abs(correctPropn - attemptPropn) < propnSlop)
			return ANS_CORRECT;
		else if (Math.abs(correctPropn - attemptPropn) < propnSlop * 10)
			return ANS_CLOSE;
		else
			return ANS_WRONG;
	}
	
	protected int assessAnswer() {			//	side effect is to set sdResult and meanResult
																			//	OK for use by getMark() because they are not used in message unless overall result changes from ANS_UNCHECKED
		if (meanResultPanel.isClear() || sdResultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attemptPropn = getAttemptMean();
			if (!isPropnNotCount())
				attemptPropn /= getNTrials();
			if (attemptPropn < 0 || attemptPropn > 1)
				meanResult = ANS_INVALID;
			else
				meanResult = assessAttemptPropn(attemptPropn);
			
			double attemptSd = getAttemptSd();
			if (attemptSd < 0)
				sdResult = ANS_INVALID;
			else {
				double correctSd = getCorrectSd();
				double sdSlop = kSdSlopPropn * correctSd;
				if (Math.abs(correctSd - attemptSd) < sdSlop)
					sdResult = ANS_CORRECT;
				else if (Math.abs(correctSd - attemptSd) < sdSlop * 10)
					sdResult = ANS_CLOSE;
				else
					sdResult = ANS_WRONG;				
			}
			if (meanResult == ANS_INVALID || sdResult == ANS_INVALID)
				return ANS_INVALID;
			else
				return Math.max(meanResult, sdResult);
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue n = new NumValue(getNTrials(), 0);
		NumValue pSuccess = getPSuccess();
		NumValue pFailure = new NumValue(pSuccess);
			pFailure.setValue(1 - pSuccess.toDouble());
		
		if (isPropnNotCount()) {
			xMeanTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
			xSdTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
			pSdTemplate.setValues(n, pSuccess, pFailure);
			
			meanResultPanel.showAnswer(pSuccess);
			sdResultPanel.showAnswer(pSdTemplate.getResult());
		}
		else {
			xMeanTemplate.setValues(n, pSuccess, pFailure);
			xSdTemplate.setValues(n, pSuccess, pFailure);
			pSdTemplate.setValues(NumValue.NAN_VALUE, NumValue.NAN_VALUE, NumValue.NAN_VALUE);
			
			meanResultPanel.showAnswer(xMeanTemplate.getResult());
			sdResultPanel.showAnswer(xSdTemplate.getResult());
		}
	}
	
	protected double getMark() {
		assessAnswer();			//	side effect sets sdResult and meanResult
		double meanMark = (meanResult == ANS_CORRECT) ? 0.5 : (meanResult == ANS_CLOSE) ? 0.35 : 0;
		double sdMark = (sdResult == ANS_CORRECT) ? 0.5 : (sdResult == ANS_CLOSE) ? 0.35 : 0;
		return meanMark + sdMark;
	}
	
}