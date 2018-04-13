package graphics3D;

import java.awt.*;

import dataView.*;
import models.*;


public class RotateDotPlaneView extends Rotate3DView {
	
	protected String modelKey;
	protected String[] explanKey;
	
	private boolean canSelectCrosses = false;
	private boolean drawPlaneOutline = false;
	
	public RotateDotPlaneView(DataSet theData,
						XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, explanKey == null ? null : explanKey[0],
																				yKey, explanKey == null ? null : explanKey[1]);
		this.modelKey = modelKey;
		this.explanKey = explanKey;
	}

//--------------------------------------------------------------------------------
	
	public void setSelectCrosses(boolean canSelectCrosses) {
		this.canSelectCrosses = canSelectCrosses;
	}
	
	public void setDrawPlaneOutline(boolean drawPlaneOutline) {
		this.drawPlaneOutline = drawPlaneOutline;
	}
	
	public void changeVariables(String xKey, String yKey, String zKey, String modelKey) {
		if (xKey != null && !xKey.equals(explanKey[0]))
			explanKey[0] = xKey;
		if (zKey != null && !zKey.equals(explanKey[1]))
			explanKey[1] = zKey;
		if (modelKey != null && !modelKey.equals(this.modelKey))
			this.modelKey = modelKey;
		super.changeVariables(xKey, yKey, zKey);
	}
	
	protected MultipleRegnModel getModel() {
		return (modelKey == null) ? null : (MultipleRegnModel)getVariable(modelKey);
	}
	
	protected boolean viewingPlaneFromTop() {
		MultipleRegnModel model = getModel();
		if (model == null)
			return true;
		
		double minX = xAxis.getMinOnAxis();
		double maxX = xAxis.getMaxOnAxis();
		double minZ = zAxis.getMinOnAxis();
		double maxZ = zAxis.getMaxOnAxis();
		
		double nearX, nearZ;
//		double nearY, y1, y2;
//		double angle = map.getTheta1();
		if (map.zAxisBehind())
			nearX = maxX;
		else
			nearX = minX;
		if (map.xAxisBehind())
			nearZ = maxZ;
		else
			nearZ = minZ;
		
		double x1 = minX;
		double x2 = maxX;
		double z1, z2;
		if (map.zAxisBehind() == map.xAxisBehind()) {
			z1 = maxZ;
			z2 = minZ;
		}
		else {
			z1 = minZ;
			z2 = maxZ;
		}
		
		double xVals[] = new double[2];
		xVals[0] = nearX;
		xVals[1] = nearZ;
		Point nearPos = getModelPoint(xVals, model);
		
		xVals[0] = x1;
		xVals[1] = z1;
		Point pos1 = getModelPoint(xVals, model);
		
		xVals[0] = x2;
		xVals[1] = z2;
		Point pos2 = getModelPoint(xVals, model);
		
		return (pos1.x > pos2.x) == ((pos2.y - pos1.y) * (nearPos.x - pos1.x)
											> (nearPos.y - pos1.y) * (pos2.x - pos1.x));
	}
	
	protected Point getModelPoint(double[] xVals, MultipleRegnModel model) {
		double fit = model.evaluateMean(xVals);
		return getScreenPoint(xVals[0], fit, xVals[1], null);
	}
	
	protected Color getPlaneColor() {
		return Color.lightGray;
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		MultipleRegnModel model = getModel();
		if (model == null)
			return null;
		
		Polygon p = ModelGraphics3D.getFull3DPlane(map, this, yAxis, xAxis, zAxis, model.getParameter(0).toDouble(),
										model.getParameter(1).toDouble(), model.getParameter(2).toDouble());
										
		g.setColor(getPlaneColor());
		g.fillPolygon(p);
		if (drawPlaneOutline)
			g.setColor(getForeground());
		g.drawPolygon(p);
		
		return p;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		Point crossPos = null;
		double xVals[] = new double[explanKey.length];
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		g.setColor(Color.black);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = xe[i].nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			if (model != null) {
				double fit = model.evaluateMean(xVals);
				if (shadeHandling == USE_OPAQUE)
					g.setColor(((y >= fit) == fromPlaneTop) ? Color.black : Color.gray);
			}
			drawCross(g, crossPos);
		}
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------

	static final private int kMinCrossHitDistance = 9;

	private Point crossPos[];
	
	private boolean draggingSelection = false;
	private boolean validPoints = false;
	
	protected boolean needsHitToDrag() {
		return !canSelectCrosses && super.needsHitToDrag();
	}
	
	protected void findScreenPositions(Point[] crossPos) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		int noOfVals = yVar.noOfValues();
		for (int i=0 ; i<noOfVals ; i++)
			crossPos[i] = getScreenPoint(xe[0].nextDouble(), ye.nextDouble(),
																	xe[1].nextDouble(), crossPos[i]);
	}
	
	protected int distance(int x, int y, Point crossPos) {
		int xDist = crossPos.x - x;
		int yDist = crossPos.y - y;
		return xDist * xDist + yDist * yDist;
	}
	
	protected int minHitDistance() {
		return kMinCrossHitDistance;
	}
	
/*
	protected PositionInfo getInitialPosition(int x, int y) {
		if (!draggingSelection) {
			PositionInfo rotatePosInfo = super.getInitialPosition(x, y);
			if (rotatePosInfo != null || !canSelectCrosses)
				return rotatePosInfo;
		}
		
		NumVariable xVar = (NumVariable)getVariable(explanKey[0]);
		int noOfVals = xVar.noOfValues();
		if (crossPos == null || crossPos.length != noOfVals)
			crossPos = new Point[noOfVals];
		
		if (!validPoints)
			findScreenPositions(crossPos);
		
		int minDist = Integer.MAX_VALUE;
		int minIndex = -1;
		
		for (int i=0 ; i<crossPos.length ; i++) {
			int dist = distance(x, y, crossPos[i]);
			if (dist < minDist) {
				minDist = dist;
				minIndex = i;
			}
		}
		
		if (minDist <= minHitDistance())
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
*/
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int minDist = Integer.MAX_VALUE;
		int minIndex = -1;
		
		if (canSelectCrosses) {
			NumVariable xVar = (NumVariable)getVariable(explanKey[0]);
			int noOfVals = xVar.noOfValues();
			if (crossPos == null || crossPos.length != noOfVals)
				crossPos = new Point[noOfVals];
			
			if (!validPoints)
				findScreenPositions(crossPos);
			
			for (int i=0 ; i<crossPos.length ; i++) {
				int dist = distance(x, y, crossPos[i]);
				if (dist < minDist) {
					minDist = dist;
					minIndex = i;
				}
			}
		}
		
		if (minDist <= minHitDistance())
			return new IndexPosInfo(minIndex);
		else if (!draggingSelection)
			return super.getInitialPosition(x, y);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (draggingSelection)
			return getInitialPosition(x, y);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null || startInfo instanceof IndexPosInfo) {
			setArrowCursor();
			draggingSelection = true;
			if (startInfo == null)
				getData().clearSelection();
			else {
				int selectionIndex = ((IndexPosInfo)startInfo).itemIndex;
				getData().setSelection(selectionIndex);
			}
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (draggingSelection)
			startDrag(toPos);
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (draggingSelection) {
			draggingSelection = false;		//		retains last selection
			validPoints = false;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}

}
	
