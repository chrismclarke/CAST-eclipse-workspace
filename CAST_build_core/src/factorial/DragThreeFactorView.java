package factorial;

import java.awt.*;

import dataView.*;
import axis.*;
import graphics3D.*;

import exper.*;


public class DragThreeFactorView extends Rotate3DView {
//	static public final String DRAG_3_FACTOR_PLOT = "dragThreeFactor";
	
	static final protected Color kResidualColor = new Color(0xFF6699);
	
	static final public int NO_RESIDUALS = 0;
	static final public int LINE_RESIDUALS = 1;
	
	static final public int ALL_BLACK = 0;
	static final public int X_COLOURS = 1;
	static final public int Z_COLOURS = 2;
	static final public int W_COLOURS = 3;
	
	static final private int kHalfArrowLength = 6;
	static final private int kArrowHead = 3;
	static final private int kWLabelOffset = 12;
	static final private int kWKeySpacing = 2;
	static final private int kWKeyRightBorder = 6;
	
	private String modelKey, wKey;
	private String wVarName;
	private int residualDisplay = NO_RESIDUALS;
	private int crossColouring = W_COLOURS;
	
	private boolean allowDragParams = true;
	
	public DragThreeFactorView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey, String wKey, String modelKey, String wVarName) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		this.modelKey = modelKey;
		this.wKey = wKey;
		this.wVarName = wVarName;
	}
	
	public void setResidualDisplay(int residualDisplay) {
		this.residualDisplay = residualDisplay;
	}
	
	public void setCrossColouring(int crossColouring) {
		this.crossColouring = crossColouring;
	}
	
	public void setAllowDragParams(boolean allowDragParams) {
		this.allowDragParams = allowDragParams;
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
	
	protected void setCrossColor(Graphics g, int xCat, int zCat, int wCat, int index) {
		switch (crossColouring) {
			case X_COLOURS:
				g.setColor(TreatEffectSliderView.getBaseBarColor(xCat));
				break;
			case Z_COLOURS:
				g.setColor(TreatEffectSliderView.getBaseBarColor(zCat));
				break;
			case W_COLOURS:
				g.setColor(TreatEffectSliderView.getBaseBarColor(wCat));
				break;
			case ALL_BLACK:
				break;
		}
	}
	
	private void drawWKey(Graphics g) {
		CatVariable wVariable = (CatVariable)getVariable(wKey);
		FontMetrics fm = g.getFontMetrics();
		int keyWidth = fm.stringWidth(wVarName);
		int nw = wVariable.noOfCategories();
		for (int i=0 ; i<nw ; i++)
			keyWidth = Math.max(keyWidth, wVariable.getLabel(i).stringWidth(g) + kWLabelOffset);
		keyWidth += kWKeyRightBorder;
		
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int baseline = ascent + kWKeySpacing;
		g.setColor(getForeground());
		g.drawString(wVarName, getSize().width - keyWidth, baseline);
		for (int i=nw-1 ; i>=0 ; i--) {
			baseline += ascent + descent + kWKeySpacing;
			g.setColor(TreatEffectSliderView.getBaseBarColor(i));
			wVariable.getLabel(i).drawRight(g, getSize().width - keyWidth + kWLabelOffset, baseline);
		}
		
		g.setColor(getForeground());
		g.drawRect(getSize().width - keyWidth - kWKeyRightBorder, 0,
												keyWidth + kWKeyRightBorder - 1, baseline + descent + kWKeySpacing);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		drawWKey(g);
		if (drawData) {
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			CatVariable wVariable = (CatVariable)getVariable(wKey);
			int nx = xVariable.noOfCategories();
			int nz = zVariable.noOfCategories();
			int nw = wVariable.noOfCategories();
			
			Point crossPos = null;
			
			if (residualDisplay == LINE_RESIDUALS) {
				g.setColor(kResidualColor);
				MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
				int[] cat = new int[3];
				int index = 0;
				ValueEnumeration ye = yVariable.values();
				Point meanPos = null;
				while (ye.hasMoreValues()) {
					double y = ye.nextDouble();
					int xCat = cat[0] = xVariable.getItemCategory(index);
					int zCat = cat[1] = zVariable.getItemCategory(index);
					cat[2] = wVariable.getItemCategory(index);
					double mean = model.evaluateMean(cat);
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
				int wCat = wVariable.getItemCategory(index);
				crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
				if (crossPos != null) {
					setCrossColor(g, xCat, zCat, wCat, index);
					drawCross(g, crossPos);
				}
				index ++;
			}
			g.setColor(getForeground());
			
			if (modelKey != null && allowDragParams) {
				MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
				drawArrows(g, model, nx, nz, nw);
			}
		}
	}
	
	private Color getZColor(int w) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return TreatEffectSliderView.getShadedBarColor(w, 1.0 - rot);
	}
	
	private Color getXColor(int w) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return TreatEffectSliderView.getShadedBarColor(w, rot);
	}
	
	private Color getWColor() {
		double rot = map.getTheta1() % 90;
		if (rot > 45)
			rot = 90 - rot;
		rot /= 45;
		
		return TreatEffectSliderView.getShadedBarColor(-1, rot);
	}
	
	private void drawArrow(Graphics g, Point p, boolean isHighlighted, Color c) {
		Color oldColor = g.getColor();
		g.setColor(c);
		
		g.drawLine(p.x, p.y - kHalfArrowLength, p.x, p.y + kHalfArrowLength);
		g.drawLine(p.x, p.y - kHalfArrowLength, p.x - kArrowHead, p.y - kHalfArrowLength + kArrowHead);
		g.drawLine(p.x, p.y - kHalfArrowLength, p.x + kArrowHead, p.y - kHalfArrowLength + kArrowHead);
		g.drawLine(p.x, p.y + kHalfArrowLength, p.x - kArrowHead, p.y + kHalfArrowLength - kArrowHead);
		g.drawLine(p.x, p.y + kHalfArrowLength, p.x + kArrowHead, p.y + kHalfArrowLength - kArrowHead);
		
		if (isHighlighted) {
			g.drawLine(p.x - 1, p.y - kHalfArrowLength + 1, p.x - 1, p.y + kHalfArrowLength - 1);
			g.drawLine(p.x + 1, p.y - kHalfArrowLength + 1, p.x + 1, p.y + kHalfArrowLength - 1);
		}
		g.setColor(oldColor);
	}
	
	private void drawArrows(Graphics g, MultiFactorModel model, int nx, int nz, int nw) {
		int[] cat = new int[3];
		Point p = null;
		for (int x=0 ; x<nx ; x++) {
			cat[0] = x;
			for (int z=0 ; z<nz ; z++) {
				cat[1] = z;
				for (int w=0 ; w<nw ; w++) {
					cat[2] = w;
					if (model.canDragMean(cat)) {
						double mean = model.evaluateMean(cat);
						p = getScreenPoint(mean, x, z, nx, nz, p);
						drawArrow(g, p, (dragX == x) && (dragZ == z) && (dragW == w), Color.red);
					}
				}
			}
		}
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		if (modelKey == null)
			return null;
		MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
		
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int nx = xVariable.noOfCategories();
		CatVariable zVariable = (CatVariable)getVariable(zKey);
		int nz = zVariable.noOfCategories();
		CatVariable wVariable = (CatVariable)getVariable(wKey);
		int nw = wVariable.noOfCategories();
		
		int[] cat = new int[3];
		Point p = null;
		Point q = null;
		
		for (int w=0 ; w<nw ; w++) {
			cat[2] = w;
			for (int x=0 ; x<nx ; x++) {
				cat[0] = x;
				g.setColor(getXColor(w));
				for (int z=1 ; z<nz ; z++) {
					cat[1] = z - 1;
					double mean = model.evaluateMean(cat);
					p = getScreenPoint(mean, x, z-1, nx, nz, p);
					cat[1] = z;
					mean = model.evaluateMean(cat);
					q = getScreenPoint(mean, x, z, nx, nz, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
		}
		
		for (int w=0 ; w<nw ; w++) {
			cat[2] = w;
			for (int z=0 ; z<nz ; z++) {
				cat[1] = z;
				g.setColor(getZColor(w));
				for (int x=1 ; x<nx ; x++) {
					cat[0] = x - 1;
					double mean = model.evaluateMean(cat);
					p = getScreenPoint(mean, x-1, z, nx, nz, p);
					cat[0] = x;
					mean = model.evaluateMean(cat);
					q = getScreenPoint(mean, x, z, nx, nz, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
		}
		
		for (int x=0 ; x<nx ; x++) {
			cat[0] = x;
			for (int z=0 ; z<nz ; z++) {
				cat[1] = z;
				g.setColor(getWColor());
				for (int w=1 ; w<nw ; w++) {
					cat[2] = w - 1;
					double mean = model.evaluateMean(cat);
					p = getScreenPoint(mean, x, z, nx, nz, p);
					cat[2] = w;
					mean = model.evaluateMean(cat);
					q = getScreenPoint(mean, x, z, nx, nz, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
		}
		
		g.setColor(getForeground());
		int oldCrossSize = getCrossSize();
		setCrossSize(oldCrossSize - 1);
		
		for (int x=0 ; x<nx ; x++) {
			cat[0] = x;
			for (int z=0 ; z<nz ; z++) {
				cat[1] = z;
				for (int w=0 ; w<nw ; w++) {
					cat[2] = w;
					double mean = model.evaluateMean(cat);
					p = getScreenPoint(mean, x, z, nx, nz, p);
					drawBlob(g, p);
				}
			}
		}
		setCrossSize(oldCrossSize);
		
		if (allowDragParams)
			drawArrows(g, model, nx, nz, nw);
		
		return null;
	}

//-----------------------------------------------------------------------------------
	
	static final private int kMinHitDist = 100;
	
	private int hitOffset;
	private int dragX = -1;
	private int dragZ = -1;
	private int dragW = -1;
	
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (modelKey != null && allowDragParams) {
			MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			CatVariable wVariable = (CatVariable)getVariable(wKey);
			int nx = xVariable.noOfCategories();
			int nz = zVariable.noOfCategories();
			int nw = wVariable.noOfCategories();
			
			int minHitDist = Integer.MAX_VALUE;
			int minHitOffset = 0;
			int hitX = -1;
			int hitZ = -1;
			int hitW = -1;
			Point p = null;
			int[] cat = new int[3];
			
			for (int i=0 ; i<nx ; i++) {
				cat[0] = i;
				for (int j=0 ; j<nz ; j++) {
					cat[1] = j;
					for (int k=0 ; k<nw ; k++) {
						cat[2] = k;
						if (model.canDragMean(cat)) {
							p = getScreenPoint(model.evaluateMean(cat), i, j, nx, nz, p);
							if (p != null) {
								int xDist = x - p.x;
								int yDist = y - p.y;
								int thisHitDist = xDist * xDist + yDist * yDist;
								if (thisHitDist <= minHitDist) {
									
									hitX = i;
									hitZ = j;
									hitW = k;
									minHitDist = thisHitDist;
									minHitOffset = yDist;
								}
							}
						}
					}
				}
			}
			
			if (minHitDist <= kMinHitDist)
				return new VertDragPosInfo(y, (hitX * nz + hitZ) * nw + hitW, minHitOffset);
		}
			return super.getInitialPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (dragX >= 0 && dragZ >= 0 && dragW >= 0)
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
			CatVariable wVariable = (CatVariable)getVariable(wKey);
			int nw = wVariable.noOfCategories();
			
			int codedPos = posInfo.index;
			dragW = codedPos % nw;
			codedPos /= nw;
			
			dragX = codedPos / nz;
			dragZ = codedPos % nz;
			hitOffset = posInfo.hitOffset;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (dragX >= 0 && dragZ >= 0 && dragW >= 0) {
			MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
			
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
				int[] cat = {dragX, dragZ, dragW};
				model.setDragMean(cat, newMean);
			} catch (AxisException e) {
			}
			getData().variableChanged(modelKey);
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragX >= 0 && dragZ >= 0 && dragW >= 0) {
			dragX = dragZ = dragW = -1;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}

}
	
