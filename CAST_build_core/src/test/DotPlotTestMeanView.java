package test;

import java.awt.*;

import dataView.*;
import axis.*;
import qnUtils.*;
import coreGraphics.*;

//import dotPlot.*;


public class DotPlotTestMeanView extends DotPlotView {
//	static public final String DOTPLOT_TEST_MEAN = "dotPlotTestMean";
	
	static final public int kMuWidth = 11;
	static final private int kSpaceForText = 20;
	static final private int kTopBorder = 2;
	
	private LabelValue kMeanLabel;
	
	private HypothesisTest test;
	
	public DotPlotTestMeanView(DataSet theData, XApplet applet, NumCatAxis theAxis, HypothesisTest test) {
		super(theData, applet, theAxis, 1.0);
		this.test = test;
		kMeanLabel = new LabelValue(applet.translate("mean"));
	}
	
	protected int getMaxJitter() {
		return Math.min(super.getMaxJitter(), getDisplayWidth() - getDisplayBorderNearAxis()
																- getDisplayBorderAwayAxis() - kSpaceForText);
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		NumVariable yVar = getNumVariable();
		ValueEnumeration ye = yVar.values();
		double sy = 0.0;
		while (ye.hasMoreValues())
			sy += ye.nextDouble();
		double mean = sy / yVar.noOfValues();
		
		Dimension valueSize = test.getSize(g, HypothesisTest.PARAM_BOUNDARY_DRAW);
		
		try {
			g.setColor(Color.green);
			int horizPos = axis.numValToPosition(mean);
			Point p1 = translateToScreen(horizPos, 0, null);
			int baseline = valueSize.height + kTopBorder + 2 + g.getFontMetrics().getAscent();
			g.drawLine(p1.x, baseline + 2, p1.x, getSize().height - 1);
			
			kMeanLabel.drawCentred(g, p1.x, baseline);
		} catch (AxisException ex) {
		}
		
		try {
			g.setColor(Color.blue);
			int horizPos = axis.numValToPosition(test.getTestValue().toDouble());
			Point p1 = translateToScreen(horizPos, 0, null);
			g.drawLine(p1.x, valueSize.height + kTopBorder + 2, p1.x, getSize().height - 1);
			
			FontMetrics fm = g.getFontMetrics();
			int textWidth = valueSize.width + fm.stringWidth("?");
			int baseline = kTopBorder + test.getBaselineFromTop(g, HypothesisTest.PARAM_BOUNDARY_DRAW);
			test.paintBlue(g, horizPos - textWidth / 2, baseline, true,
																					HypothesisTest.PARAM_BOUNDARY_DRAW, this);
			
			g.setFont(getApplet().getBigBoldFont());
			g.drawString("?", horizPos - textWidth / 2 + valueSize.width, baseline);
		} catch (AxisException ex) {
		}
		g.setColor(getForeground());
	}
}
	
