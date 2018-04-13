package responseSurface;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class DragRestrictSurfaceView extends DragResponseSurfaceView {
	
	private String[] dataExplanKey;
	private String dataYKey;
	
	private boolean showSurface = false;
	
	public DragRestrictSurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] dataExplanKey, String dataYKey, String[] dragExplanKey, String dragYKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, dragExplanKey, dragYKey);
		this.dataExplanKey = dataExplanKey;
		this.dataYKey = dataYKey;
		
		drawData = !showSurface;
		
		constraints[3] = constraints[4] = constraints[5] = Double.NaN;
	}
	
	public void setShowSurface(boolean showSurface) {
		this.showSurface = showSurface;
		drawData = !showSurface;
	}
	
	public void resetModel(double startY) {
								//		anchors and model already set up
	}
	
//--------------------------------------------------------------
	
	protected Polygon drawShadeRegion(Graphics g) {
		if (showSurface)
			return super.drawShadeRegion(g);
		else
			return null;
	}
	
	protected NumVariable getYDataVar() {
		return (NumVariable)getVariable(dataYKey);
	}
	
	protected NumVariable getXDataVar() {
		return (NumVariable)getVariable(dataExplanKey[0]);
	}
	
	protected NumVariable getZDataVar() {
		return (NumVariable)getVariable(dataExplanKey[1]);
	}
	
	protected boolean[] arrowVisibility() {
		boolean arrowsVisible = map.getTheta2() < 90 || map.getTheta2() > 270;
		for (int i=0 ; i<5 ; i++)
			tempVisibility[i] = false;
		tempVisibility[5] = arrowsVisible && showSurface;
		
		return tempVisibility;
	}
	
	protected void drawForeground(Graphics g) {
		if (showSurface) {
			for (int i=0 ; i<5 ; i++) {
				tempPt = indexToScreenPoint(i);
				drawBlob(g, tempPt);
			}
			
			boolean[] isVisible = arrowVisibility();
			if (isVisible[5])
				drawArrowAt(g, 5, dragIndex == 5);
		}
	}
}
	
