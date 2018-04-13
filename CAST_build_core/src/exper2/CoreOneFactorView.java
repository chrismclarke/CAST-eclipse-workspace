package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class CoreOneFactorView extends MarginalDataView {
	
	static final protected int kMeanExtra = 10;
	
	static final protected Color kMeanColor = Color.red;
	static final private Color kResidualColor = new Color(0xFFCED3);		//	pink
	
	protected String xKey, yKey;
	protected String modelKey;
	protected NumCatAxis xAxis, yAxis;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	protected int noInXCat[] = null;
	protected int scalePercent = 100;
	
	private boolean showResiduals = true;
	
	public CoreOneFactorView(DataSet theData, XApplet applet, NumCatAxis xAxis,
											NumCatAxis yAxis, String xKey, String yKey, String modelKey) {
		super(theData, applet, new Insets(5, 5, 5, 5), xAxis);
		this.xKey = xKey;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	public void setModelKey(String modelKey) {
		this.modelKey = modelKey;
	}
	
	public void setShowResiduals(boolean showResiduals) {
		this.showResiduals = showResiduals;
		jitteringInitialised = false;
	}
	
	protected Point getScreenPoint(double y, int xPos, Point thePoint) {
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
		if (!showResiduals)
			scalePercent /= 3;
		
		jitteringInitialised = true;
	}
	
	protected int getXPos(Value x, CatVariable xVar, int index) {
		int xCat = xVar.labelIndex(x);
		int offset = ((2 * jitter[index] - noInXCat[xCat]) * scalePercent) / 100 + 1;
		int xCatPos = (xAxis ==  null) ? getSize().width / 2 : xAxis.catValToPosition(xCat);
		return xCatPos + offset;
	}
	
	protected double evaluateModelMean(CoreModelVariable model, int index, CatVariable xVar) {
		if (model instanceof GroupsModelVariable || model instanceof MultipleRegnModel)
			return model.evaluateMean(xVar.getLabel(index));
		else
			return model.evaluateMean(new NumValue(index));
	}
	
	protected double getFittedValue(CoreModelVariable model, int index, CatVariable xVar) {
		int catIndex = xVar.getItemCategory(index);
		return evaluateModelMean(model, catIndex, xVar);
	}
	
	private void drawResiduals(Graphics g, NumVariable yVar, CatVariable xVar,
														CoreModelVariable model) {
		Point crossPoint = null;
		Point fitPoint = null;
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		int index = 0;
		g.setColor(kResidualColor);
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			int xPos = getXPos(x, xVar, index);
			crossPoint = getScreenPoint(y, xPos, crossPoint);
			
			if (crossPoint != null) {
				double fit = getFittedValue(model, index, xVar);
				int fitPos = yAxis.numValToRawPosition(fit);
				fitPoint  = translateToScreen(xPos, fitPos, fitPoint);
				if (fitPoint != null)
					g.drawLine(fitPoint.x, fitPoint.y, crossPoint.x, crossPoint.y);
			}
			index ++;
		}
	}
	
	protected void setCrossColor(Graphics g, int index) {
	}
	
	private void drawCrosses(Graphics g, NumVariable yVar, CatVariable xVar) {
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
			
			if (crossPoint != null) {
				setCrossColor(g, index);
				drawCross(g, crossPoint);
			}
			index ++;
		}
	}
	
	protected void drawBackground(Graphics g) {
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CoreModelVariable model = (modelKey == null) ? null
																				: (CoreModelVariable)getVariable(modelKey);
		
		if (!jitteringInitialised)
			initialiseJittering(xVar);
		
		drawBackground(g);
		
		if (showResiduals)
			drawResiduals(g, yVar, xVar, model);
		
		drawFactorMeans(g, model, xVar);
		
		drawCrosses(g, yVar, xVar);
	}
	
	protected void drawOneMean(int levelIndex, Point p0, Point p1, Graphics g) {
		g.setColor(kMeanColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
	}
	
	protected void drawFactorMeans(Graphics g, CoreModelVariable model, CatVariable xVar) {
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<xVar.noOfCategories() ; i++) {
			double yMean = evaluateModelMean(model, i, xVar);
			
			int xCenter = xAxis.catValToPosition(i);
			int maxJitter = noInXCat[i] * scalePercent / 100;
			int lowMeanXPos = xCenter - maxJitter - kMeanExtra;
			int highMeanXPos = xCenter + maxJitter + kMeanExtra;
			
			p0 = getScreenPoint(yMean, lowMeanXPos, p0);
			p1 = getScreenPoint(yMean, highMeanXPos, p1);
			
			drawOneMean(i, p0, p1, g);
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
	
