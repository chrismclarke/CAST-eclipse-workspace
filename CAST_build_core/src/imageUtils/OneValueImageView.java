package imageUtils;

import java.awt.*;

import dataView.*;
import valueList.*;
import images.*;


public class OneValueImageView extends OneValueView {
	
	static final private int kMaxWait = 30000;		//		30 seconds
	static final private int kImageValueGap = 4;
	
	private Image image;
	private int imageAscent;
	
	public OneValueImageView(DataSet theData, String variableKey, XApplet applet,
																			String imageName, int imageAscent, Value maxValue) {
		super(theData, variableKey, applet, maxValue);
		image = CoreImageReader.getImage(imageName);
		MediaTracker tracker = new MediaTracker(applet);
		tracker.addImage(image, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		this.imageAscent = imageAscent;
	}
	
	public OneValueImageView(DataSet theData, String variableKey, XApplet applet,
																											String imageName, int imageAscent) {
		this(theData, variableKey, applet, imageName, imageAscent, null);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return image.getWidth(this) + kImageValueGap;
	}
	
	protected int getLabelAscent(Graphics g) {
		return imageAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return image.getHeight(this) - imageAscent;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawImage(image, startHoriz, baseLine - imageAscent, this);
	}
}
