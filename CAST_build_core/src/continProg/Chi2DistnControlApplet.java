package continProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class Chi2DistnControlApplet extends XApplet {
	private final static String CHI2_APPLET_PARAM = "chi2DistnApplet";
	
	private XCheckbox showTheoryCheck;
	
	public void setupApplet() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		showTheoryCheck = new XCheckbox("Show Theoretical Distn", this);
		add(showTheoryCheck);
	}
	
	private boolean localAction(Object target) {
		if (target == showTheoryCheck) {
			String chi2AppletName = getParameter(CHI2_APPLET_PARAM);
			Chi2DistnApplet theApplet = (Chi2DistnApplet)getApplet(chi2AppletName);
			theApplet.setTheoryShow(showTheoryCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}