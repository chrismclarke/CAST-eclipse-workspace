package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

import boxPlotProg.*;


public class DragBoxHistoView extends CoreDragBoxView {
//	static public final String DRAG_BOX_HISTO_PLOT = "dragBoxHistoPlot";
	
	static final public int SYMMETRIC = 0;
	static final public int SKEW_RIGHT = 1;
	static final public int SKEW_LEFT = 2;
	static final public int LONG_TAILS = 3;
	
	static final private int kBoxVertBorder = 5;
	static final private int kTopHistoBorder = 20;
	static final private int kBoxHistoGap = 10;
	
	public DragBoxHistoView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	protected void initialiseBox(NumValue sortedVal[], BoxInfo boxInfo) {
		initialiseBox(SYMMETRIC);
	}
	
	public void initialiseBox(int specialDistn) {
		double axisRange = axis.maxOnAxis - axis.minOnAxis;
		int minPos = getAxisPos(axis.minOnAxis + axisRange * 0.05);
		int maxPos = getAxisPos(axis.minOnAxis + axisRange * 0.95);
		int centre = (minPos + maxPos) / 2;
		int halfLen = (maxPos - minPos) / 2;
		minPos = centre - halfLen;
		maxPos = centre + halfLen;
		boxInfo.boxPos[LOW_EXT] = minPos;
		boxInfo.boxPos[HIGH_EXT] = maxPos;
		switch (specialDistn) {
			case SYMMETRIC:
			case LONG_TAILS:
				boxInfo.boxPos[MEDIAN] = centre;
				int innerLen = (specialDistn == SYMMETRIC) ? (halfLen * 2 / 5)
																			: (innerLen = halfLen / 8);
				boxInfo.boxPos[LOW_QUART] = centre - innerLen;
				boxInfo.boxPos[HIGH_QUART] = centre + innerLen;
				break;
			case SKEW_RIGHT:
			case SKEW_LEFT:
				int len1 = halfLen / 10;
				int len2 = halfLen / 6;
				int len3 = halfLen / 4;
				if (specialDistn == SKEW_RIGHT) {
					boxInfo.boxPos[LOW_QUART] = minPos + len1;
					boxInfo.boxPos[MEDIAN] = boxInfo.boxPos[LOW_QUART] + len2;
					boxInfo.boxPos[HIGH_QUART] = boxInfo.boxPos[MEDIAN] + len3;
				}
				else {
					boxInfo.boxPos[HIGH_QUART] = maxPos - len1;
					boxInfo.boxPos[MEDIAN] = boxInfo.boxPos[HIGH_QUART] - len2;
					boxInfo.boxPos[LOW_QUART] = boxInfo.boxPos[MEDIAN] - len3;
				}
				break;
			default:
		}
	}
	
	protected int getBoxBottom() {
		return kBoxVertBorder;
	}
	
	protected int getDragTop() {
		return getSize().height;
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		int histoBottom = boxInfo.getBoxHeight() + 2 * kBoxVertBorder + kBoxHistoGap;
		
		int rectArea = (boxInfo.boxPos[HIGH_EXT] - boxInfo.boxPos[LOW_EXT])
												* (getSize().height - histoBottom - kTopHistoBorder) / 20;
																//		ht = 1/5 if all boxes are same width
		Point p0 = null;
		Point p1 = null;
		for (int i=LOW_EXT ; i<HIGH_EXT ; i++) {
			int width = boxInfo.boxPos[i+1] - boxInfo.boxPos[i];
			p0 = translateToScreen(boxInfo.boxPos[i], histoBottom, p0);
			if (width == 0) {
				g.setColor(Color.black);
				g.drawLine(p0.x, 0, p0.x, p0.y);
			}
			else {
				int ht = rectArea / width;
				p1 = translateToScreen(boxInfo.boxPos[i+1], histoBottom + ht, p1);
				
				g.setColor(Color.lightGray);
				g.fillRect(p0.x, p1.y, (p1.x - p0.x), (p0.y - p1.y));
				g.setColor(Color.black);
				g.drawRect(p0.x, p1.y, (p1.x - p0.x), (p0.y - p1.y));
			}
		}
		
		p0 = translateToScreen(0, histoBottom, p0);
		g.drawLine(0, p0.y, getSize().width, p0.y);
	}
	
	
	protected void notifyStartDrag() {
		DragBoxHistoApplet applet = (DragBoxHistoApplet)getApplet();
		applet.notifyStartDrag();
	}
}
	
