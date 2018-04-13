package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class SimpsonNumerView extends DotPlotView {
//	static final public String SIMPSON_NUM_VIEW = "simpsonNum";
	
	static final public int GROUP_MODE = 0;
	static final public int SLICE_MODE = 1;
	
//	private static final int kHiliteWidth = 2;
	private static final int kLabelGap = 3;
	
	private static final int kMaxVertJitter = 30;
	
	private int displayMode;
	
	private LabelValue overallMeanLabel;
	private LabelValue zMeanLabel[];
	
	private CatVariable xVariable = null;
	private CatVariable zVariable = null;
	private VertAxis vertAxis = null;
	
	private boolean showSubMeans = false;
	private boolean showSlices = false;
	private int currentSlice = 0;
	
	private Point p = new Point(0,0);
	
	public SimpsonNumerView(DataSet theData, XApplet applet, HorizAxis horizAxis, VertAxis vertAxis,
								String sliceMeanTemplate, LabelValue overallMeanLabel,
								String xKey, String zKey, int displayMode) {
		super(theData, applet, horizAxis, 1.0);
		this.vertAxis = vertAxis;
		this.overallMeanLabel = overallMeanLabel;
		xVariable = (CatVariable)getVariable(xKey);
		zVariable = (CatVariable)getVariable(zKey);
		createSliceLabels(sliceMeanTemplate, zVariable);
		this.displayMode = displayMode;
	}
	
	private void createSliceLabels(String sliceMeanTemplate, CatVariable zVariable) {
		zMeanLabel = new LabelValue[zVariable.noOfCategories()];
		int catLocation = sliceMeanTemplate.indexOf('#');
		if (catLocation < 0) {
			LabelValue l = new LabelValue(sliceMeanTemplate);
			for (int i=0 ; i<zMeanLabel.length ; i++)
				zMeanLabel[i] = l;
		}
		else {
			String startString = sliceMeanTemplate.substring(0, catLocation);
			String endString = sliceMeanTemplate.substring(catLocation + 1, sliceMeanTemplate.length());
			for (int i=0 ; i<zMeanLabel.length ; i++)
				zMeanLabel[i] = new LabelValue(startString + zVariable.getLabel(i).toString() + endString);
		}
	}
	
	public void setShowSubMeans(boolean showSubMeans) {
		this.showSubMeans = showSubMeans;
		repaint();
	}
	
	public void setShowSliced(boolean showSlices) {
		this.showSlices = showSlices;
		repaint();
	}
	
	public void setCurrentSlice(int currentSlice) {
		this.currentSlice = currentSlice;
		repaint();
	}
	
	protected int groupIndex(int itemIndex) {
		return (displayMode == SLICE_MODE && !showSlices) ? 0 : zVariable.getItemCategory(itemIndex);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		if (showSlices && zVariable.getItemCategory(index) != currentSlice)
			return null;
		
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null && xVariable != null) {
			int groupIndex = xVariable.getItemCategory(index);
			newPoint.y -= vertAxis.catValToPosition(groupIndex) - currentJitter / 2;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		if (xVariable == null)
			return super.getMaxJitter();
		int noOfGroups = xVariable.noOfCategories();
		return Math.min(kMaxVertJitter,
							(getSize().height - getViewBorder().top - getViewBorder().bottom) / noOfGroups / 2);
	}
	
	private void drawBackground(Graphics g) {
		int nx = xVariable.noOfCategories();
		int nz = zVariable.noOfCategories();
		
		int n[][] = new int[nx][];
		double sy[][] = new double[nx][];
		for (int i=0 ; i<nx ; i++) {
			n[i] = new int[nz];
			sy[i] = new double[nz];
		}
			
		ValueEnumeration e = getNumVariable().values();
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ze = zVariable.values();
		while (e.hasMoreValues()) {
			double y = e.nextDouble();
			int x = xVariable.labelIndex(xe.nextValue());
			int z = zVariable.labelIndex(ze.nextValue());
			n[x][z] ++;
			sy[x][z] += y;
		}
			
		int top = getSize().height - getViewBorder().bottom;
		for (int i=0 ; i<nx ; i++) {
			int bottom = top;
			top = getSize().height - getViewBorder().bottom
																	- (getSize().height - getViewBorder().top - getViewBorder().bottom) * (i+1) / nx;
			
			if (showSlices) {
					g.setColor(getCrossColor(currentSlice));
				drawMean(n[i][currentSlice], sy[i][currentSlice], top, bottom, zMeanLabel[currentSlice], g);
			}
			else if (showSubMeans) {
				for (int j=0 ; j<nz ; j++) {
					g.setColor(getCrossColor(j));
					drawMean(n[i][j], sy[i][j], top, bottom, zMeanLabel[j], g);
				}
			}
			else {
				g.setColor(Color.black);
				int ni = 0;
				double syi = 0.0;
				for (int j=0 ; j<nz ; j++) {
					ni += n[i][j];
					syi += sy[i][j];
				}
				drawMean(ni, syi, top, bottom, overallMeanLabel, g);
			}
		}
	}
	
	private void drawMean(int n, double sx, int topPos, int bottomPos, Value meanLabel,
																																						Graphics g) {
		if (n > 0) {
			double mean = sx / n;
			int meanPos = axis.numValToRawPosition(mean);
			int mx = translateToScreen(meanPos, 0, p).x;
			
			int width = meanLabel.stringWidth(g);
			FontMetrics fm = g.getFontMetrics();
			int baseline = topPos + fm.getAscent() + kLabelGap;
			
			int valCentre = Math.max(mx, width / 2);
			valCentre = Math.min(valCentre, getSize().width - width / 2);
			
			meanLabel.drawCentred(g, valCentre, baseline);
			
//			int topOfJitter = translateToScreen(0, currentJitter + 4, p).y;
			g.drawLine(mx, baseline + kLabelGap, mx, bottomPos - (fm.getAscent() + kLabelGap));
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
}