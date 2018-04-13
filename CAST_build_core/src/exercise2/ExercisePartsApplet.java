package exercise2;

import java.awt.*;

import dataView.*;
import qnUtils.*;


abstract public class ExercisePartsApplet extends ExerciseApplet {
	
	protected XTabbedPanel tabbedPanel;
	private String[] partText;
	
	protected XPanel createTabbedPanel(String title, int nTextLines) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			tabbedPanel = new XTabbedPanel(title, noOfTabs(), nTextLines, this);
		thePanel.add("Center", tabbedPanel);
		return thePanel;
	}
	
	protected boolean hasQuestionParts() {
		return true;
	}
	
	public String expandQuestion(String qnParamTemplate, String qnTemplate, String qnExtraTemplate) {
																//	decodes the part questions that are stored in qnExtra[]
		Object oldParam[] = cloneParameters();
		
		String mainQuestionText = super.expandQuestion(qnParamTemplate, qnTemplate, qnExtraTemplate);
		
		String partTemplate[] = qnExtra[questionVersion];
		partText = new String[partTemplate.length];
		for (int i=0 ; i<partTemplate.length ; i++)
			partText[i] = expandOneQuestionString(partTemplate[i], oldParam);
		
		return mainQuestionText;
	}
	
	protected void setDisplayForQuestion() {
		tabbedPanel.setTabStrings(partText);
	}
	
//================================================
	
	abstract protected int noOfTabs();
	abstract public void noteChangedTab(int newTabIndex);
}