package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;


public class SimpleDistnView extends MarginalDataView {
//	static public final String SIMPLE_DISTN_VIEW = "simpleDistnView";
	
	static final public int POLYGON_ALGORITHM = 0;
	static final public int STACK_ALGORITHM = 1;
	static final public int ACCURATE_STACK_ALGORITHM = 2;
	
	static final private int kMinDensityWidth = 60;
	
	protected String distnKey;
	private BackgroundArtistInterface distnDrawer;
	
	private LabelValue label;
	private Color labelColor;
	
	public SimpleDistnView(DataSet theData, XApplet applet, NumCatAxis theAxis,
								String distnKey, LabelValue label, Color labelColor, int algorithm) {
		super(theData, applet, new Insets(0, 0, 0, 0), theAxis);
		this.distnKey = distnKey;
		this.label = label;
		this.labelColor = labelColor;
		if (algorithm == STACK_ALGORITHM)
			distnDrawer = new AccurateDistnArtist(distnKey, theData);
		else if (algorithm == ACCURATE_STACK_ALGORITHM)
			distnDrawer = new AccurateDistn2Artist(distnKey, theData);
		else
			distnDrawer = new BackgroundNormalArtist(distnKey, theData);
	}
	
	public SimpleDistnView(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey) {
		this(theData, applet, theAxis, distnKey, null, null, STACK_ALGORITHM);
	}
	
	public void setDistnKey(String distnKey, XApplet applet) {
		this.distnKey = distnKey;
	}
	
	public void setDensityColor(Color c) {
		distnDrawer.setFillColor(c);
	}
	
	public void setHighlightColor(Color c) {
		distnDrawer.setHighlightColor(c);
	}
	
	public void setLabel(LabelValue label, Color labelColor) {
		this.label = label;
		this.labelColor = labelColor;
	}
	
	public void setAreaProportion(double areaProportion) {
		if (distnDrawer instanceof AccurateDistn2Artist)
			((AccurateDistn2Artist)distnDrawer).setAreaProportion(areaProportion);
	}
	
	public void setDensityScaling(double densityScaling) {
		if (distnDrawer instanceof AccurateDistnArtist)
			((AccurateDistnArtist)distnDrawer).setDensityScaling(densityScaling);
	}
	
	public void paintView(Graphics g) {
		DistnVariable distn = (DistnVariable)getVariable(distnKey);
		double minSelection = distn.getMinSelection();
		double maxSelection = distn.getMaxSelection();
		
		if (minSelection == maxSelection)
			distnDrawer.paintDistn(g, this, axis);
		else
			distnDrawer.paintDistn(g, this, axis, minSelection, maxSelection);
		
		if (label != null) {
			g.setColor(labelColor);
			int ascent = g.getFontMetrics().getAscent();
			label.drawLeft(g, getSize().width - 2, ascent + 2);
			g.setColor(getForeground());
		}
	}

//-----------------------------------------------------------------------------------

	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(distnKey))
			distnDrawer.resetDistn();
		super.doChangeVariable(g, key);
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	public int minDisplayWidth() {
		return kMinDensityWidth;
	}
}