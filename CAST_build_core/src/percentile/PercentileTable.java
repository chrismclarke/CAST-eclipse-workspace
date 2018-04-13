package percentile;

import java.awt.*;

import dataView.*;


public class PercentileTable extends DataView {
	static private final int kRowGap = 4;
	static private final int kValueGap = 10;
	static private final int kInnerLeftRightBorder = 5;
	static private final int kInnerTopBottomBorder = 4;
	static private final int kLabelRightBorder = 6;
	static private final int kLabelTopBorder = 4;
	static private final int kPercentileBottomGap = 4;
	
//	static public final String PERCENTILE_TABLE = "percentileTable";
	
	private LabelValue kPercentileTitle;
	
	private GroupedPercentileView bandView;
	
	private NumValue maxValue;
	private String catKey;
	
	private Font headingFont;
	
	private boolean initialised = false;
	private int ascent, headingAscent, maxValueWidth;
	private int noOfCats, catLabelWidth;
	private int innerLeft, innerTop, innerWidth, innerHeight;
	
	private int selectedCat = -1;
	
	public PercentileTable(DataSet theData, XApplet applet, GroupedPercentileView bandView,
																									String catKey, NumValue maxValue) {
		super(theData, applet, null);
		kPercentileTitle = new LabelValue(applet.translate("Percentiles"));
		this.bandView = bandView;
		this.catKey = catKey;
		this.maxValue = maxValue;
		bandView.setLinkedTable(this);
		headingFont = applet.getStandardBoldFont();
	}
	
	public void selectCat(int selectedCat) {
		this.selectedCat = selectedCat;
		repaint();
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		maxValueWidth = maxValue.stringWidth(g);
		
		CatVariable catVar = (CatVariable)getVariable(catKey);
		noOfCats = catVar.noOfCategories();
		catLabelWidth = catVar.getMaxWidth(g);
		int noOfProbs = bandView.getProbs().length;
		
		Font oldFont = g.getFont();
		g.setFont(headingFont);
		headingAscent = g.getFontMetrics().getAscent();
		g.setFont(oldFont);
		
		innerLeft = catLabelWidth + kLabelRightBorder;
		innerTop = headingAscent + kLabelTopBorder + ascent + kPercentileBottomGap;
		innerWidth = 2 * kInnerLeftRightBorder + noOfProbs * maxValueWidth + (noOfProbs - 1) * kValueGap;
		innerHeight = 2 * kInnerTopBottomBorder + noOfCats * ascent + (noOfCats - 1) * kRowGap;
		
		initialised = true;
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CatVariable catVar = (CatVariable)getVariable(catKey);
		
		g.setColor(Color.white);
		g.fillRect(innerLeft + 1, innerTop + 1, innerWidth, innerHeight);
		g.setColor(Color.black);
		g.drawRect(innerLeft, innerTop, innerWidth + 1, innerHeight + 1);
		g.setColor(getForeground());
		
		double prob[] = bandView.getProbs();
		int baseline = headingAscent;
		int titleCenter = innerLeft + innerWidth / 2;
		Font oldFont = g.getFont();
		g.setFont(headingFont);
		kPercentileTitle.drawCentred(g, titleCenter, baseline);
		g.setFont(oldFont);
		
		baseline += (ascent + kPercentileBottomGap);
		int percentCenter = innerLeft + 1 + kInnerLeftRightBorder + maxValueWidth / 2;
		NumValue percentVal = new NumValue(0.0, 0);
		for (int i=0 ; i<prob.length ; i++) {
			percentVal.setValue(prob[i] * 100);
			percentVal.drawCentred(g, percentCenter, baseline);
			percentCenter += (maxValueWidth + kValueGap);
		}
		
		
		baseline = innerTop + 1 + kInnerTopBottomBorder + ascent;
		NumValue percentileVal = new NumValue(0.0, maxValue.decimals);
		for (int i=0 ; i<noOfCats ; i++) {
			if (i == selectedCat) {
				g.setColor(Color.yellow);
				g.fillRect(innerLeft + 1, baseline - ascent - kRowGap + 1, innerWidth - 2,
																																	ascent + 2 * kRowGap - 2);
				g.setColor(Color.red);
			}
			
			Value catLabel = catVar.getLabel(i);
			catLabel.drawRight(g, 0, baseline);
			
			NumValue sortedSubset[] = bandView.getSortedSubset(i);
			int valRight = innerLeft + 1 + kInnerLeftRightBorder + maxValueWidth;
			for (int j=0 ; j<prob.length ; j++) {
				percentileVal.setValue(PercentileInfo.evaluatePercentile(sortedSubset, prob[j],
																																			PercentileInfo.SMOOTH));
				percentileVal.drawLeft(g, valRight, baseline);
				valRight += (maxValueWidth + kValueGap);
			}
			if (i == selectedCat)
				g.setColor(getForeground());
			
			baseline += ascent + kRowGap;
		}
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		g.setFont(getFont());
		initialise(g);
		return new Dimension(innerLeft + innerWidth + 2, innerTop + innerHeight + 2);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		return getPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y < innerTop + 1 || y > innerTop + innerHeight - 2)
			return null;
		
		int hitCat = (y - innerTop - 1 - (kInnerTopBottomBorder + kRowGap)) / (ascent + kRowGap);
		hitCat = Math.max(0, Math.min(hitCat, noOfCats - 1));
		return new CatPosInfo(hitCat, true);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		CatPosInfo catPos = (CatPosInfo)startInfo;
		selectedCat = catPos.catIndex;
		repaint();
		if (bandView != null)
			bandView.selectCat(selectedCat);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedCat = -1;
			repaint();
		if (bandView != null)
			bandView.selectCat(-1);
		}
		else
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedCat = -1;
		repaint();
		if (bandView != null)
			bandView.selectCat(-1);
	}
}
