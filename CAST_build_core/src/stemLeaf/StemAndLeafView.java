package stemLeaf;

import java.awt.*;

import dataView.*;


public class StemAndLeafView extends DataView {
	
	protected int leafCount[] = null;
	private int tooLowCount;
	
	private boolean initialised = false;
	
	protected StemAndLeafAxis axis;
	
	public StemAndLeafView(DataSet theData, XApplet applet, String axisInfo) {
		super(theData, applet, null);
		axis = new StemAndLeafAxis(axisInfo, applet);
		LeafDigitImages.loadDigits(applet);
	}
	
	public boolean changeNoOfBins(boolean moreNotLess, int maxBins, double minDisplayVal, double maxDisplayVal) {
		if (axis.changeNoOfBins(moreNotLess, maxBins, minDisplayVal, maxDisplayVal)) {
			initialised = false;
			repaint();
			return true;
		}
		else
			return false;
	}
	
	public void resetAxis(String axisInfo) {
		axis = new StemAndLeafAxis(axisInfo, getApplet());
		initialised = false;
		invalidate();
		repaint();
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		axis.setMaxStemWidth();
		
		leafCount = new int[axis.maxStemAndLeaf - axis.minStemAndLeaf + 1];
		tooLowCount = 0;
		NumValue sortedVal[] = getNumVariable().getSortedData();
		
		for (int i = 0; i<sortedVal.length ; i++) {
			int stemAndLeaf = axis.findStemAndLeaf(sortedVal[i]);
			if (stemAndLeaf < axis.minStemAndLeaf)
				tooLowCount ++;
			else if (stemAndLeaf <= axis.maxStemAndLeaf)
				leafCount[stemAndLeaf - axis.minStemAndLeaf] ++;
		}
		initialised = true;
		return true;
	}
	
	protected void drawStem(Graphics g, int currentStem, int stemVert) {
		axis.drawStem(g, currentStem, stemVert, StemAndLeafAxis.kLeftOffset + axis.stemWidth, this);
	}
	
	protected void drawLeaf(Graphics g, Image leaf, int currentCharPos, int stemVert) {
		axis.drawLeaf(g, leaf, currentCharPos, stemVert, this);
	}
	
	protected void drawHeading(Graphics g) {
		NumValue sortedVal[] = getNumVariable().getSortedData();
		NumValue maxValue = sortedVal[sortedVal.length - 1];
		if (!Double.isNaN(maxValue.toDouble()))
			axis.drawHeading(g, 0, maxValue, this);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		drawHeading(g);
		
		g.setColor(Color.blue);
		int linePos = axis.leafStart - StemAndLeafAxis.kLineGap - 1;
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		
		g.drawLine(linePos, headingHt, linePos, headingHt + axis.noOfBins * lineHt);
		g.setColor(getForeground());
		
//		int stemRight = linePos - 2 - StemAndLeafAxis.kLineGap;
		int stemVert = headingHt + LeafDigitImages.kDigitHeight + (axis.noOfBins - 1) * lineHt;
		int currentStem = axis.minStem;
		int currentRepeat = axis.minStemRepeat;
		int currentLeafIndex = 0;
		
		int selectedRank = getSelection().findSingleSetFlag();
		if (selectedRank >= 0)
			selectedRank = getNumVariable().indexToRank(selectedRank);
		int valRank = 0;
		
		for (int binIndex = 0 ; binIndex < axis.noOfBins ; binIndex++) {
			drawStem(g, currentStem, stemVert);
			
			int currentCharPos = axis.leafStart;
			for (int i=0 ; i<axis.leavesPerBin ; i++) {
				Image leaf = axis.getLeaf(currentRepeat, i, currentStem < 0);
				for (int j=0 ; j<leafCount[currentLeafIndex] ; j++) {
					if (valRank == selectedRank) {
						g.setColor(Color.yellow);
						g.fillRect(currentCharPos - 2, stemVert - LeafDigitImages.kDigitHeight - 2,
									LeafDigitImages.kDigitWidth + 4, LeafDigitImages.kDigitHeight + 4);
						g.setColor(getForeground());
					}
					drawLeaf(g, leaf, currentCharPos, stemVert);
					currentCharPos += (LeafDigitImages.kDigitWidth + LeafDigitImages.kHorizSpace);
					valRank ++;
				}
				currentLeafIndex++;
			}
			
			stemVert -= lineHt;
			currentRepeat++;
			if (currentRepeat >= axis.repeatsPerStem) {
				currentStem++;
				currentRepeat = 0;
			}
		}
	}
	
//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		return new Dimension(20, headingHt + axis.noOfBins * lineHt + StemAndLeafAxis.kTopBorder);
	}

//------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		initialised = false;
		repaint();
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		if (x < axis.leafStart || y < headingHt || y >= headingHt + lineHt * axis.noOfBins)
			return null;
		
		int leafIndex = ((axis.noOfBins - 1) - (y - headingHt) / lineHt) * axis.leavesPerBin;
		int indexInLeaf = (x - axis.leafStart) / (LeafDigitImages.kDigitWidth + LeafDigitImages.kHorizSpace);
		boolean foundLeaf = false;
		for (int i=0 ; i<axis.leavesPerBin ; i++) {
			if (indexInLeaf < leafCount[leafIndex]) {
				foundLeaf = true;
				break;
			}
			indexInLeaf -= leafCount[leafIndex];
			leafIndex++;
		}
		if (foundLeaf) {
			int orderedIndex = tooLowCount + indexInLeaf;
			for (int i=0 ; i<leafIndex ; i++)
				orderedIndex += leafCount[i];
			
			NumVariable theVariable = getNumVariable();
			int index = theVariable.rankToIndex(orderedIndex);
			return new IndexPosInfo(index);
		}
		return null;
	}
	
}