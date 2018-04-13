package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import regn.*;


public class LineView extends DataView {
	
	static final private int kXHitSlop = 30;
	static final private int kYHitSlop = 30;
	static final private int kArrowSize = 3;
	static final private int kOneOverhang = 36;
	
	static final private int NO_DRAG = -1;
	static final private int ZERO_DRAG = 0;
	static final private int ONE_DRAG = 1;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private String lineKey;
	
	private int selectedHandle = NO_DRAG;
	
	private LinearEquationView linkedEqn = null;
	
	public LineView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis, String lineKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.lineKey = lineKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	public void setLinkedEqn(LinearEquationView linkedEqn) {
		this.linkedEqn = linkedEqn;
	}

//-----------------------------------------------------------------------------------
	
	private Point getZeroPoint(LinearModel model) {
		double yAt0 = model.evaluateMean(0.0);
		int vert0Pos = yAxis.numValToRawPosition(yAt0);
		int horiz0Pos = xAxis.numValToRawPosition(0.0);
		return translateToScreen(horiz0Pos, vert0Pos, null);
	}
	
	private Point getOnePoint(LinearModel model) {
		double yAt1 = model.evaluateMean(1.0);
		int vert1Pos = yAxis.numValToRawPosition(yAt1);
		int horiz1Pos = xAxis.numValToRawPosition(1.0);
		return translateToScreen(horiz1Pos, vert1Pos, null);
	}
	
	public void paintView(Graphics g) {
		Color oldColor = g.getColor();
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		Point zeroPos = getZeroPoint(model);
		Point onePos = getOnePoint(model);
		
		g.setColor(Color.lightGray);
		g.drawLine(zeroPos.x, getSize().height, zeroPos.x, zeroPos.y);
		
		if (selectedHandle != ZERO_DRAG) {
			g.drawLine(0, zeroPos.y, onePos.x + kOneOverhang, zeroPos.y);
			g.drawLine(0, onePos.y, onePos.x + kOneOverhang, onePos.y);
			g.drawLine(onePos.x, getSize().height, onePos.x, onePos.y);
		}
		
		if (selectedHandle != ZERO_DRAG) {
			g.setColor(Color.blue);
			int oneArrowHoriz = onePos.x + kOneOverhang / 2;
			g.drawLine(oneArrowHoriz, zeroPos.y, oneArrowHoriz, onePos.y);
			if (Math.abs(zeroPos.y - onePos.y) > kArrowSize + 3) {
				if (zeroPos.y > onePos.y) {
					if (selectedHandle == ONE_DRAG) {
						g.drawLine(oneArrowHoriz - 1, zeroPos.y, oneArrowHoriz - 1, onePos.y + 1);
						g.drawLine(oneArrowHoriz + 1, zeroPos.y, oneArrowHoriz + 1, onePos.y + 1);
						for (int i=2 ; i<kArrowSize + 2 ; i++)
							g.drawLine(oneArrowHoriz - i, onePos.y + i, oneArrowHoriz + i, onePos.y + i);
					}
					else {
						g.drawLine(oneArrowHoriz, onePos.y, oneArrowHoriz + kArrowSize, onePos.y + kArrowSize);
						g.drawLine(oneArrowHoriz, onePos.y, oneArrowHoriz - kArrowSize, onePos.y + kArrowSize);
					}
				}
				else {
					if (selectedHandle == ONE_DRAG) {
						g.drawLine(oneArrowHoriz - 1, zeroPos.y, oneArrowHoriz - 1, onePos.y - 1);
						g.drawLine(oneArrowHoriz + 1, zeroPos.y, oneArrowHoriz + 1, onePos.y - 1);
						for (int i=2 ; i<kArrowSize + 2 ; i++)
							g.drawLine(oneArrowHoriz - i, onePos.y - i, oneArrowHoriz + i, onePos.y - i);
					}
					else {
						g.drawLine(oneArrowHoriz, onePos.y, oneArrowHoriz + kArrowSize, onePos.y - kArrowSize);
						g.drawLine(oneArrowHoriz, onePos.y, oneArrowHoriz - kArrowSize, onePos.y - kArrowSize);
					}
				}
			}
			else {
				g.drawLine(oneArrowHoriz - 1, zeroPos.y, oneArrowHoriz - 1, onePos.y);
				g.drawLine(oneArrowHoriz + 1, zeroPos.y, oneArrowHoriz + 1, onePos.y);
			}
		}
		
		if (selectedHandle != ONE_DRAG) {
			g.setColor(Color.red);
			g.drawLine(zeroPos.x, zeroPos.y, 0, zeroPos.y);
			if (selectedHandle == ZERO_DRAG) {
				g.drawLine(zeroPos.x, zeroPos.y - 1, 1, zeroPos.y - 1);
				g.drawLine(zeroPos.x, zeroPos.y + 1, 1, zeroPos.y + 1);
				for (int i=2 ; i<kArrowSize + 2 ; i++)
					g.drawLine(i, zeroPos.y - i, i, zeroPos.y + i);

			}
			else {
				g.drawLine(0, zeroPos.y, kArrowSize, zeroPos.y + kArrowSize);
				g.drawLine(0, zeroPos.y, kArrowSize, zeroPos.y - kArrowSize);
			}
		}
		
		g.setColor(oldColor);
		model.drawMean(g, this, xAxis, yAxis);
		
		if (selectedHandle != ONE_DRAG)
			ModelGraphics.drawHandle(g, zeroPos, selectedHandle == ZERO_DRAG);
		if (selectedHandle != ZERO_DRAG)
			ModelGraphics.drawHandle(g, onePos, selectedHandle == ONE_DRAG);
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 10;
	
	private int hitHandle, hitOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kXHitSlop || y - hitOffset < -kYHitSlop || x >= getSize().width + kXHitSlop
																|| y - hitOffset >= getSize().height + kYHitSlop)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.y + hitOffset >= yAxis.getAxisLength())
			return new VertDragPosInfo(-hitOffset + yAxis.getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		Point zeroPos = getZeroPoint(model);
		if (Math.abs(zeroPos.x - x) + Math.abs(zeroPos.y - y) < kMinHitDistance)
			return new VertDragPosInfo(hitPos.y, ZERO_DRAG, y - zeroPos.y);
		
		Point onePos = getOnePoint(model);
		if (Math.abs(onePos.x - x) + Math.abs(onePos.y - y) < kMinHitDistance)
			return new VertDragPosInfo(hitPos.y, ONE_DRAG, y - onePos.y);
		return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
		hitOffset = dragPos.hitOffset;
		selectedHandle = hitHandle = dragPos.index;
		if (linkedEqn != null) {
			linkedEqn.setSelectedParamIndex(selectedHandle);
			linkedEqn.repaint();
		}
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
//			selectedHandle = NO_DRAG;
//			repaint();
		}
		else {
			selectedHandle = hitHandle;
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y + hitOffset;
			try {
				double newHandleValue = yAxis.positionToNumVal(newYPos);
				LinearModel model = (LinearModel)getVariable(lineKey);
				
				if (selectedHandle == ZERO_DRAG) {
					yAxis.numValToPosition(newHandleValue + model.getSlope().toDouble());
								//		throws AxisException if handle is off screen
					model.setIntercept(newHandleValue);
				}
				else
					model.setSlope(newHandleValue - model.getIntercept().toDouble());
			} catch (AxisException e) {
//				selectedHandle = NO_DRAG;
			}
			getData().variableChanged(lineKey);
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedHandle = NO_DRAG;
		if (linkedEqn != null) {
			linkedEqn.setSelectedParamIndex(-1);
			linkedEqn.repaint();
		}
		repaint();
	}
}
	
