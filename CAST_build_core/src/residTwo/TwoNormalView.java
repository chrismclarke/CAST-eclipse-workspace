package residTwo;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class TwoNormalView extends JitterPlusNormalView {
	static final private Color kPaleRed = new Color(0xFFAACC);
	
	private String normOutlineKey;
	private BackgroundArtistInterface outlineDrawer;
	
	public TwoNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis, String normalKey,
									String normOutlineKey) {
		super(theData, applet, theAxis, normalKey, 1.0, POLYGON_ALGORITHM);
		setDensityColor(Color.blue);
		this.normOutlineKey = normOutlineKey;
		outlineDrawer = new BackgroundNormalArtist(normOutlineKey, theData);
		outlineDrawer.setFillColor(kPaleRed);
	}
	
	public void paintView(Graphics g) {
		outlineDrawer.paintDistn(g, this, axis);
		paintBackground(g);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(normOutlineKey))
			outlineDrawer.resetDistn();
		super.doChangeVariable(g, key);
	}
}