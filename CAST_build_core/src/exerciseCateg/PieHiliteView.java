package exerciseCateg;

import java.awt.*;

import dataView.*;


public class PieHiliteView extends DataView {
//	static final public String PIE_HILITE = "pieHilite";
	
	static final private int kPercentHorizBorder = 5;
	static final private int kPercentTopBorder = 2;
	static final private int kPercentBottomBorder = 4;
	
	static final private int kPercentDecimals = 0;
	
	static final private Color kArrowColor = Color.black;

	private String catKey;
	
	private int lowIndex = -1, highIndex = -1;
	private boolean showPercent;
	
	private double cumCount[] = null;
	private int totalCount;
	
	protected boolean initialised = false;
	private PieDrawer pieDrawer;
	
	public PieHiliteView(DataSet theData, XApplet applet, String catKey, PieDrawer pieDrawer) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
		this.pieDrawer = pieDrawer;
	}
	
	
	public void setHilite(int lowIndex, int highIndex, boolean showPercent) {
		this.lowIndex = lowIndex;
		this.highIndex = highIndex;
		this.showPercent = showPercent;
	}
	
	public void clearHilite() {
		setHilite(-1, -1, false);
	}

//-------------------------------------------------------------------
	
	protected boolean initialise() {
		if (!initialised) {
			CatVariable yVar = (CatVariable)getVariable(catKey);
			int[] counts = yVar.getCounts();
			
			totalCount = yVar.noOfValues();
			int nCats = yVar.noOfCategories();
			cumCount = new double[nCats];
			cumCount[0] = counts[0];
			for (int i=1 ; i<nCats ; i++)
				cumCount[i] = cumCount[i - 1] + counts[i];
			
			lowIndex = highIndex = -1;
			
//			pieDrawer.setRadius(Math.min(getSize().height, getSize().width) / 2 - 1, this);
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		pieDrawer.setRadius(Math.min(getSize().height, getSize().width) / 2 - 1, this);
		pieDrawer.fillPieSegments(g, cumCount, -1, -1);
		
		if (lowIndex >= 0 && highIndex >= lowIndex) {
			g.setColor(kArrowColor);
			pieDrawer.drawArc(g, cumCount, lowIndex, highIndex);
			
			if (showPercent) {
				Point midPoint = pieDrawer.findMidSegment(cumCount, lowIndex, highIndex);
				double count = cumCount[highIndex];
				if (lowIndex > 0)
					count -= cumCount[lowIndex - 1];
				NumValue percent = new NumValue(100 * count / totalCount, kPercentDecimals);
				LabelValue percentLabel = new LabelValue(percent.toString() + "%");
				drawPercent(g, percentLabel, midPoint);
			}
		}
	}
	
	private void drawPercent(Graphics g, LabelValue percentLabel, Point midPoint) {
		double midPropn = pieDrawer.findPropn(midPoint.x, midPoint.y);
		boolean isTop = midPropn <= 0.25 || midPropn >= 0.75;
		boolean isLeft = midPropn > 0.5;
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int halfWidth = percentLabel.stringWidth(g) / 2 + 1 + kPercentHorizBorder;
		int center = isLeft ? halfWidth : getSize().width - halfWidth ;
		int boxHeight = ascent + kPercentTopBorder + kPercentBottomBorder;
		int top = isTop ? 0 : getSize().height - boxHeight;
		int lineX = isLeft ? 2 * halfWidth : getSize().width - 2 * halfWidth;
		int lineY = isTop ? boxHeight : top;
		
		g.setColor(Color.yellow);
		g.fillRect(center - halfWidth, top, 2 * halfWidth, boxHeight);

		g.setColor(kArrowColor);
		
		int baseline = top + kPercentTopBorder + ascent;
		percentLabel.drawCentred(g, center, baseline);
		
		g.drawLine(midPoint.x, midPoint.y, lineX, lineY);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (catKey.equals(key)) {
			initialised = false;
			repaint();
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