package exper2;

import java.awt.*;
import java.util.*;

import dataView.*;


public class IncompleteDesignView extends DataView {
	static final private Color kGridColor = new Color(0x666666);
	static final public Color kTreatColor[] = {Color.blue, Color.yellow, new Color(0x009900), Color.red, new Color(0x9900FF), new Color(0xFF6600), Color.white};
	
	static final private int kRowNameGap = 20;
	static final private int kColNameGap = 6;
	
	static final private int kNoOfFrames = 100;
	static final private int kFramesPerSec = 40;
	
	static final private int ROW_PERM = 0;
	static final private int COL_PERM = 1;
	static final private int NO_PERM = 2;
	static final private int UNIT_PERM = 3;
	static final private int BLOCK_PERM = 4;
	
	static final public int PICK_TREATMENTS = 0;
	static final public int ALLOCATE_UNITS = 1;
	
	static private Random rand01 = new Random();
	
	static public int[] initialPerm(int n) {
		int[] perm = new int[n];
		for (int i=0 ; i<n ; i++)
			perm[i] = i;
		return perm;
	}
	
	static public void newPermutation(int[] perm) {
		int oldPerm[] = (int[])perm.clone();
		while (true) {
			perm[0] = 0;				//	does not use previous values in array
			for (int i=1 ; i<perm.length ; i++) {
				int j = (int)Math.round((i + 1) * rand01.nextDouble() - 0.5);
				if (i == j)
					perm[i] = i;
				else {
					perm[i] = perm[j];
					perm[j] = i;
				}
			}
			boolean changed = false;
			for (int i=0 ; i<perm.length ; i++)
				if (perm[i] != oldPerm[i])
					changed = true;
			if (changed)
				break;
		}
	}
	
	private int nCols;
	private LabelValue[] rowName, colName, treatName;
	private int[] rowPerm, colPerm, treatPerm;
	private int[] rowPermNext, colPermNext;
	
	private int[] unitPerm, unitPermNext;
	private int[][] blockPerm, blockPermNext;
	
	private int[][] baseDesign;
	
	private boolean rowsAreBlocks, colsAreBlocks;
	
	private int animateType = NO_PERM;
	
	private int designMode = PICK_TREATMENTS;
	
	public IncompleteDesignView(LabelValue[] rowName, LabelValue[] colName, LabelValue[] treatName,
																																						XApplet applet) {
		super(new DataSet(), applet, null);
		this.rowName = rowName;
		nCols = colName.length;
		this.colName = colName;
		this.treatName = treatName;
		rowPerm = initialPerm(rowName.length);
		colPerm = initialPerm(nCols);
		treatPerm = initialPerm(treatName.length);
		
		baseDesign = new int[rowName.length][nCols];
		for (int row=0 ; row<rowName.length ; row++)
			for (int col=0 ; col<nCols ; col++) {
				int treat = col - row;
				if (treat < 0)
					treat += nCols;
				baseDesign[row][col] = treat;
			}
	}
	
	public IncompleteDesignView(LabelValue[] rowName, int nCols, int[][] baseDesign,
																										LabelValue[] treatName, XApplet applet) {
		super(new DataSet(), applet, null);
		this.rowName = rowName;
		this.nCols = nCols;
		colName = null;
		this.treatName = treatName;
		rowPerm = initialPerm(rowName.length);
		colPerm = initialPerm(nCols);
		treatPerm = initialPerm(treatName.length);
		
		this.baseDesign = baseDesign;
	}
	
	public void setDesignMode(int designMode, boolean rowsAreBlocks, boolean colsAreBlocks) {
		this.designMode = designMode;
		this.rowsAreBlocks = rowsAreBlocks;
		this.colsAreBlocks = colsAreBlocks;
		if (designMode == ALLOCATE_UNITS) {
			if (rowPermNext != null) {
				rowPerm = rowPermNext;
				rowPermNext = null;
			}
			if (colPermNext != null) {
				colPerm = colPermNext;
				colPermNext = null;
			}
			if (!rowsAreBlocks && !colsAreBlocks) {
				unitPerm = initialPerm(rowName.length * nCols);
				unitPermNext = null;
				blockPerm = blockPermNext = null;
			}
			else if (rowsAreBlocks && !colsAreBlocks) {
				blockPerm = new int[rowName.length][];
				for (int i=0 ; i<rowName.length ; i++)
					blockPerm[i] = initialPerm(nCols);
				blockPermNext = new int[rowName.length][];
				unitPerm = unitPermNext = null;
			}
			setFrame(0);
		}
		else {
			animateType = NO_PERM;
			repaint();
		}
	}
	
	public void setTreatPerm(int[] treatPerm) {
		this.treatPerm = treatPerm;
		repaint();
	}
	
	public int[] getTreatPerm() {
		return treatPerm;
	}
	
	public void permuteUnits() {
		if (!rowsAreBlocks && !colsAreBlocks) {
			if (unitPermNext == null)
				unitPermNext = new int[unitPerm.length];
			else
				unitPerm = (int[])unitPermNext.clone();
			newPermutation(unitPermNext);
			animateType = UNIT_PERM;
		}
		else if (rowsAreBlocks && !colsAreBlocks) {
			for (int i=0 ; i<blockPerm.length ; i++) {
				if (blockPermNext[i] == null)
					blockPermNext[i] = new int[blockPerm[i].length];
				else
					blockPerm[i] = (int[])blockPermNext[i].clone();
				newPermutation(blockPermNext[i]);
			}
			animateType = BLOCK_PERM;
		}
		else {
			animateType = NO_PERM;
			return;		//	not implemented if cols are only block variables
		}
		animateFrames(0, kNoOfFrames, kFramesPerSec, null);
		repaint();
	}
	
	public void permuteRows() {
		if (colPermNext != null) {
			colPerm = colPermNext;
			colPermNext = null;
		}
		if (rowPermNext == null)
			rowPermNext = new int[rowPerm.length];
		else
			rowPerm = (int[])rowPermNext.clone();
		newPermutation(rowPermNext);
		animateType = ROW_PERM;
		animateFrames(0, kNoOfFrames, kFramesPerSec, null);
		repaint();
	}
	
	public void permuteCols() {
		if (rowPermNext != null) {
			rowPerm = rowPermNext;
			rowPermNext = null;
		}
		if (colPermNext == null)
			colPermNext = new int[colPerm.length];
		else
			colPerm = (int[])colPermNext.clone();
		newPermutation(colPermNext);
		animateType = COL_PERM;
		animateFrames(0, kNoOfFrames, kFramesPerSec, null);
		repaint();
	}
	
	private void setColHeading(int col, int leftBorder, int topBorder, Rectangle r) {
		r.width = (getSize().width - leftBorder - 1) / nCols;
		r.height = topBorder;
		r.x = leftBorder + col * r.width;
		r.y = 0;
	}
	
	private void setRowHeading(int row, int leftBorder, int topBorder, Rectangle r) {
		r.height = (getSize().height - topBorder - 1) / rowName.length;
		r.width = leftBorder;
		r.y = topBorder + row * r.height;
		r.x = 0;
	}
	
	private void setColPos(int col, int leftBorder, Rectangle r) {
		int permCol = colPerm[col];
		r.width = (getSize().width - leftBorder - 1) / nCols;
		r.x = leftBorder + permCol * r.width;
		if (animateType == COL_PERM && colPermNext != null) {
			int currentFrame = getCurrentFrame();
			int nextPermCol = colPermNext[col];
			int nextX = leftBorder + nextPermCol * r.width;
			r.x = (currentFrame * nextX + (kNoOfFrames - currentFrame) * r.x) / kNoOfFrames;
		}
	}
	
	private void setRowPos(int row, int topBorder, Rectangle r) {
		r.height = (getSize().height - topBorder - 1) / rowName.length;
		int permRow = rowPerm[row];
		r.y = topBorder + permRow * r.height;
		if (animateType == ROW_PERM && rowPermNext != null) {
			int currentFrame = getCurrentFrame();
			int nextPermRow = rowPermNext[row];
			int nextY = topBorder + nextPermRow * r.height;
			r.y = (currentFrame * nextY + (kNoOfFrames - currentFrame) * r.y) / kNoOfFrames;
		}
	}
	
	private int getRowPerm(int row, int col, int[] perm) {
		int unit = row * nCols + col;
		unit = perm[unit];
		return unit / nCols;
	}
	
	private int getColPerm(int row, int col, int[] perm) {
		int unit = row * nCols + col;
		unit = perm[unit];
		return unit % nCols;
	}
	
	private Point getTreatmentCenter(int row, int col, int leftBorder, int topBorder,
																								int cellWidth, int cellHeight, Point p) {
		int startRow = row;
		int startCol = col;
		if (animateType == UNIT_PERM) {
			startRow = getRowPerm(row, col, unitPerm);
			startCol = getColPerm(row, col, unitPerm);
		}
		else if (animateType == BLOCK_PERM)
			startCol = blockPerm[row][col];
		
		p.x = leftBorder + startCol * cellWidth + cellWidth / 2;
		p.y = topBorder + startRow * cellHeight + cellHeight / 2;
		
		int currentFrame = getCurrentFrame();
		if (currentFrame > 0) {
			if (animateType == UNIT_PERM) {
				int nextRow = getRowPerm(row, col, unitPermNext);
				int nextCol = getColPerm(row, col, unitPermNext);
				int nextX = leftBorder + nextCol * cellWidth + cellWidth / 2;
				int nextY = topBorder + nextRow * cellHeight + cellHeight / 2;
				p.x = (currentFrame * nextX + (kNoOfFrames - currentFrame) * p.x) / kNoOfFrames;
				p.y = (currentFrame * nextY + (kNoOfFrames - currentFrame) * p.y) / kNoOfFrames;
			}
			else if (animateType == BLOCK_PERM) {
				int nextCol = blockPermNext[row][col];
				int nextX = leftBorder + nextCol * cellWidth + cellWidth / 2;
				p.x = (currentFrame * nextX + (kNoOfFrames - currentFrame) * p.x) / kNoOfFrames;
			}
		}
		
		return p;
	}
	
	private void drawTreatment(Graphics g, Point p, LabelValue rowLabel, LabelValue colLabel,
																		LabelValue treatLabel, int ascent, int descent) {
		int noOfLabels = 1 + (rowLabel == null ? 0 : 1) + (colLabel == null ? 0 : 1);
		int baseline = p.y + (ascent - descent) / 2 - (noOfLabels - 1) * (ascent + descent) / 2;
		if (rowLabel != null) {
			rowLabel.drawCentred(g, p.x, baseline);
			baseline += (ascent + descent);
		}
		if (colLabel != null) {
			colLabel.drawCentred(g, p.x, baseline);
			baseline += (ascent + descent);
		}
		treatLabel.drawCentred(g, p.x, baseline);
	}
	
	public void paintView(Graphics g) {
		int maxRowNameLength = 0;
		for (int row=0 ; row<rowName.length ; row++)
			maxRowNameLength = Math.max(maxRowNameLength, rowName[row].stringWidth(g));
		int leftBorder = maxRowNameLength + kRowNameGap;
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int topBorder = (colName == null) ? 0 : (ascent + descent + kColNameGap);
		Rectangle r = new Rectangle(0,0,0,0);
		
		if (designMode == PICK_TREATMENTS || rowsAreBlocks)
			for (int row=0 ; row<rowName.length ; row++) {
				setRowHeading(row, leftBorder, topBorder, r);
				int baseline = r.y + (r.height + ascent - descent) / 2;
				rowName[row].drawRight(g, 0, baseline);
			}
		if ((colName != null) && (designMode == PICK_TREATMENTS || colsAreBlocks))
			for (int col=0 ; col<nCols ; col++) {
				setColHeading(col, leftBorder, topBorder, r);
				int center = r.x + r.width / 2;
				int baseline = ascent;
				colName[col].drawCentred(g, center, baseline);
			}
		
		if (designMode == PICK_TREATMENTS)
			for (int row=0 ; row<rowName.length ; row++) {
				setRowPos(row, topBorder, r);
				for (int col=0 ; col<nCols ; col++) {
					setColPos(col, leftBorder, r);
					
					int treat = baseDesign[row][col];
					g.setColor(kTreatColor[treat]);
					g.fillRect(r.x, r.y, r.width, r.height);
					
					g.setColor(kGridColor);
					g.drawRect(r.x, r.y, r.width, r.height);
					
					if (treatPerm != null) {
						g.setColor(getForeground());
						int permTreat = treatPerm[treat];
						int baseline = r.y + (r.height + ascent - descent) / 2;
						int center = r.x + r.width / 2;
						treatName[permTreat].drawCentred(g, center, baseline);
					}
				}
			}
		else {
			int cellWidth = (getSize().width - leftBorder - 1) / nCols;
			int cellHeight = (getSize().height - topBorder - 1) / rowName.length;
			g.setColor(Color.white);
			g.fillRect(leftBorder, topBorder, nCols * cellWidth, rowName.length * cellHeight);
			
			g.setColor(kGridColor);
			for (int i=0 ; i<=nCols ; i++)
				g.drawLine(leftBorder + i * cellWidth, topBorder, leftBorder + i * cellWidth, topBorder + rowName.length * cellHeight);
			for (int i=0 ; i<=rowName.length ; i++)
				g.drawLine(leftBorder, topBorder + i * cellHeight, leftBorder + nCols * cellWidth, topBorder + i * cellHeight);
			
			Point p = new Point(0, 0);
			g.setColor(getForeground());
			for (int row=0 ; row<rowName.length ; row++)
				for (int col=0 ; col<nCols ; col++) {
					int permRow = rowPerm[row];
					int permCol = colPerm[col];
					int treat = baseDesign[row][col];
					if (treatPerm != null)
						treat = treatPerm[treat];
					
					p = getTreatmentCenter(permRow, permCol, leftBorder, topBorder, cellWidth, cellHeight, p);
					
					drawTreatment(g, p, rowsAreBlocks ? null : rowName[permRow],
																(colsAreBlocks || colName == null) ? null : colName[permCol],
																treatName[treat], ascent, descent);
				}
		}
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(50, 50);			//	should always be in the centre of a BorderLayout
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}