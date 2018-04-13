package sampling;

import java.awt.*;

import dataView.*;


public class FractionValueView extends DataView {
	private static final int kValueLeftRightBorder = 10;	//	at all sides of value inside box
	private static final int kValueTopBottomBorder = 3;	//	at all sides of value inside box
	protected static final int kLabelLeftBorder = 3;
	private static final int kLabelRightBorder = 5;
	private static final int kFractionGap = 2;
	
	private DragPropnDotPlotView dotView;
	private NumValue minVal, maxVal;
	private NumValue numerVal, denomVal;
	private double longestVal;
	
	private boolean initialised = false;
	private int itemHeight, itemWidth, labelWidth, maxValueWidth, fontAscent, fontDescent;
	
	public FractionValueView(DataSet theData, XApplet applet, DragPropnDotPlotView dotView,
												int decimals, double longestVal) {
		super(theData, applet, null);
		this.dotView = dotView;
		minVal = new NumValue(0.0, decimals);
		maxVal = new NumValue(0.0, decimals);
		numerVal = new NumValue(0.0, 0);
		denomVal = new NumValue(0.0, 0);
		this.longestVal = longestVal;
		
		dotView.setLinkedFraction(this);
	}
	
	//----------------------------------------------------------------
	
	private String getProbString(double min, double max) {
		minVal.setValue(min);
		maxVal.setValue(max);
		if (min == max)
			return "P(X = " + maxVal.toString() + ") = ";
		else if (min == Double.NEGATIVE_INFINITY && max != Double.POSITIVE_INFINITY)
			return "P(X < " + maxVal.toString() + ") = ";
		else if (min != Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY)
			return "P(X > " + minVal.toString() + ") = ";
		else
			return "P(" + minVal.toString() + "< X < " + maxVal.toString() + ") = ";
	}
	
	private void initialiseSizes(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		fontAscent = fm.getAscent();
		fontDescent = fm.getDescent();
		itemHeight = 2 * (kValueTopBottomBorder + 2 + kFractionGap + fontAscent) + 1;
		
		int maxCount = getSelection().getNoOfFlags();
		maxValueWidth = fm.stringWidth(String.valueOf(maxCount));
		itemWidth = 2 * kValueLeftRightBorder + 4 + maxValueWidth;
		
		labelWidth = fm.stringWidth(getProbString(longestVal, longestVal - 0.0001));
		if (labelWidth > 0)
			itemWidth += kLabelLeftBorder + labelWidth + kLabelRightBorder;
		
		initialised = true;
	}
	
	public Dimension getMinimumSize() {
		if (!initialised) {
			Graphics g = getGraphics();
			initialiseSizes(g);
		}
		return new Dimension(itemWidth, itemHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	//----------------------------------------------------------------
	
	private void drawLabel(Graphics g) {
		int baseline = (itemHeight + fontAscent - fontDescent) / 2;
		String label = getProbString(dotView.getMinSelection(), dotView.getMaxSelection());
		int width = g.getFontMetrics().stringWidth(label);
		g.drawString(label, kLabelLeftBorder + labelWidth - width, baseline);
	}
	
	private void drawValueInterior(Graphics g) {
		int interiorLeft = 2 + kLabelLeftBorder + kLabelRightBorder + labelWidth;
		int interiorWidth = maxValueWidth + 2 * kValueLeftRightBorder;
		
		int interiorTop = itemHeight / 2 - fontAscent - kValueTopBottomBorder - kFractionGap;
		int interiorBottom = itemHeight / 2 + fontAscent + kValueTopBottomBorder + kFractionGap + 1;
		
		g.setColor(Color.white);
		g.fillRect(interiorLeft, interiorTop, interiorWidth, interiorBottom - interiorTop);
		g.setColor(getForeground());
		
		Flags selection = getSelection();
		denomVal.setValue(selection.getNoOfFlags());
		numerVal.setValue(selection.countSetFlags());
		
		int valueCenter = interiorLeft + interiorWidth / 2;
		numerVal.drawCentred(g, valueCenter, itemHeight / 2 - kFractionGap);
		denomVal.drawCentred(g, valueCenter, itemHeight / 2 + kFractionGap + fontAscent + 1);
		
		g.drawLine(interiorLeft + kValueLeftRightBorder, itemHeight / 2,
										interiorLeft + kValueLeftRightBorder + maxValueWidth, itemHeight / 2);
	}
	
	public void paintView(Graphics g) {
		if (!initialised)
			initialiseSizes(g);
			
		drawLabel(g);
		
		int valueBoxLeft = kLabelLeftBorder + kLabelRightBorder + labelWidth;
		int interiorTop = itemHeight / 2 - fontAscent - kValueTopBottomBorder - kFractionGap;
		int interiorBottom = itemHeight / 2 + fontAscent + kValueTopBottomBorder + kFractionGap + 1;
		
		g.setColor(Color.gray);
		g.drawLine(valueBoxLeft, interiorTop - 2, valueBoxLeft + maxValueWidth + 2*kValueLeftRightBorder + 3,
															interiorTop - 2);
		g.drawLine(valueBoxLeft, interiorTop - 2, valueBoxLeft, interiorBottom + 1);
		g.setColor(Color.white);
		g.drawLine(valueBoxLeft + 1, interiorBottom + 1, valueBoxLeft + maxValueWidth + 2*kValueLeftRightBorder + 3,
															interiorBottom + 1);
		g.drawLine(valueBoxLeft + maxValueWidth + 2*kValueLeftRightBorder + 3, interiorTop - 1,
															valueBoxLeft + maxValueWidth + 2*kValueLeftRightBorder + 3,
															interiorBottom + 1);
		g.setColor(getForeground());
		g.drawRect(valueBoxLeft + 1, interiorTop - 1, maxValueWidth + 2*kValueLeftRightBorder + 1,
															interiorBottom - interiorTop + 1);
		
		drawValueInterior(g);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
