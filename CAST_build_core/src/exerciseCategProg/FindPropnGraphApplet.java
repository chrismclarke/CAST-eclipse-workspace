package exerciseCategProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;

import exerciseCateg.*;


public class FindPropnGraphApplet extends CoreFindPropnApplet {
	private XLabel freqLabel, cumLabel;
	
	private HorizAxis valAxis;
	private MultiVertAxis countAxis, cumAxis;
	private BarCumulativeView barView;
	
//-----------------------------------------------------------
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
		
		add("South", getBottomPanel());
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("countAxis", "string");
		registerParameter("cumAxis", "string");
		registerParameter("showCount", "boolean");
	}
	
	private String getCountAxis() {
		return getStringParam("countAxis");
	}
	
	private String getCumAxis() {
		return getStringParam("cumAxis");
	}
	
	private boolean getShowCount() {
		return getBooleanParam("showCount");
	}
	
//-----------------------------------------------------------
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new BorderLayout());
			
				freqLabel = new XLabel("", XLabel.LEFT, this);
			labelPanel.add("West", freqLabel);
			labelPanel.add("Center", new XPanel());
				cumLabel = new XLabel("", XLabel.RIGHT, this);
			labelPanel.add("East", cumLabel);
			
		thePanel.add("North", labelPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				valAxis = new HorizAxis(this);
			mainPanel.add("Bottom", valAxis);
			
				countAxis = new MultiVertAxis(this, 2);
			mainPanel.add("Left", countAxis);
			
				cumAxis = new MultiVertAxis(this, 2);
			mainPanel.add("Right", cumAxis);
				
				barView = new BarCumulativeView(data, this, "y", valAxis, countAxis, cumAxis);
				barView.lockBackground(Color.white);
				registerStatusItem("selection", barView);
				
			mainPanel.add("Center", barView);
				
		thePanel.add("Center", mainPanel);
		
		thePanel.add("South", getPropnTemplatePanel());
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		
		freqLabel.setText(getShowCount() ? "Freq" : "Propn");
		freqLabel.invalidate();
		
		cumLabel.setText(getShowCount() ? "Cumulative freq" : "Cumulative propn");
		cumLabel.invalidate();
		
		valAxis.setCatLabels(yVar);
		
		countAxis.resetLabels();
		countAxis.readNumLabels(getCountAxis());
		String propnAxisString = neatAxisString(countAxis.maxOnAxis / yVar.noOfValues());
		countAxis.readExtraNumLabels(propnAxisString);
		countAxis.setStartAlternate(getShowCount() ? 0 : 1);
		countAxis.invalidate();
		
		cumAxis.resetLabels();
		cumAxis.readNumLabels(getCumAxis());
//		double maxCum = cumAxis.maxOnAxis;
		String cumPropnAxisString = neatAxisString(cumAxis.maxOnAxis / yVar.noOfValues());
		cumAxis.readExtraNumLabels(cumPropnAxisString);
		cumAxis.setStartAlternate(getShowCount() ? 0 : 1);
		cumAxis.invalidate();
		
		super.setDisplayForQuestion();
	}
	
	private String neatAxisString(double maxP) {
		double step = (maxP > 0.8) ? 0.2
									: (maxP > 0.4) ? 0.1
									: 0.05;
		return "0 " + maxP + " 0 " + step; 
	}
	
	
	protected int getMessageHeight() {
		return 120;
	}

		
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		super.insertMessageContent(messagePanel);
		if (result == ANS_UNCHECKED)
			messagePanel.insertText("\n(Click bars on the bar chart to find relevant proportions and cumulative proportions.)");
	}
	
	protected String getCategoriesString() {
		return "categories";
	}
	
	protected boolean usesCumulative() {
		return true;
	}
	
	protected void selectCorrectCounts() {
		if (barView != null) {
			boolean[] correctCats = getCorrectCats(getQuestionType());
			barView.setSelectedBars(correctCats);
			barView.repaint();
		}
	}
	
}