package exper;

import java.awt.*;
import java.util.*;

import dataView.*;
import random.RandomInteger;
import images.*;


public class BlockPlotsView extends DataView {
	
//	static final private Color kBlockSeparatorColor = Color.lightGray;
	
	static final public int kBlockedFrame = 20;
	
	static final private int kCoreRowGap = 2;
	static final private int kBlockExtraRowGap = 3;
	static final private int kColGap = 12;
	static final private int kImageLeftRightGap = 4;
	
	private String treatmentKey, blockKey, responseKey;
	private Image noBlockImage;
	private Image blockImage[];
	private Image treatImage[];
	private int blockImageHeight, blockImageWidth, treatImageHeight, treatImageWidth, maxImageHeight;
	private int rows, cols, rowsPerBlock;
	
	private boolean initialised = false;
	private boolean showTreatments = false;
	private int ascent, descent, boxWidth, boxHeight, extraRowGap;
	
	private int permutation[];
	
	private boolean showBlocks = true;
	
	public BlockPlotsView(DataSet theData, XApplet applet,
						String noBlockImageName, String blockImageNames, String treatmentImageNames,
						String blockKey, String treatmentKey, String responseKey, int rows, int cols,
						int rowsPerBlock, long permutationSeed) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.blockKey = blockKey;
		this.treatmentKey = treatmentKey;
		this.responseKey = responseKey;
		this.rows = rows;
		this.cols = cols;
		this.rowsPerBlock = rowsPerBlock;
		
		noBlockImage = loadOneImage(noBlockImageName, applet);
		blockImage = loadImages(blockImageNames, blockKey, applet);
		blockImageWidth = blockImage[0].getWidth(this);
		blockImageHeight = blockImage[0].getHeight(this);
		treatImage = loadImages(treatmentImageNames, treatmentKey, applet);
		treatImageWidth = treatImage[1].getWidth(this);
		treatImageHeight = treatImage[1].getHeight(this);
		maxImageHeight = Math.max(blockImageHeight, treatImageHeight);
		
		CatVariable block = (CatVariable)getVariable(blockKey);
		int n = block.noOfValues();
		int noOfBlocks = block.noOfCategories();
		permutation = new int[n];
		for (int i=0 ; i<n ; i++)
			permutation[i] = i;
		RandomInteger rand = new RandomInteger(0, noOfBlocks - 1, n);
		rand.setSeed(permutationSeed);
		int[] swap = rand.generate();
		for (int i=0 ; i<n ; i++)
			if (i != swap[i]) {
				int temp = permutation[i];
				permutation[i] = permutation[swap[i]];
				permutation[swap[i]] = temp;
			}
	}
	
	public void setShowBlocks(boolean showBlocks) {
		if (this.showBlocks == showBlocks)
			return;
		this.showBlocks = showBlocks;
		repaint();
	}
	
	private Image loadOneImage(String imageName, XApplet applet) {
		Image result = null;
		
		if (imageName != null) {
			MediaTracker mt = new MediaTracker(this);
				result = CoreImageReader.getImage(imageName);
			mt.addImage(result, 0);
		
			try {
				mt.waitForAll();
			} catch (InterruptedException e) {
				System.err.println("could not load image: " + e);
			}
		}
		return result;
	}
	
	private Image[] loadImages(String imageNames, String variableKey, XApplet applet) {
		CatVariable v = (CatVariable)getVariable(variableKey);
		Image result[] = new Image[v.noOfCategories()];
		
		MediaTracker mt = new MediaTracker(this);
		StringTokenizer st = new StringTokenizer(imageNames);
		int index = 0;
		while (st.hasMoreTokens()) {
			String imageName = st.nextToken();
			if (imageName.equals("*"))				//		code for blank image
				result[index] = null;
			else {
				result[index] = CoreImageReader.getImage(imageName);
				mt.addImage(result[index], 0);
			}
			index ++;
		}
		
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image: " + e);
		}
		return result;
	}
	
	private boolean initialise(Graphics g) {
		if (!initialised) {
			g.setFont(getApplet().getStandardBoldFont());
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
//			CatVariable block = (CatVariable)getVariable(blockKey);
			boxWidth = 2 * kImageLeftRightGap + cols * (blockImageWidth + treatImageWidth)
																								+ (cols - 1) * kColGap;
			extraRowGap = kBlockExtraRowGap + (ascent + descent) / rowsPerBlock;
			boxHeight = rows * (maxImageHeight + kCoreRowGap + extraRowGap);
			
			initialised = true;
			return true;
		}
		return false;
	}
	
	private Point imageTopLeft(int index, boolean blocked) {
		int posIndex = blocked ? index : permutation[index];
		int rowIndex = posIndex / cols;
		int colIndex = posIndex % cols;
		int horizPos =  kImageLeftRightGap
													+ colIndex * (blockImageWidth + treatImageWidth + kColGap);
		int vertPos = rowIndex * (maxImageHeight + kCoreRowGap);
		if (blocked) {
			int blockIndex = rowIndex / rowsPerBlock;
			vertPos += (blockIndex + 1) * (rowsPerBlock * extraRowGap) + kCoreRowGap / 2;
		}
		else
			vertPos += rowIndex * extraRowGap + (kCoreRowGap + extraRowGap) / 2;
		return new Point(horizPos, vertPos);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		if (getCurrentFrame() > kBlockedFrame / 2) {
			CatVariable block = (CatVariable)getVariable(blockKey);
			g.setFont(getApplet().getStandardBoldFont());
			int gapBetweenBlocks = extraRowGap * rowsPerBlock;
			int blockHeight = (maxImageHeight + kCoreRowGap + extraRowGap) * rowsPerBlock;
			for (int i=0 ; i<rows/rowsPerBlock ; i++) {
				g.setColor(getBackground());
				g.fillRect(0, i * blockHeight, boxWidth, gapBetweenBlocks);
				
				g.setColor(Color.black);
				block.getLabel(i).drawRight(g, 3, i * blockHeight + gapBetweenBlocks - descent - 1);
			}
		}
		
		CatVariable block = (CatVariable)getVariable(blockKey);
		CatVariable treat = (CatVariable)getVariable(treatmentKey);
		for (int i=0 ; i<block.noOfValues() ; i++) {
			int blockIndex = block.getItemCategory(i);
			int treatIndex = treat.getItemCategory(i);
			int left, top;
			if (getCurrentFrame() == 0) {
				Point topLeft = imageTopLeft(i, false);
				left = topLeft.x;
				top = topLeft.y;
			}
			else if (getCurrentFrame() == kBlockedFrame) {
				Point topLeft = imageTopLeft(i, true);
				left = topLeft.x;
				top = topLeft.y;
			}
			else {
				Point noBlockPt = imageTopLeft(i, false);
				Point blockPt = imageTopLeft(i, true);
				left = (noBlockPt.x * (kBlockedFrame - getCurrentFrame())
															+ blockPt.x * getCurrentFrame()) / kBlockedFrame;
				top = (noBlockPt.y * (kBlockedFrame - getCurrentFrame())
															+ blockPt.y * getCurrentFrame()) / kBlockedFrame;
			}
			if (getSelection().valueAt(i)) {
				g.setColor(Color.yellow);
				g.fillRect(left - kColGap, top - kCoreRowGap,
						blockImageWidth + treatImageWidth + 2 * kColGap, maxImageHeight + 2 * kCoreRowGap);
			}
			g.drawImage(showBlocks ? blockImage[blockIndex] : noBlockImage, left, top, this);
			if (showTreatments && (treatImage[treatIndex] != null))
				g.drawImage(treatImage[treatIndex], left + blockImageWidth, top, this);
		}
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(boxWidth, boxHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(treatmentKey)) {
			showTreatments = false;
			repaint();
		}
		else if (key.equals(responseKey)) {
			showTreatments = true;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		int colIndex = Math.max(0, Math.min(cols - 1, x / (blockImageWidth + treatImageWidth + kColGap)));
		int rowIndex = Math.max(0, Math.min(rows - 1, y / (maxImageHeight + kCoreRowGap + extraRowGap)));
		int rawIndex = rowIndex * cols + colIndex;
		
		if (getCurrentFrame() == 0) {
			for (int i=0 ; i<permutation.length ; i++)
				if (rawIndex == permutation[i]) {
					rawIndex = i;
					break;
				}
		}
		else if (getCurrentFrame() != kBlockedFrame)
			return null;
		
		return new IndexPosInfo(rawIndex);
	}
	
}
	
