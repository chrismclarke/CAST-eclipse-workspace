package exper2;

import dataView.*;
import axis.*;
import models.*;


public class AdjustedOneFactorView extends CoreOneFactorView {
	
	protected String blockKey;
	protected int adjustBlock;
	
//	private boolean showResiduals = true;
	
	public AdjustedOneFactorView(DataSet theData, XApplet applet, NumCatAxis xAxis,
											NumCatAxis yAxis, String xKey, String yKey, 
											String blockKey, int adjustBlock, String modelKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, modelKey);
		this.blockKey = blockKey;
		this.yAxis = yAxis;
	}
	
	public void setBlockIndex(int adjustBlock) {
		this.adjustBlock = adjustBlock;
		repaint();
	}
	
	protected double evaluateModelMean(CoreModelVariable model, int xIndex, CatVariable xVar) {
		int nx = xVar.noOfCategories();
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int nBlocks = blockVar.noOfCategories();
		
		double x[] = new double[nx + nBlocks - 2];
		if (adjustBlock > 0)
			x[adjustBlock - 1] = 1;
		else if (adjustBlock < 0) {
			double average = 1.0 / nBlocks;
			for (int i=1 ; i<nBlocks ; i++)
				x[i - 1] = average;
		}
		
		if (xIndex > 0)
			x[nBlocks + xIndex - 2] = 1;
			
		return ((MultipleRegnModel)model).evaluateMean(x);
	}
}
	
