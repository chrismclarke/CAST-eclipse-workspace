package ssq;

import java.awt.*;

import dataView.*;
import distn.*;
import models.*;


abstract public class CoreAnovaTableView extends DataView {
	static final public int SSQ_ONLY = 0;
	static final public int SSQ_AND_DF = 1;
	static final public int SSQ_AND_MSSQ = 2;
	static final public int SSQ_AND_F = 3;
	static final public int SSQ_F_PVALUE = 4;
	
	static final private NumValue kMaxPValue = new NumValue(1.0, 4);
	static final private String kMaxDFString = "999";
	
	static final private int kExpResidGap = 3;
	static final private int kLineSpace = 3;
	static final private int kLineGap = 2 * kLineSpace + 1;
	static final private int kLeftRightBorder = 2;
	static final private int kColumnGap = 12;
	static final private int kBottomBorder = 2;
	static final private int kMaxColExtraGap = 20;
	
	static final private Value kSSString = new LabelValue("SS");
	static final private Value kDFString = new LabelValue("df");
	static final private Value kMSSString = new LabelValue("MSS");
	static final private Value kFString = new LabelValue("F");
	
	static final private Value kInfinityValue = new LabelValue("infinity");
	static final private NumValue kZero = new NumValue(0.0, kMaxPValue.decimals);
	static final private double kEps = 0.0000001;
	
	private Value kPValueString;
	
	private NumValue maxSsq, maxMsq, maxF;
	private int tableDisplayType;
	
	private int fDenom[] = null;
	
	private String componentString[];
	private Color componentColor[] = {BasicComponentVariable.kTotalColor,
									BasicComponentVariable.kExplainedColor, BasicComponentVariable.kResidColor};
	
	private boolean initialised = false;
	
	private int ascent, descent, tableHeight, tableWidth, residWidth, totalWidth;
	private int ssqRight, dfRight, msqRight, fRight, pValRight;
	
	private int hiliteIndex = -1;
	private Color hiliteColor;
	
	public CoreAnovaTableView(DataSet theData, XApplet applet,
										NumValue maxSsq, NumValue maxMsq, NumValue maxF, int tableDisplayType) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		kPValueString = new LabelValue(applet.translate("p-value"));
		
		this.maxSsq = maxSsq;
		this.maxMsq = maxMsq;
		this.maxF = maxF;
		this.tableDisplayType = tableDisplayType;
	}
	
	public void setComponentNames(String[] name) {
		componentString = name;
	}
	
	public void setComponentColors(Color[] color) {
		componentColor = color;
	}
	
	public void setMaxValues(NumValue maxSsq, NumValue maxMsq, NumValue maxF) {
		this.maxSsq = maxSsq;
		this.maxMsq = maxMsq;
		this.maxF = maxF;
		initialised = false;
	}
	
	private void initComponentNames() {
		if (componentString == null) {
			componentString = new String[3];
			componentString[0] = getApplet().translate("Total");
			componentString[1] = getApplet().translate("Explained");
			componentString[2] = getApplet().translate("Residual");
		}
	}
	
	public void setHilite(int hiliteIndex, Color hiliteColor) {
		this.hiliteIndex = hiliteIndex;
		this.hiliteColor = hiliteColor;
	}
	
	public void setFDenom(int numer, int denom) {
		if (fDenom == null) {
			fDenom = new int[countAnovaComponents()];
			for (int i=0 ; i<fDenom.length ; i++)
				fDenom[i] = -1;
		}
		fDenom[numer] = denom;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			int nComp = countAnovaComponents();
			tableHeight = (ascent + descent) * (nComp + 1) + kExpResidGap * (nComp - 2)
																													+ 2 * kLineGap + kBottomBorder + 3;
			int sourceWidth = 0;
			initComponentNames();
			for (int i=0 ; i<nComp ; i++)
				sourceWidth = Math.max(sourceWidth, fm.stringWidth(componentString[i]));
			int ssqWidth = Math.max(maxSsq.stringWidth(g), kSSString.stringWidth(g));
			tableWidth = 2 * kLeftRightBorder + sourceWidth + ssqWidth + kColumnGap;
			residWidth = tableWidth;
			totalWidth = tableWidth;
			ssqRight = kLeftRightBorder + sourceWidth + kColumnGap + ssqWidth;
			
			if (tableDisplayType >= SSQ_AND_DF) {
				int dfWidth = Math.max(fm.stringWidth(kMaxDFString), kDFString.stringWidth(g));
				dfRight = ssqRight + kColumnGap + dfWidth;
				
				tableWidth += dfWidth + kColumnGap;
				residWidth += dfWidth + kColumnGap;
				totalWidth += dfWidth + kColumnGap;
				if (tableDisplayType >= SSQ_AND_MSSQ) {
					int msqWidth = Math.max(maxMsq.stringWidth(g), kMSSString.stringWidth(g));
					msqRight = dfRight + kColumnGap + msqWidth;
					
					tableWidth += msqWidth + kColumnGap;
					residWidth += msqWidth + kColumnGap;
					
					if (tableDisplayType >= SSQ_AND_F) {
						int fWidth = Math.max(maxF.stringWidth(g), kFString.stringWidth(g));
						fRight = msqRight + kColumnGap + fWidth;
						tableWidth += fWidth + kColumnGap;
						
						if (tableDisplayType == SSQ_F_PVALUE) {
							int pValWidth = Math.max(kMaxPValue.stringWidth(g), kPValueString.stringWidth(g));
							pValRight = fRight + kColumnGap + pValWidth;
							tableWidth += pValWidth + kColumnGap;
						}
					}
				}
			}
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected AnovaLayoutInfo getAnovaLayoutInfo() {
		int nComp = countAnovaComponents();
		int headingBaseline = ascent;
		int firstComponentBaseline = headingBaseline + kLineGap + ascent + descent;
		int firstComponentTop = firstComponentBaseline - ascent - kExpResidGap + 1;		//	for shading tow
		int componentStep = kExpResidGap + ascent + descent;
		int residBaseline = firstComponentBaseline + componentStep * (nComp - 2);
		int totalBaseline = residBaseline + kLineGap + ascent + descent + 2;
		
		int leftMargin = leftMargin();
		int noOfColGaps = (tableDisplayType == SSQ_ONLY) ? 1
											: (tableDisplayType == SSQ_AND_DF) ? 2
											: (tableDisplayType == SSQ_AND_MSSQ) ? 3
											: (tableDisplayType == SSQ_AND_F) ? 4 : 5;
		int colGapExtra = Math.min(kMaxColExtraGap,
																	(getSize().width - tableWidth - leftMargin) / noOfColGaps);
		int localTableWidth = tableWidth + noOfColGaps * colGapExtra;
		int tableLeft = (getSize().width - localTableWidth + leftMargin) / 2;
		
		int localSsqRight = ssqRight + colGapExtra;
		int localDfRight = dfRight + 2 * colGapExtra;
		int localMsqRight = msqRight + 3 * colGapExtra;
		int localFRight = fRight + 4 * colGapExtra;
		int localPValRight = pValRight + 5 * colGapExtra;
		int localTotalWidth = totalWidth + Math.min(noOfColGaps, 2) * colGapExtra;
		int localResidWidth = residWidth + Math.min(noOfColGaps, 3) * colGapExtra;
		return new AnovaLayoutInfo(nComp, headingBaseline, firstComponentBaseline, firstComponentTop,
										componentStep, residBaseline, totalBaseline,
										leftMargin, localTableWidth, tableLeft, localSsqRight, localDfRight, localMsqRight,
										localFRight, localPValRight, localTotalWidth, localResidWidth);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		AnovaLayoutInfo anovaLayout = getAnovaLayoutInfo();
		int componentBaseline = anovaLayout.firstComponentBaseline;
		
		int topLineVert = ascent + descent + kLineSpace;
		int bottomLineVert = anovaLayout.residBaseline + descent + kLineSpace;
		
		g.drawLine(anovaLayout.tableLeft, topLineVert, anovaLayout.tableRight() - 1, topLineVert);
		g.drawLine(anovaLayout.tableLeft, bottomLineVert, anovaLayout.residRight() - 1, bottomLineVert);
		g.drawLine(anovaLayout.tableLeft, bottomLineVert + 2, anovaLayout.totalRight() - 1, bottomLineVert + 2);
		
		g.setColor(Color.white);
		g.fillRect(anovaLayout.tableLeft, topLineVert + 1, anovaLayout.tableWidth,
																							kLineSpace + anovaLayout.componentStep * (anovaLayout.nComp - 2));
		g.fillRect(anovaLayout.tableLeft, anovaLayout.residBaseline - ascent,
																					anovaLayout.residWidth, ascent + descent + kLineSpace);
		if (hiliteIndex >= 0) {
			g.setColor(hiliteColor);
			if (hiliteIndex == 0)
				g.fillRect(anovaLayout.tableLeft, anovaLayout.residBaseline - ascent,
																					anovaLayout.residWidth, ascent + descent + kLineSpace);
			else
				g.fillRect(anovaLayout.tableLeft, topLineVert + 1 + kLineSpace / 2 + (hiliteIndex - 1) * anovaLayout.componentStep,
																		anovaLayout.tableWidth, anovaLayout.componentStep);
		}
		
		g.setColor(getForeground());
		String sourceString = getApplet().translate("Source");
		g.drawString(sourceString, anovaLayout.tableLeft + kLeftRightBorder, anovaLayout.headingBaseline);
		kSSString.drawLeft(g, anovaLayout.ssqRight(), anovaLayout.headingBaseline);
		if (tableDisplayType >= SSQ_AND_DF) {
			kDFString.drawLeft(g, anovaLayout.dfRight(), anovaLayout.headingBaseline);
			if (tableDisplayType >= SSQ_AND_MSSQ) {
				kMSSString.drawLeft(g, anovaLayout.msqRight(), anovaLayout.headingBaseline);
				if (tableDisplayType >= SSQ_AND_F) {
					kFString.drawLeft(g, anovaLayout.fRight(), anovaLayout.headingBaseline);
					if (tableDisplayType == SSQ_F_PVALUE)
						kPValueString.drawLeft(g, anovaLayout.pValueRight(), anovaLayout.headingBaseline);
				}
			}
		}
		
//		int selectedIndex = getData().getSelection().findSingleSetFlag();
		NumValue ssq[] = new NumValue[anovaLayout.nComp];
		NumValue df[] = null;
		NumValue msq[] = null;
		Value f[] = null;
		Value pValue[] = null;
		if (tableDisplayType >= SSQ_AND_DF) {
			df = new NumValue[anovaLayout.nComp];
			if (tableDisplayType >= SSQ_AND_MSSQ) {
				msq = new NumValue[anovaLayout.nComp];
				if (tableDisplayType >= SSQ_AND_F) {
					f = new Value[anovaLayout.nComp - 1];
					if (tableDisplayType == SSQ_F_PVALUE)
						pValue = new Value[anovaLayout.nComp - 1];
				}
			}
		}
		
		if (canDrawTable()) {
			findComponentSsqs(ssq, df);
			
			for (int i=0 ; i<anovaLayout.nComp ; i++) {
				if (msq != null)
					msq[i] = (ssq[i] == null) ? null : new NumValue(ssq[i].toDouble() / df[i].toDouble(), maxMsq.decimals);
			}
			if (f != null)
				for (int i=1 ; i<anovaLayout.nComp-1 ; i++) {
					int denomIndex = (fDenom == null) ? anovaLayout.nComp - 1 : fDenom[i];
					if (denomIndex >= 0) {
						double denomMsq = msq[denomIndex].toDouble();
						double denomDF = df[denomIndex].toDouble();
						
						if (msq[i] == null) {
							f[i] = null;
							if (pValue != null)
								pValue[i] = null;
						}
						else if (denomMsq < kEps * msq[i].toDouble()) {
							f[i] = kInfinityValue;
							if (pValue != null)
								pValue[i] = kZero;
						}
						else {
							f[i] = new NumValue(msq[i].toDouble() / denomMsq, maxF.decimals);
							if (pValue != null)
								pValue[i] = (df[i].toDouble() < .01 || denomDF < .01) ? new NumValue(Double.NaN)
															: new NumValue(1.0 - FTable.cumulative(((NumValue)f[i]).toDouble(), df[i].toDouble(), denomDF),
																																						kMaxPValue.decimals);
						}
					}
				}
		}
			
		for (int i=1 ; i<anovaLayout.nComp-1 ; i++) {
			drawComponentBackground(i - 1, anovaLayout.tableLeft, anovaLayout.tableWidth,
																anovaLayout.componentTop(i - 1), anovaLayout.componentStep, g);
			int colorIndex = Math.min(componentColor.length - 2, i);
			g.setColor(componentColor[colorIndex]);
			g.drawString(componentString[i], anovaLayout.tableLeft + kLeftRightBorder, componentBaseline);
			if (canDrawTable()) {
				if (ssq[i] != null)
					ssq[i].drawLeft(g, anovaLayout.ssqRight(), componentBaseline);
				if (df != null)
					df[i].drawLeft(g, anovaLayout.dfRight(), componentBaseline);
				if (msq != null && msq[i] != null)
					msq[i].drawLeft(g, anovaLayout.msqRight(), componentBaseline);
				if (f != null && f[i] != null)
					f[i].drawLeft(g, anovaLayout.fRight(), componentBaseline);
				if (pValue != null && pValue[i] != null)
					pValue[i].drawLeft(g, anovaLayout.pValueRight(), componentBaseline);
			}
			componentBaseline += anovaLayout.componentStep;
		}
		
		g.setColor(componentColor[componentColor.length - 1]);
		int residualBaseline = componentBaseline;
		int residIndex = anovaLayout.nComp - 1;
		g.drawString(componentString[residIndex], anovaLayout.tableLeft + kLeftRightBorder, residualBaseline);
		if (canDrawTable()) {
			ssq[residIndex].drawLeft(g, anovaLayout.ssqRight(), residualBaseline);
			if (df != null)
				df[residIndex].drawLeft(g, anovaLayout.dfRight(), residualBaseline);
			if (msq != null)
				msq[residIndex].drawLeft(g, anovaLayout.msqRight(), residualBaseline);
		}
		
		g.setColor(componentColor[0]);
		g.drawString(componentString[0], anovaLayout.tableLeft + kLeftRightBorder, anovaLayout.totalBaseline);
		if (canDrawTable()) {
			ssq[0].drawLeft(g, anovaLayout.ssqRight(), anovaLayout.totalBaseline);
			if (df != null)
				df[0].drawLeft(g, anovaLayout.dfRight(), anovaLayout.totalBaseline);
			if (msq != null)
				msq[0].drawLeft(g, anovaLayout.msqRight(), anovaLayout.totalBaseline);
		}
	}
	
	protected int leftMargin() {
		return 0;
	}
	
	protected void drawComponentBackground(int explainedIndex, int tableLeft, int tableWidth,
																			int componentTop, int componentHeight, Graphics g) {
	}

//-----------------------------------------------------------------------------------
	
	abstract protected int countAnovaComponents();
																								//	includes total and resid
	
	abstract protected boolean canDrawTable();
	
	abstract protected void findComponentSsqs(NumValue[] ssq, NumValue[] df);
																								//	first is total, last is residual


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
		return new Dimension(tableWidth + leftMargin(), tableHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}