package ssq;

import java.awt.*;

import imageGroups.AudioVisual;


public class AnovaImages extends AudioVisual {
	
	static public Image totalDevn, explDevn, residDevn,
									totalDevn2, explDevn2, residDevn2,							//	Groups
									rSquared, totalSsq, explSsq, residSsq,
									rSquared2, totalSsq2, explSsq2, residSsq2,			//	Between & Within
									totalMsq, explMsq, residMsq, f;
	
	static public Image rawTotalDevn, rawMeanDevn, rawResidDevn,
									rawTotalSsq, rawMeanSsq, rawResidSsq;
	
	static public Image group2TotalDevn, group2MeanDevn, group2Resid0Devn, group2Resid1Devn,
									group2TotalSsq, group2MeanSsq, group2Resid0Ssq, group2Resid1Ssq;
	
	static public Image quadTotalDevn, quadLinDevn, quadQuadDevn, quadResidDevn,
															quadTotalSsq, quadLinSsq, quadQuadSsq, quadResidSsq,
															quadTotalSsq2, quadLinSsq2, quadQuadSsq2, quadResidSsq2;
	
	static public Image pureTotalDevn, pureLinDevn, pureNonlinDevn, pureResidDevn,
															pureTotalSsq, pureLinSsq, pureNonlinSsq, pureResidSsq,
															pureNonlinSsq2;
															
	static public Image xzTotalSsq, xzResidSsq, xzExplXSsq, xzExplZSsq, xzExplZAfterXSsq,
															xzExplXAfterZSsq;
															
	static public Image quadXZTotalSsq, quadXZResidSsq, quadXZLinXZSsq, quadXZQuadXSsq;
															
	static public Image blockTotalSsq, blockResidSsq, blockBlockSsq, blockTreatSsq;
	
	static public Image[] basicRegnDevns;
	static public Image[] basicGroupDevns;
	static public Image[] rawDevns;
	static public Image[] group2Devns;
	static public Image[] quadDevns;
	static public Image[] pureDevns;
	
	static public Image[] basicRegnSsqs;
	static public Image[] basicGroupSsqs;
	static public Image[] rawSsqs;
	static public Image[] group2Ssqs;
	static public Image[] quadSsqs;
	static public Image[] quadSsqs2;
	static public Image[] pureSsqs;
	static public Image[] pureSsqs2;
	static public Image[] xThenZSsqs;
	static public Image[] zThenXSsqs;
	static public Image[] quadXZSsqs;
	static public Image[] blockSsqs;
	
	static final public int kRSquaredWidth = 90;
	static final public int kRSquared2Width = 105;
	static final public int kRSquaredAscent = 23;
	static final public int kRSquaredDescent = 14;
	static final public int kRSquaredHeight = kRSquaredAscent + kRSquaredDescent;	//	=37

	static final public int kDevnWidth = 60;
	static final public int kDevnAscent = 14;
	static final public int kDevnDescent = 7;
	static final public int kDevnHeight = kDevnAscent + kDevnDescent;		//	=21
	static final public int kRawDevnWidth = 54;

	static final public int kSsqWidth = 43;
	static final public int kSsq2Width = 57;
	static final public int kSsqAscent = 12;
	static final public int kSsqDescent = 4;
	static final public int kSsqHeight = kSsqAscent + kSsqDescent;		//		=16
	static final public int kRawSsqHeight = 24;
	static final public int kRawSsqWidth = 77;
	
	static final public int kGroup2SsqWidth = 92;
	static final public int kGroup2SsqHeight = 35;
	static final public int kGroup2DevnWidth = 53;
	static final public int kGroup2DevnHeight = 33;
	
	static final public int kQuadDevnWidth = 81;
	static final public int kQuadDevnHeight = 24;
	static final public int kQuadSsqWidth = 103;
	static final public int kQuadSsqHeight = 26;
	static final public int kQuadSsq2Width = 64;
	static final public int kQuadSsq2Height = 17;
	
	static final public int kXZSsqWidth = 103;
	static final public int kXZSsqHeight = 26;
	
	static final public int kMsqWidth = 60;
	static final public int kFWidth = 10;
	
	static final public int kQuadXZSsqWidth = 58;
	static final public int kQuadXZSsqHeight = 17;
	
	static final public int kBlockSsqWidth = 50;
	static final public int kBlockSsqHeight = 15;

	static private boolean loadedAnova = false;
	static private boolean loadedRegnImages = false;
	static private boolean loadedGroupImages = false;
	static private boolean loadedRawImages = false;
	static private boolean loadedGroup2Images = false;
	static private boolean loadedQuadImages = false;
	static private boolean loadedPureImages = false;
	static private boolean loadedXZImages = false;
	static private boolean loadedQuadXZImages = false;
	static private boolean loadedBlockImages = false;
	
	synchronized static private void loadCoreImages(Component theComponent) {
		if (loadedAnova)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		rSquared = loadImage("anova/rSquared.gif", tracker, rSquared, theComponent);
		rSquared2 = loadImage("anova/rSquared2.gif", tracker, rSquared2, theComponent);
		
		totalMsq = loadImage("anova/totalMsq.gif", tracker, totalMsq, theComponent);
		explMsq = loadImage("anova/explMsq.gif", tracker, explMsq, theComponent);
		residMsq = loadImage("anova/residMsq.gif", tracker, residMsq, theComponent);
		
		f = loadImage("anova/f.gif", tracker, f, theComponent);
		
		waitForLoad(tracker);
		loadedAnova = true;
	}
	
	synchronized static public void loadRegnImages(Component theComponent) {
		if (loadedRegnImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		totalDevn = loadImage("anova/totalDevn.gif", tracker, totalDevn, theComponent);
		explDevn = loadImage("anova/explDevn.gif", tracker, explDevn, theComponent);
		residDevn = loadImage("anova/residDevn.gif", tracker, residDevn, theComponent);
			basicRegnDevns = new Image[3];
			basicRegnDevns[0] = totalDevn;
			basicRegnDevns[1] = explDevn;
			basicRegnDevns[2] = residDevn;
		
		totalSsq = loadImage("anova/totalSsq.gif", tracker, totalSsq, theComponent);
		explSsq = loadImage("anova/explSsq.gif", tracker, explSsq, theComponent);
		residSsq = loadImage("anova/residSsq.gif", tracker, residSsq, theComponent);
			basicRegnSsqs = new Image[3];
			basicRegnSsqs[0] = totalSsq;
			basicRegnSsqs[1] = explSsq;
			basicRegnSsqs[2] = residSsq;
		
		waitForLoad(tracker);
		loadedRegnImages = true;
	}
	
	synchronized static public void loadQuadImages(Component theComponent) {
		if (loadedQuadImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		quadTotalDevn = loadImage("anova/quadTotalDevn.gif", tracker, quadTotalDevn, theComponent);
		quadLinDevn = loadImage("anova/quadLinDevn.gif", tracker, quadLinDevn, theComponent);
		quadQuadDevn = loadImage("anova/quadQuadDevn.gif", tracker, quadQuadDevn, theComponent);
		quadResidDevn = loadImage("anova/quadResidDevn.gif", tracker, quadResidDevn, theComponent);
			quadDevns = new Image[4];
			quadDevns[0] = quadTotalDevn;
			quadDevns[1] = quadLinDevn;
			quadDevns[2] = quadQuadDevn;
			quadDevns[3] = quadResidDevn;
		
		quadTotalSsq = loadImage("anova/quadTotalSsq.gif", tracker, quadTotalSsq, theComponent);
		quadLinSsq = loadImage("anova/quadLinSsq.gif", tracker, quadLinSsq, theComponent);
		quadQuadSsq = loadImage("anova/quadQuadSsq.gif", tracker, quadQuadSsq, theComponent);
		quadResidSsq = loadImage("anova/quadResidSsq.gif", tracker, quadResidSsq, theComponent);
			quadSsqs = new Image[4];
			quadSsqs[0] = quadTotalSsq;
			quadSsqs[1] = quadLinSsq;
			quadSsqs[2] = quadQuadSsq;
			quadSsqs[3] = quadResidSsq;
		
		quadTotalSsq2 = loadImage("anova/quadTotalSsq2.gif", tracker, quadTotalSsq2, theComponent);
		quadLinSsq2 = loadImage("anova/quadLinSsq2.gif", tracker, quadLinSsq2, theComponent);
		quadQuadSsq2 = loadImage("anova/quadQuadSsq2.gif", tracker, quadQuadSsq2, theComponent);
		quadResidSsq2 = loadImage("anova/quadResidSsq2.gif", tracker, quadResidSsq2, theComponent);
			quadSsqs2 = new Image[4];
			quadSsqs2[0] = quadTotalSsq2;
			quadSsqs2[1] = quadLinSsq2;
			quadSsqs2[2] = quadQuadSsq2;
			quadSsqs2[3] = quadResidSsq2;
		
		waitForLoad(tracker);
		loadedQuadImages = true;
	}
	
	synchronized static public void loadPureImages(Component theComponent) {
		if (loadedPureImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		pureTotalDevn = loadImage("anova/pureTotalDevn.gif", tracker, pureTotalDevn, theComponent);
		pureLinDevn = loadImage("anova/pureLinDevn.gif", tracker, pureLinDevn, theComponent);
		pureNonlinDevn = loadImage("anova/pureNonlinDevn.gif", tracker, pureNonlinDevn, theComponent);
		pureResidDevn = loadImage("anova/pureResidDevn.gif", tracker, pureResidDevn, theComponent);
			pureDevns = new Image[4];
			pureDevns[0] = pureTotalDevn;
			pureDevns[1] = pureLinDevn;
			pureDevns[2] = pureNonlinDevn;
			pureDevns[3] = pureResidDevn;
		
		pureTotalSsq = loadImage("anova/pureTotalSsq.gif", tracker, pureTotalSsq, theComponent);
		pureLinSsq = loadImage("anova/pureLinSsq.gif", tracker, pureLinSsq, theComponent);
		pureNonlinSsq = loadImage("anova/pureNonlinSsq.gif", tracker, pureNonlinSsq, theComponent);
		pureResidSsq = loadImage("anova/pureResidSsq.gif", tracker, pureResidSsq, theComponent);
			pureSsqs = new Image[4];
			pureSsqs[0] = pureTotalSsq;
			pureSsqs[1] = pureLinSsq;
			pureSsqs[2] = pureNonlinSsq;
			pureSsqs[3] = pureResidSsq;
		
		quadTotalSsq2 = loadImage("anova/quadTotalSsq2.gif", tracker, quadTotalSsq2, theComponent);
		quadLinSsq2 = loadImage("anova/quadLinSsq2.gif", tracker, quadLinSsq2, theComponent);
		pureNonlinSsq2 = loadImage("anova/pureNonlinSsq2.gif", tracker, pureNonlinSsq2, theComponent);
		quadResidSsq2 = loadImage("anova/quadResidSsq2.gif", tracker, quadResidSsq2, theComponent);
			pureSsqs2 = new Image[4];
			pureSsqs2[0] = quadTotalSsq2;
			pureSsqs2[1] = quadLinSsq2;
			pureSsqs2[2] = pureNonlinSsq2;
			pureSsqs2[3] = quadResidSsq2;
		
		waitForLoad(tracker);
		loadedPureImages = true;
	}
	
	synchronized static public void loadXZImages(Component theComponent) {
		if (loadedXZImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		xzTotalSsq = loadImage("anova/xzTotalSsq.gif", tracker, xzTotalSsq, theComponent);
		xzResidSsq = loadImage("anova/xzResidSsq.gif", tracker, xzResidSsq, theComponent);
		xzExplXSsq = loadImage("anova/xzExplXSsq.gif", tracker, xzExplXSsq, theComponent);
		xzExplZAfterXSsq = loadImage("anova/xzExplZAfterXSsq.gif", tracker, xzExplZAfterXSsq, theComponent);
		xzExplZSsq = loadImage("anova/xzExplZSsq.gif", tracker, xzExplZSsq, theComponent);
		xzExplXAfterZSsq = loadImage("anova/xzExplXAfterZSsq.gif", tracker, xzExplXAfterZSsq, theComponent);
			xThenZSsqs = new Image[4];
			xThenZSsqs[0] = xzTotalSsq;
			xThenZSsqs[1] = xzExplXSsq;
			xThenZSsqs[2] = xzExplZAfterXSsq;
			xThenZSsqs[3] = xzResidSsq;
		
			zThenXSsqs = new Image[4];
			zThenXSsqs[0] = xzTotalSsq;
			zThenXSsqs[1] = xzExplZSsq;
			zThenXSsqs[2] = xzExplXAfterZSsq;
			zThenXSsqs[3] = xzResidSsq;
		
		waitForLoad(tracker);
		loadedXZImages = true;
	}
	
	synchronized static public void loadGroupImages(Component theComponent) {
		if (loadedGroupImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		totalDevn2 = loadImage("anova/totalDevn2.gif", tracker, totalDevn2, theComponent);
		explDevn2 = loadImage("anova/explDevn2.gif", tracker, explDevn2, theComponent);
		residDevn2 = loadImage("anova/residDevn2.gif", tracker, residDevn2, theComponent);
			basicGroupDevns = new Image[3];
			basicGroupDevns[0] = totalDevn2;
			basicGroupDevns[1] = explDevn2;
			basicGroupDevns[2] = residDevn2;
		
		totalSsq2 = loadImage("anova/totalSsq2.gif", tracker, totalSsq2, theComponent);
		explSsq2 = loadImage("anova/explSsq2.gif", tracker, explSsq2, theComponent);
		residSsq2 = loadImage("anova/residSsq2.gif", tracker, residSsq2, theComponent);
			basicGroupSsqs = new Image[3];
			basicGroupSsqs[0] = totalSsq2;
			basicGroupSsqs[1] = explSsq2;
			basicGroupSsqs[2] = residSsq2;
		
		waitForLoad(tracker);
		loadedGroupImages = true;
	}
	
	synchronized static public void loadRawImages(Component theComponent) {
		if (loadedRawImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		rawTotalDevn = loadImage("anova/rawTotalDevn.gif", tracker, rawTotalDevn, theComponent);
		rawMeanDevn = loadImage("anova/rawMeanDevn.gif", tracker, rawMeanDevn, theComponent);
		rawResidDevn = loadImage("anova/rawResidDevn.gif", tracker, rawResidDevn, theComponent);
			rawDevns = new Image[3];
			rawDevns[0] = rawTotalDevn;
			rawDevns[1] = rawMeanDevn;
			rawDevns[2] = rawResidDevn;
		
		rawTotalSsq = loadImage("anova/rawTotalSsq.gif", tracker, rawTotalSsq, theComponent);
		rawMeanSsq = loadImage("anova/rawMeanSsq.gif", tracker, rawMeanSsq, theComponent);
		rawResidSsq = loadImage("anova/rawResidSsq.gif", tracker, rawResidSsq, theComponent);
			rawSsqs = new Image[3];
			rawSsqs[0] = rawTotalSsq;
			rawSsqs[1] = rawMeanSsq;
			rawSsqs[2] = rawResidSsq;
		
		waitForLoad(tracker);
		loadedRawImages = true;
	}
	
	synchronized static public void loadGroup2Images(Component theComponent) {
		if (loadedGroup2Images)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		group2TotalDevn = loadImage("anova/group2TotalDevn.gif", tracker, group2TotalDevn, theComponent);
		group2MeanDevn = loadImage("anova/group2MeanDevn.gif", tracker, group2MeanDevn, theComponent);
		group2Resid0Devn = loadImage("anova/group2Resid0Devn.gif", tracker, group2Resid0Devn, theComponent);
		group2Resid1Devn = loadImage("anova/group2Resid1Devn.gif", tracker, group2Resid1Devn, theComponent);
			group2Devns = new Image[4];
			group2Devns[0] = group2TotalDevn;
			group2Devns[1] = group2MeanDevn;
			group2Devns[2] = group2Resid0Devn;
			group2Devns[3] = group2Resid1Devn;
		
		group2TotalSsq = loadImage("anova/group2TotalSsq.gif", tracker, group2TotalSsq, theComponent);
		group2MeanSsq = loadImage("anova/group2MeanSsq.gif", tracker, group2MeanSsq, theComponent);
		group2Resid0Ssq = loadImage("anova/group2Resid0Ssq.gif", tracker, group2Resid0Ssq, theComponent);
		group2Resid1Ssq = loadImage("anova/group2Resid1Ssq.gif", tracker, group2Resid1Ssq, theComponent);
			group2Ssqs = new Image[4];
			group2Ssqs[0] = group2TotalSsq;
			group2Ssqs[1] = group2MeanSsq;
			group2Ssqs[2] = group2Resid0Ssq;
			group2Ssqs[3] = group2Resid1Ssq;
		
		waitForLoad(tracker);
		loadedGroup2Images = true;
	}
	
	synchronized static public void loadQuadXZImages(Component theComponent) {
		if (loadedQuadXZImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		quadXZTotalSsq = loadImage("anova/quadXZTotalSsq.gif", tracker, quadXZTotalSsq, theComponent);
		quadXZLinXZSsq = loadImage("anova/quadXZLinXZSsq.gif", tracker, quadXZLinXZSsq, theComponent);
		quadXZQuadXSsq = loadImage("anova/quadXZQuadXSsq.gif", tracker, quadXZQuadXSsq, theComponent);
		quadXZResidSsq = loadImage("anova/quadXZResidSsq.gif", tracker, quadXZResidSsq, theComponent);
			quadXZSsqs = new Image[4];
			quadXZSsqs[0] = quadXZTotalSsq;
			quadXZSsqs[1] = quadXZLinXZSsq;
			quadXZSsqs[2] = quadXZQuadXSsq;
			quadXZSsqs[3] = quadXZResidSsq;
		
		waitForLoad(tracker);
		loadedQuadXZImages = true;
	}
	
	synchronized static public void loadBlockImages(Component theComponent) {
		if (loadedBlockImages)
			return;
		loadCoreImages(theComponent);
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		blockTotalSsq = loadImage("anova/blockTotalSsq.gif", tracker, blockTotalSsq, theComponent);
		blockBlockSsq = loadImage("anova/blockBlockSsq.gif", tracker, blockBlockSsq, theComponent);
		blockTreatSsq = loadImage("anova/blockTreatSsq.gif", tracker, blockTreatSsq, theComponent);
		blockResidSsq = loadImage("anova/blockResidSsq.gif", tracker, blockResidSsq, theComponent);
			blockSsqs = new Image[4];
			blockSsqs[0] = blockTotalSsq;
			blockSsqs[1] = blockBlockSsq;
			blockSsqs[2] = blockTreatSsq;
			blockSsqs[3] = blockResidSsq;
		
		waitForLoad(tracker);
		loadedBlockImages = true;
	}
}