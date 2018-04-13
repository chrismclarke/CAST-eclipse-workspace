package histoProg;

import java.awt.*;

import dataView.*;
import utils.*;
import histo.*;
import valueList.OneValueView;


public class HistoSwap3Applet extends HistoSwap2Applet {
	private XCheckbox horizLinesCheck;
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		groupingChoice = new XChoice(this);
		LabelEnumeration theValues = new LabelEnumeration(getParameter(GROUPING_NAMES_PARAM));
		groupingChoice.addItem((String)theValues.nextElement());
		groupingChoice.addItem((String)theValues.nextElement());
		groupingChoice.addItem((String)theValues.nextElement());
		controlPanel.add(groupingChoice);
		groupingChoice.select(1);						//	standard grouping
		
		horizLinesCheck = new XCheckbox("Box Values", this);
		horizLinesCheck.setState(false);
		controlPanel.add(horizLinesCheck);
		
		OneValueView theValue = new OneValueView(data, "y", this);
		controlPanel.add(theValue);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == horizLinesCheck) {
			int newBoxing = horizLinesCheck.getState() ? HistoView.BOTH_BARS : HistoView.VERT_BARS;
			theHisto.setBarType(newBoxing);
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