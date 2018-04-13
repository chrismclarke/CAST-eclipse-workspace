package qnUtils;

import java.awt.*;

import dataView.*;


public class HypothesisView extends XPanel {
	static final public int VERTICAL = 0;
	static final public int HORIZONTAL = 1;
	
	static final private int kMinHypothesisGap = 40;
	static final private int kMaxHypothesisGap = 60;
	static final private int kVertHypothesisGap = 5;
	
	private HypothesisTest test;
	
//	private boolean initialised = false;
	private int orientation;
	
//	private int ascent, descent, titleAscent, minTopBorder, minBottomBorder, maxLabelWidth;
	
	public HypothesisView(HypothesisTest test, XApplet applet) {
		this(test, HORIZONTAL, applet);
	}
	
	public HypothesisView(HypothesisTest test, int orientation, XApplet applet) {
		this.test = test;
		setFont(applet.getBigBoldFont());
		setForeground(Color.blue);
		this.orientation = orientation;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int h0Width = hypothesisWidth(g, true);
		int hAWidth = hypothesisWidth(g, false);
		
		if (orientation == HORIZONTAL) {
			int centreGap = Math.min(kMaxHypothesisGap, (getSize().width - h0Width - hAWidth) / 3);
			
			int x = (getSize().width - h0Width - hAWidth - centreGap) / 2;
			drawHypothesis(g, true, x, Math.max(test.getBaselineFromTop(g, HypothesisTest.GENERIC_DRAW),
													test.getBaselineFromTop(g, HypothesisTest.PARAM_DRAW)));
			x += h0Width + centreGap;
			drawHypothesis(g, false, x, Math.max(test.getBaselineFromTop(g, HypothesisTest.GENERIC_DRAW),
													test.getBaselineFromTop(g, HypothesisTest.PARAM_DRAW)));
		}
		else {
			int x = (getSize().width - Math.max(h0Width, hAWidth)) / 2;
			int baselineFromTop = Math.max(test.getBaselineFromTop(g, HypothesisTest.GENERIC_DRAW),
													test.getBaselineFromTop(g, HypothesisTest.PARAM_DRAW));
			int lineHt = Math.max(test.getSize(g, HypothesisTest.GENERIC_DRAW).height,
													test.getSize(g, HypothesisTest.PARAM_DRAW).height);
			drawHypothesis(g, true, x, baselineFromTop);
			drawHypothesis(g, false, x, lineHt + kVertHypothesisGap + baselineFromTop);
		}
	}
	
	private void drawHypothesis(Graphics g, boolean nullTrue, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		
		test.paintBlue(g, x, y, nullTrue, HypothesisTest.GENERIC_DRAW, this);
		x += test.getSize(g, HypothesisTest.GENERIC_DRAW).width;
		
		g.drawString(": ", x, y);
		x += fm.stringWidth(": ");
		
		test.paintBlue(g, x, y, nullTrue, HypothesisTest.PARAM_DRAW, this);
	}
	
	public void setHypothesis(HypothesisTest test) {
		this.test = test;
	}
	
	private int hypothesisWidth(Graphics g, boolean nullTrue) {
		return test.getSize(g, HypothesisTest.GENERIC_DRAW).width
													+ g.getFontMetrics().stringWidth(": ")
													+ test.getSize(g, HypothesisTest.PARAM_DRAW).width;
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		int genericHeight = test.getSize(g, HypothesisTest.GENERIC_DRAW).height;
		int paramHeight = test.getSize(g, HypothesisTest.PARAM_DRAW).height;
		int genericAscent = test.getBaselineFromTop(g, HypothesisTest.GENERIC_DRAW);
		int paramAscent = test.getBaselineFromTop(g, HypothesisTest.PARAM_DRAW);
		
		int height = Math.max(genericAscent, paramAscent)
									+ Math.max(genericHeight - genericAscent, paramHeight - paramAscent);
		int width;
		if (orientation == HORIZONTAL)
			width = hypothesisWidth(g, true) + kMinHypothesisGap + hypothesisWidth(g, false);
		else {
			width = Math.max(hypothesisWidth(g, true), hypothesisWidth(g, false));
			height = height * 2 + kVertHypothesisGap;
		}
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
	
