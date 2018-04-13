package exerciseMeanSum;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;

import exerciseNumGraph.*;


public class MultipleNormalDistnView extends MultipleDistnView {
//	static public final String MULTIPLE_NORMAL_DISTN = "multipleNormalDistn";
	
	static final public int SAMPLE_SIZES = TEXT_LABELS;			//	0
	static final public int DENSITY = 5;
	
	static final private int kNoOfShades = AccurateDistnArtist.kNoOfShades;
	
	static final private Color kDensityColor = new Color(0xCCCCEE);
	static private Color densityShade[] = new Color[kNoOfShades + 1];
	
	static {
		AccurateDistnArtist.setShades(densityShade, kDensityColor);
	}
	
	private double[][] density;
	
	private boolean initialised = false;
	
	public MultipleNormalDistnView(DataSet theData, XApplet applet, HorizAxis axis, String[] yKey,
														int[] order, int displayType) {
		super(theData, applet, axis, yKey, order, displayType);
		setViewBorder(new Insets(0,0,0,0));
	}
	
	public void setYKeys(String[] yKey) {
		super.setYKeys(yKey);
		initialised = false;
	}

//----------------------------------------------------------------

	protected void doInitialisation(Graphics g) {
		int nDistns = noOfItems();
		density = new double[nDistns][];
		
		double minSd = Double.POSITIVE_INFINITY;
		for (int i=0 ; i<nDistns ; i++) {
			ContinDistnVariable yVar = (ContinDistnVariable)getVariable(yKey[i]);
			minSd = Math.min(minSd, yVar.getSD().toDouble());
		}
		
		int nPixels = getSize().width;
		double axisMin = theAxis.minOnAxis;
		double axisMax = theAxis.maxOnAxis;
		for (int i=0 ; i<nDistns ; i++) {
			density[i] = new double[nPixels];
			
			ContinDistnVariable yVar = (ContinDistnVariable)getVariable(yKey[i]);
			double sd = yVar.getSD().toDouble();
			for (int j=0 ; j<nPixels ; j++)
				density[i][j] = yVar.getScaledDensity(axisMin + j * (axisMax - axisMin) / nPixels);
			standardiseHeights(density[i], minSd / sd);
		}
	}
	
	private void standardiseHeights(double[] d, double factor) {
		double maxD = 0.0;
		for (int i=0 ; i<d.length ; i++)
			if (d[i] > maxD)
				maxD = d[i];
		
		for (int i=0 ; i<d.length ; i++)
			d[i] *= (factor / maxD);
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
	
	protected void drawOneItem(Graphics g, int index, int baseline, int height) {
		if (displayType == DENSITY)
			drawOneDensity(g, index, baseline, height);
		else
			super.drawOneItem(g, index, baseline, height);
	}
	
	protected void drawOneDensity(Graphics g, int index, int baseline, int height) {
		baseline = getSize().height - baseline;
		double htFactor = height * 0.9;
		for (int i=0 ; i<density[index].length ; i++) {
			g.setColor(kDensityColor);
			double ht = density[index][i] * htFactor;
			int htPix = (int)Math.round(Math.floor(ht));
			g.drawLine(i, baseline - htPix, i, baseline);
			
			int topShade = (int)Math.round((ht - htPix) * kNoOfShades);
			g.setColor(densityShade[topShade]);
			g.drawLine(i, baseline - htPix - 1, i, baseline - htPix - 1);
		}
		
		g.setColor(getForeground());
		g.drawLine(0, baseline, density[index].length - 1, baseline);
	}
	
	public void paintView(Graphics g) {
		if (displayType == DENSITY)
			initialise(g);
		super.paintView(g);
	}

//----------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		for (int i=0 ; i<yKey.length ; i++)
			if (key.equals(yKey[i])) {
				initialised = false;
				repaint();
				return;
			}
	}
}