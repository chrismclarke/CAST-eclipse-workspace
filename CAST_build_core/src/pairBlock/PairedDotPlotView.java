package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;


public class PairedDotPlotView extends CorePairedView {
//	static public final String PAIRED_DOT_PLOT = "pairedDotPlot";
	
	static final private Color kBandEvenColor = new Color(0xF6FFD5);
	static final private Color kBandOddColor = new Color(0xFFE3F0);
	static final private Color kPartLineColor = new Color(0x0066FF);
	
	public PairedDotPlotView(DataSet theData, XApplet applet,
												String preKey, String postKey, NumCatAxis theAxis, NumCatAxis groupAxis,
												double initialJittering) {
		super(theData, applet, preKey, postKey, theAxis, groupAxis, initialJittering);
	}
	
	public int[] getJittering() {
		return jittering;
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		NumVariable postVariable = (NumVariable)getVariable(postKey);
		int oldGroupCentre = groupCentre;
		groupCentre = groupAxis.catValToPosition(1);
		Point postPoint = getScreenPoint(index, (NumValue)postVariable.valueAt(index), null);
		groupCentre = oldGroupCentre;
		
		if (thePoint != null)
			drawCrossBackground(g, thePoint);
		
		if (showPairing) {
			if (postPoint != null)
				drawCrossBackground(g, postPoint);
			if (thePoint != null && postPoint != null)
				g.drawLine(thePoint.x, thePoint.y, postPoint.x, postPoint.y);
		}
		else {
			if (vertNotHoriz) {
				g.setColor(kPartLineColor);
				int center = getSize().width / 2;
				g.drawLine(thePoint.x, thePoint.y, center + 12, thePoint.y);
				g.drawLine(postPoint.x, postPoint.y, center - 12, postPoint.y);
				g.setColor(Color.red);
				drawVertArrow(g, thePoint.y, postPoint.y, center);
				
//				g.setColor(Color.gray);
//				g.drawLine(thePoint.x, thePoint.y, postPoint.x, thePoint.y);
//				g.setColor(Color.red);
//				drawVertArrow(g, thePoint.y, postPoint.y, postPoint.x);
			}
			else {
				g.setColor(Color.gray);
				g.drawLine(thePoint.x, postPoint.y, thePoint.x, thePoint.y);
				g.setColor(Color.red);
				drawHorizArrow(g, thePoint.x, postPoint.x, postPoint.y);
			}
		}
	}
	
	private  void drawVertArrow(Graphics g, int startPos, int endPos, int horizPos) {
		int y1 = Math.min(startPos, endPos);
		int y2 = Math.max(startPos, endPos);
		
		int yDir = (startPos > endPos) ? 1 : -1;
		
		g.drawLine(horizPos, startPos - yDir, horizPos, endPos + yDir);
		if (y2 - y1 > 8) {
			g.drawLine(horizPos - 1, startPos - yDir, horizPos - 1, endPos + 2 * yDir);
			g.drawLine(horizPos + 1, startPos - yDir, horizPos + 1, endPos + 2 * yDir);
		}
		
		if (y2 - y1 > 8) {
			g.drawLine(horizPos, endPos + yDir, horizPos - 4, endPos + 5 * yDir);
			g.drawLine(horizPos, endPos + yDir, horizPos + 4, endPos + 5 * yDir);
			
			g.drawLine(horizPos, endPos + 2 * yDir, horizPos - 4, endPos + 6 * yDir);
			g.drawLine(horizPos, endPos + 2 * yDir, horizPos + 4, endPos + 6 * yDir);
		}
		else if (y2 - y1 > 6) {
			g.drawLine(horizPos, endPos + yDir, horizPos - 3, endPos + 4 * yDir);
			g.drawLine(horizPos, endPos + yDir, horizPos + 3, endPos + 4 * yDir);
		}
		else if (y2 - y1 > 4) {
			g.drawLine(horizPos, endPos + yDir, horizPos - 2, endPos + 3 * yDir);
			g.drawLine(horizPos, endPos + yDir, horizPos + 2, endPos + 3 * yDir);
		}
		else if (y2 - y1 > 2) {
			g.drawLine(horizPos, endPos + yDir, horizPos - 1, endPos + 2 * yDir);
			g.drawLine(horizPos, endPos + yDir, horizPos + 1, endPos + 2 * yDir);
		}
		
/*		
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
*/
	}
	
	private  void drawHorizArrow(Graphics g, int startPos, int endPos, int vertPos) {
		int x1 = Math.min(startPos, endPos);
		int x2 = Math.max(startPos, endPos);
		
		int xDir = (startPos > endPos) ? 1 : -1;
		
		g.drawLine(startPos - xDir, vertPos, endPos + xDir, vertPos);
		
		if (x2 - x1 > 8) {
			g.drawLine(startPos - xDir, vertPos - 1, endPos + 2 * xDir, vertPos - 1);
			g.drawLine(startPos - xDir, vertPos + 1, endPos + 2 * xDir, vertPos + 1);
		}
		
		if (x2 - x1 > 8) {
			g.drawLine(endPos + xDir, vertPos, endPos + 5 * xDir, vertPos - 4);
			g.drawLine(endPos + xDir, vertPos, endPos + 5 * xDir, vertPos + 4);
			
			g.drawLine(endPos + 2 * xDir, vertPos, endPos + 6 * xDir, vertPos - 4);
			g.drawLine(endPos + 2 * xDir, vertPos, endPos + 6 * xDir, vertPos + 4);
		}
		else if (x2 - x1 > 6) {
			g.drawLine(endPos + xDir, vertPos, endPos + 4 * xDir, vertPos - 3);
			g.drawLine(endPos + xDir, vertPos, endPos + 4 * xDir, vertPos + 3);
		}
		else if (x2 - x1 > 4) {
			g.drawLine(endPos + xDir, vertPos, endPos + 3 * xDir, vertPos - 2);
			g.drawLine(endPos + xDir, vertPos, endPos + 3 * xDir, vertPos + 2);
		}
		else if (x2 - x1 > 2) {
			g.drawLine(endPos + xDir, vertPos, endPos + 2 * xDir, vertPos - 1);
			g.drawLine(endPos + xDir, vertPos, endPos + 2 * xDir, vertPos + 1);
		}
		
/*
		if (x2 - x1 > 4) {
			g.drawLine(endPos + 2 * xDir, vertPos - 1, endPos + 2 * xDir, vertPos + 1);
			
			if (x2 - x1 > 6) {
				g.drawLine(endPos + 3 * xDir, vertPos - 2, endPos + 3 * xDir, vertPos + 2);
				
				if (x2 - x1 > 8) {
					g.drawLine(startPos - xDir, vertPos - 1, endPos + 3 * xDir, vertPos - 1);
					g.drawLine(startPos - xDir, vertPos + 1, endPos + 3 * xDir, vertPos + 1);
					
					if (x2 - x1 > 6) {
						g.drawLine(endPos + 4 * xDir, vertPos - 2, endPos + 4 * xDir, vertPos + 2);
						if (x2 - x1 > 8)
							g.drawLine(endPos + 5 * xDir, vertPos - 3, endPos + 5 * xDir, vertPos + 3);
					}
				}
			}
		}
*/
	}
	
	protected Color getJoiningColor(int i) {
		if (showPairing)
			return Color.blue;
		else
			return null;
	}
	
	protected void drawBackground(Graphics g) {
		g.setColor(kBandEvenColor);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setColor(kBandOddColor);
		if (vertNotHoriz)
			g.fillRect(0, 0, getSize().width / 2, getSize().height);
		else
			g.fillRect(0, 0, getSize().width, getSize().height / 2);
		g.setColor(getForeground());
		
		super.drawBackground(g);
	}
}
	
