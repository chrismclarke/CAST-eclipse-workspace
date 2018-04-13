package dynamicProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import dynamic.*;


public class PopnPyramidLagApplet extends PopnPyramidApplet {
	private int classWidth;
	
	private XCheckbox showLagCheck;
	private XCheckbox percentageCheck;
	
	private XChoice lagChoice;
	private int currentLagIndex = 0;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(FREQ_AXIS_PARAM));
			classWidth = Integer.parseInt(st.nextToken());
			int freqMax = Integer.parseInt(st.nextToken());
			int axisMax = Integer.parseInt(st.nextToken());
			int axisStep = Integer.parseInt(st.nextToken());
			LabelValue freqLabel = new LabelValue(getParameter(FREQ_AXIS_NAME_PARAM));
		
			theView = new PyramidLagView(data, this, "left", "right", classWidth, freqMax, axisMax, axisStep, freqLabel);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			showLagCheck = new XCheckbox("Show cohort sizes...", this);
		thePanel.add(showLagCheck);
				
			lagChoice = new XChoice(this);
			for (int i=0 ; i<4 ; i++)
				lagChoice.add(((i + 1) * classWidth) + " years ago");
			lagChoice.disable();
		thePanel.add(lagChoice);
		
			percentageCheck = new XCheckbox("Show percentage", this);
			percentageCheck.disable();
		thePanel.add(percentageCheck);
		
		return thePanel;
	}
	
	protected void frameChanged(DataView theView) {
		PyramidLagView pyramid = (PyramidLagView)theView;
		if (pyramid.getCurrentFrame() == 0 || pyramid.getCurrentFrame() == PyramidLagView.kFinalFrame) {
			if (showLagCheck.getState())
				percentageCheck.enable();
		}
	}
	
	private boolean localAction(Object target) {
		if (target == showLagCheck) {
			if (showLagCheck.getState()) {
				lagChoice.enable();
				percentageCheck.enable();
				((PyramidLagView)theView).setLag(lagChoice.getSelectedIndex() + 1);
			}
			else {
				lagChoice.disable();
				percentageCheck.disable();
				((PyramidLagView)theView).setLag(0);
				((PyramidLagView)theView).setShowPercentage(false);
			}
			theView.repaint();
			
			return true;
		}
		else if (target == lagChoice) {
			int newChoice = lagChoice.getSelectedIndex();
			if (newChoice != currentLagIndex) {
				currentLagIndex = newChoice;
				
				((PyramidLagView)theView).setLag(newChoice + 1);
				theView.repaint();
			}
			return true;
		}
		else if (target == percentageCheck) {
			percentageCheck.disable();
			((PyramidLagView)theView).animateShowPercentage(percentageCheck.getState());
			
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