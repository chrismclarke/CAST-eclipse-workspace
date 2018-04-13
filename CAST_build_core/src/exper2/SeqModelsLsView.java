package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

import glmAnova.*;


public class SeqModelsLsView extends CoreOneFactorView implements SetLastExplanInterface {
	
	private String[] fitKey;
	private int fitIndex = 0;
	
	private double[] levelMean = null;
	
	public SeqModelsLsView(DataSet theData, XApplet applet, NumCatAxis xAxis, NumCatAxis yAxis,
								String xKey, String yKey, String[] fitKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, null);
		this.fitKey = fitKey;
		findLevelMeans();
	}
	
	public void setLastExplanatory(int lastSeparateX) {
		fitIndex = lastSeparateX;
		findLevelMeans();
		repaint();
	}
	
	private void findLevelMeans() {
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int nLevels = xVar.noOfCategories();
		if (levelMean == null || levelMean.length != nLevels)
			levelMean = new double[nLevels];
		boolean foundMean[] = new boolean[nLevels];
		
		if (fitIndex < 0) {
			NumVariable yVar = (NumVariable)getVariable(yKey);
			double sy = 0.0;
			int n = yVar.noOfValues();
			for (int i=0 ; i<n ; i++)
				sy += yVar.doubleValueAt(i);
			double mean = sy / n;
			for (int i=0 ; i<nLevels ; i++)
				levelMean[i] = mean;
		}
		else {
			NumVariable fitVar = (NumVariable)getVariable(fitKey[fitIndex]);
			for (int i=0 ; i<xVar.noOfValues() ; i++) {
				int xCat = xVar.getItemCategory(i);
				if (!foundMean[xCat])
					levelMean[xCat] = ((NumValue)fitVar.valueAt(i)).toDouble();
			}
		}
	}
	
	protected double getFittedValue(CoreModelVariable model, int index, CatVariable xVar) {
		if (fitIndex < 0)
			return levelMean[0];
		else {
			NumVariable fitVar = (NumVariable)getVariable(fitKey[fitIndex]);
			return ((NumValue)fitVar.valueAt(index)).toDouble();
		}
	}
	
	protected void drawFactorMeans(Graphics g, CoreModelVariable model, CatVariable xVar) {
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<xVar.noOfCategories() ; i++) {
			double yMean = levelMean[i];
			
			int xCenter = xAxis.catValToPosition(i);
			int maxJitter = noInXCat[i] * scalePercent / 100;
			int lowMeanXPos = xCenter - maxJitter - kMeanExtra;
			int highMeanXPos = xCenter + maxJitter + kMeanExtra;
			
			p0 = getScreenPoint(yMean, lowMeanXPos, p0);
			p1 = getScreenPoint(yMean, highMeanXPos, p1);
			
			drawOneMean(i, p0, p1, g);
		}
	}
	
}
	
