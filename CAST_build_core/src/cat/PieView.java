package cat;

import java.awt.*;

import dataView.*;


public class PieView extends CatDataView {
	
	static final private double kDegToRad = Math.PI / 180;
	protected int radius, left, top, cx, cy;
	
	public PieView(DataSet theData, XApplet applet, String catKey, int dragType) {
		super(theData, applet, catKey, dragType);
	}
	
	protected void setRadius(int newRadius) {
		radius = newRadius;
		left = getSize().width / 2 - radius;
		top = getSize().height / 2 - radius;
		cx = left + radius;
		cy = top + radius;
	}
	
	protected int getXShift(int angle, int radius) {
		return (int)Math.round(radius * Math.cos(angle * kDegToRad));
	}
	
	protected int getYShift(int angle, int radius) {
		return (int)Math.round(radius * Math.sin(angle * kDegToRad));
	}
	
	protected void drawRadius(Graphics g, int angle, int radius) {
		int dx = getXShift(angle, radius);
		int dy = getYShift(angle, radius);
		for (int xi=0 ; xi<2 ; xi++)
			for (int yi=0 ; yi<2 ; yi++)
				g.drawLine(cx - xi, cy - yi, cx - xi + dx, cy - yi - dy);
	}
	
	protected int getDegrees(int count) {
		return 90 - (int)Math.round(count * 360.0 / totalCount);
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		setRadius(Math.min(getSize().height, getSize().width) / 2 - 1);
		
		if (variable.noOfValues() <= 0)
			return;
		
		boolean selectedCats[] = getSelectedCats();
		boolean noSelection = noSelectedCats(selectedCats);
		int noOfCats = selectedCats.length;
		
		int startAngle = 90;
		boolean previousSelected = selectedCats[noOfCats - 1];
		for (int i=0 ; i<noOfCats ; i++) {
			boolean thisSelected = selectedCats[i];
//			int endAngle = 90 - (cumCount[i] * 360) / totalCount;
			int endAngle = getDegrees(cumCount[i]);
			if (endAngle < 0)
				endAngle += 360;
			int degrees = endAngle - startAngle;
			if (degrees > 0)
				degrees -= 360;
			if (cumCount[i] == totalCount && (i == 0 || cumCount[i-1] == 0))
				degrees = -360;
			if (degrees < 0) {
				if (thisSelected) {
					g.setColor(Color.black);
					g.fillArc(left, top, 2 * radius, 2 * radius, startAngle, degrees);
					g.setColor(getCatColor(i, noSelection || thisSelected));
					g.fillArc(left + 2, top + 2, 2 * radius - 4, 2 * radius - 4, startAngle, degrees);
				}
				else {
					g.setColor(getCatColor(i, noSelection || thisSelected));
					g.fillArc(left, top, 2 * radius, 2 * radius, startAngle, degrees);
				}
				if (thisSelected != previousSelected) {
					g.setColor(Color.black);
					drawRadius(g, startAngle, radius);
				}
			}
			startAngle = endAngle;
			previousSelected = thisSelected;
		}
		if (selectedCats[0] != selectedCats[noOfCats - 1]) {
			g.setColor(Color.black);
			drawRadius(g, startAngle, radius);
		}
		
		if (targetBefore != -1) {
			g.setColor(Color.red);
//			int targetAngle = (targetBefore == 0 || targetBefore == noOfCats)
//												? 90 : 90 - (cumCount[targetBefore - 1] * 360) / totalCount;
			int targetAngle = (targetBefore == 0 || targetBefore == noOfCats)
																										? 90 : getDegrees(cumCount[targetBefore - 1]);
			if (targetAngle < 0)
				targetAngle += 360;
			drawRadius(g, targetAngle, radius);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		int dx = x - cx;
		int dy = y - cy;
		if (dx * dx + dy * dy <= radius * radius) {
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
			int previousCum = 0;
			double hitCount = proportion * totalCount;
			for (int i=0 ; i<cumCount.length ; i++) {
				int thisCum = cumCount[i];
				if (hitCount <= thisCum)
					return new CatPosInfo(i, thisCum - hitCount < hitCount - previousCum);
				previousCum = thisCum;
			}
		}
		
		return null;
	}
}