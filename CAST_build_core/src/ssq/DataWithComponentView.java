package ssq;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class DataWithComponentView extends DataView {
//	static public final String LS_COMPONENT_PLOT = "lsComponent";
	
	static final public int NO_COMPONENT_DISPLAY = -1;
	
	static final private Color kPaleBlue = new Color(0x99CCFF);
	static final private Color kPaleGray = new Color(0xDDDDDD);
	static final private Color kMidGray = new Color(0xCCCCCC);
	
	static final protected int kLineOffset = 4;
	static final private int kArrowSize = 3;
	
	protected String xKey, yKey;
	protected String lsKey, modelKey;
	protected int componentDisplay;
	protected HorizAxis xAxis;
	protected VertAxis yAxis;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	private int noInXCat[] = null;
	private int scalePercent = 100;
	
	public DataWithComponentView(DataSet theData, XApplet applet,
							HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey,
							String lsKey, String modelKey, int componentDisplay) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xKey = xKey;
		this.yKey = yKey;
		this.lsKey = lsKey;
		this.modelKey = modelKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.componentDisplay = componentDisplay;
	}
	
	public void changeComponentDisplay(int newComponentDisplay) {
		componentDisplay = newComponentDisplay;
		repaint();
	}
	
	protected Point getScreenPoint(double y, int xPos, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		try {
			int vertPos = yAxis.numValToPosition(y);
			return translateToScreen(xPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void paintView(Graphics g) {
		drawModel(g);
		
		int meanOnScreen = drawOverallMean(g, yKey, kMidGray);
		Point thePoint = null;
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Variable xVar = (Variable)getVariable(xKey);
		
		CoreModelVariable lsFit = null;
		if (lsKey != null) {
			lsFit = (CoreModelVariable)getVariable(lsKey);
			drawFittedMean(g, lsFit);
		}
		
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex < 0) {
			if (componentDisplay != NO_COMPONENT_DISPLAY)
				drawComponent(g, meanOnScreen, lsFit);
		}
		else {
			double y = yVar.doubleValueAt(selectedIndex);
			Value x = xVar.valueAt(selectedIndex);
			int xPos = getXPos(x, xVar, selectedIndex);
			thePoint = getScreenPoint(y, xPos, thePoint);
			if (thePoint != null) {
				g.setColor(Color.red);
				drawCrossBackground(g, thePoint);
				g.setColor(getForeground());
			}
			
			drawAllComponents(g, meanOnScreen, lsFit, selectedIndex);
		}
		
		g.setColor(getForeground());
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		int index = 0;
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			int xPos = getXPos(x, xVar, index);
			thePoint = getScreenPoint(y, xPos, thePoint);
			
			if (thePoint != null)
				drawCross(g, thePoint);
			index ++;
		}
	}
	
	protected void drawModel(Graphics g) {
		if (modelKey != null) {
			CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
			if (model != null)
				model.drawModel(g, this, xAxis, yAxis, kPaleGray, Color.white);
			
			g.setColor(getForeground());
		}
	}
	
	protected int getXPos(Value x, Variable xVar, int index) {
		if (xVar instanceof NumVariable)
			return xAxis.numValToRawPosition(((NumValue)x).toDouble());
		else {
			if (!jitteringInitialised)
				initialiseJittering((CatVariable)xVar);
			int xCat = ((CatVariable)xVar).labelIndex(x);
			int offset = ((2 * jitter[index] - noInXCat[xCat]) * scalePercent) / 100 + 1;
			int xCatPos = (xAxis ==  null) ? getSize().width / 2 : xAxis.catValToPosition(xCat);
			return xCatPos + offset;
		}
	}
	
	protected int getXPos(Value xNum, Variable xNumVar, Value xCat, Variable xCatVar,
																																					int index) {
		int basicPos = xAxis.numValToRawPosition(((NumValue)xNum).toDouble());
		
		if (!jitteringInitialised)
			initialiseJittering((CatVariable)xCatVar);
		int xCatIndex = ((CatVariable)xCatVar).labelIndex(xCat);
		int offset = ((2 * jitter[index] - noInXCat[xCatIndex]) * scalePercent) / 100 + 1;
		return basicPos + offset;
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
	
	protected int drawOverallMean(Graphics g, String yKey, Color meanColor) {
		double sy = 0.0;
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues())
			sy += ye.nextDouble();
		double mean = sy / yVar.noOfValues();
		
		int meanPos = yAxis.numValToRawPosition(mean);
		int meanOnScreen = translateToScreen(0, meanPos, null).y;
		
		int selectedIndex = getSelection().findSingleSetFlag();
		if (canDrawOverallMean() && (componentDisplay != NO_COMPONENT_DISPLAY
																												|| selectedIndex >= 0)) {
			g.setColor(meanColor);
			g.drawLine(0, meanOnScreen, getSize().width - 1, meanOnScreen);
		}
		
		return meanOnScreen;
	}
	
	protected boolean canDrawOverallMean() {
		return true;
	}
	
	protected void drawFittedMean(Graphics g, CoreModelVariable lsFit) {
		g.setColor(kPaleBlue);
		lsFit.drawMean(g, this, xAxis, yAxis);
	}
	
	protected void drawComponent(Graphics g, int meanOnScreen, CoreModelVariable lsFit) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		Variable xVar = (Variable)getVariable(xKey);
		ValueEnumeration xe = xVar.values();
		Point dataPoint = null;
		Point fitPoint = new Point(0, 0);
		int index = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			Value x = xe.nextValue();
			int xPos = getXPos(x, xVar, index);
			
			double y = ye.nextDouble();
			int yPos = yAxis.numValToRawPosition(y);
			dataPoint  = translateToScreen(xPos, yPos, dataPoint);
			
			drawOneComponent(g, meanOnScreen, dataPoint, xVar, x, xPos, lsFit, fitPoint);
			
			index ++;
		}
	}
	
	protected void drawOneComponent(Graphics g, int meanOnScreen, Point dataPoint, Variable xVar,
															Value x, int xPos, CoreModelVariable lsFit, Point fitPoint) {
		g.setColor(BasicComponentVariable.kComponentColor[componentDisplay]);
		if (componentDisplay == BasicComponentVariable.EXPLAINED
								|| componentDisplay == BasicComponentVariable.RESIDUAL) {
			double fit = lsFit.evaluateMean(x);
			int fitPos = yAxis.numValToRawPosition(fit);
			fitPoint  = translateToScreen(xPos, fitPos, fitPoint);
		}
		switch (componentDisplay) {
			case BasicComponentVariable.TOTAL:
				g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, meanOnScreen);
				break;
			case BasicComponentVariable.EXPLAINED:
				g.drawLine(dataPoint.x, fitPoint.y, dataPoint.x, meanOnScreen);
				break;
			case BasicComponentVariable.RESIDUAL:
				g.drawLine(dataPoint.x, fitPoint.y, dataPoint.x, dataPoint.y);
				break;
			default:
				break;
		}
	}
	
	protected void drawAllComponents(Graphics g, int meanOnScreen, CoreModelVariable lsFit,
																int selectedIndex) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Variable xVar = (Variable)getVariable(xKey);
		
		Value x = xVar.valueAt(selectedIndex);
		int xPos = getXPos(x, xVar, selectedIndex);
		
		double y = yVar.doubleValueAt(selectedIndex);
		int yPos = yAxis.numValToRawPosition(y);
		Point dataPoint  = translateToScreen(xPos, yPos, null);
		
		double fit = lsFit.evaluateMean(x);
		int fitPos = yAxis.numValToRawPosition(fit);
		Point fitPoint  = translateToScreen(xPos, fitPos, null);
		
		g.setColor(BasicComponentVariable.kTotalColor);
		drawArrow(g, dataPoint.x - kLineOffset, dataPoint.y, meanOnScreen);
		
		g.setColor(BasicComponentVariable.kExplainedColor);
		drawArrow(g, dataPoint.x + kLineOffset, fitPoint.y, meanOnScreen);
		
		g.setColor(BasicComponentVariable.kResidColor);
		drawArrow(g, dataPoint.x, dataPoint.y, fitPoint.y);
	}
	
	protected void drawArrow(Graphics g, int x, int y1, int y2) {
		g.drawLine(x, y1, x, y2);
		int yOffset = (y2 > y1) ? -kArrowSize : kArrowSize;
		g.drawLine(x - kArrowSize, y2 + yOffset, x, y2);
		g.drawLine(x + kArrowSize, y2 + yOffset, x, y2);
	}

//-------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		jitteringInitialised = false;
		repaint();
	}
	
//-----------------------------------------------------------------------------------
	
	private Point crossPos[];
	private static final int kMinHitDist = 9;
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (crossPos == null) {
			NumVariable yVar = (NumVariable)getVariable(yKey);
			int noOfVals = yVar.noOfValues();
			crossPos = new Point[noOfVals];
			Variable xVar = (Variable)getVariable(xKey);
			ValueEnumeration ye = yVar.values();
			ValueEnumeration xe = xVar.values();
			for (int i=0 ; i<noOfVals ; i++) {
				double yVal = ye.nextDouble();
				Value xVal = xe.nextValue();
				int xPos = getXPos(xVal, xVar, i);
				crossPos[i] = getScreenPoint(yVal, xPos, null);
			}
		}
		
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<crossPos.length ; i++)
			if (crossPos[i] != null) {
				int xDist = crossPos[i].x - x;
				int yDist = crossPos[i].y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
					minDist = dist;
				}
			}
		if (gotPoint && minDist < kMinHitDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		super.endDrag(startPos, endPos);
		crossPos = null;
	}
}
	
