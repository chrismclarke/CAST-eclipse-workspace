package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class AccurateDistnArtist implements BackgroundArtistInterface {
	static final public int kNoOfShades = 50;
	
	static public void setShades(Color[] shades, Color baseColor) {
		int r = baseColor.getRed();
		int g = baseColor.getGreen();
		int b = baseColor.getBlue();
		for (int i=0 ; i<=kNoOfShades ; i++)
			shades[i] = new Color( 255 - (255 - r) * i / kNoOfShades,
												255 - (255 - g) * i / kNoOfShades, 255 - (255 - b) * i / kNoOfShades);
	}
	
	private String distnKey;
	private DataSet data;
	
	private int pixHt[];
	private int topPixDensity[];
	
	private Color fillColor = Color.lightGray;
	private Color highlightColor = Color.blue;
	
	private double densityScaling = 1.0;
	
	private Color fillShade[] = new Color[kNoOfShades + 1];
	private Color highlightShade[] = new Color[kNoOfShades + 1];
	
	private boolean initialised = false;
	
	public AccurateDistnArtist(String distnKey, DataSet data) {
		this.distnKey = distnKey;
		this.data = data;
		
		setShades(fillShade, fillColor);
		setShades(highlightShade, highlightColor);
	}
	
	public void changeDistnKey(String distnKey) {
		this.distnKey = distnKey;
		resetDistn();
	}
	
	public void resetDistn() {
		initialised = false;
	}
	
	public void setDensityScaling(double densityScaling) {
		this.densityScaling = densityScaling;
	}
	
	public void initialise(DataView view, NumCatAxis axis) {
		int noOfClasses = axis.getAxisLength();
		if (initialised || noOfClasses <= 0)		//	can be called before view is laid out
			return;
		
//		for (double x=0.9 ; x<=1.1001 ; x+=0.01) {
//			double cumProb = FTable.cumulative(x, 1, 3);
//			double inverseX = FTable.quantile(cumProb, 1, 3);
//			System.out.print("F-cum(" + x + ") = " + cumProb + " / " + inverseX);
//			System.out.println("   (" + (1.0 - TTable.cumulative(-Math.sqrt(x), 3) * 2.0) + ")");
//		}
		
		ContinDistnVariable dist = (ContinDistnVariable)data.getVariable(distnKey);
		
		double min = axis.minOnAxis;
		double max = axis.maxOnAxis;
		
		if (pixHt == null || pixHt.length != noOfClasses)
			pixHt = new int[noOfClasses];
		if (topPixDensity == null || topPixDensity.length != noOfClasses)
			topPixDensity = new int[noOfClasses];
		
		int viewHt = view.getSize().height - view.getViewBorder().top - view.getViewBorder().bottom;
		double x = min;
		double step = (max - min) / noOfClasses;
		double maxProb = modeProb(dist, step);
		double lastCum = dist.getCumulativeProb(x);
		for (int i=0 ; i<noOfClasses ; i++) {
			x += step;
			double nextCum = dist.getCumulativeProb(x);
			double thisProb = nextCum - lastCum;
			
			double doubleHt = thisProb / maxProb * viewHt * densityScaling;
			double doublePix = Math.floor(doubleHt);
			pixHt[i] = (int)Math.round(doublePix);
			topPixDensity[i] = (int)Math.round((doubleHt - doublePix) * kNoOfShades);
			
			lastCum = nextCum;
		}
		initialised = true;
	}
	
	private double modeProb(ContinDistnVariable dist, double step) {
		double mode = dist.zToX(dist.getDistnInfo().zMode());
		double modeCum = dist.getCumulativeProb(mode);
		
		double cumPlus = dist.getCumulativeProb(mode + step);
		double cumMinus = dist.getCumulativeProb(mode - step);
		
		double maxProb = Math.max(cumPlus - modeCum, modeCum - cumMinus);
		maxProb = Math.max(maxProb, dist.getCumulativeProb(mode + 0.5 * step)
																									- dist.getCumulativeProb(mode - 0.5 * step));
		return maxProb;
	} 
	
	synchronized public void paintDistn(Graphics g, DataView view, NumCatAxis axis,
															double lowHighlightX, double highHighlightX) {
		initialise(view, axis);
		
		int noOfClasses = pixHt.length;
		double min = axis.minOnAxis;
		double max = axis.maxOnAxis;
		int leftBorder = view.getViewBorder().left;
		
		int lowHighlightPix = (lowHighlightX <= min) ? -1 : (int)Math.round((lowHighlightX - min) / (max - min) * noOfClasses);
		int highHighlightPix = (highHighlightX >= max) ? noOfClasses : (int)Math.round((highHighlightX - min) / (max - min) * noOfClasses);
		
		if (lowHighlightPix >= 0)
			drawBars(g, view, highlightColor, highlightShade, 0, lowHighlightPix, leftBorder);
		
		if (highHighlightPix < noOfClasses)
			drawBars(g, view, highlightColor, highlightShade, highHighlightPix, noOfClasses - 1, leftBorder);
		
		if (lowHighlightPix < highHighlightPix - 1)
			drawBars(g, view, fillColor, fillShade, lowHighlightPix + 1, highHighlightPix - 1, leftBorder);
	}
	
	synchronized public void paintDistn(Graphics g, DataView view, NumCatAxis axis) {
		paintDistn(g, view, axis, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	private void drawBars(Graphics g, DataView view, Color mainColor, Color[] shade,
																										int minX, int maxX, int leftBorder) {
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_OFF);
																					//	so that top of each bar is sharply defined
		
		g.setColor(mainColor);
		int displayBottom = view.getSize().height - view.getViewBorder().bottom;
		for (int i=minX ; i<=maxX ; i++)
			g.drawLine(leftBorder + i, displayBottom - pixHt[i], leftBorder + i, displayBottom);
		
		for (int i=minX ; i<=maxX ; i++) {
			g.setColor(shade[topPixDensity[i]]);
			int topPix = displayBottom - pixHt[i] - 1;
			g.drawLine(leftBorder + i, topPix, leftBorder + i, topPix);
		}
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
	}
	
	public void setFillColor(Color c) {
		fillColor = c;
		setShades(fillShade, fillColor);
		setShades(highlightShade, highlightColor);
	}
	
	public void setHighlightColor(Color c) {
		highlightColor = c;
		setShades(fillShade, fillColor);
		setShades(highlightShade, highlightColor);
	}
}