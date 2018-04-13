package timeProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class SimpleSeasonalApplet extends SeasonalApplet {
	static final private String EXP_CONST_PARAM = "expConst";
	
	private XChoice componentChoise;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		String constString = getParameter(EXP_CONST_PARAM);
		smoothVariable.setExpSmoothConst((new NumValue(constString)).toDouble());
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 3));
		
		componentChoise = new XChoice(this);
		componentChoise.addItem("Mean component");
		componentChoise.addItem("Trend component (+ mean)");
		componentChoise.addItem("Seasonal component (+ mean)");
		componentChoise.addItem("Residual component (+ mean)");
		componentChoise.addItem("Total of components");
		componentChoise.select(0);
		thePanel.add(componentChoise);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == componentChoise) {
			int selectedIndex = componentChoise.getSelectedIndex();
			synchronized (getData()) {
				smoothVariable.setLinearShow(selectedIndex == 1 || selectedIndex == 4);
				smoothVariable.setExpSmoothShow(selectedIndex == 1 || selectedIndex == 4);
				smoothVariable.setSeasonShow(selectedIndex == 2 || selectedIndex == 4);
				smoothVariable.setResidShow(selectedIndex == 3 || selectedIndex == 4);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}