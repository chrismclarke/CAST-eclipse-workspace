package propnVenn;

import java.awt.*;

import dataView.*;
import utils.*;
import imageUtils.*;

import contin.*;


public class MarginConditPanel extends XPanel {
	static public int HORIZ_AXIS = 0;
	static public int VERT_AXIS = 1;
	
	static public boolean MARGINAL = true;
	static public boolean CONDITIONAL = false;
	
	private int axisType;
	protected boolean marginNotCondit;
	protected boolean isFinite;
	private XApplet applet;
	
	protected ImageCanvas theImage;
	protected XLabel theLabel;
	private String varName;
	
	public MarginConditPanel(int axisType, String varName, boolean marginNotCondit, boolean isFinite, XApplet applet) {
		this.axisType = axisType;
		this.varName = varName;
		this.marginNotCondit = marginNotCondit;
		this.isFinite = isFinite;
		this.applet = applet;
		
		setLayout(new BorderLayout(0, 0));
		if (axisType == HORIZ_AXIS)
			if (varName == null || !BufferedCanvas.isJavaUpToDate) {
				theImage = new ImageCanvas(ContinImages.xConditLabel, ContinImages.kXLabelWidth, ContinImages.kXLabelHeight, applet);
				setImage();
				add(theImage);
			}
			else {
				theLabel = new XLabel("", XLabel.CENTER, applet);
				theLabel.setFont(applet.getBigBoldFont());
				setLabelText();
				add(theLabel);
			}
		else
			if (varName == null || !BufferedCanvas.isJavaUpToDate) {
				theImage = new ImageCanvas(ContinImages.yMarginLabel, ContinImages.kYLabelWidth, ContinImages.kYLabelHeight, applet);
				setImage();
				add(theImage);
			}
			else {
				theLabel = new XVertLabel("", XLabel.CENTER, applet);
				theLabel.setFont(applet.getBigBoldFont());
				setLabelText();
				add(theLabel);
			}
	}
	
	protected void setLabelText() {
		if (marginNotCondit)
			if (isFinite)
				theLabel.setText(applet.translate("Marginal propn for") + " " + varName);
			else
				theLabel.setText(applet.translate("Marginal prob for") + " " + varName);
		else
			if (isFinite)
				theLabel.setText(applet.translate("Conditional propn for") + " " + varName);
			else
				theLabel.setText(applet.translate("Conditional prob for") + " " + varName);
	}
	
	protected void setImage() {
		if (axisType == HORIZ_AXIS)
			theImage.setImage(marginNotCondit ? ContinImages.xMarginLabel : ContinImages.xConditLabel);
		else
			theImage.setImage(marginNotCondit ? ContinImages.yMarginLabel : ContinImages.yConditLabel);
	}
	
	public void changeMarginNotCondit(boolean marginNotCondit) {
		if (this.marginNotCondit == marginNotCondit)
			return;
		this.marginNotCondit = marginNotCondit;
		
		if (theImage == null)
			setLabelText();
		else
			setImage();
	}
}
