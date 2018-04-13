package matrix;

import java.awt.*;

import dataView.*;



abstract public class CoreMatrixView extends DataView {
	static final public Color kSelectedBackground = new Color(0xFFFF66);		//	pale yellow
	
	static final public int kRowGap = 4;
	static final private int kRightBorder = 6;
	static final private int kTopBottomBorder = 2;
	
	static final private int kCurlyBracketWidth = 8;
	
	static private void drawCurlyBracket(int top, int bottom, int right, Graphics g) {
		int center = (top + bottom) / 2;
		g.drawLine(right - 2, top, right - 3, top);
		g.drawLine(right - 3, top, right - 4, top + 1);
		
		g.drawLine(right - 4, top + 1, right - 4, center - 2);
		g.drawLine(right - 4, center - 2, right - 6, center);
		g.drawLine(right - 6, center, right - 4, center + 2);
		g.drawLine(right - 4, center + 2, right - 4, bottom - 2);
		
		g.drawLine(right - 4, bottom - 2, right - 3, bottom - 1);
		g.drawLine(right - 3, bottom - 1, right - 2, bottom - 1);
	}
	
	private boolean allowRowSelection = false;
	
	protected ModelTerm columns[];
	
	private String groupKey = null;
	
	protected int ascent, descent;
	
	protected int headingHt, noOfRows, contentWidth, contentHeight;
	private int leftBorder = kRightBorder;
	private int rowGap = kRowGap;
	
	private boolean initialised = false;
	
	public CoreMatrixView(DataSet theData, XApplet applet) {
		super(theData, applet, new Insets(0,0,0,0));
		setRetainLastSelection(true);
	}
	
	public void addMatrixColumn(ModelTerm column) {
		if (columns == null)
			columns = new ModelTerm[1];
		else {
			ModelTerm temp[] = columns;
			columns = new ModelTerm[temp.length + 1];
			System.arraycopy(temp, 0, columns, 0, temp.length);
		}
		columns[columns.length - 1] = column;
	}
	
	public void setMatrixColumns(ModelTerm[] columns) {
		this.columns = columns;
	}
	
	public void setAllowRowSelection(boolean allowRowSelection) {
		this.allowRowSelection = allowRowSelection;
	}
	
	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}
	
	public void setRowGap(int rowGap) {
		this.rowGap = rowGap;
	}
	
//----------------------------------
	
	protected void doInitialisation(Graphics g) {
		ascent = g.getFontMetrics().getAscent();
		descent = g.getFontMetrics().getDescent();
		
		noOfRows = getNoOfRows(g);
		
		headingHt = getHeadingHeight(g);
		contentWidth = getContentWidth(g);
		contentHeight = noOfRows * (ascent + descent) + (noOfRows + 1) * rowGap;
		
		if (groupKey != null) {
			CatVariable groupVar = (CatVariable)getVariable(groupKey);
			leftBorder = kRightBorder + kCurlyBracketWidth + groupVar.getMaxWidth(g);
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
	
//----------------------------------
	
	abstract protected int getContentWidth(Graphics g);
	
	abstract protected int getNoOfRows(Graphics g);
	
	abstract protected boolean isSelectedRow(int row);
	
//----------------------------------
	
	abstract protected int getHeadingHeight(Graphics g);
	
	abstract protected void drawHeading(int horiz, int bottom, Graphics g);
	
	abstract protected void drawMatrixRow(int row, boolean selected, int horiz,
																															int baseline, Graphics g);
	
//----------------------------------

	protected Color getBackgroundColor() {
		return Color.white;
	}
	
	private void drawBraces(Graphics g, int localRowGap) {
		initialise(g);
		int bottom = headingHt + noOfRows * (ascent + descent)
												+ (noOfRows + 1) * localRowGap + 2 * kTopBottomBorder;
		int left = leftBorder - kRightBorder;
		int right = contentWidth + leftBorder + kRightBorder;
		
		g.drawLine(left, headingHt, left + 3, headingHt);
		g.drawLine(left, headingHt, left, bottom - 1);
		g.drawLine(left, bottom - 1, left + 3, bottom - 1);
		
		g.drawLine(right - 1, headingHt, right - 4, headingHt);
		g.drawLine(right - 1, headingHt, right - 1, bottom - 1);
		g.drawLine(right - 1, bottom - 1, right - 4, bottom - 1);
		
		if (groupKey != null) {
			CatVariable groupVar = (CatVariable)getVariable(groupKey);
			int counts[] = groupVar.getCounts();
			int topRow = 0;
			for (int i=0 ; i<counts.length ; i++)
				if (counts[i] > 0) {
//					int bottomRow = topRow + counts[i];
					int topPix = headingHt + localRowGap / 2 + 4 + topRow * (localRowGap + ascent + descent);
					int bottomPix = topPix + counts[i] * (localRowGap + ascent + descent) - 2;
					drawCurlyBracket(topPix, bottomPix, left, g);
					
					int baseline = (topPix + bottomPix) / 2 + (ascent - descent) / 2;
					groupVar.getLabel(i).drawLeft(g, left - kCurlyBracketWidth, baseline);
					
					topRow += counts[i];
				}
		}
	}
	
	private void drawBackground(Graphics g, int localRowGap) {
		int bottom = headingHt + 2 * kTopBottomBorder + noOfRows * (ascent + descent) + (noOfRows + 1) * localRowGap;
		int right = contentWidth + leftBorder + kRightBorder;
		int left = leftBorder - kRightBorder;
		
		g.setColor(getBackgroundColor());
		g.fillRect(left + 1, headingHt + 1, right - left - 1, bottom - headingHt - 2);
		
		Flags flags = getSelection();
		for (int row=0 ; row<flags.getNoOfFlags() ; row++)
			if (flags.valueAt(row)) {
				g.setColor(kSelectedBackground);
				int baseline = headingHt + kTopBottomBorder + localRowGap + ascent;
				baseline += row * (localRowGap + ascent + descent);
				g.fillRect(left + 1, baseline - ascent - localRowGap,
													right - left - 1, ascent + descent + 2 * localRowGap);
			}
	}
	
	private int getLocalRowGap(Graphics g) {
		Dimension bestDim = getMinimumSize(g);
		if (bestDim.height > getSize().height) {
			int difference = (getSize().height - headingHt - 2 * kTopBottomBorder - noOfRows * (ascent + descent));
			int bestRowGap = difference / (noOfRows + 1);
			if (difference < 0)
				bestRowGap --;
			
			return bestRowGap;
		}
		else
			return rowGap;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		Color normalColor = getForeground();
		Font normalFont = g.getFont();
		Font boldFont = new Font(normalFont.getName(), Font.BOLD, normalFont.getSize());
		
		int localRowGap = getLocalRowGap(g);
		
//		int selectedRow = getSelectedRow();
		
		drawBackground(g, localRowGap);
		
		g.setColor(normalColor);
		drawHeading(leftBorder, headingHt, g);
		
		g.setColor(normalColor);
		int baseline = headingHt + kTopBottomBorder + localRowGap + ascent;
		for (int row=0 ; row<noOfRows ; row++) {
			boolean selected = isSelectedRow(row);
			if (selected)
				g.setFont(boldFont);
			
			drawMatrixRow(row, selected, leftBorder, baseline, g);
			
			if (selected)
				g.setFont(normalFont);
			baseline += localRowGap + ascent + descent;
		}
		
		g.setColor(normalColor);
		drawBraces(g, localRowGap);
	}

//-----------------------------------------------------------------------------------
	
	private Dimension getMinimumSize(Graphics g) {
		initialise(g);
		
		return new Dimension(contentWidth + leftBorder + kRightBorder,
																		headingHt + contentHeight + 2 * kTopBottomBorder);
	}
	
	public Dimension getMinimumSize() {
		return getMinimumSize(getGraphics());
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return allowRowSelection;
	}
	
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < leftBorder || x >= getSize().width - kRightBorder
																								|| y < headingHt || y >= getSize().height)
			return null;
		
		int localRowGap = getLocalRowGap(getGraphics());
		int row = (y - headingHt - kTopBottomBorder - localRowGap / 2) / (localRowGap + ascent + descent);
		
		if (row < 0 || row >= noOfRows)
			return null;
		
		return new IndexPosInfo(row);
	}
}