package exper2;

import java.awt.*;

import dataView.*;
import models.*;

import indicator.*;


public class FactorEstimatesView extends DataView {
	
	static final private int kColumnGap = 30;
//	static final private int kRowGap = 20;
//	static final private int kMaxRowGap = 40;
	
	private String xKey;
	private String modelKey;
	
	private NumValue maxParam;
	private TermRecord paramTerm[];
	private Dimension termSize[];
	
	private int baselineIndex = -1;		//	no baseline
	
	private boolean initialised = false;
	
	public FactorEstimatesView(DataSet theData, XApplet applet, String xKey, String modelKey,
																																				NumValue maxParam) {
		super(theData, applet, new Insets(0, 10, 0, 10));
		
		this.xKey = xKey;
		this.modelKey = modelKey;
		this.maxParam = maxParam;
		
		paramTerm = new TermRecord[2];
		paramTerm[0] = new TermRecord("", maxParam, null, applet);
		paramTerm[1] = new TermRecord((Variable)theData.getVariable(xKey), maxParam, null);
	}
	
	public void setBaselineIndex(int baselineIndex) {
		this.baselineIndex = baselineIndex;
		CatVariable xVar = (CatVariable)getVariable(xKey);
		String baselineString = (baselineIndex < 0) ? "" : "(" + xVar.getLabel(baselineIndex) + ")";
		paramTerm[0].setBaselineHeading(baselineString);
		repaint();
	}
	
	private void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		termSize = new Dimension[paramTerm.length];
		for (int i=0 ; i<paramTerm.length ; i++)
			termSize[i] = paramTerm[i].getMinimumSize(g);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		
		Dimension minSize = getMinimumSize();
		int colExtra = (getSize().width - minSize.width) / termSize.length;
		int columnGap = kColumnGap + colExtra;
		
		int left = colExtra / 2;
		
		double intercept = model.getParameter(0).toDouble();
		NumValue interceptVal = null;
		
		CatVariable xCat = (CatVariable)getVariable(xKey);
		int nCats = xCat.noOfCategories();
		
		double effect[] = new double[nCats];
		for (int i=1 ; i<nCats ; i++)
			effect[i] = model.getParameter(i).toDouble();
		
		if (baselineIndex < 0)
			for (int i=0 ; i<nCats ; i++)
				effect[i] += intercept;
		else {
			if (baselineIndex > 0)
				for (int i=0 ; i<nCats ; i++)
					if (i != baselineIndex)
						effect[i] -= effect[baselineIndex];
			interceptVal = new NumValue(intercept + effect[baselineIndex], maxParam.decimals);
		}
		
		NumValue effectVal[] = new NumValue[nCats];
		for (int i=0 ; i<nCats ; i++)
			if (i != baselineIndex)
				effectVal[i] = new NumValue(effect[i], maxParam.decimals);
		
		for (int i=0 ; i<termSize.length ; i++) {
			int colWidth = termSize[i].width;
			int termLeft = left + (colWidth - termSize[i].width) / 2;
			if (i == 0)
				paramTerm[0].drawParameters(g, termLeft, 0, interceptVal, null, null,
																																	null, Color.black);
			else
				paramTerm[i].drawParameters(g, termLeft, 0, null, null, effectVal,
																																	null, Color.black);
				
			left += colWidth + columnGap;
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int width = 0;
		int height = 0;
		
		for (int i=0 ; i<termSize.length ; i++) {
			width += termSize[i].width;
			if (i > 0)
				width += kColumnGap;
			height = Math.max(height, termSize[i].height);
		}
		
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
	
