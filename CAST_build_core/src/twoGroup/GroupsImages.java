package twoGroup;

import java.awt.*;

import imageGroups.AudioVisual;


public class GroupsImages extends AudioVisual {
	static public Image mu[] = new Image[3];
	static public Image sigma[] = new Image[3];
	static public Image jointSigma;
	
	static public Image muHat[] = new Image[2];
	static public Image sigmaHat[] = new Image[2];
	
	static public Image xBar[] = new Image[2];
	static public Image s[] = new Image[2];
	static public Image n[] = new Image[2];
	static public Image p[] = new Image[2];
	
	static public Image muDiffHat, xBarDiff, sdDiffHat, piDiffHat, sdPDiffHat, pDiff,
							sdDiffHatPooled;
	
	static final public int kMuParamWidth = 31;
	static final public int kMuParamAscent = 9;
	static final public int kMuParamDescent = 6;
	static final public int kMuParamHeight = kMuParamAscent + kMuParamDescent;		//		=15
	static final public int kJointSigmaWidth = 26;
	
	static final public int kMuHatWidth = 69;
	static final public int kMuHatAscent = 15;
	static final public int kMuHatDescent = 6;
	static final public int kMuHatHeight = kMuHatAscent + kMuHatDescent;		//		=21
	
	static final public int kXBarWidth = 32;
	static final public int kXBarAscent = 11;
	static final public int kXBarDescent = 6;
	static final public int kXBarHeight = kXBarAscent + kXBarDescent;		//		=17
	
	static final public int kMuDiffHatWidth = 124;
	static final public int kPiDiffHatWidth = 151;
	static final public int kMuDiffHatAscent = 17;
	static final public int kMuDiffHatDescent = 6;
	static final public int kMuDiffHatHeight = kMuDiffHatAscent + kMuDiffHatDescent;		//		=23
	
	static final public int kXBarDiffWidth = 64;
	static final public int kXBarDiffAscent = 11;
	static final public int kXBarDiffDescent = 6;
	static final public int kXBarDiffHeight = kXBarDiffAscent + kXBarDiffDescent;		//		=17
	
	static final public int kSDDiffHatWidth = 156;
	static final public int kSDPDiffHatWidth = 169;
	static final public int kSDDiffHatAscent = 31;
	static final public int kSDDiffHatDescent = 15;
	static final public int kSDDiffHatHeight = kSDDiffHatAscent + kSDDiffHatDescent;		//		=46
	
	static final public int kSDPooledDiffWidth = 210;
	static final public int kSDPooledDiffAscent = 27;
	static final public int kSDPooledDiffDescent = 11;
	static final public int kSDPooledDiffHeight = kSDPooledDiffAscent + kSDPooledDiffDescent;		//		=38
	
	static public boolean loadedAnova = false;
	
	synchronized static public void loadGroups(Component theComponent) {
		if (loadedAnova)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		mu[0] = loadImage("groups/mu1.png", tracker, mu[0], theComponent);
		mu[1] = loadImage("groups/mu2.png", tracker, mu[1], theComponent);
		mu[2] = loadImage("groups/mu3.png", tracker, mu[2], theComponent);
		sigma[0] = loadImage("groups/sigma1.png", tracker, sigma[0], theComponent);
		sigma[1] = loadImage("groups/sigma2.png", tracker, sigma[1], theComponent);
		sigma[2] = loadImage("groups/sigma3.png", tracker, sigma[2], theComponent);
		jointSigma = loadImage("groups/jointSigma.png", tracker, jointSigma, theComponent);
		
		muHat[0] = loadImage("groups/mu1Hat.png", tracker, muHat[0], theComponent);
		muHat[1] = loadImage("groups/mu2Hat.png", tracker, muHat[1], theComponent);
		sigmaHat[0] = loadImage("groups/sigma1Hat.png", tracker, sigmaHat[0], theComponent);
		sigmaHat[1] = loadImage("groups/sigma2Hat.png", tracker, sigmaHat[1], theComponent);
		xBar[0] = loadImage("groups/x1Bar.png", tracker, xBar[0], theComponent);
		xBar[1] = loadImage("groups/x2Bar.png", tracker, xBar[1], theComponent);
		s[0] = loadImage("groups/s1.png", tracker, s[0], theComponent);
		s[1] = loadImage("groups/s2.png", tracker, s[1], theComponent);
		n[0] = loadImage("groups/n1.png", tracker, n[0], theComponent);
		n[1] = loadImage("groups/n2.png", tracker, n[1], theComponent);
		p[0] = loadImage("groups/p1.png", tracker, p[0], theComponent);
		p[1] = loadImage("groups/p2.png", tracker, p[1], theComponent);
		
		muDiffHat = loadImage("groups/muDiffHat.png", tracker, muDiffHat, theComponent);
		xBarDiff = loadImage("groups/xBarDiff.png", tracker, xBarDiff, theComponent);
		sdDiffHat = loadImage("groups/sdDiffHat.png", tracker, sdDiffHat, theComponent);
		piDiffHat = loadImage("groups/piDiffHat.png", tracker, piDiffHat, theComponent);
		sdPDiffHat = loadImage("groups/sdPDiffHat.png", tracker, sdPDiffHat, theComponent);
		pDiff = loadImage("groups/pDiff.png", tracker, pDiff, theComponent);
		
		sdDiffHatPooled = loadImage("groups/sdDiffHatPooled.png", tracker, sdDiffHatPooled, theComponent);
		
		waitForLoad(tracker);
		loadedAnova = true;
	}
}