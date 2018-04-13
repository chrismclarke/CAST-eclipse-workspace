package distribution;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DiscreteCdfDragView extends DiscreteCdfView {
	static final private Color kArrowColor = new Color(0xFF0000);
	
	static final private int kArrowLength = 5;
	
	private double selectedCum;
	
	
	public DiscreteCdfDragView(DataSet theData, XApplet applet, String distnKey,
																		NumCatAxis countAxis, NumCatAxis cumAxis, double startCum) {
		super(theData, applet, distnKey, countAxis, NO_DRAG);
		setProbAxis(cumAxis);
		this.selectedCum = startCum;
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		double probFactor = y.getProbFactor();
		
		double cumProb = 0;
		int maxY = getMaxCount(y);
		g.setColor(kArrowColor);
		for (int i=0 ; i<=maxY ; i++)
			try {
				cumProb += y.getScaledProb(i) * probFactor;
				
				if (selectedCum <= cumProb) {
					int xPos = countAxis.numValToPosition(i);
					int yPos = probAxis.numValToPosition(selectedCum);
					Point curvePt = translateToScreen(xPos, yPos, null);
					
					g.drawLine(0, curvePt.y, curvePt.x, curvePt.y);		//	assumes standard orientation
					
					g.drawLine(curvePt.x, curvePt.y, curvePt.x, getSize().height - 1);
					g.drawLine(curvePt.x, getSize().height - 1, curvePt.x - kArrowLength, getSize().height - 1 - kArrowLength);
					g.drawLine(curvePt.x, getSize().height - 1, curvePt.x + kArrowLength, getSize().height - 1 - kArrowLength);
					
					return;
				}
			} catch (AxisException e) {
				return;
			}
						//	only reached if the x-value is more than the max on the axis (e.g. for Poisson)
		try {
			int onePos = probAxis.numValToPosition(selectedCum);
			Point onePt = translateToScreen(0, onePos, null);
			g.drawLine(0, onePt.y, getSize().width, onePt.y);
		} catch (AxisException e) {
		}
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
			double hitVal = probAxis.positionToNumVal(hitPos.y);
		} catch (AxisException e) {
			return null;
		}
		return new VertDragPosInfo(hitPos.y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y < 0 || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y < 0)
			hitPos.y = 0;
		else if (hitPos.y >= probAxis.getAxisLength())
			hitPos.y = probAxis.getAxisLength() - 1;
		return new VertDragPosInfo(hitPos.y);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		doDrag(null, startPos);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			try {
				selectedCum = Math.min(1.0, Math.max(0.0, probAxis.positionToNumVal(dragPos.y)));
			} catch (AxisException e) {
				selectedCum = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? 0.0 : 1.0;
			}
			repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		repaint();
	}
}