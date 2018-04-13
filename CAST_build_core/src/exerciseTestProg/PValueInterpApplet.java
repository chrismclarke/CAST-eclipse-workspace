package exerciseTestProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;

import exerciseTest.*;


public class PValueInterpApplet extends ExerciseApplet implements PValueConstants {
//	static final private String NULL_HYPOTH_PARAM = "nullHypoth";
//	static final private String ALT_HYPOTH_PARAM = "altHypoth";
	
	static final private String kSortedString = "sorted";
	static final private String kRandomisedString = "randomised";
//	static final private String kRepeatsString = "randomRepeats";
	
	static final private String kGenericString = "generic";
	static final private String kAppString = "application";
//	static final private String kRandomString = "mixedAppGen";
	
	static final private String kAltOnlyString = "altOnly";
//	static final private String kNullAltBadString = "nullAltBad";
	
//	private String altHypothesis[];
//	private String nullHypothesis[];
	
	private AlmostRandomInteger pValueGenerator;
	
	private PValueChoicePanel multiChoicePanel;
		
//-----------------------------------------------------------
	
	protected void createDisplay() {
		pValueGenerator = new AlmostRandomInteger(0, 5, nextSeed());
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
				questionPanel = new QuestionPanel(this);
			topPanel.add(questionPanel);
				
			topPanel.add(getWorkingPanels(null));
			
			topPanel.add(createMarkingPanel(NO_HINTS));
			
		add("North", topPanel);
				
			message = new ExerciseMessagePanel(this);
			
		add("Center", message);
	}
		
//-----------------------------------------------------------
	
	protected void addTypeDelimiters() {
		addType("p-value", ",");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("p-value"))
			return new NumValue(valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("p-value")) {
			Random generator = new Random(nextSeed());
			double rand = generator.nextDouble();
			int pValueType = pValueGenerator.generateOne();
			double pValue = 0.0;
			switch (pValueType) {
				case 0:
				case 1:
					pValue = 0.1001 + rand * 0.8999;
					break;
				case 2:
					pValue = 0.0501 + rand * 0.0498;
					break;
				case 3:
					pValue = 0.0101 + rand * 0.0398;
					break;
				case 4:
					pValue = 0.001 + rand * 0.0089;
					break;
				case 5:
					pValue = 0.0;
					break;
			}
			return new NumValue(pValue, 4);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
		
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("p-value", "p-value");
		registerParameter("nullHypoth", "string");
		registerParameter("altHypoth", "string");
	}
	
	private NumValue getPValue() {
		return getNumValueParam("p-value");
	}
	
	private String getNullHypothesis() {
		return getStringParam("nullHypoth");
	}
	
	private String getAltHypothesis() {
		return getStringParam("altHypoth");
	}
		
//-----------------------------------------------------------

/*	
	protected void readQuestions() {
		super.readQuestions();
		int nQuestions = question.length;
		altHypothesis = new String[nQuestions];
		nullHypothesis = new String[nQuestions];
		
		for (int i=0 ; i<nQuestions ; i++) {
			altHypothesis[i] = readString(ALT_HYPOTH_PARAM + i);
			nullHypothesis[i] = readString(NULL_HYPOTH_PARAM + i);
		}
		
		pValueGenerator = new AlmostRandomInteger(0, 5, nextSeed());
	}
*/
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		int ordering = (hasOption(kSortedString)) ? SORTED
									: (hasOption(kRandomisedString)) ? RANDOMISED
									: RANDOM_REPEATS;
		int appOrGeneric = (hasOption(kGenericString)) ? GENERIC
									: (hasOption(kAppString)) ? APP
									: RANDOM;
		boolean altOnly = hasOption(kAltOnlyString);
		
		multiChoicePanel = new PValueChoicePanel(this, "", "", getPValue(),
																					ordering, appOrGeneric, altOnly);
		registerStatusItem("pValueChoice", multiChoicePanel);
		
		return multiChoicePanel;
	}
	
	
	protected void setDisplayForQuestion() {
		if (multiChoicePanel != null) {
			multiChoicePanel.changeOptions(getNullHypothesis(), getAltHypothesis(), getPValue());
			multiChoicePanel.clearRadioButtons();
			multiChoicePanel.invalidate();
		}
	}
	
	protected void setDataForQuestion() {
	}
	
	protected DataSet getData() {
		return null;
	}

	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Select the answer that best interprets the p-value.");
				if (hasHints)
					messagePanel.insertText("\n(The closer the p-value to zero, the stronger the evidence that the null hypothesis is wrong -- i.e. the stronger the evidence that " + getAltHypothesis() + ".)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(multiChoicePanel.getSelectedOptionMessage());
				break;
			case ANS_CORRECT:
				messagePanel.insertHeading("Correct!\n");
				messagePanel.insertText(multiChoicePanel.getSelectedOptionMessage());
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must select one of the options by clicking a radio button.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText(multiChoicePanel.getSelectedOptionMessage());
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 0;			//		in Center of BorderLayout
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		return multiChoicePanel.checkCorrect();
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		multiChoicePanel.showAnswer();
	}
	
	protected double getMark() {
		return (multiChoicePanel.checkCorrect() == ANS_CORRECT) ? 1 : 0;
	}
	
}