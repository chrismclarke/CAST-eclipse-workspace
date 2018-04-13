package timeProg;

import java.awt.*;

import dataView.*;

import time.*;


class MedianRunSlider extends XValueAdjuster {
	public MedianRunSlider(String title, int maxRun, XApplet applet) {
		super(title, 1, maxRun / 2 + 1, 1, applet);
	}
	
	protected Value translateValue(int scrollValue) {
		return new NumValue(scrollValue * 2 - 1, 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
	
	public int getRunLength(int scrollValue) {
		return scrollValue * 2 - 1;
	}
	
	public int getRunLength() {
		return getRunLength(getValue());
	}
}

public class RunningMedianApplet extends BasicTimeApplet {
	static final private String SMOOTH_VAR_NAME_PARAM = "smoothName";
	static final private String SHOW_ENDS_PARAM = "showEnds";
	
	private MeanMedianVariable theMedianVariable;
	private MedianRunSlider theRunSlider;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String showEndsString = getParameter(SHOW_ENDS_PARAM);
		boolean showEnds = (showEndsString == null) || showEndsString.equals("true");
		theMedianVariable = new MeanMedianVariable(getParameter(SMOOTH_VAR_NAME_PARAM),
																							data, "y", showEnds);
		data.addVariable("smooth", theMedianVariable);
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
		XPanel thePanel = super.controlPanel(data);
		
//		thePanel.setLayout(new BorderLayout(10, 3));
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 3));
		
		theRunSlider = new MedianRunSlider("Running median of", 9, this);
		thePanel.add(theRunSlider);
		thePanel.add(createShadingCheck());
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == theRunSlider) {
			theMedianVariable.setMedianRun(theRunSlider.getRunLength());
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