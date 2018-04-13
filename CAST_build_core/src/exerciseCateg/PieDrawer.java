package exerciseCateg;

import java.awt.*;

import dataView.*;


public class PieDrawer {
	static final public String DRAG_PIECHART = "dragPieChart";
	
	static final private double kDegToRad = Math.PI / 180;
	static final private int kMarkRadius = 8;
	
	static final private Color catColor[] = {new Color(0x006600),
																					new Color(0xFF3333),
																					new Color(0x0066FF),
																					new Color(0xFF6600),
																					new Color(0xCC66FF),
																					new Color(0x990099),
																					new Color(0x009999),
																					new Color(0xFF9966),
																					new Color(0x669933),
																					new Color(0x666666)};
	
	static final private Color catDimColor[] = new Color[catColor.length];
	static {
		for (int i=0 ; i<catColor.length ; i++)
			catDimColor[i] = DataView.dimColor(catColor[i], 0.5);
	}
		
	static final public int noOfColors() {
		return catColor.length;
	}
	
	private int radius, left, top, cx, cy;
	private int[] colorPerm = null;
	
	
	public int[] getColorPerm() {
		if (colorPerm == null) {
			colorPerm = new int[catColor.length];
			for (int i=0 ; i<colorPerm.length ; i++)
				colorPerm[i] = i;
		}
		return colorPerm;
	}
	
	public Color getCatColor(int catIndex) {
		if (colorPerm != null)
			catIndex = colorPerm[catIndex];
		return catColor[catIndex];
	}
	
	public Color getCatDimColor(int catIndex) {
		if (colorPerm != null)
			catIndex = colorPerm[catIndex];
		return catDimColor[catIndex];
	}
	
	public void setRadius(int newRadius, DataView theView) {
		radius = newRadius;
		left = theView.getSize().width / 2 - radius;
		top = theView.getSize().height / 2 - radius;
		cx = left + radius;
		cy = top + radius;
	}
	
	private int getXShift(int angle, int radius) {
		return (int)Math.round(radius * Math.cos(angle * kDegToRad));
	}
	
	private int getYShift(int angle, int radius) {
		return (int)Math.round(radius * Math.sin(angle * kDegToRad));
	}
	
	public void drawRadius(Graphics g, int angle, int radius) {
		int dx = getXShift(angle, radius);
		int dy = getYShift(angle, radius);
		for (int xi=0 ; xi<2 ; xi++)
			for (int yi=0 ; yi<2 ; yi++)
				g.drawLine(cx - xi, cy - yi, cx - xi + dx, cy - yi - dy);
	}
	
	private int getDegrees(double propn) {
		return 90 - (int)Math.round(propn * 360.0);
	}
	
	private Point findPoint(int radius, int degrees) {
		int x = cx + getXShift(degrees, radius);
		int y = cy - getYShift(degrees, radius);
		return new Point(x, y);
	}
	
	public void fillPieSegments(Graphics g, double[] cumCount, int minHiliteCat, int maxHiliteCat) {
		int nCats = cumCount.length;
		double totalCount = cumCount[cumCount.length - 1];
		
		int startAngle = 90;
		for (int i=0 ; i<nCats ; i++) {
			int endAngle = getDegrees(cumCount[i] / totalCount);
			if (endAngle < 0)
				endAngle += 360;
			int degrees = endAngle - startAngle;
			if (degrees > 0)
				degrees -= 360;
			if (cumCount[i] == cumCount[cumCount.length - 1] && (i == 0 || cumCount[i-1] == 0))
				degrees = -360;
			if (degrees < 0) {
				g.setColor((minHiliteCat < 0 || minHiliteCat <= i && maxHiliteCat >= i) ? getCatColor(i) : getCatDimColor(i));
				g.fillArc(left, top, 2 * radius, 2 * radius, startAngle, degrees);
			}
			startAngle = endAngle;
		}
	}
	
	private int getArcRadius() {
		return radius * 2 / 3;
	}
	
	public void drawArc(Graphics g, double[] cumCount, int minHiliteCat, int maxHiliteCat) {
		double totalCount = cumCount[cumCount.length - 1];
		double lowCum = (minHiliteCat > 0) ? cumCount[minHiliteCat - 1] : 0;
		double highCum = cumCount[maxHiliteCat];
		int lowDegrees = getDegrees(lowCum / totalCount);
		int highDegrees = getDegrees(highCum / totalCount);
		
		drawRadius(g, lowDegrees, radius);
		drawRadius(g, highDegrees, radius);
		
		int arcRadius = getArcRadius();
		int arcDegrees = highDegrees - lowDegrees;
		for (int i=0 ; i<3 ; i++) {
			g.drawArc(cx - arcRadius, cy - arcRadius, 2 * arcRadius, 2 * arcRadius, lowDegrees, arcDegrees);
			arcRadius --;
		}
	}
	
	public void markWrongCats(Graphics g, double[] cumCount, boolean[] wrongCats) {
		int nCats = cumCount.length;
		double totalCount = cumCount[cumCount.length - 1];
		
		g.setColor(Color.yellow);
		for (int i=0 ; i<nCats ; i++)
			if (wrongCats[i]) {
				double lowCum = (i > 0) ? cumCount[i - 1] : 0;
				double highCum = cumCount[i];
				int midDegrees = (getDegrees(lowCum / totalCount) + getDegrees(highCum / totalCount)) / 2;
				int markRadius = radius * 2 / 3;
				Point midMarkP = findPoint(markRadius, midDegrees);
				g.fillOval(midMarkP.x - kMarkRadius, midMarkP.y - kMarkRadius, 2 * kMarkRadius, 2 * kMarkRadius);
			}
	}
	
	public Point findMidSegment(double[] cumCount, int minHiliteCat, int maxHiliteCat) {
		double totalCount = cumCount[cumCount.length - 1];
		
		double lowCum = (minHiliteCat > 0) ? cumCount[minHiliteCat - 1] : 0;
		double highCum = cumCount[maxHiliteCat];
		int lowDegrees = getDegrees(lowCum / totalCount);
		int highDegrees = getDegrees(highCum / totalCount);
	
		int midDegrees = (lowDegrees + highDegrees) / 2;
		if (midDegrees < 0)
			midDegrees += 360;
		
		int arcRadius = getArcRadius();
		return findPoint(arcRadius, midDegrees);
	}
	
	
	public double findPropn(int x, int y) {
		int dx = x - cx;
		int dy = y - cy;
		
		double proportion = 0;
		if (dx == 0)
			proportion = (dy > 0) ? 0.5 : 0.0;
		else {
			proportion = 0.25 + Math.atan((double)dy / dx) / (2.0 * Math.PI);
			if (dx <= 0.0)
				proportion += 0.5;
			if (proportion >= 1.0)
				proportion -= 1.0;
		}
		return proportion;
	}
}