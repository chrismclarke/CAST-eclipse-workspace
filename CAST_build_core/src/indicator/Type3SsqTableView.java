package indicator;

import java.awt.*;

import dataView.*;
import distn.*;
import models.*;


public class Type3SsqTableView extends DataView {
//	static final public String TYPE3_SSQ_VIEW = "type3Ssq";
								//		much of code copied from curveInteract.ParamTestView and ParamTestsRemoveView
	
	static final protected int kTopBottomBorder = 2;
	static final protected int kLineSpace = 3;
	static final protected int kLineGap = 2 * kLineSpace + 1;
	static final private int kHeadingGap = kLineGap + 4;
	
	static final private int kLeftRightBorder = 2;
	static final private int kMaxLeftRightBorder = 8;
	static final private int kColumnGap = 25;
	static final private int kMaxColGap = 30;
	
	static final private int kBoxLeftRight = 5;
	static final private int kBoxSize = 12;
	
	static final private Value kFLabel = new LabelValue("F");
	
	static final private Color kConstrainedBackColor = new Color(0xE0E0E0);
	static final private Color kConstrainedForeColor = new Color(0x999999);
	static final protected Color kResidColor = new Color(0x000099);
	
	static final private Color kLightGray = new Color(0x999999);
	static final private Color kDimCheckColor = new Color(0x666666);
	
	private Value kPValueLabel, kDfLabel, kMeanSsqLabel;
	
	private NumValue kMaxPValue = new NumValue(0.0, 4);
	
	protected String modelKey, yKey;
	private String xKey[];
	private String termName[];
	
	private NumValue maxDf, maxType3Ssq, maxMeanSsq, maxF;
	
	private int hierarchy[][];		//	must be ordered with interactions after main effects
																//	intercept is not included, so first var is indexed by 0
	
	private boolean enabledCheck[] = null;
	
	private boolean initialised = false;
	
	protected int ascent, descent, tableHeight, tableWidth, tableBottomHeight;
	private int paramNameWidth, type3SsqWidth, dfWidth, meanSsqWidth, fWidth, pValueWidth;
	private int tableLeft;		//	set by paintView() and needed by getInitialPosition()
	
	private boolean termInModel[] = null;
	private int coeffPerTerm[] = null;
	private int totalCoeffs;
	
	private int selectedIndex = -1;
	private boolean doingDrag = false;
	
	public Type3SsqTableView(DataSet theData, XApplet applet,
							String modelKey, String yKey, NumValue maxType3Ssq, NumValue maxDf,
							NumValue maxMeanSsq, NumValue maxF, String[] termName, int[] nxForTerm,
							int[][] hierarchy) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		kPValueLabel = new LabelValue(applet.translate("p-value"));
		kDfLabel = new LabelValue(applet.translate("df"));
		kMeanSsqLabel = new LabelValue(applet.translate("Mean ssq"));
		
		this.modelKey = modelKey;
		this.yKey = yKey;
		xKey = ((MultipleRegnModel)getVariable(modelKey)).getXKey();
		
		this.maxType3Ssq = maxType3Ssq;
		this.maxDf = maxDf;
		this.maxMeanSsq = maxMeanSsq;
		this.maxF = maxF;
		
		setupTerms(termName, nxForTerm);
		
		this.hierarchy = hierarchy;
		doEnabling();
	}
	
	public Type3SsqTableView(DataSet theData, XApplet applet,
							String modelKey, String yKey, NumValue maxType3Ssq, NumValue maxDf,
							NumValue maxMeanSsq, NumValue maxF) {
		this(theData, applet, modelKey, yKey, maxType3Ssq, maxDf, maxMeanSsq, maxF, null, null, null);
	}
	
//-------------------------------------------------
	
	public boolean[] getTermsInModel() {
		return termInModel;
	}
	
	private void setupTerms(String[] inputTermName, int[] nxForTerm) {
		if (inputTermName == null)
			termName = new String[xKey.length];
		else
			termName = inputTermName;
		int nTerms = termName.length;
		if (nxForTerm == null)
			nxForTerm = new int[nTerms];
			
		termInModel = new boolean[nTerms];
		coeffPerTerm = new int[nTerms];
		totalCoeffs = 1;		//	for intercept
		int xIndex = 0;
		for (int i=0 ; i<nTerms ; i++) {
			termInModel[i] = true;
			if (termName[i] == null) {
				termName[i] = getVariable(xKey[xIndex]).name;
				nxForTerm[i] = 1;
			}
			for (int j=0 ; j<nxForTerm[i] ; j++) {
				CoreVariable xVar = getVariable(xKey[xIndex ++]);
				if (xVar instanceof CatVariable)
					coeffPerTerm[i] += ((CatVariable)xVar).noOfCategories() - 1;
				else
					coeffPerTerm[i] ++;
			}
			totalCoeffs += coeffPerTerm[i];
		}
	}
	
/*
	private int noOfTerms() {
		return termName.length;
	}
*/
	
//-------------------------------------------------
	
	public double[] expandConstraints() {
		double constraints[] = new double[totalCoeffs];
		constraints[0] = Double.NaN;
		int index = 1;
		for (int i=0 ; i<termInModel.length ; i++) {
			if (termInModel[i])
				for (int j=0 ; j<coeffPerTerm[i] ; j++)
					constraints[index ++] = Double.NaN;
			else
				index += coeffPerTerm[i];
		}
		return constraints;
	}
	
	public void setConstraint(int paramIndex, boolean constrainedZero) {
		termInModel[paramIndex] = !constrainedZero;
		
		double constraints[] = expandConstraints();
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		model.updateLSParams(yKey, constraints);
		
		doEnabling();
		getData().variableChanged(modelKey);
	}

//-----------------------------------------------------------------------------------
	
	private void doEnabling() {
		if (hierarchy == null)
			return;
		for (int i=0 ; i<hierarchy.length ; i++) {
			if (hierarchy[i] == null)
				enableCheck(i, true);
			else {
				int requiredIndex[] = hierarchy[i];
				if (!isConstrained(i))
					for (int j=0 ; j<requiredIndex.length ; j++)
						enableCheck(requiredIndex[j], false);
				else {
					boolean canEnable = true;
					for (int j=0 ; j<requiredIndex.length ; j++)
						if (isConstrained(requiredIndex[j]))
							canEnable = false;
					enableCheck(i, canEnable);
				}
			}
		}
	}
	
	protected void enableCheck(int paramIndex, boolean enabled) {
		if (enabledCheck == null) {
			enabledCheck = new boolean[termName.length];
			for (int i=1 ; i<termName.length ; i++)
				enabledCheck[i] = true;
		}
		enabledCheck[paramIndex] = enabled;
	}
	
	private boolean isEnabled(int paramIndex) {
		return enabledCheck == null || enabledCheck[paramIndex];
	}

//-----------------------------------------------------------------------------------
	
	protected boolean isConstrained(int paramIndex) {
		return !termInModel[paramIndex];
	}
	
	protected Value getSsqHeading() {
		LabelValue kType3SsqLabel = new LabelValue(getApplet().translate("Type 3 ssq"));
		return kType3SsqLabel;
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		int nTerms = termName.length;
		tableHeight = (ascent + descent) * (nTerms + 1) + kLineGap * (nTerms - 1)
																							+ 2 * kTopBottomBorder + kHeadingGap;
		tableBottomHeight = ascent + descent + 2 * kLineGap;
		tableHeight += tableBottomHeight;
		
		paramNameWidth = 0;
		for (int i=0 ; i<nTerms ; i++)
			paramNameWidth = Math.max(paramNameWidth, fm.stringWidth(termName[i]));
		
		type3SsqWidth = Math.max(getSsqHeading().stringWidth(g), maxType3Ssq.stringWidth(g));
		dfWidth = Math.max(kDfLabel.stringWidth(g), maxDf.stringWidth(g));
		meanSsqWidth = Math.max(kMeanSsqLabel.stringWidth(g), maxMeanSsq.stringWidth(g));
		fWidth = Math.max(kFLabel.stringWidth(g), maxF.stringWidth(g));
		pValueWidth = Math.max(kPValueLabel.stringWidth(g), kMaxPValue.stringWidth(g));
		
		int nCols = 6;
		
		tableWidth = paramNameWidth + type3SsqWidth + dfWidth + meanSsqWidth + pValueWidth
													+ fWidth + 2 * kLeftRightBorder + (nCols - 1) * kColumnGap;
	}
	
	protected final int getParamBaseline(int row) {
		int lineGap = kLineGap + Math.min(0, (getSize().height - tableHeight) / (termName.length - 1));
		return kTopBottomBorder + ascent + kHeadingGap + ascent + descent
																									+ row * (ascent + descent + lineGap);
	}

//-----------------------------------------------------------------------------------
	
	protected int leftCheckBorder() {			//	for checkboxes to remove variables
		return kBoxSize + 2 * kBoxLeftRight;
	}
	
	public void drawLeftChecks(Graphics g) {
		int nTerms = termName.length;
		for (int i=0 ; i<nTerms ; i++) {
			int paramBaseline = getParamBaseline(i);
			boolean constrainedToZero = !termInModel[i];
			boolean buttonDown = doingDrag && (i == selectedIndex);
			
			Color topLeftColor = buttonDown	? kLightGray : Color.white;
			Color bottomRightColor = buttonDown	? Color.white : kLightGray;
			
			int boxLeft = tableLeft - kBoxSize - kBoxLeftRight;
			int boxTop = paramBaseline - (kBoxSize - 2);
			
			if (isEnabled(i)) {
				g.setColor(Color.white);
				g.fillRect(boxLeft + 1, boxTop + 1, kBoxSize - 1, kBoxSize - 1);
				
				g.setColor(topLeftColor);
				g.drawLine(boxLeft - 1, boxTop - 1, boxLeft - 1, boxTop + kBoxSize + 1);
				g.drawLine(boxLeft - 1, boxTop - 1, boxLeft + kBoxSize + 1, boxTop - 1);
				
				g.setColor(bottomRightColor);
				g.drawLine(boxLeft, boxTop + kBoxSize + 1, boxLeft + kBoxSize + 1, boxTop + kBoxSize + 1);
				g.drawLine(boxLeft + kBoxSize + 1, boxTop, boxLeft + kBoxSize + 1, boxTop + kBoxSize + 1);
			}
			
			g.setColor(isEnabled(i) ? Color.black : kDimCheckColor);
			g.drawRect(boxLeft, boxTop, kBoxSize, kBoxSize);
			if (buttonDown)
				g.drawRect(boxLeft + 1, boxTop + 1, kBoxSize - 2, kBoxSize - 2);
			
			if (!constrainedToZero) {
				g.drawLine(boxLeft, boxTop, boxLeft + kBoxSize, boxTop + kBoxSize);
				g.drawLine(boxLeft, boxTop + kBoxSize, boxLeft + kBoxSize, boxTop);
			}
		}
	}
	
	private boolean meaningfulTest(int paramIndex) {
		return (enabledCheck == null || enabledCheck[paramIndex]) && !isConstrained(paramIndex);
	}
	
	protected SSComponent nextExplainedSsq(MultipleRegnModel model, double[] modelConstraints,
										double[] seqConstraints, int startCoeffIndex, int nCoeffInTerm,
										SSComponent residSsqComponent) {
		for (int j=0 ; j<nCoeffInTerm ; j++)			//	temporarily remove variable
			modelConstraints[startCoeffIndex + j] = 0.0;
		
		SSComponent ssqComponent = model.getResidSsqComponent(yKey, modelConstraints);
		ssqComponent.ssq -= residSsqComponent.ssq;
		ssqComponent.df -= residSsqComponent.df;
		
		for (int j=0 ; j<nCoeffInTerm ; j++)			//	restore variable
			modelConstraints[startCoeffIndex + j] = Double.NaN;
			
		return ssqComponent;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int nTerms = termName.length;
		
		int headingBaseline = kTopBottomBorder + ascent;
		
		int nCols = 6;
		
		int extraGap = (getSize().width - tableWidth - leftCheckBorder()) / (nCols - 1);
		int colGap = Math.min(kMaxColGap, kColumnGap + extraGap);
		int leftRightBorder = Math.max(kLeftRightBorder,
													Math.min(kMaxLeftRightBorder, kLeftRightBorder + extraGap));
		
		int localTableWidth = tableWidth + (nCols - 1) * (colGap - kColumnGap)
																								+ 2 * (leftRightBorder - kLeftRightBorder);
		tableLeft = (getSize().width - localTableWidth - leftCheckBorder()) / 2 + leftCheckBorder();
		
		int paramNameLeft = tableLeft + leftRightBorder;
		int type3SsqRight = paramNameLeft + paramNameWidth + colGap + type3SsqWidth;
		int dfRight = type3SsqRight + colGap + dfWidth;
		int meanSsqRight = dfRight + colGap + meanSsqWidth;
		int fRight = meanSsqRight + colGap + fWidth;
		int pValueLeft = fRight + colGap;
		
		int lineGap = kLineGap + Math.min(0, (getSize().height - tableHeight) / (nTerms - 1));
		
		g.setColor(Color.white);
		int innerTop = headingBaseline + descent + kHeadingGap / 2;
		int innerHeight = tableHeight - innerTop - tableBottomHeight;
		g.fillRect(tableLeft, innerTop, localTableWidth, innerHeight);
		g.setColor(Color.black);
		
		getSsqHeading().drawCentred(g, type3SsqRight - type3SsqWidth / 2, headingBaseline);
		kDfLabel.drawCentred(g, dfRight - dfWidth / 2, headingBaseline);
		kMeanSsqLabel.drawCentred(g, meanSsqRight - meanSsqWidth / 2, headingBaseline);
		kFLabel.drawCentred(g, fRight - fWidth / 2, headingBaseline);
		kPValueLabel.drawCentred(g, pValueLeft + pValueWidth / 2, headingBaseline);
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		double[] modelConstraints = expandConstraints();
		
		SSComponent residSsqComponent = model.getResidSsqComponent(yKey, modelConstraints);
		double residSsq = residSsqComponent.ssq;
		int residDf = residSsqComponent.df;
		
		double[] seqConstraints = new double[modelConstraints.length];
		seqConstraints[0] = Double.NaN;
		SSComponent totalSsqComponent = model.getResidSsqComponent(yKey, seqConstraints);
		
		int startCoeffIndex = 1;	//	ignore constant term
		int termBaseline = getParamBaseline(0);
		
		for (int i=0 ; i<nTerms ; i++) {
//			boolean constrainedToZero = modelConstraints != null && !Double.isNaN(modelConstraints[i]);
			if (termInModel[i] && meaningfulTest(i))
				g.setColor(Color.black);
			else {
				g.setColor(kConstrainedBackColor);
				g.fillRect(tableLeft, termBaseline - ascent - kLineSpace - kTopBottomBorder,
																								localTableWidth, ascent + descent + lineGap);
				g.setColor(kConstrainedForeColor);
			}
			
			g.drawString(termName[i], paramNameLeft, termBaseline);
			
			if (termInModel[i]) {
				SSComponent explainedSsq = nextExplainedSsq(model, modelConstraints,
												seqConstraints, startCoeffIndex, coeffPerTerm[i], residSsqComponent);
				
				double ssq = explainedSsq.ssq;
				int df = explainedSsq.df;
				
				NumValue ssqValue = new NumValue(ssq, maxType3Ssq.decimals);
				ssqValue.drawLeft(g, type3SsqRight, termBaseline);
				
				NumValue dfValue = new NumValue(df, maxDf.decimals);
				dfValue.drawLeft(g, dfRight, termBaseline);
				
				double meanSsq = ssq / df;
				NumValue meanSsqValue = new NumValue(meanSsq, maxMeanSsq.decimals);
				meanSsqValue.drawLeft(g, meanSsqRight, termBaseline);
				
				double f = meanSsq / (residSsq / residDf);
				NumValue fValue = new NumValue(f, maxF.decimals);
				fValue.drawLeft(g, fRight, termBaseline);
				
				double pValue = 1.0 - FTable.cumulative(f, df, residDf);
				NumValue pValueVal = new NumValue(pValue, kMaxPValue.decimals);
				pValueVal.drawRight(g, pValueLeft, termBaseline);
			}
			
			startCoeffIndex += coeffPerTerm[i];
			termBaseline += (ascent + descent + lineGap);
		}
		
		termBaseline = drawExtraTableRow(g, residSsqComponent, totalSsqComponent,
										termBaseline, paramNameLeft, type3SsqRight, dfRight, meanSsqRight, lineGap);
		
		g.setColor(Color.black);
		g.drawLine(tableLeft, innerTop, tableLeft + localTableWidth - 1, innerTop);
		g.drawLine(tableLeft, innerTop + innerHeight - 1, tableLeft + localTableWidth - 1, innerTop + innerHeight - 1);
		
		termBaseline = drawRowUnderTable(g, residSsqComponent, totalSsqComponent,
										termBaseline, paramNameLeft, type3SsqRight, dfRight, meanSsqRight, lineGap);
		
		drawLeftChecks(g);
	}
	
	protected int drawExtraTableRow(Graphics g, SSComponent residSsqComponent,
										SSComponent totalSsqComponent, int termBaseline, int paramNameLeft,
										int type3SsqRight, int dfRight, int meanSsqRight, int lineGap) {
		return termBaseline + kTopBottomBorder;
	}
	
	protected int drawRowUnderTable(Graphics g, SSComponent residSsqComponent,
										SSComponent totalSsqComponent, int termBaseline, int paramNameLeft,
										int type3SsqRight, int dfRight, int meanSsqRight, int lineGap) {
		drawNontestRow(g, residSsqComponent.ssq, residSsqComponent.df, getApplet().translate("Residual"),
											termBaseline, paramNameLeft, type3SsqRight, dfRight, meanSsqRight,
											kResidColor);
		return termBaseline + (ascent + descent + lineGap);
	}
	
	protected void drawNontestRow(Graphics g, double ssq, int df, String rowName,
											int baseline, int paramNameLeft, int type3SsqRight, int dfRight,
											int meanSsqRight, Color c) {
		g.setColor(c);
		g.drawString(rowName, paramNameLeft, baseline);
		
		NumValue ssqValue = new NumValue(ssq, maxType3Ssq.decimals);
		ssqValue.drawLeft(g, type3SsqRight, baseline);
		
		NumValue dfValue = new NumValue(df, maxDf.decimals);
		dfValue.drawLeft(g, dfRight, baseline);
			
		NumValue meanSsqValue = new NumValue(ssq / df, maxMeanSsq.decimals);
		meanSsqValue.drawLeft(g, meanSsqRight, baseline);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (x < tableLeft - kBoxSize - 2 * kBoxLeftRight || x >= tableLeft)
			return null;
		
		int rowSpacing = getParamBaseline(1) - getParamBaseline(0);
		int top = (getParamBaseline(0) + 2 + getParamBaseline(1) - kBoxSize + 2) / 2 - rowSpacing;
		
		if (y < top)
			return null;
		
		int hitIndex = (y - top) / rowSpacing;
		
		if (hitIndex < 0 || hitIndex >= termName.length)
			return null;
		else
			return isEnabled(hitIndex) ? new IndexPosInfo(hitIndex) : null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		IndexPosInfo hitItem = (IndexPosInfo)getInitialPosition(x, y);
		if (hitItem == null)
			return null;
		int hitIndex = hitItem.itemIndex;
		
		if (selectedIndex == hitIndex)
			return hitItem;
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof IndexPosInfo) {
			selectedIndex = ((IndexPosInfo)startInfo).itemIndex;
			doingDrag = true;
			repaint();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (doingDrag) {
			setConstraint(selectedIndex, !isConstrained(selectedIndex));
			doingDrag = false;
		}
		repaint();
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