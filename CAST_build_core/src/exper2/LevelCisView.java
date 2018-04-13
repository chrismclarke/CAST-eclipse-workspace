package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import models.*;


public class LevelCisView extends DataView {
	
	static final private Color kCiFillColor = new Color(0xBBDDFF);
	static final private Color kCiLineColor = new Color(0x999999);
	
	static final private int kCiBarWidth = 14;
	static final private int kCiBarGap = 3;
	
	protected String xKey, yKey, lsKey;
	protected HorizAxis xAxis;
	protected VertAxis yAxis;
	
	private boolean initialised = false;
	private int jitter[] = null;
	private int noInXCat[] = null;
	private int scalePercent = 100;
	
	public LevelCisView(DataSet theData, XApplet applet, HorizAxis xAxis,
													VertAxis yAxis, String xKey, String yKey, String lsKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xKey = xKey;
		this.yKey = yKey;
		this.lsKey = lsKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	private void initialise(CatVariable xVar) {
		int noOfVals = xVar.noOfValues();
		if (jitter == null || jitter.length != noOfVals)
			jitter = new int[noOfVals];
		int noOfCats = xVar.noOfCategories();
		if (noInXCat == null || noInXCat.length != noOfCats)
			noInXCat = new int[noOfCats];
		else
			for (int i=0 ; i<noOfCats ; i++)
				noInXCat[i] = 0;
		
		for (int i=0 ; i<noOfVals ; i++) {
			int xCat = xVar.getItemCategory(i);
			jitter[i] = noInXCat[xCat];
			noInXCat[xCat] ++;
		}
		int usedHoriz = noOfVals * 2 + noOfCats * getCrossSize() * 2;
		int availableHoriz = (getSize().width * 1) / 4;
		scalePercent = Math.min(100, (availableHoriz * 100) / usedHoriz);
		
		initialised = true;
	}
	
	protected Point getScreenPoint(double y, Value x, CatVariable xVar, int index, Point thePoint) {
		if (Double.isNaN(y))
			return null;
			
		int vertPos = yAxis.numValToRawPosition(y);
		int xCat = xVar.labelIndex(x);
		int horizPos = xAxis.catValToPosition(xCat);
		int offset;
		if (index >= 0)
			offset = ((2 * jitter[index] - noInXCat[xCat]) * scalePercent) / 100 + 1;
		else
			offset = noInXCat[xCat] * scalePercent / 100 + 1;		//	just to right of all crosses
		horizPos += offset;
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		if (!initialised)
			initialise(xVar);
		
		Point p = null;
		
		GroupsModelVariable lsFit = (GroupsModelVariable)getVariable(lsKey);
		drawCis(g, yVar, xVar, lsFit);
		
		g.setColor(getForeground());
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		int index = 0;
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			p = getScreenPoint(y, x, xVar, index, p);
			
			if (p != null)
				drawCross(g, p);
			index ++;
		}
	}
	
	protected void drawCis(Graphics g, NumVariable yVar, CatVariable xVar,
																											GroupsModelVariable lsFit) {
		int catCounts[] = new int[xVar.noOfCategories()];
		for (int i=0 ; i<yVar.noOfValues() ; i++)
			if (!Double.isNaN(yVar.doubleValueAt(i)))
				catCounts[xVar.getItemCategory(i)] ++;
		
		Point pMid = null;
		Point pLow = null;
		Point pHigh = null;
		
		for (int i=0 ; i<xVar.noOfCategories() ; i++) {
			double s = lsFit.getSD(i).toDouble();
			int df = lsFit.getDf(i);
			double t95 = TTable.quantile(0.975, df);
			
			Value x = xVar.getLabel(i);
			double yMean = lsFit.evaluateMean(x);
			double se = s / Math.sqrt(catCounts[i]);
			
			pMid = getScreenPoint(yMean, x, xVar, -1, pMid);
			pLow = getScreenPoint(yMean - se * t95, x, xVar, -1, pLow);
			pHigh = getScreenPoint(yMean + se * t95, x, xVar, -1, pHigh);
			pMid.x += kCiBarGap;
			pLow.x += kCiBarGap;
			pHigh.x += kCiBarGap;
			
			g.setColor(kCiFillColor);
			g.fillRect(pHigh.x, pHigh.y, kCiBarWidth, pLow.y - pHigh.y);
			
			g.setColor(kCiLineColor);
			g.drawLine(pHigh.x, pHigh.y, pHigh.x + kCiBarWidth - 1, pHigh.y);
			g.drawLine(pLow.x, pLow.y, pHigh.x + kCiBarWidth - 1, pLow.y);
			g.drawLine(pMid.x, pMid.y, pHigh.x + kCiBarWidth - 1, pMid.y);
		}
	}

//-------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(yKey)) {
			initialised = false;
			repaint();
		}
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
