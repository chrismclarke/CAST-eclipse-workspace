package exper2;

import dataView.*;
import models.*;


public class AdjustedMeanView extends RawMeanView {
	private String blockKey;
	private int blockIndex;
	
	public AdjustedMeanView(DataSet theData, XApplet applet, String xKey, int xIndex,
														String blockKey, int blockIndex, String lsKey, NumValue maxVal) {
		super(theData, applet, xKey, xIndex, lsKey, maxVal);
		this.blockKey = blockKey;
		this.blockIndex = blockIndex;
	}
	
	public void setBlockIndex(int blockIndex) {
		this.blockIndex = blockIndex;
		repaint();
	}

//--------------------------------------------------------------------------------
	
	protected String getValueString() {
		MultipleRegnModel ls = (MultipleRegnModel)getVariable(lsKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int nx = xVar.noOfCategories();
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int nBlocks = blockVar.noOfCategories();
		
		double x[] = new double[nx + nBlocks - 2];
		if (blockIndex > 0)
			x[blockIndex - 1] = 1;
		else if (blockIndex < 0) {
			double average = 1.0 / nBlocks;
			for (int i=1 ; i<nBlocks ; i++)
				x[i - 1] = average;
		}
		
		if (xIndex > 0)
			x[nBlocks + xIndex - 2] = 1;
		
		double mean = ls.evaluateMean(x);
		tempVal.setValue(mean);
		return tempVal.toString();
	}
}
