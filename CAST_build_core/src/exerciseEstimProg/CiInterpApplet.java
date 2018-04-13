package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;

import exerciseEstim.*;


public class CiInterpApplet extends ExerciseApplet {
	protected boolean meanNotPropn = true;
	
	private CoreCiInterpChoicePanel multiChoicePanel;
	
//================================================
	
	protected void createDisplay() {
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
	
	protected void registerParameterTypes() {
		registerParameter("sampleValues", "string");
		registerParameter("popnValues", "string");
		registerParameter("newValue", "string");
		registerParameter("newSample", "string");
		registerParameter("samples", "string");
		registerParameter("sampleStat", "string");
		registerParameter("popnParam", "string");
		registerParameter("lowLimit", "const");
		registerParameter("highLimit", "const");
		registerParameter("units", "string");
	}
	
	private String getSampleValues() {
		return getStringParam("sampleValues");
	}
	
	private String getPopnValues() {
		return getStringParam("popnValues");
	}
	
	protected String getNewValue() {					//		not used by CiInterpPApplet so overridden by it
		return getStringParam("newValue");
	}
	
	private String getNewSample() {
		return getStringParam("newSample");
	}
	
	private String getSamples() {
		return getStringParam("samples");
	}
	
	protected String getSampleStat() {					//		not used by CiInterpPApplet so overridden by it
		return getStringParam("sampleStat");
	}
	
	protected String getPopnMean() {					//		not used by CiInterpPApplet so overridden by it
		return getStringParam("popnParam");
	}
	
	protected String getSuccess() {					//		overridden by CiInterpPApplet
		return null;
	}
	
	private NumValue getLowLimit() {
		return getNumValueParam("lowLimit");
	}
	
	private NumValue getHighLimit() {
		return getNumValueParam("highLimit");
	}
	
	protected String getUnits() {					//		must be overridden by CiInterpPApplet since there is no units param
		return getStringParam("units");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			if (meanNotPropn)
				multiChoicePanel = new CiMeanInterpChoicePanel(this);
			else
				multiChoicePanel = new CiPropnInterpChoicePanel(this);
			registerStatusItem("ciInterpChoice", multiChoicePanel);
			
		thePanel.add("Center", multiChoicePanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		multiChoicePanel.changeOptions(getSampleValues(), getPopnValues(), getNewValue(),
								getNewSample(), getSamples(), getSampleStat(), getPopnMean(),
								getSuccess(), getLowLimit(), getHighLimit(), getUnits());
		multiChoicePanel.clearRadioButtons();
		multiChoicePanel.invalidate();
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
				messagePanel.insertText("Select correct option.");
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
	
	public void showHints(boolean hasHints) {
		super.showHints(hasHints);
		message.changeContent();
	}
	
}