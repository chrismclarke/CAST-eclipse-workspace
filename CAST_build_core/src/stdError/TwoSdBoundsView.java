package stdError;

import java.awt.*;

import dataView.*;

public class TwoSdBoundsView extends DataView {
	static final protected int kTopBottomBorder = 2;
	static final protected int kLeftRightBorder = 8;
	static final protected int kLineGap = 4;
	
	static final protected LabelValue kApprox95 = new LabelValue("is approx 0.95");
	
	protected Stacked2SdBoundsView stackedErrorView;
	protected NumValue maxErrorBound;
	
	public TwoSdBoundsView(DataSet theData, XApplet applet, Stacked2SdBoundsView stackedErrorView,
															NumValue maxErrorBound) {
		super(theData, applet, null);
		this.stackedErrorView = stackedErrorView;
		this.maxErrorBound = maxErrorBound;
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		double bias = stackedErrorView.findMean();
		double se = stackedErrorView.findSd();
		
		NumValue lowBound = new NumValue(bias - 2.0 * se, maxErrorBound.decimals);
		String lowBoundString = lowBound.toString();
		NumValue highBound = new NumValue(bias + 2.0 * se, maxErrorBound.decimals);
		String highBoundString = highBound.toString();
		String probFormula = "P( " + lowBoundString + " < error < " + highBoundString + " )";
		g.drawString(probFormula, kLeftRightBorder, kTopBottomBorder + ascent);
		
		kApprox95.drawLeft(g, getSize().width - kLeftRightBorder,
																		kTopBottomBorder + kLineGap + 2 * ascent + descent);
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int height = 2 * (ascent + descent + kTopBottomBorder) + kLineGap;
		
		int width = fm.stringWidth("P( - < error <  )") + 2 * maxErrorBound.stringWidth(g)
																					 + 2 * kLeftRightBorder;
		
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
