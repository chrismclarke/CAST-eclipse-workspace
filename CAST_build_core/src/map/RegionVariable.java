package map;

import java.util.*;
import java.awt.*;

import dataView.*;

public class RegionVariable extends Variable {

	private Rectangle sourceRect;
	private int left, top, width, height;
	
	public RegionVariable(String theName) {
		super(theName);
	}
	
	public void readValues(String valueString) {
		clearData();
		StringTokenizer theRegions = new StringTokenizer(valueString, "#");
		while (theRegions.hasMoreTokens()) {
			String nextRegion = theRegions.nextToken();
			addValue(new RegionValue(nextRegion));
		}
	}
	
	public Rectangle getRegionBounds() {
		Rectangle tempRect = new Rectangle(0, 0, 0, 0);
		int minLeft = Integer.MAX_VALUE;
		int minTop = Integer.MAX_VALUE;
		int maxRight = Integer.MIN_VALUE;
		int maxBottom = Integer.MIN_VALUE;
		
		ValueEnumeration re = values();
		while (re.hasMoreValues()) {
			RegionValue nextR = (RegionValue)re.nextValue();
			nextR.getRegionBounds(tempRect);
			if (tempRect.x < minLeft)
				minLeft = tempRect.x;
			if (tempRect.x + tempRect.width > maxRight)
				maxRight = tempRect.x + tempRect.width;
			if (tempRect.y < minTop)
				minTop = tempRect.y;
			if (tempRect.y + tempRect.height > maxBottom)
				maxBottom = tempRect.y + tempRect.height;
			
		}
		tempRect.setBounds(minLeft, minTop, maxRight - minLeft + 1, maxBottom - minTop + 1);
		return tempRect;
	}
	
	public void scaleGraphicsToFit(Graphics g, int left, int top, int width, int height) {
		sourceRect = getRegionBounds();
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		
		if (BufferedCanvas.isJavaUpToDate) {
			g.setColor(Color.white);
			g.fillRect(left, top, width, height);
			Graphics2DActions.setScalingTransform(g, sourceRect, left, top, width, height);
		}
	}
	
	public Polygon rescalePoly(Polygon p, Polygon tempP) {
																		//	transforms coords (source -> drawing)
		if (BufferedCanvas.isJavaUpToDate)
			return null;
		if (tempP == null)
			tempP = new Polygon(p.xpoints, p.ypoints, p.npoints);
		for (int i=0 ; i<tempP.npoints ; i++) {
			tempP.xpoints[i] = left + (p.xpoints[i] - sourceRect.x) * width / sourceRect.width;
			tempP.ypoints[i] = top + (p.ypoints[i] - sourceRect.y) * height / sourceRect.height;
		}
		return tempP;
	}
	
	public int rescaleRadius(int screenRadius) {
																		//	transforms coords (screen -> drawing)
		if (BufferedCanvas.isJavaUpToDate)
			return screenRadius * sourceRect.width / width;
		else
			return screenRadius;
	}
	
	public void restoreGraphicScaling(Graphics g) {
		if (BufferedCanvas.isJavaUpToDate)
			Graphics2DActions.setIdentityTransform(g);
	}
	
	public int findHit(int xHit, int yHit, int screenLeft, int screenTop, int screenWidth,
																																			int screenHeight) {
		Rectangle sourceRect = getRegionBounds();
		int xInMap = sourceRect.x + sourceRect.width * (xHit - screenLeft) / screenWidth;
		int yInMap = sourceRect.y + sourceRect.height * (yHit - screenTop) / screenHeight;
		ValueEnumeration re = values();
		int index = 0;
		while (re.hasMoreValues()) {
			RegionValue nextR = (RegionValue)re.nextValue();
			if (nextR.hitInRegion(xInMap, yInMap))
				return index;
			index ++;
		}
		return -1;
	}
}
