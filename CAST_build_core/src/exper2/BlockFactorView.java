package exper2;

import java.awt.*;

import dataView.*;
import random.RandomInteger;
import images.*;


public class BlockFactorView extends DataView {
	static final public int GRASS_RAIN = 0;
	static final public int CORN_EMZYME = 1;
	
	static final private String kGrassUnitGif = "exper/grass.gif";
	static final private int kGrassSourceUnitWidth = 141;
	static final private int kGrassSourceUnitHeight = 196;
	
	static final private String kRainTreat2Gif = "exper/raindrop_std.gif";
	static final private String kRainTreat1Gif = "exper/raindrop_small.gif";
	static final private int kRainSourceTreatWidth = 405;
	static final private int kRainSourceTreatHeight = 592;
	
	static final private double kGrassDestUnitRatio = 1.39;		//		height / width
	static final private double kRainDestTreatRatio = 1.46;		//		height / width
	
	static final private Color kGrassColor0 = new Color(0x006600);
	static final private Color kGrassColor1 = new Color(0xFF9933);
	
	static final private String kCornUnitGif = "exper/corn.gif";
	static final private int kCornSourceUnitWidth = 142;
	static final private int kCornSourceUnitHeight = 238;
	
	static final private String kEmzymeTreat2Gif = "exper/mounds_2.gif";
	static final private String kEmzymeTreat1Gif = "exper/mounds_1.gif";
	static final private String kEmzymeTreat0Gif = "exper/mounds_0.gif";
	static final private int kEmzymeSourceTreatWidth = 564;
	static final private int kEmzymeSourceTreatHeight = 533;
	
	static final private double kCornDestUnitRatio = 1.68;		//		height / width
	static final private double kEmzymeDestTreatRatio = 0.95;		//		height / width
	
	static final private Color kCornColor0 = new Color(0x336600);
	static final private Color kCornColor1 = new Color(0x99CC33);
	
	static final private int kNoOfTreats = 3;
//	static final private double treatPosX[] = {};
//	static final private double treatPosY[] = {};
	
	static final private int kUnitTreatGap = 3;
	static final private int kUnitHorizGap = 5;
	static final private int kUnitVertGap = 5;
	static final private int kBlockGap = 10;
	
	static final private int kNoOfFrames = 100;
	static final private int kFramesPerSec = 40;
	
	
	private String[] blockNames;
	private Image unitImage;
	private Image treatImage[] = new Image[3];
	private Color unitColor0, unitColor1;
	private int sourceUnitWidth, sourceUnitHeight, sourceTreatWidth, sourceTreatHeight;
	private double destUnitRatio, destTreatRatio;
	
	private int noOfBlocks, noOfReps, nDisplayCols, nDisplayRows, nVals;
	
	private RandomInteger randComplete, randBlock;
	
	private int unitCompletePerm[];		//	covar --> location in grid
	private int unitBlockPerm[];
	
	private int treat[];
	private int treatPerm[];					//	index in treat[] --> location in grid
	private int lastTreatPerm[];
	
	private boolean showBlocks = false;
	
	private Image bufferedImage = null;
	private int bufferWidth, bufferHeight;
	
	
	public BlockFactorView(DataSet theData, XApplet applet, String[] blockNames,
													int noOfReps, int nDisplayCols, long permutationSeed, int iconType) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		noOfBlocks = blockNames.length;
		this.blockNames = blockNames;
		this.noOfReps = noOfReps;
		this.nDisplayCols = nDisplayCols;
		
		int nUnitsInBlock = kNoOfTreats * noOfReps;
		if ((nUnitsInBlock / nDisplayCols) * nDisplayCols != nUnitsInBlock)
			throw new RuntimeException("BlockFactorView: no of units in block should be multiple of nDisplayCols");
		nVals = noOfBlocks * nUnitsInBlock;
		nDisplayRows = nVals / nDisplayCols;
		
		treat = new int[nVals];
		for (int i=0 ; i<nVals ; i++)
			treat[i] = i % kNoOfTreats;
		
		loadImages(applet, iconType);
		
		randComplete = new RandomInteger(0, nVals - 1, nVals);
		randComplete.setSeed(permutationSeed);
		randBlock = new RandomInteger(0, nUnitsInBlock - 1, nUnitsInBlock);
		permutationSeed += 2348435398712l;
		randBlock.setSeed(permutationSeed);
		
		permuteUnits();
	}

//	---------------------------------
//	to prevent flicker when animating
//	if paintComponent() is NOT overridden, AppletViewer does not flicker but Mac browsers do.
//	---------------------------------
	
	public void paintComponent(Graphics g) {
		if (bufferedImage == null || bufferWidth != getSize().width || bufferHeight != getSize().height) {
			bufferWidth = getSize().width;
			bufferHeight = getSize().height;
			bufferedImage = createImage(bufferWidth, bufferHeight);
		}
		Graphics2D buffer = (Graphics2D) bufferedImage.getGraphics();
		
		buffer.setColor(getApplet().getBackground());
		buffer.fillRect(0, 0, getWidth(), getHeight());
		checkAliasing(buffer);
		corePaint(buffer);
		
		g.drawImage(bufferedImage, 0, 0, this);
	}
	
//	---------------------------------
	
	
	public void animateShowBlocks(boolean showBlocks) {
		this.showBlocks = showBlocks;
		treatPerm = null;
		animateFrames(kNoOfFrames, -kNoOfFrames, kFramesPerSec, null);
	}
	
	public void animateRandomise() {
		lastTreatPerm = treatPerm;
		permuteTreats();			//		sets treatPerm[]
		if (lastTreatPerm == null) {
			lastTreatPerm = treatPerm;
			permuteTreats();			//		sets treatPerm[]
		}
		
		animateFrames(kNoOfFrames, -kNoOfFrames, kFramesPerSec, null);
	}
	
	private void loadImages(XApplet applet, int iconType) {
		MediaTracker mt = new MediaTracker(this);
		if (iconType == GRASS_RAIN) {
			unitImage = CoreImageReader.getImage(kGrassUnitGif);
			mt.addImage(unitImage, 0);
			
			unitColor0 = kGrassColor0;
			unitColor1 = kGrassColor1;
			sourceUnitWidth = kGrassSourceUnitWidth;
			sourceUnitHeight = kGrassSourceUnitHeight;
			destUnitRatio = kGrassDestUnitRatio;
			
			treatImage[0] = null;
			
			treatImage[1] = CoreImageReader.getImage(kRainTreat1Gif);
			mt.addImage(treatImage[0], 0);
			
			treatImage[2] = CoreImageReader.getImage(kRainTreat2Gif);
			mt.addImage(treatImage[2], 0);
			
			sourceTreatWidth = kRainSourceTreatWidth;
			sourceTreatHeight = kRainSourceTreatHeight;
			destTreatRatio = kRainDestTreatRatio;
		}
		else {
			unitImage = CoreImageReader.getImage(kCornUnitGif);
			mt.addImage(unitImage, 0);
			
			unitColor0 = kCornColor0;
			unitColor1 = kCornColor1;
			sourceUnitWidth = kCornSourceUnitWidth;
			sourceUnitHeight = kCornSourceUnitHeight;
			destUnitRatio = kCornDestUnitRatio;
			
			treatImage[0] = CoreImageReader.getImage(kEmzymeTreat0Gif);
			mt.addImage(treatImage[0], 0);
			
			treatImage[1] = CoreImageReader.getImage(kEmzymeTreat1Gif);
			mt.addImage(treatImage[1], 0);
			
			treatImage[2] = CoreImageReader.getImage(kEmzymeTreat2Gif);
			mt.addImage(treatImage[2], 0);
			
			sourceTreatWidth = kEmzymeSourceTreatWidth;
			sourceTreatHeight = kEmzymeSourceTreatHeight;
			destTreatRatio = kEmzymeDestTreatRatio;
		}
	
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image: " + e);
		}
	}
	
	private Color getCovarColor(int i, int nVals) {
		double p = i / (nVals - 1.0);
		return mixColors(unitColor0, unitColor1, p);
	}

	
	private void permuteUnits() {
		unitCompletePerm = new int[nVals];
		
		for (int i=0 ; i<nVals ; i++)
			unitCompletePerm[i] = i;
		int[] swap = randComplete.generate();
		for (int i=0 ; i<nVals ; i++)
			if (i != swap[i]) {
				int temp = unitCompletePerm[i];
				unitCompletePerm[i] = unitCompletePerm[swap[i]];
				unitCompletePerm[swap[i]] = temp;
			}
		
		unitBlockPerm = new int[nVals];
		
		for (int i=0 ; i<nVals ; i++)
			unitBlockPerm[i] = i;
		int noInBlock = kNoOfTreats * noOfReps;
		
		for (int block=0 ; block<noOfBlocks ; block++) {
			swap = randBlock.generate();
			int baseIndex = block * noInBlock;
			for (int i=0 ; i<noInBlock ; i++)
				if (i != swap[i]) {
					int temp = unitBlockPerm[baseIndex + i];
					unitBlockPerm[baseIndex + i] = unitBlockPerm[baseIndex + swap[i]];
					unitBlockPerm[baseIndex + swap[i]] = temp;
				}
		}
	}
	
	private void permuteTreats() {
		treatPerm = new int[nVals];
		for (int i=0 ; i<nVals ; i++)
			treatPerm[i] = i;
		
		if (showBlocks) {
			int noInBlock = kNoOfTreats * noOfReps;
			
			for (int block=0 ; block<noOfBlocks ; block++) {
				int[] swap = randBlock.generate();
				int baseIndex = block * noInBlock;
				for (int i=0 ; i<noInBlock ; i++)
					if (i != swap[i]) {
						int temp = treatPerm[baseIndex + i];
						treatPerm[baseIndex + i] = treatPerm[baseIndex + swap[i]];
						treatPerm[baseIndex + swap[i]] = temp;
					}
			}
		}
		else {
			int[] swap = randComplete.generate();
		for (int i=0 ; i<nVals ; i++)
			if (i != swap[i]) {
				int temp = treatPerm[i];
				treatPerm[i] = treatPerm[swap[i]];
				treatPerm[swap[i]] = temp;
			}
		}
	}
	
	public void drawUnitImage(Rectangle r, int covar, Graphics g) {
		g.drawImage(unitImage, r.x, r.y, r.x + r.width, r.y + r.height,
									0, 0, sourceUnitWidth, sourceUnitHeight, getCovarColor(covar, nVals), this);
	}
	
	public void drawTreatImage(Rectangle r, int treatIndex, Graphics g) {
		Image theImage = treatImage[treatIndex];
		if (theImage != null)
			g.drawImage(theImage, r.x, r.y, r.x + r.width, r.y + r.height,
															0, 0, sourceTreatWidth, sourceTreatHeight, this);
	}
	
	private Point getUnitTopLeft(int index, int blockLabelHt, boolean localShowBlocks, Point tempP) {
		int row = index / nDisplayCols;
		int col = index % nDisplayCols;
		
		int availableHeight = getSize().height;
		if (localShowBlocks)
			availableHeight -= noOfBlocks * blockLabelHt;
		
		int top = availableHeight * row / nDisplayRows;
		int rowsPerBlock = nDisplayRows / noOfBlocks;
		if (localShowBlocks)
			top += (row / rowsPerBlock + 1) * blockLabelHt;
			
		int availableWidth = getSize().width;
		int left = availableWidth * col / nDisplayCols;
		
		if (tempP == null)
			return new Point(left, top);
		else {
			tempP.setLocation(left, top);
			return tempP;
		}
	}
	
	
	
	public void paintView(Graphics g) {
		int width = (getSize().width - kUnitHorizGap * (nDisplayCols - 1)) / nDisplayCols;
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int blockLabelHt = ascent + descent + kBlockGap;
		int nRowsPerBlock = kNoOfTreats*noOfReps / nDisplayCols;
		int nRows = nRowsPerBlock * noOfBlocks;
		int availHeight = getSize().height - noOfBlocks * (blockLabelHt + nRowsPerBlock * kUnitVertGap);
		int height = availHeight / nRows;
		
		int availWidth = width - kUnitTreatGap;
		int widthUsed = (int)Math.round(height * (1 + 1 / destTreatRatio) / destUnitRatio);
		int horizUnitOffset, vertUnitOffset, unitWidth, unitHeight;
		int horizTreatOffset, vertTreatOffset, treatWidth, treatHeight;
		if (widthUsed < availWidth) {
			vertUnitOffset = 0;
			horizUnitOffset = (availWidth - widthUsed) / 2;
			unitWidth = (int)Math.round(height / destUnitRatio);
			unitHeight = height;
			
			horizTreatOffset = horizUnitOffset + unitWidth + kUnitTreatGap;
			treatHeight = (int)Math.round(height / destUnitRatio);
			vertTreatOffset = (height - treatHeight) / 2;
			treatWidth = (int)Math.round(treatHeight / destTreatRatio);
		}
		else {
			horizUnitOffset = 0;
			unitWidth = (int)Math.round(availWidth * destTreatRatio / (1 + destTreatRatio));
			unitHeight = (int)Math.round(unitWidth * destUnitRatio);
			vertUnitOffset = (height - unitHeight) / 2;
			
			horizTreatOffset = horizUnitOffset + unitWidth + kUnitTreatGap;
			treatWidth = width - horizTreatOffset;
			treatHeight = unitWidth;
			vertTreatOffset = (height - treatHeight) / 2;
		}
		
		if (showBlocks) {
			int blockBaseline = kUnitVertGap + ascent;
			for (int i=0 ; i<noOfBlocks ; i++) {
				g.drawString(blockNames[i], 0, blockBaseline);
				blockBaseline += (height + kUnitVertGap) * nRowsPerBlock + blockLabelHt;
			}
		}
		
		Point blockTopLeft = null;
		Point completeTopLeft = null;
		Point topLeft = new Point(0, 0);
		int currentFrame = getCurrentFrame();
		
		Rectangle unitRect = new Rectangle(0, 0, unitWidth, unitHeight);
		for (int covar=0 ; covar<nVals ; covar++) {
			blockTopLeft = getUnitTopLeft(unitBlockPerm[covar], blockLabelHt, true,
																																			blockTopLeft);
			completeTopLeft = getUnitTopLeft(unitCompletePerm[covar], blockLabelHt, false,
																																			completeTopLeft);
			
			if (treatPerm != null || currentFrame == 0)
				topLeft.setLocation(showBlocks ? blockTopLeft : completeTopLeft);
			else {
				Point p0 = showBlocks ? blockTopLeft : completeTopLeft;
				Point p1 = showBlocks ? completeTopLeft : blockTopLeft;
				topLeft.setLocation((p1.x * currentFrame + p0.x * (kNoOfFrames - currentFrame)) / kNoOfFrames,
														(p1.y * currentFrame + p0.y * (kNoOfFrames - currentFrame)) / kNoOfFrames);
			}
			
			unitRect.x = topLeft.x + horizUnitOffset;
			unitRect.y = topLeft.y + vertUnitOffset;
			drawUnitImage(unitRect, covar, g);
		}
		
		Point lastTopLeft = null;
		Point nextTopLeft = null;
		
		Rectangle treatRect = new Rectangle(0, 0, treatWidth, treatHeight);
		if (treatPerm != null)
			for (int i=0 ; i<nVals ; i++) {
				int treatIndex = treat[i];
				
				nextTopLeft = getUnitTopLeft(treatPerm[i], blockLabelHt, showBlocks, nextTopLeft);
				if (lastTreatPerm == null)
					topLeft.setLocation(nextTopLeft);
				else {
					lastTopLeft = getUnitTopLeft(lastTreatPerm[i], blockLabelHt, showBlocks, lastTopLeft);
					topLeft.setLocation((lastTopLeft.x * currentFrame + nextTopLeft.x * (kNoOfFrames - currentFrame)) / kNoOfFrames,
														(lastTopLeft.y * currentFrame + nextTopLeft.y * (kNoOfFrames - currentFrame)) / kNoOfFrames);
				}
				
				treatRect.x = topLeft.x + horizTreatOffset;
				treatRect.y = topLeft.y + vertTreatOffset;
				drawTreatImage(treatRect, treatIndex, g);
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
	
