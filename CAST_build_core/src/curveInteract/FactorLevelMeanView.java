package curveInteract;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class FactorLevelMeanView extends MarginalDataView {
//	static public final String FACTOR_LEVEL_MEAN_PLOT = "factorLevelMean";
	
	static final private Color kGroupMeanColor = new Color(0x6699FF);
	static final private Color kResidColor = Color.red;
	
//	static final private int kMeanExtra = 10;
	
	private String xFactorKey, yKey;
	private String lsKey;
	private NumCatAxis xAxis, yAxis;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	private int noInXCat[] = null;
	private int scalePercent = 100;
	
	public FactorLevelMeanView(DataSet theData, XApplet applet, NumCatAxis xAxis, NumCatAxis yAxis,
																												String yKey, String xFactorKey, String lsKey) {
		super(theData, applet, new Insets(5, 5, 5, 5), xAxis);
		this.xFactorKey = xFactorKey;
		this.yKey = yKey;
		this.lsKey = lsKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	private Point getScreenPoint(double y, int xPos, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		int yPos = yAxis.numValToRawPosition(y);
		return translateToScreen(xPos, yPos, thePoint);
	}
	
	private void initialiseJittering(CatVariable xVar) {
		if (jitteringInitialised)
			return;
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
		int availableHoriz = (getSize().width * 3) / 5;
		scalePercent = Math.min(100, (availableHoriz * 100) / usedHoriz);
		
		jitteringInitialised = true;
	}
	
	private int getXPos(Value x, CatVariable xVar, int index) {
		int xCat = xVar.labelIndex(x);
		int offset = ((2 * jitter[index] - noInXCat[xCat]) * scalePercent) / 100 + 1;
		int xCatPos = (xAxis ==  null) ? getSize().width / 2 : xAxis.catValToPosition(xCat);
		return xCatPos + offset;
	}
	
	private void drawBackground(Graphics g) {
		CatVariable xVar = (CatVariable)getVariable(xFactorKey);
		int nLevels = xVar.noOfCategories();
		MultipleRegnModel ls = (MultipleRegnModel)getVariable(lsKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		Point p0 = null, p1 = null;
		double indicatorValue[] = new double[nLevels - 1];
		double levelMean[] = new double[nLevels];
		for (int i=0 ; i<nLevels ; i++) {
//			Value xValue = xVar.getLabel(i);
			for (int j=1 ; j<nLevels ; j++)
				indicatorValue[j - 1] = (j == i) ? 1.0 : 0.0;
			levelMean[i] = ls.evaluateMean(indicatorValue);
		}
		
		g.setColor(kGroupMeanColor);
		int xOffset = (xAxis.catValToPosition(1) - xAxis.catValToPosition(0)) / 2 + 1;
		for (int i=0 ; i<nLevels ; i++) {
			int xCenter = xAxis.catValToPosition(i);
//			int maxJitter = noInXCat[i] * scalePercent / 100;
//			int lowMeanXPos = xCenter - maxJitter - kMeanExtra;
//			int highMeanXPos = xCenter + maxJitter + kMeanExtra;
			
			p0 = getScreenPoint(levelMean[i], xCenter - xOffset, p0);
			p1 = getScreenPoint(levelMean[i], xCenter + xOffset, p1);
			
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		
		g.setColor(kResidColor);
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ye = yVar.values();
		int index = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			Value x = xe.nextValue();
			int xLevel = xVar.labelIndex(x);
			double y = ye.nextDouble();
			int xPos = getXPos(x, xVar, index);
			
			p0 = getScreenPoint(y, xPos, p0);
			p1 = getScreenPoint(levelMean[xLevel], xPos, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			index ++;
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xFactorKey);
		
		initialiseJittering(xVar);
		
		drawBackground(g);
		
		Point crossPoint = null;
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		int index = 0;
		g.setColor(getForeground());
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			int xPos = getXPos(x, xVar, index);
			crossPoint = getScreenPoint(y, xPos, crossPoint);
			
			if (crossPoint != null)
				drawCross(g, crossPoint);
			index ++;
		}
	}

//-------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		jitteringInitialised = false;
		repaint();
	}
	
	public int minDisplayWidth() {
		return 150;
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
