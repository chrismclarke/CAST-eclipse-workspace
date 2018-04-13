package distribution;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class ContinuousCdfView extends ContinuousProbView {
	static final private Color kCdfColor = new Color(0x000000);
	static final private Color kArrowColor = new Color(0xFF0000);
	
	static final private int kArrowLength = 5;
	
	private double selectedX;
	
	public ContinuousCdfView(DataSet theData, XApplet applet, String distnKey,
												NumCatAxis horizAxis, NumCatAxis densityAxis, double startX) {
		super(theData, applet, distnKey, horizAxis, densityAxis);
		this.selectedX = startX;
	}
	
	public void paintView(Graphics g) {
		drawTitleString(g);
		
		double xVal[] = new double[kXPropns.length];
		double yVal[] = new double[kXPropns.length];
		
		ContinDistnVariable distnVar = (ContinDistnVariable)getVariable(distnKey);
		setupCdfPoints(distnVar, minSupportX, maxSupportX, 1.0, xVal, yVal);
		
		g.setColor(kCdfColor);
		drawCurve(g, xVal, yVal, horizAxis, densityAxis, true);
		
		double selectedY = distnVar.getCumulativeProb(selectedX);
		
		try {
			int xPos = horizAxis.numValToPosition(selectedX);
			int yPos = densityAxis.numValToPosition(selectedY);
			Point curvePt = translateToScreen(xPos, yPos, null);
			
			g.setColor(kArrowColor);
			g.drawLine(0, curvePt.y, curvePt.x, curvePt.y);		//	assumes standard orientation
			g.drawLine(0, curvePt.y, kArrowLength, curvePt.y + kArrowLength);
			g.drawLine(0, curvePt.y, kArrowLength, curvePt.y - kArrowLength);
			
			g.drawLine(curvePt.x, curvePt.y, curvePt.x, getSize().height - 1);
			g.drawLine(curvePt.x, getSize().height - 1, curvePt.x - kArrowLength, getSize().height - 1 - kArrowLength);
			g.drawLine(curvePt.x, getSize().height - 1, curvePt.x + kArrowLength, getSize().height - 1 - kArrowLength);
		} catch (AxisException e) {
		}
	}
	
	private void setupCdfPoints(ContinDistnVariable distnVar, double minSupport,
																	double maxSupport, double scalingFactor, double[] xVal, double[] yVal) {
		double minX = getDrawMin(minSupport);
		double maxX = getDrawMax(maxSupport);
		
		for (int i=0 ; i<kXPropns.length ; i++) {
			xVal[i] = minX + kXPropns[i] * (maxX - minX);
			yVal[i] = distnVar.getCumulativeProb(xVal[i]);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		try {
			@SuppressWarnings("unused")
			double hitVal = horizAxis.positionToNumVal(hitPos.x);
		} catch (AxisException e) {
			return null;
		}
		return new HorizDragPosInfo(hitPos.x);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || x >= getSize().width)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x < 0)
			hitPos.x = 0;
		else if (hitPos.x >= horizAxis.getAxisLength())
			hitPos.x = horizAxis.getAxisLength() - 1;
		return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		doDrag(null, startPos);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			try {
				selectedX = horizAxis.positionToNumVal(dragPos.x);
			} catch (AxisException e) {
				selectedX = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? horizAxis.minOnAxis
																																: horizAxis.maxOnAxis;
			}
			repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		repaint();
	}
}