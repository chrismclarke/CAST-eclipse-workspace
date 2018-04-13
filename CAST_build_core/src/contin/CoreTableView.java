package contin;

import java.awt.*;

import dataView.*;
import imageGroups.TickCrossImages;


abstract public class CoreTableView extends DataView {
	static final public int NO_SELECTION = -1;
	static final public int ALL_SELECTED = -2;
	
	static final private int kVertBorder = 5;
	static final private int kRowGap = 10;
	static final private int kHalfMinColGap = 10;
	
	static final private int kXLabelBorder = 6;
	static final protected int kYLabelBorder = 3;
	static final private int kYLabelTopGap = 6;
	static final private int kOneRightGap = 6;
	static final private int kOneBottomGap = 3;
	
	static final private NumValue kOne = new NumValue(1.0, 1);
	
//	protected LabelValue kConditString;
//	private LabelValue kMarginString;
	
	private String xKey, yKey;
	protected int probDecimals;
	private NumValue zeroVal;
	
	private boolean initialised = false;
	
	protected int nYCats, nXCats;
	private Font boldFont;
	protected int ascent, descent;
	private int maxValWidth, oneWidth;
	private int maxXCatWidth, columnWidth, topHeight;
	private Dimension headingSize;
	
	private int selectedX = -1;
	private int selectedY = -1;
	
	public CoreTableView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.probDecimals = probDecimals;
		zeroVal = new NumValue(0.0, probDecimals);
		
//		kConditString = new LabelValue(applet.translate("Conditional"));
//		kMarginString = new LabelValue(applet.translate("Marginal"));
	}
	
	public void setSelection(int selectedX, int selectedY) {
		this.selectedX = selectedX;
		this.selectedY = selectedY;
		repaint();
	}

//---------------------------------------------------------------------------------
	
	abstract protected Dimension getHeadingSize(Graphics g, CoreVariable yVar, CoreVariable xVar);
	abstract protected int noOfTableCols();
	abstract protected int noOfTableRows();
	abstract protected void drawHeading(Graphics g, int horizCenter,
											CoreVariable yVar, CoreVariable xVar);
											
	protected double[] getSingleProbs(CoreVariable yVar, CoreVariable xVar) {
		return null;
	}
	
	protected double[][] getProbs(CoreVariable yVar, CoreVariable xVar) {
		return null;
	}
	
	protected int maxColHeadingWidth(int maxYCatWidth) {
		return maxYCatWidth;
	}
	
	protected int maxRowHeadingWidth(int maxXCatWidth) {
		return maxXCatWidth;
	}
	
	protected boolean hasRightMargin() {
		return false;
	}
	
	protected boolean hasBottomMargin() {
		return false;
	}
	
	protected void drawLeftMargin(Graphics g, int boxTop, int boxLeft, int boxHeight,
																int nRows, CatVariableInterface xVar) {
		int baseline = boxTop + kVertBorder + ascent;
		if (nRows == 1) {
			g.setColor(Color.red);
			LabelValue marginString = new LabelValue(getApplet().translate("Marginal"));
			marginString.drawLeft(g, boxLeft - kXLabelBorder, baseline);
			g.setColor(getForeground());
		}
		else
			for (int i=0 ; i<nRows ; i++) {
				xVar.getLabel(i).drawLeft(g, boxLeft - kXLabelBorder, baseline);
				baseline += ascent + descent + kRowGap;
			}
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return zeroVal.stringWidth(g);
	}

//---------------------------------------------------------------------------------
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		maxValWidth = getMaxValueWidth(g);
		oneWidth = kOne.stringWidth(g);
		
		Font standardFont = g.getFont();
		boldFont = new Font(standardFont.getName(), Font.BOLD, standardFont.getSize());
		
		g.setFont(boldFont);
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
		nYCats = yVar.noOfCategories();
		CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
		nXCats = xVar.noOfCategories();
		
		headingSize = getHeadingSize(g, (CoreVariable)yVar, (CoreVariable)xVar);
		topHeight = ascent + descent + kYLabelTopGap;
		if (headingSize != null)
			topHeight += headingSize.height;
		
		maxXCatWidth = 0;
		for (int i=0 ; i<nXCats ; i++)
			maxXCatWidth = Math.max(maxXCatWidth, xVar.getLabel(i).stringWidth(g));
		
		int maxYCatWidth = 0;
		for (int j=0 ; j<nYCats ; j++)
			maxYCatWidth = Math.max(maxYCatWidth, yVar.getLabel(j).stringWidth(g));
		columnWidth = Math.max(maxColHeadingWidth(maxYCatWidth), maxValWidth) + 2 * kHalfMinColGap;
		if (headingSize != null)
			columnWidth = Math.max(columnWidth, headingSize.width / noOfTableCols());
		
		
		initialised = true;
		return true;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int displayHeight = noOfTableRows() * (ascent + descent)
													+ (noOfTableRows() - 1) * kRowGap + 2 * kVertBorder;
		displayHeight += topHeight;
		
		if (hasBottomMargin())
			displayHeight += ascent + descent + kOneBottomGap;
		
		int displayWidth = maxRowHeadingWidth(maxXCatWidth) + kXLabelBorder
																			+ noOfTableCols() * columnWidth;
		if (hasRightMargin())
			displayWidth += kOneRightGap + oneWidth;
		return new Dimension(displayWidth, displayHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public int getTableCenter() {
		int nCols = noOfTableCols();
		int boxLeft = maxRowHeadingWidth(maxXCatWidth) + kXLabelBorder;
		int boxWidth = nCols * columnWidth;
		return boxLeft + boxWidth / 2;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CoreVariable yVar = (CoreVariable)getVariable(yKey);
		CoreVariable xVar = (CoreVariable)getVariable(xKey);
		
		CatVariableInterface yCatVar = (CatVariableInterface)yVar;
		CatVariableInterface xCatVar = (CatVariableInterface)xVar;
		
		double[] singleProbs = getSingleProbs(yVar, xVar);
		double[][] probs = getProbs(yVar, xVar);
		
		int nRows = noOfTableRows();
		int nCols = noOfTableCols();
		
		int boxLeft = maxRowHeadingWidth(maxXCatWidth) + kXLabelBorder;
		int boxTop = topHeight;
		int boxWidth = nCols * columnWidth;
		int boxHeight = nRows * (ascent + descent) + (nRows - 1) * kRowGap + 2 * kVertBorder;
		
		g.setColor(Color.white);
		g.fillRect(boxLeft, boxTop, boxWidth, boxHeight);
		
		g.setColor(getForeground());
		g.drawRect(boxLeft, boxTop, boxWidth - 1, boxHeight - 1);
		
		if (singleProbs == null && probs == null) {
			int horizCenter = boxLeft + boxWidth / 2;
			int qnTop = boxTop + (boxHeight - TickCrossImages.question.getHeight(this)) / 2;
			g.drawImage(TickCrossImages.question,
								horizCenter - TickCrossImages.question.getWidth(this), qnTop, this);
			g.drawImage(TickCrossImages.question, horizCenter, qnTop, this);
			
		}
		else {
			int baseline = boxTop + kVertBorder + ascent;
			for (int i=0 ; i<nRows ; i++) {
				int colCenter = boxLeft + columnWidth / 2;
				int colRight = colCenter + maxValWidth / 2;
				for (int j=0 ; j<nCols ; j++) {
					double p = (probs != null) ? probs[i][j]
											: (nRows == 1) ? singleProbs[j]
											: singleProbs[i];
					NumValue pij = new NumValue(p, probDecimals);
					
					if ((i == selectedX || selectedX == ALL_SELECTED) && (j == selectedY || selectedY == ALL_SELECTED)) {
						g.setColor(Color.yellow);
						g.fillRect(colCenter - columnWidth / 2 + 1, baseline - ascent - kVertBorder + 1,
																	columnWidth - 2, ascent + descent + 2 * kVertBorder - 2); 
						g.setColor(Color.red);
						pij.drawLeft(g, colRight, baseline);
						g.setColor(getForeground());
					}
					else
						pij.drawLeft(g, colRight, baseline);
					colCenter += columnWidth;
					colRight += columnWidth;
				}
				if (hasRightMargin())
					kOne.drawRight(g, boxLeft + boxWidth + kOneRightGap, baseline);
				baseline += ascent + descent + kRowGap;
			}
		}
		
		if (hasBottomMargin()) {
			int colCenter = boxLeft + columnWidth / 2;
			for (int j=0 ; j<nCols ; j++) {
				kOne.drawCentred(g, colCenter, boxTop + boxHeight + kOneBottomGap + ascent);
				colCenter += columnWidth;
			}
		}
		
		g.setColor(Color.lightGray);
		if (hasRightMargin()) {
			int separatorPos = boxTop + kVertBorder + ascent + descent + kRowGap / 2 - 2;
			for (int i=1 ; i<nRows ; i++) {
				g.drawLine(boxLeft + 1, separatorPos, boxLeft + boxWidth - 2, separatorPos);
				g.drawLine(boxLeft + 1, separatorPos + 1, boxLeft + boxWidth - 2, separatorPos + 1);
				separatorPos += ascent + descent + kRowGap;
			}
		}
		else if (hasBottomMargin()) {
			int separatorPos = boxLeft + columnWidth - 1;
			for (int j=1 ; j<nCols ; j++) {
				g.drawLine(separatorPos, boxTop + 1, separatorPos, boxTop + boxHeight - 2);
				g.drawLine(separatorPos + 1, boxTop + 1, separatorPos + 1, boxTop + boxHeight - 2);
				separatorPos += columnWidth;
			}
		}
		
		g.setFont(boldFont);
		g.setColor(getForeground());
		
		int baseline = boxTop - descent - kYLabelBorder;
		int colCenter = boxLeft + columnWidth / 2;
		if (nCols == 1) {
			g.setColor(Color.red);
			LabelValue marginString = new LabelValue(getApplet().translate("Marginal"));
			marginString.drawCentred(g, colCenter, baseline);
			g.setColor(getForeground());
		}
		else
			for (int j=0 ; j<nCols ; j++) {
				yCatVar.getLabel(j).drawCentred(g, colCenter, baseline);
				colCenter += columnWidth;
			}
		
		drawLeftMargin(g, boxTop, boxLeft, boxHeight, nRows, xCatVar);
		
		g.setColor(Color.red);
		drawHeading(g, boxLeft + boxWidth / 2, yVar, xVar);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(xKey) || key.equals(yKey))
			repaint();
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int nRows = noOfTableRows();
		int nCols = noOfTableCols();
		
		int boxLeft = maxRowHeadingWidth(maxXCatWidth) + kXLabelBorder;
		int boxTop = topHeight;
		int boxWidth = nCols * columnWidth;
		int boxHeight = nRows * (ascent + descent) + (nRows - 1) * kRowGap + 2 * kVertBorder;
		
		if (x < boxLeft || x > boxLeft + boxWidth || y < boxTop || y > boxTop + boxHeight)
			return null;
		
		int yCat = (x - boxLeft) / columnWidth;
		yCat = Math.min(Math.max(yCat, 0), nCols - 1);
		
		int xCat = (y - boxTop - kVertBorder) / (ascent + descent + kRowGap);
		xCat = Math.min(Math.max(xCat, 0), nRows - 1);
		
		return new ContinCatInfo(xCat, yCat);
	}
	
	@SuppressWarnings("deprecation")
	protected boolean startDrag(PositionInfo startInfo) {
		ContinCatInfo catInfo = (ContinCatInfo)startInfo;
		int newX = (catInfo == null) ? -1 : catInfo.xIndex;
		int newY = (catInfo == null) ? -1 : catInfo.yIndex;
		if (newX != selectedX || newY != selectedY) {
			selectedX = newX;
			selectedY = newY;
			repaint();
			
			postEvent(new Event(this, Event.ACTION_EVENT, catInfo));
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		ContinCatInfo catInfo = (ContinCatInfo)endPos;
		if (catInfo != null) {
			catInfo.xIndex = -1;
			catInfo.yIndex = -1;
		}
		startDrag(catInfo);
	}
}
