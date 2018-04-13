package randomisation;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


abstract public class RandomisationView extends DotPlotView implements RandomisationInterface {
	protected CatVariable randVariable, actualRandVariable;
	
	protected boolean initialised = false;
	
	public RandomisationView(DataSet theData, XApplet applet, NumCatAxis numAxis, String catKey,
								String actualRandKey) {
		super(theData, applet, numAxis, 0.5);
		fixOldInfo();
		actualRandVariable = (CatVariable)theData.getVariable(actualRandKey);
		setActiveCatVariable(catKey);
		randVariable = getCatVariable();
	}
	
	abstract public void fixOldInfo();
	
	public void doAnimation() {
		animateFrames(1, kEndFrame - 1, 40, null);
	}
	
	protected int groupIndex(int itemIndex) {
		return actualRandVariable.getItemCategory(itemIndex) * 5;
	}
	
	abstract protected void drawBackground(Graphics g);
	
	public void paintView(Graphics g) {
		if (!initialised) {
			fixOldInfo();
			initialised = true;
		}
		
		if (getCurrentFrame() == 0 || getCurrentFrame() == kEndFrame)
			drawBackground(g);
		super.paintView(g);
	}
	
	protected void drawArrow(Graphics g, int startPos, int endPos, int horizPos) {
		int y1 = Math.min(startPos, endPos);
		int y2 = Math.max(startPos, endPos);
		
		int yDir = (startPos > endPos) ? 1 : -1;
		
		g.drawLine(horizPos, startPos - yDir, horizPos, endPos + yDir);
		if (y2 - y1 > 4) {
			g.drawLine(horizPos - 1, endPos + 2 * yDir, horizPos + 1, endPos + 2 * yDir);
			
			if (y2 - y1 > 6) {
				g.drawLine(horizPos - 2, endPos + 3 * yDir, horizPos + 2, endPos + 3 * yDir);
				
				if (y2 - y1 > 8) {
					g.drawLine(horizPos - 1, startPos - yDir, horizPos - 1, endPos + 3 * yDir);
					g.drawLine(horizPos + 1, startPos - yDir, horizPos + 1, endPos + 3 * yDir);
					
					if (y2 - y1 > 6) {
						g.drawLine(horizPos - 2, endPos + 4 * yDir, horizPos + 2, endPos + 4 * yDir);
						if (y2 - y1 > 8)
							g.drawLine(horizPos - 3, endPos + 5 * yDir, horizPos + 3, endPos + 5 * yDir);
					}
				}
			}
		}
	}
	
//----------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(getActiveNumKey()) || key.equals(getActiveCatKey()))
			super.doChangeVariable(g, key);
	}
	
}