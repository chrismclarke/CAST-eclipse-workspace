package indicator;

import java.awt.*;

import dataView.*;

import twoFactor.*;


public class FactorEstimatesView extends DataView {
//	static public final String FACTOR_ESTIMATES = "factorEstimates";
	
	static final private int kColumnGap = 10;
	static final private Color kBaselineColor = Color.blue;
	static final private Color kXParamColor = Color.red;
	static final private Color kZParamColor = new Color(0x009900);
	
	private String xKey, zKey, modelKey;
	private NumValue maxParam[];
	
	private TermRecord baselineTerm, xCatTerm, zCatTerm;
	
	private Dimension baselineSize, xParamSize, zParamSize;
	
	public FactorEstimatesView(DataSet theData, XApplet applet,
						String xKey, String zKey, String modelKey, NumValue[] maxParam) {
		super(theData, applet, new Insets(0, 10, 0, 10));
		
		this.xKey = xKey;
		this.zKey = zKey;
		this.modelKey = modelKey;
		this.maxParam = maxParam;
		
		CatVariable xVar = (CatVariable)theData.getVariable(xKey);
		CatVariable zVar = (CatVariable)theData.getVariable(zKey);
		String baselineCats = "(" + xVar.getLabel(0).toString() + ", "
																										+ zVar.getLabel(0).toString() + ")";
		baselineTerm = new TermRecord(baselineCats, maxParam[0], null, applet);
		xCatTerm = new TermRecord(xVar, maxParam[1], null);
		zCatTerm = new TermRecord(zVar, maxParam[2], null);
	}
	
	public void paintView(Graphics g) {
		TwoFactorModel model = (TwoFactorModel)getVariable(modelKey);
		double baselineMean = model.evaluateMean(0, 0);
		NumValue baselineParam = new NumValue(baselineMean, maxParam[0].decimals);
		int left = 0;
		baselineTerm.drawParameters(g, left, 0, baselineParam, null, kBaselineColor);
		left += baselineSize.width + kColumnGap;
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		NumValue params[] = new NumValue[xVar.noOfCategories()];
//		params[0] = new NumValue(0.0, 0);
		for (int i=1 ; i<params.length ; i++)
			params[i] = new NumValue(model.evaluateMean(i, 0) - baselineMean, maxParam[1].decimals);
		xCatTerm.drawParameters(g, left, 0, null, params, kXParamColor);
		left += xParamSize.width + kColumnGap;
		
		CatVariable zVar = (CatVariable)getVariable(zKey);
		params = new NumValue[zVar.noOfCategories()];
//		params[0] = new NumValue(0.0, 0);
		for (int j=1 ; j<params.length ; j++)
			params[j] = new NumValue(model.evaluateMean(0, j) - baselineMean, maxParam[2].decimals);
		zCatTerm.drawParameters(g, left, 0, null, params, kZParamColor);
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
		Graphics g = getGraphics();
		baselineSize = baselineTerm.getMinimumSize(g);
		xParamSize = xCatTerm.getMinimumSize(g);
		zParamSize = zCatTerm.getMinimumSize(g);
		
		return new Dimension(kColumnGap * 2 + baselineSize.width + xParamSize.width + zParamSize.width + 1,
										Math.max(baselineSize.height, Math.max(xParamSize.height, zParamSize.height)) + 1);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
	
