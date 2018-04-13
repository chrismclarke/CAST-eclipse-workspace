package exper2;

import java.awt.*;

import dataView.*;


public class TreatmentPermView extends DataView {
	static final private Color kGridColor = new Color(0x666666);
	static final private Color kTreatColor[] = IncompleteDesignView.kTreatColor;
	
	static final private int kFactorNameGap = 6;
	static final private int kRectVertBorder = 12;
	
	static final private int kNoOfFrames = 100;
	static final private int kFramesPerSec = 40;
	
	private IncompleteDesignView latinSquare;
	
	private LabelValue factorName;
	private LabelValue[] treatName;
	private int[] treatPerm, treatPermNext;
	
	public TreatmentPermView(LabelValue factorName, LabelValue[] treatName, IncompleteDesignView latinSquare, XApplet applet) {
		super(new DataSet(), applet, null);
		this.factorName = factorName;
		this.treatName = treatName;
		this.latinSquare = latinSquare;
		treatPerm = IncompleteDesignView.initialPerm(treatName.length);
	}
	
	public void permuteTreats() {
		if (treatPermNext == null)
			treatPermNext = new int[treatPerm.length];
		else
			treatPerm = (int[])treatPermNext.clone();
		IncompleteDesignView.newPermutation(treatPermNext);
		animateFrames(0, kNoOfFrames, kFramesPerSec, null);
		repaint();
		latinSquare.setTreatPerm(null);
	}
	
	protected void drawNextFrame() {
		super.drawNextFrame();
		if (getCurrentFrame() == kNoOfFrames) {
			int inversePerm[] = new int[treatPermNext.length];
			for (int i=0 ; i<treatPermNext.length ; i++)
				inversePerm[treatPermNext[i]] = i;
			latinSquare.setTreatPerm(inversePerm);
		}
	}
	
	private int getTreatCenter(int treat, int cellWidth) {
//		int permTreat = treatPerm[treat];
		int center = treat * cellWidth + cellWidth / 2;
		if (treatPermNext != null) {
			int currentFrame = getCurrentFrame();
			int nextPermTreat = treatPermNext[treat];
			int nextCenter = nextPermTreat * cellWidth + cellWidth / 2;
			center = (currentFrame * nextCenter + (kNoOfFrames - currentFrame) * center) / kNoOfFrames;
		}
		return center;
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int topBorder = ascent + descent + kFactorNameGap;
		
		factorName.drawCentred(g, getSize().width / 2, ascent);
		
		int cellWidth = (getSize().width - 1) / treatName.length;
		Rectangle r = new Rectangle(0, topBorder, cellWidth, getSize().height - topBorder - 1);
		for (int treat=0 ; treat<treatName.length ; treat++) {
			r.x = treat * cellWidth;
			g.setColor(kTreatColor[treat]);
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(kGridColor);
			g.drawRect(r.x, r.y, r.width, r.height);
		}
		g.setColor(getForeground());
		
		int treatBaseline = r.y + (r.height + ascent - descent) / 2;
		
		for (int treat=0 ; treat<treatName.length ; treat++) {
			int center = getTreatCenter(treat, cellWidth);
			treatName[treat].drawCentred(g, center, treatBaseline);
		}
	}
	
	public Dimension getMinimumSize() {
		FontMetrics fm = getGraphics().getFontMetrics();
		int height = (fm.getAscent() + fm.getDescent()) * 2 + kFactorNameGap
																														+ 2 * kRectVertBorder;
		return new Dimension(50, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}