package exerciseRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;


public class InterpretOutputApplet extends ExercisePartsApplet {
	
	private ResultValuePanel resultPanel;
	
	private XLabel partLabel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			
				questionPanel = new QuestionPanel("Context", this);
			topPanel.add(questionPanel);
			
			topPanel.add(createTabbedPanel("Question", 3));
			
		add("North", topPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				partLabel = new XLabel("", XLabel.LEFT, this);
			bottomPanel.add(partLabel);
			
				resultPanel = new ResultValuePanel(this, translate("Correlation coefficient") + ", r =", 6);
				registerStatusItem("corr", resultPanel);
			bottomPanel.add(resultPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
	protected int noOfTabs() {
		return 2;
	}
	
	public void noteChangedTab(int newTabIndex) {
		partLabel.setText("Selected part " + newTabIndex);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("mean", "const");
		registerParameter("sd", "const");
		registerParameter("count", "int");
	}
	
	protected NumValue getMean() {
		return getNumValueParam("mean");
	}
	
	protected NumValue getSD() {
		return getNumValueParam("sd");
	}

/*
	private int getCount() {
		return getIntParam("count");
	}
*/
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		return thePanel;
	}
	
/*
	protected void setDisplayForQuestion() {
		tabbedPane.setSelectedIndex(0);
		partLabel.setText(getPartQuestion());
	}
*/
	
	protected void setDataForQuestion() {
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Answer the question.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Incomplete!");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Invalid answer!");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This is the answer.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your answer is correct.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong answer!\n");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
			return ANS_CORRECT;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
	}
	
	protected double getMark() {
		return 1;
	}
}