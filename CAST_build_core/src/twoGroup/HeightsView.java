package twoGroup;

import java.awt.*;

import dataView.*;
import models.*;


public class HeightsView extends DataView {
	static final private int kMinWidth = 250;
	static final private int kMinHeight = 250;
	static final private int kTopBottomBorder = 5;
	static final private int kLeftBorder = 5;
	static final private int kRightBorder = 5;
	static final private int kMaleFemaleGap = 15;
	static final private int kArrowSize = 5;
	
//	private String yKey, xKey;
	private NumValue maxHeight;
	
	public HeightsView(DataSet theData, XApplet applet,
						               String yKey, String xKey, NumValue maxHeight) {
		super(theData, applet, new Insets(0, 0, 0, 0));
//		this.yKey = yKey;
//		this.xKey = xKey;
		this.maxHeight = maxHeight;
	}
	
	public void paintView(Graphics g) {
		int bottom = getSize().height - kTopBottomBorder;
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int maxTop = kTopBottomBorder + ascent;
		
		DataSet data = getData();
		double maleValue, femaleValue;
		if (data instanceof GroupsDataSet) {
			GroupsDataSet anovaData = (GroupsDataSet)getData();
			maleValue = anovaData.getMean(1);
			femaleValue = anovaData.getMean(0);
		}
		else {
			NumVariable y1 = (NumVariable)data.getVariable("y1");
			NumVariable y2 = (NumVariable)data.getVariable("y2");
			maleValue = y1.doubleValueAt(0);
			femaleValue = y2.doubleValueAt(0);
		}
		
		double maleHt = (bottom - maxTop) * maleValue / maxHeight.toDouble();
		int maleTop = bottom - (int)Math.round(maleHt);
		double femaleHt = (bottom - maxTop) * femaleValue / maxHeight.toDouble();
		int femaleTop = bottom - (int)Math.round(femaleHt);
		
		int maleLeftPos = kLeftBorder;
		int maleRightPos = PersonPicture.drawMale(g, bottom, maleLeftPos, maleHt);
		g.setColor(PersonPicture.kMaleBorderColor);
		g.drawLine((maleLeftPos + maleRightPos) / 2, maleTop, getSize().width, maleTop);
		
		int maleValueBaseline = (maleTop > femaleTop) ? maleTop + ascent + 2 : maleTop - 2;
		new NumValue(maleValue, maxHeight.decimals).drawLeft(g, getSize().width - kRightBorder,
																								maleValueBaseline);
		
		int femaleLeftPos = maleRightPos + kMaleFemaleGap;
		int femaleRightPos = PersonPicture.drawFemale(g, bottom, femaleLeftPos, femaleHt);
		g.setColor(PersonPicture.kFemaleBorderColor);
		g.drawLine((femaleLeftPos + femaleRightPos) / 2, femaleTop, getSize().width, femaleTop);
		
		int femaleValueBaseline = (femaleTop >= maleTop) ? femaleTop + ascent + 2 : femaleTop - 2;
		new NumValue(femaleValue, maxHeight.decimals).drawLeft(g, getSize().width - kRightBorder,
																								femaleValueBaseline);
		
		int diffLineX = (femaleRightPos + getSize().width) / 2;
		
		g.setColor(Color.red);
		if (femaleTop >= maleTop + 2) {
			int yEnd = femaleTop - 1;
			int yStart = maleTop + 1;
			g.drawLine(diffLineX, yStart, diffLineX, yEnd);
			g.drawLine(diffLineX + 1, yStart, diffLineX + 1, yEnd);
			g.drawLine(diffLineX - kArrowSize, yEnd - kArrowSize, diffLineX, yEnd);
			g.drawLine(diffLineX + 1 + kArrowSize, yEnd - kArrowSize, diffLineX + 1, yEnd);
			
			g.drawLine(diffLineX + 1 - kArrowSize, yEnd - kArrowSize, diffLineX, yEnd - 1);
			g.drawLine(diffLineX + kArrowSize, yEnd - kArrowSize, diffLineX, yEnd - 1);
		}
		else if (femaleTop <= maleTop - 2) {
			int yEnd = femaleTop + 1;
			int yStart = maleTop - 1;
			g.drawLine(diffLineX + 1, yStart, diffLineX + 1, yEnd);
			g.drawLine(diffLineX - kArrowSize, yEnd + kArrowSize, diffLineX, yEnd);
			g.drawLine(diffLineX + 1 + kArrowSize, yEnd + kArrowSize, diffLineX + 1, yEnd);
			
			g.drawLine(diffLineX + 1 - kArrowSize, yEnd + kArrowSize, diffLineX, yEnd + 1);
			g.drawLine(diffLineX + kArrowSize, yEnd + kArrowSize, diffLineX + 1, yEnd + 1);
		}
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(kMinWidth, kMinHeight);
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
	
