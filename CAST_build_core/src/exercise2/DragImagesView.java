package exercise2;

import java.awt.*;

import dataView.*;

import images.*;


public class DragImagesView extends CoreDragItemsView {
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final public int IMAGES = TEXT_LABELS + 1;
	
	private Image images[];
	
	public DragImagesView(DataSet theData, XApplet applet, String[] imageName, int[] order) {
		super(theData, applet, order, IMAGES, new Insets(0,5,0,5));
		
		images = new Image[imageName.length];
		MediaTracker tracker = new MediaTracker(this);
		for (int i=0 ; i<imageName.length ; i++) {
			images[i] = CoreImageReader.getImage(imageName[i]);
			tracker.addImage(images[i], 0);
		}
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}

//----------------------------------------------------------------
	
	protected int noOfItems() {
		return images.length;
	}
	
	protected void drawBackground(Graphics g) {
	}
	
	protected String getItemName(int index) {
		return "";
	}
	
	protected void drawOneItem(Graphics g, int index, int baseline, int height) {
		int imageWidth = images[index].getWidth(this);
		int imageHeight = images[index].getHeight(this);
		int width = getSize().width;
		
		int imageTop = translateToScreen(0, baseline + (height + imageHeight) / 2, null).y;
		
		g.drawImage(images[index], (width - imageWidth) / 2, imageTop, this);
	}

//----------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		int maxWidth = 0;
		int maxHeight = 0;
		for (int i=0 ; i<images.length ; i++) {
			maxWidth = Math.max(maxWidth, images[i].getWidth(this));
			maxHeight = Math.max(maxHeight, images[i].getHeight(this));
		}
		return new Dimension(maxWidth, images.length * maxHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}