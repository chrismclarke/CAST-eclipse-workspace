package curveInteract;

import java.awt.*;

import dataView.*;
import distn.*;
import models.*;


public class ParamTestsView extends DataView {
//	static final public String PARAM_TEST_VIEW = "paramTestView";
	
	static final private int kTopBottomBorder = 2;
	static final private int kLineSpace = 3;
	static final private int kLineGap = 2 * kLineSpace + 1;
	static final private int kHeadingGap = kLineGap + 4;
	
	static final private int kLeftRightBorder = 2;
	static final private int kMaxLeftRightBorder = 8;
	static final private int kColumnGap = 25;
	static final private int kMaxColGap = 30;
	
	static final private Value kTLabel = new LabelValue("t");
	
	static final private Color kConstrainedBackColor = new Color(0xDDDDDD);
	static final private Color kConstrainedForeColor = new Color(0xAAAAAA);
	static final private Color kPValToAddColor = new Color(0x990000);				//	dark red
	
	static final private String kPlusMinus = "\u00B1";
	
	private Value kPValueLabel;
	
	protected String modelKey, yKey;
	protected String paramName[];
	private NumValue maxParam, maxSE, maxT, maxVIF, maxType3Ssq;
	private Color paramColor[];
	
	private NumValue maxPValue = new NumValue(0.0, 4);
	
	private boolean initialised = false;
	
	private int ascent, descent, tableHeight, tableWidth;
	private int paramNameWidth, paramWidth, seWidth, tWidth, pValueWidth, type3SsqWidth,
																																								vifWidth;
	protected int tableLeft;		//	set by paintView() and needed by ParamTestsRemoveView.getInitialPosition()
	
	private int hiliteIndex = -1;
	private Color hiliteColor;
	
	private boolean drawSE = true;			//	always leaves space for se, even if not displayed
	private boolean showT = true;
	private boolean showPValue = true;
	private boolean displayPValueToAdd = false;
	private boolean showVIF = false;
	private boolean showType3Ssq = false;
	
	protected double constraints[] = null;
													//	allows some coefficients to be constrained to zero
	@SuppressWarnings("unused")
	private boolean canOnlyDeleteOne = false;
	
	public ParamTestsView(DataSet theData, XApplet applet, String modelKey, String yKey,
									String paramName[], Color paramColor[], NumValue maxParam, NumValue maxSE, NumValue maxT) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		kPValueLabel = new LabelValue(applet.translate("p-value"));
		this.modelKey = modelKey;
		this.yKey = yKey;
		this.paramName = paramName;
		this.paramColor = paramColor;		//	can be null (for black)
		this.maxParam = maxParam;
		this.maxSE = maxSE;
		this.maxT = maxT;
		
		setDefaultConstraints();
	}
	
	public void setCanOnlyDeleteOne(boolean canOnlyDeleteOne) {
		this.canOnlyDeleteOne = canOnlyDeleteOne;
	}
	
	private void setDefaultConstraints() {
		constraints = new double[paramName.length];
		for (int i=0 ; i<constraints.length ; i++)
			constraints[i] = (paramName[i] == null) ? 0.0 : Double.NaN;
	}
	
	public void setConstraint(int paramIndex, boolean constrainedZero) {
		constraints[paramIndex] = constrainedZero ? 0.0 : Double.NaN;
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		model.updateLSParams(yKey, constraints);
	}
	
	protected boolean isConstrained(int paramIndex) {
		return (constraints != null) && !Double.isNaN(constraints[paramIndex]);
	}
	
	public void setDrawSE(boolean drawSE) {
		this.drawSE = drawSE;
	}
	
	public void setShowT(boolean showT) {
		this.showT = showT;
	}
	
	public void setShowPValue(boolean showPValue) {
		this.showPValue = showPValue;
	}
	
	public void setShowType3Ssq(boolean showType3Ssq, NumValue maxType3Ssq) {
		this.showType3Ssq = showType3Ssq;
		this.maxType3Ssq = maxType3Ssq;
	}
	
	public void setShowVIF(boolean showVIF, NumValue maxVIF) {
		this.showVIF = showVIF;
		this.maxVIF = maxVIF;
	}
	
	public void setDisplayPValueToAdd(boolean displayPValueToAdd) {
		this.displayPValueToAdd = displayPValueToAdd;
	}
	
	public void setHilite(int hiliteIndex, Color hiliteColor) {
		this.hiliteIndex = hiliteIndex;
		this.hiliteColor = hiliteColor;
	}
	
	public void setMaxParams(NumValue maxParam, NumValue maxSE) {
		this.maxParam = maxParam;
		this.maxSE = maxSE;
		initialised = false;
		invalidate();
	}

//-----------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			int nParam = 0;
			paramNameWidth = 0;
			for (int i=0 ; i<paramName.length ; i++)		//	null name if always constrained zero
				if (paramName[i] != null) {
					paramNameWidth = Math.max(paramNameWidth, fm.stringWidth(paramName[i]));
					nParam ++;
				}
			tableHeight = (ascent + descent) * (nParam + 1) + kLineGap * (nParam - 1)
																								+ 2 * kTopBottomBorder + kHeadingGap;
			
			int nCols = 3;
			LabelValue paramLabel = new LabelValue(getApplet().translate("param"));
			paramWidth = Math.max(paramLabel.stringWidth(g), maxParam.stringWidth(g));
			Value seLabel = new LabelValue(getApplet().translate("se"));
			seWidth = Math.max(seLabel.stringWidth(g), maxSE.stringWidth(g));
			
			if (showT) {
				tWidth = Math.max(kTLabel.stringWidth(g), maxT.stringWidth(g));
				nCols ++;
			}
			
			if (showPValue) {
				pValueWidth = Math.max(kPValueLabel.stringWidth(g), maxPValue.stringWidth(g));
				nCols ++;
			}
			
			if (showType3Ssq) {
				Value type3SsqLabel = new LabelValue(getApplet().translate("Type 3 ssq"));
				type3SsqWidth = Math.max(type3SsqLabel.stringWidth(g), maxType3Ssq.stringWidth(g));
				nCols ++;
			}
			
			if (showVIF) {
				Value vifLabel = new LabelValue(getApplet().translate("VIF"));
				vifWidth = Math.max(vifLabel.stringWidth(g), maxVIF.stringWidth(g));
				nCols ++;
			}
			
			tableWidth = paramNameWidth + paramWidth + seWidth + tWidth + pValueWidth + type3SsqWidth
													+ vifWidth + 2 * kLeftRightBorder + (nCols - 1) * kColumnGap;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected int leftCheckBorder() {			//	space for checkboxes to remove variables
		return 0;
	}
	
	protected final int getParamBaseline(int row) {
		int lineGap = kLineGap + Math.min(0, (getSize().height - tableHeight) / (paramName.length - 1));
		return kTopBottomBorder + ascent + kHeadingGap + ascent + descent
																									+ row * (ascent + descent + lineGap);
	}
	
	protected void drawLeftChecks(Graphics g) {
	}
	
	protected boolean meaningfulTest(int paramIndex) {
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int nParam = 0;
			for (int i=0 ; i<paramName.length ; i++)		//	null name if always constrained zero
					nParam ++;
		
		int headingBaseline = kTopBottomBorder + ascent;
		int paramBaseline = getParamBaseline(0);
		
		int nCols = 3;
		if (showT)
			nCols ++;
		if (showPValue)
			nCols ++;
		if (showType3Ssq)
			nCols ++;
		if (showVIF)
			nCols ++;
		
		int extraGap = (getSize().width - tableWidth - leftCheckBorder()) / (nCols - 1);
		int colGap = Math.min(kMaxColGap, kColumnGap + extraGap);
		int leftRightBorder = Math.max(kLeftRightBorder,
													Math.min(kMaxLeftRightBorder, kLeftRightBorder + extraGap));
		
		int localTableWidth = tableWidth + (nCols - 1) * (colGap - kColumnGap)
																								+ 2 * (leftRightBorder - kLeftRightBorder);
		tableLeft = (getSize().width - localTableWidth - leftCheckBorder()) / 2 + leftCheckBorder();
		
		int paramNameLeft = tableLeft + leftRightBorder;
		int paramRight = paramNameLeft + paramNameWidth + colGap + paramWidth;
		int seRight = paramRight + colGap + seWidth;
		int tRight = seRight + (showT ? colGap + tWidth : 0);
		int pValueLeft = tRight + colGap;
		int type3SsqRight = pValueLeft + pValueWidth + colGap + type3SsqWidth;
		int vifRight = type3SsqRight + colGap + vifWidth;
		
		int lineGap = kLineGap + Math.min(0, (getSize().height - tableHeight) / (nParam - 1));
		
		g.setColor(Color.white);
		int innerTop = headingBaseline + descent + kHeadingGap / 2;
		int innerHeight = tableHeight - innerTop;
		g.fillRect(tableLeft, innerTop, localTableWidth, tableHeight - innerTop);
		
		g.setColor(Color.black);
		g.drawLine(tableLeft, innerTop, tableLeft + localTableWidth - 1, innerTop);
		g.drawLine(tableLeft, innerTop + innerHeight - 1, tableLeft + localTableWidth - 1,
																														innerTop + innerHeight - 1);
		LabelValue paramLabel = new LabelValue(getApplet().translate("param"));
		paramLabel.drawCentred(g, paramRight - paramWidth / 2, headingBaseline);
		if (drawSE) {
			Value seLabel = new LabelValue(getApplet().translate("se"));
			seLabel.drawCentred(g, seRight - seWidth / 2, headingBaseline);
		}
		if (showT)
			kTLabel.drawCentred(g, tRight - tWidth / 2, headingBaseline);
		if (showPValue)
			kPValueLabel.drawCentred(g, pValueLeft + pValueWidth / 2, headingBaseline);
		if (showType3Ssq) {
			Value type3SsqLabel = new LabelValue(getApplet().translate("Type 3 ssq"));
			type3SsqLabel.drawCentred(g, type3SsqRight - type3SsqWidth / 2, headingBaseline);
		}
		if (showVIF) {
			Value vifLabel = new LabelValue(getApplet().translate("VIF"));
			vifLabel.drawCentred(g, vifRight - vifWidth / 2, headingBaseline);
		}
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		double[] paramVar = model.getCoeffVariances(yKey, constraints);
		
		SSComponent residSsqComponent = model.getResidSsqComponent(yKey, constraints);
		double residSsq = residSsqComponent.ssq;
		int residDf = residSsqComponent.df;
		
//		int nObs = ((NumVariable)getVariable(yKey)).noOfValues();
//		int residDf = nObs - model.getNoOfParameters();
//		if (constraints != null)
//			for (int i=0 ; i<constraints.length ; i++)
//				if (!Double.isNaN(constraints[i]))
//					residDf ++;
		
		double[] scaledParamVar = null;
		if (showVIF)
			scaledParamVar = model.getCoeffVariances(yKey, constraints, true, 1.0);
		
		for (int i=0 ; i<paramName.length ; i++)
			if (paramName[i] != null) {
				boolean constrainedToZero = constraints != null && !Double.isNaN(constraints[i]);
				if (constrainedToZero) {
					g.setColor(kConstrainedBackColor);
					g.fillRect(tableLeft, paramBaseline - ascent - kLineSpace, localTableWidth,
																															ascent + descent + lineGap);
				}
				else if (hiliteIndex >= 0) {
					g.setColor(hiliteColor);
					g.fillRect(tableLeft + 1, paramBaseline - ascent - kLineSpace, localTableWidth - 2,
																																	ascent + descent + lineGap);
				}
				
				if (constrainedToZero)
					g.setColor(kConstrainedForeColor);
				else if (paramColor == null || paramColor[i] == null)
					g.setColor(Color.black);
				else
					g.setColor(paramColor[i]);
				
				g.drawString(paramName[i], paramNameLeft, paramBaseline);
				
					NumValue paramVal = model.getParameter(i);
				paramVal.drawLeft(g, paramRight, paramBaseline);
				
				NumValue seVal = new NumValue(Math.sqrt(paramVar[(i + 1) * (i + 2) / 2 - 1]), paramVal.decimals);
				if (drawSE)
					seVal.drawLeft(g, seRight, paramBaseline);
				
				boolean inModelOrDisplay = !constrainedToZero || !displayPValueToAdd;
				NumValue tVal = new NumValue(paramVal.toDouble() / seVal.toDouble(), maxT.decimals);
				if (showT && inModelOrDisplay)
					tVal.drawLeft(g, tRight, paramBaseline);
					
				if (showPValue && inModelOrDisplay) {
					double pValue = TTable.cumulative(tVal.toDouble(), residDf);
					pValue = 2.0 * Math.min(pValue, 1.0 - pValue);
					NumValue pValueVal = new NumValue(pValue, maxPValue.decimals);
					if (meaningfulTest(i))
						pValueVal.drawRight(g, pValueLeft, paramBaseline);
					else {
						Color oldColor = g.getColor();
						g.setColor(kConstrainedForeColor);
						pValueVal.drawRight(g, pValueLeft, paramBaseline);
						g.setColor(oldColor);
					}
				}
				
				if (showType3Ssq && i > 0 && inModelOrDisplay) {
					double type3Ssq = tVal.toDouble() * tVal.toDouble() * residSsq / residDf;
					NumValue ssqValue = new NumValue(type3Ssq, maxType3Ssq.decimals);
					ssqValue.drawLeft(g, type3SsqRight, paramBaseline);
				}
				
				if (showVIF && i > 0) {
					double vif = constrainedToZero ? Double.NaN : model.getVIF(i, scaledParamVar);
					NumValue vifVal = new NumValue(vif, maxVIF.decimals);
					vifVal.drawLeft(g, vifRight, paramBaseline);
				}
				
				paramBaseline += (ascent + descent + lineGap);
			}

		if (displayPValueToAdd && constraints != null) {
			g.setColor(kPValToAddColor);
			paramBaseline = getParamBaseline(1);
			
			for (int i=1 ; i<paramName.length ; i++)
				if (paramName[i] != null) {
					if (!Double.isNaN(constraints[i])) {
						constraints[i] = Double.NaN;
						
						SSComponent withVarComponent = model.getResidSsqComponent(yKey, constraints);
						double residSsqWithVar = withVarComponent.ssq;
						int residDfWithVar = withVarComponent.df;
						
						double explainedSsq = residSsq - residSsqWithVar;
						int explainedDf = residDf - residDfWithVar;		//	should be 1
						
						double f = explainedSsq / explainedDf / (residSsqWithVar / residDfWithVar);
						
						if (showT) {
							double t = Math.sqrt(f);
							String tString = kPlusMinus + new NumValue(t, maxT.decimals).toString();
							int tLeft = tRight - g.getFontMetrics().stringWidth(tString);
							g.drawString(tString, tLeft, paramBaseline);
						}
						
						if (showPValue) {
							double pValue = 1.0 - FTable.cumulative(f, explainedDf, residDfWithVar);
							NumValue pValueVal = new NumValue(pValue, maxPValue.decimals);
							
							pValueVal.drawRight(g, pValueLeft, paramBaseline);
						}
						
						if (showType3Ssq) {
							NumValue ssqValue = new NumValue(explainedSsq, maxType3Ssq.decimals);
							ssqValue.drawLeft(g, type3SsqRight, paramBaseline);
						}
						
						constraints[i] = 0.0;
					}
					paramBaseline += (ascent + descent + lineGap);
			}
		}
		
		g.setColor(Color.black);
		g.drawLine(tableLeft, innerTop, tableLeft + localTableWidth - 1, innerTop);
		g.drawLine(tableLeft, innerTop + innerHeight - 1, tableLeft + localTableWidth - 1,
																														innerTop + innerHeight - 1);
		
		drawLeftChecks(g);
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
		return new Dimension(tableWidth + leftCheckBorder(), tableHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}