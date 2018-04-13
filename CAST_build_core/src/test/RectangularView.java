package test;

import java.awt.*;

import dataView.*;
import axis.*;


public class RectangularView extends DragLocationView {
//	static public final String RECTANGULAR_VIEW = "RectView";
	
	static final private String kExtremeString = "prob of more 'extreme' data";
	static final private int kTopBorder = 5;
	static final private int kRightBorder = 2;
	static final private int kArrowOffset = 20;
	static final private int kArrowEndOffset = 30;
	
	VertAxis densityAxis;
	
	public RectangularView(DataSet theData, XApplet applet, DragValAxis theAxis, VertAxis densityAxis) {
		super(theData, applet, theAxis, new Insets(0, 0, 0, 0));
		this.densityAxis = densityAxis;
	}
	
	private boolean initialised = false;
	
	public void paintView(Graphics g) {
		if (!initialised) {
			requestFocus();
			initialised = true;
		}
		
		try {
			int zeroPos = axis.numValToPosition(0.0);
			int onePos = axis.numValToPosition(1.0);
			int pPos = axis.getAxisValPos();
			
			int rectHeight = densityAxis.numValToPosition(1.0);
			
			Point topLeft = translateToScreen(zeroPos, rectHeight, null);
			Point bottomRight = translateToScreen(onePos, 0, null);
			
			Point pBottomRight = translateToScreen(pPos, 0, null);
//			if (selectedVal) {
//				g.setColor(Color.yellow);
//				g.fillRect(pBottomRight.x - 2, 0, 5, getSize().height);
//			}
			
			g.setColor(Color.black);
			g.drawRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y + 1);
			
			g.setColor(Color.red);
			g.drawLine(pBottomRight.x, topLeft.y + 1, pBottomRight.x, bottomRight.y);
			
			if (pBottomRight.x > topLeft.x + 1) {
				g.setColor(Color.blue);
				g.fillRect(topLeft.x + 1, topLeft.y + 1, pBottomRight.x - topLeft.x - 1,
																						pBottomRight.y - topLeft.y);
			}
			
			if (bottomRight.x > pBottomRight.x + 1) {
				g.setColor(Color.lightGray);
				g.fillRect(pBottomRight.x + 1, topLeft.y + 1, bottomRight.x - pBottomRight.x - 1,
																						pBottomRight.y - topLeft.y);
			}
			
			g.setColor(Color.blue);
			FontMetrics fm = g.getFontMetrics();
			
			int lineVert = kTopBorder + fm.getAscent();
			int lineHoriz = getSize().width - kRightBorder - fm.stringWidth(kExtremeString);
			g.drawString(kExtremeString, lineHoriz, lineVert);
			
			Point arrowStart = new Point(lineHoriz + kArrowOffset, lineVert + fm.getDescent());
			
			lineVert += fm.getHeight();
			NumValue pValue = axis.getAxisVal();
			String p = "= " + pValue.toString();
			lineHoriz = getSize().width - kRightBorder - fm.stringWidth(p);
			g.drawString(p, lineHoriz, lineVert);
			
			Point arrowEnd = new Point((topLeft.x + pBottomRight.x) / 2,
																						topLeft.y + kArrowEndOffset);
			g.setColor(Color.black);
			g.drawLine(arrowStart.x, arrowStart.y, arrowEnd.x, arrowEnd.y);
		} catch (AxisException e) {
		}
	}
}
	
