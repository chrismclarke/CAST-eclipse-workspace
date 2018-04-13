package exper;

import java.awt.*;

import dataView.*;


public class TreatmentLabelsView extends DataView {
//	static final public String TREAT_LABELS = "treatLabels";
	
	static final private int kOuterLeftMargin = 20;
	static final private int kTopBottomBorder = 4;
	static final private int kLeftRightBorder = 6;
	static final private int kArrowWidth = 4;
	
	private String treatKey;
	
	private int selectedCat = -1;
	
	public TreatmentLabelsView(DataSet theData, XApplet applet, String treatKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.treatKey = treatKey;
	}
	
	public void setSelectedCat(int selectedCat) {
		this.selectedCat = selectedCat;
		repaint();
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int lineHt = fm.getLeading() + ascent + descent;
		
		g.setColor(Color.white);
		g.fillRect(kOuterLeftMargin, 0, getSize().width - kOuterLeftMargin, getSize().height);
		
		CatVariable treat = (CatVariable)getVariable(treatKey);
		int startHoriz = kOuterLeftMargin + kLeftRightBorder;
		int baseline = kTopBottomBorder + ascent;
		for (int i=0 ; i<treat.noOfCategories() ; i++) {
			if (selectedCat == i) {
				g.setColor(Color.red);
				int arrowMiddle = baseline + i * lineHt - (ascent - descent) / 2;
				for (int j=0 ; j<kArrowWidth ; j++) {
					g.drawLine(0, arrowMiddle - j, kOuterLeftMargin - j - 1, arrowMiddle - j);
					g.drawLine(0, arrowMiddle + j, kOuterLeftMargin - j - 1, arrowMiddle + j);
				}
				for (int j=kArrowWidth ; j<2*kArrowWidth ; j++)
					g.drawLine(kOuterLeftMargin - j, arrowMiddle - j, kOuterLeftMargin - j, arrowMiddle + j);
				g.setColor(Color.yellow);
				g.fillRect(kOuterLeftMargin, kTopBottomBorder - 2 + i * lineHt,
														getSize().width - kOuterLeftMargin, ascent + descent + 4);
			}
			Value label = treat.getLabel(i);
			g.setColor(TreatEffectSliderView.getBaseBarColor(i));
			label.drawRight(g, startHoriz, baseline + i * lineHt);
		}
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
		Graphics g = getGraphics();
		g.setFont(getFont());
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int lineHt = fm.getLeading() + ascent + descent;
		
		int maxWidth = 0;
		CatVariable treat = (CatVariable)getVariable(treatKey);
		int nTreats = treat.noOfCategories();
		for (int i=0 ; i<nTreats ; i++) {
			Value label = treat.getLabel(i);
			maxWidth = Math.max(maxWidth, label.stringWidth(g));
		}
		
		return new Dimension(kOuterLeftMargin + 2 * kLeftRightBorder + maxWidth,
							2 * kTopBottomBorder + lineHt * (nTreats - 1) + ascent + descent);
	}
}
	
