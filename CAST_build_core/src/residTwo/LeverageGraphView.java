package residTwo;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class LeverageGraphView extends Rotate3DView {
	
	static final private Color kGridColor = Color.blue;
	
	static final private int kMinArrowLength = 50;
	static final private int kDiagArrowLength = 4;
	static final private int kHorizArrowLength = 3;
	
	static final private int kGridSquares = 12;
	
	private String modelKey;
	private DragAddPointView linkedDataView;
	
	private NumValue tempX[] = {new NumValue(0.0, 0), new NumValue(0.0, 0)};
	
	public LeverageGraphView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String zKey, String modelKey, DragAddPointView linkedDataView) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, null, zKey);
		this.modelKey = modelKey;
		this.linkedDataView = linkedDataView;
	}
	
	private void drawLeverageGrid(Graphics g) {
		g.setColor(kGridColor);
		
		double xMin = xAxis.getMinOnAxis();
		double xMax = xAxis.getMaxOnAxis();
		double zMin = zAxis.getMinOnAxis();
		double zMax = zAxis.getMaxOnAxis();
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		
		Point p0 = null;
		Point p1 = null;
		
		double errorSD = model.evaluateSD().toDouble();
		double scaleFactor = errorSD * errorSD;
		
		for (int i=0 ; i<=kGridSquares ; i++) {
			tempX[0].setValue(xMin + i * (xMax - xMin) / kGridSquares);
			tempX[1].setValue(zMin);
			double leverage = model.getLeverage(tempX) * scaleFactor;
			p0 = getScreenPoint(tempX[0].toDouble(), leverage, tempX[1].toDouble(), p0);
			
			for (int j=1 ; j<=kGridSquares ; j++) {
				tempX[1].setValue(zMin + j * (zMax - zMin) / kGridSquares);
				leverage = model.getLeverage(tempX) * scaleFactor;
				p1 = getScreenPoint(tempX[0].toDouble(), leverage, tempX[1].toDouble(), p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				Point pTemp = p0; p0 = p1; p1 = pTemp;
			}
		}
		
		for (int j=0 ; j<=kGridSquares ; j++) {
			tempX[0].setValue(xMin);
			tempX[1].setValue(zMin + j * (zMax - zMin) / kGridSquares);
			double leverage = model.getLeverage(tempX) * scaleFactor;
			p0 = getScreenPoint(tempX[0].toDouble(), leverage, tempX[1].toDouble(), p0);
			
			for (int i=1 ; i<=kGridSquares ; i++) {
				tempX[0].setValue(xMin + i * (xMax - xMin) / kGridSquares);
				leverage = model.getLeverage(tempX) * scaleFactor;
				p1 = getScreenPoint(tempX[0].toDouble(), leverage, tempX[1].toDouble(), p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				Point pTemp = p0; p0 = p1; p1 = pTemp;
			}
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
	
	protected void drawData(Graphics g, int shadeHandling) {
		boolean xzAxesBehind = map.getTheta2() < 180.0;
		
		if (!xzAxesBehind)
			drawLeverageGrid(g);
		
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable zVariable = (NumVariable)getVariable(zKey);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ze = zVariable.values();
		Point crossPos = null;
		
		FlagEnumeration fe = getSelection().getEnumeration();
		g.setColor(Color.red);
		while (xe.hasMoreValues() && ze.hasMoreValues()) {
			boolean selected = fe.nextFlag();
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			if (selected) {
				crossPos = getScreenPoint(x, 0.0, z, crossPos);
				if (crossPos != null) {
					drawCrossBackground(g, crossPos);
					
					tempX[0].setValue(x);
					tempX[1].setValue(z);
					MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
					double errorSD = model.evaluateSD().toDouble();
					double scaleFactor = errorSD * errorSD;
					double leverage = model.getLeverage(tempX) * scaleFactor;
					Point leveragePos = getScreenPoint(x, leverage, z, null);
					g.drawLine(crossPos.x, crossPos.y, leveragePos.x, leveragePos.y);
					
					Point axisPos = getScreenPoint(xAxis.getMinOnAxis(), leverage,
																																zAxis.getMinOnAxis(), null);
					g.drawLine(leveragePos.x, leveragePos.y, axisPos.x, axisPos.y);
					drawLineHead(g, leveragePos.x, leveragePos.y, axisPos.x, axisPos.y,
																								kHorizArrowLength, kDiagArrowLength);
				}
			}
		}
		
		g.setColor(getForeground());
		xe = xVariable.values();
		ze = zVariable.values();
		while (xe.hasMoreValues() && ze.hasMoreValues()) {
			crossPos = getScreenPoint(xe.nextDouble(), 0.0, ze.nextDouble(), crossPos);
			if (crossPos != null)
				drawCross(g, crossPos);
		}
		
		if (xzAxesBehind)
			drawLeverageGrid(g);
	}

//-----------------------------------------------------------------------------------
	
	private Point hitPos = new Point(0,0);
	private double hitFracts[] = new double[2];
	private boolean draggingXZ = false;
	
	private double constrain01(double fract) {
		return Math.max(0.0, Math.min(1.0, fract));
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		PositionInfo result = super.getInitialPosition(x, y);
		if (result == null) {
			hitPos = translateFromScreen(x, y, hitPos);
			hitFracts = map.mapToYX(hitPos);
			
			result = new XZDragPosInfo(constrain01(hitFracts[0]), constrain01(hitFracts[1]));
		}
		return result;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (draggingXZ) {
			hitPos = translateFromScreen(x, y, hitPos);
			hitFracts = map.mapToYX(hitPos);
			
			return new XZDragPosInfo(constrain01(hitFracts[0]), constrain01(hitFracts[1]));
		}
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof XZDragPosInfo) {
			setArrowCursor();
			draggingXZ = true;
			linkedDataView.setLastXZ((XZDragPosInfo)startInfo, xAxis, zAxis);
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (draggingXZ) {
			if (toPos == null)
				linkedDataView.setLastXZ(null, xAxis, zAxis);
			else
				linkedDataView.setLastXZ((XZDragPosInfo)toPos, xAxis, zAxis);
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (draggingXZ) {
			draggingXZ = false;
			linkedDataView.setLastXZ(null, xAxis, zAxis);
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
