package normal;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class StdNormalView extends MarginalDataView implements DataPlusDistnInterface {
//	static public final String STD_NORMAL_DISTN = "stdNormal";
	
	static final private int kMinDensityWidth = 60;
	
	private String distnKey;
	private BackgroundArtistInterface distnDrawer;
	
	private double lowZValue = 0.0;
	private double highZValue = 0.0;
	
	private NumValue sd = null;
	
	public StdNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey) {
		super(theData, applet, new Insets(0, 0, 0, 0), theAxis);
		this.distnKey = distnKey;
		AccurateDistnArtist tempDrawer = new AccurateDistnArtist(distnKey, theData);
		tempDrawer.setDensityScaling(0.85);
		distnDrawer = tempDrawer;
	}
	
	public void setZValue(double zValue) {
		this.lowZValue = -zValue;
		this.highZValue = zValue;
	}
	
	public void setTopZValue(double zValue) {
		this.lowZValue = Double.NEGATIVE_INFINITY;
		this.highZValue = zValue;
	}
	
	public void setSD(NumValue sd) {
		this.sd = sd;
	}
	
	public void setDistnColors(Color fillColor, Color hiliteColor) {
		distnDrawer.setFillColor(fillColor);
		distnDrawer.setHighlightColor(hiliteColor);
	}
	
	public void paintView(Graphics g) {
//		ContinDistnVariable distn = (ContinDistnVariable)getVariable(distnKey);
		
		distnDrawer.paintDistn(g, this, axis, lowZValue, highZValue);
		
		int zeroPos = axis.numValToRawPosition(0.0);
		int zeroOnScreen = translateToScreen(zeroPos, 0, null).x;
		
		g.setColor(Color.gray);
		g.drawLine(zeroOnScreen, 0, zeroOnScreen, getSize().height);
		
		if (sd != null) {
			int onePos = axis.numValToRawPosition(1.0);
			int oneOnScreen = translateToScreen(onePos, 0, null).x;
			
			int arrowVert = getSize().height - 30;
			
			g.setColor(Color.red);
			for (int i=-1 ; i<2 ; i++)
				g.drawLine(zeroOnScreen + 1, arrowVert + i, oneOnScreen - 1, arrowVert + i);
			
			for (int i=0 ; i<6 ; i++)
				g.drawLine(oneOnScreen - i, arrowVert - i, oneOnScreen - i, arrowVert + i);
			
			sd.drawCentred(g, (zeroOnScreen + oneOnScreen) / 2, arrowVert - 4);
		}
	}

//-----------------------------------------------------------------------------------
	
	public void setShowDensity (int densityType) {	//	always shows density
	}
	
	public void setDensityColor(Color c) {
		distnDrawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {	//	never draws label
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