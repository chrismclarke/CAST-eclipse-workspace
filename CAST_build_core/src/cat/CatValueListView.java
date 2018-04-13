package cat;

import java.awt.*;

import dataView.*;


public class CatValueListView extends DataView {
//	static public final String CAT_VALUE_LIST = "catValueList";
	
	static private final int kLeftRightBorder = 10;
	static private final int kTopBottomBorder = 3;
	static private final int kMinValueGap = 15;
	static private final int kLineGap = 4;
	
	private String catKey;
	private CoreCreateTableView freqTableView;
	private int nRows, nCols;
	
	private boolean initialised = false;
	
	private CatSelection clickSelection;
	private int clickedOffset = 0;
	private int conditCount;
	
	private Font boldFont;
	private int ascent, descent;
	private int leftRightBorder, valueGap, maxWidth, lineGap;
	
	private boolean canSelectValues = true;
	
	public CatValueListView(DataSet theData, XApplet applet, String catKey,
																					CoreCreateTableView freqTableView, int nCols) {
		super(theData, applet, new Insets(0,0,0,0));
		this.catKey = catKey;
		this.freqTableView = freqTableView;
		this.nCols = nCols;
		boldFont = applet.getStandardBoldFont();
	}
	
	public void setCanSelectValues(boolean canSelectValues) {
		this.canSelectValues = canSelectValues;
	}
	
	public void clearHilite() {
		clickSelection.selectedVal = -1;
		repaint();
	}
	
	public void resetList() {
		clickSelection.resetList();
		repaint();
		
		freqTableView.clearCounts();
	}
	
	public void completeTable() {
		clickSelection.completeList();
		repaint();
		
		freqTableView.completeCounts();
	}
	
	public int numberCompleted() {
		return clickSelection.numberCompleted();
	}
	
	public void setCommonValuesClicked(CatSelection clickSelection) {
		this.clickSelection = clickSelection;		//		can allow this and CatValueScrollList to share same array
	}
	
	public void setConditOffset(int clickedOffset, int conditCount) {
		this.clickedOffset = clickedOffset;	//		starts from different offset (for condit list)
		this.conditCount = conditCount;			//		assumes condit values are contiguous
	}
	
	protected ValueEnumeration getEnumeration(CatVariable catVar) {
		return catVar.values();
	}
	
	protected int noOfValues(CatVariable catVar) {
		return catVar.noOfValues();
	}
	
	protected int translateHitIndex(int hitIndex) {
		return hitIndex;
	}
	
	private void selectValue(int index) {
		clickSelection.selectIndex(index);
		repaint();
		
		freqTableView.addCatValue(translateHitIndex(index));
	}
	
	private int getBestWidth(Graphics g) {
		CatVariable catVar = (CatVariable)getVariable(catKey);
		maxWidth = catVar.getMaxWidth(g);
		return maxWidth * nCols + 2 * kLeftRightBorder + (nCols - 1) * kMinValueGap;
	}
	
	private void setupValueClickedArray() {
		CatVariable catVar = (CatVariable)getVariable(catKey);
		int nVals = noOfValues(catVar);
		nRows = (nVals - 1) / nCols + 1;
		
		if (clickSelection == null)
			clickSelection = new CatSelection(catVar.noOfValues());
		
		conditCount = nVals;
	}
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		setupValueClickedArray();
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		int horizError = getSize().width - getBestWidth(g);
		leftRightBorder = (horizError <= 0) ? kLeftRightBorder
																				: kLeftRightBorder + horizError / (2 * nCols); 
		valueGap = (horizError <= 0) ? kMinValueGap + horizError / (nCols - 1)
																					: kMinValueGap + horizError / nCols;
		
		int minHeight = getMinimumSize().height;
		lineGap = kLineGap + (int)Math.round(Math.floor((getSize().height - minHeight) / (double)nRows));
		
		initialised = true;
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CatVariable catVar = (CatVariable)getVariable(catKey);
		ValueEnumeration e = getEnumeration(catVar);
		
		int index = clickedOffset;
		int row = 0;
//		int col = 0;
		int baseline = kTopBottomBorder + ascent;
		int valueLeft = kLeftRightBorder;
		
		while (e.hasMoreValues()) {
			LabelValue nextCat = (LabelValue)e.nextValue();
			if (index == clickSelection.selectedVal) {
				g.setColor(Color.yellow);
				g.fillRect(valueLeft - valueGap / 2, baseline - ascent - lineGap,
															maxWidth + valueGap, ascent + descent + 2 * lineGap);
				
				Font standardFont = g.getFont();
				g.setFont(boldFont);
				g.setColor(Color.red);
				nextCat.drawRight(g, valueLeft, baseline);
				g.setFont(standardFont);
			}
			else if (clickSelection.valueClicked[index]) {
				g.setColor(Color.lightGray);
				nextCat.drawRight(g, valueLeft, baseline);
			}
			else {
				g.setColor(getForeground());
				nextCat.drawRight(g, valueLeft, baseline);
			}
			
			row ++;
			baseline += (ascent + descent + lineGap);
			if (row >= nRows) {
				row = 0;
				valueLeft += (maxWidth + valueGap);
				baseline = kTopBottomBorder + ascent;
			}
//			col ++;
//			valueLeft += (maxWidth + valueGap);
//			if (col >= nCols) {
//				col = 0;
//				baseline += (ascent + descent + lineGap);
//				valueLeft = kLeftRightBorder;
//			}
			index ++;
		}
	}

//-----------------------------------------------------------------------------------

	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		if (g == null)
			return new Dimension(20, 20);
		
		setupValueClickedArray();
		
		FontMetrics fm = g.getFontMetrics();
		
		int bestWidth = getBestWidth(g);
		int minHeight = 2 * kTopBottomBorder + nRows * (fm.getAscent() + fm.getDescent())
																										+ (nRows - 1) * kLineGap;
		
		return new Dimension(bestWidth, minHeight);
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return canSelectValues;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y < kTopBottomBorder || y >= getSize().height - kTopBottomBorder
												|| x < leftRightBorder || x >= getSize().width - leftRightBorder)
			return null;
		
		int col = (x - leftRightBorder + valueGap / 2) / (maxWidth + valueGap);
		int row = (y - kTopBottomBorder + lineGap / 2) / (ascent + descent + lineGap);
		int hitIndex = row + col * nRows;
//		int hitIndex = col + row * nCols;
		
		if (hitIndex < conditCount && !clickSelection.valueClicked[clickedOffset + hitIndex])
			return new IndexPosInfo(clickedOffset + hitIndex);
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		IndexPosInfo indexPos = (IndexPosInfo)startInfo;
		selectValue(indexPos.itemIndex);
		
		getApplet().notifyDataChange(this);
		
		return false;
	}
	
}