package imageUtils;

import java.awt.*;

import dataView.*;
import valueList.*;
import images.*;


abstract public class ValueImageView extends ValueView {
	static final private int kMaxWait = 30000;		//		30 seconds
	static final private int kImageValueGap = 4;
	
	private Image image;
	private int imageAscent;
	
	public ValueImageView(DataSet theData, XApplet applet, String imageName, int imageAscent) {
		super(theData, applet);
		
		if (imageName != null) {
			image = CoreImageReader.getImage(imageName);
			MediaTracker tracker = new MediaTracker(applet);
			tracker.addImage(image, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
			this.imageAscent = imageAscent;
		}
	}

//--------------------------------------------------------------------------------
	
	abstract protected int getMaxValueWidth(Graphics g);
	abstract protected String getValueString();
	abstract protected boolean highlightValue();

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return (image == null) ? 0 : image.getWidth(this) + kImageValueGap;
	}
	
	protected int getLabelAscent(Graphics g) {
		return imageAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return (image == null) ? 0 : image.getHeight(this) - imageAscent;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (image != null)
			g.drawImage(image, startHoriz, baseLine - imageAscent, this);
	}
}
