package exerciseMeanSum;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class SimpleDensityView extends DataView {
//	static public final String SIMPLE_DENSITY = "simpleDensity";
	
	static final private int kNoOfShades = AccurateDistnArtist.kNoOfShades;
	
	static final private Color kDensityColor = new Color(0xCCCCEE);
	static private Color densityShade[] = new Color[kNoOfShades + 1];
	
	static {
		AccurateDistnArtist.setShades(densityShade, kDensityColor);
	}
	
	private String yKey;
	private HorizAxis theAxis;
	
	private double[] density;
	
	private boolean initialised = false;
	
	public SimpleDensityView(DataSet theData, XApplet applet, String yKey, HorizAxis theAxis) {
		super(theData, applet, new Insets(2, 5, 0, 5));
		this.yKey = yKey;
		this.theAxis = theAxis;
		
		setViewBorder(new Insets(0,0,0,0));
	}
	
	public void changeDistnKey(String yKey) {
		this.yKey = yKey;
	}

//----------------------------------------------------------------

	protected void doInitialisation(Graphics g) {
		int nPixels = getSize().width;
		double axisMin = theAxis.minOnAxis;
		double axisMax = theAxis.maxOnAxis;
		
		density = new double[nPixels];
		
		ContinDistnVariable yVar = (ContinDistnVariable)getVariable(yKey);
		for (int j=0 ; j<nPixels ; j++)
			density[j] = yVar.getScaledDensity(axisMin + j * (axisMax - axisMin) / nPixels);
		standardiseHeights(density);
	}
	
	private void standardiseHeights(double[] d) {
		double maxD = 0.0;
		for (int i=0 ; i<d.length ; i++)
			if (d[i] > maxD)
				maxD = d[i];
		
		for (int i=0 ; i<d.length ; i++)
			d[i] /= maxD;
	}

	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	public void resetDisplay() {
		initialised = false;
	}

//----------------------------------------------------------------
	
	public void paintView(Graphics g) {
		initialise(g);
		
//		ContinDistnVariable distn = (ContinDistnVariable)getVariable(yKey);
		
		int baseline = getSize().height;
		double htFactor = baseline * 0.9;
		
		for (int i=0 ; i<density.length ; i++) {
			g.setColor(kDensityColor);
			double ht = density[i] * htFactor;
			int htPix = (int)Math.round(Math.floor(ht));
			g.drawLine(i, baseline - htPix, i, baseline);
			
			int topShade = (int)Math.round((ht - htPix) * kNoOfShades);
			g.setColor(densityShade[topShade]);
			g.drawLine(i, baseline - htPix - 1, i, baseline - htPix - 1);
		}
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (yKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
