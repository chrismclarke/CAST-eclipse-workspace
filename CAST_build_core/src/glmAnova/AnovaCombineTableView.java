package glmAnova;

import java.awt.*;

import dataView.*;
import distn.*;
import models.*;


public class AnovaCombineTableView extends DataView {
//	static final public String ANOVA_COMBINE_TABLE = "anovaCombineTable";
	
	static final private String kMaxDFString = "999";
	static final private NumValue kMaxPvalue = new NumValue(1.0, 4);
	static final private double kMaxF = 99999;
	
	static final protected int kExpResidGap = 6;
	static final private int kLineSpace = 4;
	static final private int kLineGap = 2 * kLineSpace + 1;
	static final private int kLeftRightInnerBorder = 6;
	static final private int kColumnGap = 12;
	static final private int kBottomBorder = 2;
	static final private int kMaxColExtraGap = 20;
	
	static final private Value kSsqString = new LabelValue("SSq");
	static final private Value kDfString = new LabelValue("df");
	static final private Value kMsqString = new LabelValue("Msq");
	static final private Value kFString = new LabelValue("F");
	
	static final private int kNoOfFrames = 40;
	static final private int kFramesPerSec = 20;
	
	static final private Color kArrowColor = Color.red;
	
	private Value kPvalueString;
	private String kSourceString;
	
	protected String componentKey[];		//	first is total, last is residual
	private NumValue maxSsq, maxMsq, maxF;
	
	protected String componentName[];
	protected String variableName[];
	protected Color componentColor[];
	
	protected boolean showTests = false;
	
	private boolean initialised = false;
	
	protected int ascent, descent;
	protected int leftBorder, tableHeight, tableWidth;
	private int ssqRight, dfRight, msqRight, fRight, pValueRight;
	
	private int animateStartIndex, animateEndIndex;
	private String animateCombinedName;
	
	private boolean showFArrows = false;
	
	public AnovaCombineTableView(DataSet theData, XApplet applet, String[] componentKey, NumValue maxSsq,
									String[] componentName, Color[] componentColor, String[] variableName) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		kPvalueString = new LabelValue(applet.translate("p-value"));
		kSourceString = applet.translate("Source");
		this.componentKey = componentKey;
		this.maxSsq = maxSsq;
		this.componentName = (String[])componentName.clone();
		this.variableName = variableName;
		this.componentColor = (Color[])componentColor.clone();
	}
	
	public void setShowFArrows(boolean showFArrows) {
		this.showFArrows = showFArrows;
	}
	
	public void animateGrouping(int firstIndex, int lastIndex, String animateCombinedName) {
		animateStartIndex = firstIndex;
		animateEndIndex = lastIndex;
		this.animateCombinedName = animateCombinedName;
		animateFrames(1, kNoOfFrames - 1, kFramesPerSec, null);
	}
	
	public void animateSplitting(int firstIndex, int lastIndex, String[] allComponentNames,
																																	Color[] allComponentColors) {
		for (int j=lastIndex-1 ; j>=firstIndex ; j--)
			splitSsq(j, allComponentNames[j], allComponentColors[j], allComponentNames[j + 1],
																														allComponentColors[j + 1]);
		
		animateStartIndex = firstIndex;
		animateEndIndex = lastIndex;
		animateFrames(kNoOfFrames - 1, 1 - kNoOfFrames, kFramesPerSec, null);
	}
	
	public void immediateGroup(int firstIndex, int lastIndex, String animateCombinedName) {
		for (int j=lastIndex-1 ; j>=firstIndex ; j--)
			combineSsqWithNext(j, animateCombinedName, null);
		
		setFrame(0);
	}
	
	public void combineSsqWithNext(int componentIndex, String combinedName, Color combinedColor) {
		componentName[componentIndex] = null;
		componentColor[componentIndex] = null;
		for (int i=componentIndex+1 ; i<componentName.length ; i++)
			if (componentName[i] != null) {
				if (combinedName != null)
					componentName[i] = combinedName;
				if (combinedColor != null)
					componentColor[i] = combinedColor;
				break;
			}
	}
	
	public void splitSsq(int componentIndex, String splitName1, Color splitColor1,
																								String splitName2, Color splitColor2) {
		componentName[componentIndex] = splitName1;
		componentColor[componentIndex] = splitColor1;
		for (int i=componentIndex+1 ; i<componentName.length ; i++)
			if (componentName[i] != null) {
				if (splitName2 != null)
					componentName[i] = splitName2;
				if (splitColor2 != null)
					componentColor[i] = splitColor2;
				break;
			}
	}
	
	public void setShowTests(boolean showTests, NumValue maxMsq, NumValue maxF) {
		this.showTests = showTests;
		this.maxMsq = maxMsq;
		this.maxF = maxF;
	}

//--------------------------------------------------------------------
	
	protected int getLeftBorder(Graphics g) {
		return 0;
	}
	
	protected void drawLeftBorder(int borderRight, int actualTableWidth, Graphics g) {
	}
	
	protected boolean keepToLeft() {
		return false;
	}
	
	protected void drawRightBorder(int left, Graphics g) {
	}

//--------------------------------------------------------------------
	
	protected int getMaxSourceWidth(FontMetrics fm) {
		int sourceWidth = 0;
		for (int i=0 ; i<componentName.length ; i++)
			if (componentName[i] != null)
				sourceWidth = Math.max(sourceWidth, fm.stringWidth(componentName[i]));
		return sourceWidth;
	}
	
	protected void doInitialisation(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		int nComp = componentKey.length;
		tableHeight = (ascent + descent) * (nComp + 1) + kExpResidGap * (nComp - 2)
																												+ 2 * kLineGap + kBottomBorder + 3;
		leftBorder = getLeftBorder(g);
		int sourceWidth = getMaxSourceWidth(fm);
		int ssqWidth = Math.max(maxSsq.stringWidth(g), kSsqString.stringWidth(g));
		int dfWidth = Math.max(fm.stringWidth(kMaxDFString), kDfString.stringWidth(g));
		
		tableWidth = 2 * kLeftRightInnerBorder + sourceWidth + ssqWidth + dfWidth + 2 * kColumnGap;
		ssqRight = kLeftRightInnerBorder + sourceWidth + kColumnGap + ssqWidth;
		dfRight = ssqRight + kColumnGap + dfWidth;
		
		if (showTests) {
			int msqWidth = Math.max(maxMsq.stringWidth(g), kMsqString.stringWidth(g));
			int fWidth = Math.max(maxF.stringWidth(g), kFString.stringWidth(g));
			int pValueWidth = Math.max(kMaxPvalue.stringWidth(g), kPvalueString.stringWidth(g));
			
			tableWidth += 3 * kColumnGap + msqWidth + fWidth + pValueWidth;
			
			msqRight = dfRight + kColumnGap + msqWidth;
			fRight = msqRight + kColumnGap + fWidth;
			pValueRight = fRight + kColumnGap + pValueWidth;
		}
	}
	
	final protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		else {
			doInitialisation(g);
			initialised = true;
			return true;
		}
	}
	
	protected int getResidIndex(int i) {
		return componentKey.length - 1;
	}
	
	protected int getComponentBaseline(int i) {
		return ascent + kLineGap + ascent + descent + i * (kExpResidGap + ascent + descent);
	}
	
	private int getBaseline(int index, int firstBaseline, int componentBaseline, int componentStep) {
		int baselineToUse;
		boolean doingAnimation = getCurrentFrame() > 0;
		if (doingAnimation && index>=animateStartIndex && index<=animateEndIndex) {
			int groupStartBaseline = componentBaseline - (index - animateStartIndex) * componentStep;
			int groupEndBaseline = componentBaseline + (animateEndIndex - index) * componentStep;
			int groupedBaseline = (groupStartBaseline + groupEndBaseline) / 2;
			int currentFrame = getCurrentFrame();
			baselineToUse = (groupedBaseline * currentFrame
											 + componentBaseline * (kNoOfFrames - currentFrame)) / kNoOfFrames;
		}
		else
			baselineToUse = (firstBaseline + componentBaseline) / 2;
		return baselineToUse;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		if (getCurrentFrame() == kNoOfFrames)
			immediateGroup(animateStartIndex, animateEndIndex, animateCombinedName);
		
		int headingBaseline = ascent;
		int componentBaseline = getComponentBaseline(0);
		int componentStep = kExpResidGap + ascent + descent;
		int nComp = componentKey.length;
		int totalBaseline = componentBaseline + componentStep * (nComp - 2)
																										+ kLineGap + ascent + descent + 2;
		int noOfColGaps = 2;
		if (showTests)
			noOfColGaps += 3;
		int colGapExtra = Math.min(kMaxColExtraGap, (getSize().width - tableWidth - leftBorder)
																																					/ noOfColGaps);
		int localTableWidth = tableWidth + noOfColGaps * colGapExtra;
		int tableLeft = keepToLeft() ? leftBorder : (getSize().width - localTableWidth + leftBorder) / 2;
		
		int localSsqRight = ssqRight + colGapExtra;
		int localDfRight = dfRight + 2 * colGapExtra;
		int localMsqRight = msqRight + 3 * colGapExtra;
		int localFRight = fRight + 4 * colGapExtra;
		int localPvalueRight = pValueRight + 5 * colGapExtra;
		
		int topLineVert = ascent + descent + kLineSpace;
		int bottomLineVert = componentBaseline + componentStep * (nComp - 2) + descent + kLineSpace;
		g.drawLine(tableLeft, topLineVert, tableLeft + localTableWidth - 1, topLineVert);
		g.drawLine(tableLeft, bottomLineVert, tableLeft + localTableWidth - 1, bottomLineVert);
		g.drawLine(tableLeft, bottomLineVert + 2, tableLeft + localDfRight + kLeftRightInnerBorder - 1, bottomLineVert + 2);
		
		g.setColor(Color.white);
		g.fillRect(tableLeft, topLineVert + 1, localTableWidth,
																							kLineSpace + componentStep * (nComp - 2));
		g.fillRect(tableLeft, componentBaseline + componentStep * (nComp - 2) - ascent,
																					localTableWidth, ascent + descent + kLineSpace);
		
		g.setColor(getForeground());
		g.drawString(kSourceString, tableLeft + kLeftRightInnerBorder, headingBaseline);
		kSsqString.drawLeft(g, tableLeft + localSsqRight, headingBaseline);
		kDfString.drawLeft(g, tableLeft + localDfRight, headingBaseline);
		if (showTests) {
			kMsqString.drawLeft(g, tableLeft + localMsqRight, headingBaseline);
			kFString.drawLeft(g, tableLeft + localFRight, headingBaseline);
			kPvalueString.drawLeft(g, tableLeft + localPvalueRight, headingBaseline);
		}
		
		double allSsq[] = new double[nComp];
		int allDf[] = new int[nComp];
		
		for (int i=1 ; i<nComp ; i++)
			if (componentName[i] != null) {
				CoreComponentVariable comp = (CoreComponentVariable)getVariable(componentKey[i]);
				allSsq[i] = comp.getSsq();
				allDf[i] = comp.getDF();
				for (int j=i-1 ; j>=0 && componentName[j]==null ; j--) {
					comp = (CoreComponentVariable)getVariable(componentKey[j]);
					allSsq[i] += comp.getSsq();
					allDf[i] += comp.getDF();
				}
			}
		
		int firstBaseline = componentBaseline;
		for (int i=1 ; i<nComp ; i++) {
			if (componentName[i] != null) {
				int baselineToUse = getBaseline(i, firstBaseline, componentBaseline, componentStep);
				g.setColor(componentColor[i]);
				g.drawString(componentName[i], tableLeft + kLeftRightInnerBorder, baselineToUse);
				
				double sumSsq = allSsq[i];
				int sumDf = allDf[i];
				
				int residIndex = (i < nComp - 1) ? getResidIndex(i): 0;
				double residSsq = allSsq[residIndex];
				double residDf = allDf[residIndex];
				
				NumValue df = new NumValue(sumDf, 0);
				NumValue ssq = new NumValue(sumSsq, maxSsq.decimals);
				ssq.drawLeft(g, tableLeft + localSsqRight, baselineToUse);
				df.drawLeft(g, tableLeft + localDfRight, baselineToUse);
				
				if (showTests) {
					NumValue msq = new NumValue(ssq.toDouble() / df.toDouble(), maxMsq.decimals);
					msq.drawLeft(g, tableLeft + localMsqRight, baselineToUse);
					
					if (i < nComp - 1) {
						NumValue f;
						if (residDf < 0.5)
							f = NumValue.NAN_VALUE;
						else
							f = new NumValue(msq.toDouble() / (residSsq / residDf), maxF.decimals);
						if (f.toDouble() > kMaxF)
							f = NumValue.POS_INFINITY_VALUE;
						f.drawLeft(g, tableLeft + localFRight, baselineToUse);
						
						if (showFArrows) {
							g.setColor(kArrowColor);
							int arrowEnd = tableLeft + localMsqRight + 3;
							int arrowStart = tableLeft + localFRight - f.stringWidth(g) - 3;
							int arrowBaseline = baselineToUse - ascent / 2;
							g.drawLine(arrowStart, arrowBaseline, arrowEnd, arrowBaseline);
							
							boolean doingAnimation = getCurrentFrame() > 0;
							int residBaseline = componentBaseline + (residIndex - i) * componentStep;
							if (doingAnimation && residIndex>=animateStartIndex && residIndex<=animateEndIndex)
								residBaseline = getBaseline(residIndex, firstBaseline, residBaseline, componentStep);
							else {
								int nCombined = 0;
								while (residIndex - nCombined - 1 > 0 && componentName[residIndex - nCombined - 1] == null)
									nCombined ++;
								if (nCombined > 0)
									residBaseline -= (componentStep * nCombined) / 2;
							}
							residBaseline -= ascent / 2;
							g.drawLine(arrowStart, arrowBaseline, arrowEnd, residBaseline);
							g.setColor(componentColor[i]);
						}
						
						NumValue pValue;
						if (f == NumValue.NAN_VALUE)
							pValue = NumValue.NAN_VALUE;
						else if (f == NumValue.POS_INFINITY_VALUE)
							pValue = new NumValue(0, kMaxPvalue.decimals);
						else
							pValue = new NumValue(1.0 - FTable.cumulative(f.toDouble(), df.toDouble(), residDf),
																																		kMaxPvalue.decimals);
						pValue.drawLeft(g, tableLeft + localPvalueRight, baselineToUse);
					}
				}
				
				firstBaseline = componentBaseline + componentStep;
			}
			componentBaseline += componentStep;
		}
		
		g.setColor(componentColor[0]);
		g.drawString(componentName[0], tableLeft + kLeftRightInnerBorder, totalBaseline);
		
		CoreComponentVariable comp = (CoreComponentVariable)getVariable(componentKey[0]);
		NumValue ssq = new NumValue(comp.getSsq(), maxSsq.decimals);
		NumValue df = new NumValue(comp.getDF(), 0);
		
		ssq.drawLeft(g, tableLeft + localSsqRight, totalBaseline);
		df.drawLeft(g, tableLeft + localDfRight, totalBaseline);
		
		drawLeftBorder(tableLeft, localTableWidth, g);
		drawRightBorder(tableLeft + localTableWidth, g);
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
		return new Dimension(leftBorder + tableWidth, tableHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}