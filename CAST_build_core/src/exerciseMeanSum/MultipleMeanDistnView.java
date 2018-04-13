package exerciseMeanSum;

import java.awt.*;

import dataView.*;
import distn.*;
import exercise2.*;
import coreGraphics.*;


public class MultipleMeanDistnView extends CoreDragItemsView {
//	static public final String MULTIPLE_MEAN_DISTN = "multipleMeanDistn";
	
	static final public int SAMPLE_SIZES = TEXT_LABELS;
	static final public int DENSITY = TEXT_LABELS + 1;
	
	static final private double kMinDisplayDensity = 0.005;
	
	static final private int kNoOfShades = AccurateDistnArtist.kNoOfShades;
	
	static final private Color kDensityColor = new Color(0xCCCCEE);
	static private Color densityShade[] = new Color[kNoOfShades + 1];
	
	static {
		AccurateDistnArtist.setShades(densityShade, kDensityColor);
	}
	
	private String yKey;
	private double axisMin, axisMax;
	
	private double[][] density;
	
	private boolean initialised = false;
	
	public MultipleMeanDistnView(DataSet theData, XApplet applet, double axisMin,
															double axisMax, String yKey, int[] order, int displayType) {
		super(theData, applet, order, displayType, new Insets(0,5,0,5));
		
		this.yKey = yKey;
		this.axisMin = axisMin;
		this.axisMax = axisMax;
		
		if (displayType == SAMPLE_SIZES) {
			Font bigFont = applet.getBigFont();
			setFont(new Font(bigFont.getName(), Font.BOLD, bigFont.getSize() * 2));
		}
	}
	
	public void setYKey(String yKey) {
		this.yKey = yKey;
		initialised = false;
	}

//----------------------------------------------------------------

	protected void doInitialisation(Graphics g) {
		int nDistns = noOfItems();
		density = new double[nDistns][];
		
		int nPixels = getSize().width;
		density[0] = new double[nPixels];
		
		ContinDistnVariable yVar = (ContinDistnVariable)getVariable(yKey);
		for (int i=0 ; i<nPixels ; i++)
			density[0][i] = yVar.getScaledDensity(axisMin + i * (axisMax - axisMin) / nPixels);
		standardiseHeights(density[0]);
				
		for (int i=1 ; i<nDistns  ; i++) {
			density[i] = createConvolution(density[i - 1]);
			standardiseHeights(density[i]);
		}
	}
	
	private double[] createConvolution(double[] d) {
		double[] d2 = new double[d.length * 2];
		for (int i=0 ; i<d.length ; i++)
			for (int j=0 ; j<d.length ; j++)
				d2[i + j] += d[i] * d[j];
		
		double maxD = 0.0;
		for (int i=0 ; i<d2.length ; i++)
			if (d2[i] > maxD)
				maxD = d2[i];
		
		double minD = maxD * kMinDisplayDensity;
		int iLow;
		for (iLow=0 ; d2[iLow] < minD ; iLow++)
			;
		int iHigh;
		for (iHigh=d2.length-1 ; d2[iHigh] < minD ; iHigh--)
			;
		
		double[] d3 = new double[d.length];
		double step = (iHigh - iLow) / (double)d3.length;
		for (int i=0 ; i<d3.length ; i++) {
			double j0Double = iLow + i * step;
			int j0 = (int)Math.round(Math.ceil(j0Double));
			double j1Double = iLow + (i + 1) * step;
			int j1 = (int)Math.round(Math.floor(j1Double));
			
			double dTotal = 0.0;
			for (int j=j0 ; j<=j1 ; j++)
				dTotal += d2[j];
			
			if (j0Double < j0)
				dTotal += d2[j0 - 1] * (j0 - j0Double);
			if (j1Double > j1)
				dTotal += d2[j1 + 1] * (j1Double - j1);
			
			d3[i] = dTotal;
		}
		
		return d3;
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

//----------------------------------------------------------------
	
	protected int noOfItems() {
		return 4;
	}
	
	protected void drawBackground(Graphics g) {
	}
	
	protected String getItemName(int index) {
		int n = 1;
		for (int i=0 ; i<index ; i++)
			n *= 2;
		return getApplet().translate("Sample size") + " " + n;
	}
	
	protected void drawOneItem(Graphics g, int index, int baseline, int height) {
		baseline = getSize().height - baseline - 1;
		double htFactor = height * 0.9;
		for (int i=0 ; i<density[index].length ; i++) {
			g.setColor(kDensityColor);
			double ht = density[index][i] * htFactor;
			int htPix = (int)Math.round(Math.floor(ht));
			g.drawLine(i, baseline - htPix, i, baseline);
			
			int topShade = (int)Math.round((ht - htPix) * kNoOfShades);
			g.setColor(densityShade[topShade]);
//			g.fillRect(i, baseline - htPix - 1, 1, 1);
			g.drawLine(i, baseline - htPix - 1, i, baseline - htPix - 1);
		}
		
		g.setColor(getForeground());
		g.drawLine(0, baseline, density[index].length - 1, baseline);
	}
	
//	private void printDoubleArray(String title, double[] d) {
//		System.out.print(title + ":  ");
//		for (int i=0 ; i<d.length ; i++)
//			System.out.print(" " + d[i]);
//		System.out.print("\n");
//	}
	
	public void paintView(Graphics g) {
		if (displayType != SAMPLE_SIZES)
			initialise(g);
		super.paintView(g);
	}

//----------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(yKey)) {
			initialised = false;
			repaint();							//		override this if instant redraw is required
		}
	}
}