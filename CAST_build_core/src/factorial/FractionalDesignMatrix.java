package factorial;

import java.awt.*;

import dataView.*;

public class FractionalDesignMatrix extends DataView {
	static final private int kLeftRightBorder = 6;
//	static final private int kTopBottomBorder = 2;
	static final private int kMinHeadingGap = 2;
	static final private int kMinColumnGap = 8;
	static final private int kMinRowGap = 4;
	
	static final private LabelValue kPlusOne = new LabelValue("+1");
	static final private LabelValue kMinusOne = new LabelValue("-1");
	
	static final public Color kBlockColor[] = {new Color(0xBFFFBF), new Color(0xD5BFFF),
																							new Color(0xFFF2BF), new Color(0xFFBFBF),
																							new Color(0xBFE4FF), new Color(0xFFBFFF),
																							new Color(0xF2FFBF), new Color(0xFFDFBF)};
	
	static final private int NO_DRAG = 0;
	static final private int DRAG_ROW = 1;
	static final private int DRAG_COL = 2;
	
	private String[] varName;
	private int nComplete, nAlias;
	private int nRows;
	
	private Term[] term;
	
	private boolean showBlocks;
	private int blockColorOffset = 0;
	
	private boolean initialised = false;
	
	private int ascent;
	private int maxTermWidth;
	private int bestWidth, bestHeight;
	private Font stdFont, boldFont;
	
	private int dragType = NO_DRAG;
	
	public FractionalDesignMatrix(DataSet data, String[] varName, int nComplete, XApplet applet) {
		super(data, applet, null);
		this.varName = varName;
		this.nComplete = nComplete;
		nAlias = varName.length - nComplete;
		nRows = (int)Math.pow(2, nComplete);
		
		term = new Term[nRows];
		boolean[] currentTerm = new boolean[varName.length];			//	all false
		fillTerms(currentTerm, 0, 0);
		sortTerms();
		
		boolean[] design = new boolean[varName.length];		//	all false
		for (int i=0 ; i<varName.length-nComplete ; i++) {
			int temp = i + 1;
			for (int j=0 ; j<varName.length ; j++) {
				design[j] = temp % 2 == 1;
				temp /= 2;
			}
			design[nComplete + i] = true;
			setDesign(design, i);
			design[nComplete + i] = false;
		}
	}
	
	public void setShowBlocks(boolean showBlocks) {
		this.showBlocks = showBlocks;
	}
	
	public void setBlockColorOffset(int blockColorOffset) {
		this.blockColorOffset = blockColorOffset;
	}
	
	public boolean[] getMainEffects(int index, boolean[] mainEffects) {
		if (mainEffects == null || mainEffects.length != (nComplete + nAlias))
			mainEffects = new boolean[nComplete + nAlias];
		for (int i=0 ; i<nComplete ; i++)
			mainEffects[i] = term[i + 1].getValue(index);		//	term[0] is all +1
		
		for (int aliasIndex=0 ; aliasIndex<nAlias ; aliasIndex++) {
			for (int i=1 ; i<nRows ; i++)
				if (term[i].isMainAliasEffect(aliasIndex)) {
					mainEffects[nComplete + aliasIndex] = term[i].getValue(index);
					break;
				}
		}
		
		return mainEffects;
	}
	
	public int getBlock(int index) {				//		assumes that all alias variables define blocks
		int block = 0;
		for (int aliasIndex=0 ; aliasIndex<nAlias ; aliasIndex++) {
			block *= 2;
			for (int i=1 ; i<nRows ; i++)
				if (term[i].isMainAliasEffect(aliasIndex) && term[i].getValue(index)) {
					block ++;
					break;
				}
		}
		
		return block;
	}
	
	public void setDesign(boolean[] design, int aliasIndex) {
		for (int i=0 ; i<term.length ; i++)
			term[i].setDesign(design, aliasIndex);
		initialised = false;
		repaint();
	}
	
	private int fillTerms(boolean[] currentTerm, int fromLevel, int termIndex) {
		currentTerm[fromLevel] = true;
		if (fromLevel == nComplete - 1)
			term[termIndex ++] = new Term(varName, currentTerm, nComplete);
		else
			termIndex = fillTerms(currentTerm, fromLevel + 1, termIndex);
		
		currentTerm[fromLevel] = false;
		if (fromLevel == nComplete - 1)
			term[termIndex ++] = new Term(varName, currentTerm, nComplete);
		else
			termIndex = fillTerms(currentTerm, fromLevel + 1, termIndex);
		
		return termIndex;
	}
	
	private void sortTerms() {
		for (int i=0 ; i<term.length ; i++)
			for (int j=1 ; j<term.length-i ; j++)
				if (term[j - 1].getOrder() > term[j].getOrder()) {
					Term tempTerm = term[j - 1];
					term[j - 1] = term[j];
					term[j] = tempTerm;
				}
	}
	
	final protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		stdFont = g.getFont();
		boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
		
		ascent = g.getFontMetrics().getAscent();
		
		bestWidth = 2 * kLeftRightBorder + (term.length - 1) * kMinColumnGap;
		
		StringBuffer sb = new StringBuffer(varName.length);
		for (int i=0 ; i<varName.length ; i++)
				sb.append(varName[i]);
		
		g.setFont(boldFont);
		FontMetrics fm = g.getFontMetrics();
		maxTermWidth = fm.stringWidth(sb.toString());
		g.setFont(stdFont);
		
		bestWidth += (term.length - 1) * maxTermWidth;
		
		int headingHeight = Term.headingHeight(g, nAlias, kMinHeadingGap);
		bestHeight = headingHeight + nRows * (ascent + kMinRowGap);
	}
	
	private int getHeadingGap() {
		int nHeadingRows = 1 << nAlias;		//	2 to power nAlias;
		int halfRowExtra = (getSize().height - bestHeight) / (2  * nRows + nHeadingRows + 3);
		return kMinHeadingGap + halfRowExtra;
	}
	
	private int getRowTop(int row, int headingHeight) {
		return headingHeight + (getSize().height - headingHeight) * row / term.length;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int headingGap = getHeadingGap();
		int headingHeight = Term.headingHeight(g, nAlias, headingGap);
		
		int colExtra = (getSize().width - bestWidth) / nRows;
		int colGap = kMinColumnGap + colExtra;
		int leftRightBorder = kLeftRightBorder + colExtra;
		
		if (showBlocks)
			for (int i=0 ; i<nRows ; i++) {
				int thisTop = getRowTop(i, headingHeight);
				int nextTop = getRowTop(i + 1, headingHeight);
				int block = getBlock(i);
				g.setColor(kBlockColor[blockColorOffset + block]);
				g.fillRect(0, thisTop, getSize().width, nextTop - thisTop);
			}
		else {
			g.setColor(Color.white);
			g.fillRect(0, headingHeight, getSize().width, getSize().height - headingHeight);
		}
		
		int hitRow = getSelection().findSingleSetFlag();
		if (hitRow >= 0) {
			g.setColor(Color.yellow);
			int hitTop = getRowTop(hitRow, headingHeight);
			int hitBottom = getRowTop(hitRow + 1, headingHeight);
			g.fillRect(0, hitTop, getSize().width, hitBottom - hitTop);
		}
		
		g.setColor(getForeground());
		
		int colLeft = leftRightBorder;
		for (int i=1 ; i<term.length ; i++) {
			int colRight = colLeft + maxTermWidth;
			int colCentre = (colRight + colLeft) / 2;
			
			g.setFont(boldFont);
			term[i].drawHeading(g, colCentre, headingGap, showBlocks);
			g.setFont(stdFont);
			g.setColor(term[i].getValueColor());
			
			for (int j=0 ; j<nRows ; j++) {
				int thisTop = getRowTop(j, headingHeight);
				int nextTop = getRowTop(j + 1, headingHeight);
				int baseline = (thisTop + nextTop + ascent) / 2;
				boolean isPositive = term[i].getValue(j);
				if (isPositive)
					kPlusOne.drawCentred(g, colCentre, baseline);
				else
					kMinusOne.drawCentred(g, colCentre, baseline);
			}
			g.setColor(getForeground());
			colLeft = colRight + colGap;
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getPreferredSize() {
		initialise(getGraphics());
		return new Dimension(bestWidth, bestHeight);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	
	protected PositionInfo getPosition(int x, int y) {
		int headingGap = getHeadingGap();
		int headingHeight = Term.headingHeight(getGraphics(), nAlias, headingGap);
		if (dragType != DRAG_COL) {
			if (y > headingHeight && y < getSize().height && x > 0 && x < getSize().width) {
				int row = term.length * (y - headingHeight) / (getSize().height - headingHeight);
				row = Math.min(nRows - 1, row);
				return new IndexPosInfo(row);
			}
		}
		
		if (dragType != DRAG_ROW) {
			int colExtra = (getSize().width - bestWidth) / nRows;
			int colGap = kMinColumnGap + colExtra;
			int leftRightBorder = kLeftRightBorder + colExtra;
			
			int colLeft = leftRightBorder - colGap / 2;
			if (x < colLeft)
				return null;
			else
				x -= colLeft;
			
			for (int i=1 ; i<term.length ; i++) {
				int colWidth = maxTermWidth + colGap;
				if (x < colWidth) {
					int aliasIndex = term[i].getAliasDefnHit(y, headingGap, ascent);
					if (aliasIndex >= 0)
						return new HorizDragPosInfo(i, aliasIndex, 0);
					else
						return null;
				}
				else
					x -= colWidth;
			}
		}
		
		return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo != null) {
			if (startInfo instanceof IndexPosInfo) {
				dragType = DRAG_ROW;
				int hitRow = ((IndexPosInfo)startInfo).itemIndex;
				getData().setSelection(hitRow);
			}
			else {
				HorizDragPosInfo horizPos = (HorizDragPosInfo)startInfo;
				int hitIndex = horizPos.x;
				int aliasIndex = horizPos.index;
				boolean[] design = term[hitIndex].aliasedDesign(aliasIndex);
				setDesign(design, aliasIndex);
				
				getData().variableChanged("dummy");		//	so display of blocks changes
			}
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragType == DRAG_ROW) {
			dragType = NO_DRAG;
			getData().clearSelection();
		}
		else if (dragType == DRAG_COL)
			dragType = NO_DRAG;
	}
}