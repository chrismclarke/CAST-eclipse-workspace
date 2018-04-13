package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DiscretePlusNormView extends StackedDiscreteView implements DataPlusDistnInterface {
//	static public final String STACKED_PLUS_NORMAL = "stackedPlusNormal";
	
	static final private Color kNormalFillColour = new Color(0xFF9999);
	static final private Color kNormalHighlightColour = new Color(0xFF3333);
	
	private String distnKey;
	
	private BackgroundNormalArtist backgroundDrawer;
	private boolean drawNormal = false;
	
	private LabelValue label = null;
	private Color labelColor;
	
	public DiscretePlusNormView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey,
																																								String distnKey) {
		super(theData, applet, theAxis, yKey, 1.0);
		this.distnKey = distnKey;
		backgroundDrawer = new BackgroundNormalArtist(distnKey, theData);
		backgroundDrawer.setFillColor(kNormalFillColour);
		backgroundDrawer.setHighlightColor(kNormalHighlightColour);
	}
	
	public void setShowDensity (int densityType) {
		setDrawTheory(densityType != NO_DISTN);
	}
	
	public void setDensityColor(Color c) {
		backgroundDrawer.setFillColor(c);
	}
	
	public void setInverseColors() {		//	allows two tails to be highlighted by making 'selection' in middle
		backgroundDrawer.setFillColor(kNormalHighlightColour);
		backgroundDrawer.setHighlightColor(kNormalFillColour);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		this.label = label;
		this.labelColor = labelColor;
	}
	
	public void setDrawTheory(boolean drawTheory) {
		drawNormal = drawTheory;
	}
	
	private void setNormalScaleFactor(NormalDistnVariable normalVar) {
		double normalSD = normalVar.getSD().toDouble();
		
		backgroundDrawer.setMaxDensityFactor(1.0);
		double maxStdDensity = backgroundDrawer.getMaxDensity();
		
		int groupSize = getCrossSize() * 2 + 3;
		double histoArea = getNumVariable().noOfValues() * groupSize / normalSD;
		int displayHeight = getDisplayWidth();
		if (maxStackHeight > displayHeight)
			histoArea = histoArea * displayHeight / maxStackHeight;
		
		backgroundDrawer.setMaxDensityFactor(Math.min(2.0,
												displayHeight / (maxStdDensity * histoArea)));
																	//	 height is at least 0.5 time maximum
	}
	
	public void drawBackground(Graphics g) {
		super.drawBackground(g);
		
		NormalDistnVariable normalVar = (NormalDistnVariable)getData().getVariable(distnKey);
		
		if (drawNormal) {
			setNormalScaleFactor(normalVar);
			double maxSelection = normalVar.getMaxSelection();
			if (Double.isInfinite(maxSelection))
				backgroundDrawer.paintDistn(g, this, axis, Double.NEGATIVE_INFINITY,
																			normalVar.getMinSelection());
			else
				backgroundDrawer.paintDistn(g, this, axis, normalVar.getMaxSelection(),
																			Double.POSITIVE_INFINITY);
										//		backgroundDrawer highlights two tails of distribution
			if (label != null) {
				g.setColor(labelColor);
				int ascent = g.getFontMetrics().getAscent();
				label.drawLeft(g, getSize().width - 2, ascent + 2);
				g.setColor(getForeground());
			}
		}
	}
}