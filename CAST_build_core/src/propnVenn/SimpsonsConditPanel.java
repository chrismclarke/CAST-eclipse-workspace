package propnVenn;

import dataView.*;

import contin.*;


public class SimpsonsConditPanel extends MarginConditPanel {
	private boolean initialised = false;
	
	private String zName, yName;
	private XApplet applet;
	
	public SimpsonsConditPanel(String zName, String yName, boolean marginNotCondit, boolean isFinite, XApplet applet) {
		super(HORIZ_AXIS, "", marginNotCondit, isFinite, applet);
		this.zName = zName;
		this.yName = yName;
		this.applet = applet;
		
		initialised = true;
		
		if (theImage == null)
			setLabelText();
		else
			setImage();
	}
	
	protected void setLabelText() {
		if (!initialised)				//	because setLabelText is called by MarginConditPanel.init()
														//	before zName or yName have been initialised
			return;
		
		if (marginNotCondit)
			if (isFinite)
				theLabel.setText(applet.translate("Conditional propn for") + " " + zName);
			else
				theLabel.setText(applet.translate("Conditional prob for") + " " + zName);
		else
			if (isFinite)
				theLabel.setText(applet.translate("Conditional propn for") + " " + zName + " & " + yName);
			else
				theLabel.setText(applet.translate("Conditional prob for") + " " + zName + " & " + yName);
	}
	
	protected void setImage() {
		theImage.setImage(ContinImages.xConditLabel);
	}
}
