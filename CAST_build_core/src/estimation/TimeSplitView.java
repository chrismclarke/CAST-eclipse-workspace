package estimation;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;


public class TimeSplitView extends DataView {
	static final private Color kEvenSliceColor = new Color(0xFFFFBB);
	static final private Color kOddSliceColor = new Color(0xBBEEFF);
	static final private Color kCrossColor = new Color(0xCC0000);
	
	private String distnKey;
	private HorizAxis timeAxis;
	
	private Random generator;
	
	private boolean hasSample = false;
	private boolean[] hasEvent = null;
	
	public TimeSplitView(DataSet theData, XApplet applet, String distnKey, HorizAxis timeAxis,
																																	 long randomSeed) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.distnKey = distnKey;
		this.timeAxis = timeAxis;
		
		generator = new Random(randomSeed);
		setCrossSize(HUGE_CROSS);
	}
	
	public void paintView(Graphics g) {
		if (!hasSample)
			generateEvents();
		
		BinomialDistnVariable binom = (BinomialDistnVariable)getData().getVariable(distnKey);
		int n = binom.getCount();
		
		Point p1 = null;
		Point p2 = null;
		for (int i=0 ; i<n ; i++) {
			double lowVal = i / (double)n;
			double highVal = (i + 1) / (double)n;
			try {
				int lowPos = timeAxis.numValToPosition(lowVal);
				int highPos = timeAxis.numValToPosition(highVal);
				p1 = translateToScreen(lowPos, 0, p1);
				p2 = translateToScreen(highPos, 0, p2);
				g.setColor(i % 2 == 0 ? kEvenSliceColor : kOddSliceColor);
				g.fillRect(p1.x, 0, p2.x - p1.x, getSize().height);
			}
			catch (AxisException e) {
			}
		}
		
		for (int i=0 ; i<n ; i++)
			if (hasEvent[i]) {
				double lowVal = i / (double)n;
				double highVal = (i + 1) / (double)n;
				try {
					int lowPos = timeAxis.numValToPosition(lowVal);
					int highPos = timeAxis.numValToPosition(highVal);
					p1 = translateToScreen(lowPos, 0, p1);
					p2 = translateToScreen(highPos, 0, p2);
					g.setColor(kCrossColor);
					drawBoldCross(g, new Point((p1.x + p2.x) / 2, getSize().height - getCrossSize() - 4));
				}
				catch (AxisException e) {
				}
			}
	}
	
	private void generateEvents() {
		BinomialDistnVariable binom = (BinomialDistnVariable)getData().getVariable(distnKey);
		int n = binom.getCount();
		double p = binom.getProb();
		if (hasEvent == null || hasEvent.length != n)
			hasEvent = new boolean[n];
		
		for (int i=0 ; i<n ; i++)
			hasEvent[i] = generator.nextDouble() < p;
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key)) {
			hasSample = false;
			repaint();
		}
	}
	
	public void takeSample() {
		hasSample = false;
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