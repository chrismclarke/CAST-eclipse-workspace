package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import random.*;


public class SimpleBoxDotView extends BoxView {
	private static final int kMaxJitter = 30;
	private static final int kMinTopGap = 20;
	
	static final private Color kDotBackground = new Color(0xEDEDFF);
	
//	static public final String SIMPLE_BOXDOT_PLOT = "simpleBoxDot";
	
	protected int currentJitter = 0;
	protected int jittering[] = null;
	
	private boolean shadeDotPlot = false;
	
	public SimpleBoxDotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	public void setShadeDotPlot(boolean shadeDotPlot) {
		this.shadeDotPlot = shadeDotPlot;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		try {
			int horizPos = axis.numValToPosition(theVal.toDouble());
			int vertPos =  (currentJitter * jittering[index]) >> 14;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected int getMaxJitter() {
		return Math.min(kMaxJitter, (getSize().height - getViewBorder().top - getViewBorder().bottom) / 3);
	}
	
	protected void initialiseJittering() {
		currentJitter = getMaxJitter();
		
		int dataLength = getNumVariable().noOfValues();
		RandomBits generator = new RandomBits(14, dataLength);
																					//	between 0 and 2^14 = 16384
		jittering = generator.generate();
	}
	
	protected int getBoxBottom() {
		return Math.min(2 * currentJitter, getSize().height - BoxInfo.kBoxHeight - kMinTopGap);
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		if (shadeDotPlot) {
			g.setColor(kDotBackground);
			int dotHeight = currentJitter + 3 * getViewBorder().bottom;
			g.fillRect(0, getSize().height - dotHeight, getSize().width, dotHeight);
			g.setColor(getForeground());
		}
		
		Point thePoint = null;
		NumValue sortedData[] = variable.getSortedData();
		for (int i=0 ; i<sortedData.length ; i++) {
			thePoint = getScreenPoint(i, sortedData[i], thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		if (jittering == null)
			initialiseJittering();
		if (!initialised)
			initialise(variable);
		
		drawDotPlot(g, variable);
		drawBoxPlot(g, variable.getSortedData(), boxInfo);
	}
}
	
