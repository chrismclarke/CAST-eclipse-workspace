package indicator;

import java.awt.*;

import dataView.*;
import models.*;
import distn.*;

//import twoFactor.*;


public class TermEstimatesView extends DataView {
//	static public final String TERM_ESTIMATES = "termEstimates";
	
	static final private int kColumnGap = 30;
	static final private int kRowGap = 20;
	static final private int kMaxRowGap = 40;
	
	private String xKey[];
	private String modelKey;
	
//	private NumValue maxParam[];
	private NumValue maxPlusMinus[];
	private TermRecord paramTerm[];
	private Dimension termSize[];
	
	private int termsPerColumn[];
	
	private Type3SsqTableView linkedTable;
	
	private boolean initialised = false;
	
	public TermEstimatesView(DataSet theData, XApplet applet, String[] xKey, String modelKey,
														NumValue[] maxParam, NumValue[] maxPlusMinus, int[] termsPerColumn) {
		super(theData, applet, new Insets(0, 10, 0, 10));
		
		this.xKey = xKey;
		this.modelKey = modelKey;
//		this.maxParam = maxParam;
		this.maxPlusMinus = maxPlusMinus;
		this.termsPerColumn = termsPerColumn;
		
		paramTerm = new TermRecord[xKey.length + 1];
		paramTerm[0] = new TermRecord((String)null, maxParam[0],
																(maxPlusMinus == null) ? null : maxPlusMinus[0], applet);
		for (int i=0 ; i<xKey.length ; i++) {
			paramTerm[i + 1] = new TermRecord((Variable)theData.getVariable(xKey[i]),
													maxParam[i + 1], (maxPlusMinus == null) ? null : maxPlusMinus[i + 1]);
		}
	}
	
	public void setLinkedSsqTable(Type3SsqTableView linkedTable) {
		this.linkedTable = linkedTable;
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
		
		double bVar[] = null;
		int residDf = 0;
		double t95 = 0.0;
		if (maxPlusMinus != null) {
			if (linkedTable == null) {
				bVar = model.getCoeffVariances("y");
				residDf = model.getResidSsqComponent("y", null).df;
			}
			else {
				bVar = model.getCoeffVariances("y", linkedTable.expandConstraints());
				residDf = model.getResidSsqComponent("y", linkedTable.expandConstraints()).df;
			}
			t95 = TTable.quantile(0.975, residDf);
		}
		
		boolean termInModel[];
		if (linkedTable == null) {
			termInModel = new boolean[xKey.length];
			for (int i=0 ; i<termInModel.length ; i++)
				termInModel[i] = true;
		}
		else
			termInModel = linkedTable.getTermsInModel();
		
		Dimension minSize = getMinimumSize();
		int colExtra = (getSize().width - minSize.width) / termsPerColumn.length;
		int columnGap = kColumnGap + colExtra;
		
		int left = colExtra / 2;
		int termIndex = 0;
		int paramIndex = 0;
		
		for (int col=0 ; col<termsPerColumn.length ; col++) {
			int colWidth = 0;
			for (int i=0 ; i<termsPerColumn[col] ; i++) 
				colWidth = Math.max(colWidth, termSize[termIndex + i].width);
			
			int rowExtra = (getSize().height - minSize.height) / termsPerColumn[col];
			int rowGap = Math.min(kRowGap + rowExtra, kMaxRowGap);
				
			int top = 0;
			for (int i=0 ; i<termsPerColumn[col] ; i++) {
				int termLeft = left + (colWidth - termSize[termIndex].width) / 2;
				if (termIndex == 0) {
					NumValue intercept = model.getParameter(paramIndex ++);
					NumValue plusMinus = (bVar == null) ? null : new NumValue(t95 * Math.sqrt(bVar[0]),
																																			maxPlusMinus[0].decimals);
					paramTerm[termIndex].drawParameters(g, termLeft, top, intercept, plusMinus,
																																		null, null, Color.black);
				}
				else {
					CoreVariable xVar = getVariable(xKey[termIndex - 1]);
					boolean hasTerm = termInModel[termIndex - 1];
					if (xVar instanceof CatVariable) {
						CatVariable xCat = (CatVariable)xVar;
						int nCats = xCat.noOfCategories();
						NumValue effect[] = new NumValue[nCats];
						NumValue plusMinus[] = new NumValue[nCats];
						effect[0] = null;
						plusMinus[0] = null;
						for (int j=1 ; j<nCats ; j++) {
							effect[j] = hasTerm ? model.getParameter(paramIndex) : null;
							plusMinus[j] = (bVar == null || !hasTerm) ? null
													: new NumValue(t95 * Math.sqrt(bVar[(paramIndex + 1) * (paramIndex + 2) / 2 - 1]),
																					maxPlusMinus[termIndex].decimals);
							paramIndex ++;
						}
						paramTerm[termIndex].drawParameters(g, termLeft, top, null, null, effect,
																																					plusMinus, Color.black);
					}
					else {
						NumValue slope = hasTerm ? model.getParameter(paramIndex) : null;
						NumValue plusMinus = (bVar == null || !hasTerm) ? null
													: new NumValue(t95 * Math.sqrt(bVar[(paramIndex + 1) * (paramIndex + 2) / 2 - 1]),
																				maxPlusMinus[termIndex].decimals);
						paramIndex ++;
						paramTerm[termIndex].drawParameters(g, termLeft, top, slope, plusMinus, null,
																																						null, Color.black);
					}
				}
				
				top += termSize[termIndex].height + rowGap;
				termIndex ++;
			}
			
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
		int termIndex = 0;
		
		for (int col=0 ; col<termsPerColumn.length ; col++) {
			int colWidth = 0;
			int colHeight = 0;
			for (int i=0 ; i<termsPerColumn[col] ; i++) {
				colHeight += termSize[termIndex].height + kRowGap;
				colWidth = Math.max(colWidth, termSize[termIndex].width);
				termIndex ++;
			}
			
			width += colWidth + kColumnGap;
			height = Math.max(height, colHeight);
		}
		
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
	
