package transform;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class TwinAxisDotPlotView extends DotPlotView {
	static final private int kArrowSize = 4;
	
	public TwinAxisDotPlotView(DataSet theData, XApplet applet,
																				NumCatAxis theAxis, double initialJittering) {
		super(theData, applet, theAxis, initialJittering);
		setRetainLastSelection(true);
	}
	
	public TwinAxisDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		this(theData, applet, theAxis, 0.3);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		thePoint = super.getScreenPoint(index, theVal, thePoint);
		
		if (thePoint != null) {
			int available = getDisplayWidth() - getDisplayBorderNearAxis() - getDisplayBorderAwayAxis();
			int offset = (available - currentJitter) / 2;
			thePoint.y -= offset;
		}
		return thePoint;
	}
	
	protected void doHilite(Graphics g, int index, Point p) {
		if (p != null) {
			drawCrossBackground(g, p);
			g.drawLine(p.x, 0, p.x, getSize().height - 1);
			for (int i=1 ; i<kArrowSize ; i++) {
				g.drawLine(p.x - i, i, p.x + i, i);
				g.drawLine(p.x - i, getSize().height - 1 - i, p.x + i, getSize().height - 1 - i);
			}
		}
	}

}
	
