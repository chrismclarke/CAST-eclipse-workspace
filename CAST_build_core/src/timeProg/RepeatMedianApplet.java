package timeProg;

import java.awt.*;

import dataView.*;

import time.*;


class MedianCountSlider extends XValueAdjuster {
	public MedianCountSlider(String title, int maxRepeat, XApplet applet) {
		super(title, 1, maxRepeat, 1, applet);
	}
	
	protected Value translateValue(int scrollValue) {
		return new NumValue(scrollValue, 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
}

public class RepeatMedianApplet extends BasicTimeApplet {
	static final private String SMOOTH_VAR_NAME_PARAM = "smoothName";
	static final private String SHOW_ENDS_PARAM = "showEnds";
	
	private RepeatMedianVariable theSmoothedVariable;
	private MedianCountSlider theCountSlider;
	private MedianRunSlider theMedianSlider;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String showEndsString = getParameter(SHOW_ENDS_PARAM);
		boolean showEnds = (showEndsString == null) || showEndsString.equals("true");
		theSmoothedVariable = new RepeatMedianVariable(getParameter(SMOOTH_VAR_NAME_PARAM),
																							data, "y", showEnds);
		data.addVariable("smooth", theSmoothedVariable);
		return data;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"smooth"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return true;
	}
	
	protected boolean showSmoothedValue() {
		return true;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel sliderPanel = new XPanel();
		sliderPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 3));
		
		theMedianSlider = new MedianRunSlider("Running median of", 9, this);
		sliderPanel.add(theMedianSlider);
		theCountSlider = new MedianCountSlider("Repeat count =", 5, this);
		sliderPanel.add(theCountSlider);
		
		thePanel.add("Center", sliderPanel);
		
		XPanel checkPanel = new XPanel();
		checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 3));
		checkPanel.add(createShadingCheck());
		
		thePanel.add("South", checkPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == theMedianSlider) {
			theSmoothedVariable.setMedianRun(theMedianSlider.getRunLength());
			return true;
		}
		else if (target == theCountSlider) {
			int value = theCountSlider.getValue();
			theSmoothedVariable.setMedianCount(value);
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