package bivarCatProg;

import java.awt.*;

import dataView.*;
import bivarCat.*;


public class ContinTableApplet extends Core2WayApplet {
	protected ContinTableView tableView = null;
	
	protected void addDisplayComponents(DataSet data) {
		setLayout(new BorderLayout(10, 0));
	
		add("North", displayPanel(data));
		
		tableView.setDisplayType(TwoWayView.XMAIN, initialVertScale, false);
		
		add("Center", vertScalePanel(data, "Display: "));
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		String decimalString = getParameter(DECIMALS_PARAM);
		int decimals = (decimalString == null) ? 3 : Integer.parseInt(decimalString);
		
		tableView = new ContinTableView(data, this, "x", "y", decimals, false, true);
		thePanel.add(tableView);
		
		return thePanel;
	}
	
	protected void changeVertScale(int newVertScale) {
		if (!canScaleCount)
			newVertScale ++;
		if (!canScalePropnInX && newVertScale == 1)
			newVertScale += 2;
		if (tableView != null && newVertScale != tableView.getVertScale())
			tableView.setDisplayType(tableView.getMainGrouping(), newVertScale, false);
	}
}