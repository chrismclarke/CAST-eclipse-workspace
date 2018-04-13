package exerciseBivarProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;

import exerciseBivar.*;


public class BetaInterpInterpApplet extends ExerciseApplet {
	static final private Color kEqnColor = new Color(0x990000);
	
	private XLabel eqnView;
	private BetaInterpChoicePanel multiChoicePanel;
	
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
//		registerParameter("index", "int");					//	always registered
		registerParameter("slope", "const");
		registerParameter("intercept", "const");	
		registerParameter("xVarName", "string");
		registerParameter("yVarName", "string");
		registerParameter("qnParam", "choice");
		registerParameter("paramOrder", "choice");
		registerParameter("qnText", "array");		//	slope (correct, wrong), intercept (correct, wrong)
	}
	
	private NumValue getSlope() {
		return getNumValueParam("slope");
	}
	
	private NumValue getIntercept() {
		return getNumValueParam("intercept");
	}
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private int getQuestionParam() {
		return getIntParam("qnParam");
	}
	
	private String[] getQuestionText() {
		return getArrayParam("qnText").getStrings();
		
//		StringTokenizer st = new StringTokenizer(getStringParam("qnText"), "*");
//		String[] result = new String[st.countTokens()];
//		
//		for (int i=0 ; i<4 ; i++)
//			result[i] = st.nextToken();
//		
//		return result;
	}
	
	private int getParamOrder() {
		return getIntParam("paramOrder");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel eqnPanel = new XPanel();
			eqnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				eqnView = new XLabel("", XLabel.CENTER, this);
				eqnView.setFont(getBigFont());
				eqnView.setForeground(kEqnColor);
			eqnPanel.add(eqnView);
		
		thePanel.add("North", eqnPanel);
			
			multiChoicePanel = new BetaInterpChoicePanel(this, getXVarName(), getYVarName(), getSlope(),
																						getIntercept(), getQuestionParam(), getQuestionText());
			registerStatusItem("betaInterpChoice", multiChoicePanel);
			
		thePanel.add("Center", multiChoicePanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		multiChoicePanel.changeOptions(getXVarName(), getYVarName(), getSlope(), getIntercept(),
																												getQuestionParam(), getQuestionText());
		multiChoicePanel.clearRadioButtons();
		multiChoicePanel.invalidate();
		
		NumValue slope = new NumValue(getSlope());					//	clone since we may need to change sign
		NumValue intercept = new NumValue(getIntercept());	//	clone since we may need to change sign
		String rightString;
		if (getParamOrder() == 0) {
			if (slope.toDouble() > 0)
				rightString = intercept + " + " + slope + " " + getXVarName();
			else {
				slope.setValue(-slope.toDouble());
				rightString = intercept + " - " + slope + " " + getXVarName();
			}
		}
		else {
			if (intercept.toDouble() > 0)
				rightString = slope + " " + getXVarName() + " + " + intercept;
			else {
				intercept.setValue(-intercept.toDouble());
				rightString = slope + " " + getXVarName() + " - " + intercept;
			}
		}
		eqnView.setText(getYVarName() + "  =  " + rightString);
		eqnView.invalidate();
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