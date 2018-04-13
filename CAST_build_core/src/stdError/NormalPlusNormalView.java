package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class NormalPlusNormalView extends DataView {
	static final private Color kPink = new Color(0xFFE0E0);
	
	private String backNormalKey, frontNormalKey;
	private NumCatAxis axis;
	
	private BackgroundArtistInterface backNormalDrawer, frontNormalDrawer;
	
	public NormalPlusNormalView(DataSet theData, XApplet applet, NumCatAxis axis, String backNormalKey,
									String frontNormalKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.axis = axis;
		this.backNormalKey = backNormalKey;
		this.frontNormalKey = frontNormalKey;
		backNormalDrawer = new BackgroundNormalArtist(backNormalKey, theData);
		backNormalDrawer.setFillColor(kPink);
		frontNormalDrawer = new BackgroundNormalArtist(frontNormalKey, theData);
		frontNormalDrawer.setFillColor(null);
		setForeground(Color.red);
	}
	
	public void paintView(Graphics g) {
		backNormalDrawer.paintDistn(g, this, axis);
		frontNormalDrawer.paintDistn(g, this, axis);
	}
	
	public void setColors(Color backFillColor, Color frontOutlineColor) {
		backNormalDrawer.setFillColor(backFillColor);
		setForeground(frontOutlineColor);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(backNormalKey))
			backNormalDrawer.resetDistn();
		if (key.equals(frontNormalKey))
			frontNormalDrawer.resetDistn();
		repaint();
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}