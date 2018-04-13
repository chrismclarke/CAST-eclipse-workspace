package exper2;

import java.awt.*;

import dataView.*;
import random.*;


public class RandomiseBlockView extends DataView {
	static final private int kUnitLeftRightBorder = 5;
	static final private int kTreatLeftRightBorder = 10;
	static final private int kHeadingBottomBorder = 5;
	
	static final private int kFrameMax = 50;
	static final private int kFramesPerSec = 20;
	
	static final private Color kHeadingColor = new Color(0x990000);
	static final private Color kUnitGridColor = new Color(0x999999);
	
	static final private Color[] kBlockColor = {new Color(0x000099), new Color(0x990000), new Color(0x006600), new Color(0x333333)};
	static final private Color[] kBlockBackgroundColor = new Color[kBlockColor.length];
	static {
		for (int i=0 ; i<kBlockColor.length ; i++)
			kBlockBackgroundColor[i] = dimColor(kBlockColor[i], 0.8);
	}
	
	static private Color getBlockColor(int i) {
		return kBlockColor[i % kBlockColor.length];
	}
	
	static private Color getBlockBackgroundColor(int i) {
		return kBlockBackgroundColor[i % kBlockColor.length];
	}
	
	private LabelValue kTreatmentsString, kUnitsString;
	
	private int blockRows, nCols, nRows, nBlocks;
	private Value treat[];
	
	private int permutation[] = null;
	private boolean blockPerm = false;
	
	public RandomiseBlockView(DataSet theData, XApplet applet, Value treat[], int nBlocks,
																																		int blockRows, int nCols) {
		super(theData, applet, null);
		this.treat = treat;
		this.blockRows = blockRows;
		this.nBlocks = nBlocks;
		this.nCols = nCols;
		nRows = nBlocks * blockRows;
		
		kTreatmentsString = new LabelValue(applet.translate("Treatments"));
		kUnitsString = new LabelValue(applet.translate("Experimental units"));
	}
	
	public void setBlockPerm(boolean blockPerm) {
		this.blockPerm = blockPerm;
		permutation = null;
	}
	
	public void setBlocks(int nBlocks, int blockRows) {
		this.blockRows = blockRows;
		this.nBlocks = nBlocks;
		permutation = null;
	}
	
	private int getBaseline(int i, int tableTop, int cellHeight, int vertTextOffset, boolean onLeft) {
		int row = i / nCols;
		int top = tableTop + row * cellHeight;
		if (onLeft && blockPerm) {
			int rowInBlock = row % blockRows;
			top += 10 * (blockRows / 2 - rowInBlock);
		}
		return top + vertTextOffset;
	}
	
	private int getCenter(int i, int cellWidth, int tableLeft) {
		int col = i % nCols;
		int left = tableLeft + col * cellWidth;
		return left + cellWidth / 2;
	}
	
	private Value getTreatment(int i) {
		if (blockPerm) {
			int unitsInBlock = blockRows * nCols;
			int treatRepsInBlock = (blockRows * nCols) / treat.length;
			int indexInBlock = i % unitsInBlock;
			return treat[indexInBlock / treatRepsInBlock];
		}
		else {
			int treatReps = (nRows * nCols) / treat.length;
			return treat[i / treatReps];
		}
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int treatWidth = 0;
		for (int i=0 ; i<treat.length ; i++)
			treatWidth = Math.max(treatWidth, treat[i].stringWidth(g));
		
		Font boldFont = getApplet().getBigBoldFont();
		g.setFont(boldFont);
		fm = g.getFontMetrics();
		int boldAscent = fm.getAscent();
		int boldDescent = fm.getDescent();
		int treatHeadingWidth = kTreatmentsString.stringWidth(g);
		int unitHeadingWidth = kUnitsString.stringWidth(g);
		
		int tableTop = boldAscent + boldDescent + kHeadingBottomBorder;
		
		int unitCellWidth = Math.max(unitHeadingWidth / nCols, treatWidth + 2 * kUnitLeftRightBorder);
		int treatCellWidth = Math.max(treatHeadingWidth / nCols, treatWidth + 2 * kTreatLeftRightBorder);
		int cellHeight = (getSize().height - tableTop - 1) / nRows;
		
		g.setColor(kHeadingColor);
		int treatHeadingLeft = (treatCellWidth * nCols - treatHeadingWidth) / 2;
		kTreatmentsString.drawRight(g, treatHeadingLeft, boldAscent);
		
		int unitHeadingRight = getSize().width - (unitCellWidth * nCols - unitHeadingWidth) / 2;
		kUnitsString.drawLeft(g, unitHeadingRight, boldAscent);
		g.setFont(getFont());
		
		int top = tableTop;
		int left = getSize().width - nCols * unitCellWidth - 1;
		int width = nCols * unitCellWidth;
		
		int blockHeight = blockRows * cellHeight;
		for (int i=0 ; i<nBlocks ; i++) {
			g.setColor(getBlockBackgroundColor(i));
			g.fillRect(left, top, width, blockHeight);
			
			if (blockPerm) {
				g.setColor(getBlockColor(i));
				g.fillRect(0, top + 6, 3, blockHeight - 12);
				g.fillRect(treatCellWidth * nCols + 10, top + 6, 3, blockHeight - 12);
			}
			
			top += blockHeight;
		}
		
		g.setColor(kUnitGridColor);
		for (int i=0 ; i<=nRows ; i++) {
			int vert = tableTop + cellHeight * i;
			g.drawLine(getSize().width - unitCellWidth * nCols - 1, vert, getSize().width, vert);
		}
		for (int i=0 ; i<=nCols ; i++) {
			int horiz = getSize().width - unitCellWidth * i - 1;
			g.drawLine(horiz, tableTop, horiz, tableTop + cellHeight * nRows);
		}
		
		int vertTextOffset = (cellHeight + ascent - descent) / 2;
		
		int currentFrame = getCurrentFrame();
//		Point p = null;
		int n = nBlocks * nCols * blockRows;
//		int repsInBlock = (nCols * blockRows) / treat.length;
		for (int i=0 ; i<n ; i++) {
			Value thisTreat = getTreatment(i);
			
			int block = i / (blockRows * nCols);
			Color c = (currentFrame == 0) ? (blockPerm ? getBlockColor(block) : Color.black)
																																: Color.white;
			g.setColor(c);
			
			int baseline = getBaseline(i, tableTop, cellHeight, vertTextOffset, true);
			int center = getCenter(i, treatCellWidth, 5);
			
			thisTreat.drawCentred(g, center, baseline);
		}
		
		if (currentFrame > 0) {
			int unitTableLeft = getSize().width - nCols * unitCellWidth;
			for (int i=0 ; i<n ; i++) {
				Value thisTreat = getTreatment(i);
				
				int block = i / (blockRows * nCols);
				g.setColor(blockPerm ? getBlockColor(block) : Color.black);
				
				int leftBaseline = getBaseline(i, tableTop, cellHeight, vertTextOffset, true);
				int leftCenter = getCenter(i, treatCellWidth, 5);
				
				int rightBaseline = getBaseline(permutation[i], tableTop, cellHeight, vertTextOffset, false);
				int rightCenter = getCenter(permutation[i], unitCellWidth, unitTableLeft);
				
				int baseline = (rightBaseline * currentFrame + leftBaseline * (kFrameMax - currentFrame)) / kFrameMax;
				int center = (rightCenter * currentFrame + leftCenter * (kFrameMax - currentFrame)) / kFrameMax;
				
				thisTreat.drawCentred(g, center, baseline);
			}
		}
	}
	
	public void animatePermutation() {
		int n = nBlocks * nCols * blockRows;
		if (permutation == null) {
			permutation = new int[n];
			for (int i=0 ; i<n ; i++)
				permutation[i] = i;
		}
		
		if (blockPerm) {
			int noInBlock = nCols * blockRows;
			RandomInteger generator = new RandomInteger(0, noInBlock - 1, noInBlock);
			for (int i=0 ; i<nBlocks ; i++) {
				int swap[] = generator.generate();
				int baseIndex = noInBlock * i;
				for (int j=0 ; j<noInBlock ; j++)
					if (swap[j] != j) {
						int temp = permutation[baseIndex + j];
						permutation[baseIndex + j] = permutation[baseIndex + swap[j]];
						permutation[baseIndex + swap[j]] = temp;
					}
			}
		}
		else {
			RandomInteger generator = new RandomInteger(0, n - 1, n);
			int swap[] = generator.generate();
			for (int i=0 ; i<n ; i++)
				if (swap[i] != i) {
					int temp = permutation[i];
					permutation[i] = permutation[swap[i]];
					permutation[swap[i]] = temp;
				}
		}
		animateFrames(0, kFrameMax, kFramesPerSec, null);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}