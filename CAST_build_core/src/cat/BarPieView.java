package cat;

import java.awt.*;

import dataView.*;


public class BarPieView extends DataView {
	
	static final private int kHalfTransitions = 100;
	static final public int kTransitions = 2 * kHalfTransitions;
	static final private int kSegments = 200;
	static final private int kTopBottomBorder = 10;
	static final private int kLeftRightBorder = 10;

	static final private Color kCatColour[] = CatKey3View.kCatColour;
	
	protected String catKey;
	
	private boolean initialised = false;
	
	private double barY[], pieX[], pieY[];
	@SuppressWarnings("unused")
	private double xc, yc, r, x0, x1, y0, y1;
	
	private int tempX[] = new int[2 * kSegments + 3];
	private int tempY[] = new int[2 * kSegments + 3];
	
	private int cumProp[];
	
	public BarPieView(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.catKey = catKey;
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		int width = getSize().width - 2 * kLeftRightBorder;
		int height = getSize().height - 2 * kTopBottomBorder;
		
		xc = getSize().width / 2;
		yc = getSize().height / 2;
		r = Math.min(width, height) / 2;
		
		x0 = xc - r / 4;
		x1 = xc + r / 4;
		y0 = kTopBottomBorder;
		y1 = y0 + height;
		
		barY = new double[kSegments + 1];
		pieX = new double[kSegments + 1];
		pieY = new double[kSegments + 1];
		
		double doubleHt = height;
		
		for (int i=0 ; i<=kSegments ; i++) {
			barY[i] = y0 + (i * doubleHt) / kSegments;
			pieX[i] = xc + r * Math.sin((Math.PI * i) / kSegments);
			pieY[i] = yc - r * Math.cos((Math.PI * i) / kSegments);
		}
		
		initialised = true;
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int frame = getCurrentFrame();
		double p = (double)frame / kHalfTransitions;
		
		g.setColor(Color.white);
		int diameter = Math.min(getSize().width, getSize().height);
		g.fillRect((getSize().width - diameter) / 2, (getSize().height - diameter) / 2, diameter, diameter);
		
		if (frame < kHalfTransitions)
			drawFirstHalf(g, p);
		else
			drawSecondHalf(g, p);
	}
	
	private void setUpFirstCounts() {
		CatVariable y = (CatVariable)getVariable(catKey);
		int noOfCats = y.noOfCategories();
		int totalCount = y.noOfValues();
		
		if (cumProp == null || cumProp.length != noOfCats)
			cumProp = new int[noOfCats];
		
		int[] counts = y.getCounts();
		int cumCount = 0;
		
		for (int i=0 ; i<noOfCats ; i++) {
			cumCount += counts[noOfCats - i - 1];
			cumProp[i] = cumCount * kSegments / totalCount;
		}
	}
	
	private void drawFirstHalf(Graphics g, double p) {
		setUpFirstCounts();
		
		double q = 1.0 - p;
		
		int startCum = 0;
		for (int i=0 ; i<cumProp.length ; i++) {
			int endCum = cumProp[i];
			int halfPts = endCum - startCum + 1;
			for (int j=startCum ; j<=endCum ; j++) {
				double barYj = barY[j];
				double pieXj = pieX[j];
				double pieYj = pieY[j];
				double tempX0 = x0 * q + 0.5 * (xc + pieXj) * p;
				double tempX1 = x1 * q + pieXj * p;
				double tempY0 = barYj * q + 0.5 * (yc + pieYj) * p;
				double tempY1 = barYj * q + pieYj * p;
				
				int i0 = j - startCum;
				int i1 = 2 * halfPts - i0 - 1;
				tempX[i0] = (int)Math.round(tempX0);
				tempY[i0] = (int)Math.round(tempY0);
				tempX[i1] = (int)Math.round(tempX1);
				tempY[i1] = (int)Math.round(tempY1);
			}
			tempX[2 * halfPts] = tempX[0];
			tempY[2 * halfPts] = tempY[0];
			g.setColor(kCatColour[cumProp.length - i - 1]);
			g.fillPolygon(tempX, tempY, 2 * halfPts + 1);
			startCum = endCum;
		}
	}
	
	private void drawSecondHalf(Graphics g, double p) {
		int left = (int)Math.round(xc - r);
		int top = (int)Math.round(yc - r);
		int diam = (int)Math.round(2 * r);
		
		CatVariable y = (CatVariable)getVariable(catKey);
		int noOfCats = y.noOfCategories();
		int totalCount = y.noOfValues();
		
		int[] counts = y.getCounts();
		int cumCount = 0;
		
		int startAngle = 90;
		for (int i=0 ; i<noOfCats ; i++) {
			cumCount += counts[noOfCats - i - 1];
			int endAngle = 90 - (int)Math.round(cumCount * p * 180 / totalCount);
			g.setColor(kCatColour[noOfCats - i - 1]);
			
			g.fillArc(left, top, diam, diam, startAngle, endAngle - startAngle);
			startAngle = endAngle;
		}
		
		g.setColor(Color.white);
		
		double innerR = r * (2.0 - p) * 0.5;
		left = (int)Math.round(xc - innerR);
		top = (int)Math.round(yc - innerR);
		diam = (int)Math.round(2 * innerR);
		g.fillOval(left, top, diam, diam);
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

}