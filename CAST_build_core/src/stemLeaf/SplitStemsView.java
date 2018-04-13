package stemLeaf;

import java.awt.*;

import dataView.*;
import utils.*;


public class SplitStemsView extends StemAndLeafView {
	static private final int kFramesPerSec = 10;
	static private final int kSpacedFrame = 20;
	static public final int kSplitIndex = 50;
	
	private int targetRepeatsPerStem = 2;
	private boolean targetPositionsInitialised = false;
	private int startHoriz[];
	private int startVert[];
	private int endHoriz[];
	private int endVert[];
	
	public SplitStemsView(DataSet theData, XApplet applet, String axisInfo) {
		super(theData, applet, axisInfo);
		if (axis.repeatsPerStem != 5 || axis.minStemRepeat != 0 || axis.noOfBins % 5 != 0)
			System.err.println("Axis repeats should be set up for 5 repeats per stem");
	}
	
	public void setTargetRepeats(int target, XSlider controller) {
		if (target == 2 || target == 5) {
			targetRepeatsPerStem = target;
			targetPositionsInitialised = false;
			setFrame(0, controller);
		}
	}
	
	public void doSplittingAnimation(XSlider controller) {
		if (leafCount != null)			//	Don't try animation until painted once
			animateFrames(1, kSplitIndex - 1, kFramesPerSec, controller);
	}
	
	private void initialisePositions(int[] horizPos, int[] vertPos, int repeatsPerStem) {
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		
		int noOfBins = (axis.noOfBins / axis.repeatsPerStem) * repeatsPerStem;
		int leavesPerBin = (repeatsPerStem == 1) ? 10 : (repeatsPerStem == 2) ? 5 : 2;
		int digitWidth = axis.getDigitWidth();
		
		int vert = headingHt + LeafDigitImages.kDigitHeight + (noOfBins - 1) * lineHt;
		int currentLeafIndex = 0;
		for (int binIndex = 0 ; binIndex < noOfBins ; binIndex++) {
			int currentCharPos = axis.leafStart;
			for (int i=0 ; i<leavesPerBin ; i++) {
				vertPos[currentLeafIndex] = vert;
				horizPos[currentLeafIndex] = currentCharPos;
				currentCharPos += digitWidth * leafCount[currentLeafIndex];
				currentLeafIndex++;
			}
			vert -= lineHt;
		}
	}
	
	private int getScaledVert(int leafIndex) {
		int middleVert = endVert[(leafIndex / 10) * 10];
		
		if (getCurrentFrame() >= kSpacedFrame)
			return  (middleVert * (kSplitIndex - getCurrentFrame())
									+ endVert[leafIndex] * (getCurrentFrame() - kSpacedFrame))
														/ (kSplitIndex - kSpacedFrame);
		else
			return (startVert[leafIndex] * (kSpacedFrame - getCurrentFrame())
											+ middleVert * getCurrentFrame()) / kSpacedFrame;
	}
	
	private int getScaledHoriz(int leafIndex) {
		if (getCurrentFrame() <= kSpacedFrame)
			return startHoriz[leafIndex];
		else
			return (startHoriz[leafIndex] * (kSplitIndex - getCurrentFrame())
											+ endHoriz[leafIndex] * (getCurrentFrame() - kSpacedFrame))
														/ (kSplitIndex - kSpacedFrame);
	}
	
//	protected void drawHeading(Graphics g) {
//		NumValue sortedVal[] = getNumVariable().getSortedData();
//		NumValue maxValue = sortedVal[sortedVal.length - 1];
//		axis.drawHeading(g, 0, maxValue);
//	}

	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			startHoriz = new int[leafCount.length];
			startVert = new int[leafCount.length];
			endHoriz = new int[leafCount.length];
			endVert = new int[leafCount.length];
			initialisePositions(startHoriz, startVert, 1);
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		if (!targetPositionsInitialised) {
			initialisePositions(endHoriz, endVert, targetRepeatsPerStem);
			targetPositionsInitialised = true;
		}
		
		int headingHt = axis.getHeadingHt();
		int lineHt = axis.getLineHt();
		
		drawHeading(g);
		int lineEnd = getScaledVert(0) - LeafDigitImages.kDigitHeight + lineHt;
		int lineHoriz = axis.leafStart - StemAndLeafAxis.kLineGap - 1;
		g.drawLine(lineHoriz, headingHt, lineHoriz, lineEnd);
		
		int currentStem = axis.minStem;
		int currentRepeat = 0;
		int currentLeafIndex = 0;
		
		for (int binIndex = 0 ; binIndex < axis.noOfBins ; binIndex++) {
			int vert = getScaledVert(currentLeafIndex);
			drawStem(g, currentStem, vert);
			
			for (int i=0 ; i<axis.leavesPerBin ; i++) {
				Image leaf = axis.getLeaf(currentRepeat, i, currentStem < 0);
				int leafVert = getScaledVert(currentLeafIndex);
				int leafHoriz = getScaledHoriz(currentLeafIndex);
				for (int j=0 ; j<leafCount[currentLeafIndex] ; j++) {
					axis.drawLeaf(g, leaf, leafHoriz, leafVert, this);
					leafHoriz += axis.getDigitWidth();
				}
				currentLeafIndex++;
			}
			currentRepeat++;
			if (currentRepeat >= axis.repeatsPerStem) {
				currentStem++;
				currentRepeat = 0;
			}
		}
	}
	
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}