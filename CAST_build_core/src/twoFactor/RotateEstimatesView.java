package twoFactor;

import java.awt.*;

import dataView.*;
import random.*;
import graphics3D.*;


public class RotateEstimatesView extends Rotate3DView {
	
	static public final int MODEL = 0;
	static public final int X_EFFECT = 1;
	static public final int Z_EFFECT = 2;
	
	static final private double kMeanCrossOffset = 0.1;
	static final private double kArrowOffset = 0.1;
	
	static final private Color kMeanColor = Color.red;
	
	static final private Color kXDimSlopeColor = new Color(0x66FF66);
	static final public Color kXArrowColor = new Color(0x009900);
	static final private Color kZDimSlopeColor = new Color(0x9999FF);
	static final public Color kZArrowColor = Color.blue;
//	static final private Color kGrayColor = new Color(0xCCCCCC);
	
	static final private int kSmallCrossN = 20;		//	small crosses if n >= 20;
	static final private int kMaxJitter = 5;
	
	private String modelKey;
	
	private double mean[][] = new double[2][2];
	private int displayEffect = MODEL;
	
	private int jitter[] = null;
	
	public RotateEstimatesView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey, String modelKey, int displayEffect) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		this.modelKey = modelKey;
		initialiseMeans(theData);
		this.displayEffect = displayEffect;
	}
	
	public void setDisplayEffect(int displayEffect) {
		this.displayEffect = displayEffect;
	}
	
	private void initialiseMeans(DataSet data) {
		int n[][] = new int[2][2];
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		CatVariable zVariable = (CatVariable)getVariable(zKey);
		if (xVariable.noOfCategories() != 2 || zVariable.noOfCategories() != 2)
			throw new RuntimeException("Wrong number of categories for X or Z. They must both be 2");
		
		for (int i=0 ; i<2 ; i++)
			for (int j=0 ; j<2 ; j++)
				mean[i][j] = 0.0;
			
		ValueEnumeration ye = yVariable.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int xCat = xVariable.getItemCategory(index);
			int zCat = zVariable.getItemCategory(index);
			if (!Double.isNaN(y)) {
				n[xCat][zCat] ++;
				mean[xCat][zCat] += y;
			}
			index ++;
		}
		
		for (int i=0 ; i<2 ; i++)
			for (int j=0 ; j<2 ; j++)
				mean[i][j] /= n[i][j];
	}
	
	protected Point getScreenPoint(double y, double x, double z, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		double yFract = yAxis.numValToPosition(y);
		double xFract = (x + 0.5) / 2.0;
		double zFract = (z + 0.5) / 2.0;
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		g.setColor(kMeanColor);
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<2 ; i++)
			for (int j=0 ; j<2 ; j++) {
				p0 = getScreenPoint(mean[i][j], i, j - kMeanCrossOffset, p0);
				p1 = getScreenPoint(mean[i][j], i, j + kMeanCrossOffset, p1);
				if (p0 != null && p1 != null)
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
				p0 = getScreenPoint(mean[i][j], i - kMeanCrossOffset, j, p0);
				p1 = getScreenPoint(mean[i][j], i + kMeanCrossOffset, j, p1);
				if (p0 != null && p1 != null)
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		
		if (drawData) {
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			
			Point crossPos = null;
			int n = yVariable.noOfValues();
			if (n >= kSmallCrossN) {
				setCrossSize(MEDIUM_CROSS);
				if (jitter == null || jitter.length != n)
					jitter = new RandomInteger(-kMaxJitter, kMaxJitter, n).generate();
			}
			else {
				setCrossSize(LARGE_CROSS);
				jitter = null;
			}
			
			int index = 0;
			ValueEnumeration ye = yVariable.values();
			g.setColor(getForeground());
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				int xCat = xVariable.getItemCategory(index);
				int zCat = zVariable.getItemCategory(index);
				crossPos = getScreenPoint(y, xCat, zCat, crossPos);
				if (crossPos != null) {
					if (jitter != null)
						crossPos.x += jitter[index];
					drawCross(g, crossPos);
				}
				index ++;
			}
		}
			
		drawArrows(g);
	}
	
	private void drawArrows(Graphics g) {
		Point p = null;
		Point q = null;
		
		if (displayEffect == X_EFFECT) {
			g.setColor(kXArrowColor);
			for (int z=0 ; z<2 ; z++) {
				p = getScreenPoint(mean[0][z], 1 + kArrowOffset, z, p);
				q = getScreenPoint(mean[1][z], 1 + kArrowOffset, z, q);
				if (p != null && q != null)
					drawOneArrow(g, p, q);
			}
		}
		else if (displayEffect == Z_EFFECT) {
			g.setColor(kZArrowColor);
			for (int x=0 ; x<2 ; x++) {
				p = getScreenPoint(mean[x][0], x, 1 + kArrowOffset, p);
				q = getScreenPoint(mean[x][1], x, 1 + kArrowOffset, q);
				if (p != null && q != null)
					drawOneArrow(g, p, q);
			}
		}
	}
	
	private void drawOneArrow(Graphics g, Point p0, Point p1) {
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		if (p1.y > p0.y + 3) {
			g.drawLine(p0.x - 1, p0.y, p1.x - 1, p1.y - 1);
			g.drawLine(p0.x + 1, p0.y, p1.x + 1, p1.y - 1);
			int arrowHead = Math.min(p1.y - p0.y - 3, 4);
			if (arrowHead > 0) {
				g.drawLine(p1.x, p1.y, p1.x - arrowHead, p1.y - arrowHead);
				g.drawLine(p1.x, p1.y, p1.x + arrowHead, p1.y - arrowHead);
			}
		}
		else if (p1.y < p0.y - 3) {
			g.drawLine(p0.x - 1, p0.y, p1.x - 1, p1.y + 1);
			g.drawLine(p0.x + 1, p0.y, p1.x + 1, p1.y + 1);
			int arrowHead = Math.min(p0.y - p1.y - 3, 4);
			if (arrowHead > 0) {
				g.drawLine(p1.x, p1.y, p1.x - arrowHead, p1.y + arrowHead);
				g.drawLine(p1.x, p1.y, p1.x + arrowHead, p1.y + arrowHead);
			}
		}
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		TwoFactorModel model = (TwoFactorModel)getVariable(modelKey);
		Point p = null;
		Point q = null;
		
		if (displayEffect == MODEL) {
			g.setColor(kZDimSlopeColor);
			for (int x=0 ; x<2 ; x++) {
				p = getScreenPoint(model.evaluateMean(x, 0), x, 0, p);
				q = getScreenPoint(model.evaluateMean(x, 1), x, 1, q);
				if (p != null && q != null)
					g.drawLine(p.x, p.y, q.x, q.y);
			}
		
			g.setColor(kXDimSlopeColor);
			for (int z=0 ; z<2 ; z++) {
				p = getScreenPoint(model.evaluateMean(0, z), 0, z, p);
				q = getScreenPoint(model.evaluateMean(1, z), 1, z, q);
				if (p != null && q != null)
					g.drawLine(p.x, p.y, q.x, q.y);
			}
		}
		
		else if (displayEffect == Z_EFFECT) {
			g.setColor(kZDimSlopeColor);
			for (int x=0 ; x<2 ; x++) {
				p = getScreenPoint(mean[x][0], x, 0, p);
				q = getScreenPoint(mean[x][1], x, 1, q);
				if (p != null && q != null) {
					g.drawLine(p.x, p.y, q.x, q.y);
					q = getScreenPoint(mean[x][0], x, 1 + kArrowOffset, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
		}
		
		else if (displayEffect == X_EFFECT) {
			g.setColor(kXDimSlopeColor);
			for (int z=0 ; z<2 ; z++) {
				p = getScreenPoint(mean[0][z], 0, z, p);
				q = getScreenPoint(mean[1][z], 1, z, q);
				if (p != null && q != null) {
					g.drawLine(p.x, p.y, q.x, q.y);
					q = getScreenPoint(mean[0][z], 1 + kArrowOffset, z, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
		}
		
		return null;
	}

//-------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(modelKey))
			repaint();
		else if (key.equals(yKey) || key.equals(xKey) || key.equals(zKey)) {
			initialiseMeans(getData());
			repaint();
		}
	}
}
	
