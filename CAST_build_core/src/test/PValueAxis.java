package test;

import java.awt.*;

import dataView.*;
import qnUtils.*;


public class PValueAxis extends BufferedCanvas {
	static final public int STANDARD_TEXT = 0;
	static final public int P_VALUE_TEXT = 1;
	static final public int SIMPLE_TEXT = 2;
	
	static final private int kTickLength = 4;
	static final private int kTickTextGap = 3;
	static final private int kTopBottomBorder = 2;
	static final private int kLabelAxisGap = 3;
	static final private int kMinAxisLength = 50;
//	static final private int kSubscriptDrop = 2;
	
	private String kPValueString;
	
	private int labelType;
	private HypothesisTest test;
	private PValueScalesView scalesView;
	private int hypothDrawType;
	private Font headingFont;
	
	private String startText_H0, endText_H0, startText_HA, endText_HA;
	
	private boolean initialised = false;
	
	private int ascent, descent, titleAscent, minTopBorder, minBottomBorder, maxLabelWidth;
	
	public PValueAxis(int labelType, HypothesisTest test, int hypothDrawType, XApplet applet) {
		super(applet);
		kPValueString = applet.translate("p-value");
		this.labelType = labelType;
		this.test = test;
		this.hypothDrawType = hypothDrawType;
		
		if (labelType == SIMPLE_TEXT) {
			startText_H0 = startText_HA = null;
			endText_H0 = endText_HA = " " + applet.translate("is true");
		}
		else if (labelType == P_VALUE_TEXT) {
			startText_H0 = "1  (" + applet.translate("consistent with") + " ";
			endText_H0 = ")";
			
			startText_HA = "0  (";
			endText_HA = " " + applet.translate("must be true") + ")";
		}
		else {
			startText_H0 = applet.translate("consistent with") + " ";
			startText_HA = applet.translate("not consistent with") + " ";
			endText_H0 = endText_HA = null;
		}
		
		headingFont = applet.getStandardBoldFont();
		setFont(applet.getStandardFont());
	}
	
	public void setScalesView(PValueScalesView scalesView) {
		this.scalesView = scalesView;
	}
	
	public int getMinTopBorder() {
		if (!initialised)						//	avoid getGraphics() if already initialised
			initialise(getGraphics());
		return minTopBorder;
	}
	
	public int getMinBottomBorder() {
		if (!initialised)						//	avoid getGraphics() if already initialised
			initialise(getGraphics());
		return minBottomBorder;
	}
	
	private boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			minBottomBorder = kTopBottomBorder + (ascent + descent) / 2;
			minTopBorder = minBottomBorder;
			
			String text_H0 = (startText_H0 == null ? "" : startText_H0) + (endText_H0 == null ? "" : endText_H0);
			String text_HA = (startText_HA == null ? "" : startText_HA) + (endText_HA == null ? "" : endText_HA);
			
			maxLabelWidth = Math.max(fm.stringWidth(text_H0), fm.stringWidth(text_HA));
			maxLabelWidth += test.getSize(g, hypothDrawType).width;
			
			if (labelType == P_VALUE_TEXT) {
				g.setFont(headingFont);
				fm = g.getFontMetrics();
				titleAscent = fm.getAscent();
				minTopBorder += titleAscent + fm.getDescent() + kLabelAxisGap;
				g.setFont(getFont());
			}
			
			initialised = true;
			return true;
		}
		return false;
	}
	
	public void corePaint(Graphics g) {
		int topBorder = scalesView.getTopBorder();
		int bottomBorder = scalesView.getBottomBorder();
		
		int axisBottom = getSize().height - bottomBorder;
		g.drawLine(0, topBorder, 0, axisBottom);
		g.drawLine(0, topBorder, kTickLength, topBorder);
		g.drawLine(0, axisBottom, kTickLength, axisBottom);
		
		if (labelType == P_VALUE_TEXT) {
			g.setFont(headingFont);
			g.drawString(kPValueString, 0, kTopBottomBorder + titleAscent);
			g.setFont(getFont());
		}
		
		int topBaseline = topBorder + (ascent - descent) / 2;
		drawLabel(g, true, kTickLength + kTickTextGap, topBaseline);
		
		int bottomBaseline = axisBottom + (ascent - descent) / 2;
		drawLabel(g, false, kTickLength + kTickTextGap, bottomBaseline);
		
		if (labelType == P_VALUE_TEXT) {
			double pValue = test.evaluatePValue();
			int pValueY = scalesView.getPValuePos();
			int valueBaseline = Math.max(topBaseline + ascent + descent,
						Math.min(bottomBaseline - ascent - descent, pValueY + (ascent - descent) / 2));
			g.setColor(Color.red);
			
			String pValueString = kPValueString + " = " + (new NumValue(pValue, 4)).toString();
			g.drawString(pValueString, 5, valueBaseline);
			g.setColor(getForeground());
		}
	}
	
	private void drawLabel(Graphics g, boolean nullTrue, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		String startText = nullTrue ? startText_H0 : startText_HA;
		String endText = nullTrue ? endText_H0 : endText_HA;
		if (labelType == STANDARD_TEXT)
			nullTrue = true;		//	because the wording of startText and endText distinguish
		
		if (startText != null) {
			g.drawString(startText, x, y);
			x += fm.stringWidth(startText);
		}
		
		test.paintBlue(g, x, y, nullTrue, hypothDrawType, this);
		x += test.getSize(g, hypothDrawType).width;
		
		if (endText != null)
			g.drawString(endText, x, y);
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(kTickLength + kTickTextGap + maxLabelWidth,
															kMinAxisLength + minTopBorder + minBottomBorder);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
	
}
	
