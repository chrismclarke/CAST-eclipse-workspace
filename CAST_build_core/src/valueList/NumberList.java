package valueList;

import java.awt.*;
import dataView.*;

public class NumberList extends DataView {
	private boolean fontInitialised;
	private Font theFont;
	
	protected int topBorder;
	protected int lineHt;
	protected int baselineOffset;
	protected int leftRightBorder;
	protected int columnWidth;
	protected int offsetOfPoint;
	protected int maxRows;
	protected int colsUsed;
	
//	static public final String NUMBER_LIST = "numberList";
	
	
	public NumberList(DataSet theData, XApplet applet) {
		super(theData, applet, null);
		leftRightBorder = 20;
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
	}
	
	private boolean variateFits(FontMetrics fMetrics, int decDigits, int leftDigits, Dimension displaySize) {
		int noOfChars = leftDigits + decDigits + ((decDigits > 0) ? 1 : 0);
		int digitWidth = fMetrics.charWidth('0');
		int valueWidth = noOfChars * digitWidth;
		columnWidth = 2*leftRightBorder + valueWidth;
		lineHt = fMetrics.getHeight();
		topBorder = lineHt;										//		for variable name
		baselineOffset = fMetrics.getAscent() + fMetrics.getLeading();
		int maxCols = Math.max(displaySize.width / columnWidth, 1);
		maxRows = (displaySize.height - topBorder) / lineHt;
		colsUsed = 1 + (getNumVariable().noOfValues() - 3) / (maxRows - 2);	//	Two values are lost for linking arrows
		if (colsUsed > maxCols)
			return false;
		else {
			offsetOfPoint = leftDigits * digitWidth;
			return true;
		}
	}
	
	public void paintView(Graphics g) {
		if (fontInitialised)
			g.setFont(theFont);
		else
			initialiseFont(g);
		
		NumVariable theVariable = getNumVariable();
		g.drawString(theVariable.name, 5, baselineOffset);
		
		paintValues(g, null, false);
		g.draw3DRect(0, 0, getSize().width - 1, getSize().height - 1, false);
	}
	
	public void paintValues(Graphics g, boolean doPaint[], boolean eraseFirst) {
		NumVariable theVariable = getNumVariable();
//		int vertPos = baselineOffset + topBorder;
//		int horizPos = leftRightBorder + offsetOfPoint;
		int noOfDecs = theVariable.getMaxDecimals();
		
		int rowIndex = 0;		//		0 for 1st row
		int colIndex = 0;		//		0 for 1st col
		int itemIndex = 0;
		
		g.setFont(theFont);
		
		ValueEnumeration e = theVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			boolean drawItem = (doPaint == null) || doPaint[itemIndex];
			if (rowIndex >= maxRows - 1 && e.hasMoreValues()) {		//	need to move to new column
				if (drawItem) {
					int x2 = (colIndex + 1) * columnWidth;
					int x1 = x2 - columnWidth / 2;
					int x3 = x1 + columnWidth;
					int y1 = topBorder + lineHt / 2;
					int y2 = y1 + (maxRows - 1) * lineHt;
					Color textColor = g.getColor();
					g.setColor(Color.blue);
					g.drawLine(x1, y2 - 3, x1, y2);
					g.drawLine(x1, y2, x2, y2);
					g.drawLine(x2, y2, x2, y1);
					g.drawLine(x2, y1, x3, y1);
					g.drawLine(x3, y1, x3, y1 + 3);
					g.setColor(textColor);
				}
				rowIndex = 1;
				colIndex++;
			}
			if (drawItem) {
				if (eraseFirst || nextSel) {
					int leftPos = leftRightBorder / 2 + colIndex*columnWidth;
					int topPos = topBorder + rowIndex*lineHt;
					if (nextSel) {
						Color textColor = g.getColor();
						g.setColor(Color.yellow);
						g.fillRect(leftPos, topPos, columnWidth - leftRightBorder, lineHt);
						g.setColor(textColor);
					}
					else
						g.clearRect(leftPos, topPos, columnWidth - leftRightBorder, lineHt);
				}
				nextVal.drawAtPoint(g, noOfDecs, leftRightBorder + offsetOfPoint + colIndex*columnWidth,
														baselineOffset + topBorder + rowIndex*lineHt);
			}
			rowIndex++;
			itemIndex++;
		}
	}
	
	public void paintValues(Graphics g, int lowIndex, int highIndex, boolean eraseFirst) {
		boolean redraw[] = new boolean[getNumVariable().noOfValues()];
		for (int i=lowIndex ; i<=highIndex ; i++)
			redraw[i] = true;
		paintValues(g, redraw, eraseFirst);
	}

//-----------------------------------------------------------------------------------
	
	protected Rectangle getItemRect(int itemIndex) {
		int colIndex = 0;
		int rowIndex = 0;
		if (itemIndex > 0) {
			colIndex = Math.min((itemIndex - 1) / (maxRows - 2), colsUsed - 1);
			rowIndex = itemIndex - colIndex * (maxRows - 2);
		}
		return new Rectangle(colIndex * columnWidth + leftRightBorder/2, topBorder + rowIndex * lineHt,
																columnWidth - leftRightBorder, lineHt);
	}
	
	protected void doChangeSelection(Graphics g) {
		paintValues(g, null, true);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int rowIndex = (y - topBorder) / lineHt;
		if (rowIndex < 0 && y > topBorder - lineHt/2)
			rowIndex = 0;							//	allow a little slop at the top
		else if (rowIndex == maxRows && y < topBorder + maxRows * lineHt + lineHt/2)
			rowIndex = maxRows - 1;				//	allow a little slop at the bottom
		
		int colIndex = x / columnWidth;
		if (rowIndex < 0 || rowIndex >= maxRows || colIndex >= colsUsed)
			return null;				//	outside rectangle of data array
		int posInCol = x - colIndex * columnWidth;
		if ((posInCol < leftRightBorder / 2) || (posInCol > columnWidth - leftRightBorder / 2))
			return null;				//	not close enough to values in column
		
		if (rowIndex == 0 && colIndex > 0)				//	hit in top 'continuation' arrow
			if (y > topBorder + lineHt/2)
				rowIndex = 1;
			else
				return null;
		else if (rowIndex == maxRows - 1 && colIndex < colsUsed - 1)
																	//	hit in bottom 'continuation' arrow
			if (y < topBorder + maxRows * lineHt - lineHt/2)
				rowIndex = maxRows - 2;
			else
				return null;
		int itemIndex = colIndex * (maxRows - 2) + rowIndex;
		if (itemIndex >= getNumVariable().noOfValues())
			return null;				//	after final value in list
		boolean topOfRect = (y - topBorder) - rowIndex * lineHt < lineHt / 2;
		return new ListPosInfo(itemIndex, topOfRect);
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
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		startDrag(endPos);
	}
	
}
