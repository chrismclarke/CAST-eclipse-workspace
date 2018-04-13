package contin;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class TailAreaView extends DataView {
//	static public final String TAIL_AREA = "tailArea";
	
	private HorizAxis axis;
	private BackgroundNormalArtist backgroundDrawer;
	private String distnKey;
	
	public TailAreaView(DataSet theData, XApplet applet, HorizAxis theAxis, String distnKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		backgroundDrawer = new BackgroundNormalArtist(distnKey, theData);
		this.axis = theAxis;
		this.distnKey = distnKey;
	}
	
	public void paintView(Graphics g) {
		DistnVariable distn = (DistnVariable)getVariable(distnKey);
		backgroundDrawer.paintDistn(g, this, axis, Double.NEGATIVE_INFINITY, distn.getMinSelection());
	}
	
	public void setDensityColor(Color c) {
		backgroundDrawer.setFillColor(c);
	}
	
	public void setHighlightColor(Color c) {
		backgroundDrawer.setHighlightColor(c);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}