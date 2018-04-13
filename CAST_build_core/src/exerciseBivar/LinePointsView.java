package exerciseBivar;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class LinePointsView extends DataView {
//	static public final String LINE_POINTS_PLOT = "linePoints";
	
	static final private int kArrowHead = 3;
	
//	static final private int kMaxExactError = 2;
//	static final private int kMaxCloseError = 7;
	
	static final private Color kLowColor = new Color(0x9966CC);
	static final private Color kHighColor = new Color(0x66CC99);
	static final private Color kCrossColor = new Color(0xBBBBBB);
	
	
	private String xKey, yKey, lineKey, xDataKey, yDataKey;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	private boolean showSlope = false;
	
	public LinePointsView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, new Insets(10, 10, 10, 10));
		this.xKey = xKey;
		this.yKey = yKey;
		this.lineKey = lineKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	public void setShowSlope(boolean showSlope) {
		if (this.showSlope != showSlope) {
			this.showSlope = showSlope;
			repaint();
		}
	}
	
	public void setDataKeys(String xDataKey, String yDataKey) {
		this.xDataKey = xDataKey;
		this.yDataKey = yDataKey;
	}

	
//-----------------------------------------------------------------
	
	private Point getScreenPoint(double x, double y, Point thePoint) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = xAxis.numValToRawPosition(x);
		
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	private Point drawMainArrows(Graphics g, NumValue xVal, NumValue yVal, int ascent, Color c) {
		double x = xVal.toDouble();
		double y = yVal.toDouble();
		Point p = getScreenPoint(x, y, null);
		
		g.setColor(c);
		g.drawLine(p.x, p.y, p.x, getSize().height);
		g.drawLine(p.x, getSize().height - 1, p.x - kArrowHead, getSize().height - kArrowHead - 1);
		g.drawLine(p.x, getSize().height - 1, p.x + kArrowHead, getSize().height - kArrowHead - 1);
		
		g.drawLine(p.x, p.y, 0, p.y);
		g.drawLine(0, p.y, kArrowHead, p.y - kArrowHead);
		g.drawLine(0, p.y, kArrowHead, p.y + kArrowHead);
		
		int baseline = p.y - kArrowHead - 1;
		if (baseline < ascent + 2)
			baseline = p.y + kArrowHead + ascent;
		yVal.drawRight(g, kArrowHead + 1, baseline);
		
		return p;
	}
	
	public void paintView(Graphics g) {
		if (xDataKey != null && yDataKey != null) {
			NumVariable xData = (NumVariable)getVariable(xDataKey);
			NumVariable yData = (NumVariable)getVariable(yDataKey);
			ValueEnumeration xe = xData.values();
			ValueEnumeration ye = yData.values();
			g.setColor(kCrossColor);
			Point p = null;
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				double x = xe.nextDouble();
				double y = ye.nextDouble();
				p = getScreenPoint(x, y, p);
				drawCross(g, p);
			}
		}
		
		g.setColor(Color.blue);
		LinearModel line = (LinearModel)getVariable(lineKey);
		line.drawMean(g, this, xAxis, yAxis);
		
		int ascent = g.getFontMetrics().getAscent();
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		NumValue x0 = (NumValue)xVar.valueAt(0);
		NumValue x1 = (NumValue)xVar.valueAt(1);
		NumValue y0 = (NumValue)yVar.valueAt(0);
		NumValue y1 = (NumValue)yVar.valueAt(1);
		
		Point p0 = drawMainArrows(g, x0, y0, ascent, kLowColor);
		Point p1 = drawMainArrows(g, x1, y1, ascent, kHighColor);
		
		if (showSlope) {
			g.setColor(Color.red);
			g.drawLine(p0.x, p0.y, p1.x, p0.y);
			g.drawLine(p1.x, p0.y, p1.x, p1.y);
			
			NumValue dx = new NumValue(x1.toDouble() - x0.toDouble(), Math.max(x0.decimals, x1.decimals));
			NumValue dy = new NumValue(y1.toDouble() - y0.toDouble(), Math.max(y0.decimals, y1.decimals));
			
			int dxBaseline = p0.y;
			if (dy.toDouble() > 0)
				dxBaseline += ascent + 3;
			else
				dxBaseline -= 3;
			dx.drawCentred(g, (p0.x + p1.x) / 2, dxBaseline);
			
			int dyWidth = dy.stringWidth(g);
			if (p1.x + dyWidth + 4 < getSize().width)
				dy.drawRight(g, p1.x + 4, (p0.y + p1.y + ascent) / 2);
			else
				dy.drawLeft(g, p1.x - 4, (p0.y + p1.y + ascent) / 2);
		}
	}
	

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
