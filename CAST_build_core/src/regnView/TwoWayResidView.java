package regnView;

import java.awt.*;

import dataView.*;
import axis.*;

import regn.*;


public class TwoWayResidView extends DataView {
	static final public Color xResidColor = new Color(0xFF9933);
	static final public Color yResidColor = Color.blue;
	static final public Color anchorColor = Color.red;
	
	static final private int kAnchorSize = 8;
	static final private int kArrowHeadSize = 3;
	static final private double kForbiddenPropn = 0.05;
	
	static final private int kMaxDragAnchors = 1;
									//		If this is 1, the second anchor is set at means of X and Y
	
	private String xKey, yKey, modelKey;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	private Point pTemp = null;
	private Point qTemp = null;
	
	private Point drawEnd[] = new Point[2];
	
	private boolean drawXResid = false;
	private boolean drawYResid = false;
	
	public TwoWayResidView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																				String xKey, String yKey, String modelKey) {
		super(theData, applet, new Insets(10, 10, 10, 10));
		this.xKey = xKey;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		setAnchorsFromLine();
	}
	
	public void setDrawResiduals(boolean drawXResid, boolean drawYResid) {
		this.drawXResid = drawXResid;
		this.drawYResid = drawYResid;
		repaint();
	}
	
	protected Point getScreenPoint(double xVal, double yVal, Point thePoint) {
		try {
			int horizPos = xAxis.numValToPosition(xVal);
			int vertPos = yAxis.numValToPosition(yVal);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	private void drawCrosses(Graphics g) {
		g.setColor(getForeground());
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ye = yVar.values();
		while (xe.hasMoreValues()) {
			double nextX = xe.nextDouble();
			double nextY = ye.nextDouble();
			pTemp = getScreenPoint(nextX, nextY, pTemp);
			if (pTemp != null)
				drawCross(g, pTemp);
		}
	}
	
	private void drawYResids(Graphics g) {
		g.setColor(yResidColor);
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		TwoWayModel model = (TwoWayModel)getVariable(modelKey);
		
		double yMin = yAxis.minOnAxis;
		double yMax = yAxis.maxOnAxis;
		double ySlop = (yMax - yMin) * 0.1;
		yMin -= ySlop;
		yMax += ySlop;
		
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ye = yVar.values();
		while (xe.hasMoreValues()) {
			double nextX = xe.nextDouble();
			double nextY = ye.nextDouble();
			pTemp = getScreenPoint(nextX, nextY, pTemp);
			
			double fitY = Math.min(yMax, Math.max(yMin, model.predict(nextX, true)));
			int vertPos = yAxis.numValToRawPosition(fitY);
			qTemp = translateToScreen(0, vertPos, qTemp);
			
			if (pTemp != null)
				g.drawLine(pTemp.x, pTemp.y, pTemp.x, qTemp.y);
		}
	}
	
	private void drawXResids(Graphics g) {
		g.setColor(xResidColor);
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		TwoWayModel model = (TwoWayModel)getVariable(modelKey);
		
		double xMin = xAxis.minOnAxis;
		double xMax = xAxis.maxOnAxis;
		double xSlop = (xMax - xMin) * 0.1;
		xMin -= xSlop;
		xMax += xSlop;
		
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ye = yVar.values();
		while (xe.hasMoreValues()) {
			double nextX = xe.nextDouble();
			double nextY = ye.nextDouble();
			pTemp = getScreenPoint(nextX, nextY, pTemp);
			
			double fitX = Math.min(xMax, Math.max(xMin, model.predict(nextY, false)));
			int horizPos = xAxis.numValToRawPosition(fitX);
			qTemp = translateToScreen(horizPos, 0, qTemp);
			
			if (pTemp != null)
				g.drawLine(pTemp.x, pTemp.y, qTemp.x, pTemp.y);
		}
	}
	
	public void paintView(Graphics g) {
		drawLine(g);
		
		drawCrosses(g);
		
		if (drawYResid)
			drawYResids(g);
		if (drawXResid)
			drawXResids(g);
		
		drawAnchors(g);
	}
	
	private void addLineEnd(double x, double y) {
		int index;
		if (drawEnd[0] == null)
			index = 0;
		else if (drawEnd[1] == null)
			index = 1;
		else
			return;
		
		int xPos = xAxis.numValToRawPosition(x);
		int yPos = yAxis.numValToRawPosition(y);
		drawEnd[index] = translateToScreen(xPos, yPos, null);
	}
	
	private void drawLine(Graphics g) {
		TwoWayModel model = (TwoWayModel)getVariable(modelKey);
		g.setColor(Color.gray);
		
		double xMin = xAxis.minOnAxis;
		double xMax = xAxis.maxOnAxis;
		double xSlop = (xMax - xMin) * 0.1;
		xMin -= xSlop;
		xMax += xSlop;
		double yAtXMin = model.predict(xMin, true);
		double yAtXMax = model.predict(xMax, true);
		
		double yMin = yAxis.minOnAxis;
		double yMax = yAxis.maxOnAxis;
		double ySlop = (yMax - yMin) * 0.1;
		yMin -= ySlop;
		yMax += ySlop;
		double xAtYMin = model.predict(yMin, false);
		double xAtYMax = model.predict(yMax, false);
		
		drawEnd[0] = drawEnd[1] = null;
		if (yAtXMin >= yMin && yAtXMin <= yMax)
			addLineEnd(xMin, yAtXMin);
		if (yAtXMax >= yMin && yAtXMax <= yMax)
			addLineEnd(xMax, yAtXMax);
		if (xAtYMin >= xMin && xAtYMin <= xMax)
			addLineEnd(xAtYMin, yMin);
		if (xAtYMax >= xMin && xAtYMax <= xMax)
			addLineEnd(xAtYMax, yMax);
		
		if (drawEnd[1] != null)
			g.drawLine(drawEnd[0].x, drawEnd[0].y, drawEnd[1].x, drawEnd[1].y);
	}
	
	private void drawArrowHead(Graphics g, int x, int y, int dx, int dy) {
		g.drawLine(x, y, x + dx - dy, y + dy - dx);
		g.drawLine(x, y, x + dx + dy, y + dy + dx);
	}
	
	private void drawAnchors(Graphics g) {
		g.setColor(anchorColor);
		for (int i=0 ; i<kMaxDragAnchors ; i++) {
			int xPos = xAxis.numValToRawPosition(anchorX[i]);
			int yPos = yAxis.numValToRawPosition(anchorY[i]);
			pTemp = translateToScreen(xPos, yPos, pTemp);
			if (doingDrag && dragIndex == i) {
				for (int j=-1 ; j<2 ; j++) {
					g.drawLine(pTemp.x - kAnchorSize, pTemp.y + j, pTemp.x + kAnchorSize, pTemp.y + j);
					g.drawLine(pTemp.x + j, pTemp.y - kAnchorSize, pTemp.x + j, pTemp.y + kAnchorSize);
				}
			}
			else {
				g.drawLine(pTemp.x - kAnchorSize, pTemp.y, pTemp.x + kAnchorSize, pTemp.y);
				g.drawLine(pTemp.x, pTemp.y - kAnchorSize, pTemp.x, pTemp.y + kAnchorSize);
			}
			drawArrowHead(g, pTemp.x - kAnchorSize - 1, pTemp.y, kArrowHeadSize, 0);
			drawArrowHead(g, pTemp.x + kAnchorSize + 1, pTemp.y, -kArrowHeadSize, 0);
			drawArrowHead(g, pTemp.x, pTemp.y - kAnchorSize - 1, 0, kArrowHeadSize);
			drawArrowHead(g, pTemp.x, pTemp.y + kAnchorSize + 1, 0, -kArrowHeadSize);
		}
	}

//-----------------------------------------------------------------------------------
	
	private double anchorX[] = new double[2];
	private double anchorY[] = new double[2];
	private int anchorEdge[] = new int[2];
	
	static final private int BOTTOM = 0;
	static final private int TOP = 1;
	static final private int LEFT = 2;
	static final private int RIGHT = 3;
	static final private int CENTER = 4;
	
	public void setAnchorsFromLine() {
		TwoWayModel model = (TwoWayModel)getVariable(modelKey);
		double xMin = xAxis.minOnAxis;
		double xMax = xAxis.maxOnAxis;
		double yMin = yAxis.minOnAxis;
		double yMax = yAxis.maxOnAxis;
		
		int nAnchors = 0;
		
		double yAtXMax = model.predict(xMax, true);
		if (yAtXMax >= yMin && yAtXMax <= yMax) {
			anchorX[nAnchors] = xMax;
			anchorY[nAnchors] = yAtXMax;
			anchorEdge[nAnchors ++] = RIGHT;
		}
		
		if (nAnchors < kMaxDragAnchors) {
			double xAtYMax = model.predict(yMax, false);
			if (xAtYMax > xMin && xAtYMax < xMax) {
				anchorX[nAnchors] = xAtYMax;
				anchorY[nAnchors] = yMax;
				anchorEdge[nAnchors ++] = TOP;
			}
			if (nAnchors < kMaxDragAnchors) {
				double yAtXMin = model.predict(xMin, true);
				if (yAtXMin >= yMin && yAtXMin <= yMax) {
					anchorX[nAnchors] = xMin;
					anchorY[nAnchors] = yAtXMin;
					anchorEdge[nAnchors ++] = LEFT;
				}
				
				if (nAnchors < kMaxDragAnchors) {
					double xAtYMin = model.predict(yMin, false);
					if (xAtYMin > xMin && xAtYMin < xMax) {
						anchorX[nAnchors] = xAtYMin;
						anchorY[nAnchors] = yMin;
						anchorEdge[nAnchors ++] = BOTTOM;
					}
				}
			}
		}
		
		if (nAnchors < 2) {
			ValueEnumeration xe = ((NumVariable)getData().getVariable(xKey)).values();
			double sx = 0.0;
			int n = 0;
			while (xe.hasMoreValues()) {
				sx += xe.nextDouble();
				n ++;
			}
			double xMean = sx / n;
			double yAtXMean = model.predict(xMean, true);
			anchorX[nAnchors] = xMean;
			anchorY[nAnchors] = yAtXMean;
			anchorEdge[nAnchors] = CENTER;
		}
	}

//-----------------------------------------------------------------------------------
	
	private static final int kMinHitDist = 20;
	private int dragIndex = 0;
	private int xOffset, yOffset;
	private boolean doingDrag = false;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		for (int i=0 ; i<kMaxDragAnchors ; i++) {
			int horizPos = xAxis.numValToRawPosition(anchorX[i]);
			int vertPos = yAxis.numValToRawPosition(anchorY[i]);
			pTemp = translateToScreen(horizPos, vertPos, pTemp);
			if (pTemp != null) {
				int xDist = pTemp.x - x;
				int yDist = pTemp.y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (dist <= kMinHitDist) {
					dragIndex = i;
					return new DragPosInfo(x - pTemp.x, y - pTemp.y);
				}
			}
		}
		
		return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		return new DragPosInfo(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo != null && (startInfo instanceof DragPosInfo)) {
			DragPosInfo localInfo = (DragPosInfo)startInfo;
			doingDrag = true;
			xOffset = localInfo.x;
			yOffset = localInfo.y;
			repaint();
		}
		return true;
	}
	
	private void extendAnchor(double newX, double newY) {
		TwoWayModel model = (TwoWayModel)getVariable(modelKey);
		double xMin = xAxis.minOnAxis;
		double xMax = xAxis.maxOnAxis;
		double xForbidden = (xMax - xMin) * kForbiddenPropn;
		double yMin = yAxis.minOnAxis;
		double yMax = yAxis.maxOnAxis;
		double yForbidden = (yMax - yMin) * kForbiddenPropn;
		
		int fixedIndex = 1 - dragIndex;
		int fixedEdge = anchorEdge[fixedIndex];
		double fixedX = anchorX[fixedIndex];
		double fixedY = anchorY[fixedIndex];
		
		if (Math.abs(newX - fixedX) < xForbidden && Math.abs(newY - fixedY) < yForbidden) {
			newX = fixedX + xForbidden;
			if (newX > xMax)
				newX = fixedX - xForbidden;
		}
		
		if (newX == anchorX[fixedIndex]) {
			anchorX[dragIndex] = newX;
			if (fixedEdge == BOTTOM) {
				anchorEdge[dragIndex] = TOP;
				anchorY[dragIndex] = yMax;
			}
			else if (newY > fixedY && (yMax - newY) > yForbidden) {
				anchorEdge[dragIndex] = TOP;
				anchorY[dragIndex] = yMax;
			}
			else {
				anchorEdge[dragIndex] = BOTTOM;
				anchorY[dragIndex] = yMin;
			}
			model.setParameters(newX, Double.POSITIVE_INFINITY);
		}
		else if (newY == anchorY[fixedIndex]) {
			anchorY[dragIndex] = newY;
			if (fixedEdge == LEFT) {
				anchorEdge[dragIndex] = RIGHT;
				anchorX[dragIndex] = xMax;
			}
			else if (newX > fixedX && (xMax - newX) > xForbidden) {
				anchorEdge[dragIndex] = RIGHT;
				anchorX[dragIndex] = xMax;
			}
			else {
				anchorEdge[dragIndex] = LEFT;
				anchorX[dragIndex] = xMin;
			}
			model.setParameters(newY, 0.0);
		}
		else {
			double b1 = (anchorY[fixedIndex] - newY) / (anchorX[fixedIndex] - newX);
			double b0 = anchorY[fixedIndex]  - b1 * anchorX[fixedIndex];
			model.setParameters(b0, b1);
			
			if (fixedEdge != RIGHT) {
				double yAtXMax = model.predict(xMax, true);
				if (yAtXMax >= yMin && yAtXMax <= yMax) {
					anchorX[dragIndex] = xMax;
					anchorY[dragIndex] = yAtXMax;
					anchorEdge[dragIndex] = RIGHT;
					return;
				}
			}
			if (fixedEdge != TOP) {
				double xAtYMax = model.predict(yMax, false);
				if (xAtYMax > xMin && xAtYMax < xMax) {
					anchorX[dragIndex] = xAtYMax;
					anchorY[dragIndex] = yMax;
					anchorEdge[dragIndex] = TOP;
					return;
				}
			}
			if (fixedEdge != LEFT) {
				double yAtXMin = model.predict(xMin, true);
				if (yAtXMin >= yMin && yAtXMin <= yMax) {
					anchorX[dragIndex] = xMin;
					anchorY[dragIndex] = yAtXMin;
					anchorEdge[dragIndex] = LEFT;
					return;
				}
			}
			if (fixedEdge != BOTTOM) {
				double xAtYMin = model.predict(yMin, false);
				if (xAtYMin > xMin && xAtYMin < xMax) {
					anchorX[dragIndex] = xAtYMin;
					anchorY[dragIndex] = yMin;
					anchorEdge[dragIndex] = BOTTOM;
					return;
				}
			}
		}
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null && (toPos instanceof DragPosInfo)) {
			DragPosInfo localInfo = (DragPosInfo)toPos;
			pTemp = translateFromScreen(localInfo.x - xOffset, localInfo.y - yOffset, pTemp);
			double newX;
			try {
				newX = xAxis.positionToNumVal(pTemp.x);
			} catch (AxisException e) {
				if (e.axisProblem == AxisException.TOO_LOW_ERROR)
					newX = xAxis.minOnAxis;
				else
					newX = xAxis.maxOnAxis;
			}
			double newY;
			try {
				newY = yAxis.positionToNumVal(pTemp.y);
			} catch (AxisException e) {
				if (e.axisProblem == AxisException.TOO_LOW_ERROR)
					newY = yAxis.minOnAxis;
				else
					newY = yAxis.maxOnAxis;
			}
			
//			System.out.println("Temp anchors: (" + anchorX[0] + "," + anchorY[0] + ")  ("
//																				+ anchorX[1] + "," + anchorY[1] + ")");
			
			extendAnchor(newX, newY);
			getData().variableChanged(modelKey);
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
			doingDrag = false;
			repaint();
	}
	
	
}


	
