package exerciseCateg;

import java.awt.*;

import dataView.*;
import axis.*;


abstract public class CoreCumulativeView extends DataView {
	
	static final protected int kArrowHead = 5;
	static final private int kCountGap = 5;
	static final private int kMinHalfBarWidth = 4;
	static final private int kMaxHalfBarWidth = 8;
	
	static final protected Color kBarOutlineColour = Color.black;
	static final protected Color kBarDimOutlineColour = new Color(0x666666);
	static final protected Color kBarFillColour = new Color(0xAAAAAA);
	static final protected Color kBarDimFillColour = new Color(0xCCCCCC);
	
	static final protected Color kBarHiliteColour = Color.yellow;
	static final private Color kCumulativeColor = new Color(0x000099);
	static final private Color kCumExtremeColor = new Color(0xEEEEEE);
	static final private Color kHundredPercentColor = new Color(0xDDDDDD);
	
	protected String yKey;
	protected HorizAxis valAxis;
	protected VertAxis leftAxis, rightAxis;
	
	private boolean catPropns = true;
	private boolean cumPropns = true;
	
	public CoreCumulativeView(DataSet theData, XApplet applet,
												String yKey, HorizAxis valAxis, VertAxis leftAxis, VertAxis rightAxis) {
		super(theData, applet, new Insets(2, 5, 0, 5));
		this.yKey = yKey;
		this.valAxis = valAxis;
		this.leftAxis = leftAxis;
		this.rightAxis = rightAxis;
	}
	
	abstract protected int[] getCounts();
	
	public void setCatPropns(boolean catPropns) {
		this.catPropns = catPropns;
	}
	
	public void setCumPropns(boolean cumPropns) {
		this.cumPropns = cumPropns;
	}

//-------------------------------------------------------------------
	
	protected int getHalfBarWidth() {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int nCats = yVar.noOfCategories();
		return Math.min(kMaxHalfBarWidth, Math.max(kMinHalfBarWidth, getSize().width / (6 * nCats)));
	}
	
	protected void drawBackground(Graphics g, double hundredPercentVal) {
		int totalPos = rightAxis.numValToRawPosition(hundredPercentVal);	
		Point p = translateToScreen(0, totalPos, null);
		g.setColor(kCumExtremeColor);
		g.fillRect(0, 0, getSize().width, p.y);
		g.setColor(kHundredPercentColor);
		g.drawLine(0, p.y, getSize().width, p.y);
	}
	
	protected void drawCumulative(Graphics g, double scaleFactor) {
		Point p = null;
		Point p0 = null;
		
		int count[] = getCounts();
		int cumCount = 0;
		g.setColor(kCumulativeColor);
		for (int i=0 ; i<count.length ; i++) {
			cumCount += count[i];
			int cumHt = rightAxis.numValToRawPosition(cumCount * scaleFactor);
			int xPos = valAxis.catValToPosition(i);
			p = translateToScreen(xPos, cumHt, p);
			
			g.fillOval(p.x - 3, p.y - 3, 6, 6);
			
			if (p != null && p0 != null)
				g.drawLine(p.x, p.y, p0.x, p0.y);
			
			Point pTemp = p;
			p = p0;
			p0 = pTemp;
		}
	}
	
	protected void drawPropns(Graphics g, int barIndex, double barAxisValue, double cumAxisValue) {
		Point p = null;
		NumValue countVal = new NumValue(0.0, 0);
		
		int count[] = getCounts();
		int total = 0;
		for (int i=0 ; i<count.length ; i++)
			total += count[i];
		
		int cumCount = 0;
		for (int i=0 ; i<=barIndex ; i++)
			cumCount += count[i];
		
		g.setColor(Color.red);
		g.setFont(getApplet().getStandardBoldFont());
		int xPos = valAxis.catValToPosition(barIndex);
		
		int barHt = leftAxis.numValToRawPosition(barAxisValue);
		p = translateToScreen(xPos, barHt, p);
		
		int halfBarWidth = getHalfBarWidth();
		g.drawLine(0, p.y, p.x - halfBarWidth, p.y);
		g.drawLine(0, p.y, kArrowHead, p.y - kArrowHead);
		g.drawLine(0, p.y, kArrowHead, p.y + kArrowHead);
		
		drawCount(g, count[barIndex], total, countVal, kArrowHead + 4, p.y, catPropns);
		
		int cumHt = rightAxis.numValToRawPosition(cumAxisValue);
		p = translateToScreen(xPos, cumHt, p);

		int rightPos = getSize().width - 1;
		g.drawLine(p.x, p.y, rightPos, p.y);
		g.drawLine(rightPos, p.y, rightPos - kArrowHead, p.y - kArrowHead);
		g.drawLine(rightPos, p.y, rightPos - kArrowHead, p.y + kArrowHead);
		
		drawCount(g, cumCount, total, countVal, -(kArrowHead + 5), p.y, cumPropns);
	}
	
	private void drawCount(Graphics g, int count, int total, NumValue val, int offsetFromEdge,
																																			int arrowVert, boolean isFraction) {
		int baseline = arrowVert - kCountGap;
		g.setFont(isFraction ? getApplet().getSmallBoldFont() : getApplet().getStandardBoldFont());
		int ascent = g.getFontMetrics().getAscent();
		int countHt = (isFraction ? (2 * ascent + 3) : ascent) + kCountGap;
		if (countHt > arrowVert)
			baseline = arrowVert + countHt;
		
		if (isFraction) {
			val.setValue(total);
			int valWidth = val.stringWidth(g);
			int halfWidth = (valWidth + 1) / 2;
			int centre = (offsetFromEdge > 0) ? (offsetFromEdge + halfWidth)
																				: (getSize().width + offsetFromEdge - halfWidth);
			val.drawCentred(g, centre, baseline);
			baseline -= (ascent + 1);
			g.drawLine(centre - halfWidth, baseline, centre + halfWidth, baseline);
			baseline -= 1;
			val.setValue(count);
			val.drawCentred(g, centre, baseline);
		}
		else {
			val.setValue(count);
			if (offsetFromEdge > 0)
				val.drawRight(g, offsetFromEdge, baseline);
			else
				val.drawLeft(g, getSize().width + offsetFromEdge, baseline);
		}
	}
}
	
