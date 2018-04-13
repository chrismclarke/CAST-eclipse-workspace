package test;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class TNormalView extends MarginalDataView implements DataPlusDistnInterface {
	static final private int kMinDensityWidth = 60;
	static final private Color kLightBlueColor = new Color(0x66CCFF);
	
	private BackgroundNormalArtist normalDrawer, tDrawer;
	private int densityType = CONTIN_DISTN;
	
	public TNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis, String normalKey,
									String tKey) {
		super(theData, applet, new Insets(0, 0, 0, 0), theAxis);
		normalDrawer = new BackgroundNormalArtist(normalKey, theData);
		normalDrawer.setFillColor(null);
		
		double maxNormalDensity = normalDrawer.getMaxDensity();
		
		tDrawer = new ScaledBackgroundArtist(tKey, theData, maxNormalDensity);
		tDrawer.setFillColor(kLightBlueColor);
	}
	
	public void paintView(Graphics g) {
		if (densityType == CONTIN_DISTN)
			tDrawer.paintDistn(g, this, axis);
		g.setColor(Color.red);
		normalDrawer.paintDistn(g, this, axis);
	}
	
	public void setShowDensity (int densityType) {
		this.densityType = densityType;
		repaint();
	}
	
	public void setDensityColor(Color c) {
		tDrawer.setFillColor(c);
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
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
	}
}