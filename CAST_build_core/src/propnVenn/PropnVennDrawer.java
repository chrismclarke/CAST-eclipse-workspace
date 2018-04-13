package propnVenn;

import java.awt.*;

import dataView.*;
import axis.*;

import contin.*;


public class PropnVennDrawer {
	static final private Color[] outlineColor = RotateContinView.outlineColor;
	
	static final private int kProbDecimals = 3;
	static final private int kArrowGap = 2;
	static final private int kPropnDecimals = 3;
	
	private VertAxis yAxis;
	private HorizAxis xAxis;
	private DataView theView;
	double minYP, maxYP, diffYP;
	
	protected double[][] yConditXProb, xConditYProb;
	protected double[] xMarginalProb, yMarginalProb;
	
	protected double[][] yCumConditXProb, xCumConditYProb;
	protected double[] xCumMarginalProb, yCumMarginalProb;
	
	private int ascent;
	
	private Color[] fillColor = RotateContinView.fillColor;
	
	public PropnVennDrawer(double minYP, double maxYP, HorizAxis xAxis, VertAxis yAxis,
																												DataView theView) {
		this.minYP = minYP;
		this.maxYP = maxYP;
		diffYP = maxYP - minYP; 
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.theView = theView;
	}
	
	public void setFillColors(Color[] fillColor) {
		this.fillColor = fillColor;
	}
	
	public double getXMarginProb(int x) {
		return (x >= 0) ? xMarginalProb[x] : 0.0;
	}
	
	public double getYMarginProb(int y) {
		return (y >= 0) ? yMarginalProb[y] : 0.0;
	}
	
	public double getXConditProb(int x, int y) {
		return (x >= 0 && y >= 0) ? xConditYProb[y][x] : 0.0;
	}
	
	public double getYConditProb(int y, int x) {
		return (x >= 0 && y >= 0) ? yConditXProb[x][y] : 0.0;
	}
	
	public void initialise(double xMarginalProb[], double yConditXProb[][]) {
		FontMetrics fm = theView.getGraphics().getFontMetrics();
		ascent = fm.getAscent();
		
		int nXCats = xMarginalProb.length;
		int nYCats = yConditXProb[0].length;
		
		this.xMarginalProb = xMarginalProb;
		this.yConditXProb = yConditXProb;
		
		boolean reallocateMemory = (yMarginalProb == null || yMarginalProb.length != nYCats)
														|| (xConditYProb == null || xConditYProb.length != nXCats);
		
		if (reallocateMemory)
			yMarginalProb = new double[nYCats];
		if (reallocateMemory)
			xConditYProb = new double[nYCats][];
		for (int j=0 ; j<nYCats ; j++) {
			yMarginalProb[j] = 0.0;
			for (int i=0 ; i<nXCats ; i++)
				yMarginalProb[j] += xMarginalProb[i] * yConditXProb[i][j];
			if (reallocateMemory)
				xConditYProb[j] = new double[nXCats];
			for (int i=0 ; i<nXCats ; i++)
				xConditYProb[j][i] = (yMarginalProb[j] >= 0.0)
										? xMarginalProb[i] * yConditXProb[i][j] / yMarginalProb[j]
										: 1.0 / nYCats;
		}
		
		if (reallocateMemory) {
			xCumMarginalProb = new double[nXCats + 1];
			xCumConditYProb = new double[nYCats + 2][];
			for (int j=0 ; j<nYCats + 2 ; j++)
				xCumConditYProb[j] = new double[nXCats + 1];
		}
		
		for (int i=1 ; i<=nXCats ; i++) {
			xCumMarginalProb[i] = xCumMarginalProb[i-1] + xMarginalProb[i-1];
			for (int j=1 ; j<=nYCats ; j++)
				xCumConditYProb[j][i] = xCumConditYProb[j][i-1] + xConditYProb[j-1][i-1];
			xCumConditYProb[0][i] = xCumConditYProb[1][i];
			xCumConditYProb[nYCats+1][i] = xCumConditYProb[nYCats][i];
		}
		
		if (reallocateMemory) {
			yCumMarginalProb = new double[nYCats + 1];
			yCumConditXProb = new double[nXCats + 2][];
			for (int i=0 ; i<nXCats + 2 ; i++)
				yCumConditXProb[i] = new double[nYCats + 1];
		}
		
		for (int j=1 ; j<=nYCats ; j++) {
			yCumMarginalProb[j] = yCumMarginalProb[j-1] + yMarginalProb[j-1];
			for (int i=1 ; i<=nXCats ; i++)
				yCumConditXProb[i][j] = yCumConditXProb[i][j-1] + yConditXProb[i-1][j-1];
			yCumConditXProb[0][j] = yCumConditXProb[1][j];
			yCumConditXProb[nXCats+1][j] = yCumConditXProb[nXCats][j];
		}
	}
	
	private void addPoint(double x1, double y1, double x2, double y2, Polygon p, double propn,
										Point pt) {
		double x = x1 * propn + x2 * (1.0 - propn);
		double y = y1 * propn + y2 * (1.0 - propn);
		
		int xPos = xAxis.numValToRawPosition(x);
		int yPos = yAxis.numValToRawPosition(y * diffYP + minYP);
		
		pt = theView.translateToScreen(xPos, yPos, pt);
		
		p.addPoint(pt.x, pt.y);
	}
	
	private Polygon getPolygon(int y, int x, double framePropn, Point pt) {
		Polygon p = new Polygon();
		
		if (yCumConditXProb[x+1][y+1] > yCumConditXProb[x][y+1]) {
			addPoint(xCumMarginalProb[x], yCumConditXProb[x][y+1],
						xCumConditYProb[y+1][x], yCumMarginalProb[y+1], p, framePropn, pt);
		
			addPoint(xCumMarginalProb[x], yCumConditXProb[x+1][y+1],
						xCumConditYProb[y+2][x], yCumMarginalProb[y+1], p, framePropn, pt);
		}
		else
			addPoint(xCumMarginalProb[x], yCumConditXProb[x+1][y+1],
						xCumConditYProb[y+1][x], yCumMarginalProb[y+1], p, framePropn, pt);
		
		
		if (yCumConditXProb[x+1][y+1] > yCumConditXProb[x+2][y+1]) {
			addPoint(xCumMarginalProb[x+1], yCumConditXProb[x+1][y+1],
							xCumConditYProb[y+2][x+1], yCumMarginalProb[y+1], p, framePropn, pt);
		
			addPoint(xCumMarginalProb[x+1], yCumConditXProb[x+2][y+1],
						xCumConditYProb[y+1][x+1], yCumMarginalProb[y+1], p, framePropn, pt);
		}
		else
			addPoint(xCumMarginalProb[x+1], yCumConditXProb[x+1][y+1],
							xCumConditYProb[y+1][x+1], yCumMarginalProb[y+1], p, framePropn, pt);
		
		
		
		if (yCumConditXProb[x+2][y] > yCumConditXProb[x+1][y]) {
			addPoint(xCumMarginalProb[x+1], yCumConditXProb[x+2][y],
						xCumConditYProb[y+1][x+1], yCumMarginalProb[y], p, framePropn, pt);
		
			addPoint(xCumMarginalProb[x+1], yCumConditXProb[x+1][y],
						xCumConditYProb[y][x+1], yCumMarginalProb[y], p, framePropn, pt);
		}
		else
			addPoint(xCumMarginalProb[x+1], yCumConditXProb[x+1][y],
						xCumConditYProb[y+1][x+1], yCumMarginalProb[y], p, framePropn, pt);
		
		
		if (yCumConditXProb[x][y] > yCumConditXProb[x+1][y]) {
			addPoint(xCumMarginalProb[x], yCumConditXProb[x+1][y],
						xCumConditYProb[y][x], yCumMarginalProb[y], p, framePropn, pt);
		
			addPoint(xCumMarginalProb[x], yCumConditXProb[x][y],
						xCumConditYProb[y+1][x], yCumMarginalProb[y], p, framePropn, pt);
		}
		else
			addPoint(xCumMarginalProb[x], yCumConditXProb[x+1][y],
						xCumConditYProb[y+1][x], yCumMarginalProb[y], p, framePropn, pt);
		
		return p;
	}
	
	protected Rectangle getBoundingRect(int x, int y, double framePropn) {
		if (x < 0 || y < 0)
			return null;
		Point pt = new Point(0, 0);
		Polygon p = getPolygon(y, x, framePropn, pt);
		return p.getBounds();
	}
	
	private void insetPolygon(int y, int x, int dLeft, int dTop, int dRight, int dBottom,
																		Polygon p) {
		int left = p.xpoints[0];
		int right = left;
		int top = p.ypoints[0];
		int bottom = top;
		for (int i=1 ; i<p.npoints ; i++) {
			int newX = p.xpoints[i];
			int newY = p.ypoints[i];
			if (newX < left) left = newX;
			if (newX > right) right = newX;
			if (newY < top) top = newY;
			if (newY > bottom) bottom = newY;
		}
		
		int width = right - left;
		int height = bottom - top;
		
		if (width < dLeft + dRight)
			dLeft = dRight = 0;
		if (height < dTop + dBottom)
			dTop = dBottom = 0;
		
		int i = 0;
		
		p.xpoints[i] += dLeft;
		p.ypoints[i++] += dTop;
		if (yCumConditXProb[x+1][y+1] > yCumConditXProb[x][y+1]) {
			p.xpoints[i] += dLeft;
			p.ypoints[i++] += dTop;
		}
		
		p.xpoints[i] -= dRight;
		p.ypoints[i++] += dTop;
		if (yCumConditXProb[x+1][y+1] > yCumConditXProb[x+2][y+1]) {
			p.xpoints[i] -= dRight;
			p.ypoints[i++] += dTop;
		}
		
		p.xpoints[i] -= dRight;
		p.ypoints[i++] -= dBottom;
		if (yCumConditXProb[x+2][y] > yCumConditXProb[x+1][y]) {
			p.xpoints[i] -= dRight;
			p.ypoints[i++] -= dBottom;
		}
		
		p.xpoints[i] += dLeft;
		p.ypoints[i++] -= dBottom;
		if (yCumConditXProb[x][y] > yCumConditXProb[x+1][y]) {
			p.xpoints[i] += dLeft;
			p.ypoints[i++] -= dBottom;
		}
	}
	
	public void drawDiagram(int selectedX, int selectedY, double framePropn, Graphics g) {
		int nYCats = yMarginalProb.length;
		int nXCats = xMarginalProb.length;
		
		Polygon p = null;
		Point pt = new Point(0, 0);
		
//		int xSelectedPos = 0;
//		int xSelectedWidth = 0;
//		int ySelectedPos = 0;
//		int ySelectedHeight = 0;
		
		for (int i=0 ; i<nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++) {
				p = getPolygon(j, i, framePropn, pt);
				
				boolean highlight = (i == selectedX && j == selectedY);
				
				g.setColor(highlight ? Color.yellow : fillColor[i]);
				g.fillPolygon(p);
				
				g.setColor(highlight ? Color.black : outlineColor[j]);
				insetPolygon(j, i, 0, 0, 1, 1, p);
				g.drawPolygon(p);
				
				Rectangle boundingRect = p.getBounds();
				if (boundingRect.width >= 5 && boundingRect.height >= 5) {
					insetPolygon(j, i, 1, 1, 1, 1, p);
					g.drawPolygon(p);
				}
			}
	}
	
	private double xCentre(int x, int y, double framePropn) {
		double startX = xCumMarginalProb[x] + 0.5 * xMarginalProb[x];
		double endX = xCumConditYProb[y+1][x] + 0.5 * xConditYProb[y][x];
		return startX * framePropn + endX * (1 - framePropn);
	}
	
	private double yCentre(int x, int y, double framePropn) {
		double startY = yCumConditXProb[x+1][y] + 0.5 * yConditXProb[x][y];
		double endY = yCumMarginalProb[y] + 0.5 * yMarginalProb[y];
		return startY * framePropn + endY * (1 - framePropn);
	}
	
	public void drawCounts(CatVariable xVar, CatVariable yVar, double framePropn, Graphics g, XApplet applet) {
		int nYCats = yVar.noOfCategories();
		int nXCats = xVar.noOfCategories();
		int count[][] = yVar.getCounts(xVar);
		
		NumValue v = new NumValue(0.0, 0);
		Point pt = new Point(0, 0);
		
		Font oldFont = g.getFont();
		g.setFont(applet.getBigFont());
		
		int baselineOffset = ascent / 2;
		
		for (int i=0 ; i<nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++) {
				double xp = xCentre(i, j, framePropn);
				double yp = yCentre(i, j, framePropn);
				
				int xPos = xAxis.numValToRawPosition(xp);
				int yPos = yAxis.numValToRawPosition(yp * diffYP + minYP);
				
				pt = theView.translateToScreen(xPos, yPos, pt);
				
				v.setValue(count[j][i]);
				v.drawCentred(g, pt.x, pt.y + baselineOffset);
			}
		g.setFont(oldFont);
	}
	
	public void drawJointProbs(CatVariableInterface xVar, CatVariableInterface yVar,
									double framePropn, Graphics g, XApplet applet) {
		int nYCats = yVar.noOfCategories();
		int nXCats = xVar.noOfCategories();
		
		NumValue v = new NumValue(0.0, kProbDecimals);
		Point pt = new Point(0, 0);
		
		Font oldFont = g.getFont();
		g.setFont(applet.getBigFont());
		
		int baselineOffset = ascent / 2;
		
		for (int i=0 ; i<nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++) {
				double xp = xCentre(i, j, framePropn);
				double yp = yCentre(i, j, framePropn);
				
				int xPos = xAxis.numValToRawPosition(xp);
				int yPos = yAxis.numValToRawPosition(yp * diffYP + minYP);
				
				pt = theView.translateToScreen(xPos, yPos, pt);
				
				v.setValue(xMarginalProb[i] * yConditXProb[i][j]);
				v.drawCentred(g, pt.x, pt.y + baselineOffset);
			}
		g.setFont(oldFont);
	}
	
	public void drawHorizArrow(int x, int y, double framePropn, Graphics g) {
		Rectangle r = getBoundingRect(x, y, framePropn);
		TopVennDrawer.drawHorizArrow(g, r.x, r.y + r.height / 2, r.width);
	}
	
	public void drawConditionalArrow(int x, int y, double framePropn, Graphics g) {
		if (x < 0 || y < 0 || (framePropn != 0.0 && framePropn != 1.0))
			return;
		
		int localAscent = g.getFontMetrics().getAscent();		//		perhaps different font
		Rectangle r = getBoundingRect(x, y, framePropn);
		int centreX = r.x + r.width / 2;
		int baseline = r.y + (r.height + localAscent - TopVennDrawer.kArrowSize - kArrowGap) / 2;
		
		TopVennDrawer.drawHorizArrow(g, r.x, baseline + kArrowGap, r.width);
		
		NumValue propn = new NumValue((framePropn == 1.0) ? xMarginalProb[x] : xConditYProb[y][x],
													kPropnDecimals);
		
		propn.drawCentred(g, centreX, baseline);
	}

//-----------------------------------------------------------------------------------

	public PositionInfo findHit(double yTarget, double xTarget, CatVariableInterface xVar,
										CatVariableInterface yVar, boolean marginForY) {
		int nXCats = xVar.noOfCategories();
		int nYCats = yVar.noOfCategories();
		
		if (marginForY) {
			for (int j=0 ; j<nYCats ; j++) {
				if (yTarget <= yCumMarginalProb[j+1]) {
					for (int i=0 ; i<nXCats ; i++) {
						if (xTarget <= xCumConditYProb[j+1][i+1])
							return new ContinCatInfo(i, j);
					}
				}
			}
		}
		else {
			for (int i=0 ; i<nXCats ; i++) {
				if (xTarget <= xCumMarginalProb[i+1]) {
					for (int j=0 ; j<nYCats ; j++) {
						if (yTarget <= yCumConditXProb[i+1][j+1])
							return new ContinCatInfo(i, j);
					}
				}
			}
		}
		
		return null;
	}
}
