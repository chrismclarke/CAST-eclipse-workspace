package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;


import exerciseNumGraph.*;


abstract public class CoreFindPropnApplet extends ExerciseApplet {
	static final private int EQUALS_CHOICE = 0;
	static final private int NOT_EQUALS_CHOICE = 1;
	static final private int LESS_THAN_CHOICE = 2;
	static final protected int LESS_EQUALS_CHOICE = 3;
	static final protected int GREATER_THAN_CHOICE = 4;
	static final private int GREATER_EQUALS_CHOICE = 5;
	
	
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
	
	static final protected double kEps = 0.0005;
	static final protected double kRoughEps = 0.005;
	static final private int kPropnDecimals = 3;
	
	private PropnTemplatePanel propnTemplate;
	
	private ResultValuePanel resultPanel;
	
//-----------------------------------------------------------
	
	protected XPanel getBottomPanel() {
		XPanel thePanel = new InsetPanel(0, 20, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel answerPanel = new XPanel();
			answerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, "Answer =", 6);
				registerStatusItem("answer", resultPanel);
			answerPanel.add(resultPanel);
			
			answerPanel.add(createMarkingPanel(NO_HINTS));
		
		thePanel.add("North", answerPanel);
		
			XPanel messagePanel = new XPanel();
			messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
				
				message = new ExerciseMessagePanel(this);
			messagePanel.add(message);
		thePanel.add("Center", messagePanel);
		return thePanel;
	}
		
	protected XPanel getPropnTemplatePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
			XPanel templatePanel = new InsetPanel(10, 5);
			templatePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
				FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
				propnTemplate = new PropnTemplatePanel(null, 5, stdContext);
				registerStatusItem("propnTemplate", propnTemplate);
			
			templatePanel.add(propnTemplate);
			
			templatePanel.lockBackground(kTemplateBackground);
		thePanel.add(templatePanel);
		return thePanel;
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("varName", "string");
		registerParameter("itemName", "string");
		registerParameter("categories", "array");
		registerParameter("counts", "string");
		registerParameter("percentPropn", "string");
		registerParameter("questionType", "choice");
		registerParameter("criticalIndex", "choice");
		registerParameter("rangeText", "array");				//		not directly referred to but the question picks out one array value
	}
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	protected String getItemName() {
		return getStringParam("itemName");
	}
	
	protected Value[] getCategories() {
		StringArray values = getArrayParam("categories");
		
		Value label[] = new Value[values.getNoOfStrings()];
		for (int i=0 ; i<label.length ; i++) {
			String labelString = values.getValue(i);
			label[i] = new LabelValue(labelString);
		}
		
		return label;
	}
	
	private int[] getCounts() {
		StringTokenizer st = new StringTokenizer(getStringParam("counts"));
		int counts[] = new int[st.countTokens()];
		for (int i=0 ; i<counts.length ; i++)
			counts[i] = Integer.parseInt(st.nextToken());
		
		return counts;
	}
	
	protected boolean isPercent() {
		return getStringParam("percentPropn").equals("percentage");
	}
	
	protected int getQuestionType() {
		return getIntParam("questionType");
	}
	
	protected int getCriticalIndex() {
		return getIntParam("criticalIndex");
	}
	
	
//-----------------------------------------------------------
	
	protected void setDisplayForQuestion() {
		propnTemplate.setValues(new NumValue(1, 0), new NumValue(1, 0));
		
		resultPanel.clear();
		
		data.variableChanged("y");
	}
	
	
	protected void setDataForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.setLabels(getCategories());
		yVar.setCounts(getCounts());
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int questionType = getQuestionType();
		double attempt;
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the requested " + (isPercent() ? "percentage" : "proportion") + " into the box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a " + (isPercent() ? "percentage" : "proportion") + " into the text box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				attempt = getAttempt();
				if (isPercent() && attempt < 0 || attempt > 100)
					messagePanel.insertText("Percentages must be between 0 and 100.");
				else
					messagePanel.insertText("Proportions must be between 0 and 1.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				insertSolution(messagePanel, questionType);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				insertSolution(messagePanel, questionType);
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("Your answer is close, but try to specify the ");
				messagePanel.insertText(isPercent() ? ("percentage correct to " + (kPropnDecimals - 2) + " decimal digit.")
																									: ("proportion correct to " + kPropnDecimals + " decimal digits."));
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				attempt = getAttempt();
				double correctPropn = getCorrectCount(questionType) / (double)getNValues();
				if (!showPercentPropnError(messagePanel, attempt, correctPropn)) {
					if (isPercent())
						attempt /= 100;
					if (Math.abs(1 - attempt - correctPropn) < kEps) {
						messagePanel.insertRedText("You have added all of the counts ");
						messagePanel.insertBoldRedText("except");
						messagePanel.insertRedText(" the correct ones. (Read the question more carefully.)");
					}
					else if (isEqualsError(questionType, attempt)) {
						CatVariable yVar = (CatVariable)data.getVariable("y");
						String criticalCat = yVar.getLabel(getCriticalIndex()).toString();
						switch (questionType) {
							case LESS_THAN_CHOICE:
								messagePanel.insertRedText("You have counted the " + getItemName() + "s with " + getVarName() + " less than");
								messagePanel.insertBoldRedText(" or equal to ");
								messagePanel.insertRedText(criticalCat + " not less than this.");
								break;
							case LESS_EQUALS_CHOICE:
								messagePanel.insertRedText("You have counted the " + getItemName() + "s with " + getVarName());
								messagePanel.insertBoldRedText(" less than ");
								messagePanel.insertRedText(criticalCat + " not less than or equal to this.");
								break;
							case GREATER_THAN_CHOICE:
								messagePanel.insertRedText("You have counted the " + getItemName() + "s with " + getVarName() + " greater than");
								messagePanel.insertBoldRedText(" or equal to ");
								messagePanel.insertRedText(criticalCat + " not greater than this.");
								break;
							case GREATER_EQUALS_CHOICE:
								messagePanel.insertRedText("You have counted the " + getItemName() + "s with " + getVarName());
								messagePanel.insertBoldRedText(" greater than ");
								messagePanel.insertRedText(criticalCat + " not greater than or equal to this.");
								break;
						}
					}
					else
						messagePanel.insertText("Your answer is not close to the correct value.");
				}
				break;
		}
	}
	
	protected boolean showPercentPropnError(MessagePanel messagePanel, double attempt, double correctPropn) {
		if (isPercent() && Math.abs(attempt - correctPropn) < kEps) {
			messagePanel.insertRedText("You have given the correct ");
			messagePanel.insertBoldRedText("proportion");
			messagePanel.insertRedText(", not the correct ");
			messagePanel.insertBoldRedText("percentage");
			messagePanel.insertRedText(".");
			return true;
		}
		else if (!isPercent() && Math.abs(attempt / 100.0 - correctPropn) < kEps) {
			messagePanel.insertRedText("You have given the correct ");
			messagePanel.insertBoldRedText("percentage");
			messagePanel.insertRedText(", not the correct ");
			messagePanel.insertBoldRedText("proportion");
			messagePanel.insertRedText(".");
			return true;
		}
		else
			return false;
	}
	
	private boolean isEqualsError(int questionType, double attempt) {
		if (questionType == EQUALS_CHOICE || questionType == NOT_EQUALS_CHOICE)
			return false;
		
		if (questionType == LESS_THAN_CHOICE || questionType == GREATER_THAN_CHOICE)
			questionType ++;
		else
			questionType --;
		
		CatVariable yVar = (CatVariable)data.getVariable("y");
		double errorPropn = getCorrectCount(questionType) / (double)yVar.noOfValues();
		
		return Math.abs(errorPropn - attempt) < kEps;
	}
	
	private void insertSolution(MessagePanel messagePanel, int questionType) {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		int count[] = yVar.getCounts();
		boolean correct[] = getCorrectCats(questionType);
		int nCorrectCats = 0;
		int nCorrect = 0;
		int n = 0;
		for (int i=0 ; i<correct.length ; i++) {
			if (correct[i]) {
				nCorrectCats ++;
				nCorrect += count[i];
			}
			n += count[i];
		}
		NumValue correctPropn = new NumValue(nCorrect / (double)n, kPropnDecimals);
		if (nCorrectCats == 1) {
			messagePanel.insertText("The question refers to exactly one of the " + getCategoriesString() + ".\n");
			messagePanel.insertText("The correct proportion is therefore " + nCorrect + "/" + n + " = " + correctPropn + ".");
		}
		else if (usesCumulative() && correct[0] && !correct[correct.length - 1]) {		//		low cum prob
			messagePanel.insertText("The question refers to the lowest " + nCorrectCats + " " + getCategoriesString() + ".\n");
			messagePanel.insertText("From the cumulative frequency, the correct proportion is therefore " + nCorrect + "/" + n + " = " + correctPropn + ".");
		}
		else if (usesCumulative() && !correct[0] && correct[correct.length - 1]) {		//		high cum prob
			messagePanel.insertText("The question refers to the highest " + nCorrectCats + " " + getCategoriesString() + ".\n");
			messagePanel.insertText("From the cumulative frequency, the correct proportion is therefore (" + n + "-" + (n - nCorrect) + ")/" + n + " = " + correctPropn + ".");
		}
		else if (nCorrectCats == count.length - 1) {
			messagePanel.insertText("The question refers to all but one of the " + getCategoriesString() + ".\n");
			messagePanel.insertText("The correct proportion is therefore (" + n + "-" + (n - nCorrect) + ")/" + n + " = " + correctPropn + ".");
		}
		else {
			messagePanel.insertText("The question refers to " + nCorrectCats + " of the " + getCategoriesString() + ".\n");
			messagePanel.insertText("The correct proportion is therefore (");
			boolean firstCount = true;
			for (int i=0 ; i<correct.length ; i++)
				if (correct[i]) {
					if (firstCount)
						firstCount = false;
					else
						messagePanel.insertText("+");
				messagePanel.insertText(String.valueOf(count[i]));
			}
			messagePanel.insertText(")/" + n + " = " + correctPropn + ".");
		}
		if (isPercent())
			messagePanel.insertText(" The percentage is 100 times this.");
	}
	
	
	abstract protected String getCategoriesString();
	abstract protected boolean usesCumulative();
	
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable yVar = new CatVariable("");
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected boolean[] getCorrectCats(int questionType) {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		int criticalIndex = getCriticalIndex();
		boolean correct[] = new boolean[yVar.noOfCategories()];		//	all false
		
		switch (questionType) {
			case EQUALS_CHOICE:
				correct[criticalIndex] = true;
				break;
			case NOT_EQUALS_CHOICE:
				for (int i=0 ; i<correct.length ; i++)
					if (i != criticalIndex)
						correct[i] = true;
				break;
			case LESS_THAN_CHOICE:
				criticalIndex --;
			case LESS_EQUALS_CHOICE:
				for (int i=0 ; i<=criticalIndex ; i++)
					correct[i] = true;
				break;
			case GREATER_THAN_CHOICE:
				criticalIndex ++;
			case GREATER_EQUALS_CHOICE:
				for (int i=criticalIndex ; i<correct.length ; i++)
					correct[i] = true;
				break;
		}
		return correct;
	}
	
	protected int getCorrectCount(int questionType) {
		int count[] = getDataCounts();
		boolean correct[] = getCorrectCats(questionType);
		
		int sum = 0;
		for (int i=0 ; i<count.length ; i++)
			if (correct[i])
				sum += count[i];
		return sum;
	}
	
	protected int[] getDataCounts() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		return yVar.getCounts();
	}
	
	protected int getNValues() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		return yVar.noOfValues();
	}
	
//-----------------------------------------------------------
	
	abstract protected void selectCorrectCounts();
	
	protected int checkPropn() {
		int localResult;
		double attemptPropn = getAttempt();
		if (isPercent())
			attemptPropn /= 100;
			
		if (resultPanel.isClear())
			localResult = ANS_INCOMPLETE;
		if (attemptPropn < 0.0 || attemptPropn > 1)
			localResult = ANS_INVALID;
		else {
			double correctPropn = getCorrectCount(getQuestionType()) / (double)getNValues();
			
			if (Math.abs(correctPropn - attemptPropn) <= kEps)
				localResult = ANS_CORRECT;
			else {
			 if (Math.abs(correctPropn - attemptPropn) <= kRoughEps)
					localResult = ANS_CLOSE;
				else
					localResult = ANS_WRONG;
			}
		}
		return localResult;
	}
	
	protected int assessAnswer() {
		return checkPropn();
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		selectCorrectCounts();
		
		int n = getCorrectCount(getQuestionType());
		int nTotal = getNValues();
		double correctPropn = n / (double)nTotal;
		
		if (isPercent())
			resultPanel.showAnswer(new NumValue(correctPropn * 100, kPropnDecimals - 2));
		else
			resultPanel.showAnswer(new NumValue(correctPropn, kPropnDecimals));
		
		if (propnTemplate != null)
			propnTemplate.setValues(new NumValue(n, 0), new NumValue(nTotal, 0));
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
	
}