package dotPlotProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class JitterControlApplet extends XApplet {
	private final static String DOTPLOT_PARAM = "dotPlotName";
	private final static int kSliderMax = 100;
	
	public void setupApplet() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		XPanel buttonPanel = new XPanel();
		newJitterButton = new XButton("Redo jittering", this);
		buttonPanel.add(newJitterButton);
		add("East", buttonPanel);
		
		jitterSlider = new XNoValueSlider("none", "max", "Jittering", 0, kSliderMax, 0, this);
		add("Center", jitterSlider);
	}
	
	private boolean localAction(Object target) {
		String plotAppletName = getParameter(DOTPLOT_PARAM);
		DotJitterApplet dotApplet = (DotJitterApplet)getApplet(plotAppletName);
		
		if (target == newJitterButton) {
			dotApplet.setNewJittering();
			return true;
		}
		else if (target == jitterSlider) {
			int value = jitterSlider.getValue();
			dotApplet.setJitterAmount(((double)value) / kSliderMax);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
	private XButton newJitterButton;
	private XSlider jitterSlider;
}