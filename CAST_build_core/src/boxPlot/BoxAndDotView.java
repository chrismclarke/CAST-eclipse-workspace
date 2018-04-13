package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import random.RandomBits;


public class BoxAndDotView extends BoxView {
	private static final int kMaxJitter = 30;
	
//	static public final String BOX_AND_DOT_PLOT = "boxAndDotPlot";
	
	protected int currentJitter = 0;
	protected int jittering[] = null;
	
	private Color crossHiliteColor = new Color(0xFF6600);
	
	public BoxAndDotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
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
	
	private int groupIndex(int itemIndex) {
		return 0;
	}
	
	protected int getMaxJitter() {
//		return Math.min(kMaxJitter, getDisplayWidth() / 3);
		return Math.min(kMaxJitter, (getDisplayWidth() - BoxInfo.kBoxHeight) / 2);
	}
	
	protected void initialiseJittering() {
		currentJitter = getMaxJitter();
	}
	
	protected void initialiseCounts() {
	}
	
	protected void countPosition(int screenHoriz) {
	}
	
	protected Color getHiliteColor() {
		return crossHiliteColor;
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		int dataLength = getNumVariable().noOfValues();
		if (jittering == null || jittering.length != dataLength) {
			RandomBits generator = new RandomBits(14, dataLength);
																					//	between 0 and 2^14 = 16384
			jittering = generator.generate();
		}
		
		Point thePoint = null;
		int index = 0;
		
		if (getHiliteColor() != null) {
			g.setColor(getHiliteColor());
			ValueEnumeration e = variable.values();
			FlagEnumeration fe = getSelection().getEnumeration();
			while (e.hasMoreValues()) {
				NumValue nextVal = (NumValue)e.nextValue();
				boolean nextSel = fe.nextFlag();
				if (nextSel) {
					thePoint = getScreenPoint(index, nextVal, thePoint);
					if (thePoint != null)
						drawCrossBackground(g, thePoint);
				}
				index++;
			}
		}
		
		g.setColor(getForeground());
		ValueEnumeration e = variable.values();
		index = 0;
		initialiseCounts();
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null) {
				countPosition(thePoint.x);
				drawMark(g, thePoint, groupIndex(index));
			}
			index++;
		}
	}
	
	protected void shadeBackground(Graphics g) {
	}
	
	protected void drawCounts(Graphics g) {
	}
	
	protected int getBoxBottom() {
		return 2 * currentJitter;
	}
	
	protected void initialise(NumVariable variable) {
		initialiseJittering();
		super.initialise(variable);
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		if (!initialised)
			initialise(variable);
		
		shadeBackground(g);
		
		drawDotPlot(g, variable);
		drawBoxPlot(g, variable.getSortedData(), boxInfo);
		drawCounts(g);
	}

	public int minDisplayWidth() {
		return super.minDisplayWidth() + 12;
	}
}
	
