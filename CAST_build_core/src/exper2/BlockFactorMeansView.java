package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class BlockFactorMeansView extends CoreOneFactorView {
	
	static final private Color[] kBlockCrossColor = {Color.red, Color.blue, Color.orange, Color.green, Color.black};
	static final private Color[] kBlockMeanColor = new Color[kBlockCrossColor.length];
	static {
		for (int i=0 ; i<kBlockCrossColor.length ; i++)
			kBlockMeanColor[i] = dimColor(kBlockCrossColor[i], 0.85);
	}
	
	private String blockKey;
	
	public BlockFactorMeansView(DataSet theData, XApplet applet, NumCatAxis xAxis, NumCatAxis yAxis,
								String xKey, String blockKey, String yKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, modelKey);
		this.blockKey = blockKey;
	}
	
	protected double getFittedValue(CoreModelVariable model, int index, CatVariable xVar) {
		Value xLabel = xVar.valueAt(index);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		Value blockLabel = blockVar.valueAt(index);
		return evaluateModelMean(model, xLabel, blockLabel);
	}
	
	private double evaluateModelMean(CoreModelVariable model, Value xLabel, Value blockLabel) {
		Value[] v = {xLabel, blockLabel};
		return model.evaluateMean(v);
	}
	
	
	protected void setCrossColor(Graphics g, int index) {
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int blockCat = blockVar.getItemCategory(index);
		g.setColor(kBlockCrossColor[blockCat]);
	}
	
	protected void drawFactorMeans(Graphics g, CoreModelVariable model, CatVariable xVar) {
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int nx = xVar.noOfCategories();
		int nBlocks = blockVar.noOfCategories();
//		int xStep = xAxis.catValToPosition(1) - xAxis.catValToPosition(0);
		
		Point p0 = null;
		Point p1 = null;
		for (int b=0 ; b<nBlocks ; b++) {
			Value blockValue = blockVar.getLabel(b);
			Point pLast = null;
			for (int x=0 ; x<nx ; x++) {
				Value xValue = xVar.getLabel(x);
				int xCenter = xAxis.catValToPosition(x);
				int maxJitter = noInXCat[x] * scalePercent / 100;
				int lowMeanXPos = xCenter - maxJitter - kMeanExtra;
				int highMeanXPos = xCenter + maxJitter + kMeanExtra;
				
				double mean = evaluateModelMean(model, xValue, blockValue);
				p0 = getScreenPoint(mean, lowMeanXPos, p0);
				p1 = getScreenPoint(mean, highMeanXPos, p1);
				
				if (pLast != null) {
					g.setColor(kBlockMeanColor[b]);
					g.drawLine(pLast.x, pLast.y, p0.x, p0.y);
				}
				g.setColor(kBlockCrossColor[b]);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				Point pTemp = pLast;
				pLast = p1;
				p1 = pTemp;
			}
		}
	}
}
	
