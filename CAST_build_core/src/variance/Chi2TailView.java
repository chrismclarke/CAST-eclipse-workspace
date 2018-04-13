package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class Chi2TailView extends MarginalDataView implements DataPlusDistnInterface {
	static final private int kMinDensityWidth = 60;
	static final private Color kDensityColor = Color.lightGray;
	static final private Color kTailColor = Color.blue;
	
	private AccurateDistn2Artist chi2Drawer;
	private String chi2Key;
	
	
	public Chi2TailView(DataSet theData, XApplet applet, NumCatAxis theAxis, String chi2Key) {
		super(theData, applet, new Insets(0, 0, 0, 0), theAxis);
		
		this.chi2Key = chi2Key;
		chi2Drawer = new AccurateDistn2Artist(chi2Key, theData);
		chi2Drawer.setFillColor(kDensityColor);
		chi2Drawer.setHighlightColor(kTailColor);
	}
	
	public void setAreaProportion(double areaProportion) {
		if (chi2Drawer instanceof AccurateDistn2Artist)
			((AccurateDistn2Artist)chi2Drawer).setAreaProportion(areaProportion);
	}
	
	public void paintView(Graphics g) {
		ContinDistnVariable chi2 = (ContinDistnVariable)getVariable(chi2Key);
		double lowQuantle = chi2.getMinSelection();
		double highQuantle = chi2.getMaxSelection();
		chi2Drawer.paintDistn(g, this, axis, lowQuantle, highQuantle);
	}
	
	public void setShowDensity (int densityType) {
	}
	
	public void setDensityColor(Color c) {
		chi2Drawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
	}

//-----------------------------------------------------------------------------------
	
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