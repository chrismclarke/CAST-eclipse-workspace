package randomisation;

import java.awt.*;

import dataView.*;
import axis.*;


public class RoundMedianDotView extends RandomisationView {
//	static final private Color kPaleBlueColor = new Color(0xEEEEFF);
	
	private double oldValue[];
	private double median;
	
	public RoundMedianDotView(DataSet theData, XApplet applet, NumCatAxis numAxis, String catKey,
																																									String actualRandKey) {
		super(theData, applet, numAxis, catKey, actualRandKey);
		median = PivotedVariable.getMedian(theData, getActiveNumKey());
	}
	
	public void fixOldInfo() {
		NumVariable yVar = getNumVariable();
		if (oldValue == null || oldValue.length != yVar.noOfValues())
			oldValue = new double[yVar.noOfValues()];
		for (int i=0 ; i<oldValue.length ; i++)
			oldValue[i] = yVar.doubleValueAt(i);
		if (initialised)
			setFrame(0);
		else
			setInitialFrame(0);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		double p = getCurrentFrame() / (double)kEndFrame;
		int j = (currentJitter > 0 && jittering != null && index < jittering.length)
															? ((currentJitter * jittering[index]) >> 14) : 0;
		try {
			int oldH = axis.numValToPosition(oldValue[index]);
			int newH = axis.numValToPosition(theVal.toDouble());
			
			double h = 0.5 * (oldH - newH);
			
			double oldAngle = Math.atan(j / h);
			if (oldAngle < 0)
				oldAngle = Math.PI + oldAngle;
			
			double angle = oldAngle + p * (Math.PI - 2 * oldAngle);
			double r = Math.sqrt(h * h + j * j);
			
			int horizPos = (int)Math.round(0.5 * (oldH + newH) + Math.cos(angle) * r);
			int vertPos = (int)Math.round(Math.sin(angle) * r);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException e) {
			return null;
		}
	}
	
	public double getMean() {
		double sum = 0.0;
		
		ValueEnumeration ye = getNumVariable().values();
		while (ye.hasMoreValues())
			sum += ye.nextDouble();
		return sum / getNumVariable().noOfValues();
	}
	
	public double getMedian() {
		return median;
	}
	
	protected void drawBackground(Graphics g) {
		double mean = getMean();
		Point p0 = null;
		Point p1 = null;
		
		try {
			int medianPos = axis.numValToPosition(median);
			
			p0 = translateToScreen(medianPos, 0, p0);
			g.setColor(Color.blue);
			g.drawLine(0, p0.y, getSize().width, p0.y);
		}
		catch (AxisException e) {
		}
			
		try {
			int meanPos = axis.numValToPosition(mean);
			
			p1 = translateToScreen(meanPos, 0, p1);
			g.setColor(Color.green);
			g.drawLine(0, p1.y, getSize().width, p1.y);
		}
		catch (AxisException e) {
		}
		
		g.setColor(Color.red);
			
		int xPos = getSize().width - 6;
		drawArrow(g, p0.y, p1.y, xPos);
			
		g.setColor(getForeground());
	}
}