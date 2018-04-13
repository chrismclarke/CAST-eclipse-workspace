package statisticProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;

import statistic.*;


public class CenterSpreadStatApplet extends CenterSpreadApplet {
	static final private String STAT_DECIMALS_PARAM = "statDecimals";
	
	private XChoice rangeChoice;
	private int currentRangeChoice = 0;
	
	protected DotPlotView getDataView(DataSet data, HorizAxis axis) {
		int decimals = Integer.parseInt(getParameter(STAT_DECIMALS_PARAM));
		TwoGroupStatView theView = new TwoGroupStatView(data, this, axis, "y", "group", "y0", "y1", decimals);
		theView.setStatColors(Color.red, Color.blue);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new ProportionLayout(0.5, 10));
			
				centerSlider = new XNoValueSlider(translate("low"), translate("high"), translate("Centre"), 0, 100, 50, this);
				centerSlider.setForeground(kCenterColor);
			sliderPanel.add(ProportionLayout.LEFT, centerSlider);
		
				spreadSlider = new XNoValueSlider(translate("low"), translate("high"), translate("Spread"), 0, 100, 35, this);
				spreadSlider.setForeground(kSpreadColor);
			sliderPanel.add(ProportionLayout.RIGHT, spreadSlider);
		
		thePanel.add("Center", sliderPanel);
		
			XPanel rangePanel = new XPanel();
			rangePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				rangeChoice = new XChoice(translate("Measure of spread") + ":", XChoice.HORIZONTAL, this);
				rangeChoice.addItem(translate("Inter-quartile range"));
				rangeChoice.addItem(translate("Range"));
				
			rangePanel.add(rangeChoice);
			
		thePanel.add("South", rangePanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == rangeChoice) {
			int newChoice = rangeChoice.getSelectedIndex();
			if (newChoice != currentRangeChoice) {
				currentRangeChoice = newChoice;
				
				((TwoGroupStatView)theView).setRangeType(newChoice);
			}
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