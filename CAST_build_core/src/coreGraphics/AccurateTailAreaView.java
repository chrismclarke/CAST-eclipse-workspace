package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class AccurateTailAreaView extends DataView implements DataPlusDistnInterface {
//	static public final String F_TAIL = "fTail";
	
	static final public int UPPER_TAIL = 0;
	static final public int TWO_TAILED = 1;
	
	static final private int kTopGap = 2;
	static final private int kFLabelGap = 2;
	static final private int kFArrowVertGap = 2;
	static final private int kFArrowHorizGap = 4;
	static final private int kArrowBottomSpace = 15;
	static final private int kMinArrowLength = 9;
	static final private int kArrowHead = 6;
	static final private int kHorizArrowLength = 14;
	
	private int tailType = UPPER_TAIL;
	private boolean showValueArrow = true;
	
	private NumCatAxis axis;
	private String distnKey;
	
	private AccurateDistnArtist backgroundDrawer;
	private LabelValue label = null;
	private Color labelColor;
	
	private LabelValue fLabel = new LabelValue("F");		//		Written initially for F distn
	private Font labelFont;
	
	public AccurateTailAreaView(DataSet theData, XApplet applet, NumCatAxis axis, String distnKey) {
		super(theData, applet, new Insets(0, kArrowHead, 0, kArrowHead));
		backgroundDrawer = new AccurateDistnArtist(distnKey, theData);
		labelFont = applet.getBigBoldFont();
		this.axis = axis;
		this.distnKey = distnKey;
	}
	
	public void setTailType(int tailType) {
		this.tailType = tailType;
	}
	
	public void setShowValueArrow(boolean showValueArrow) {
		this.showValueArrow = showValueArrow;
	}
	
	public void setValueLabel(LabelValue fLabel) {
		this.fLabel = fLabel;
	}
	
	public void setShowDensity (int densityType) {
	}
	
	public void setDensityScaling(double densityScaling) {
		backgroundDrawer.setDensityScaling(densityScaling);
	}
	
	public void setDistnColors(Color fillColor, Color hiliteColor) {
		backgroundDrawer.setFillColor(fillColor);
		backgroundDrawer.setHighlightColor(hiliteColor);
	}
	
	public void paintView(Graphics g) {
		Font stdFont = g.getFont();
		g.setFont(labelFont);
		FontMetrics fm = g.getFontMetrics();
		
		int labelBaseline = fm.getAscent() + kTopGap;
		
		NumVariable fVar = getNumVariable();
		double f = fVar.doubleValueAt(0);
		try {
			int fPos = axis.numValToPosition(f);
			int fHoriz = translateToScreen(fPos, 0, null).x;
			
			double lowF = Double.NEGATIVE_INFINITY;
			double highF = f;
			
			if (tailType == TWO_TAILED) {
				ContinDistnVariable dist = (ContinDistnVariable)getVariable(distnKey);
				double cumProb = dist.getCumulativeProb(f);
				double otherQuantile = dist.getQuantile(1.0 - cumProb);
				lowF = Math.min(f, otherQuantile);
				highF = Math.max(f, otherQuantile);
			}
			
			backgroundDrawer.paintDistn(g, this, axis, lowF, highF);
			
			if (showValueArrow) {
				g.setColor(Color.red);
				int fBaseline = labelBaseline;
				if (fLabel != null) {
					fBaseline += fm.getAscent() + fm.getDescent() + kFLabelGap;
					fLabel.drawCentred(g, fHoriz, fBaseline);
				}
				
				int arrowTop = fBaseline + kFArrowVertGap;
				int arrowBottom = Math.max(getSize().height - kArrowBottomSpace, arrowTop + kMinArrowLength);
				int arrowHead = Math.min(kArrowHead, (arrowBottom - arrowTop) / 2 + 1);
				g.drawLine(fHoriz, arrowTop, fHoriz, arrowBottom);
				g.drawLine(fHoriz - 1, arrowTop, fHoriz - 1, arrowBottom - 1);
				g.drawLine(fHoriz + 1, arrowTop, fHoriz + 1, arrowBottom - 1);
				for (int i=2 ; i<arrowHead ; i++)
					g.drawLine(fHoriz - i, arrowBottom - i, fHoriz + i, arrowBottom - i);
			}
		} catch (AxisException ex) {
			backgroundDrawer.paintDistn(g, this, axis);
			
			if (showValueArrow) {
				g.setColor(Color.red);
				int fVertCenter = (getSize().height + labelBaseline) / 2;
				int fBaseline = fVertCenter + (fm.getAscent() - fm.getDescent()) / 2;
				
				int arrowRight = getSize().width - 2;
				int arrowLeft = arrowRight - kHorizArrowLength;
				int fRight = arrowLeft - kFArrowHorizGap;
				
				if (fLabel != null)
					fLabel.drawLeft(g, fRight, fBaseline);
				g.drawLine(arrowLeft, fVertCenter, arrowRight, fVertCenter);
				g.drawLine(arrowLeft, fVertCenter - 1, arrowRight - 1, fVertCenter - 1);
				g.drawLine(arrowLeft, fVertCenter + 1, arrowRight - 1, fVertCenter + 1);
				for (int i=2 ; i<kArrowHead ; i++)
					g.drawLine(arrowRight - i, fVertCenter - i, arrowRight - i, fVertCenter + i);
			}
		}
		
		if (label != null) {
			g.setColor(labelColor);
			int ascent = g.getFontMetrics().getAscent();
			label.drawLeft(g, getSize().width - 2, ascent + 2);
		}
		
		g.setColor(getForeground());
		g.setFont(stdFont);
	}
	
	public void setDensityColor(Color c) {
		if (backgroundDrawer != null)
			backgroundDrawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		this.label = label;
		this.labelColor = labelColor;
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(distnKey));
			backgroundDrawer.resetDistn();
		super.doChangeVariable(g, key);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}