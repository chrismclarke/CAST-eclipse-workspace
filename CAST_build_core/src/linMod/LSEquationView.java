package linMod;

import java.awt.*;

import dataView.*;
import regn.*;


public class LSEquationView extends EquationView {
//	static public final String LS_EQUATION = "lsEquation";
	
	static protected final int kLeftRightBorder = 3;
	static protected final int kYEqualsGap = 6;
	static protected final int kParamXGap = 6;
	static protected final int kHatExtra = 2;
	
	protected String yKey, xKey;
	protected NumValue maxIntercept, maxSlope;
	
	protected int maxInterceptWidth, maxSlopeWidth;
	protected Dimension interceptSize, slopeSize;
	protected int linearWidth, baseline;
	
	private Color interceptColor, slopeColor;
	
	private boolean showData = false;
	
	public LSEquationView(DataSet theData, XApplet applet, String yKey, String xKey, NumValue maxIntercept,
								NumValue maxSlope, Color interceptColor, Color slopeColor) {
		super(theData, applet);
		this.yKey = yKey;
		this.xKey = xKey;
		RegnImages.loadRegn(applet);
		this.maxIntercept = maxIntercept;
		this.maxSlope = maxSlope;
		this.interceptColor = interceptColor;
		this.slopeColor = slopeColor;
	}
	
	public LSEquationView(DataSet theData, XApplet applet, String yKey,
						String xKey, NumValue maxIntercept, NumValue maxSlope) {
		this(theData, applet, yKey, xKey, maxIntercept, maxSlope, null, null);
	}
	
	public void setShowData(boolean showData) {
		this.showData = showData;
	}

//--------------------------------------------------------------------------------
	
	protected void findLinearPartSize(Graphics g) {
		maxInterceptWidth = maxIntercept.stringWidth(g);
		maxSlopeWidth = maxSlope.stringWidth(g);
		
		FontMetrics fm = g.getFontMetrics();
		interceptSize = getValueSize(fm, maxInterceptWidth);
		slopeSize = getValueSize(fm, maxSlopeWidth);
		
		linearWidth = fm.stringWidth(" +  ") + interceptSize.width + slopeSize.width;
		linearWidth += (kParamXGap + RegnImages.kXParamWidth);
	}
	
	protected int drawLinearPart(Graphics g, int horizPos, int baseline) {
		FontMetrics fm = g.getFontMetrics();
		Color oldColor = g.getColor();
		
		double slope = 0.0;
		double intercept = 0.0;
		if (showData) {
			LSEstimate lse = new LSEstimate(getData(), xKey, yKey);
			slope = lse.getSlope();
			intercept = lse.getIntercept();
		}
		
		String interceptString = showData ? 
						new NumValue(intercept, maxIntercept.decimals).toString() : "";
		if (interceptColor != null)
			g.setColor(interceptColor);
		drawParameter(g, interceptString, maxInterceptWidth, horizPos, baseline);
		horizPos += interceptSize.width;
		
		g.setColor(oldColor);
		g.drawString(" + ", horizPos, baseline);
		horizPos += fm.stringWidth(" + ");
		
		if (slopeColor != null)
			g.setColor(slopeColor);
		
		String slopeString = showData ? 
						new NumValue(slope, maxSlope.decimals).toString() : "";
		drawParameter(g, slopeString, maxSlopeWidth, horizPos, baseline);
		horizPos += slopeSize.width;
		
		g.setColor(oldColor);
		horizPos += kParamXGap;
		g.drawImage(RegnImages.blueX, horizPos, baseline - RegnImages.kXParamHeight, this);
		horizPos += RegnImages.kXParamWidth;
		
		return horizPos;
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			findLinearPartSize(g);
			
			int leftWidth = RegnImages.kYHatParamWidth + kYEqualsGap;
			modelWidth = leftWidth + 2 * kLeftRightBorder + linearWidth;
			
			modelHeight = interceptSize.height + kHatExtra;
			baseline = getValueBaseline(g.getFontMetrics()) + kHatExtra;
			return true;
		}
		else
			return false;
	}
	
	public int paintModel(Graphics g) {
//		Color oldColor = g.getColor();
//		FontMetrics fm = g.getFontMetrics();
		int horizPos = kLeftRightBorder;
		
		g.drawImage(RegnImages.yHat, horizPos, baseline - RegnImages.kYHatParamAscent, this);
		horizPos += RegnImages.kYHatParamWidth + kYEqualsGap;
		
		horizPos = drawLinearPart(g, horizPos, baseline);
		
		return horizPos;
	}
}
