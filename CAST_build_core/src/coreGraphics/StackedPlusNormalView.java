package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;


public class StackedPlusNormalView extends StackedDotPlotView implements DataPlusDistnInterface {
//	static public final String STACK_NORMAL = "stackNormal";
	
	static final public int POLYGON_ALGORITHM = 0;
	static final public int STACK_ALGORITHM = 1;
	static final public int ACCURATE_STACK_ALGORITHM = 2;
	
	protected String normalKey;
	
	private BackgroundArtistInterface backgroundDrawer;
	private int densityType = CONTIN_DISTN;
	private LabelValue label = null;
	private Color labelColor;
	
	public StackedPlusNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																												String normalKey, int algorithm) {
		super(theData, applet, theAxis, null, false);
		if (normalKey != null) {
			if (algorithm == STACK_ALGORITHM)
				backgroundDrawer = new AccurateDistnArtist(normalKey, theData);
			else if (algorithm == ACCURATE_STACK_ALGORITHM)
				backgroundDrawer = new AccurateDistn2Artist(normalKey, theData);
			else
				backgroundDrawer = new BackgroundNormalArtist(normalKey, theData);
		}
		this.normalKey = normalKey;
	}
	
	public StackedPlusNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																																		String normalKey) {
		this(theData, applet, theAxis, normalKey, POLYGON_ALGORITHM);
	}
	
	protected void paintBackground(Graphics g) {
		if (densityType == CONTIN_DISTN) {
			backgroundDrawer.paintDistn(g, this, axis);
			if (label != null) {
				g.setColor(labelColor);
				int ascent = g.getFontMetrics().getAscent();
				label.drawLeft(g, getSize().width - 2, ascent + 2);
				g.setColor(getForeground());
			}
		}
	}
	
	public void setShowDensity (int densityType) {
		this.densityType = densityType;
		repaint();
	}
	
	public void setDensityColor(Color c) {
		if (backgroundDrawer != null)
			backgroundDrawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		this.label = label;
		this.labelColor = labelColor;
	}
	
	public void setAreaProportion(double areaPropn) {
		if (backgroundDrawer instanceof AccurateDistn2Artist)
			((AccurateDistn2Artist)backgroundDrawer).setAreaProportion(areaPropn);
	}
	
	public void setVariableKeys(String yKey, String normalKey) {
		setActiveNumVariable(yKey);
		this.normalKey = normalKey;
		backgroundDrawer.changeDistnKey(normalKey);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(normalKey))
			backgroundDrawer.resetDistn();
		super.doChangeVariable(g, key);
	}
}