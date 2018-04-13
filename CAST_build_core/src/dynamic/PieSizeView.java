package dynamic;

import java.awt.*;

import dataView.*;


public class PieSizeView extends DataView {
//	static public final String PIE_SIZE_VIEW = "pieSizeView";
	
	protected String yKey;
	protected Color[] catColors;
	protected boolean showOnlyProportions = false;
	
	private boolean initialised = false;
	private double maxTotal;
	
	public PieSizeView(DataSet theData, XApplet applet, String yKey, Color[] catColors) {
		super(theData, applet, new Insets(0,0,0,0));
		this.yKey = yKey;
		this.catColors = catColors;
	}
	
	public void setOnlyProportions(boolean showOnlyProportions) {
		this.showOnlyProportions = showOnlyProportions;
	}
	
	public void setCatColors(Color[] catColors) {
		this.catColors = catColors;
	}
	
	protected void doInitialisation(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		if (yVar instanceof NumSeriesVariable) {
			NumSeriesVariable ySeriesVar = (NumSeriesVariable)getVariable(yKey);
			int seriesLength = ySeriesVar.seriesLength();
			double total[] = new double[seriesLength];
			ValueEnumeration ye = ySeriesVar.values();
			while (ye.hasMoreValues()) {
				NumSeriesValue y = (NumSeriesValue)ye.nextValue();
				for (int i=0 ; i<seriesLength ; i++)
					total[i] += y.toDouble(i);
			}
			for (int i=0 ; i<seriesLength ; i++)
				maxTotal = Math.max(maxTotal, total[i]);
		}
		else {
			ValueEnumeration ye = yVar.values();
			while (ye.hasMoreValues())
				maxTotal += ye.nextDouble();
		}
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected double getTotal() {
		double total = 0.0;
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues())
			total += ye.nextDouble();
		return total;
	}
	
	private double circleScaling() {
		if (showOnlyProportions)
			return 1.0;
		else
			return Math.sqrt(getTotal() / maxTotal);
	}
	
	
	public void paintView(Graphics g) {
		initialise(g);
		int maxDiameter = Math.min(getSize().width, getSize().height) - 10;
		int diameter = (int)Math.round(maxDiameter * circleScaling());
		int circleLeft = (getSize().width - diameter) / 2;
		int circleTop = (getSize().height - diameter) / 2;
		
		double total = 0.0;
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		for (int i=0 ; i<nVals ; i++)
			total += yVar.doubleValueAt(i);
		
		double endCum = 0.25;
		for (int i=0 ; i<nVals ; i++) {
			if (endCum < 0.0)
				endCum += 1.0;
			double startCum = endCum;
			endCum -= yVar.doubleValueAt(i) / total;
			
			int startDegrees = (int)Math.round(360 * startCum);
			int endDegrees = (int)Math.round(360 * endCum);
			g.setColor(catColors[i]);
			g.fillArc(circleLeft, circleTop, diameter, diameter, startDegrees, (endDegrees - startDegrees));
		}
		
		g.setColor(getForeground());
		g.drawOval(circleLeft, circleTop, diameter, diameter);
		
//		Graphics2D g2d = (Graphics2D)g;
//		Composite oldComposite = g2d.getComposite();
//		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
//		g.fillRect(0, getSize().height / 3, getSize().width, getSize().height / 3);
//		g2d.setComposite(oldComposite);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
	
