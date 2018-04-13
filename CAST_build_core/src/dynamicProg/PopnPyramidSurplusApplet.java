package dynamicProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import dynamic.*;


public class PopnPyramidSurplusApplet extends PopnPyramidApplet {
	
	private XCheckbox showSurplusCheck;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(FREQ_AXIS_PARAM));
			int classWidth = Integer.parseInt(st.nextToken());
			int freqMax = Integer.parseInt(st.nextToken());
			int axisMax = Integer.parseInt(st.nextToken());
			int axisStep = Integer.parseInt(st.nextToken());
			LabelValue freqLabel = new LabelValue(getParameter(FREQ_AXIS_NAME_PARAM));
		
			theView = new PyramidSurplusView(data, this, "left", "right", classWidth, freqMax, axisMax, axisStep, freqLabel);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			showSurplusCheck = new XCheckbox("Show gender surplus", this);
		thePanel.add(showSurplusCheck);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == showSurplusCheck) {
			((PyramidSurplusView)theView).setShowSurplus(showSurplusCheck.getState());
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