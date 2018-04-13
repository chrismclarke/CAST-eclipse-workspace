package glmAnova;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class ContrastFactorView extends MarginalDataView {
//	static public final String CONTRAST_FACTOR_PLOT = "contrastFactor";
	
	static final public int FACTOR = 0;
	static final public int BETWEEN_GROUPS = 1;
	static final public int WITHIN_GROUPS = 2;
	
	static final private int kMeanExtra = 10;
//	static final private int kHitSlop = 7;
	
	static final private Color kMeanColor = new Color(0xDDDDDD);
	static final private Color kGroupedMeanColor = new Color(0xFFCED3);		//	pink
	static final private Color kCategoryMeanColor = new Color(0xAACCFF);
	static final private Color kComponentColor = Color.red;
	
	private String xKey, xGroupKey, yKey;
	private String ls1Key, ls2Key;
	private NumCatAxis xAxis, yAxis;
	private int levelsInGroup0;
	
	private int displayType = FACTOR;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	private int noInXCat[] = null;
	private int scalePercent = 100;
	
	public ContrastFactorView(DataSet theData, XApplet applet,
							NumCatAxis xAxis, NumCatAxis yAxis, String yKey, String xKey, String xGroupKey,
							String ls1Key, String ls2Key, int levelsInGroup0) {
		super(theData, applet, new Insets(5, 5, 5, 5), xAxis);
		this.xKey = xKey;
		this.xGroupKey = xGroupKey;
		this.yKey = yKey;
		this.ls1Key = ls1Key;
		this.ls2Key = ls2Key;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.levelsInGroup0 = levelsInGroup0;
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
	}
	
	private Point getScreenPoint(double y, int xPos, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		int yPos = yAxis.numValToRawPosition(y);
		return translateToScreen(xPos, yPos, thePoint);
	}
	
	private void initialiseJittering(CatVariable xVar) {
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
	
	private double getOverallMean() {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		double sy = 0.0;
		while (ye.hasMoreValues())
			sy += ye.nextDouble();
		return sy / yVar.noOfValues();
	}
	
	private void drawBackground(Graphics g) {
		double overallMean = getOverallMean();
		Point p0 = getScreenPoint(overallMean, 0, null);
		g.setColor(kMeanColor);
		g.drawLine(0, p0.y, getSize().width, p0.y);
		
		CatVariable xGroupVar = (CatVariable)getVariable(xGroupKey);
		Value catGroup0 = xGroupVar.getLabel(0);
		Value catGroup1 = xGroupVar.getLabel(1);
		GroupsModelVariable groupMeanLS = (GroupsModelVariable)getVariable(ls1Key);
		int xPosBetweenGroups = (xAxis.catValToPosition(levelsInGroup0)
																					+ xAxis.catValToPosition(levelsInGroup0 - 1)) / 2;
		
		g.setColor(kGroupedMeanColor);
		double lowGroupMean = groupMeanLS.evaluateMean(catGroup0);
		p0 = getScreenPoint(lowGroupMean, xPosBetweenGroups, null);
		g.drawLine(0, p0.y, p0.x, p0.y);
		
		double highGroupMean = groupMeanLS.evaluateMean(catGroup1);
		p0 = getScreenPoint(highGroupMean, xPosBetweenGroups, null);
		g.drawLine(p0.x, p0.y, getSize().width, p0.y);
		
		g.setColor(kCategoryMeanColor);
		
		Point p1 = null;
		CatVariable xVar = (CatVariable)getVariable(xKey);
		GroupsModelVariable factorMeanLS = (GroupsModelVariable)getVariable(ls2Key);
		for (int i=0 ; i<xVar.noOfCategories() ; i++) {
			Value xValue = xVar.getLabel(i);
			double levelMean = factorMeanLS.evaluateMean(xValue);
			
			int xCenter = xAxis.catValToPosition(i);
			int maxJitter = noInXCat[i] * scalePercent / 100;
			int lowMeanXPos = xCenter - maxJitter - kMeanExtra;
			int highMeanXPos = xCenter + maxJitter + kMeanExtra;
			
			p0 = getScreenPoint(levelMean, lowMeanXPos, p0);
			p1 = getScreenPoint(levelMean, highMeanXPos, p1);
			
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
	}
	
	public void drawComponents(Graphics g, CatVariable xVar, CatVariable xGroupVar) {
		Point fitAPoint = null;
		Point fitBPoint = null;
		
		double overallMean = getOverallMean();
		GroupsModelVariable ls1Fit = (GroupsModelVariable)getVariable(ls1Key);
		GroupsModelVariable ls2Fit = (GroupsModelVariable)getVariable(ls2Key);
		
		ValueEnumeration xe = xVar.values();
		ValueEnumeration xge = xGroupVar.values();
		int index = 0;
		g.setColor(kComponentColor);
		while (xe.hasMoreValues()) {
			Value x = xe.nextValue();
			Value xg = xge.nextValue();
			int xPos = getXPos(x, xVar, index);
			
			double fitA = (displayType == FACTOR || displayType == BETWEEN_GROUPS) ? overallMean :
										ls1Fit.evaluateMean(xg);
			fitAPoint = getScreenPoint(fitA, xPos, fitAPoint);
			double fitB = (displayType == FACTOR || displayType == WITHIN_GROUPS) ? ls2Fit.evaluateMean(x)
																										: ls1Fit.evaluateMean(xg);
			fitBPoint = getScreenPoint(fitB, xPos, fitBPoint);
			
			g.drawLine(fitAPoint.x, fitAPoint.y, fitBPoint.x, fitBPoint.y);
			
			index ++;
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable xGroupVar = (CatVariable)getVariable(xGroupKey);
		
		if (!jitteringInitialised)
			initialiseJittering(xVar);
		
		drawBackground(g);
		
		drawComponents(g, xVar, xGroupVar);
		
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
	
