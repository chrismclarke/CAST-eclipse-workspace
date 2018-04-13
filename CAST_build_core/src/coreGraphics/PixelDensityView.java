package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


abstract public class PixelDensityView extends DataView {
	protected String distnKey;
	protected HorizAxis horizAxis;
	protected VertAxis probAxis;
	
	protected double prob[];
	
	private boolean initialised = false;
	
	public PixelDensityView(DataSet theData, XApplet applet, HorizAxis horizAxis,
																										VertAxis probAxis, String distnKey) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		
		this.distnKey = distnKey;
		this.horizAxis = horizAxis;
		this.probAxis = probAxis;
	}
	
	protected boolean initialise() {
		if (!initialised) {
			if (distnKey != null) {
				CoreVariable yCore = getVariable(distnKey);
				if (yCore instanceof ContinDistnVariable) {
					ContinDistnVariable y = (ContinDistnVariable)yCore;
					
					int noOfClasses = horizAxis.getAxisLength();
					double min = horizAxis.minOnAxis;
					double max = horizAxis.maxOnAxis;
					
					if (prob == null || prob.length != (noOfClasses + 2))
						prob = new double[noOfClasses + 2];
					double x = min;
					double step = (max - min) / noOfClasses;
					double lastCum = 0.0;
					for (int i=0 ; i<=noOfClasses ; i++) {
						double nextCum = y.getCumulativeProb(x);
						prob[i] = nextCum - lastCum;
						lastCum = nextCum;
						x += step;
					}
					prob[noOfClasses + 1] = 1.0 - lastCum;
				}
			}
			
			initialised = true;
			return true;
		}
		else
			return false;
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(distnKey)) {
			initialised = false;
			reset();
		}
		super.doChangeVariable(g, key);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
