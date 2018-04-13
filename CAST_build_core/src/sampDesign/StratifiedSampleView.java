package sampDesign;

import java.awt.*;

import dataView.*;
import axis.*;
import random.*;


public class StratifiedSampleView extends DataView {
//	static public final String STRATIFIED_SAMPLE_PLOT = "stratifiedSamplePlot";
	
	private static final int kMaxJitter = 35;
	static final protected Color kSampleColor = new Color(0x990000);
	
	protected int currentJitter = 0;
	protected int jittering[] = null;
	private boolean jitteringInitialised = false;
	
	private VertAxis numAxis;
	protected HorizAxis stratumAxis;
	private StratifiedSampleView linkedView;
	
	protected String yKey;
	
	public StratifiedSampleView(DataSet theData, XApplet applet, HorizAxis stratumAxis,
																	VertAxis numAxis, String yKey, StratifiedSampleView linkedView) {
									//		linkedView allows two views to show identical jittering for the popn
									//		values, despite different samples
		super(theData, applet, new Insets(5, 5, 5, 5));
		
		this.stratumAxis = stratumAxis;
		this.numAxis = numAxis;
		this.yKey = yKey;
		this.linkedView = linkedView;
	}
	
	public void setLinkedView(StratifiedSampleView linkedView) {
		this.linkedView = linkedView;
	}
	
	protected void checkJittering() {
		int noOfStrata = getNoOfStrata(yKey);
		if (!jitteringInitialised) {
			currentJitter = Math.min(kMaxJitter, (getSize().width - getViewBorder().left
																							- getViewBorder().right) / noOfStrata / 2);
			jitteringInitialised = true;
		}
		int dataLength = getPopnSize(yKey);
		if (currentJitter > 0 && (jittering == null || jittering.length != dataLength)) {
			RandomBits generator = new RandomBits(14, dataLength);			//	between 0 and 2^14 = 16384
			jittering = generator.generate();
			if (linkedView != null)
				linkedView.jittering = jittering;
		}
	}
	
	protected int getNoOfStrata(String yKey) {
		StratifiedSampleVariable yVar = (StratifiedSampleVariable)getVariable(yKey);
		return yVar.getPopnValues().length;
	}
	
	protected int getPopnSize(String yKey) {
		StratifiedSampleVariable yVar = (StratifiedSampleVariable)getVariable(yKey);
		return yVar.getPopnSize();
	}


//---------------------------------------------------------------

	
	protected Point getScreenPoint(int index, NumValue theVal, int stratum, Point thePoint) {
		if (Double.isNaN(theVal.toDouble()))
			return null;
		try {
			int vertPos = numAxis.numValToPosition(theVal.toDouble());
			int horizPos = stratumToPos(stratum);
			
			if (currentJitter > 0 && jittering != null && index < jittering.length)
				horizPos += ((currentJitter * jittering[index]) >> 14) - currentJitter / 2;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected int stratumToPos(int stratum) throws AxisException {
		return stratumAxis.catValToPosition(stratum);
	}
	
	public void paintView(Graphics g) {
		StratifiedSampleVariable y = (StratifiedSampleVariable)getVariable(yKey);
		NumValue[][] popnValue = y.getPopnValues();
		Point thePoint = null;
		
		checkJittering();
		
		g.setColor(Color.lightGray);
		int index = 0;
		for (int stratum=0 ; stratum<popnValue.length ; stratum++) {
			for (int i=0 ; i<popnValue[stratum].length ; i++) {
				NumValue popVal = popnValue[stratum][i];
				thePoint = getScreenPoint(index, popVal, stratum, thePoint);
				drawCross(g, thePoint);
				index ++;
			}
		}
		
		g.setColor(kSampleColor);
		ValueEnumeration e = y.values();
		NumValue nextSampleVal = e.hasMoreValues() ? (NumValue)e.nextValue() : null;
		index = 0;
		for (int stratum=0 ; stratum<popnValue.length ; stratum++) {
			for (int i=0 ; i<popnValue[stratum].length ; i++) {
				NumValue popVal = popnValue[stratum][i];
				thePoint = getScreenPoint(index, popVal, stratum, thePoint);
				
				if (popVal == nextSampleVal) {
					drawBlob(g, thePoint);
					nextSampleVal = e.hasMoreValues() ? (NumValue)e.nextValue() : null;
				}
				index ++;
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
	
