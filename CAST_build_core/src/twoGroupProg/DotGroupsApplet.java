package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import twoGroup.*;

public class DotGroupsApplet extends XApplet {
							//		Only used to generate dot plots for Javascript images
							
	static final protected String MAX_SUMMARY_PARAM = "maxSummary";
							
	private XChoice dataSetChoice;
	private XCheckbox showMeansCheck;
	
	private XLabel responseNameLabel;
	
	private VerticalDotView theView;
	private VertAxis theNumAxis;
	private HorizAxis theGroupAxis;
	
	private GroupsDataSet data;
	
	public void setupApplet() {
		data = new GroupsDataSet(this);
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("East", infoPanel(data));
		
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
		
			theView = new VerticalDotView(data, this, theNumAxis, theGroupAxis, "y", "x", null, 0.5);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 4));
			
			dataSetChoice = data.dataSetChoice(this);
			thePanel.add(dataSetChoice);
			
		return thePanel;
	}
	
	private XPanel infoPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
			
			String maxSummaryString = getParameter(MAX_SUMMARY_PARAM);
		
			GroupSummaryView mean1View = new GroupSummaryView(data, this, GroupSummaryView.X_BAR, 0,
																																	maxSummaryString, data.getSummaryDecimals());
			mean1View.setForeground(Color.blue);
		thePanel.add(mean1View);
		
			GroupSummaryView mean2View = new GroupSummaryView(data, this, GroupSummaryView.X_BAR, 1,
																														maxSummaryString, data.getSummaryDecimals());
			mean2View.setForeground(Color.blue);
		thePanel.add(mean2View);
		
			GroupSummary2View meanDiffView = new GroupSummary2View(data, this, GroupSummary2View.X_BAR_DIFF,
																														maxSummaryString, data.getSummaryDecimals());
			meanDiffView.setForeground(Color.red);
		thePanel.add(meanDiffView);
		
			showMeansCheck = new XCheckbox("Show means", this);
		thePanel.add(showMeansCheck);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == showMeansCheck) {
			theView.setMeanDisplay(showMeansCheck.getState() ? VerticalDotView.MEAN_CHANGE
																												: VerticalDotView.NO_MEAN);
			theView.repaint();
			return true;
		}
		else if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex(), null, null)) {
				
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