package dotPlot;

import java.awt.*;

import dataView.*;
import valueList.*;

public class NumberList2 extends DataView {
//	static final private int kLeftRightBorder = 20;
	static final private int kMaxValueGap = 8;
	static final private int kMinValueGap = 2;
	static final private int kMaxTopBorder = 6;
	
	private boolean fontInitialised;
	private Font theFont;
	
	private int lineHt;
	private int baselineOffset;
//	private int offsetOfPoint;
	private int rightPos;
	private int topBorder;
	
//	static public final String NUMBER_LIST2 = "numberList2";
	
	
	public NumberList2(DataSet theData, XApplet applet) {
		super(theData, applet, null);
		fontInitialised = false;
		lockBackground(Color.white);
		repaint();
	}
	
	private void initialiseFont(Graphics g) {
		theFont = getApplet().getStandardFont();
		fontInitialised = true;
		Dimension displaySize = getSize();
		g.setFont(theFont);
		NumVariable theVariable = getNumVariable();
		int decDigits = theVariable.getMaxDecimals();
		int leftDigits = theVariable.getMaxLeftDigits();
		while (theFont.getSize() > 5 && !variateFits(g.getFontMetrics(), decDigits, leftDigits, displaySize)) {
			theFont = new Font(theFont.getName(), theFont.getStyle(), theFont.getSize() - 1);
			g.setFont(theFont); 
		}
		topBorder = Math.min(kMaxTopBorder, (displaySize.height - 2 - theVariable.noOfValues() * lineHt) / 2);
		
		theVariable.setDecimals(decDigits);		//	to make sure all have same decimals
		int maxWidth = theVariable.getMaxWidth(g);
		rightPos = (displaySize.width + maxWidth) / 2;
	}
	
	private boolean variateFits(FontMetrics fMetrics, int decDigits, int leftDigits, Dimension displaySize) {
//		int noOfChars = leftDigits + decDigits + ((decDigits > 0) ? 1 : 0);
//		int digitWidth = fMetrics.charWidth('0');
//		int valueWidth = noOfChars * digitWidth;
		
		int availForLine = (displaySize.height -  2) / getNumVariable().noOfValues();
		int minHt = fMetrics.getAscent() + kMinValueGap;
		int maxHt = fMetrics.getAscent() + kMaxValueGap;
		
		if (availForLine < minHt)
			return false;
		
		lineHt = Math.min(Math.max(availForLine, minHt), maxHt);
		
		baselineOffset = fMetrics.getAscent() + (lineHt - fMetrics.getAscent()) / 2;
		return true;
	}
	
	public void paintView(Graphics g) {
		if (fontInitialised)
			g.setFont(theFont);
		else
			initialiseFont(g);
		
		paintValues(g, false);
		g.draw3DRect(0, 0, getSize().width - 1, getSize().height - 1, false);
	}
	
	public void paintValues(Graphics g, boolean eraseFirst) {
		NumVariable theVariable = getNumVariable();
//		int vertPos = baselineOffset;
//		int horizPos = kLeftRightBorder + offsetOfPoint;
//		int noOfDecs = theVariable.getMaxDecimals();
		
		int index = 0;		//		0 for 1st row
		
		g.setFont(theFont);
		
		ValueEnumeration e = theVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (eraseFirst || nextSel) {
//				int leftPos = kLeftRightBorder / 2;
				int topPos = topBorder + index*lineHt + 1;
				if (nextSel) {
					Color textColor = g.getColor();
					g.setColor(Color.yellow);
					g.fillRect(1, topPos, getSize().width-2, lineHt);
					g.setColor(textColor);
				}
//				else
//					g.clearRect(1, topPos, getSize().width-2, lineHt);
			}
			nextVal.drawLeft(g, rightPos, topBorder + baselineOffset + index*lineHt);
//			nextVal.drawAtPoint(g, noOfDecs, kLeftRightBorder + offsetOfPoint,
//														topBorder + baselineOffset + index*lineHt);
			index++;
		}
	}

//-----------------------------------------------------------------------------------
	
	protected Rectangle getItemRect(int itemIndex) {
		return new Rectangle(0, topBorder + itemIndex * lineHt, getSize().width, lineHt);
	}
	

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int index = (y - topBorder) / lineHt;
		int nVals = getNumVariable().noOfValues();
		if (index < 0 && y > -lineHt/2)
			index = 0;							//	allow a little slop at the top
		else if (index == nVals && y < nVals * lineHt + lineHt/2)
			index = nVals - 1;			//	allow a little slop at the bottom
		
		if (index < 0 || index >= nVals)
			return null;
		boolean topOfRect = y - index * lineHt < lineHt / 2;
		return new ListPosInfo(index, topOfRect);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else {
			int listHitIndex = ((ListPosInfo)startInfo).itemIndex;
			getData().setSelection(listHitIndex);
		}
		return true;
	}
	
}
