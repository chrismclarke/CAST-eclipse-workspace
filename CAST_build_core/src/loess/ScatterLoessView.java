package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ScatterLoessView extends ScatterView {
	
//	static final private double kMinHitFraction = 0.02;
											//		min x-distance for hits as a fraction of axis length
	
	private String loessKey;
	
	private boolean showLoess = false;
	
	public ScatterLoessView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String loessKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.loessKey = loessKey;
	}
	
	public void setShowLoess(boolean showLoess) {
		if (this.showLoess != showLoess) {
			this.showLoess = showLoess;
			repaint();
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		if (showLoess) {
			g.setColor(Color.blue);
//			int selectedIndex = getData().getSelection().findSingleSetFlag();
			LoessSmoothVariable loessVar = (LoessSmoothVariable)getVariable(loessKey);
			loessVar.drawCurve(g, this);
			g.setColor(getForeground());
		}
	}

//---------------------------------------------------------------------
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		super.doTransformView(g, theAxis);
		LoessSmoothVariable loessVar = (LoessSmoothVariable)getVariable(loessKey);
		if (loessVar != null)
			loessVar.initialise(loessVar.getWindowPoints());
	}
}
	
