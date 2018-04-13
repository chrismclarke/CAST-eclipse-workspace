package exper;

import java.awt.*;
import java.util.*;

import dataView.*;
import images.*;


public class BlockAndTreatView extends DataView {
	
	static final private Color kBackgroundColor[] = {new Color(0x339900), new Color(0x996633)};
	
	static final private int kCoreRowGap = 2;
	static final private int kBlockExtraRowGap = 3;
	static final private int kColGap = 2;
	static final private int kLeftRightBorder = 4;
	
	private String treatmentKey;
	private Image treatImage[];
	private int treatImageHeight, treatImageWidth;
	private int rows, cols, rowsPerBlock;
	
	private boolean tempFlags[];
	
	
	public BlockAndTreatView(DataSet theData, XApplet applet,
						String treatmentImageNames, String treatmentKey,
						int rows, int cols, int rowsPerBlock) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.treatmentKey = treatmentKey;
		this.rows = rows;
		this.cols = cols;
		this.rowsPerBlock = rowsPerBlock;
		
		treatImage = loadImages(treatmentImageNames, applet);
		treatImageWidth = treatImage[1].getWidth(this);
		treatImageHeight = treatImage[1].getHeight(this);
	}
	
	private Image[] loadImages(String imageNames, XApplet applet) {
		CatVariable v = (CatVariable)getVariable(treatmentKey);
		Image result[] = new Image[v.noOfCategories()];
		
		MediaTracker mt = new MediaTracker(this);
		StringTokenizer st = new StringTokenizer(imageNames);
		int index = 0;
		while (st.hasMoreTokens()) {
			String imageName = st.nextToken();
			
			result[index] = CoreImageReader.getImage(imageName);
			mt.addImage(result[index], 0);
			
			index ++;
		}
		
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image: " + e);
		}
		return result;
	}
	
	public void paintView(Graphics g) {
		int nBlocks = rows / rowsPerBlock;
		int rowHeight = treatImageHeight + 2 * kCoreRowGap;
		int blockHeight = rowsPerBlock * rowHeight + 2 * kBlockExtraRowGap;
		for (int i=0 ; i<nBlocks ; i++) {
			int top = i * blockHeight;
			g.setColor(kBackgroundColor[i % 2]);
			g.fillRect(0, top, getSize().width, blockHeight);
		}
		
		CatVariable treat = (CatVariable)getVariable(treatmentKey);
		ValueEnumeration e = treat.values();
		FlagEnumeration fe = getSelection().getEnumeration();
//		int selectedIndex = getSelection().findSingleSetFlag();
//		int index = 0;
		for (int i=0 ; i<rows ; i++) {
			int blockIndex = i / rowsPerBlock;
			int rowInBlock = i % rowsPerBlock;
			int top = blockIndex * blockHeight + kBlockExtraRowGap + rowInBlock * rowHeight + kCoreRowGap;
			
//			(1 + blockIndex * 2) * kBlockExtraRowGap + i * rowHeight
//																													+ (1 + 2 * i) * kCoreRowGap;
			for (int j=0 ; j<cols ; j++) {
				Value nextVal = e.nextValue();
				boolean nextSel = fe.nextFlag();
				int treatIndex = treat.labelIndex(nextVal);
//				int treatIndex = treat.getItemCategory(index);
				int left = kLeftRightBorder + (1 + j * 2) * kColGap + j * treatImageWidth;
				
				if (nextSel) {
					g.setColor(Color.yellow);
					g.fillRect(left, top, treatImageWidth, treatImageHeight);
				}
				g.drawImage(treatImage[treatIndex], left, top, this);
//				index++;
			}
		}
	}
	
	public Dimension getMinimumSize() {
		int width = 2 * kLeftRightBorder + (treatImageWidth + 2 * kColGap) * cols;
		int nBlocks = rows / rowsPerBlock;
		int height = 2 * nBlocks * kBlockExtraRowGap + (treatImageHeight + 2 * kCoreRowGap) * rows;
		
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < kLeftRightBorder || y < 0 || x >= getSize().width - kLeftRightBorder || y >= getSize().height)
			return null;
		
		int rowHeight = treatImageHeight + 2 * kCoreRowGap;
		int blockHeight = rowsPerBlock * rowHeight + 2 * kBlockExtraRowGap;
		int blockIndex = y / blockHeight;
		int yInBlock = y - blockIndex * blockHeight - kBlockExtraRowGap;
		
		if (yInBlock < 0 || yInBlock >= rowHeight * rowsPerBlock)
			return null;
		
		return new IndexPosInfo(blockIndex);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else {
			int hitBlock = ((IndexPosInfo)startInfo).itemIndex;
			if (tempFlags ==  null)
				tempFlags = new boolean[rows * cols];
			for (int i=0 ; i<cols ; i++)
				for (int j=0 ; j<rows ; j++)
					tempFlags[j*cols + i] = j/rowsPerBlock == hitBlock;
			getData().setSelection(tempFlags);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		startDrag(null);
	}
	
//	protected PositionInfo getPosition(int x, int y) {
//		if (x < kLeftRightBorder || y < 0 || x >= getSize().width - kLeftRightBorder || y >= getSize().height)
//			return null;
//		
//		int colIndex =  (x - kLeftRightBorder) / (treatImageWidth + 2 * kColGap);
//		
//		int rowHeight = treatImageHeight + 2 * kCoreRowGap;
//		int blockHeight = rowsPerBlock * rowHeight + 2 * kBlockExtraRowGap;
//		int blockIndex = y / blockHeight;
//		int yInBlock = y - blockIndex * blockHeight - kBlockExtraRowGap;
//		
//		if (yInBlock < 0 || yInBlock >= rowHeight * rowsPerBlock)
//			return null;
//		
//		int rowIndex = blockIndex * rowsPerBlock + yInBlock / rowHeight;
//		
//		return new IndexPosInfo(rowIndex * cols + colIndex);
//	}
	
}
	
