package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class AccurateDistn2Artist implements BackgroundArtistInterface {
									//		Similar to AccurateDistnArtist, but does better job of shading
									//		mode at zero. It could probably supercede AccurateDistnArtist
									//		but it hasn't been tested fully in general.
	static final private int kNoOfShades = 50;
	
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
	
	private Color fillColor = Color.lightGray;
	private Color highlightColor = Color.blue;
	
	private double areaProportion = 0.2;
	
	private Color fillShade[] = new Color[kNoOfShades + 1];
	private Color highlightShade[] = new Color[kNoOfShades + 1];
	
	private boolean initialised = false;
	
	private double classProb[];
	private boolean infiniteDensityAtMin;
	
	public AccurateDistn2Artist(String distnKey, DataSet data) {
		this.distnKey = distnKey;
		this.data = data;
		
		setShades(fillShade, fillColor);
		setShades(highlightShade, highlightColor);
	}
	
	public void setAreaProportion(double areaProportion) {
		this.areaProportion = areaProportion;
	}
	
	public void changeDistnKey(String distnKey) {
		this.distnKey = distnKey;
		resetDistn();
	}
	
	public void resetDistn() {
		initialised = false;
	}
	
	public void setDistnKey(String distnKey) {
		this.distnKey = distnKey;
		initialised = false;
	}
	
	public void initialise(DataView view, NumCatAxis axis) {
		int noOfClasses = axis.getAxisLength();
		if (initialised || noOfClasses <= 0)		//	can be called before view is laid out
			return;
		
		double min = axis.minOnAxis;
		double max = axis.maxOnAxis;
		
		ContinDistnVariable dist = (ContinDistnVariable)data.getVariable(distnKey);
		infiniteDensityAtMin = Double.isInfinite(dist.getScaledDensity(min));
		
		if (classProb == null)
			classProb = new double[noOfClasses + 1];
		
		double step = (max - min) / noOfClasses;
		double x = min;
		double lastCum = dist.getCumulativeProb(x);
		for (int i=0 ; i<noOfClasses ; i++) {
			x += step;
			double nextCum = dist.getCumulativeProb(x);
			classProb[i] = nextCum - lastCum;
			
			lastCum = nextCum;
		}
		initialised = true;
	}
	
	private void setFillShade(double p, boolean highlight, Graphics g) {
		int topShade = (int)Math.round(kNoOfShades * p);
		g.setColor(highlight ? highlightShade[topShade] : fillShade[topShade]);
	}
	
	public double getHeightAt(int axisPosition, DataView view, NumCatAxis axis) {
		initialise(view, axis);
		
		int nColumns = classProb.length;
		int pixBottom = view.getSize().height - view.getViewBorder().bottom;
		int pixHt = pixBottom - view.getViewBorder().top;
		double scaling = areaProportion * pixHt * nColumns;
		
		if (axisPosition < 0 || axisPosition >= nColumns)
			return 0;
		else
			return classProb[axisPosition] * scaling;
	}
	
	synchronized public void paintDistn(Graphics g, DataView view, NumCatAxis axis,
															double lowHighlightX, double highHighlightX) {
		initialise(view, axis);
		
		int nColumns = classProb.length;
//		double min = axis.minOnAxis;
//		double max = axis.maxOnAxis;
		int leftBorder = view.getViewBorder().left;
		
//		int lowHighlightPix = (lowHighlightX <= min) ? -1 : (int)Math.round((lowHighlightX - min) / (max - min) * nColumns);
//		int highHighlightPix = (highHighlightX >= max) ? nColumns : (int)Math.round((highHighlightX - min) / (max - min) * nColumns);
		int lowHighlightPix = axis.numValToRawPosition(lowHighlightX);
		if (lowHighlightPix < 0)
			lowHighlightPix = -1;
		int highHighlightPix = axis.numValToRawPosition(highHighlightX);
		if (highHighlightPix > nColumns)
			highHighlightPix = nColumns;
		
		int pixBottom = view.getSize().height - view.getViewBorder().bottom;
		int pixHt = pixBottom - view.getViewBorder().top;
		double scaling = areaProportion * pixHt * nColumns;
		
		double previousPix = infiniteDensityAtMin ? Double.POSITIVE_INFINITY : 0.0;
		double thisPix = classProb[0] * scaling;
		
		for (int i=1 ; i<nColumns ; i++) {
			boolean selected = (i-1) <lowHighlightPix || (i-1) > highHighlightPix;
			double nextProb = classProb[i];
			double nextPix = nextProb * scaling;
			
			int yPix = (int)Math.round(Math.floor(thisPix));
			setFillShade(1.0, selected, g);										//	full colour
			g.drawLine(leftBorder + i-1, pixBottom - yPix, leftBorder + i-1, pixBottom);
			
			double halfMinStep = Math.min(Math.abs(previousPix - thisPix),
																												Math.abs(thisPix - nextPix)) * 0.5;
			if (halfMinStep <= 1.0 || ((thisPix > previousPix) == (thisPix > nextPix))) {
				setFillShade(thisPix - yPix, selected, g);
				g.fillRect(leftBorder + i-1, pixBottom - yPix - 1, 1, 1);
			}
			else {
				double top = thisPix + halfMinStep;
				double bottom = thisPix - halfMinStep;
				
				double floor = Math.floor(top);
				int pix = pixBottom - (int)Math.round(floor);
				double topShade = (top - floor) * (top - floor) / (top - bottom) * 0.5;
				setFillShade(topShade, selected, g);
				g.fillRect(leftBorder + i-1, pix, 1, 1);
				
				floor -= 1.0;
				pix ++;
				while (floor > bottom) {
					double shade = (top - floor - 0.5) / (top - bottom);
					setFillShade(shade, selected, g);
					g.fillRect(leftBorder + i-1, pix, 1, 1);
					floor -= 1.0;
					pix ++;
				}
				
				double ceil = floor + 1.0;
				double bottomShade = 1.0 - (ceil - bottom) * (ceil - bottom) / (top - bottom) * 0.5;
				setFillShade(bottomShade, selected, g);
				g.fillRect(leftBorder + i-1, pix, 1, 1);
			}
			
			previousPix = thisPix;
			thisPix = nextPix;
		}
	}
	
	synchronized public void paintDistn(Graphics g, DataView view, NumCatAxis axis) {
		paintDistn(g, view, axis, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
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