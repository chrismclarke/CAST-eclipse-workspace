package pairBlock;

import dataView.*;
import axis.*;

import pairBlockProg.*;


public class AnimateBlockDiffView extends BlockDotPlotView {
//	static public final String ANIMATE_BLOCK_DIFF = "animateBlockDiff";
	
	static final protected int kFinalFrame = 40;
	static final private int kFramesPerSec = 20;
	
	private XApplet applet;
	
	private boolean animateToDifferences;
	
	public AnimateBlockDiffView(DataSet theData, XApplet applet, String groupKey, String blockKey,
								NumCatAxis numAxis, NumCatAxis groupAxis, double jitter) {
		super(theData, applet, groupKey, blockKey, numAxis, groupAxis, jitter);
		this.applet = applet;
	}
	
	public void animateDifferences(boolean animateToDifferences) {
		this.animateToDifferences = animateToDifferences;
		animateFrames(animateToDifferences ? 0 : kFinalFrame,
														animateToDifferences ? kFinalFrame : -kFinalFrame, kFramesPerSec, null);
	}
	
	protected void drawNextFrame() {
		RemoveBlockVariable yVar = (RemoveBlockVariable)getVariable("y");
		yVar.setBlockEffectProportion(getCurrentFrame() / (double)kFinalFrame);
		
		super.drawNextFrame();
		
		if (applet instanceof RemoveBlockEffectApplet) {
			if (animateToDifferences && getCurrentFrame() == kFinalFrame)
				((RemoveBlockEffectApplet)applet).finishedAnimation(true);
			else if (!animateToDifferences && getCurrentFrame() == 0)
				((RemoveBlockEffectApplet)applet).finishedAnimation(false);
		}
		else if (applet instanceof BlockPairedDiffApplet) {
			if (animateToDifferences && getCurrentFrame() == kFinalFrame)
				((BlockPairedDiffApplet)applet).finishedAnimation(true);
			else if (!animateToDifferences && getCurrentFrame() == 0)
				((BlockPairedDiffApplet)applet).finishedAnimation(false);
		}
	}
}