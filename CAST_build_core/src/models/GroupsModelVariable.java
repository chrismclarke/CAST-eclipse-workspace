package models;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;

public class GroupsModelVariable extends CoreModelVariable {
	static final private int kHalfMeanGap = 5;
	
	private CatVariable explan;
	
	private NumValue sd[];
	private NumValue mean[];
	private int df[];
	
	private int pooledDf;
	private NumValue pooledSd;
	private boolean usePooledSd = false;
	
	public GroupsModelVariable(String theName, DataSet data, String catKey) {
		super(theName, data, catKey);
		explan = (CatVariable)data.getVariable(catKey);
		int noOfGroups = explan.noOfCategories();
		sd = new NumValue[noOfGroups];
		mean = new NumValue[noOfGroups];
	}
	
	public void setUsePooledSd(boolean usePooledSd) {
		this.usePooledSd = usePooledSd;
	}
	
	public void setParameters(String params) {
		StringTokenizer theParams = new StringTokenizer(params);
		int nGroups = theParams.countTokens() / 2;
		if (mean == null || mean.length != nGroups)
			mean = new NumValue[nGroups];
		if (sd == null || sd.length != nGroups)
			sd = new NumValue[nGroups];
		for (int i=0 ; i<nGroups ; i++) {
			mean[i] = new NumValue(theParams.nextToken());
			sd[i] = new NumValue(theParams.nextToken());
		}
		pooledSd = new NumValue(sd[0].toDouble(), sd[0].decimals);
	}
	
	public double evaluateMean(Value[] x) {
		return mean[explan.labelIndex(x[0])].toDouble();
	}
	
	public NumValue evaluateSD(Value x) {
		if (usePooledSd)
			return pooledSd;
		else
			return sd[explan.labelIndex(x)];
	}
	
	public NumValue evaluateSD() {
		if (usePooledSd)
			return pooledSd;
		else
			return sd[0];						//		Assumes all sds are same
	}
	
	public NumValue getMinSD() {
		if (usePooledSd)
			return pooledSd;
		else {
			NumValue minSD = sd[0];
			for (int i=1 ; i<sd.length ; i++)
				if (sd[i].toDouble() < minSD.toDouble())
					minSD = sd[i];
			return minSD;
		}
	}
	
	public void setMean(double newMean, int group) {
		if (mean[group] == null)
			mean[group] = new NumValue(newMean);
		else
			mean[group].setValue(newMean);
	}
	
	public void setMean(NumValue newMean, int group) {
		mean[group] = newMean;
	}
	
	public NumValue getMean(int group) {
		return mean[group];
	}
	
	public void setSD(double newSD) {
		pooledSd.setValue(newSD);
		for (int group=0 ; group<sd.length ; group++) {
			if (sd[group] == null)
				sd[group] = new NumValue(newSD);
			else
				sd[group].setValue(newSD);
		}
	}
	
	public void setSD(NumValue newSD) {
		pooledSd = newSD;
		for (int group=0 ; group<sd.length ; group++)
			sd[group] = newSD;
	}
	
	public void setSD(double newSD, int group) {
		if (sd[group] == null)
			sd[group] = new NumValue(newSD);
		else
			sd[group].setValue(newSD);
	}
	
	public NumValue getSD(int group) {
		if (usePooledSd)
			return pooledSd;
		else
			return sd[group];
	}
	
	public int getDf(int group) {
		if (usePooledSd)
			return pooledDf;
		else
			return df[group];
	}
	
	public int noOfParameters() {
		CatVariable xVar = (CatVariable)data.getVariable(xKey[0]);
		return xVar.noOfCategories();
	}
	
	public void updateLSParams(String yKey) {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		CatVariable xVar = (CatVariable)data.getVariable(xKey[0]);
		int noOfXCats = xVar.noOfCategories();
		if (mean == null || noOfXCats != mean.length)
			mean = new NumValue[noOfXCats];
		if (sd == null || noOfXCats != sd.length)
			sd = new NumValue[noOfXCats];
		if (df == null || noOfXCats != df.length)
			df = new int[noOfXCats];
		int[] n = new int[noOfXCats];
		double[] sy = new double[noOfXCats];
		double[] syy = new double[noOfXCats];
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			int group = xVar.labelIndex(xe.nextValue());
			if (!Double.isNaN(y)) {
				sy[group] += y;
				syy[group] += y * y;
				n[group] ++;
			}
		}
		
		double residSsq = 0.0;
		pooledDf = 0;
		for (int i=0 ; i<noOfXCats ; i++) {
			if (n[i] > 1) {
				double ss = syy[i] - sy[i] * sy[i] / n[i];
				residSsq += ss;
				df[i] = n[i] - 1;
				pooledDf += df[i];
				sd[i] = new NumValue(Math.sqrt(ss / df[i]));
			}
			else {
				df[i] = 0;
				sd[i] = null;
			}
			mean[i] = new NumValue(sy[i] / n[i]);
		}
		pooledSd = new NumValue(Math.sqrt(residSsq / pooledDf));
	}
	
	public void drawMean(Graphics g, DataView view, NumCatAxis xAxis, NumCatAxis yAxis) {
		int noOfCategories = mean.length;
		int xSpacing = (xAxis == null) ? view.getSize().width / 2
																		: xAxis.catValToPosition(1) - xAxis.catValToPosition(0);
		int offset = xSpacing / 2 - kHalfMeanGap;
		Point thePoint = null;
		for (int i=0 ; i<noOfCategories ; i++)
			try {
				int yPos = yAxis.numValToPosition(mean[i].toDouble());
				int xCenter = (xAxis == null) ? view.getSize().width / 2 : xAxis.catValToPosition(i);
				thePoint = view.translateToScreen(xCenter - offset, yPos, thePoint);
				g.drawLine(thePoint.x, thePoint.y, thePoint.x + 2 * offset, thePoint.y);
			} catch (AxisException e) {
			}
	}
	
	public void drawModel(Graphics g, DataView view, NumCatAxis xAxis,
											NumCatAxis yAxis, Color fillColor, Color meanColor) {
		int noOfCategories = mean.length;
		int xSpacing = (xAxis == null) ? view.getSize().width / 2
																		: xAxis.catValToPosition(1) - xAxis.catValToPosition(0);
		int offset = xSpacing / 4;
		Point topLeftPoint = null;
		Point bottomRightPoint = null;
		Point meanPoint = null;
		for (int i=0 ; i<noOfCategories ; i++)
			try {
				double yOffset = 2.0 * sd[i].toDouble();
		
				double m = mean[i].toDouble();
				int yMeanPos = yAxis.numValToPosition(m);
				int yHighPos = yAxis.numValToPosition(m + yOffset);
				int yLowPos = yAxis.numValToPosition(m - yOffset);
				int xCenter = (xAxis == null) ? view.getSize().width / 2 : xAxis.catValToPosition(i);
				
				topLeftPoint = view.translateToScreen(xCenter - offset, yHighPos, topLeftPoint);
				bottomRightPoint = view.translateToScreen(xCenter + offset, yLowPos, bottomRightPoint);
				meanPoint = view.translateToScreen(xCenter - offset, yMeanPos, meanPoint);
				
				g.setColor(fillColor);
				g.fillRect(topLeftPoint.x, topLeftPoint.y, bottomRightPoint.x - topLeftPoint.x,
																		bottomRightPoint.y - topLeftPoint.y);
				
				g.setColor(meanColor);
				g.drawLine(meanPoint.x, meanPoint.y, meanPoint.x + 2 * offset, meanPoint.y);
			} catch (AxisException e) {
			}
	}
}
