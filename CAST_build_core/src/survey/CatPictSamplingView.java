package survey;

import java.awt.*;

import dataView.*;
import random.RandomInteger;
import images.*;


public class CatPictSamplingView extends CatSamplingView {
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final public int PERSON = 0;
	static final public int BOX = 1;
	static final public int FLOWER = 2;
	
	static final private int kMaxPictWidth = 21;
	static final private int kMaxPictHeight = 32;
	static final private int kBorderGap = 3;
	
	static public boolean loadedPictures = false;
	static public Image box[], flower[], fHead[], fTop[], fBottom[], mHead[], mTop[], mBottom[];
	
	private int itemType;
	private RandomInteger generator3, generator4;
	private int boxType[], flowerType[], sexType[], headType[], topType[], bottomType[];
	
	static Image[] loadImages(String coreName, int noOfPicts, MediaTracker tracker, XApplet applet) {
		Image[] result = new Image[noOfPicts];
		for (int i=0 ; i<noOfPicts ; i++) {
			String fileName = "sampling/" + coreName + i + ".gif";
			result[i] = CoreImageReader.getImage(fileName);
			tracker.addImage(result[i], 0);
		}
		return result;
	}
	
	public CatPictSamplingView(DataSet theData, XApplet applet,
						int sampleSize, long randomSeed, long popnRandomSeed, int rows, int cols) {
		super(theData, applet, sampleSize, randomSeed, rows, cols);
		CatVariable v = getCatVariable();
		generator3 = new RandomInteger(0, 2, v.noOfValues());
		generator3.setSeed(popnRandomSeed);
		generator4 = new RandomInteger(0, 3, v.noOfValues());
		generator4.setSeed(popnRandomSeed + 49576225L);
		if (!loadedPictures)
			synchronized(CatPictSamplingView.class) {
				MediaTracker tracker = new MediaTracker(applet);
				
				box = loadImages("box", 4, tracker, applet);
				flower = loadImages("flower", 3, tracker, applet);
				fHead = loadImages("fHead", 3, tracker, applet);
				fTop = loadImages("fTop", 3, tracker, applet);
				fBottom = loadImages("fBottom", 3, tracker, applet);
				mHead = loadImages("mHead", 3, tracker, applet);
				mTop = loadImages("mTop", 3, tracker, applet);
				mBottom = loadImages("mBottom", 3, tracker, applet);
				
				try {
					tracker.waitForAll(kMaxWait);
					loadedPictures = true;
				} catch (InterruptedException e) {
				}
			}
		
		setUnitType(PERSON);
	}
	
	private void generatePopnInfo() {
		switch (itemType) {
			case BOX:
				boxType = generator4.generate();
				break;
			case FLOWER:
				flowerType = generator3.generate();
				break;
			case PERSON:
				sexType = generator3.generate();
				headType = generator3.generate();
				topType = generator3.generate();
				bottomType = generator3.generate();
				break;
		}
	}
	
	public void setUnitType(int newType) {
		itemType = newType;
		generatePopnInfo();
		repaint();
	}
	
	protected Point getTableBorder() {
		return new Point(kBorderGap + kMaxPictWidth / 2, kBorderGap + kMaxPictHeight / 2);
	}
	
	protected Point getOffsetBetweenRows() {
		return new Point(-12, 15);
	}
	
	protected Point getOffsetWithinRow() {
		return new Point(18, 4);
	}
	
	protected void drawSymbol(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																								boolean isSucceess) {
		switch (itemType) {
			case BOX:
				g.drawImage(box[boxType[valueIndex]], x - 10, y - 12, this);
				break;
			case FLOWER:
				g.drawImage(flower[flowerType[valueIndex]], x - 10, y - 12, this);
				break;
			case PERSON:
				if (sexType[valueIndex] == 0) {
					g.drawImage(fHead[headType[valueIndex]], x - 4, y - 16, this);
					g.drawImage(fTop[topType[valueIndex]], x - 7, y - 9, this);
					g.drawImage(fBottom[bottomType[valueIndex]], x - 8, y - 1, this);
				}
				else {
					g.drawImage(mHead[headType[valueIndex]], x - 3, y - 16, this);
					g.drawImage(mTop[topType[valueIndex]], x - 8, y - 9, this);
					g.drawImage(mBottom[bottomType[valueIndex]], x - 6, y + 1, this);
				}
				break;
		}
	}
}
	
