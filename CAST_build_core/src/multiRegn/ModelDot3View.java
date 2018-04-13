package multiRegn;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class ModelDot3View extends RotateDotPlaneView {
	static final public boolean BOLD = true;
	static final public boolean STANDARD = false;
	
	static final public int NO_HEAD = 0;
	static final public int LINE_HEAD = 1;
	static final public int FILLED_HEAD = 2;
	
	static final private int kDiagArrowLength = 4;
	static final private int kHorizArrowLength = 3;
	static final private int kDiagBArrowLength = 5;
	static final private int kHorizBArrowLength = 4;
	
	static final private int kMinArrowLength = 50;
	static final private int kMinBoldLength = 9;
	
	public ModelDot3View(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
	}
	
	private void drawFilledHead(Graphics g, int x0, int y0, int x1, int y1,
																		int horizLen, int diagLen) {
		int dx = (x1 - x0);
		int dy = (y1 - y0);
		if (dx * dx + dy * dy > kMinArrowLength) {
			if (100 * dx > 41 * dy)
				if (41 * dx < -100 * dy)
					if (100 * dx < -41 * dy)
						for (int i=1 ; i<=horizLen ; i++)
							g.drawLine(x1 - i, y1 + i, x1 + i, y1 + i);
					else
						for (int i=1 ; i<=diagLen ; i++)
							g.drawLine(x1 - i, y1, x1, y1 + i);
				else
					if (41 * dx > 100 * dy)
						for (int i=1 ; i<=horizLen ; i++)
							g.drawLine(x1 - i, y1 - i, x1 - i, y1 + i);
					else
						for (int i=1 ; i<=diagLen ; i++)
							g.drawLine(x1 - i, y1, x1, y1 - i);
			else
				if (41 * dx < -100 * dy)
					if (41 * dx > 100 * dy)
						for (int i=1 ; i<=diagLen ; i++)
							g.drawLine(x1, y1 + i, x1 + i, y1);
					else
						for (int i=1 ; i<=horizLen ; i++)
							g.drawLine(x1 + i, y1 - i, x1 + i, y1 + i);
				else
					if (100 * dx < -41 * dy)
						for (int i=1 ; i<=diagLen ; i++)
							g.drawLine(x1, y1 - i, x1 + i, y1);
					else
						for (int i=1 ; i<=horizLen ; i++)
							g.drawLine(x1 - i, y1 - i, x1 + i, y1 - i);
		}
	}
	
	private void drawLineHead(Graphics g, int x0, int y0, int x1, int y1,
																		int horizLen, int diagLen) {
		int dx = (x1 - x0);
		int dy = (y1 - y0);
		if (dx * dx + dy * dy > kMinArrowLength) {
			if (100 * dx > 41 * dy)
				if (41 * dx < -100 * dy)
					if (100 * dx < -41 * dy) {
						g.drawLine(x1, y1, x1 - horizLen, y1 + horizLen);
						g.drawLine(x1, y1, x1 + horizLen, y1 + horizLen);
					}
					else {
						g.drawLine(x1, y1, x1 - diagLen, y1);
						g.drawLine(x1, y1, x1, y1 + diagLen);
					}
				else
					if (41 * dx > 100 * dy) {
						g.drawLine(x1, y1, x1 - horizLen, y1 - horizLen);
						g.drawLine(x1, y1, x1 - horizLen, y1 + horizLen);
					}
					else {
						g.drawLine(x1, y1, x1, y1 - diagLen);
						g.drawLine(x1, y1, x1 - diagLen, y1);
					}
			else
				if (41 * dx < -100 * dy)
					if (41 * dx > 100 * dy) {
						g.drawLine(x1, y1, x1, y1 + diagLen);
						g.drawLine(x1, y1, x1 + diagLen, y1);
					}
					else {
						g.drawLine(x1, y1, x1 + horizLen, y1 + horizLen);
						g.drawLine(x1, y1, x1 + horizLen, y1 - horizLen);
					}
				else
					if (100 * dx < -41 * dy) {
						g.drawLine(x1, y1, x1, y1 - diagLen);
						g.drawLine(x1, y1, x1 + diagLen, y1);
					}
					else {
						g.drawLine(x1, y1, x1 - horizLen, y1 - horizLen);
						g.drawLine(x1, y1, x1 + horizLen, y1 - horizLen);
					}
		}
	}
	
	protected void drawLine(Graphics g, int x0, int y0, int x1, int y1,
																	boolean isBold, int headType) {
		g.drawLine(x0, y0, x1, y1);
		if (isBold) {
			int dx = (x1 - x0);
			int dy = (y1 - y0);
			if (dx * dx + dy * dy > kMinBoldLength) {
				if (dx == 0) {
					g.drawLine(x0 - 1, y0, x0 - 1, y1);
					g.drawLine(x0 + 1, y0, x0 + 1, y1);
				}
				else if (dy == 0) {
					g.drawLine(x0, y0 - 1, x1, y0 - 1);
					g.drawLine(x0, y0 + 1, x1, y0 + 1);
				}
				else if (100 * dx > 41 * dy)
					if (41 * dx < -100 * dy)
						if (100 * dx < -41 * dy) {
							g.drawLine(x0 - 1, y0, x1 - 1, y1 - 1);
							g.drawLine(x0 + 1, y0, x1 + 1, y1 - 1);
							g.drawLine(x0 - 1, y0, x1 + 1, y1 - 1);	//		draw diagonals to avoid
							g.drawLine(x0 + 1, y0, x1 - 1, y1 - 1);	//		'holes' in lines
						}
						else {
							g.drawLine(x0, y0 - 1, x1 - 1, y1);
							g.drawLine(x0 + 1, y0, x1, y1 + 1);
							g.drawLine(x0, y0 - 1, x1, y1 + 1);
							g.drawLine(x0 + 1, y0, x1 - 1, y1);
						}
					else
						if (41 * dx > 100 * dy) {
							g.drawLine(x0, y0 - 1, x1 - 1, y1 - 1);
							g.drawLine(x0, y0 + 1, x1 - 1, y1 + 1);
							g.drawLine(x0, y0 - 1, x1 - 1, y1 + 1);
							g.drawLine(x0, y0 + 1, x1 - 1, y1 - 1);
						}
						else {
							g.drawLine(x0, y0 + 1, x1 - 1, y1);
							g.drawLine(x0 + 1, y0, x1, y1 - 1);
							g.drawLine(x0, y0 + 1, x1, y1 - 1);
							g.drawLine(x0 + 1, y0, x1 - 1, y1);
						}
				else
					if (41 * dx < -100 * dy)
						if (41 * dx > 100 * dy) {
							g.drawLine(x0, y0 - 1, x1 + 1, y1);
							g.drawLine(x0 - 1, y0, x1, y1 + 1);
							g.drawLine(x0, y0 - 1, x1, y1 + 1);
							g.drawLine(x0 - 1, y0, x1 + 1, y1);
						}
						else {
							g.drawLine(x0, y0 + 1, x1 + 1, y1 + 1);
							g.drawLine(x0, y0 - 1, x1 + 1, y1 - 1);
							g.drawLine(x0, y0 + 1, x1 + 1, y1 - 1);
							g.drawLine(x0, y0 - 1, x1 + 1, y1 + 1);
						}
					else
						if (100 * dx < -41 * dy) {
							g.drawLine(x0, y0 + 1, x1 + 1, y1);
							g.drawLine(x0 - 1, y0, x1, y1 - 1);
							g.drawLine(x0, y0 + 1, x1, y1 - 1);
							g.drawLine(x0 - 1, y0, x1 + 1, y1);
						}
						else {
							g.drawLine(x0 + 1, y0, x1 + 1, y1 + 1);
							g.drawLine(x0 - 1, y0, x1 - 1, y1 + 1);
							g.drawLine(x0 + 1, y0, x1 - 1, y1 + 1);
							g.drawLine(x0 - 1, y0, x1 + 1, y1 + 1);
						}
			}
		}
		if (headType == LINE_HEAD)
			drawLineHead(g, x0, y0, x1, y1, isBold ? kHorizBArrowLength : kHorizArrowLength,
														isBold ? kDiagBArrowLength : kDiagArrowLength);
		else if (headType == FILLED_HEAD)
			drawFilledHead(g, x0, y0, x1, y1, isBold ? kHorizBArrowLength : kHorizArrowLength,
														isBold ? kDiagBArrowLength : kDiagArrowLength);
	}

}
	
