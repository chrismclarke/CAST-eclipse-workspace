package glmAnova;

import java.awt.*;

import dataView.*;
import coreGraphics.*;
import models.*;


public class AnovaReorderTableView extends AnovaCombineTableView {
	
	static final private int kArrowGap = 4;
//	static final private int kHitSlop = 7;
	static final private int kBarWidth = 16;
	
	static final private Color kExtraColor = new Color(0xFF0033);
															//	for diff between marginal & conditional when only 2 explained components
	
	static private String[] createComponentNames(String[] shortVarName, XApplet applet) {
		String componentName[] = new String[shortVarName.length + 2];
		componentName[0] = applet.translate("Total");
		componentName[1] = shortVarName[0];
		String xSequence = "";
		for (int i=1 ; i<shortVarName.length ; i++) {
			if (i > 1)
				xSequence += "&";
			xSequence += shortVarName[i - 1];
			componentName[i + 1] = shortVarName[i] + " after " + xSequence;
		}
		componentName[componentName.length - 1] = applet.translate("Residual");
		return componentName;
	}
	
	private String fullModelKey;
	private String lsKey[];
	private String shortVarName[];
	private int nComponentVars[];		//	does not get reordered when rows swapped
	private double type3Ssq[];			//	does not get reordered when rows swapped
	
	private int indexToAdd[];
	private double maxSsqBar;
	
	private int maxVarNameWidth;
	
	private boolean doingDrag = false;
	private int dragVert, hitOffset;
	private int dragComponent;
	
	public AnovaReorderTableView(DataSet theData, XApplet applet, String[] shortVarName,
									String[] componentKey, String[] lsKey, NumValue maxSsq, Color[] componentColor,
									String[] variableName, double maxSsqBar, int[] nComponentVars, double[] type3Ssq) {
		super(theData, applet, componentKey, maxSsq, createComponentNames(shortVarName, applet),
																													componentColor, variableName);
		this.shortVarName = (String[])shortVarName.clone();
		this.lsKey = lsKey;
		fullModelKey = lsKey[lsKey.length - 1];
		this.maxSsqBar = maxSsqBar;
		this.nComponentVars = nComponentVars;
		this.type3Ssq = type3Ssq;
		indexToAdd = new int[shortVarName.length];
		for (int i=0 ; i<shortVarName.length ; i++)
			indexToAdd[i] =  i;
		
		fitSequentialModels();		//	AnovaTableReorderApplet creates models but does not fit them
	}
	
//--------------------------------------------------------------------
	
	private void fitSequentialModels() {
		int nx;
		if (nComponentVars == null)
			nx = shortVarName.length;
		else {
			nx = 0;
			for (int i=0 ; i<nComponentVars.length ; i++)
				nx += nComponentVars[i];
		}
		double constraints[] = new double[nx + 1];
		constraints[0] = Double.NaN;
		
		for (int i=0 ; i<shortVarName.length ; i++) {
			if (nComponentVars == null)
				constraints[indexToAdd[i] + 1] = Double.NaN;
			else {
				int firstInGroup = 1;
				for (int j=0 ; j<indexToAdd[i] ; j++)
					firstInGroup += nComponentVars[j];
				for (int j=0 ; j<nComponentVars[indexToAdd[i]] ; j++)
					constraints[firstInGroup + j] = Double.NaN;
			}
			MultipleRegnModel lsModel = (MultipleRegnModel)getVariable(lsKey[i]);
			lsModel.updateLSParams("y", constraints);
//			System.out.println("fitting model key=" + lsKey[i]);
//			for (int j=0 ; j<constraints.length ; j++)
//				System.out.print(" " + constraints[j]);
//			System.out.println("");
		}
	}
	
	private double[] getType3Ssq() {
		if (type3Ssq != null)
			return type3Ssq;
		
		double localType3Ssq[] = new double[componentKey.length - 2];
		MultipleRegnModel fullModel = (MultipleRegnModel)getVariable(fullModelKey);
//		System.out.println("fullModelKey =" + fullModelKey);
		double[] xxInv = fullModel.getXXInv();
		
		for (int i=0 ; i<localType3Ssq.length ; i++) {
			int paramIndex = i + 1;
			double coeff = fullModel.getParameter(paramIndex).toDouble();
			double var = xxInv[(paramIndex + 1) * (paramIndex + 2) / 2 - 1];
			localType3Ssq[i] = coeff * coeff / var;
			
//			System.out.print(localType3Ssq[i] + " ");
		}
//		System.out.println("");
		return localType3Ssq;
	}
	
//--------------------------------------------------------------------
	
	protected void doInitialisation(Graphics g) {
		maxVarNameWidth = 0;
		FontMetrics fm = g.getFontMetrics();
		for (int i=0 ; i<variableName.length ; i++)
			maxVarNameWidth = Math.max(maxVarNameWidth, fm.stringWidth(variableName[i]));
		
		super.doInitialisation(g);
	}
	
	protected int getLeftBorder(Graphics g) {
		return 3 * kArrowGap + ModelGraphics.kHandleWidth + maxVarNameWidth;
	}
	
	protected void drawLeftBorder(int borderRight, int actualTableWidth, Graphics g) {
		int firstComponentBaseline = getComponentBaseline(0);
		int componentStep = getComponentBaseline(1) - firstComponentBaseline;
		
		if (doingDrag) {
			g.setColor(Color.yellow);
			int selectedTop = getZerothSeparatorVert() + dragComponent * componentStep;
			g.fillRect(0, selectedTop, getLeftBorder(g), componentStep);
		}
		
		LabelValue tempName = new LabelValue("");
		int arrowHorizCenter = borderRight - kArrowGap - ModelGraphics.kHandleWidth / 2;
		for (int i=0 ; i<variableName.length ; i++) {
			g.setColor(componentColor[i + 1]);
			tempName.label = variableName[i];
			int nameBaseline = firstComponentBaseline + i * componentStep;
			tempName.drawLeft(g, borderRight - 2 * kArrowGap - ModelGraphics.kHandleWidth, nameBaseline);
			
			if (!doingDrag) {
				g.setColor(Color.red);
				int arrowVertCenter = nameBaseline - (ascent - descent) / 2 - 1;
				g.drawLine(arrowHorizCenter, arrowVertCenter - 5, arrowHorizCenter, arrowVertCenter + 5);
				g.drawLine(arrowHorizCenter, arrowVertCenter - 5, arrowHorizCenter - 3, arrowVertCenter - 2);
				g.drawLine(arrowHorizCenter, arrowVertCenter - 5, arrowHorizCenter + 3, arrowVertCenter - 2);
				g.drawLine(arrowHorizCenter, arrowVertCenter + 5, arrowHorizCenter - 3, arrowVertCenter + 2);
				g.drawLine(arrowHorizCenter, arrowVertCenter + 5, arrowHorizCenter + 3, arrowVertCenter + 2);
			}
		}
		
		if (doingDrag) {
			int newIndex = getInsertionPoint(dragComponent, dragVert);
			if (newIndex >= 0) {
				g.setColor(Color.red);
				int newVert = getZerothSeparatorVert() + newIndex * componentStep;
				g.drawLine(0, newVert, getSize().width, newVert);
			}
			Point p = new Point(arrowHorizCenter, dragVert);
			ModelGraphics.drawHandle(g, p, true);
		}
	}
	
	protected boolean keepToLeft() {
		return true;
	}
	
	protected void drawRightBorder(int left, Graphics g) {
		if (showTests)
			return;				//	only show bars if F-tests are not displayed
		
		int componentStep = getComponentBaseline(1) - getComponentBaseline(0);
		int barCenter = getZerothComponentVert();
		type3Ssq = getType3Ssq();
		int nExplained = componentKey.length - 2;
		for (int i=0 ; i<nExplained ; i++) {
			CoreComponentVariable comp = (CoreComponentVariable)getVariable(componentKey[i + 1]);
			double ssq = comp.getSsq();
//			System.out.println("seq ssq for component " + componentKey[i + 1] + " = " + ssq);
			int barLength = (int)Math.round(ssq / maxSsqBar * (getSize().width - left));
			
			g.setColor(componentColor[i + 1]);
			g.fillRect(left, barCenter - kBarWidth / 2, barLength, kBarWidth);
			
			int type3Length = (int)Math.round(type3Ssq[indexToAdd[i]] / maxSsqBar * (getSize().width - left));
			if (nExplained == 2)
				g.setColor(kExtraColor);
			else
				g.setColor(mixColors(componentColor[i + 1], Color.white, 0.5));
			g.fillRect(left, barCenter - kBarWidth / 2 + 6, Math.max(1, type3Length), kBarWidth - 12);
																		//		always show at least 1 pixel
			barCenter += componentStep;
		}
	}
	
	private int getInsertionPoint(int dragIndex, int dragVert) {
		int componentStep = getComponentBaseline(1) - getComponentBaseline(0);
		int top = getZerothComponentVert() - componentStep;
		if (dragVert < top)
			return -1;
		int newIndex = (dragVert - top) / componentStep;
		if (newIndex > variableName.length || newIndex == dragIndex || newIndex == dragIndex + 1)
			return -1;
		return newIndex;
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	private int getZerothComponentVert() {
		return getComponentBaseline(0) - (ascent - descent) / 2;
	}
	
	private int getZerothSeparatorVert() {
		return getComponentBaseline(0) - ascent - kExpResidGap / 2;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int top = getZerothSeparatorVert();
		int componentStep = getComponentBaseline(1) - getComponentBaseline(0);
		
		if (y < top)
			return null;
		
		int hitIndex = (y - top) / componentStep;
		if (hitIndex >= variableName.length)
			return null;
		
		return new VertDragPosInfo(y, hitIndex, y - getZerothComponentVert() - hitIndex * componentStep);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		return new VertDragPosInfo(y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		doingDrag = true;
		VertDragPosInfo posInfo = (VertDragPosInfo)startInfo;
		hitOffset = posInfo.hitOffset;
		dragVert = posInfo.y  - hitOffset;
		dragComponent = posInfo.index;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			doingDrag = false;
		else {
			doingDrag = true;
			VertDragPosInfo posInfo = (VertDragPosInfo)toPos;
			int componentStep = getComponentBaseline(1) - getComponentBaseline(0);
			
			int topVert = getZerothSeparatorVert() - componentStep / 2;
			int bottomVert = topVert + (variableName.length + 1) * componentStep;
			dragVert = Math.max(topVert, Math.min(bottomVert, posInfo.y - hitOffset));
		}
		repaint();
	}
	
	private void swapEntries(int startIndex, int newIndex) {
		if (startIndex < newIndex) {
			newIndex --;
			String tempXKey = shortVarName[startIndex];
			for (int i=startIndex ; i<newIndex ; i++)
				shortVarName[i] = shortVarName[i + 1];
			shortVarName[newIndex] = tempXKey;
			
			String tempVarName = variableName[startIndex];
			for (int i=startIndex ; i<newIndex ; i++)
				variableName[i] = variableName[i + 1];
			variableName[newIndex] = tempVarName;
			
			Color tempColor = componentColor[startIndex + 1];
			for (int i=startIndex ; i<newIndex ; i++)
				componentColor[i + 1] = componentColor[i + 2];
			componentColor[newIndex + 1] = tempColor;
			
			int tempIndex = indexToAdd[startIndex];
			for (int i=startIndex ; i<newIndex ; i++)
				indexToAdd[i] = indexToAdd[i + 1];
			indexToAdd[newIndex] = tempIndex;
		}
		else {
			String tempXKey = shortVarName[startIndex];
			for (int i=startIndex ; i>newIndex ; i--)
				shortVarName[i] = shortVarName[i - 1];
			shortVarName[newIndex] = tempXKey;
			
			String tempVarName = variableName[startIndex];
			for (int i=startIndex ; i>newIndex ; i--)
				variableName[i] = variableName[i - 1];
			variableName[newIndex] = tempVarName;
			
			Color tempColor = componentColor[startIndex + 1];
			for (int i=startIndex ; i>newIndex ; i--)
				componentColor[i + 1] = componentColor[i];
			componentColor[newIndex + 1] = tempColor;
			
			int tempIndex = indexToAdd[startIndex];
			for (int i=startIndex ; i>newIndex ; i--)
				indexToAdd[i] = indexToAdd[i - 1];
			indexToAdd[newIndex] = tempIndex;
		}
		
		fitSequentialModels();
		
		componentName = createComponentNames(shortVarName, getApplet());
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (doingDrag) {
			int newIndex = getInsertionPoint(dragComponent, dragVert);
			if (newIndex >= 0)
				swapEntries(dragComponent, newIndex);
			doingDrag = false;
		}
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		fitSequentialModels();
		type3Ssq = null;
		repaint();
	}
}