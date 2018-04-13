package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;


public class JitterPlusNormalView extends DotPlotView implements DataPlusDistnInterface {
//	static public final String JITTER_NORMAL = "jitterNormal";
	
	static final public int POLYGON_ALGORITHM = 0;
	static final public int STACK_ALGORITHM = 1;
	
	private String normalKey;
	
	private BackgroundArtistInterface backgroundDrawer;
	private int densityType = CONTIN_DISTN;
	private LabelValue label = null;
	private Color labelColor;
	
	public JitterPlusNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																String normalKey, double initialJittering, int algorithm) {
		super(theData, applet, theAxis, initialJittering);
		if (normalKey != null) {
			if (algorithm == STACK_ALGORITHM)
				backgroundDrawer = new AccurateDistnArtist(normalKey, theData);
			else
				backgroundDrawer = new BackgroundNormalArtist(normalKey, theData);
		}
		this.normalKey = normalKey;
	}
	
	public JitterPlusNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																					String normalKey, double initialJittering) {
		this(theData, applet, theAxis, normalKey, initialJittering, POLYGON_ALGORITHM);
	}
	
	protected void paintBackground(Graphics g) {
		if (densityType == CONTIN_DISTN && backgroundDrawer != null) {
			backgroundDrawer.paintDistn(g, this, axis);
			if (label != null) {
				g.setColor(labelColor);
				int ascent = g.getFontMetrics().getAscent();
				label.drawLeft(g, getSize().width - 2, ascent + 2);
				g.setColor(getForeground());
			}
		}
	}
	
	public void paintView(Graphics g) {
		paintBackground(g);
		super.paintView(g);
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

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(normalKey))
			backgroundDrawer.resetDistn();
		super.doChangeVariable(g, key);
	}
}