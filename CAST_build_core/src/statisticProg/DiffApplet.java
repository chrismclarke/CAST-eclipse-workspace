package statisticProg;

import java.awt.*;

import dataView.*;
import utils.*;
import statistic.*;


public class DiffApplet extends DragAxisApplet {
	private XCheckbox showTotalCheck;
	
	protected Statistic getLowStatistic() {
		return new AbsDiffStatistic(Statistic.LOW, Statistic.SHOW_GRAPH, Statistic.SHOW_EQN, this);
	}
	
	protected Statistic getHighStatistic() {
		return new AbsDiffStatistic(Statistic.HIGH, Statistic.SHOW_GRAPH, Statistic.SHOW_EQN, this);
	}
	
	protected Statistic getTotalStatistic() {
		return new DiffStatistic(Statistic.ALL, Statistic.NO_SHOW_GRAPH, Statistic.NO_SHOW_EQN, this);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		showTotalCheck = new XCheckbox("Show Total", this);
		showTotalCheck.setState(false);
		thePanel.add(showTotalCheck);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == showTotalCheck) {
			boolean showTotal = showTotalCheck.getState();
			lowStatistic.setEqnVisibility(!showTotal);
			highStatistic.setEqnVisibility(!showTotal);
			totalStatistic.setEqnVisibility(showTotal);
			lowStatistic.setGraphVisibility(!showTotal);
			highStatistic.setGraphVisibility(!showTotal);
			totalStatistic.setGraphVisibility(showTotal);
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