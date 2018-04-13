package designProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class ExperControlApplet extends XApplet {
	private final static String EXPER_APPLET_PARAM = "experName";
	private final static String PICT_CHECK_PARAM = "pictCheckName";
	
	private XCheckbox showPictureCheck;
	
	public void setupApplet() {
		setLayout(new FlowLayout(FlowLayout.CENTER));
		showPictureCheck = new XCheckbox(getParameter(PICT_CHECK_PARAM), this);
		add(showPictureCheck);
	}
	
	private boolean localAction(Object target) {
		String experAppletName = getParameter(EXPER_APPLET_PARAM);
		ExperDesignApplet theExperApplet = (ExperDesignApplet)getApplet(experAppletName);
		if (target == showPictureCheck) {
			theExperApplet.setShowPicture(showPictureCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}