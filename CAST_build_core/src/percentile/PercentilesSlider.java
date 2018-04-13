package percentile;

import java.awt.*;

import dataView.*;
import utils.*;


public class PercentilesSlider extends XSlider {
//	static final private int kStepMax = 100;
	
	private GroupedPercentileView view;
	
	public PercentilesSlider(XApplet applet, GroupedPercentileView view) {
		super("0-5-50-95-100", "0-45-50-55-100", "Percentiles: ", 1, 9, 5, applet);
		this.view = view;
	}
	
	public void setBoxValue() {
		setValue(5);
	}
	
	protected Value translateValue(int val) {
		return new LabelValue("0-" + String.valueOf((val * 5) + "-50-" + (100 - val * 5)) + "-100");
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth("0-45-50-55-100");
	}

	
	private boolean localAction(Object target) {
		view.setPercentiles(getValue() * 0.05);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}