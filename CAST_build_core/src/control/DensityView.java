package control;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DensityView extends DataView {
//	static public final String DENSITY_PLOT = "densityPlot";
	
	static public final boolean SYMMETRIC = true;
	static public final boolean NOT_SYMMETRIC = false;
	
	static final private int kTopSpace = 10;
	
	private HorizAxis horizAxis;
	
	private boolean initialised = false;
	private double maxDensity;
	private double density[];
	private double xValue[];
	private boolean symmetricDrag;
	
	public DensityView(DataSet theData, XApplet applet, HorizAxis horizAxis,
																																boolean symmetricDrag) {
		super(theData, applet, new Insets(5, 5, 0, 5));
																					//		flush on bottom axis
		this.horizAxis = horizAxis;
		this.symmetricDrag = symmetricDrag;
	}
	
	public void resetDensities() {
		initialised = false;
	}
	
	public NumValue getDistnMean() {
		return getDistnVariable().getMean();
	}
	
	public NumValue getDistnSD() {
		return getDistnVariable().getSD();
	}
	
	protected Point getScreenPoint(double x, double d) {
		try {
			int vertPos = (int)Math.round((getSize().height - kTopSpace) * d / maxDensity) - 1;
													//		we need to subtract 1 so that filled polygon
													//		does not leave gap at botton
			int horizPos = horizAxis.numValToPosition(x);
			return translateToScreen(horizPos, vertPos, null);
		} catch (AxisException ex) {
			return null;
		}
	}
	
//----------------------------------------------------------------------------------
	
	static final private Color kShadeColor = Color.gray;
	static final private Color kHiliteColor = Color.red;
	static final private int kNoOfDensityVals = 50;
	
	protected void initialiseDensity(ContinDistnVariable variable) {
		maxDensity = variable.getMaxScaledDensity();
		
		density = new double[kNoOfDensityVals];
		xValue = new double[kNoOfDensityVals];
		double axisMin = horizAxis.minOnAxis;
		double axisMax = horizAxis.maxOnAxis;
		
		for (int i=0 ; i<density.length ; i++) {
			xValue[i] = axisMin + i * (axisMax - axisMin) / (density.length - 1);
			density[i] = variable.getScaledDensity(xValue[i]);
		}
	}
	
	protected void drawDensity(Graphics g, ContinDistnVariable variable) {
		Point lastPoint = getScreenPoint(xValue[0], density[0]);
		for (int i=1 ; i<density.length ; i++) {
			Point nextPoint = getScreenPoint(xValue[i], density[i]);
			g.drawLine(lastPoint.x, lastPoint.y, nextPoint.x, nextPoint.y);
			lastPoint = nextPoint;
		}
	}
	
	protected void shadeSelection(Graphics g, ContinDistnVariable variable) {
		double minSelection = variable.getMinSelection();
		double maxSelection = variable.getMaxSelection();
		if (minSelection >= maxSelection || minSelection >= horizAxis.maxOnAxis
																	|| maxSelection <= horizAxis.minOnAxis)
			return;
		
		boolean startAtAxisStart = minSelection <= xValue[0];
		int minIndex = xValue.length - 1;
		for (int i=0 ; i<xValue.length ; i++)
			if (xValue[i] >= minSelection) {
				minIndex = i;
				break;
			}
		
		boolean endAtAxisEnd = maxSelection >= xValue[xValue.length - 1];
		int maxIndex = 0;
		for (int i=xValue.length-1 ; i>=0 ; i--)
			if (xValue[i] <= maxSelection) {
				maxIndex = i;
				break;
			}
		
		int noOfPoints = (maxIndex - minIndex + 1) + 2;
		if (!startAtAxisStart)
			noOfPoints++;
		if (!endAtAxisEnd)
			noOfPoints++;
		
		int x[] = new int[noOfPoints];
		int y[] = new int[noOfPoints];
		
		int index;
		if (!startAtAxisStart) {
			Point p = getScreenPoint(minSelection, 0.0);
			x[0] = p.x;
			y[0] = p.y;
			p = getScreenPoint(minSelection, variable.getScaledDensity(minSelection));
			x[1] = p.x;
			y[1] = p.y;
			index = 2;
		}
		else {
			Point p = getScreenPoint(xValue[minIndex], 0.0);
			x[0] = p.x;
			y[0] = p.y;
			index = 1;
		}
		
		for (int i=minIndex ; i<=maxIndex ; i++) {
			Point p = getScreenPoint(xValue[i], density[i]);
			x[index] = p.x;
			y[index] = p.y;
			index++;
		}
		
		if (!endAtAxisEnd) {
			Point p = getScreenPoint(maxSelection, variable.getScaledDensity(maxSelection));
			x[index] = p.x;
			y[index] = p.y;
			p = getScreenPoint(maxSelection, 0.0);
			x[index + 1] = p.x;
			y[index + 1] = p.y;
		}
		else {
			Point p = getScreenPoint(xValue[maxIndex], 0.0);
			x[index] = p.x;
			y[index] = p.y;
		}
		
		g.setColor(kShadeColor);
		g.fillPolygon(x, y, noOfPoints);
		
		g.setColor(kHiliteColor);
		if (selected)
			switch (hitExtreme & (MIN_SELECTED | MAX_SELECTED)) {
				case MIN_SELECTED:
					{
						Point p1 = getScreenPoint(minSelection, 0.0);
						Point p2 = getScreenPoint(minSelection, variable.getScaledDensity(minSelection));
						g.fillRect(p1.x - 1, p2.y, 3, p1.y);
					}
					break;
				case MAX_SELECTED:
					{
						Point p1 = getScreenPoint(maxSelection, 0.0);
						Point p2 = getScreenPoint(maxSelection, variable.getScaledDensity(maxSelection));
						g.fillRect(p1.x - 1, p2.y, 3, p1.y);
					}
					break;
				default:
			}
		
		if (minSelection < maxSelection) {
			g.setColor(Color.gray);
			Point p1 = getScreenPoint(minSelection, 0.0);
			Point p2 = getScreenPoint(maxSelection, 0.0);
			g.drawLine(p1.x, 0, p1.x, getSize().height - 1);
			g.drawLine(p2.x, 0, p2.x, getSize().height - 1);
		}
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		ContinDistnVariable variable = (ContinDistnVariable)getDistnVariable();
		if (!initialised) {
			initialiseDensity(variable);
			initialised = true;
		}
		
		shadeSelection(g, variable);
		drawDensity(g, variable);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		initialised = false;
		super.doChangeVariable(g, key);
	}

//-----------------------------------------------------------------------------------
	
	static private final int kHitSlop = 4;
	private int hitOffset;
	private int hitExtreme = NONE_SELECTED;
	private double otherExtreme;
	private boolean directHit;
	private boolean selected = false;
	
	static private final int NONE_SELECTED = 0;
	static private final int MIN_SELECTED = 1;
	static private final int MAX_SELECTED = 2;
	static private final int DIRECT_HIT = 4;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		try {
			Point hitPos = translateFromScreen(x, y, null);
			DistnVariable v = getDistnVariable();
			int minPos = horizAxis.numValToPosition(v.getMinSelection());
			int maxPos = horizAxis.numValToPosition(v.getMaxSelection());
			int minHitOffset = hitPos.x - minPos;
			if (minHitOffset > kHitSlop || minHitOffset < -kHitSlop) {
				int maxHitOffset = hitPos.x - maxPos;
				if (maxHitOffset > kHitSlop || maxHitOffset < -kHitSlop) {
					if (Math.abs(maxHitOffset) <= Math.abs(minHitOffset))
						return new HorizDragPosInfo(hitPos.x, MAX_SELECTED, 0);
					else
						return new HorizDragPosInfo(hitPos.x, MIN_SELECTED, 0);
				}
				else
					return new HorizDragPosInfo(hitPos.x, MAX_SELECTED | DIRECT_HIT, maxHitOffset);
			}
			else
				return new HorizDragPosInfo(hitPos.x, MIN_SELECTED | DIRECT_HIT, minHitOffset);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x - hitOffset < 0)
			hitPos.x = hitOffset;
		else if (hitPos.x - hitOffset >= horizAxis.getAxisLength())
			hitPos.x = horizAxis.getAxisLength() - 1 + hitOffset;
		return new HorizDragPosInfo(hitPos.x);
	}
	
	protected int getMinMouseMove() {
		if (directHit)
			return super.getMinMouseMove();
		else
			return 0;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null) {
			DistnVariable v = getDistnVariable();
			hitExtreme = ((HorizDragPosInfo)startPos).index;
			hitOffset = ((HorizDragPosInfo)startPos).hitOffset;
			directHit = (hitExtreme & DIRECT_HIT) != 0;
			otherExtreme = ((hitExtreme & MIN_SELECTED) != 0) ? v.getMaxSelection()
																						: v.getMinSelection();
			selected = true;
			if (directHit)
				repaint();
			else
				doDrag(null, startPos);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selected = false;
			repaint();
		}
		else {
			selected = true;
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			
			int newAxisPos = dragPos.x - hitOffset;
			try {
				double newVal = horizAxis.positionToNumVal(newAxisPos);
				if (symmetricDrag) {
					double distnMean = getDistnVariable().getMean().toDouble();
					if (newVal < distnMean) {
						hitExtreme = MIN_SELECTED;
						getData().setSelection(activeDistnVariableKey, newVal, 2.0 * distnMean - newVal);
					}
					else {
						hitExtreme = MAX_SELECTED;
						getData().setSelection(activeDistnVariableKey, 2.0 * distnMean - newVal, newVal);
					}
				}
				else if (newVal < otherExtreme) {
					hitExtreme = MIN_SELECTED;
					getData().setSelection(activeDistnVariableKey, newVal, otherExtreme);
				}
				else {
					hitExtreme = MAX_SELECTED;
					getData().setSelection(activeDistnVariableKey, otherExtreme, newVal);
				}
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		hitExtreme = NONE_SELECTED;
		selected = false;
		repaint();
	}
}
	
