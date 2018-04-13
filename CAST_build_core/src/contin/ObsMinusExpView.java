package contin;

import java.awt.*;

import dataView.*;


public class ObsMinusExpView extends DataView {
//	static public final String OBSMINUSEXPVIEW = "obsMinusExpView";
	
	static final private int kRowGap = 10;
	static final private int kVertBorder = 5;
	
	static final private int kHalfMinColGap = 10;
	
//	static final private int kYLabelTopGap = 6;
//	static final private int kRightMarginGap = 6;
//	static final private int kBottomMarginGap = 3;
	
	private ObsExpTableView oeView;
	private NumValue maxDiff;
	
	private boolean initialised = false;
	
	private int fontAscent;
	@SuppressWarnings("unused")
	private int fontDescent;
	
	private int displayHeight, displayWidth;
	private int nYCats, nXCats;
	private int maxDiffWidth;
	
	public ObsMinusExpView(DataSet theData, XApplet applet, ObsExpTableView oeView, NumValue maxDiff) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.oeView = oeView;
		this.maxDiff = maxDiff;
	}

//---------------------------------------------------------------------------------
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		int[][] obs = oeView.getObservedArray();
		nYCats = obs.length;
		nXCats = obs[0].length;
		
		maxDiffWidth = maxDiff.stringWidth(g);
		
		FontMetrics fm = g.getFontMetrics();
		fontAscent = fm.getAscent();
		fontDescent = fm.getDescent();
		
		initialised = true;
		return true;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		displayHeight = nXCats * fontAscent + (nXCats - 1) * kRowGap + 2 * kVertBorder;
		
		displayWidth = nYCats * (maxDiffWidth + 2 * kHalfMinColGap);
		return new Dimension(displayWidth, displayHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//---------------------------------------------------------------------------------
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int[][] obs = oeView.getObservedArray();
		double[][] exp = oeView.getExpectedArray();
		
		int boxLeft = (getSize().width - displayWidth) / 2;
		int boxTop =  (getSize().height - displayHeight) / 2;
		
		g.setColor(Color.white);
		g.fillRect(boxLeft, boxTop, displayWidth, displayHeight);
		
		g.setColor(getForeground());
		g.drawRect(boxLeft, boxTop, displayWidth - 1, displayHeight - 1);
		
//		int colCenter = boxLeft + kHalfMinColGap + maxDiffWidth / 2;
		
		int baseline = boxTop + kVertBorder + fontAscent;
		int lineHt = fontAscent + kRowGap;
		int colOneRight = boxLeft + kHalfMinColGap + maxDiffWidth;
		
		for (int i=0 ; i<nXCats ; i++) {
			int colRight = colOneRight;
			for (int j=0 ; j<nYCats ; j++) {
				new NumValue(obs[j][i] - exp[j][i], maxDiff.decimals)
																			.drawLeft(g, colRight, baseline);
				colRight += maxDiffWidth + 2 * kHalfMinColGap;
			}
			
			baseline += lineHt;
		}
	}

//-----------------------------------------------------------------------------------
	
	public void changedCategoryLabels() {
		initialised = false;
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
