package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import imageUtils.*;


public class MatchAnovaFormulaeApplet extends CoreMatchApplet {
	static final private int kNoOfFormulae = 4;
	static final private String[] kImageNames = {"anova/e_rSqrFormula.png", "anova/e_msTotalFormula.png",
													"anova/e_msWithinFormula.png", "anova/e_fFormula.png"};
	static final private String kAnovaTableFile = "anova/e_anovaTable.png";
	
	static final private String R2_TEXT_PARAM = "r2Text";
	static final private String MS_TOTAL_TEXT_PARAM = "msTotalText";
	static final private String MS_WITHIN_TEXT_PARAM = "msWithinText";
	static final private String F_TEXT_PARAM = "fText";
	
	static final private Color kAnswerBackground = new Color(0xE9E9FF);
	
	private DragImagesView formulae;
	private DragStringsView descriptions;
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 7));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add("South", questionPanel);
			
				ImageCanvas anovaTable = new ImageCanvas(kAnovaTableFile, this);
			topPanel.add("Center", anovaTable);
			
		add("North", topPanel);
		
			XPanel localWorkingPanel = getWorkingPanels(data);		//	CoreMatchApplet has variable workingPanel
			setArrowPropn(0.5);
			localWorkingPanel.lockBackground(kAnswerBackground);
		add("Center", localWorkingPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
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
		registerParameter("response", "string");
		registerParameter("factor", "string");
	}
	
	private String getFactorName() {
		return getStringParam("factor");
	}
	
	private String getResponseName() {
		return getStringParam("response");
	}
	
	protected int noOfItems() {
		return kNoOfFormulae;
	}
	
	protected boolean retainFirstItems() {
		return false;
	}
	
	protected int getDragMatchHeight() {
		return descriptions.getSize().height;
	}
	
	protected void setWorkingPanelLayout(XPanel thePanel) {
		thePanel.setLayout(new BorderLayout(40, 0));
	}
	
	protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder) {
		formulae = new DragImagesView(data, this, kImageNames, leftOrder);
		registerStatusItem("imagePerm", formulae);
		
		thePanel.add("West", formulae);
		
		return formulae;
	}
	
	protected XPanel addRightItems(XPanel thePanel, int[] rightOrder) {
		descriptions = new DragStringsView(data, this, getTextStrings(), rightOrder);
		descriptions.setFont(getBigFont());
		registerStatusItem("descriptionPerm", descriptions);
		
		thePanel.add("Center", descriptions);
		
		return descriptions;
	}
	
	private String[] getTextStrings() {
		String[] textStrings = {getParameter(R2_TEXT_PARAM), getParameter(MS_TOTAL_TEXT_PARAM),
													getParameter(MS_WITHIN_TEXT_PARAM), getParameter(F_TEXT_PARAM)};
		for (int i=0 ; i<textStrings.length ; i++) {
			textStrings[i] = textStrings[i].replaceAll("response", getResponseName());
			textStrings[i] = textStrings[i].replaceAll("factor", getFactorName());
		}
		return textStrings;
	}
	
	
//-----------------------------------------------------------
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		descriptions.setTextStrings(getTextStrings());
		descriptions.repaint();
	}
	
	protected void setDataForQuestion() {
		super.setDataForQuestion();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Reorder the formulae (or the descriptions) by dragging so that each sentence matches the corresponding formula.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The correct matching of descriptions to formulae is shown.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched up the four formulae to their textual descriptions.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The red arrows indicate the formulae that do not correspond to their adjacent descriptions.");
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
	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
	}
	
}