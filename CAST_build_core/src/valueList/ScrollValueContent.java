package valueList;

import java.awt.*;
import java.util.*;
import dataView.*;


public class ScrollValueContent extends DataView {
//	static public final String SCROLL_LIST = "scrollList";
	
	static public final int kLeftRightBorder = 10;
	static public final int kValueGap = 15;
	
	static protected final int kTopBottomBorder = 3;
	static private final int kMinLinesVisible = 5;
	
	static protected final int kStepsPerValue = 6;
	
	static final private Color kVarHiliteColor = new Color(0x66FFCC);		//		pale green
	static final public Color kRowColHiliteColor = new Color(0x66FF00);	//		yellowGreen
	static final private Color kListFrameColor = new Color(0x999999);
	
	private Color selectedRowColor = Color.yellow;
	private Color selectedColColor = kVarHiliteColor;
	private Color selectedRowColColor = kRowColHiliteColor;
	
	private ScrollValueList listPanel;
	
	protected Vector varColumns;
	private String sortKey = null;
	protected boolean smallFirst = true;
	private boolean initialised = false;
	private boolean canSelectRows = true;
	
	protected int selectedCol1 = -1;
	protected int selectedCol2 = -1;
	
	protected int lineHt;
	protected int baselineOffset;
	
	private int bestWidth;
	private int minHeight;
	
	protected int topIndex = 0;
//	protected int scrollAmount;
	private boolean pendingScrollToEnd = false;
	
	
	public ScrollValueContent(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, null);
		lockBackground(Color.white);
		this.listPanel = listPanel;
		varColumns = new Vector();
		repaint();
	}
	
	public void addVariableToList(String newKey, int displayType) {
		varColumns.addElement(new ListColumn(getData(), newKey, displayType));
	}
	
	public void sortByVariable(String varKey, boolean smallFirst) {
		sortKey = varKey;
		this.smallFirst = smallFirst;
	}
	
	public void setSelectedCols(int selectedCol1, int selectedCol2) {
		this.selectedCol1 = selectedCol1;
		this.selectedCol2 = selectedCol2;
		repaint();
	}
	
	public void setSelectedCols(String selectedKey1, String selectedKey2) {
		setSelectedCols(findIndex(selectedKey1), findIndex(selectedKey2));
	}
	
	public void resetColumns() {
		initialised = false;
		invalidate();
	}
	
	public void setSelectionColors(Color selectedRowColor, Color selectedColColor,
																				Color selectedRowColColor) {
		this.selectedRowColor = selectedRowColor;
		if (selectedColColor != null)
			this.selectedColColor = selectedColColor;
		if (selectedRowColColor != null)
			this.selectedRowColColor = selectedRowColColor;
	}
	
	private int findIndex(String varKey) {
		for (int i=0 ; i<varColumns.size() ; i++) {
			ListColumn col = (ListColumn)varColumns.elementAt(i);
			if (col.getVarKey().equals(varKey))
				return i;
		}
		return -1;
	}
	
	public void setCanSelectRows(boolean canSelectRows) {
		this.canSelectRows = canSelectRows;
	}
	
	protected void setTopIndex(int index) {
		topIndex = index;
		repaint();
	}
	
	protected int extraLineSpacing() {
		return 0;
	}
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		FontMetrics fm = g.getFontMetrics();
		lineHt = fm.getHeight() + extraLineSpacing();
//		scrollAmount = lineHt / kStepsPerValue;
		baselineOffset = fm.getAscent() + fm.getLeading() + extraLineSpacing() / 2;
		
		int leftPos = kLeftRightBorder;
		boolean firstCol = true;
		Enumeration e = varColumns.elements();
		while (e.hasMoreElements()) {
			if (!firstCol)
				leftPos += kValueGap;
			ListColumn yColumn = (ListColumn)e.nextElement();
			int width = yColumn.findWidth(g);
			leftPos += width;
			firstCol = false;
		}
		bestWidth = leftPos + kLeftRightBorder;
		minHeight = 2 * kTopBottomBorder + kMinLinesVisible * lineHt;
		
		initialised = true;
		return true;
	}

	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		if (g == null || getData() == null)
			return new Dimension(20, 20);
		initialise(g);
		return new Dimension(bestWidth, minHeight);
	}

	public Dimension getPreferredSize() {
		Graphics g = getGraphics();
		if (g == null || getData() == null)
			return new Dimension(20, 20);
		initialise(g);
		
		Flags selection = getSelection();
		int nValues = selection.getNoOfFlags();
		int totalHeight = 2 * kTopBottomBorder + nValues * lineHt;
		return new Dimension(bestWidth, totalHeight);
//		return getMinimumSize();
	}
	
	private int maxVisibleInList() {
		initialise(getGraphics());

		return (getSize().height - 2 * kTopBottomBorder) * kStepsPerValue / lineHt + 1;
	}

	/*
	private int valuesForwardInList(Flags selection) {
		initialise(getGraphics());
		int maxVisible = maxVisibleInList();
		
		int noForwardInList = selection.getNoOfFlags() * kStepsPerValue - topIndex;
		return Math.min(maxVisible, noForwardInList);
	}
*/
	
	public int findExtraColumnnGap() {
		if (varColumns.size() == 1)
			return 0;
		else
			return (getSize().width - bestWidth) / (varColumns.size() - 1);
	}
	
	private Rectangle getColumnRect(int colIndex) {
//		int hiliteLeft = 0;
//		int hiliteWidth = 0;
		int extraGap = findExtraColumnnGap();
		int leftPos = kLeftRightBorder;
		int valueGap = extraGap + kValueGap;
		boolean firstCol = true;
		Enumeration e = varColumns.elements();
		int index = 0;
		while (e.hasMoreElements()) {
			if (!firstCol)
				leftPos += valueGap;
			ListColumn yColumn = (ListColumn)e.nextElement();
			if (colIndex == index) 
				return new Rectangle(leftPos - valueGap / 2, 1, 
												yColumn.getColumnWidth() + valueGap, getSize().height - 2);
			leftPos += yColumn.getColumnWidth();
			firstCol = false;
			index ++;
		}
		return null;
	}
	
	public void scrollToEnd() {
		pendingScrollToEnd = true;
	}
	
	private void localScrollToEnd() {
		Flags selection = getSelection();
		
		int noOfValues = selection.getNoOfFlags() * kStepsPerValue + 1;
		int maxVisible = maxVisibleInList();
		
		topIndex = Math.max(0, noOfValues - maxVisible);
		
		if (listPanel != null)
			listPanel.initialiseScrollbar(noOfValues, maxVisible, topIndex);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		if (pendingScrollToEnd) {
			localScrollToEnd();
			pendingScrollToEnd = false;
		}
		
		Flags selection = getSelection();
		
		int noOfValues = selection.getNoOfFlags() * kStepsPerValue + 1;
//		int noForward = valuesForwardInList(selection);
		int maxVisible = maxVisibleInList();
		if (topIndex > 0 && (topIndex + maxVisible > noOfValues))
			topIndex = Math.max(0, noOfValues - maxVisible);
		if (listPanel != null)
			listPanel.initialiseScrollbar(noOfValues, maxVisible, topIndex);
		
		int sortedIndex[] = null;
		if (sortKey != null) {
			NumVariable v = (NumVariable)getVariable(sortKey);
			sortedIndex = v.getSortedIndex();
		}
		
		Rectangle hiliteColRect1 = getColumnRect(selectedCol1);
		Rectangle hiliteColRect2 = getColumnRect(selectedCol2);
		
		if (hiliteColRect1 != null) {
			g.setColor(getSelectedColColor());
			g.fillRect(hiliteColRect1.x, hiliteColRect1.y, hiliteColRect1.width, hiliteColRect1.height);
			g.setColor(getForeground());
		}
		if (hiliteColRect2 != null) {
			g.setColor(getSelectedColColor());
			g.fillRect(hiliteColRect2.x, hiliteColRect2.y, hiliteColRect2.width, hiliteColRect2.height);
			g.setColor(getForeground());
		}
		
		drawValues(g, selection, sortedIndex);
		
		drawBox(g, hiliteColRect1, hiliteColRect2);
	}
	
	private void drawBox(Graphics g, Rectangle hiliteColRect1, Rectangle hiliteColRect2) {
		g.setColor(kListFrameColor);
		g.drawRect(0, 0, getSize().width, getSize().height - 1);	//		right line is clipped
		
		g.setColor(getBackground());
		g.drawRect(1, 1, getSize().width - 2, getSize().height - 3);
															//		1 white pixel on all sides
		if (hiliteColRect1 != null) {
			g.setColor(getSelectedColColor());
			g.fillRect(hiliteColRect1.x, 1, hiliteColRect1.width, 1);
			g.fillRect(hiliteColRect1.x, getSize().height - 2, hiliteColRect1.width, 1);
			g.setColor(getForeground());
		}
		if (hiliteColRect2 != null) {
			g.setColor(getSelectedColColor());
			g.fillRect(hiliteColRect2.x, 1, hiliteColRect2.width, 1);
			g.fillRect(hiliteColRect2.x, getSize().height - 2, hiliteColRect2.width, 1);
			g.setColor(getForeground());
		}
	}
	
	protected Color getSelectedRowColor() {
		return selectedRowColor;
	}
	
	protected Color getSelectedColColor() {
		return selectedColColor;
	}
	
	protected Color getSelectedRowColColor() {
		return selectedRowColColor;
	}
	
	protected boolean isSelectedRow(Flags selection, int dataIndex) {
		return selection.valueAt(dataIndex);
	}
	
	private boolean isSelectedCol(int colIndex) {
		return colIndex == selectedCol1 || colIndex == selectedCol2;
	}
	
	protected void drawValues(Graphics g, Flags selection, int[] sortedIndex) {
		int noOfValues = selection.getNoOfFlags();
		
		int startIndex = topIndex / kStepsPerValue;
		int lineTop = kTopBottomBorder - topIndex * lineHt / kStepsPerValue + startIndex * lineHt;
		
		while (startIndex < noOfValues && lineTop < getSize().height) {
			int dataIndex = startIndex;
			if (sortedIndex != null) {
				if (!smallFirst)
					dataIndex = noOfValues - dataIndex - 1;
				dataIndex = sortedIndex[dataIndex];
			}
			
			drawDataRow(g, isSelectedRow(selection, dataIndex), dataIndex, lineTop + baselineOffset);
			
			startIndex ++;
			lineTop += lineHt;
		}
	}
	
	protected Font setValueFormat(Graphics g, int rowIndex, boolean selectedCol) {
		return null;
	}
	
	protected Color getRowTextColor(int dataIndex, boolean selectedRow) {
		return getForeground();
	}
	
	protected void drawDataRow(Graphics g, boolean selected, int dataIndex, int baseline, Color textColor) {
		if (selected) {
			g.setColor(getSelectedRowColor());
			g.fillRect(2, baseline - baselineOffset, getSize().width - 3, lineHt);
		}
		
		g.setColor(textColor);
		int extraGap = findExtraColumnnGap();
		int leftPos = kLeftRightBorder;
		int valueGap = extraGap + kValueGap;
		boolean firstCol = true;
		Enumeration e = varColumns.elements();
		int colIndex = 0;
		while (e.hasMoreElements()) {
			if (!firstCol)
				leftPos += valueGap;
			ListColumn yColumn = (ListColumn)e.nextElement();
			Color cellBackground = getBackgroundColor(dataIndex, colIndex, selected);
			if (cellBackground != null) {
				g.setColor(cellBackground);
				g.fillRect(leftPos - valueGap / 2, baseline - baselineOffset,
																yColumn.getColumnWidth() + valueGap, lineHt);
				g.setColor(textColor);
			}
			
			Font oldFont = setValueFormat(g, dataIndex, isSelectedCol(colIndex));
			yColumn.drawValue(g, dataIndex, baseline, leftPos);
			if (oldFont != null)
				g.setFont(oldFont);
			leftPos += yColumn.getColumnWidth();
			firstCol = false;
			colIndex ++;
		}
	}
	
	protected Color getBackgroundColor(int rowIndex, int colIndex, boolean selectedRow) {
		if (isSelectedCol(colIndex))
			return selectedRow ? getSelectedRowColColor() : getSelectedColColor();
		return null;
	}
	
	protected void drawDataRow(Graphics g, boolean selected, int dataIndex, int baseline) {
		drawDataRow(g, selected, dataIndex, baseline, getRowTextColor(dataIndex, selected));
	}
	
	protected int getColumnValueRight(int dataIndex) {
		int extraGap = findExtraColumnnGap();
		int leftPos = kLeftRightBorder;
		int valueGap = extraGap + kValueGap;
		boolean firstCol = true;
		Enumeration e = varColumns.elements();
		int colIndex = 0;
		while (e.hasMoreElements()) {
			if (!firstCol)
				leftPos += valueGap;
			ListColumn yColumn = (ListColumn)e.nextElement();
			leftPos += yColumn.getColumnWidth();
			if (colIndex == dataIndex)
				return leftPos - yColumn.getColumnOffset();
			firstCol = false;
			colIndex ++;
		}
		return 0;
	}
	
	public Vector getListColumns(Graphics g) {
		initialise(g);
		return varColumns;
	}


//-----------------------------------------------------------------------------------
	
	
	protected void doChangeSelection(Graphics g) {
		int firstSelectedIndex = getSelection().findFirstSetFlag();
		if (firstSelectedIndex < 0) {
			repaint();
			return;
		}
		int lastSelectedIndex = getSelection().findLastSetFlag();
		
		if (sortKey != null) {
			NumVariable variable = (NumVariable)getVariable(sortKey);
			int firstIndexInList = variable.noOfValues() - 1;
			int lastIndexInList = 0;
			for (int i=firstSelectedIndex ; i<=lastSelectedIndex ; i++) {
				int indexInList = variable.indexToRank(i);
				indexInList = smallFirst ? indexInList : variable.noOfValues() - indexInList - 1;
				firstIndexInList = Math.min(firstIndexInList, indexInList);
				lastIndexInList = Math.max(lastIndexInList, indexInList);
			}
			firstSelectedIndex = firstIndexInList;
			lastSelectedIndex = lastIndexInList;
		}
//		System.out.println("firstSelectedIndex = " + firstSelectedIndex + ", lastSelectedIndex = " + lastSelectedIndex);
		if (!scrollToIndex(firstSelectedIndex, lastSelectedIndex))
			repaint();
	}
	
	protected boolean scrollToIndex(int firstIndexInList, int lastIndexInList) {
		if (lineHt == 0)		//		not yet initialised
			return false;
		int startIndex = topIndex / kStepsPerValue;
		int lineTop = kTopBottomBorder - topIndex * lineHt / kStepsPerValue + startIndex * lineHt;
		int visibleRows = (getSize().height - lineTop) / lineHt;
		
		if (firstIndexInList < startIndex || (lastIndexInList - firstIndexInList + 1) > visibleRows ) {
			if (listPanel != null)
				listPanel.scrollToIndex(firstIndexInList * kStepsPerValue);
			return true;
		}
		
		if (lastIndexInList >= startIndex + visibleRows) {
			if (listPanel != null)
				listPanel.scrollToIndex((lastIndexInList - visibleRows + 1) * kStepsPerValue);
			return true;
		}
		return false;
	}


//-----------------------------------------------------------------------------------

	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return canSelectRows;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y < kTopBottomBorder || y >= getSize().height - kTopBottomBorder
																				|| x < 0 || x >= getSize().width)
			return null;
		
		int scrollIndex = (y - kTopBottomBorder) * kStepsPerValue / lineHt + topIndex;
		int hitIndex = scrollIndex / kStepsPerValue;
		Flags selection = getSelection();
		if (hitIndex >= selection.getNoOfFlags())
			return null;
		else {
			if (sortKey != null) {
				NumVariable variable = (NumVariable)getVariable(sortKey);
				if (!smallFirst)
					hitIndex = variable.noOfValues() - hitIndex - 1;
				hitIndex = variable.rankToIndex(hitIndex);
			}
			return new IndexPosInfo(hitIndex);
		}
	}
}
