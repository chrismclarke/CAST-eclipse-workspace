package graphics;

import java.awt.*;

import dataView.*;
import axis.*;


public class ScatterCircleView extends DataView {
	
	static final public int DISPLAY_CROSSES = 0;
	static final public int DISPLAY_CIRCLES = 1;
	
	static final public Color kGroupColor[] = {Color.black, new Color(0x009900), new Color(0xAA0000), Color.blue, new Color(0x996600), new Color(0x660099)};
	static final private Color kGroupFillColor[];
	static {
		kGroupFillColor = new Color[kGroupColor.length];
		for (int i=0 ; i<kGroupFillColor.length ; i++)
			kGroupFillColor[i] = mixColors(kGroupColor[i], Color.white, 0.7);
	}
	
	static final public int kMaxRadius = 50;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	protected String sizeKey;
	private String xKey, yKey, groupKey;
	
	private int maxRadius = kMaxRadius;
	protected double maxSize;
	
	protected int displayType = DISPLAY_CROSSES;
	
	public ScatterCircleView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																String xKey, String yKey, String sizeKey, String groupKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xKey = xKey;
		this.yKey = yKey;
		this.sizeKey = sizeKey;
		this.groupKey = groupKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
		if (sizeKey != null)
			maxSize = calcMaxSize();
		
		setCrossSize(LARGE_CROSS);
	}
	
	public void setDrawType(int displayType) {
		this.displayType = displayType;
	}
	
	public void setSizeKey(String sizeKey) {
		this.sizeKey = sizeKey;
		maxSize = calcMaxSize();
	}
	
	public void setMaxRadius(int maxRadius) {
		this.maxRadius = maxRadius;
	}
	
	protected double calcMaxSize() {
		NumVariable sizeVar = (NumVariable)getVariable(sizeKey);
		double max = 0;
		ValueEnumeration se = sizeVar.values();
		while (se.hasMoreValues()) {
			double s = se.nextDouble();
			if (!Double.isNaN(s) && s > max)
				max = s;
		}
		return max;
	}
	
	protected Point getScreenPoint(double x, double y, Point p) {
		if (Double.isNaN(x) || Double.isNaN(y))
			return null;
		
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = xAxis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, p);
	}
	
	private void drawCurrentPoint(Point p, double size, int group, boolean selected, Graphics g) {
		if (p ==  null)
			return;
			
		if (displayType == DISPLAY_CIRCLES) {
			int radius = (int)Math.round(Math.sqrt(size / maxSize) * maxRadius);
			g.setColor(selected ? kGroupColor[group] : kGroupFillColor[group]);
			g.fillOval(p.x - radius, p.y - radius, 2 * radius, 2 * radius);
			g.setColor(selected ? Color.black : kGroupColor[group]);
			g.drawOval(p.x - radius, p.y - radius, 2 * radius, 2 * radius);
		}
		else {
			g.setColor(kGroupColor[group]);
			switch (group) {
				case 0:
					drawCross(g, p);
					break;
				case 1:
					drawCircle(g, p);
					break;
				case 2:
					drawPlus(g, p);
					break;
				case 3:
					drawSquare(g, p);
					break;
				case 4:
					drawDiamond(g, p);
					break;
			}
		}
	}
	
/*	private void drawSelectedBackground(Point p, double size, int group, Graphics g) {
		if (displayType == DISPLAY_CROSSES && p != null) {
			g.setColor(Color.red);
			drawCrossBackground(g, p);
		}
	}
*/	
	protected void drawSelectedBackground(Graphics g, NumValue xVal, NumValue yVal, NumValue sizeVal,
																												int group, Point p) {
		if (displayType == DISPLAY_CROSSES && p != null) {
			g.setColor(Color.red);
			drawCrossBackground(g, p);
		}
	}
	
	protected void drawSelectedForeground(Graphics g, NumValue xVal, NumValue yVal, NumValue sizeVal,
																												int group, Point p) {
	}
	
	public void paintView(Graphics g) {
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumVariable sizeVar = (NumVariable)getVariable(sizeKey);
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		Point p = null;
		
		int sortedSizeIndex[] = sizeVar.getSortedIndex();
		for (int i=sortedSizeIndex.length-1 ; i>=0 ; i--) {
			int index = sortedSizeIndex[i];
			boolean selected = getSelection().valueAt(index);
			
			if (selected) {
				NumValue yVal = (NumValue)yVar.valueAt(index);
				NumValue xVal = (NumValue)xVar.valueAt(index);
				NumValue sizeVal = (NumValue)sizeVar.valueAt(index);
				int group = groupVar.getItemCategory(index);
				
				double x = xVal.toDouble();
				double y = yVal.toDouble();
				p = getScreenPoint(x, y, p);
				
				drawSelectedBackground(g, xVal, yVal, sizeVal, group, p);
			}
		}
		
		for (int i=sortedSizeIndex.length-1 ; i>=0 ; i--) {
			int index = sortedSizeIndex[i];
				
			double x = xVar.doubleValueAt(index);
			double y = yVar.doubleValueAt(index);
			double size = sizeVar.doubleValueAt(index);
			int group = groupVar.getItemCategory(index);
			boolean selected = getSelection().valueAt(index);
			
			p = getScreenPoint(x, y, p);
			if (p != null)
				drawCurrentPoint(p, size, group, selected, g);
		}
		
		for (int i=sortedSizeIndex.length-1 ; i>=0 ; i--) {
			int index = sortedSizeIndex[i];
			boolean selected = getSelection().valueAt(index);
			
			if (selected) {
				NumValue yVal = (NumValue)yVar.valueAt(index);
				NumValue xVal = (NumValue)xVar.valueAt(index);
				NumValue sizeVal = (NumValue)sizeVar.valueAt(index);
				int group = groupVar.getItemCategory(index);
				
				double x = xVal.toDouble();
				double y = yVal.toDouble();
				p = getScreenPoint(x, y, p);
				
				drawSelectedForeground(g, xVal, yVal, sizeVal, group, p);
			}
		}
	}
	
//-----------------------------------------------------------------------------------
	
	private Point crossPos[];
	private static final int kMinCrossHitDist = 9;
	private static final int kMinCircleHitDist = 200;
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (crossPos == null) {
			NumVariable xVar = (NumVariable)getVariable(xKey);
			NumVariable yVar = (NumVariable)getVariable(yKey);
			int noOfVals = yVar.noOfValues();
			crossPos = new Point[noOfVals];
			for (int i=0 ; i<noOfVals ; i++)
				crossPos[i] = getScreenPoint(xVar.doubleValueAt(i), yVar.doubleValueAt(i), null);
		}
		
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<crossPos.length ; i++)
			if (crossPos[i] != null) {
				int xDist = crossPos[i].x - x;
				int yDist = crossPos[i].y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
					minDist = dist;
				}
			}
		int minValidDist = (displayType == DISPLAY_CIRCLES) ? kMinCircleHitDist : kMinCrossHitDist;
		if (gotPoint && minDist < minValidDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		getApplet().notifyDataChange(this);
		return super.startDrag(startInfo);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		super.endDrag(startPos, endPos);
		crossPos = null;
	}
	
}
	
