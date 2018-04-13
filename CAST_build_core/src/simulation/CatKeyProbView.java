package simulation;

import java.awt.*;

import dataView.*;


public class CatKeyProbView extends DataView {
	static final private int kTopBottomBorder = 5;
	static final private int kLeftRightBorder = 10;
	static final private int kShadeTopBottom = 4;
//	static final private int kShadeLeftRight = 10;
	static final private int kLineGap = 4;
	static final private NumValue kZeroProb = new NumValue(0.0, 3);
	
	private String catKey;
	private String maxCatLabel;
	private int maxLabels;
	
	private boolean initialised = false;
	private int ascent, descent;
	
	private int rowHeight, probOffset;
	private int minWidth, minHeight;
	
	public CatKeyProbView(DataSet theData, XApplet applet, String catKey, String maxCatLabel,
										int maxLabels) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.catKey = catKey;
		this.maxCatLabel = maxCatLabel;
		this.maxLabels = maxLabels;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			rowHeight = ascent + descent + 2 * kShadeTopBottom;
			
			minHeight = getHeight(maxLabels);
			probOffset = kLeftRightBorder + 1 + fm.stringWidth("P() = ")
																				+ fm.stringWidth(maxCatLabel);
			
			minWidth = probOffset + kZeroProb.stringWidth(g) + kLeftRightBorder + 1;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	private int getHeight(int noOfCatLabels) {
		return 2 * kTopBottomBorder + 2 + noOfCatLabels * rowHeight
																		+ (noOfCatLabels - 1) * kLineGap;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(minWidth, minHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		PseudoRandCatVariable catVar = (PseudoRandCatVariable)getVariable(catKey);
		int noOfCats = catVar.noOfCategories();
		double cumProb[] = catVar.getCumProbs();
		
//		int heightUsed = getHeight(noOfCats);
		
		int leftRightOutside = (getSize().width - minWidth) / 2;
		
		NumValue probValue = new NumValue(0.0, kZeroProb.decimals);
		LabelValue probEquals = new LabelValue("");
		int baseline = kTopBottomBorder + 1 + kShadeTopBottom + ascent;
		
		for (int i=noOfCats-1 ; i>=0 ;  i--) {
			Color backgroundColor = (i == noOfCats - 1) ? Color.white
														: RandomCatGeneratorView.getBackgroundColor(i);
			
			g.setColor(backgroundColor);
			g.fillRect(leftRightOutside, baseline - ascent - kShadeTopBottom, minWidth, rowHeight);
			g.setColor(getForeground());
			g.drawRect(leftRightOutside, baseline - ascent - kShadeTopBottom, minWidth - 1, rowHeight - 1);
			
			probValue.setValue((i == noOfCats-1) ? (1.0 - cumProb[i-1])
									: (i == 0) ? cumProb[0]
									: (cumProb[i] - cumProb[i-1]));
			probEquals.label = "P(" + ((LabelValue)catVar.getLabel(i)).label + ") = ";
			
			probEquals.drawLeft(g, leftRightOutside + probOffset, baseline);
			probValue.drawRight(g, leftRightOutside + probOffset, baseline);
			
			baseline += (rowHeight + kLineGap);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doAddValues(Graphics g, int noOfValues) {
									//		adding values does not change key
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (catKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
