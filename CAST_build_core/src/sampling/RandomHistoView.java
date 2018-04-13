package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import random.*;
import distn.*;
import coreGraphics.*;


public class RandomHistoView extends PixelDensityView {
//	static final public String RANDOM_HISTO = "randomHisto";
	
	static final public int NARROWER = 0;
	static final public int WIDER = 1;
	
	private int sampleSize;
	
	private RandomBinomial rand;
	private double sampleProb[];
	private int maxGroupingLevel, groupingLevel;
	
	public RandomHistoView(DataSet theData, XApplet applet, HorizAxis horizAxis, VertAxis probAxis,
																							int sampleSize, long samplingSeed, String distnKey) {
		super(theData, applet, horizAxis, probAxis, distnKey);
		
		this.sampleSize = sampleSize;
		
		rand = new RandomBinomial(1, 10, 0.5);
		rand.setSeed(samplingSeed);
	}
	
	protected boolean initialise() {
		if (super.initialise()) {
			CoreVariable yCore = getVariable(distnKey);
			if (yCore instanceof ContinDistnVariable) {
				int noOfClasses = horizAxis.getAxisLength();
				maxGroupingLevel = 0;
				noOfClasses --;
				while (noOfClasses > 0) {
					maxGroupingLevel ++;
					noOfClasses >>= 1;
				}
			}
			else {
				DiscreteDistnVariable y = (DiscreteDistnVariable)yCore;
				int min = (int)Math.round(Math.ceil(horizAxis.minOnAxis));
				int max = (int)Math.round(Math.floor(horizAxis.maxOnAxis));
				
				prob = new double[max - min + 3];
				double lowProb = y.getCumulativeProb(min - 0.5);
				prob[0] = lowProb;
				for (int i=0 ; i<=max-min ; i++) {
					double thisProb = y.getScaledProb(min + i) * y.getProbFactor();
					prob[i+1] = thisProb;
					lowProb += thisProb;
				}
				prob[max - min + 2] = 1.0 - lowProb;
				maxGroupingLevel = 0;
			}
			groupingLevel = (maxGroupingLevel * 2) / 3;
			
			takeSample();
			return true;
		}
		else
			return false;
	}
	
	public int getGroupingLevel() {
		return groupingLevel;
	}
	
	public int getMaxGroupingLevel() {
		return maxGroupingLevel;
	}
	
	public void changeGroupingLevel(int changeType) {
		groupingLevel = (changeType == WIDER) ? groupingLevel + 1 : groupingLevel - 1;
		repaint();
	}
	
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
		takeSample();
	}
	
	public void takeSample() {
		initialise();
		
		if (sampleProb == null || sampleProb.length != prob.length)
			sampleProb = new double[prob.length];
			
		if (sampleSize <= 0)				//		infinite sample size
			for (int i=0 ; i<prob.length ; i++)
				sampleProb[i] = prob[i];
		else {
			int countLeft = sampleSize;
			double sampleSizeDouble = sampleSize;
			double probLeft = 1.0;
			for (int i=0 ; i<prob.length-1 ; i++) {
				double itemProb = prob[i] / probLeft;
				rand.setParameters(countLeft, itemProb);
				int classCount = rand.generateOne();
				double classCountDouble = classCount;
				sampleProb[i] = classCountDouble / sampleSizeDouble;
				
				countLeft -= classCount;
				probLeft -= prob[i];
			}
			sampleProb[prob.length-1] = countLeft / sampleSizeDouble;
		}
		repaint();
	}
	
	private void recursivePaint(Graphics g, int xLow, int xHigh, int currentGroupingLevel,
																								double pixelFactor) {
		if (currentGroupingLevel > groupingLevel) {
			int xMid = (xLow + xHigh) / 2;
			recursivePaint(g, xLow, xMid, currentGroupingLevel -1, pixelFactor);
			recursivePaint(g, xMid, xHigh, currentGroupingLevel -1, pixelFactor);
		}
		else {
			double groupProb = 0.0;
			for (int i=xLow+1 ; i<=xHigh ; i++)
				groupProb += sampleProb[i];
			double height = groupProb / (xHigh - xLow) * pixelFactor;
			int vertPos = probAxis.numValToRawPosition(height) - 1;
			Point topLeft = translateToScreen(xLow, vertPos, null);
			Point bottomRight = translateToScreen(xHigh, 0, null);
			g.fillRect(topLeft.x, topLeft.y + 1, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
		}
	}
	
	public void paintView(Graphics g) {
		initialise();
		g.setColor(Color.blue);
		
		CoreVariable yCore = getVariable(distnKey);
		if (yCore instanceof ContinDistnVariable) {
//			ContinDistnVariable y = (ContinDistnVariable)yCore;
			int axisLength = horizAxis.getAxisLength();
			double pixelFactor = axisLength / (horizAxis.maxOnAxis - horizAxis.minOnAxis);
			
			recursivePaint(g, 0, axisLength, maxGroupingLevel, pixelFactor);
		}
		else {
//			DiscreteDistnVariable y = (DiscreteDistnVariable)yCore;
			int min = (int)Math.round(Math.ceil(horizAxis.minOnAxis));
			int max = (int)Math.round(Math.floor(horizAxis.maxOnAxis));
			
			int barBottom = translateToScreen(0, 0, null).y + 1;
			for (int i=0 ; i<=max-min ; i++)
				try {
					int horizPos = horizAxis.numValToPosition(min + i);
					int vertPos = probAxis.numValToRawPosition(sampleProb[i + 1]) - 1;
					Point barTop = translateToScreen(horizPos, vertPos, null);
					g.fillRect(barTop.x - 2, barTop.y, 5, barBottom - barTop.y);
				}
				catch (AxisException e) {
				}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
