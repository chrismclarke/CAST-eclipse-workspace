package imageUtils;

import java.awt.*;

import dataView.*;
import images.*;


public class ImageSwapCanvas extends XPanel {
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private int versionNo = 0;
	
	private Image picture[];
	@SuppressWarnings("unused")
	private int imageWidth, imageHeight;
	
	public ImageSwapCanvas(String[] pictName, XApplet theApplet) {
		picture = new Image[pictName.length];
		MediaTracker tracker = new MediaTracker(theApplet);
		for (int i=0 ; i<pictName.length ; i++) {
			picture[i] = CoreImageReader.getImage(pictName[i]);
			tracker.addImage(picture[i], 0);
		}
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		if (imageWidth != 0 && picture[0] != null)
			setSize(picture[0].getWidth(this), picture[0].getHeight(this));
	}
	
	public ImageSwapCanvas(String[] pictName, XApplet theApplet,
																		int imageWidth, int imageHeight) {
		this(pictName, theApplet);
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		
		setSize(imageWidth, imageHeight);
	}
	
	public ImageSwapCanvas(Image[] picture, int imageWidth, int imageHeight, XApplet theApplet) {
		this.picture = picture;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		
		setSize(imageWidth, imageHeight);
	}
	
	public Dimension getPreferredSize() {
		return getSize();
	}
	
	public Dimension getMinimumSize() {
		return getSize();
	}
		
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (versionNo < 0) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getSize().width, getSize().height);
			return;
		}
		
		int pictWidth = picture[versionNo].getWidth(this);
		int pictHeight = picture[versionNo].getHeight(this);
		int left = (getSize().width - pictWidth) / 2;
		int top = (getSize().height - pictHeight) / 2;

		g.setColor(getBackground());
		if (left > 0)
			g.fillRect(0, 0, left, getSize().height);
		if (left + pictWidth < getSize().width)
			g.fillRect(left + pictWidth, 0, getSize().width - left - pictWidth, getSize().height);
		if (top > 0)
			g.fillRect(0, 0, getSize().width, top);
		if (top + pictHeight < getSize().height)
			g.fillRect(0, top + pictHeight, getSize().width, getSize().height - top - pictHeight);
		
		g.drawImage(picture[versionNo], left, top, this);
	}
	
	public void showVersion(int versionNo) {		//		-1 means don't show image
		if (this.versionNo != versionNo) {
			this.versionNo = versionNo;
			repaint();
		}
	}
}