package stemLeaf;

import java.awt.*;

import dataView.*;


public class BackedStemAndLeafView extends DataView {
	static final private int kKeyOffset = 30;
	
	private int leafCount[][] = null;
	private int sortedGroupIndex[][] = null;
	
	private int offsetForLeftLeaves;
	
	private StemAndLeafAxis axis;
	
	public BackedStemAndLeafView(DataSet theData, XApplet applet, String axisInfo) {
		super(theData, applet, null);
		axis = new StemAndLeafAxis(axisInfo, applet);
		LeafDigitImages.loadDigits(applet);
	}
	
	public boolean changeNoOfBins(boolean moreNotLess, int maxBins, double minDisplayVal, double maxDisplayVal) {
		if (axis.changeNoOfBins(moreNotLess, maxBins, minDisplayVal, maxDisplayVal)) {
			leafCount = null;
			repaint();
			return true;
		}
		else
			return false;
	}
	
	synchronized protected void countLeaves() {
		CatVariable groupVariable = getCatVariable();
		NumVariable variable = getNumVariable();
		leafCount = new int[2][];
		for (int i=0 ; i<2 ; i++)
			leafCount[i] = new int[axis.maxStemAndLeaf - axis.minStemAndLeaf + 1];
		
		for (int i = 0; i<variable.noOfValues() ; i++) {
			int stemAndLeaf = axis.findStemAndLeaf((NumValue)variable.valueAt(i));
			int group = groupVariable.getItemCategory(i);
			if (stemAndLeaf >= axis.minStemAndLeaf && stemAndLeaf < axis.maxStemAndLeaf)
				leafCount[group][stemAndLeaf - axis.minStemAndLeaf] ++;
		}
		
		sortedGroupIndex = new int[2][];
		int[] groupCounts = groupVariable.getCounts();
		for (int i=0 ; i<2 ; i++) {
			sortedGroupIndex[i] = new int[groupCounts[i]];
			groupCounts[i] = 0;
		}
		
		int[] sortedIndex = variable.getSortedIndex();
		
		for (int i=0 ; i<variable.noOfValues() ; i++) {
			int valueIndex = sortedIndex[i];
			int group = groupVariable.getItemCategory(valueIndex);
			sortedGroupIndex[group][groupCounts[group]] = valueIndex;
			groupCounts[group]++;
		}
	}
	
	static final private int kLabelOffset = 5;
	
	private void drawLabels(Graphics g) {
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		
		CatVariable groupVariable = getCatVariable();
		int labelVert = headingHt + (axis.noOfBins + 1) * lineHt + LeafDigitImages.kVertSpace;
		int rightStart = offsetForLeftLeaves + axis.leafStart + kLabelOffset;
		int leftEnd = StemAndLeafAxis.kLeftOffset + offsetForLeftLeaves
															- 2 * StemAndLeafAxis.kLineGap - 1 - kLabelOffset;
		groupVariable.getLabel(0).drawLeft(g, leftEnd, labelVert);
		groupVariable.getLabel(1).drawRight(g, rightStart, labelVert);
	}
	
	protected void drawHeading(Graphics g, int stemLeafStart) {
		NumValue sortedVal[] = getNumVariable().getSortedData();
		NumValue maxValue = sortedVal[sortedVal.length - 1];
		axis.drawHeading(g, stemLeafStart, maxValue, this);
	}
	
	protected void drawStem(Graphics g, int currentStem, int stemVert) {
		axis.drawStem(g, currentStem, stemVert, offsetForLeftLeaves + axis.stemWidth, this);
	}
	
	protected void drawLeaf(Graphics g, Image leaf, int currentCharPos, int stemVert) {
		axis.drawLeaf(g, leaf, currentCharPos, stemVert, this);
	}
	
	public void paintView(Graphics g) {
		axis.setMaxStemWidth();
		if (leafCount == null) {
			countLeaves();
			offsetForLeftLeaves = (getSize().width - axis.stemWidth) / 2 - StemAndLeafAxis.kLeftOffset;
		}
		
		drawHeading(g, kKeyOffset);
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		int charWidth = axis.getDigitWidth();
		
		g.setColor(Color.blue);
		int horizLinePos = offsetForLeftLeaves + axis.leafStart - StemAndLeafAxis.kLineGap - 2;
		int lineBottom = headingHt + axis.noOfBins * lineHt + LeafDigitImages.kVertSpace;
		
		g.drawLine(horizLinePos, headingHt, horizLinePos, lineBottom);
		g.drawLine(horizLinePos, lineBottom, horizLinePos + kLabelOffset, lineBottom);
		g.drawLine(horizLinePos, headingHt, horizLinePos + kLabelOffset, headingHt);
		
		horizLinePos = offsetForLeftLeaves + StemAndLeafAxis.kLeftOffset - 2 * StemAndLeafAxis.kLineGap;
		g.drawLine(horizLinePos, headingHt, horizLinePos, lineBottom);
		g.drawLine(horizLinePos, lineBottom, horizLinePos - kLabelOffset, lineBottom);
		g.drawLine(horizLinePos, headingHt, horizLinePos - kLabelOffset, headingHt);
		
		drawLabels(g);
		g.setColor(getForeground());
		
		int stemVert = headingHt + LeafDigitImages.kDigitHeight + (axis.noOfBins - 1) * lineHt + LeafDigitImages.kVertSpace;
		int currentStem = axis.minStem;
		int currentRepeat = axis.minStemRepeat;
		int currentLeafIndex = 0;
		
		
		highlightSelectedLeaf(g);
		
		for (int binIndex = 0 ; binIndex < axis.noOfBins ; binIndex++) {
			drawStem(g, currentStem, stemVert);
			
			int rightCurrentCharPos = offsetForLeftLeaves + axis.leafStart;
			int leftCurrentCharPos = StemAndLeafAxis.kLeftOffset + offsetForLeftLeaves
																- 2 * StemAndLeafAxis.kLineGap - 1 - charWidth;
			for (int i=0 ; i<axis.leavesPerBin ; i++) {
				Image leaf = axis.getLeaf(currentRepeat, i, currentStem < 0);
				for (int j=0 ; j<leafCount[1][currentLeafIndex] ; j++) {
					drawLeaf(g, leaf, rightCurrentCharPos, stemVert);
					rightCurrentCharPos += charWidth;
				}
				for (int j=0 ; j<leafCount[0][currentLeafIndex] ; j++) {
					drawLeaf(g, leaf, leftCurrentCharPos, stemVert);
					leftCurrentCharPos -= charWidth;
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
	
	private void highlightSelectedLeaf(Graphics g) {
		int index = getSelection().findSingleSetFlag();
		if (index < 0)
			return;
		
		g.setColor(Color.yellow);
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		int charWidth = axis.getDigitWidth();
		
		int indexInGroup = -1;
		int group = 0;
		for (int gr=0 ; gr<2 ; gr++)
			for (int i=0 ; i<sortedGroupIndex[gr].length ; i++)
				if (sortedGroupIndex[gr][i] == index) {
					indexInGroup = i;
					group = gr;
					break;
				}
		for (int i=0 ; i<leafCount[group].length ; i++) {
			if (indexInGroup < leafCount[group][i]) {
				int binIndex = i / axis.leavesPerBin;
				for (int j=1 ; j <= i % axis.leavesPerBin ; j++)
					indexInGroup += leafCount[group][i - j];		//	add counts for lower leaves in bin
				if (group == 0)
					g.fillRect(StemAndLeafAxis.kLeftOffset + offsetForLeftLeaves
							- 2 * StemAndLeafAxis.kLineGap - 1 - (indexInGroup + 1) * charWidth - LeafDigitImages.kHorizSpace / 2 - 1,
							headingHt + (axis.noOfBins - 1 - binIndex) * lineHt + LeafDigitImages.kVertSpace / 2,
							charWidth + 2, lineHt + 1);
				else
					g.fillRect(offsetForLeftLeaves + axis.leafStart + indexInGroup * charWidth - LeafDigitImages.kHorizSpace / 2 - 1,
							headingHt + (axis.noOfBins - 1 - binIndex) * lineHt + LeafDigitImages.kVertSpace / 2,
							charWidth + 2, lineHt + 1);
				return;
			}
			indexInGroup -= leafCount[group][i];
		}
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		return new Dimension(20, headingHt + (axis.noOfBins + 1) * lineHt + 2 * LeafDigitImages.kVertSpace + StemAndLeafAxis.kTopBorder);
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		int charWidth = axis.getDigitWidth();
		
		if (y < headingHt || y >= headingHt + lineHt * axis.noOfBins)
			return null;
		
		int group;
		int leafIndex = ((axis.noOfBins - 1) - (y - headingHt) / lineHt) * axis.leavesPerBin;
		int indexInLeaf;
		if (x >= offsetForLeftLeaves + axis.leafStart) {
			group = 1;
			indexInLeaf = (x - axis.leafStart - offsetForLeftLeaves) / charWidth;
		}
		else if (x < StemAndLeafAxis.kLeftOffset + offsetForLeftLeaves - 2 * StemAndLeafAxis.kLineGap) {
			group = 0;
			indexInLeaf = (StemAndLeafAxis.kLeftOffset + offsetForLeftLeaves - 2 * StemAndLeafAxis.kLineGap - x) / charWidth;
		}
		else
			return null;
		
		boolean foundLeaf = false;
		for (int i=0 ; i<axis.leavesPerBin ; i++) {
			if (indexInLeaf < leafCount[group][leafIndex]) {
				foundLeaf = true;
				break;
			}
			indexInLeaf -= leafCount[group][leafIndex];
			leafIndex++;
		}
		if (foundLeaf) {
			int orderedIndex = indexInLeaf;
			for (int i=0 ; i<leafIndex ; i++)
				orderedIndex += leafCount[group][i];
			
			int index = sortedGroupIndex[group][orderedIndex];
			return new IndexPosInfo(index);
		}
		return null;
	}
}