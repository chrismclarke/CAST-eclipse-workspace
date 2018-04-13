package normal;

import java.awt.*;
import javax.swing.*;

import dataView.*;


public class ZCalcCanvas extends DataView {
	static final public int NO_SUB = 0;
	static final public int SUB_1 = 1;
	static final public int SUB_2 = 2;
	
	static final private int kTopBottomMargin = 2;
	static final private int kLeftRightMargin = 2;
	static final private int kLineNumberGap = 3;
	static final private int kLineBaselineOffset = 4;
	
	private JTextField meanField, sdField, xField;
	private int subscriptType;
	
	private Font subscriptFont;
	private int topBaseline, mainBaseline, bottomBaseline, subscriptWidth;
	
	public ZCalcCanvas(DataSet theData, XApplet applet, JTextField meanField, JTextField sdField,
																								JTextField xField, int subscriptType) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.meanField = meanField;
		this.sdField = sdField;
		this.xField = xField;
		this.subscriptType = subscriptType;
	}
	
	private void doVerticalLayout(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		topBaseline = kTopBottomMargin + ascent;
		mainBaseline = topBaseline + kLineNumberGap + kLineBaselineOffset;
		bottomBaseline = topBaseline  + 2 * kLineNumberGap + 1 + ascent;
		if (subscriptType != NO_SUB) {
			Font oldFont = g.getFont();
			if (subscriptFont == null)
				subscriptFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2);
			g.setFont(subscriptFont);
			fm = g.getFontMetrics();
			subscriptWidth = fm.stringWidth("2");
			g.setFont(oldFont);
		}
		else
			subscriptWidth = 0;
	}
	
	public void paintView(Graphics g) {
		doVerticalLayout(g);
		
		FontMetrics fm = g.getFontMetrics();
		int zWidth = fm.stringWidth("z");
		int equalsWidth = fm.stringWidth("  =  ");
		
		int zEqualsWidth = zWidth + subscriptWidth + equalsWidth;
		
		int xMinusWidth = fm.stringWidth(xField.getText() + " - ");
		int numeratorWidth = xMinusWidth + fm.stringWidth(meanField.getText());
		int denominatorWidth = fm.stringWidth(sdField.getText());
		int fractionWidth = Math.max(numeratorWidth, denominatorWidth);
		
		double xValue = Double.parseDouble(xField.getText());
		double meanValue = Double.parseDouble(meanField.getText());
		double sdValue = Double.parseDouble(sdField.getText());
		
		NumValue zValue = new NumValue((xValue - meanValue) / sdValue, 3);
		int zValueWidth = fm.stringWidth(" = " + zValue.toString());
		
		int totalWidth = zEqualsWidth + fractionWidth + zValueWidth + 2 * kLeftRightMargin;
		int horizPos = (getSize().width - totalWidth) / 2;
		
		g.drawString("z", horizPos, mainBaseline);
		if (subscriptType != NO_SUB) {
			Font oldFont = g.getFont();
			g.setFont(subscriptFont);
			g.drawString((subscriptType == SUB_1) ? "1" : "2", horizPos + zWidth, mainBaseline + 3);
			g.setFont(oldFont);
		}
		g.drawString("  =  ", horizPos + zWidth + subscriptWidth, mainBaseline);
		
		horizPos += zEqualsWidth;
		
		int numerOffset = (fractionWidth - numeratorWidth) / 2;
		g.drawString(xField.getText() + " - ", horizPos + numerOffset, topBaseline);
		g.drawLine(horizPos, topBaseline + kLineNumberGap, horizPos + fractionWidth,
																					topBaseline + kLineNumberGap);
		g.drawString("  =  " + zValue.toString(), horizPos + fractionWidth, mainBaseline);
		
		g.setColor(Color.blue);
		g.drawString(meanField.getText(), horizPos + numerOffset + xMinusWidth, topBaseline);
		
		g.setColor(Color.red);
		int denomOffset = (fractionWidth - denominatorWidth) / 2;
		g.drawString(sdField.getText(), horizPos + denomOffset, bottomBaseline);
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		doVerticalLayout(getGraphics());
		
		return new Dimension(20, bottomBaseline + kTopBottomMargin);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}