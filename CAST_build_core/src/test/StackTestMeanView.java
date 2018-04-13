package test;

import java.awt.*;

import dataView.*;
import axis.*;
import qnUtils.*;
import coreGraphics.*;

//import dotPlot.*;
//import randomStat.*;


//public class StackTestMeanView extends StackedPlusNormalView {
public class StackTestMeanView extends StackedDotPlotView {
//	static public final String STACK_TEST_MEAN = "stackTestMean";
	
	static final public int kMuWidth = 11;
//	static final private int kSpaceForText = 20;
	static final private int kTopBorder = 2;
	
	private HypothesisTest test;
	
	public StackTestMeanView(DataSet theData, XApplet applet,
										NumCatAxis theAxis, String normalKey, HypothesisTest test) {
//		super(viewName, theData, dataName, applet, theAxis, normalKey, useOffscreen);
		super(theData, applet, theAxis);
		this.test = test;
	}
	
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		try {
			Dimension valueSize = test.getSize(g, HypothesisTest.PARAM_BOUNDARY_DRAW);
			
			g.setColor(Color.blue);
			int horizPos = axis.numValToPosition(test.getTestValue().toDouble());
			Point p1 = translateToScreen(horizPos, 0, null);
			g.drawLine(p1.x, valueSize.height + kTopBorder + 2, p1.x, getSize().height - 1);
			
			test.paintBlue(g, p1.x - valueSize.width / 2, valueSize.height + kTopBorder,
															true, HypothesisTest.PARAM_BOUNDARY_DRAW, this);
			
			g.setColor(Color.red);
			horizPos = axis.numValToPosition(getMean());
			p1 = translateToScreen(horizPos, 0, p1);
			g.drawLine(p1.x, 0, p1.x, getSize().height - 1);
			
			LabelValue kMeanString = new LabelValue(getApplet().translate("sample mean"));
			kMeanString.drawLeft(g, getSize().width - 2, g.getFontMetrics().getAscent() + kTopBorder);
		} catch (AxisException ex) {
		}
		g.setColor(getForeground());
	}
	
	private double getMean() {
		NumVariable yVar = getNumVariable();
		ValueEnumeration ye = yVar.values();
		double sy = 0.0;
		while (ye.hasMoreValues())
			sy += ye.nextDouble();
		return sy / yVar.noOfValues();
	}
}
	
