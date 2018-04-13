package histo;

import java.awt.*;

import dataView.*;
import axis.*;



public class DragClassHistoView extends VariableClassHistoView {
//	static public final String DRAG_CLASS_HISTO = "dragClassHisto";
	
	static final private Color kAreaColor = Color.blue;
	static final private Color kPropnColor = Color.red;
	
	static final private int kTopMargin = 4;
	static final private int kLeftRightMargin = 10;
	static final private int kDivideGap = 2;
	
	private String kValuesString, kAreaString;
	
	public DragClassHistoView(DataSet theData, XApplet applet, NumCatAxis valAxis,
									HistoDensityInfo densityAxis, double coreClass0Start, double coreClassWidth) {
		super(theData, applet, valAxis, densityAxis, coreClass0Start, coreClassWidth, null);
		
		kValuesString = applet.translate("of values");
		kAreaString = applet.translate("of area");
	}
	
	
	private void printPropn(Graphics g, boolean leftNotRight, int nSelected, int nValues,
																																				Color fontColor) {
		g.setColor(fontColor);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		
		int numerBaseline = kTopMargin + ascent;
		int divideVert = numerBaseline + kDivideGap;
		int denomBaseline = divideVert + kDivideGap + 1 + ascent;
		int textBaseline = (numerBaseline + denomBaseline) / 2;
		
		NumValue numer = new NumValue(nSelected, 0);
		NumValue denom = new NumValue(nValues, 0);
		int fractionWidth = denom.stringWidth(g) + 2;
		
		String textString = " " + (leftNotRight ? kValuesString : kAreaString);
		
		int textLeft, fractionCenter;
		if (leftNotRight) {
			fractionCenter = kLeftRightMargin + fractionWidth / 2;
			textLeft = kLeftRightMargin + fractionWidth;
		}
		else {
			textLeft = getSize().width - kLeftRightMargin - fm.stringWidth(textString);
			fractionCenter = textLeft - fractionWidth / 2;
		}
		
		numer.drawCentred(g, fractionCenter, numerBaseline);
		denom.drawCentred(g, fractionCenter, denomBaseline);
		g.drawLine(fractionCenter - fractionWidth / 2, divideVert, fractionCenter + fractionWidth / 2,
																																											divideVert);
		g.drawString(textString, textLeft, textBaseline);
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		int nSelected = getSelection().noOfSetFlags();
		int nValues = getSelection().getNoOfFlags();
		
		if (nSelected > 0) {
			printPropn(g, true, nSelected, nValues, kAreaColor);
			printPropn(g, false, nSelected, nValues, kPropnColor);
		}
	}
	
//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		double hitVal = 0.0;
		try {
			hitVal = axis.positionToNumVal(hitPos.x);
		} catch (AxisException e) {
			return null;
		}
		
		if (hitVal <= classStart[0])
			return null;
		for (int i=0 ; i<classCount.length ; i++)
			if (hitVal <= classStart[i+1])
				return new ClassPosInfo(i);
		return null;
	}
	
//-----------------------------------------------------------------------------------
	
	private int startDragClass = -1;
	
	protected void invertAtPosition(PositionInfo posInfo) {
		NumVariable variable = getNumVariable();
		boolean selectedValues[] = new boolean[variable.noOfValues()];
		
		int hitClass = ((ClassPosInfo)posInfo).classIndex;
		int minClass = Math.min(startDragClass, hitClass);
		int maxClass = Math.max(startDragClass, hitClass);
		int countBelow = tooLowCount;
		for (int i=0 ; i<minClass ; i++)
			countBelow += classCount[i];
		int selectedCount = 0;
		for (int i=minClass ; i<=maxClass ; i++)
			selectedCount += classCount[i];
		
		for (int i=0 ; i<selectedCount ; i++) {
			int index = variable.rankToIndex(countBelow + i);
			selectedValues[index] = true;
		}
		
		getData().setSelection(selectedValues);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else {
			startDragClass = ((ClassPosInfo)startInfo).classIndex;
			invertAtPosition(startInfo);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			invertAtPosition(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
																		//		leave classes highlighted
	}
	
}