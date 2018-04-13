package exerciseSD;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import formula.*;


public class StackedPlusSdView extends DataView {
//	static public final String STACKED_PLUS_SD = "stackedPlusSd";
	
	static final private int kRightBorder = 10;
	static final private int kLeftBorder = 20;
	static final private int kMeanSdGap = 4;
	static final private int kGroupNameTopBottom = 4;
	static final private int kMinStackTopBorder = 10;
	
	static final private Color kSdColor = new Color(0x990000);
	static final private Color kSdBackgroundColor = new Color(0xFFFF66);
	static final private Color kGroupNameColor = new Color(0xFFDDDD);
	static final private Color kGridColor = new Color(0xDDDDDD);
	static final private Color kOutlierColor = Color.black;
	
	private String yKey, groupKey;
	private HorizAxis theAxis;
	
	private Font groupFont, meanSdFont, outlierFont;
	
	private NumValue meanValue, sdValue;
	
	private boolean showStatistics = true;
	private boolean showGrid = true;
	
	public StackedPlusSdView(DataSet theData, XApplet applet, HorizAxis theAxis, String yKey,
																														String groupKey, int sdDecimals) {
		super(theData, applet, new Insets(0,5,0,5));
		
		this.yKey = yKey;
		this.groupKey = groupKey;
		this.theAxis = theAxis;
		meanValue = new NumValue(0.0, sdDecimals);
		sdValue = new NumValue(0.0, sdDecimals);
		
		meanSdFont = applet.getBigFont();
		outlierFont = applet.getBigBoldFont();
		groupFont = new Font(meanSdFont.getName(), Font.BOLD, meanSdFont.getSize() * 3);
	}
	
	public void setSdDecimals(int decimals) {
		meanValue.decimals = decimals;
		sdValue.decimals = decimals;
	}
	
	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}
	
	public void setShowStatistics(boolean showStatistics) {
		this.showStatistics = showStatistics;
	}
	
	private void paintGrid(Graphics g) {
		g.setColor(kGridColor);
		
		Vector labels = theAxis.getLabels();
		Enumeration le = labels.elements();
		while (le.hasMoreElements()) {
			AxisLabel label = (AxisLabel)le.nextElement();
			double x = theAxis.minOnAxis + label.position * (theAxis.maxOnAxis - theAxis.minOnAxis);
			int xPos = getViewBorder().left + theAxis.numValToRawPosition(x);
			g.drawLine(xPos, 0, xPos, getSize().height);
		}
	}
	
	private void drawMeanSd(Graphics g, NumValue mean, NumValue sd, int baseline,
													int[] groupStackCount, int crossColWidth, int crossHt, int minVertOffset) {
		g.setFont(meanSdFont);
		FontMetrics fm = g.getFontMetrics();
		String sEqualsString = "s = ";
		String meanEqualsString = MText.expandText("x#bar# = ");
		
		int sEqualsWidth = fm.stringWidth(sEqualsString);
		int xBarEqualsWidth = fm.stringWidth(meanEqualsString);
		int sdWidth = sd.stringWidth(g);
		int meanWidth = mean.stringWidth(g);
		
		int totalWidth = Math.max(xBarEqualsWidth, sEqualsWidth) + Math.max(meanWidth, sdWidth);
		int nCrossesCovered = totalWidth / crossColWidth + 2;
		int maxStack = 0;
		for (int i=groupStackCount.length-nCrossesCovered ; i<groupStackCount.length ; i++)
			maxStack = Math.max(maxStack, groupStackCount[i]);
		
		baseline -= Math.max(minVertOffset + 3, maxStack * crossHt + 6);
		
		g.setColor(kSdBackgroundColor);
		boolean showSD = !Double.isNaN(sd.toDouble());
		int ascent = fm.getAscent();
		int boxRight = getSize().width - kRightBorder / 2;
		int boxLeft = boxRight - kRightBorder - totalWidth;
		int boxBottom = baseline + 4;
		int boxTop = boxBottom - ascent - 6;
		if (showSD)
			boxTop -= ascent + kMeanSdGap;
		g.fillRect(boxLeft, boxTop, (boxRight - boxLeft), (boxBottom - boxTop));
		
		g.setColor(kSdColor);
		if (showSD) {
			g.drawString(sEqualsString, getSize().width - kRightBorder - totalWidth, baseline);
			sd.drawRight(g, getSize().width - kRightBorder - sdWidth, baseline);
			baseline -= ascent + kMeanSdGap;
		}
		g.drawString(meanEqualsString, getSize().width - kRightBorder - totalWidth, baseline);
		mean.drawRight(g, getSize().width - kRightBorder - meanWidth, baseline);
	}
	
	public void paintView(Graphics g) {
		if (showGrid)
			paintGrid(g);
		
		g.setFont(groupFont);
		int groupAscent = g.getFontMetrics().getAscent();
		int groupDescent = g.getFontMetrics().getDescent();
		
		CatVariable groupVar = null;
		int nGroups = 1;
		if (groupKey != null) {
			groupVar = (CatVariable)getVariable(groupKey);
			nGroups = groupVar.noOfCategories();
		}
		
		double sy[] = new double[nGroups];
		double syy[] = new double[nGroups];
		int n[] = new int[nGroups];
		
		int stackWidth = getCrossSize() * 2 + 3;
		double stackStep = 0;
		try {
			stackStep = theAxis.positionToNumVal(stackWidth) - theAxis.positionToNumVal(0);
		} catch (AxisException e) {
		}
		
		int nStacks = (int)Math.round(Math.ceil((theAxis.maxOnAxis - theAxis.minOnAxis) / stackStep));
		int stackCount[][] = new int[nGroups][nStacks];
		int maxStackCount[] = new int[nGroups];
		NumValue offAxisValue[] = new NumValue[nGroups];
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration ge = (groupVar == null) ? null : groupVar.values();
		
		while (ye.hasMoreValues()) {
			NumValue yVal = (NumValue)ye.nextValue();
			double y = yVal.toDouble();
			int group = (groupVar == null) ? 0 : groupVar.labelIndex(ge.nextValue());
			sy[group] += y;
			syy[group] += y * y;
			n[group] ++;
			try {
				int stack = theAxis.numValToPosition(y) / stackWidth;
				stackCount[group][stack] ++;
				maxStackCount[group] = Math.max(maxStackCount[group], stackCount[group][stack]);
			} catch (AxisException e) {
				if (yVal.toDouble() == Math.rint(yVal.toDouble()))		//	if integer, then probably code for missing value, so don't show decimals
					offAxisValue[group] = new NumValue(yVal.toDouble(), 0);
				else
					offAxisValue[group] = yVal;
			}
		}
		
		@SuppressWarnings("unused")
		int stackTotal = 0;
		int minStackTotal = Integer.MAX_VALUE;
		for (int i=0 ; i<nGroups ; i++) {
			stackTotal += maxStackCount[i];
			minStackTotal = Math.min(minStackTotal, maxStackCount[i]);
		}
		
		int groupNameHt = groupAscent + groupDescent + 2 * kGroupNameTopBottom;
		int crossHt = stackWidth;
		int groupHt[] = new int[nGroups];
		for (crossHt=stackWidth ; crossHt>0 ; crossHt--) {
			int heightUsed = 0;
			boolean constrainedByCrosses = false;
			for (int i=0 ; i<nGroups ; i++) {
				int crossStackHt = crossHt * maxStackCount[i] + kMinStackTopBorder;
				if (crossStackHt > groupNameHt)
					constrainedByCrosses = true;
				groupHt[i] = Math.max(groupNameHt, crossStackHt);
				heightUsed += groupHt[i];
			}
			if (heightUsed <= getSize().height || !constrainedByCrosses) {
				int extraGap = (getSize().height - heightUsed) / nGroups;
				for (int i=0 ; i<nGroups ; i++)
					groupHt[i] += extraGap;
				break;
			}
		}
		
		int baseline = getSize().height;
		Point p = new Point(0, 0);
		for (int i=0 ; i<nGroups ; i++) {
			if (groupVar != null) {
				g.setFont(groupFont);
				g.setColor(kGroupNameColor);
				groupVar.getLabel(i).drawRight(g, kLeftBorder, baseline - groupDescent - kGroupNameTopBottom);
			}
			
			if (showStatistics) {
				meanValue.setValue(sy[i] / n[i]);
				sdValue.setValue(Math.sqrt((syy[i] - sy[i] * sy[i] / n[i]) / (n[i] - 1)));
				int minOffset = (offAxisValue[i] == null || offAxisValue[i].toDouble() < theAxis.minOnAxis)
																																			? 10 : (2 * kHalfArrowHt + 1);
				drawMeanSd(g, meanValue, sdValue, baseline, stackCount[i], stackWidth, crossHt, minOffset);
			}
		
			g.setColor(getForeground());
			g.drawLine(0, baseline, getSize().width, baseline);
			for (int j=0 ; j<nStacks ; j++) {
				p.x = getViewBorder().left + j * stackWidth + stackWidth / 2;
				p.y = baseline - stackWidth / 2 - 1;
				for (int k=0 ; k<stackCount[i][j] ; k++)  {
					drawCross(g, p);
					p.y -= crossHt;
				}
			}
			
			if (offAxisValue[i] != null) {
				g.setFont(outlierFont);
				g.setColor(kOutlierColor);
				if (offAxisValue[i].toDouble() > theAxis.maxOnAxis) {
					drawArrow(g, getSize().width - 3, baseline - kHalfArrowHt - 1, -1);
					offAxisValue[i].drawLeft(g, getSize().width - kArrowLength - 6, baseline - 4);
				}
				else {
					drawArrow(g, 3, baseline - kHalfArrowHt - 1, 1);
					offAxisValue[i].drawRight(g, kArrowLength + 6, baseline - 4);
				}
			}
			
			baseline -= groupHt[i];
		}
	}
	
	static final private int kArrowLength = 16;
	static final private int kHalfArrowHt = 9;
	static final private int kHalfArrowStem = 4;
	
	private void drawArrow(Graphics g, int x, int y, int direction) {
		int xCoord[] = new int[8];
		int yCoord[] = new int[8];
		xCoord[0] = xCoord[7] = x;
		xCoord[1] = xCoord[2] = xCoord[5] = xCoord[6] = x + direction * kHalfArrowHt;
		xCoord[3] = xCoord[4] = x + direction * kArrowLength;
		
		yCoord[0] = yCoord[7] = y;
		yCoord[1] = y - kHalfArrowHt;
		yCoord[6] = y + kHalfArrowHt;
		yCoord[2] = yCoord[3] = y - kHalfArrowStem;
		yCoord[4] = yCoord[5] = y + kHalfArrowStem;
		
		g.fillPolygon(xCoord, yCoord, 8);
	}
	

//-----------------------------------------------------------------------------------

	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}