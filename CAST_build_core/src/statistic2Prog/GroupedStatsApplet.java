package statistic2Prog;

import java.awt.*;

import axis.*;
import dataView.*;
import boxPlotProg.*;
import boxPlot.*;
import utils.*;
import statistic2.*;

public class GroupedStatsApplet extends RandomGroupedBoxApplet {
	static final private String DECIMALS_PARAM = "decimals";
	
	private XChoice centerChoice, spreadChoice;
	private CatCenterView centerView;
	private CatSpreadView spreadView;
	
	public void setupApplet() {
		setupData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		
		XPanel rightPanel = new XPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add("South", rightPanel(data));
		rightPanel.add("Center", statPanel(data));
		
		add("East", rightPanel);
		
		((GroupedBoxStatView)theView).setLinkedStats(centerView, spreadView);
	}
	
	protected GroupedBoxView createView(DataSet data, HorizAxis theHorizAxis,
																							VertAxis theVertAxis) {
		return new GroupedBoxStatView(data, this, theHorizAxis, theVertAxis);
	}
	
	private XPanel statPanel(DataSet data) {
		Font boldFont = getStandardBoldFont();
		Font plainFont = getStandardFont();
		
		XPanel thePanel = new XPanel();
		XLabel centerLabel = new XLabel(translate("Centre"), XLabel.CENTER, this);
		thePanel.add(centerLabel);
		centerLabel.setFont(boldFont);
		
		XLabel spreadLabel = new XLabel(translate("Spread"), XLabel.CENTER, this);
		thePanel.add(spreadLabel);
		spreadLabel.setFont(boldFont);
		
		centerChoice = new XChoice(this);
		centerChoice.addItem(translate("Median"));
		centerChoice.addItem(translate("Mean"));
		centerChoice.select(0);
		thePanel.add(centerChoice);
		
		spreadChoice = new XChoice(this);
		spreadChoice.addItem(translate("Range"));
		spreadChoice.addItem(translate("IQR"));
		spreadChoice.addItem(translate("St devn"));
		spreadChoice.select(0);
		thePanel.add(spreadChoice);
		
		CatLabelsView labelView = new CatLabelsView(data, this);
		thePanel.add(labelView);
		labelView.setFont(plainFont);
		
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		
		centerView = new CatCenterView(data, this, theView, decimals);
		centerView.setCenterStat(CenterCalculator.MEDIAN);
		centerView.lockBackground(Color.white);
		centerView.setForeground(Color.blue);
		thePanel.add(centerView);
		centerView.setFont(plainFont);
		
		spreadView = new CatSpreadView(data, this, theView, decimals);
		spreadView.lockBackground(Color.white);
		spreadView.setForeground(Color.darkGray);
		thePanel.add(spreadView);
		spreadView.setFont(plainFont);
		
		thePanel.setLayout(new StatLayout(centerLabel, spreadLabel, centerChoice, spreadChoice,
						labelView, centerView, spreadView));
		
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		thePanel.add(createSampleButton(translate("Sample")));
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == centerChoice) {
			int newCenterType = centerChoice.getSelectedIndex();
			if (newCenterType != centerView.getCenterStat())
				centerView.setCenterStat(newCenterType);
			return true;
		}
		else  if (target == spreadChoice) {
			int newSpreadType = spreadChoice.getSelectedIndex();
			if (newSpreadType != spreadView.getSpreadStat())
				spreadView.setSpreadStat(newSpreadType);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}