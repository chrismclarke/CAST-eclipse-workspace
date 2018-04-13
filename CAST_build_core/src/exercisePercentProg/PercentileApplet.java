package exercisePercentProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import coreVariables.*;
import formula.*;

import percentile.*;
import exercisePercent.*;


public class PercentileApplet extends ExerciseApplet {
//	static final private String QUESTION_EXTRA_PARAM = "questionExtra";
	
	static final private Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final private Color kTemplateBackground = new Color(0xFFE594);
	
	static final private NumValue kOneValue = new NumValue(1.0, 0);
	static final private NumValue kHundredValue = new NumValue(100.0, 0);
//	static final private NumValue kNegativeInfinity = new NumValue(Double.NEGATIVE_INFINITY);
//	static final private NumValue kPositiveInfinity = new NumValue(Double.POSITIVE_INFINITY);
	
//	static final private String kMenuTitle = "Restrict to questions about:";
//	static final private String[] kQuestionMenuOptions = {"No restriction", "Percentages",
//																																	"Rates", "Return periods"};
	
	
	protected MultTemplatePanel multTemplate;
	private PercentileWorkingPanel workingPanel;
	private ResultValuePanel resultPanel;
	
	private RefDataSet refData;
	
//================================================

	protected void createDisplay() {
		refData = getReferenceData(data);
		registerStatusItem("refValue", refData);
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
			
//			if (hasOption("hasTypeChoice")) {
//				XPanel choicePanel = new XPanel();
//				choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//				
//				choicePanel.add(createQuestionTypeMenu(kMenuTitle, kQuestionMenuOptions, QUESTION_EXTRA_MENU));
//				topPanel.add(choicePanel);
//			}
			
				questionPanel = new QuestionPanel(this);
			topPanel.add(questionPanel);
				
		add("North", topPanel);
			
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
				
				resultPanel = new ResultValuePanel(this, "Answer =", "", 6);
				registerStatusItem("answer", resultPanel);
			bottomPanel.add(resultPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("trialName", "string");
		registerParameter("maxValue", "const");
		registerParameter("axis", "string");
		registerParameter("shortVarName", "string");
		registerParameter("longVarName", "string");
		registerParameter("data", "string");
		registerParameter("units", "string");
		registerParameter("directionName", "string");
		registerParameter("percent", "const");
		registerParameter("rateNumer", "const");
		registerParameter("rateDenom", "const");
		registerParameter("returnPeriod", "const");
		registerParameter("questionType", "choice");
	}
	
	private String getTrialName() {
		return getStringParam("trialName");
	}
	
	private NumValue getMaxValue() {
		return getNumValueParam("maxValue");
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private String getShortVarName() {
		return getStringParam("shortVarName");
	}
	
	private String getLongVarName() {
		return getStringParam("longVarName");
	}
	
	private String getDataValues() {
		return getStringParam("data");
	}
	
	private String getUnits() {
		return getStringParam("units");
	}
	
	private int getDirection() {			//		0 = less, 1 = more
		String directionString = getStringParam("directionName");
		return (directionString.equals("less") || directionString.equals("before")
																								|| directionString.equals("under")) ? 0 : 1;
	}
	
	protected int getQuestionType() {
		return getIntParam("questionType");		//	0=percent, 1=rate, 2=returnPeriod
	}
	
	private NumValue getPercent() {
		return getNumValueParam("percent");
	}
	
	private NumValue getRateNumer() {
		return getNumValueParam("rateNumer");
	}
	
	private NumValue getRateDenom() {
		return getNumValueParam("rateDenom");
	}
	
	private NumValue getReturnPeriod() {
		return getNumValueParam("returnPeriod");
	}
	
	protected int getQuestionExtraMask() {
		int mask = hasOption("percent") ? 1 : 0;
		if (hasOption("rate"))
			mask += 2;
		if (hasOption("returnPeriod"))
			mask += 4;
		if (mask == 0)
			return -1;
		else
			return mask;
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			workingPanel = new PercentileWorkingPanel(data, refData, this, getAxisInfo(), getLongVarName(),
																											getMaxValue(), getUnits());
		thePanel.add("Center", workingPanel);
			
		if (hasOption("template")) {
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			multTemplate = new MultTemplatePanel(stdContext);
			multTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("multTemplate", multTemplate);
			
			thePanel.add("South", multTemplate);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		workingPanel.changeData(getAxisInfo(), getLongVarName(), getMaxValue(), getUnits());
		
		if (multTemplate != null)
			multTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		resultPanel.changeUnits(getUnits());
		resultPanel.clear();
		resultPanel.invalidate();
	}
	
	protected void setDataForQuestion() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.readValues(getDataValues());
		yVar.name = getShortVarName();
	}
	
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NumVariable yVar = new NumVariable("");
		data.addVariable("y", yVar);
		return data;
	}
	
	private RefDataSet getReferenceData(DataSet data) {
		RefDataSet referenceData = new RefDataSet(data);
				
			ScaledVariable propnVar = new ScaledVariable(translate("Proportion"),
											(NumVariable)referenceData.getVariable("ref"), "ref", 0.0, 0.01, 2);
		referenceData.addVariable("propn", propnVar);
		
		return referenceData;
	}
	
	
//-----------------------------------------------------------
	
	private double getCorrectPercent() {
		double percent;
		switch (getQuestionType()) {
			case 0:			//	percentage
				percent = getPercent().toDouble();
				break;
			case 1:			//	rate
				percent = getRateNumer().toDouble() / getRateDenom().toDouble() * 100;
				break;
			default:
			case 2:			//	return period
				percent = 100.0 / getReturnPeriod().toDouble();
				break;
		}
		if (getDirection() == 1)
			percent = 100 - percent;
		return Math.round(percent);
	}
	
	private NumValue getCorrectPercentile() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		NumValue sortedY[] = yVar.getSortedData();
		double prob = getCorrectPercent() / 100.0;
		
		double percentile = PercentileInfo.evaluatePercentile(sortedY, prob, PercentileInfo.STEP);
		int decimals = getMaxValue().decimals;
		double factor = 1.0;
		for (int i=0 ; i<decimals ; i++) {
			percentile *= 10.0;
			factor /= 10.0;
		}
		percentile = Math.round(percentile) * factor;
		return new NumValue(percentile, decimals);
	}
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type a value in the Answer box above.");
				break;
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the answer in the box above.");
				break;
			case ANS_CORRECT:
			case ANS_TOLD:
				messagePanel.insertRedHeading(result == ANS_TOLD ? "Answer\n" : "Good!\n");
				NumValue percent;
				switch (getQuestionType()) {
					case 1:			//	rate
						percent = new NumValue(getRateNumer().toDouble() / getRateDenom().toDouble() * 100, 0);
						messagePanel.insertText("The rate of " + getRateNumer() + " per " + getRateDenom()
																		+ " "  + getTrialName() + "s is an event that happens in " + percent
																		+ "% of " + getTrialName() + "s.\n");
						break;
					case 2:			//	return period
						percent = new NumValue(1.0 / getReturnPeriod().toDouble() * 100, 0);
						messagePanel.insertText("A return period of " + getReturnPeriod()
												+ " "  + getTrialName() + "s corresponds to an event that happens in " + percent
												+ "% of " + getTrialName() + "s.\n");
						break;
				}
				percent = new NumValue(getCorrectPercent(), 0);
				messagePanel.insertText("In approximately " + percent + "% of " + getTrialName() + "s, "
																						+ getShortVarName() + " was less than or equal to "
																						+ getCorrectPercentile() + " " + getUnits() + ".");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				switch (getQuestionType()) {
					case 0:			//	percentage
						messagePanel.insertRedText("Drag the horizontal red line on the cumulative distribution function to read off the value for which approximately "
																									+ getCorrectPercent() + "% of the values are less.");
						break;
					case 1:			//	rate
						percent = new NumValue(getRateNumer().toDouble() / getRateDenom().toDouble() * 100, 0);
						messagePanel.insertRedText("A rate of " + getRateNumer() + " per " + getRateDenom()
														+ " corresponds to (" + getRateNumer() + " / " + getRateDenom() + " * 100)%.");
						if (getDirection() == 1) {
							messagePanel.insertRedText(" The percentage of values ");
							message.insertBoldText("below");
							messagePanel.insertRedText(" is 100 minus this. ");
						}
						messagePanel.insertRedText("\nDrag the horizontal red line to this cumulative percentage.");
						break;
					default:
					case 2:			//	return period
						percent = new NumValue(1.0 / getReturnPeriod().toDouble() * 100, 0);
						messagePanel.insertRedText("A return period of " + getReturnPeriod()
														+ " corresponds to a percentage (100 / " + getReturnPeriod() + ")%.");
						if (getDirection() == 1) {
							messagePanel.insertRedText(" The percentage of values ");
							message.insertBoldText("below");
							messagePanel.insertRedText(" is 100 minus this. ");
						}
						messagePanel.insertRedText("\nDrag the horizontal red line to this cumulative percentage.");
						break;
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = resultPanel.getAttempt().toDouble();
			NumValue correct = getCorrectPercentile();
			
			double slop = 0.5 * Math.pow(10.0, correct.decimals);
			
			return (Math.abs(attempt - correct.toDouble()) < slop) ? ANS_CORRECT : ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue correctVal = getCorrectPercentile();
		resultPanel.showAnswer(correctVal);
		
		workingPanel.setReferenceValue(getCorrectPercent());
		
		if (multTemplate != null)
			switch (getQuestionType()) {
				case 0:			//	percentage
					multTemplate.setValues(getPercent(), kOneValue, kOneValue);
					break;
				case 1:			//	rate
					multTemplate.setValues(getRateNumer(), getRateDenom(), kHundredValue);
					break;
				default:
				case 2:			//	return period
					multTemplate.setValues(kOneValue, getReturnPeriod(), kHundredValue);
					break;
			}
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
}