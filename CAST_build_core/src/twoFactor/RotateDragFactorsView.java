package twoFactor;

import java.awt.*;

import dataView.*;
import axis.*;
import graphics3D.*;


public class RotateDragFactorsView extends Rotate3DView {
	
	static final public int NO_SLICE = 0;
	static final public int TREAT1_SLICE = 1;
	static final public int TREAT2_SLICE = 2;
	
	static final public int ALL_BLACK = 0;
	static final public int COLOURS = 1;
	
	static final private Color kResidualColor = new Color(0xFFCED3);
	
	static final private Color kGrayColor = new Color(0x666666);
	static final public Color kCatColor[] = {new Color(0x9900FF), new Color(0x00CC00),
														new Color(0xFF6600), new Color(0x6699FF), new Color(0x993300)};
	
	static final private Color kMainEffectColor = Color.red;
	static final private Color kInteractionColor = new Color(0x009900);
	
//	static final private int kHalfArrowLength = 6;
//	static final private int kArrowHead = 3;
	static final private int kHalfArrowLength = 8;
	static final private int kArrowHead = 4;
	static final private int kArrowOffset = 14;
	
	static final private Color getShadedCatColor(int i, double propn) {
		Color baseColor = (i >= 0) ? kCatColor[i % kCatColor.length] : kGrayColor;
		int red = baseColor.getRed();
		int green = baseColor.getGreen();
		int blue = baseColor.getBlue();
		red = (int)Math.rint(propn * red + (1.0 - propn) * 0xFF);
		green = (int)Math.rint(propn * green + (1.0 - propn) * 0xFF);
		blue = (int)Math.rint(propn * blue + (1.0 - propn) * 0xFF);
		return new Color(red, green, blue);
	}
	
	static final private Color getShadedCrossColor(int x, int z, double propn) {
		Color baseColorx = kCatColor[x % kCatColor.length];
		int redx = baseColorx.getRed();
		int greenx = baseColorx.getGreen();
		int bluex = baseColorx.getBlue();
		
		Color baseColorz = kCatColor[z % kCatColor.length];
		int redz = baseColorz.getRed();
		int greenz = baseColorz.getGreen();
		int bluez = baseColorz.getBlue();
		
		int red = (int)Math.rint(propn * redx + (1.0 - propn) * redz);
		int green = (int)Math.rint(propn * greenx + (1.0 - propn) * greenz);
		int blue = (int)Math.rint(propn * bluex + (1.0 - propn) * bluez);
		return new Color(red, green, blue);
	}
	
	private String modelKey;
	private int crossColouring = COLOURS;
	private boolean allowDragParams = true;
	private boolean showResiduals = true;
	private boolean showBaselineOffsetArrows = false;
	private boolean drawGridBlobs = true;
	private int paramDecimals;
	
	public RotateDragFactorsView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		this.modelKey = modelKey;
	}
	
	public void setCrossColouring(int crossColouring) {
		this.crossColouring = crossColouring;
	}
	
	public void setAllowDragParams(boolean allowDragParams) {
		this.allowDragParams = allowDragParams;
	}
	
	public void setShowResiduals(boolean showResiduals) {
		this.showResiduals = showResiduals;
	}
	
	public void setShowBaselineOffsetArrows(boolean showBaselineOffsetArrows, int paramDecimals) {
		this.showBaselineOffsetArrows = showBaselineOffsetArrows;
		this.paramDecimals = paramDecimals;
	}
	
	public void setDrawGridBlobs(boolean drawGridBlobs) {
		this.drawGridBlobs = drawGridBlobs;
	}
	
	protected String getGridModelKey() {
		return modelKey;
	}
	
	protected Point getScreenPoint(double y, int x, int z, int nXCats, int nZCats, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		double yFract = yAxis.numValToPosition(y);
		double xFract = xAxis.catValToPosition(x, nXCats);
		double zFract = zAxis.catValToPosition(z, nZCats);
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	private void setCrossColor(Graphics g, int xCat, int zCat, int index) {
		if (crossColouring != ALL_BLACK)
			g.setColor(getCrossColor(xCat, zCat));
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (drawData) {
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			int nx = xVariable.noOfCategories();
			int nz = zVariable.noOfCategories();
			
			Point crossPos = null;
			
			if (modelKey != null && showResiduals) {
				TwoFactorModel model = (TwoFactorModel)getVariable(modelKey);
				g.setColor(kResidualColor);
				int index = 0;
				ValueEnumeration ye = yVariable.values();
				Point meanPos = null;
				while (ye.hasMoreValues()) {
					double y = ye.nextDouble();
					int xCat = xVariable.getItemCategory(index);
					int zCat = zVariable.getItemCategory(index);
					double mean = model.evaluateMean(xCat, zCat);
					crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
					meanPos = getScreenPoint(mean, xCat, zCat, nx, nz, meanPos);
					if (crossPos != null && meanPos != null)
						g.drawLine(meanPos.x, meanPos.y, crossPos.x, crossPos.y);
					index ++;
				}
				g.setColor(getForeground());
			}
			
			int index = 0;
			ValueEnumeration ye = yVariable.values();
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				int xCat = xVariable.getItemCategory(index);
				int zCat = zVariable.getItemCategory(index);
				crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
				if (crossPos != null) {
					setCrossColor(g, xCat, zCat, index);
					drawCross(g, crossPos);
				}
				index ++;
			}
			g.setColor(getForeground());
			
			if (modelKey != null && allowDragParams) {
				TwoFactorModel model = (TwoFactorModel)getVariable(modelKey);
				drawArrows(g, model, nx, nz);
			}
		}
	}
	
	private Color getZColor(int z) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return getShadedCatColor(z, 1.0 - rot);
	}
	
	private Color getXColor(int x) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return getShadedCatColor(x, rot);
	}
	
	private Color getCrossColor(int x, int z) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return getShadedCrossColor(x, z, rot);
	}
	
	private void drawArrow(Graphics g, Point p, boolean isHighlighted, Color c) {
		if (dragX >= 0 && dragZ >= 0 && !isHighlighted)
			return;
		
		Color oldColor = g.getColor();
		g.setColor(isHighlighted ? Color.black : c);
		
		g.drawLine(p.x, p.y, p.x + kArrowOffset, p.y);
		int arrowCentre = p.x + kArrowOffset;
		int arrowTop = p.y - kHalfArrowLength;
		int arrowBottom = p.y + kHalfArrowLength;
		g.drawLine(arrowCentre, arrowTop, arrowCentre, arrowBottom);
		g.drawLine(arrowCentre - 1, arrowTop, arrowCentre - 1, arrowBottom);
		
		g.drawLine(arrowCentre, arrowTop, arrowCentre + kArrowHead, arrowTop + kArrowHead);
		g.drawLine(arrowCentre, arrowTop + 1, arrowCentre + kArrowHead, arrowTop + kArrowHead + 1);
		g.drawLine(arrowCentre - 1, arrowTop, arrowCentre - kArrowHead - 1, arrowTop + kArrowHead);
		g.drawLine(arrowCentre - 1, arrowTop + 1, arrowCentre - kArrowHead - 1, arrowTop + kArrowHead + 1);
		
		g.drawLine(arrowCentre, arrowBottom, arrowCentre + kArrowHead, arrowBottom - kArrowHead);
		g.drawLine(arrowCentre, arrowBottom - 1, arrowCentre + kArrowHead, arrowBottom - kArrowHead - 1);
		g.drawLine(arrowCentre - 1, arrowBottom, arrowCentre - kArrowHead - 1, arrowBottom - kArrowHead);
		g.drawLine(arrowCentre - 1, arrowBottom - 1, arrowCentre - kArrowHead - 1, arrowBottom - kArrowHead - 1);
		
/*		
		g.drawLine(p.x, p.y - kHalfArrowLength, p.x, p.y + kHalfArrowLength);
		g.drawLine(p.x, p.y - kHalfArrowLength, p.x - kArrowHead, p.y - kHalfArrowLength + kArrowHead);
		g.drawLine(p.x, p.y - kHalfArrowLength, p.x + kArrowHead, p.y - kHalfArrowLength + kArrowHead);
		g.drawLine(p.x, p.y + kHalfArrowLength, p.x - kArrowHead, p.y + kHalfArrowLength - kArrowHead);
		g.drawLine(p.x, p.y + kHalfArrowLength, p.x + kArrowHead, p.y + kHalfArrowLength - kArrowHead);
		
		if (isHighlighted) {
			g.drawLine(p.x - 1, p.y - kHalfArrowLength + 1, p.x - 1, p.y + kHalfArrowLength - 1);
			g.drawLine(p.x + 1, p.y - kHalfArrowLength + 1, p.x + 1, p.y + kHalfArrowLength - 1);
		}
*/
		g.setColor(oldColor);
	}
	
	private void drawArrows(Graphics g, TwoFactorModel model, int nx, int nz) {
		Point p = null;
		for (int x=0 ; x<nx ; x++)
			for (int z=0 ; z<nz ; z++)
				if (model.canDragMean(x, z)) {
					p = getScreenPoint(model.evaluateMean(x, z), x, z, nx, nz, p);
					drawArrow(g, p, (dragX == x) && (dragZ == z), (x == 0 || z == 0) ? kMainEffectColor : kInteractionColor);
				}
		
		if (showBaselineOffsetArrows && dragX >= 0 && dragZ >= 0)
			drawParamOffset(g, model, nx, nz);
	}
	
	private void drawParamOffset(Graphics g, TwoFactorModel model, int nx, int nz) {
		if (dragX == 0 && dragZ == 0)
			return;
		Color oldColor = g.getColor();
		g.setColor(Color.gray);
		
		double y0 = model.evaluateMean(0, 0);
		double y0Fract = yAxis.numValToPosition(y0);
		double x0Fract = xAxis.catValToPosition(0, nx);
		double x1Fract = xAxis.catValToPosition(dragX, nx);
		
		double y1 = model.evaluateMean(dragX, dragZ);
		double y1Fract = yAxis.numValToPosition(y1);
		double z0Fract = zAxis.catValToPosition(0, nz);
		double z1Fract = zAxis.catValToPosition(dragZ, nz);
		
		Point p0 = translateToScreen(map.mapH3DGraph(y0Fract, x0Fract, z0Fract),
											map.mapV3DGraph(y0Fract, x0Fract, z0Fract), null);
		Point p1 = translateToScreen(map.mapH3DGraph(y0Fract, x0Fract, 2.0),
											map.mapV3DGraph(y0Fract, x0Fract, 2.0), null);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		p1 = translateToScreen(map.mapH3DGraph(y0Fract, 2.0, z0Fract),
											map.mapV3DGraph(y0Fract, 2.0, z0Fract), p1);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		g.setColor(Color.red);
		p0 = translateToScreen(map.mapH3DGraph(y0Fract, x1Fract, z1Fract),
										map.mapV3DGraph(y0Fract, x1Fract, z1Fract), p0);
		p1 = translateToScreen(map.mapH3DGraph(y1Fract, x1Fract, z1Fract),
										map.mapV3DGraph(y1Fract, x1Fract, z1Fract), p1);
		int s = (p1.y > p0.y) ? 1 : -1;
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		g.drawLine(p0.x - 1, p0.y, p1.x - 1, p1.y - s);
		g.drawLine(p0.x + 1, p0.y, p1.x + 1, p1.y - s);
		
		int d = Math.min(6, Math.abs((p1.y - p0.y) / 2));
		g.drawLine(p1.x, p1.y, p1.x - d, p1.y - s * d);
		g.drawLine(p1.x, p1.y, p1.x + d, p1.y - s * d);
		g.drawLine(p1.x, p1.y - s, p1.x - d, p1.y - s * (d + 1));
		g.drawLine(p1.x, p1.y - s, p1.x + d, p1.y - s * (d + 1));
		
		int midLine = (p1.y + p0.y) / 2;
		NumValue paramValue = new NumValue(y1 - y0, paramDecimals);
		Font oldFont = g.getFont();
		g.setFont(getApplet().getStandardBoldFont());
		int baseline = midLine + g.getFontMetrics().getAscent() / 2;
		paramValue.drawLeft(g, p1.x - 3, baseline);
		g.setFont(oldFont);
		
		g.setColor(oldColor);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		String gridModelKey = getGridModelKey();
		
		if (gridModelKey == null)
			return null;
		
		TwoFactorModel model = (TwoFactorModel)getVariable(gridModelKey);
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int nx = xVariable.noOfCategories();
		CatVariable zVariable = (CatVariable)getVariable(zKey);
		int nz = zVariable.noOfCategories();
		
		Point p = null;
		Point q = null;
		
		for (int x=0 ; x<nx ; x++) {
			g.setColor(getXColor(x));
			for (int z=1 ; z<nz ; z++) {
				p = getScreenPoint(model.evaluateMean(x, z - 1), x, z-1, nx, nz, p);
				q = getScreenPoint(model.evaluateMean(x, z), x, z, nx, nz, q);
				g.drawLine(p.x, p.y, q.x, q.y);
			}
		}
	
		for (int z=0 ; z<nz ; z++) {
			g.setColor(getZColor(z));
			for (int x=1 ; x<nx ; x++) {
				p = getScreenPoint(model.evaluateMean(x - 1, z), x-1, z, nx, nz, p);
				q = getScreenPoint(model.evaluateMean(x, z), x, z, nx, nz, q);
				g.drawLine(p.x, p.y, q.x, q.y);
			}
		}
		
		if (drawGridBlobs) {
			g.setColor(getForeground());
			int oldCrossSize = getCrossSize();
			setCrossSize(oldCrossSize - 1);
			
			for (int x=0 ; x<nx ; x++)
				for (int z=0 ; z<nz ; z++) {
					p = getScreenPoint(model.evaluateMean(x, z), x, z, nx, nz, p);
					g.setColor(model.canDragMean(x, z) ? Color.red : Color.black);		//+++++
					drawBlob(g, p);
				}
			setCrossSize(oldCrossSize);
		}
		
		if (allowDragParams)
			drawArrows(g, model, nx, nz);
		
		return null;
	}

//-----------------------------------------------------------------------------------
	
	static final private int kMinHitDist = 120;
	
	private int hitOffset;
	private int dragX = -1;
	private int dragZ = -1;
	
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (modelKey != null && allowDragParams) {
			TwoFactorModel model = (TwoFactorModel)getVariable(modelKey);
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			int nx = xVariable.noOfCategories();
			int nz = zVariable.noOfCategories();
			
			int minHitDist = Integer.MAX_VALUE;
			int minHitOffset = 0;
			int hitX = -1;
			int hitZ = -1;
			Point p = null;
			
			for (int i=0 ; i<nx ; i++)
				for (int j=0 ; j<nz ; j++)
					if (model.canDragMean(i, j)) {
						p = getScreenPoint(model.evaluateMean(i, j), i, j, nx, nz, p);
						if (p != null) {
							int xDist = x - p.x - kArrowOffset / 2;
							int yDist = y - p.y;
							int thisHitDist = xDist * xDist + yDist * yDist;
							if (thisHitDist < minHitDist) {
								
								hitX = i;
								hitZ = j;
								minHitDist = thisHitDist;
								minHitOffset = yDist;
							}
						}
					}
			
			if (minHitDist <= kMinHitDist)
				return new VertDragPosInfo(y, hitX * nz + hitZ, minHitOffset);
		}
			return super.getInitialPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (dragX >= 0 && dragZ >= 0)
			return new VertDragPosInfo(y);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof VertDragPosInfo) {
			VertDragPosInfo posInfo = (VertDragPosInfo)startInfo;
			setArrowCursor();
			
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			int nz = zVariable.noOfCategories();
			
			dragX = posInfo.index / nz;
			dragZ = posInfo.index % nz;
			hitOffset = posInfo.hitOffset;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (dragX >= 0 && dragZ >= 0) {
			TwoFactorModel model = (TwoFactorModel)getVariable(modelKey);
			
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y - hitOffset;
			Point p = translateToScreen(0, newYPos, null);
			
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			int nx = xVariable.noOfCategories();
			int nz = zVariable.noOfCategories();
			
			try {
				double xFract = xAxis.catValToPosition(dragX, nx);
				double zFract = zAxis.catValToPosition(dragZ, nz);
				
				double newMean = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
				model.setDragMean(dragX, dragZ, newMean);
			} catch (AxisException e) {
			}
			getData().variableChanged(modelKey);
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragX >= 0 && dragZ >= 0) {
			dragX = dragZ = -1;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
