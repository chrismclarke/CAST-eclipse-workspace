package histoProg;

import java.awt.*;

import dataView.*;
import utils.*;
import histo.*;


public class HistoClassWidthApplet extends ShiftClassHistoApplet {
	
	private XCheckbox verticalHideCheck, dotPlotCheck;
	
	protected int initialBarType() {
		return HistoView.VERT_BARS;
	}
	
	protected XPanel createControls() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 8));
		
			XPanel widthPanel = new XPanel();
			widthPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 24, 0));
			widthPanel.add(widthControls());
			
			widthPanel.add(createClassWidthView());
		thePanel.add(widthPanel);
				
			XPanel verticalsPanel = new XPanel();
			verticalsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			
				dotPlotCheck = new XCheckbox(translate("Show dot plot"), this);
				dotPlotCheck.setState(true);
				theHisto.setShowDotPlot(true);
			verticalsPanel.add(dotPlotCheck);
			
				verticalHideCheck = new XCheckbox(translate("Hide verticals"), this);
				verticalHideCheck.setState(false);
			verticalsPanel.add(verticalHideCheck);
		
		thePanel.add(verticalsPanel);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == verticalHideCheck) {
			theHisto.setBarType(verticalHideCheck.getState()
																? HistoView.NO_BARS : HistoView.VERT_BARS);
			return true;
		}
		else if (target == dotPlotCheck) {
			theHisto.setShowDotPlot(dotPlotCheck.getState());
			theHisto.repaint();
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