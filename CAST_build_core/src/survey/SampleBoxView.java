package survey;

import java.awt.*;

import dataView.*;
import random.RandomInteger;


public class SampleBoxView extends SamplePictView {
	static final protected Color kPaleGreen = new Color(0xDDFFDD);
	
	
	public SampleBoxView(DataSet theData, XApplet applet,
						int sampleSize, long popnRandomSeed, long randomSeed, int rows, int cols,
						int rowCycle, int maxHorizOffset, int maxVertOffset) {
		super(theData, applet, sampleSize, popnRandomSeed, randomSeed,
											rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
	}
	
	protected PeopleImages loadPictures(XApplet applet) {
		int boxWidth = 39;
		int boxHeight = 36;
		
		int noOfOptions[] = {3, 3, 1};
		String optionName[] = {"sqrBox", "sqrRoundBox", "tallBox", "cylinder"};
		
		return new PeopleImages(optionName, noOfOptions, boxWidth, boxHeight, applet);
	}
	
	protected void setupPictureGenerators(long popnRandomSeed) {
		CatVariable v = getCatVariable();
		generator = new RandomInteger[3];
		
		generator[0] = new RandomInteger(0, 3, v.noOfValues());
		generator[0].setSeed(popnRandomSeed + 29576225L);
		
		for (int i=0 ; i<2 ; i++) {
			generator[i + 1] = new RandomInteger(0, theImages.noOfOptions[i] - 1, v.noOfValues());
			generator[i + 1].setSeed(popnRandomSeed + (i + 1) * 49576225L);
		}
		
		type = new int[3][];
	}
	
	protected void drawSymbol(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																								boolean isSucceess) {
		if (isSucceess) {
			g.setColor(dimmed ? kPink : Color.red);
			g.drawLine(x - 7, y - 7, x + 7, y + 7);
			g.drawLine(x - 7, y - 6, x + 6, y + 7);
			g.drawLine(x - 6, y - 7, x + 7, y + 6);
			
			g.drawLine(x - 7, y + 7, x + 7, y - 7);
			g.drawLine(x - 7, y + 6, x + 6, y - 7);
			g.drawLine(x - 6, y + 7, x + 7, y - 6);
		}
		else {
			g.setColor(dimmed ? kPaleGreen : Color.green);
			g.drawLine(x - 7, y + 10, x + 12, y - 9);
			g.drawLine(x - 7, y + 9, x + 11, y - 9);
			g.drawLine(x - 6, y + 10, x + 12, y - 8);
			
			g.drawLine(x - 12, y + 3, x - 6, y + 9);
			g.drawLine(x - 12, y + 4, x - 7, y + 9);
			g.drawLine(x - 11, y + 3, x - 6, y + 8);
		}
	}
	
	protected void drawPicture(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																								boolean isSuccess) {
		int mainIndex = type[0][valueIndex];
		
		int left = x - theImages.personWidth / 2;
		if (horizOffset != null)
			left += horizOffset[valueIndex];
		int top = y - theImages.personHeight / 2;
		if (vertOffset != null)
			top += vertOffset[valueIndex];
		
		for (int i=0 ; i<2 ; i++) {
			int pictVersionI = type[i + 1][valueIndex];
			g.drawImage(theImages.pict[mainIndex][i][pictVersionI][dimmed ? 1 : 0], left, top, this);
		}
		
		if (isSuccess)
			g.drawImage(theImages.pict[mainIndex][2][0][dimmed ? 1 : 0], left, top, this);
	}
}
	
