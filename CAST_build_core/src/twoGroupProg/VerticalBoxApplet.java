package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import boxPlot.*;


public class VerticalBoxApplet extends XApplet {
	static private final String DESCRIPTION_WIDTH_PARAM = "descriptionWidth";
	
	private XChoice dotOrBoxChoice, dataSetChoice;
	
	private XTextArea dataDescriptions, dataQuestions;
	private XLabel responseNameLabel;
	
	private GroupedBoxView theView;
	private VertAxis theNumAxis;
	private HorizAxis theGroupAxis;
	
	private GroupsDataSet data;
	
	public void setupApplet() {
		data = new GroupsDataSet(this);
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		
		XPanel eastPanel = new XPanel();
		eastPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
			eastPanel.add(descriptions());
			eastPanel.add(questions());
		
		add("East", eastPanel);
		
		responseNameLabel = new XLabel(data.getYVarName(), XLabel.LEFT, this);
		responseNameLabel.setFont(theNumAxis.getFont());
		add("North", responseNameLabel);
	}
	
	private XPanel displayPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theNumAxis = new VertAxis(this);
		theNumAxis.readNumLabels(data.getYAxisInfo());
		thePanel.add("Left", theNumAxis);
		
		theGroupAxis = new HorizAxis(this);
		CatVariable groupVariable = data.getCatVariable();
		theGroupAxis.setCatLabels(groupVariable);
		thePanel.add("Bottom", theGroupAxis);
		
		theView = new GroupedBoxView(data, this, theNumAxis, theGroupAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
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
	
	private XPanel controlPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 4));
			
			dataSetChoice = data.dataSetChoice(this);
			thePanel.add(dataSetChoice);
		
			dotOrBoxChoice = new XChoice(this);
				dotOrBoxChoice.addItem(translate("Dot plots"));
				dotOrBoxChoice.addItem(translate("Box plots"));
				dotOrBoxChoice.select(0);
			thePanel.add(dotOrBoxChoice);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dotOrBoxChoice) {
			int newPlotType = dotOrBoxChoice.getSelectedIndex();
			if (newPlotType != theView.getPlotType())
				theView.setPlotType(newPlotType);
			return true;
		}
		else if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex(), dataDescriptions, dataQuestions)) {
				
				theNumAxis.readNumLabels(data.getYAxisInfo());
				theNumAxis.repaint();
				
				CatVariable groupVariable = (CatVariable)data.getVariable("x");
				theGroupAxis.setCatLabels(groupVariable);
				theGroupAxis.repaint();
				
				data.variableChanged("x");
				
				responseNameLabel.setText(data.getYVarName());
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