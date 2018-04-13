package exerciseCateg;

import java.awt.*;

import dataView.*;


abstract public class CoreTwoWayTableView extends DataView {
	static final public int LEFT = 0;
	static final public int CENTER = 1;
	
	protected String xKey, yKey;
	
	private boolean initialised = false;
	
	protected int nYCats, nXCats;
	
	protected Font standardFont, boldFont;
	protected int ascent, descent, labelAscent, labelDescent;
	
	protected int topLabelHeight, cellHeight, bottomMarginHeight;
	protected int leftLabelWidth, cellWidth, rightMarginWidth, cellValueInset;
	
	public CoreTwoWayTableView(DataSet theData, XApplet applet, String yKey, String xKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.yKey = yKey;
	}
	
	public void resetLayout() {
		initialised = false;
		invalidate();
		repaint();
	}

//---------------------------------------------------------------------------------
	
	abstract protected Dimension getMaxTopLabelSize(Graphics g);
	abstract protected Dimension getMaxLeftLabelSize(Graphics g);
	abstract protected Dimension getMaxCellSize(Graphics g);
	abstract protected Dimension getMaxRightMarginSize(Graphics g);
	abstract protected Dimension getMaxBottomMarginSize(Graphics g);
	abstract protected Dimension getTotalSize(Graphics g);
	
	abstract public int[][] getCounts();				//	xVar.getCounts(yVar);
	
	abstract protected void drawTopLabel(Graphics g, int i, Rectangle cell);
	abstract protected void drawLeftLabel(Graphics g, int i, Rectangle cell);
	
	abstract protected void drawCell(Graphics g, int i, int j, Rectangle cell, int[][] counts);
	
	abstract protected void drawRightMargin(Graphics g, int i, Rectangle cell, int[][] counts);
	abstract protected void drawBottomMargin(Graphics g, int i, Rectangle cell, int[][] counts);
	
	abstract protected void drawTotal(Graphics g, Rectangle cell, int[][] counts);
	
	abstract protected void drawCellHighlight(Graphics g);
	
	protected Dimension getMaxCatNameSize(Graphics g, String key, Insets insets) {
		CatVariable yVar = (CatVariable)getVariable(key);
		FontMetrics fm = g.getFontMetrics();
		int height = fm.getAscent() + fm.getDescent();
		int width = 0;
		for (int i=0 ; i<yVar.noOfCategories() ; i++)
			width = Math.max(width, yVar.getLabel(i).stringWidth(g));
		return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
	}
	
	protected void drawCatName(Graphics g, String key, Rectangle cell, Insets insets, int index, int position) {
		CatVariable yVar = (CatVariable)getVariable(key);
		int baseline = getBaseline(g, cell, insets);
		int left = cell.x + insets.left;
		if (position == LEFT)
			yVar.getLabel(index).drawRight(g, left, baseline);
		else {
			int centre = left + (cell.width - insets.left - insets.right) / 2;
			yVar.getLabel(index).drawCentred(g, centre, baseline);
		}
	}
	
	protected void drawOneCount(Graphics g, int count, Rectangle cell, Insets insets) {
		FontMetrics fm = g.getFontMetrics();
		int baseline = getBaseline(g, cell, insets);
		int right = cell.x + cell.width - insets.right;
		String countString = String.valueOf(count);
		int valWidth = fm.stringWidth(countString);
		g.drawString(countString, right - valWidth, baseline);
	}
	
	protected void drawPlusOne(Graphics g, Rectangle cell, Insets insets) {
		int baseline = getBaseline(g, cell, insets);
		int startPos = cell.x + cell.width - insets.right + 2;
		g.drawString("+1", startPos, baseline);
	}
	
	private int getBaseline(Graphics g, Rectangle cell, Insets insets) {
		FontMetrics fm = g.getFontMetrics();
		return cell.y + cell.height - insets.bottom - fm.getDescent();
	}
	
	protected Dimension sizeOfInteger(Graphics g, int count, Insets insets) {
		FontMetrics fm = g.getFontMetrics();
		int height = fm.getAscent() + fm.getDescent();
		int width = fm.stringWidth(String.valueOf(count));
		Dimension size = new Dimension(width, height);
		size.width += (insets.left + insets.right);
		size.height += (insets.top + insets.bottom);
		return size;
	}
	
	protected int xMarginTotal(int[][] counts, int xCat) {
		int total = 0;
		for (int j=0 ; j<counts[0].length ; j++)
			total += counts[xCat][j];
		return total;
	}
	
	protected int yMarginTotal(int[][] counts, int yCat) {
		int total = 0;
		for (int i=0 ; i<counts.length ; i++)
			total += counts[i][yCat];
		return total;
	}
	
	protected int overallTotal(int[][] counts) {
		int total = 0;
		for (int i=0 ; i<counts.length ; i++)
			for (int j=0 ; j<counts[i].length ; j++)
				total += counts[i][j];
		return total;
	}

//---------------------------------------------------------------------------------
	
	protected void doInitialisation(Graphics g) {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		nYCats = yVar.noOfCategories();
		CatVariable xVar = (CatVariable)getVariable(xKey);
		nXCats = xVar.noOfCategories();
		
		standardFont = g.getFont();
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		Dimension cellSize = getMaxCellSize(g);
		Dimension rightMarginSize = getMaxRightMarginSize(g);
		Dimension bottomMarginSize = getMaxBottomMarginSize(g);
		Dimension totalSize = getTotalSize(g);
		
		boldFont = new Font(standardFont.getName(), Font.BOLD, standardFont.getSize());
		g.setFont(boldFont);
		fm = g.getFontMetrics();
		labelAscent = fm.getAscent();
		labelDescent = fm.getDescent();
		
		Dimension topLabelSize = getMaxTopLabelSize(g);
		Dimension leftLabelSize = getMaxLeftLabelSize(g);
		
		topLabelHeight = topLabelSize.height;
		leftLabelWidth = leftLabelSize.width;
		
		g.setFont(standardFont);
		
		cellWidth = cellSize.width;
		if (bottomMarginSize != null)
			cellWidth = Math.max(cellWidth, bottomMarginSize.width);
		cellValueInset = 0;
		if (topLabelSize.width > cellWidth) {
			cellValueInset = (topLabelSize.width - cellWidth) / 2;
			cellWidth = topLabelSize.width;
		}
		cellWidth = Math.max(topLabelSize.width, cellSize.width);
		
		cellHeight = Math.max(leftLabelSize.height, cellSize.height);
		if (rightMarginSize != null)
			cellHeight = Math.max(cellHeight, rightMarginSize.height);
		
		rightMarginWidth = 0;
		if (rightMarginSize != null)
			rightMarginWidth = rightMarginSize.width;
		if (totalSize != null)
			rightMarginWidth = Math.max(rightMarginWidth, totalSize.width);
		
		bottomMarginHeight = 0;
		if (bottomMarginSize != null)
			bottomMarginHeight = bottomMarginSize.height;
		if (totalSize != null)
			bottomMarginHeight = Math.max(bottomMarginHeight, totalSize.height);
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {		
			doInitialisation(g);
			initialised = true;
		}
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int displayHeight = topLabelHeight + 2 + cellHeight * nYCats + bottomMarginHeight;
		int displayWidth = leftLabelWidth + 2 + cellWidth * nXCats + rightMarginWidth;
		
		return new Dimension(displayWidth, displayHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public Rectangle getCellRectangle(int selectedX, int selectedY) {
		Rectangle r = new Rectangle();
		
		if (selectedX >= 0) {
			r.x = leftLabelWidth + 1 + selectedX * cellWidth;
			r.width = cellWidth;
		}
		else {
			r.x = leftLabelWidth + 2 + nXCats * cellWidth;
			r.width = rightMarginWidth;
		}
		
		if (selectedY >= 0) {
			r.y = topLabelHeight + 1 + selectedY * cellHeight;
			r.height = cellHeight;
		}
		else {
			r.y = topLabelHeight + 2 + nYCats * cellHeight;
			r.height = bottomMarginHeight;
		}
		
		return r;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int[][] counts = getCounts();
		
		Rectangle r = new Rectangle(leftLabelWidth + 1, 0, cellWidth, topLabelHeight);
		
		g.setFont(boldFont);
		for (int i=0 ; i<nXCats ; i++) {
			drawTopLabel(g, i, r);
			r.x += cellWidth;
		}
		
		r.setBounds(0, topLabelHeight + 1, leftLabelWidth, cellHeight);
		for (int i=0 ; i<nYCats ; i++) {
			drawLeftLabel(g, i, r);
			r.y += cellHeight;
		}
		
		r.setBounds(leftLabelWidth, topLabelHeight, cellWidth * nXCats + 1, cellHeight * nYCats + 1);
		g.drawRect(r.x, r.y, r.width, r.height);
		g.setColor(Color.white);
		r.setBounds(leftLabelWidth + 1, topLabelHeight + 1, cellWidth * nXCats, cellHeight * nYCats);
		g.fillRect(r.x, r.y, r.width, r.height);
		
		drawCellHighlight(g);
		
		g.setColor(getForeground());
		g.setFont(standardFont);
		
		r.setBounds(leftLabelWidth + 1 + cellValueInset, topLabelHeight + 1, cellWidth - 2 * cellValueInset, cellHeight);
		for (int i=0 ; i<nXCats ; i++) {
			r.y = topLabelHeight + 1;
			for (int j=0 ; j<nYCats ; j++) {
				drawCell(g, i, j, r, counts);
				r.y += cellHeight;
			}
			r.x += cellWidth;
		}
		
		if (bottomMarginHeight > 0) {
			r.setBounds(leftLabelWidth + 1 + cellValueInset, topLabelHeight + 2 + nYCats * cellHeight, cellWidth - 2 * cellValueInset, bottomMarginHeight);
			for (int i=0 ; i<nXCats ; i++) {
				drawBottomMargin(g, i, r, counts);
				r.x += cellWidth;
			}
		}
		
		if (rightMarginWidth > 0) {
			r.setBounds(leftLabelWidth + 2 + nXCats * cellWidth, topLabelHeight + 1, rightMarginWidth, cellHeight);
			for (int i=0 ; i<nYCats ; i++) {
				drawRightMargin(g, i, r, counts);
				r.y += cellHeight;
			}
		}
		
		if (bottomMarginHeight > 0 && rightMarginWidth > 0) {
			r.setBounds(leftLabelWidth + 2 + nXCats * cellWidth, topLabelHeight + 2 + nYCats * cellHeight,
																																					rightMarginWidth, bottomMarginHeight);
			drawTotal(g, r, counts);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
