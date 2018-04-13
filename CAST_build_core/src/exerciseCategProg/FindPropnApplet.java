package exerciseCategProg;

import java.awt.*;

import dataView.*;
import utils.*;

import exerciseCateg.*;


public class FindPropnApplet extends CoreFindPropnApplet {	
	private FrequencyTableView freqTable;
	
//-----------------------------------------------------------
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 10));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add("North", questionPanel);
			
			topPanel.add("Center", getWorkingPanels(data));
		
		add("North", topPanel);
		
		add("Center", getBottomPanel());
	}
	
//-----------------------------------------------------------
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				freqTable = new FrequencyTableView(data, this, "y");
				freqTable.setFont(getBigFont());
				
			leftPanel.add(freqTable);
				
		thePanel.add("Center", leftPanel);
		
		thePanel.add("East", getPropnTemplatePanel());
		
		return thePanel;
	}
		
//-----------------------------------------------------------
	
	protected String getCategoriesString() {
		return "rows in the frequency table";
	}
	
	protected boolean usesCumulative() {
		return false;
	}
		
	protected void selectCorrectCounts() {
		if (freqTable != null) {
			boolean[] correctCats = getCorrectCats(getQuestionType());
			freqTable.selectCats(correctCats);
			freqTable.repaint();
		}
	}
	
}