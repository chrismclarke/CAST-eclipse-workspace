package twoGroup;

import java.awt.*;

import dataView.*;


public class Heights2View extends DataView {
	static final private int kMinWidth = 250;
	static final private int kMinHeight = 250;
	
	static final private int kTopBorder = 16;
	static final private int kBottomBorder = 5;
	static final private int kLeftRightBorder = 15;
	static final private int kFrameBottomBorder = 10;
	static final private int kDoorFrameWidth = 4;
	static final private int kDoorWidth = 61;
	static final private int kTopDoorDrop = 16;
	static final private int kBottomDoorDrop = 12;
	static final private int kHtLineOverlap = 25;
	
	static final private int kTextTopGap = 2;
	static final private int kArrowSize = 5;
	static final private int kArrowOffset = 10;
	static final private int kArrowTextGap = 3;
	
	static final private double kStandardMaleHt = 1.85;
	static final private double kStandardFemaleHt = 1.75;
	
	static final private Color kFrameColor = new Color(0x7DA9D8);
	static final private Color kFrameBorderColor = new Color(0x336699);
	static final private Color kDoorColor = new Color(0x8AB98B);
	static final private Color kDoorBorderColor = new Color(0x5A875B);
	
//	private String yKey, xKey;
	private NumValue frameHeight;
	
	public Heights2View(DataSet theData, XApplet applet,
																			String yKey, String xKey, NumValue frameHeight) {
		super(theData, applet, new Insets(0, 0, 0, 0));
//		this.yKey = yKey;
//		this.xKey = xKey;
		this.frameHeight = frameHeight;
	}
	
	private void drawArrow(Graphics g, int horiz, int start, int end, Color arrowColor) {
		g.setColor(arrowColor);
		if (end >= start + 2) {
			end --;
			start ++;
			g.drawLine(horiz, start, horiz, end);
			g.drawLine(horiz + 1, start, horiz + 1, end);
			g.drawLine(horiz - kArrowSize, end - kArrowSize, horiz, end);
			g.drawLine(horiz + 1 + kArrowSize, end - kArrowSize, horiz + 1, end);
			
			g.drawLine(horiz + 1 - kArrowSize, end - kArrowSize, horiz, end - 1);
			g.drawLine(horiz + kArrowSize, end - kArrowSize, horiz, end - 1);
		}
		else if (end <= start - 2) {
			end ++;
			start --;
			g.drawLine(horiz + 1, start, horiz + 1, end);
			g.drawLine(horiz - kArrowSize, end + kArrowSize, horiz, end);
			g.drawLine(horiz + 1 + kArrowSize, end + kArrowSize, horiz + 1, end);
			
			g.drawLine(horiz + 1 - kArrowSize, end + kArrowSize, horiz, end + 1);
			g.drawLine(horiz + kArrowSize, end + kArrowSize, horiz + 1, end + 1);
		}
	}
	
	private void fillDoorPoly(int[] x, int[] y, int top, int left, int doorWidth) {
		x[0] = x[1] = x[4] = left;
		x[2] = x[3] = left + doorWidth;
		y[0] = y[4] = kTopBorder;
		y[1] = getSize().height - kFrameBottomBorder;
		y[2] = y[1] - kBottomDoorDrop;
		y[3] = y[0] + kTopDoorDrop;
	}
	
	private void drawDoorFrame(Graphics g) {
		int right = getSize().width;
		g.setColor(kFrameColor);
		g.fillRect(0, 0, right, kTopBorder);
		g.fillRect(0, 0, kLeftRightBorder, getSize().height - kFrameBottomBorder);
		g.fillRect(right - kLeftRightBorder, 0, kLeftRightBorder, getSize().height - kFrameBottomBorder);
		
		g.setColor(kFrameBorderColor);
		int frameBottom = getSize().height - kFrameBottomBorder;
		g.drawLine(0, frameBottom, kLeftRightBorder - 1, frameBottom);
		g.drawLine(kLeftRightBorder - 1, frameBottom, kLeftRightBorder - 1, kTopBorder);
		g.drawLine(kLeftRightBorder - 1, kTopBorder, right - kLeftRightBorder, kTopBorder);
		g.drawLine(right - kLeftRightBorder, kTopBorder, right - kLeftRightBorder, frameBottom);
		g.drawLine(right - kLeftRightBorder, frameBottom, right, frameBottom);
		
		int[] x = new int[5];
		int[] y = new int[5];
		g.setColor(kDoorColor);
		fillDoorPoly(x, y, kTopBorder, kLeftRightBorder + kDoorFrameWidth, kDoorWidth);
		g.fillPolygon(x, y, 5);
		g.setColor(kDoorBorderColor);
		g.drawPolygon(x, y, 5);
		g.fillRect(kLeftRightBorder, kTopBorder, kDoorFrameWidth, frameBottom - kTopBorder);
		
		g.setColor(kDoorColor);
		fillDoorPoly(x, y, kTopBorder, right - kLeftRightBorder - kDoorFrameWidth, -kDoorWidth);
		g.fillPolygon(x, y, 5);
		g.setColor(kDoorBorderColor);
		g.drawPolygon(x, y, 5);
		g.fillRect(right - kLeftRightBorder - kDoorFrameWidth, kTopBorder, kDoorFrameWidth,
																																	frameBottom - kTopBorder);
	}
	
	public void paintView(Graphics g) {
		DataSet data = getData();
		double maleValue, femaleValue;
//		if (data instanceof AnovaDataSet) {
//			AnovaDataSet anovaData = (AnovaDataSet)getData();
//			maleValue = anovaData.getMean(1);
//			femaleValue = anovaData.getMean(0);
//		}
//		else {
			NumVariable y1 = (NumVariable)data.getVariable("y1");
			NumVariable y2 = (NumVariable)data.getVariable("y2");
			maleValue = y1.doubleValueAt(0);
			femaleValue = y2.doubleValueAt(0);
//		}
		
		drawDoorFrame(g);
		
		int innerLeft = kLeftRightBorder + kDoorFrameWidth;
		int innerRight = getSize().width - innerLeft;
		int maleCentre = innerLeft + (innerRight - innerLeft) / 4;
		int femaleCentre = getSize().width - maleCentre;
		
		int bottom = getSize().height - kBottomBorder;
		
		double maleHt = (frameHeight.toDouble() - maleValue) / frameHeight.toDouble() * (bottom - kTopBorder);
		double maleXScaling = Math.sqrt(kStandardMaleHt / (frameHeight.toDouble() - maleValue));
		PersonPicture.drawCenteredMale(g, bottom, maleCentre, maleHt, maleXScaling);
		
		double femaleHt = (frameHeight.toDouble() - femaleValue) / frameHeight.toDouble() * (bottom - kTopBorder);
		double femaleXScaling = Math.sqrt(kStandardFemaleHt / (frameHeight.toDouble() - femaleValue));
		PersonPicture.drawCenteredFemale(g, bottom, femaleCentre, femaleHt, femaleXScaling,
																																	Color.white, kDoorColor);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int baseline = kTopBorder + ascent + kTextTopGap;
		
		int maleTop = bottom - (int)Math.round(maleHt);
		g.setColor(PersonPicture.kMaleBorderColor);
		g.drawLine(innerLeft, maleTop, (innerLeft + innerRight) / 2 + kHtLineOverlap, maleTop);
		drawArrow(g, maleCentre + kArrowOffset, kTopBorder, maleTop, PersonPicture.kMaleBorderColor);
		new NumValue(maleValue, frameHeight.decimals).drawRight(g,
																				maleCentre + kArrowOffset + kArrowTextGap, baseline);
		
		int femaleTop = bottom - (int)Math.round(femaleHt);
		g.setColor(PersonPicture.kFemaleBorderColor);
		g.drawLine((innerLeft + innerRight) / 2 - kHtLineOverlap, femaleTop, innerRight - 1, femaleTop);
		drawArrow(g, femaleCentre - kArrowOffset, kTopBorder, femaleTop, PersonPicture.kFemaleBorderColor);
		new NumValue(femaleValue, frameHeight.decimals).drawLeft(g,
																				femaleCentre - kArrowOffset - kArrowTextGap, baseline);
		
		drawArrow(g, (innerLeft + innerRight) / 2, maleTop, femaleTop, Color.red);
		
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
	
