package bivarCatProg;

import java.awt.*;

import dataView.*;
import utils.*;
import bivarCat.*;


public class ContinTable2Applet extends Core2WayApplet {
	protected ContinTable2View tableView = null;
	
	private XChoice freqPropnChoice, groupingChoice;
	private int currentFreqPropn, currentGrouping;
	
	protected void addDisplayComponents(DataSet data) {
		setLayout(new BorderLayout(10, 0));
	
		add("Center", displayPanel(data));
		
			tableView.setDisplayType(TwoWayView.XMAIN, initialVertScale, false);
		
			XPanel controlPanel = canScalePropnInY ? horizVertPanel(data)
																								: horizOnlyPanel(data);
		add("South", controlPanel);
	}
	
	private XPanel horizOnlyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		thePanel.add(vertScalePanel(data, translate("Display") + ": "));
		return thePanel;
	}
	
	private XPanel horizVertPanel(DataSet data) {new XPanel();
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
//			XLabel freqPropnLabel = new XLabel("Display:", XLabel.LEFT, this);
//			freqPropnLabel.setFont(getStandardBoldFont());
//			leftPanel.add(freqPropnLabel);
				
				freqPropnChoice = new XChoice(translate("Display") + ":", XChoice.VERTICAL_CENTER, this);
				freqPropnChoice.addItem(translate("Frequency"));
				freqPropnChoice.addItem(translate("Proportion"));
				freqPropnChoice.addItem(translate("Percentage"));
				currentFreqPropn = 0;
			leftPanel.add(freqPropnChoice);
			
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
//			XLabel groupingLabel = new XLabel("Separately for each:", XLabel.LEFT, this);
//			groupingLabel.setFont(getStandardBoldFont());
//			rightPanel.add(groupingLabel);
				
				groupingChoice = new XChoice(translate("Separately for each") + ":", XChoice.VERTICAL_CENTER, this);
				groupingChoice.addItem(data.getVariable("x").name);
				groupingChoice.addItem(data.getVariable("y").name);
				currentGrouping = 0;
				groupingChoice.disable();
			rightPanel.add(groupingChoice);
			
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		String decimalString = getParameter(DECIMALS_PARAM);
		int decimals = (decimalString == null) ? 3 : Integer.parseInt(decimalString);
		
		tableView = new ContinTable2View(data, this, "x", "y", decimals, true);
		thePanel.add(tableView);
		
		return thePanel;
	}
	
	protected void changeVertScale(int newVertScale) {
		if (!canScaleCount)
			newVertScale ++;
		if (!canScalePropnInX && newVertScale == 1)
			newVertScale ++;
		if (tableView != null && newVertScale != tableView.getVertScale()) {
			int newMainGrouping = (newVertScale == TwoWayView.COUNT || newVertScale == TwoWayView.PROPN_IN_X || newVertScale == TwoWayView.PERCENT_IN_X)
													? TwoWayView.XMAIN : TwoWayView.YMAIN;
			tableView.setDisplayType(newMainGrouping, newVertScale, false);
		}
	}
	
	private boolean localAction(Object target) {
		if (target == freqPropnChoice) {
			if (currentFreqPropn != freqPropnChoice.getSelectedIndex()) {
				currentFreqPropn = freqPropnChoice.getSelectedIndex();
				int newVertScale = TwoWayView.COUNT;
				if (currentFreqPropn == 1)
					newVertScale = (currentGrouping == TwoWayView.XMAIN) ? TwoWayView.PROPN_IN_X
																										: TwoWayView.PROPN_IN_Y;
				else if (currentFreqPropn == 2)
					newVertScale = (currentGrouping == TwoWayView.XMAIN) ? TwoWayView.PERCENT_IN_X
																										: TwoWayView.PERCENT_IN_Y;
				tableView.setDisplayType(currentGrouping, newVertScale, false);
				
				groupingChoice.setEnabled(newVertScale != TwoWayView.COUNT);
			}
			return true;
		}
		else if (target == groupingChoice) {
			if (currentGrouping != groupingChoice.getSelectedIndex()) {
				currentGrouping = groupingChoice.getSelectedIndex();
				int newVertScale = TwoWayView.COUNT;
				if (currentFreqPropn == 1)
					newVertScale = (currentGrouping == TwoWayView.XMAIN) ? TwoWayView.PROPN_IN_X
													: TwoWayView.PROPN_IN_Y;
				else if (currentFreqPropn == 2)
					newVertScale = (currentGrouping == TwoWayView.XMAIN) ? TwoWayView.PERCENT_IN_X
													: TwoWayView.PERCENT_IN_Y;
				tableView.setDisplayType(currentGrouping, newVertScale, false);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}