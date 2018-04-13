package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DiscretePlusBinomView extends StackedDiscreteView implements DataPlusDistnInterface {
//	static public final String STACKED_PLUS_BINOMIAL = "stackedPlusBinomial";
	
	static final private Color kBinomialFillColour = new Color(0xFF9999);
	static final private Color kBinomialHighlightColour = new Color(0xFF3333);
	
	static final private double kDistnBarWidth = 0.5;
	static final private double kMaxDisplayHt = 0.9;
	
	private String distnKey;
	
	private boolean drawBinomial = false;
	private Color binomialFillColour = kBinomialFillColour;
	
	private LabelValue label = null;
	private Color labelColor;
	
	public DiscretePlusBinomView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																													String yKey, String distnKey) {
		super(theData, applet, theAxis, yKey, 1.0);
		this.distnKey = distnKey;
	}
	
	public void setShowDensity (int densityType) {
		setDrawTheory(densityType != NO_DISTN);
		repaint();
	}
	
	public void setDensityColor(Color c) {
		binomialFillColour = c;
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		this.label = label;
		this.labelColor = labelColor;
	}
	
	public void setDrawTheory(boolean drawTheory) {
		drawBinomial = drawTheory;
	}
	
	public void drawBackground(Graphics g) {
		super.drawBackground(g);
		
		DiscreteDistnVariable binomVar = (DiscreteDistnVariable)getData().getVariable(distnKey);
		
		if (drawBinomial) {
			int displayHeight = getDisplayWidth();
			double scaleFactor = kMaxDisplayHt + displayHeight / binomVar.getMaxScaledProb();
			
			int iMinOnAxis = (int)Math.ceil(axis.minOnAxis / step - 0.00001);
			int iMaxOnAxis = (int)Math.floor(axis.maxOnAxis / step + 0.00001);
			
			Point p0 = null;
			Point p1 = null;
			
			for (int i=iMinOnAxis ; i<=iMaxOnAxis ; i++) {
				int classLowPos = axis.numValToRawPosition(i - kDistnBarWidth / 2);
				int classHighPos = axis.numValToRawPosition(i + kDistnBarWidth / 2);
				
				int classVertPos = (int)Math.round(scaleFactor * binomVar.getScaledProb(i));
					
				p0 = translateToScreen(classLowPos, classVertPos, p0);
				p1 = translateToScreen(classHighPos, 0, p1);
				int topPos = p0.y + getSize().height - p1.y;		//	to make bottom flush with axis
				g.setColor(((i <= highlightVal) == (highlightSide == LOW_HIGHLIGHT))
												? kBinomialHighlightColour : binomialFillColour);
				g.fillRect(p0.x, topPos, (p1.x - p0.x), (p1.y - p0.y));
				g.setColor(getForeground());
				g.drawRect(p0.x, topPos, (p1.x - p0.x), (p1.y - p0.y));
			}
			
			if (label != null) {
				g.setColor(labelColor);
				int ascent = g.getFontMetrics().getAscent();
				label.drawLeft(g, getSize().width - 2, ascent + 2);
				g.setColor(getForeground());
			}
		}
	}
}