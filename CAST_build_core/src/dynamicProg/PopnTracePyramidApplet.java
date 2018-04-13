package dynamicProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import dynamic.*;


public class PopnTracePyramidApplet extends PopnPyramidApplet {
	private XChoice baseYearChoice;
	private int currentBaseIndex = 0;
	
	private XCheckbox currentYearCheck;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(FREQ_AXIS_PARAM));
			int classWidth = Integer.parseInt(st.nextToken());
			int freqMax = Integer.parseInt(st.nextToken());
			int axisMax = Integer.parseInt(st.nextToken());
			int axisStep = Integer.parseInt(st.nextToken());
			LabelValue freqLabel = new LabelValue(getParameter(FREQ_AXIS_NAME_PARAM));
		
			theView = new PyramidTraceCohortView(data, this, "left", "right", classWidth, freqMax, axisMax, axisStep, freqLabel);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected String yearSliderName() {
		return null;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
			currentYearCheck = new XCheckbox("Show actual population", this);
		thePanel.add(currentYearCheck);
		
			XPanel basePanel = new XPanel();
			basePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XLabel baseLabel = new XLabel("Base year =", XLabel.LEFT, this);
				baseLabel.setFont(getStandardBoldFont());
			basePanel.add(baseLabel);
			
				baseYearChoice = new XChoice(this);
				for (int i=startYear ; i<=endYear ; i+= yearStep)
					baseYearChoice.addItem(String.valueOf(i));
			basePanel.add(baseYearChoice);
			
		thePanel.add(basePanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
			yearSlider.setAddEquals(false);
			yearSlider.setTitle("Shift cohorts to ", this);
		
		return thePanel;
	}
	
	protected void yearIndexChange() {
		double newYear = yearSlider.getYear();
		int baseIndex = baseYearChoice.getSelectedIndex();
		int baseYear = startYear + baseIndex * yearStep;
		
		if (newYear < baseYear)
			yearSlider.setYear(baseYear);
		else
			super.yearIndexChange();
	}
	
	private boolean localAction(Object target) {
		if (target == baseYearChoice) {
			int newChoice = baseYearChoice.getSelectedIndex();
			if (newChoice != currentBaseIndex) {
				currentBaseIndex = newChoice;
				int newBaseYear = startYear + newChoice * yearStep;
				int year = (int)Math.round(yearSlider.getYear());
				if (year < newBaseYear)
					yearSlider.setYear(newBaseYear);
				
				((PyramidTraceCohortView)theView).setBaseYearIndex(currentBaseIndex);
				theView.repaint();
			}
			return true;
		}
		else if (target == currentYearCheck) {
			((PyramidTraceCohortView)theView).setShowCurrentYear(currentYearCheck.getState());
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