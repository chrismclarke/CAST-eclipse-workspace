package distribution;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class ContinuousProbView extends MarginalDataView {
	static final protected double kXPropns[] = {0.0,0.0002,0.0005,0.001,0.002,0.003,0.004,0.005,0.006,0.007,0.008,0.010,0.012,0.015,0.02,0.03,0.04,0.05,0.06,0.08,0.10,0.12,0.14,0.16,0.18,0.20,0.22,0.24,0.26,0.28,0.30,0.32,0.34,0.36,0.38,0.40,0.42,0.44,0.46,0.48,0.50,0.52,0.54,0.56,0.58,0.60,0.62,0.64,0.66,0.68,0.70,0.72,0.74,0.76,0.78,0.80,0.82,0.84,0.86,0.88,0.90,0.92,0.94,0.96,0.98,0.99,0.992,0.994, 0.995, 0.996, 0.997, 0.998, 0.999, 0.9995, 0.9998,1};
	
	static final private double kInfiniteFactor = 3.0;
												//	infinite densities are 3 times axis length above top
	
	static final private Color kDistnColor = new Color(0x999999);
	static final private Color kDistn2Color = new Color(0xCC0000);
	static final private Color kHighlightColor = new Color(0x0000CC);
	
	static final protected Color kLabelColor = new Color(0xBBBBBB);
	
	private static final int kMinDensityWidth = 30;
	
	protected String distnKey, distnKey2 = null;
	protected NumCatAxis horizAxis;
	protected NumCatAxis densityAxis;
	
	private int minWidth = kMinDensityWidth;
	
	@SuppressWarnings("unused")
	private Color distnColor = kDistnColor;
	@SuppressWarnings("unused")
	private Color highlightColor = kHighlightColor;
	
	private String titleString = null;
	
	private boolean ignoreDensityAxis = false;
	
	protected double minSupportX = Double.NEGATIVE_INFINITY;
	protected double maxSupportX = Double.POSITIVE_INFINITY;
	private double minSupportX2 = Double.NEGATIVE_INFINITY;
	private double maxSupportX2 = Double.POSITIVE_INFINITY;
						//	changed for distns such as Expon and Rect (and even normal)
	
	private double pseudoMaxDensity = 0.0;	//	used as maxDensity when true max is infinite
	
	public ContinuousProbView(DataSet theData, XApplet applet, String distnKey,
																			NumCatAxis horizAxis, NumCatAxis densityAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0), horizAxis);
//		super(theData, applet, null, horizAxis);
		this.distnKey = distnKey;
		this.horizAxis = horizAxis;
		this.densityAxis = densityAxis;
	}
	
	public int minDisplayWidth() {
		return minWidth;
	}
	
	public void setMinDisplayWidth(int minWidth) {
		this.minWidth = minWidth;
	}
	
	public void setSecondDistn(String distnKey2) {
		this.distnKey2 = distnKey2;
	}
	
	public void setDensityColor(Color c) {
		distnColor = c;
	}
	
	public void setHighlightColor(Color c) {
		highlightColor = c;
	}
	
	public void setSupport(double minSupportX, double maxSupportX) {
		this.minSupportX = minSupportX;
		this.maxSupportX = maxSupportX;
	}
	
	public void set2ndSupport(double minSupportX2, double maxSupportX2) {
		this.minSupportX2 = minSupportX2;
		this.maxSupportX2 = maxSupportX2;
	}
	
	public void setPseudoMaxDensity(double pseudoMaxDensity) {
		this.pseudoMaxDensity = pseudoMaxDensity;
	}
	
	public void setIgnoreDensityAxis(boolean ignoreDensityAxis) {
		this.ignoreDensityAxis = ignoreDensityAxis;
		repaint();
	}
	
	public void setTitleString(String titleString, XApplet applet) {
		this.titleString = titleString;
		setFont(applet.getBigBoldFont());
	}
	
	protected void drawTitleString(Graphics g) {
		if (titleString != null) {
			g.setColor(kLabelColor);
			int ascent = g.getFontMetrics().getAscent();
			g.drawString(titleString, 4, ascent + 2);
			g.setColor(getForeground());
		}
	}
	
	public void paintView(Graphics g) {
		drawTitleString(g);
		
		double xVal[] = new double[kXPropns.length];
		double yVal[] = new double[kXPropns.length];
		
		ContinDistnVariable distnVar = (ContinDistnVariable)getVariable(distnKey);
		setupDensityPoints(distnVar, minSupportX, maxSupportX, 1.0, xVal, yVal);
		
//		for (int i=kXPropns.length - 3 ; i<kXPropns.length ; i++)
//			System.out.println(i + ": " + xVal[i] + ", " + yVal[i]);
		
		g.setColor(kDistnColor);
		fillCurve(g, xVal, yVal, horizAxis, densityAxis);
		
		if (distnKey2 != null) {
			ContinDistnVariable distn2Var = (ContinDistnVariable)getVariable(distnKey2);
			
			double maxMainDensity = distnVar.getMaxScaledDensity() * distnVar.getDensityFactor();
			double max2ndDensity = distn2Var.getMaxScaledDensity() * distn2Var.getDensityFactor();
			double scalingFactor = maxMainDensity / max2ndDensity;
			
			setupDensityPoints(distn2Var, minSupportX2, maxSupportX2, scalingFactor, xVal, yVal);
			Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER , (float)0.3);
			((Graphics2D)g).setComposite(comp);
			g.setColor(kDistn2Color);
			fillCurve(g, xVal, yVal, horizAxis, densityAxis);
		}
	}
	
	protected double getDrawMin(double minSupport) {
		double minX = horizAxis.minOnAxis;
		double maxX = horizAxis.maxOnAxis;
		double slop = (maxX - minX) * 0.02;		//	to ensure that it reaches end of axis
		minX -= slop;
		return Math.max(minX, minSupport);
	}
	
	protected double getDrawMax(double maxSupport) {
		double minX = horizAxis.minOnAxis;
		double maxX = horizAxis.maxOnAxis;
		double slop = (maxX - minX) * 0.02;		//	to ensure that it reaches end of axis
		maxX += slop;
		return Math.min(maxX, maxSupport);
	}
	
	private void setupDensityPoints(ContinDistnVariable distnVar, double minSupport,
							double maxSupport, double scalingFactor, double[] xVal, double[] yVal) {
		double minX = getDrawMin(minSupport);
		double maxX = getDrawMax(maxSupport);
		
		double densityFactor = distnVar.getDensityFactor();
		if (densityAxis == null) {
			densityFactor = 0.90 / distnVar.getMaxScaledDensity();
		}
		else if (ignoreDensityAxis) {
			double maxDensity = distnVar.getMaxScaledDensity() * densityFactor;
			if (Double.isInfinite(maxDensity))
				maxDensity = pseudoMaxDensity;
			double maxOnAxis = densityAxis.maxOnAxis;
			densityFactor *= maxOnAxis / maxDensity * 0.95 / scalingFactor;
										//	scalingFactor is to keep 2nd density consistent with 1st
		}
		
		double pseudoInfiniteDensity = (densityAxis == null) ? Double.POSITIVE_INFINITY
											: densityAxis.maxOnAxis + kInfiniteFactor * (densityAxis.maxOnAxis - densityAxis.minOnAxis);
		for (int i=0 ; i<kXPropns.length ; i++) {
			xVal[i] = minX + kXPropns[i] * (maxX - minX);
			yVal[i] = Math.min(getDensity(xVal[i], distnVar, densityFactor),
												 pseudoInfiniteDensity);
		}
	}
	
	protected double getDensity(double x, ContinDistnVariable distnVar, double densityFactor) {
		return distnVar.getScaledDensity(x) * densityFactor;
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}