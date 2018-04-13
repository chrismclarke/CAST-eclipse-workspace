package graphics3D;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ContourControlView extends MarginalDataView {
	
	static final private int kMinWidth = 36;
	static final private int kDisplayBorder = 10;
	
	private ColourMap colourMap;
	private double contourValue;
	private String modelKey;				//	only to call getData().variableChanged(modelKey)
	
	public ContourControlView(DataSet theData, XApplet applet, NumCatAxis theAxis,
															String modelKey, ColourMap colourMap, double contourValue) {
		super(theData, applet, new Insets(10, 0, 10, 0), theAxis);
		this.colourMap = colourMap;
		this.contourValue = contourValue;
		this.modelKey = modelKey;
	}
	
	public double getContourValue() {
		return contourValue;
	}
	
	public int minDisplayWidth() {
		return kMinWidth;
	}
	
	public void paintView(Graphics g) {
		int height = getDisplayHeight();
		double min = axis.minOnAxis;
		double max = axis.maxOnAxis;
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<height ; i++) {
			double y = min + i * (max - min) / height;
			Color c = colourMap.getColour(y);
			p0 = translateToScreen(i, 0, p0);
			p1 = translateToScreen(i, getDisplayWidth() - kDisplayBorder, p1);
			g.setColor(c);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		
		if (!Double.isNaN(contourValue)) {
			g.setColor(Color.black);
			int contourPos = axis.numValToRawPosition(contourValue);
			p0 = translateToScreen(contourPos, 0, p0);
			p1 = translateToScreen(contourPos, getDisplayWidth() - kDisplayBorder, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			p0 = translateToScreen(contourPos, 0, p0);
			p1 = translateToScreen(contourPos - 5, 5, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			p0 = translateToScreen(contourPos, 0, p0);
			p1 = translateToScreen(contourPos + 5, 5, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			if (doingDrag) {
				p0 = translateToScreen(contourPos - 1, 2, p0);
				p1 = translateToScreen(contourPos - 1, getDisplayWidth() - kDisplayBorder, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				p0 = translateToScreen(contourPos + 1, 2, p0);
				p1 = translateToScreen(contourPos + 1, getDisplayWidth() - kDisplayBorder, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	static final private int kHitSlop = 6;
	static final private int kDragSlop = 30;
	
	private boolean doingDrag = false;
	private int hitOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return !Double.isNaN(contourValue);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int contourPos = axis.numValToRawPosition(contourValue);
		
		int hitPos = translateFromScreen(x, y, null).x;
		
		if (Math.abs(hitPos - contourPos) > kHitSlop)
			return null;
		else
			return new HorizDragPosInfo(hitPos, 0, hitPos - contourPos);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kDragSlop || y < 0 || x >= getSize().width + kDragSlop || y >= getSize().height)
			return null;
		
		int hitPos = translateFromScreen(x, y, null).x;
		return new HorizDragPosInfo(hitPos);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo thePos = (HorizDragPosInfo)startInfo;
		hitOffset = thePos.hitOffset;
		doingDrag = true;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo thePos = (HorizDragPosInfo)toPos;
			
			int yPos = thePos.x - hitOffset;
			try {
				contourValue = axis.positionToNumVal(yPos);
				getData().variableChanged(modelKey);
			} catch (AxisException ex) {
				return;
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
	
}
	
