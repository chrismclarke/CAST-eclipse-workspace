package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class SlopeDistnControlApplet extends XApplet {
	private final static String SLOPE_APPLET_PARAM = "slopeDistnApplet";
	
	private XCheckbox showTheoryCheck;
	
	public void setupApplet() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		showTheoryCheck = new XCheckbox(translate("Show theoretical distn"), this);
		add(showTheoryCheck);
	}
	
	private boolean localAction(Object target) {
		if (target == showTheoryCheck) {
			String meanAppletName = getParameter(SLOPE_APPLET_PARAM);
			XApplet theApplet = (XApplet)getApplet(meanAppletName);
			if (theApplet instanceof SampleSlopeApplet) {
				SampleSlopeApplet theSlopeApplet = (SampleSlopeApplet)theApplet;
				theSlopeApplet.setTheoryShow(showTheoryCheck.getState());
			}
			else {
				PredictionErrorApplet theSlopeApplet = (PredictionErrorApplet)theApplet;
				theSlopeApplet.setTheoryShow(showTheoryCheck.getState());
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}