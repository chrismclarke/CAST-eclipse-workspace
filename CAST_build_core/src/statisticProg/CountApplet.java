package statisticProg;

import java.awt.*;

import dataView.*;
import utils.*;
import statistic.*;


public class CountApplet extends DragAxisApplet {
	private XCheckbox showGraphCheck;
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		showGraphCheck = new XCheckbox("Show Graph", this);
		showGraphCheck.setState(false);
		thePanel.add(showGraphCheck);
		return thePanel;
	}
	
	protected Statistic getLowStatistic() {
		return new CountStatistic(Statistic.LOW, Statistic.NO_SHOW_GRAPH, Statistic.SHOW_EQN, this);
	}
	
	protected Statistic getHighStatistic() {
		return new CountStatistic(Statistic.HIGH, Statistic.NO_SHOW_GRAPH, Statistic.SHOW_EQN, this);
	}
	
	protected Statistic getTotalStatistic() {
		return null;
	}
	
	private boolean localAction(Object target) {
		if (target == showGraphCheck) {
			boolean showGraph = showGraphCheck.getState();
			lowStatistic.setGraphVisibility(showGraph);
			highStatistic.setGraphVisibility(showGraph);
			theView.repaint();
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