package structure;

import java.awt.*;

import dataView.*;
import random.RandomInteger;
import images.*;


public class PeopleView extends DataView {
	
	static final public int WHITE = 0;
	static final public int BLACK = 1;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private int kMaxPictWidth = 25;
	static final private int kMaxPictHeight = 43;
	static final private int kMinBorder = 3;
	
	static public boolean loadedPictures = false;
	static public Image mHead[], mTop[], mBottom[];
	
	private RandomInteger generator3;
	private int headType[], topType[], bottomType[];
	
	private boolean initialised = false;
	
	static Image[] loadImages(String coreName, int noOfPicts, MediaTracker tracker, XApplet applet) {
		Image[] result = new Image[noOfPicts];
		for (int i=0 ; i<noOfPicts ; i++) {
			String fileName = "sampling/" + coreName + i + ".gif";
			result[i] = CoreImageReader.getImage(fileName);
			tracker.addImage(result[i], 0);
		}
		return result;
	}
	
	public PeopleView(DataSet theData, XApplet applet, long popnRandomSeed, int noOfValues,
																																								int colour) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		if (!loadedPictures) {
			MediaTracker tracker = new MediaTracker(applet);
			
			mHead = loadImages(colour == WHITE ? "mHead" : "amHead", 3, tracker, applet);
			mTop = loadImages(colour == WHITE ? "mTop" : "amTop", 3, tracker, applet);
			mBottom = loadImages("mBottom", 3, tracker, applet);
			
			try {
				tracker.waitForAll(kMaxWait);
				loadedPictures = true;
			} catch (InterruptedException e) {
			}
			loadedPictures = true;
		}
		
		generator3 = new RandomInteger(0, 2, noOfValues);
		generator3.setSeed(popnRandomSeed);
		headType = generator3.generate();
		topType = generator3.generate();
		bottomType = generator3.generate();
	}
	
	private int pictHeight, pictWidth;
	private int nRows, nCols;
	private int topMargin, leftMargin;
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		int noOfValues = headType.length;
		pictHeight = kMaxPictHeight;
		pictWidth = kMaxPictWidth;
		while (true) {
			nRows = (getSize().height - 2 * kMinBorder) / pictHeight;
			nCols = (getSize().width - 2 * kMinBorder) / pictWidth;
			if (nRows * nCols >= noOfValues)
				break;
			pictHeight --;
			pictWidth --;
		}
		
		int htUsed = nRows * pictHeight;
		int widthUsed = ((noOfValues - 1) / nRows + 1) * pictWidth;
		topMargin = (getSize().height - htUsed) / 2;
		leftMargin = (getSize().width - widthUsed) / 2;
		
		initialised = true;
		return true;
	}
	
	protected void drawSymbol(Graphics g, int valueIndex, boolean selected) {
		int left = leftMargin + pictWidth * (valueIndex / nRows);
		int top = topMargin + pictHeight * (valueIndex % nRows);
		
		g.setColor(selected ? Color.yellow : Color.white);
		g.fillRect(left, top, pictWidth, pictHeight);
		g.setColor(getForeground());
		
		int x = left + pictWidth / 2;
		int y = top + pictHeight / 2;
		g.drawImage(mHead[headType[valueIndex]], x - 3, y - 16, this);
		g.drawImage(mTop[topType[valueIndex]], x - 8, y - 9, this);
		g.drawImage(mBottom[bottomType[valueIndex]], x - 6, y + 1, this);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while(fe.hasMoreFlags()) {
			drawSymbol(g, index, fe.nextFlag());
			index++;
		}
	}


//-----------------------------------------------------------------------------------
		
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y < topMargin || y >= topMargin + pictHeight * nRows || x < leftMargin)
			return null;
		
		int row = (y - topMargin) / pictHeight;
		int col = (x - leftMargin) / pictWidth;
		
		int hitIndex = col * nRows + row;
		Flags selection = getSelection();
		return (hitIndex >= selection.getNoOfFlags()) ? null : new IndexPosInfo(hitIndex);
	}
}
	
