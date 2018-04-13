package randomStatProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class MeanDistnControlApplet extends XApplet {
	private final static String MEAN_APPLET_PARAM = "meanDistnApplet";
	
	private XCheckbox showTheoryCheck;
	
	public void setupApplet() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		showTheoryCheck = new XCheckbox("Show Theoretical Distn", this);
		add(showTheoryCheck);
	}
	
	private boolean localAction(Object target) {
		if (target == showTheoryCheck) {
			String meanAppletName = getParameter(MEAN_APPLET_PARAM);
			SampleMean2Applet theMeanApplet = (SampleMean2Applet)getApplet(meanAppletName);
			theMeanApplet.setTheoryShow(showTheoryCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}