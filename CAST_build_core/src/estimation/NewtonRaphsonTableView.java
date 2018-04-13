package estimation;

import java.awt.*;

import dataView.*;


public class NewtonRaphsonTableView extends DataView {
	static final private int kColumnSpacing = 20;
	static final private int kHeadingVertGap = 8;
	static final private int kRowSpacing = 8;
	
	private String thetaKey, d1Key, d2Key;
	
	public NewtonRaphsonTableView(DataSet theData, XApplet applet, String thetaKey, String d1Key, String d2Key) {
		super(theData, applet, null);
		this.thetaKey = thetaKey;
		this.d1Key = d1Key;
		this.d2Key = d2Key;
	}
	
	public void paintView(Graphics g) {
		g.setFont(getApplet().getBigFont());
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int lineHeight = ascent + fm.getDescent();
		
		String iterationHeading = getApplet().translate("Iteration");
		int iterationWidth = fm.stringWidth(iterationHeading);
		
		NumVariable theta = (NumVariable)getData().getVariable(thetaKey);
		NumVariable d1 = (NumVariable)getData().getVariable(d1Key);
		NumVariable d2 = (NumVariable)getData().getVariable(d2Key);
		
		int thetaWidth = theta.getMaxWidth(g);
		int d1Width = d1.getMaxWidth(g);
		int d2Width = d2.getMaxWidth(g);
		LabelValue thetaHeading = new LabelValue("\u03B8");
		LabelValue d1Heading = new LabelValue("\u2113\u2032(\u03B8)");
		LabelValue d2Heading = new LabelValue("\u2113\u2033(\u03B8)");
		
//		int iterationRows = theta.noOfValues();
		int leftRightMargin = (getWidth() - iterationWidth - thetaWidth - d1Width - d2Width - 5 * kColumnSpacing) / 2;
		int iterationsCenter = leftRightMargin + kColumnSpacing + iterationWidth / 2;
		
		int thetaRight = leftRightMargin + iterationWidth + 2 * kColumnSpacing + thetaWidth;
		int thetaCenter = thetaRight - thetaWidth / 2;
		
		int d1Right = thetaRight + kColumnSpacing + d1Width;
		int d1Center = d1Right - d1Width / 2;
		
		int d2Right = d1Right + kColumnSpacing + d2Width;
		int d2Center = d2Right - d2Width / 2;
		
		int headingBaseline = lineHeight + kHeadingVertGap;
		(new LabelValue(iterationHeading)).drawCentred(g, iterationsCenter, headingBaseline);
		thetaHeading.drawCentred(g, thetaCenter, headingBaseline);
		d1Heading.drawCentred(g, d1Center, headingBaseline);
		d2Heading.drawCentred(g, d2Center, headingBaseline);
		
		int nRows = theta.noOfValues();
		int tableHeight = nRows * lineHeight + (nRows + 1) * kRowSpacing;
		int tableTop = headingBaseline + kHeadingVertGap;
		g.setColor(Color.white);
		g.fillRect(leftRightMargin, tableTop, getWidth() - 2 * leftRightMargin, tableHeight);
		
		g.setColor(getForeground());
		g.drawLine(leftRightMargin, tableTop, getWidth() - leftRightMargin, tableTop);
		g.drawLine(leftRightMargin, tableTop + tableHeight, getWidth() - leftRightMargin, tableTop + tableHeight);
		
		int baseline = tableTop + kRowSpacing + ascent;
		for (int i=0 ; i<nRows ; i++) {
			(new NumValue(i, 0)).drawCentred(g, iterationsCenter, baseline);
			theta.valueAt(i).drawLeft(g, thetaRight, baseline);
			if (i < nRows - 1) {
				d1.valueAt(i).drawLeft(g, d1Right, baseline);
				d2.valueAt(i).drawLeft(g, d2Right, baseline);
			}
			baseline += lineHeight + kRowSpacing;
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}