package statistic2Prog;

import java.awt.*;

import axis.*;
import dataView.*;
import boxPlotProg.*;
import boxPlot.*;
import utils.*;
import statistic2.*;

public class GroupedStats2Applet extends RandomGroupedBoxApplet {
	static final private String DECIMALS_PARAM = "decimals";
	
	private XChoice spreadChoice;
	private CatSpreadView spreadView;
	private CatCenterView centerView;
	
	public void setupApplet() {
		setupData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new BorderLayout(30, 0));
		bottomPanel.add("West", statPanel(data));
		bottomPanel.add("Center", buttonPanel(data));
		
		add("South", bottomPanel);
		
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
		centerLabel.setForeground(Color.blue);
		
		XLabel spreadLabel = new XLabel(translate("Spread"), XLabel.CENTER, this);
		thePanel.add(spreadLabel);
		spreadLabel.setFont(boldFont);
		spreadLabel.setForeground(Color.red);
		
		XLabel medianLabel = new XLabel("(Median)", XLabel.CENTER, this);
		thePanel.add(medianLabel);
		medianLabel.setFont(boldFont);
		medianLabel.setForeground(Color.blue);
		
		spreadChoice = new XChoice(this);
		spreadChoice.addItem(translate("Range"));
		spreadChoice.addItem(translate("IQR"));
		spreadChoice.select(0);
		thePanel.add(spreadChoice);
		
		CatLabelsView labelView = new CatLabelsView(data, this);
		thePanel.add(labelView);
		labelView.setFont(plainFont);
		labelView.setForeground(Color.black);
		
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		
		centerView = new CatCenterView(data, this, theView, decimals);
		centerView.setCenterStat(CenterCalculator.MEDIAN);
		centerView.lockBackground(Color.white);
		centerView.setForeground(Color.blue);
		thePanel.add(centerView);
		centerView.setFont(plainFont);
		
		spreadView = new CatSpreadView(data, this, theView, decimals);
		spreadView.lockBackground(Color.white);
		spreadView.setForeground(Color.red);
		thePanel.add(spreadView);
		spreadView.setFont(plainFont);
		
		thePanel.setLayout(new StatLayout(centerLabel, spreadLabel, medianLabel, spreadChoice,
						labelView, centerView, spreadView));
		
		return thePanel;
	}
	
	protected XPanel buttonPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		thePanel.add(createSampleButton(translate("Another sample")));
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == spreadChoice) {
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