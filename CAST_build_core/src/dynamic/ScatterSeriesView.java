package dynamic;

import java.awt.*;

import dataView.*;
import axis.*;

import graphics.*;


public class ScatterSeriesView extends ScatterCircleView {
	
	static final private Color kLightGray = new Color(0xEEEEEE);
	static final private Color kGroupTraceColor[];
	static {
		kGroupTraceColor = new Color[kGroupColor.length];
		for (int i=0 ; i<kGroupTraceColor.length ; i++)
			kGroupTraceColor[i] = mixColors(kGroupColor[i], kLightGray, 0.3);
	}
	
	public ScatterSeriesView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String sizeKey,
										String groupKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, sizeKey, groupKey);
	}
	
	protected double calcMaxSize() {
		NumSeriesVariable sizeVar = (NumSeriesVariable)getVariable(sizeKey);
		int len = sizeVar.seriesLength();
		double max = 0;
		ValueEnumeration se = sizeVar.values();
		while (se.hasMoreValues()) {
			NumSeriesValue sVal = (NumSeriesValue)se.nextValue();
			for (int i=0 ; i<len ; i++) {
				double s = sVal.toDouble(i);
				if (!Double.isNaN(s) && s > max)
					max = s;
			}
		}
		return max;
	}
	
	private void drawTracePoint(Point p, Point pOld, double size, int group, Graphics g) {
		if (p == null)
			return;
		
		g.setColor(kGroupTraceColor[group]);
		if (displayType == DISPLAY_CIRCLES) {
			int radius = (int)Math.round(Math.sqrt(size / maxSize) * kMaxRadius);
			g.drawOval(p.x - radius, p.y - radius, 2 * radius, 2 * radius);
		}
		else {
			if (pOld != null)
				g.drawLine(p.x, p.y, pOld.x, pOld.y);
			drawCross(g, p);
		}
	}
	
	protected void drawSelectedForeground(Graphics g, NumValue xVal, NumValue yVal,
																								NumValue sizeVal, int group, Point p) {
		NumSeriesValue ySeriesVal = (NumSeriesValue)yVal;
		NumSeriesValue xSeriesVal = (NumSeriesValue)xVal;
		NumSeriesValue sizeSeriesVal = (NumSeriesValue)sizeVal;
		double seriesIndex = ySeriesVal.getSeriesIndex();
		
		int wholeIndex = (int)Math.round(Math.floor(seriesIndex - 0.0001));
		Point p1 = null;
		for (int j=wholeIndex ; j>=0 ; j--) {
			Point pTemp = p;
			p = p1;
			p1 = pTemp;
			p = getScreenPoint(xSeriesVal.toDouble(j), ySeriesVal.toDouble(j), p);
			drawTracePoint(p, p1, sizeSeriesVal.toDouble(j), group, g);
		}
	}
	
}
	
