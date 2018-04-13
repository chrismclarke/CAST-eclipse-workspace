package histoProg;

import java.awt.*;

import dataView.*;
import utils.*;
import histo.*;


public class HistoRandomApplet extends ShiftClassHistoApplet {
	
	private XButton takeSampleButton;
	
	protected int initialBarType() {
		return HistoView.NO_BARS;
	}
	
	protected XPanel createControls() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 2));
		
			XPanel histoControlPanel = new XPanel();
			histoControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
			histoControlPanel.add(widthControls());
			histoControlPanel.add(shiftControls());
		
		thePanel.add(histoControlPanel);
		
			takeSampleButton = new XButton(translate("Another sample"), this);
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			double vals[] = generateData();
			data.getNumVariable().setValues(vals);
			data.variableChanged("y");
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