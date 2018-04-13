package corr;

import java.awt.*;

import dataView.*;
import valueList.*;
import images.*;


public class CorrelationView extends ValueView {
	
	static public final boolean DRAW_FORMULA = true;
	static public final boolean NO_FORMULA = false;
	
	static final protected int kDefaultDecimals = 3;
	static final private String kStartString = "r =";
	static final private int kImageValueGap = 4;
	static final private int kCorrAscent = 26;
	static final protected int kMaxWait = 30000;		//		30 seconds
	
	protected String xKey, yKey;
	private boolean drawFormula;
	private int decimals;
	
	private Image corrImage;
	
	public CorrelationView(DataSet theData, String xKey, String yKey, boolean drawFormula,
																										int decimals, XApplet applet) {
		super(theData, applet);
		this.xKey = xKey;
		this.yKey = yKey;
		this.drawFormula = drawFormula;
		this.decimals = decimals;
		if (drawFormula) {
			corrImage = CoreImageReader.getImage("corr/corr.gif");
			MediaTracker tracker = new MediaTracker(applet);
			tracker.addImage(corrImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public CorrelationView(DataSet theData, String xKey, String yKey, boolean drawFormula,
																																			XApplet applet) {
		this(theData, xKey, yKey, drawFormula, kDefaultDecimals, applet);
	}

//--------------------------------------------------------------------------------
	
	public void changeVariables(String yKey, String xKey) {
		if (yKey != null && this.yKey != yKey)
			this.yKey = yKey;
		if (xKey != null && this.xKey != xKey)
			this.xKey = xKey;
		redrawValue();
	}

//--------------------------------------------------------------------------------
	
	protected Image getCorrImage() {
		return corrImage;
	}
	
	protected int getLabelWidth(Graphics g) {
		if (drawFormula)
			return getCorrImage().getWidth(this) + kImageValueGap;
		else
			return g.getFontMetrics().stringWidth(kStartString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		String maxString = "-0.";
		for (int i=0 ; i<decimals ; i++)
			maxString += "0";
		FontMetrics fm = g.getFontMetrics();
		return fm.stringWidth(maxString);
	}
	
	protected int getLabelAscent(Graphics g) {
		if (drawFormula)
			return kCorrAscent;
		else
			return super.getLabelAscent(g);
	}
	
	protected int getLabelDescent(Graphics g) {
		if (drawFormula)
			return getCorrImage().getHeight(this) - kCorrAscent;
		else
			return super.getLabelDescent(g);
	}
	
	protected String getValueString() {
		NumVariable x = (NumVariable)getVariable(xKey);
		NumVariable y = (NumVariable)getVariable(yKey);
		ValueEnumeration xe = x.values();
		ValueEnumeration ye = y.values();
		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double syy = 0.0;
		double sxy = 0.0;
		int nVals = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double xVal = xe.nextDouble();
			double yVal = ye.nextDouble();
			if (!Double.isNaN(xVal) && !Double.isNaN(yVal)) {
				sx += xVal;
				sy += yVal;
				sxx += xVal * xVal;
				syy += yVal * yVal;
				sxy += xVal * yVal;
				nVals++;
			}
		}
		double corr = (sxy - sx * sy / nVals)
										/ Math.sqrt((sxx - sx * sx / nVals) * (syy - sy * sy / nVals));
		return (new NumValue(corr, decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (drawFormula)
			g.drawImage(getCorrImage(), startHoriz, baseLine - kCorrAscent, this);
		else
			g.drawString(kStartString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
