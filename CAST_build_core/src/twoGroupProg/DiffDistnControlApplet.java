package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class DiffDistnControlApplet extends XApplet {
	private final static String DIFF_APPLET_PARAM = "diffDistnApplet";
	
	private XCheckbox showTheoryCheck;
	
	public void setupApplet() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		showTheoryCheck = new XCheckbox("Show Theoretical Distn", this);
		add(showTheoryCheck);
	}
	
	private boolean localAction(Object target) {
		if (target == showTheoryCheck) {
			String diffAppletName = getParameter(DIFF_APPLET_PARAM);
			CoreDiffApplet theDiffApplet = (CoreDiffApplet)getApplet(diffAppletName);
			theDiffApplet.setShowTheory(showTheoryCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}