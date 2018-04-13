package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;

import twoGroup.*;
import bivarCat.*;


public class ProportionsApplet extends XApplet {
	static private final String DESCRIPTION_WIDTH_PARAM = "descriptionWidth";
	
	private XChoice dataSetChoice;
	private XCheckbox proportionCheck;
	
	private XTextArea dataDescriptions, dataQuestions;
	
	private ContinFitView theView;
	
	private ContinTableDataSet data;
	
	public void setupApplet() {
		data = new ContinTableDataSet(this);
		
		setLayout(new BorderLayout());
			theView = new ContinFitView(data, this, "x", "y", null, 4, 0);
			theView.setFont(getBigFont());
			theView.setForeground(Color.blue);
		add("Center", theView);
		add("East", controlPanel(data));
		
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
			bottomPanel.add(descriptions());
			bottomPanel.add(questions());
		
		add("South", bottomPanel);
	}
	
	private XTextArea descriptions() {
		String messageText[] = data.getDescriptionStrings();
		int messageWidth = Integer.parseInt(getParameter(DESCRIPTION_WIDTH_PARAM));
		
		dataDescriptions = new XTextArea(messageText, 0, messageWidth, this);
		dataDescriptions.setFont(getStandardFont());
		dataDescriptions.lockBackground(Color.white);
		
		return dataDescriptions;
	}
	
	private XTextArea questions() {
		String messageText[] = data.getQuestionStrings();
		int messageWidth = Integer.parseInt(getParameter(DESCRIPTION_WIDTH_PARAM));
		
		dataQuestions = new XTextArea(messageText, 0, messageWidth, this);
		dataQuestions.setFont(getStandardBoldFont());
		dataQuestions.setForeground(Color.red);
		dataQuestions.lockBackground(Color.white);
		
		return dataQuestions;
	}
	
	private XPanel controlPanel(ContinTableDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
			dataSetChoice = data.dataSetChoice(this);
			thePanel.add(dataSetChoice);
		
			proportionCheck = new XCheckbox(translate("Proportions"), this);
			thePanel.add(proportionCheck);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == proportionCheck) {
			theView.setDisplayType(TwoWayView.XMAIN,
						proportionCheck.getState() ? TwoWayView.PROPN_IN_X : TwoWayView.COUNT, false);
			return true;
		}
		else if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex(), dataDescriptions, dataQuestions)) {
				theView.reinitialise();
				data.variableChanged("x");
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}