package simulation;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class StripedDensityView extends PixelDensityView {
	static final private double kMaxHtFactor = 0.85;
	
	private String dataKey;
//	private XApplet applet;
	
	private boolean initialisedData = false;
	private int pixelTop[];
	private int cumulative[];
	
	public StripedDensityView(DataSet theData, XApplet applet, HorizAxis horizAxis, String distnKey, String dataKey) {
		super(theData, applet, horizAxis, null, distnKey);
		this.dataKey = dataKey;
//		this.applet = applet;
	}
	
	protected boolean initialise() {
		if (super.initialise()) {
			if (pixelTop == null || pixelTop.length != (prob.length - 2))
				pixelTop = new int[prob.length - 2];
			
			double probFactor = getSize().height * kMaxHtFactor / maxProb();
			
			for (int i=0 ; i<pixelTop.length ; i++)
				pixelTop[i] = getSize().height - (int)Math.round(prob[i+1] * probFactor);
			
			initialisedData = false;
			return true;
		}
		else
			return false;
	}
	
	protected boolean initialiseData() {
		if (!initialisedData) {
			if (cumulative == null || cumulative.length != pixelTop.length)
				cumulative = new int[pixelTop.length];
			else
				for (int i=0 ; i<cumulative.length ; i++)
					cumulative[i] = 0;
			
			NumVariable yVar = (NumVariable)getVariable(dataKey);
			ValueEnumeration e = yVar.values();
			while (e.hasMoreValues()) {
				double nextVal = e.nextDouble();
				int horizPos = 0;
				try {
					horizPos = horizAxis.numValToPosition(nextVal);
				} catch (AxisException ex) {
					if (ex.axisProblem == AxisException.TOO_HIGH_ERROR)
						horizPos = cumulative.length-1;
				}
				for (int i=horizPos ; i<cumulative.length ; i++)
					cumulative[i] ++;
			}
			
			initialisedData = true;
			return true;
		}
		else
			return false;
	}
	
	private double maxProb() {
		double maxP = 0.0;
		for (int i=1 ; i<prob.length-1 ; i++)
			maxP = Math.max(maxP, prob[i]);
		return maxP;
	}
	
	public void paintView(Graphics g) {
		initialise();
		initialiseData();
		
		Point zeroPos = translateToScreen(0, 0, null);
		
		for (int i=0 ; i<horizAxis.getAxisLength() ; i++) {
			g.setColor((cumulative[i] & 1) == 0 ? Color.lightGray
															: Color.darkGray);
			g.drawLine(zeroPos.x + i, pixelTop[i], zeroPos.x + i, zeroPos.y + 1);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		super.doChangeVariable(g, key);
		if (key.equals(distnKey) || key.equals(dataKey))
			initialisedData = false;
	}
}
	
