package multivar;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class Rotate3DLineView extends Rotate3DView {
	
	protected boolean drawLine = false;
	
	public Rotate3DLineView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
	}
	
	public void setDrawLine(boolean drawLine) {
		this.drawLine = drawLine;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (drawLine) {
			g.setColor(Color.blue);
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			NumVariable zVariable = (NumVariable)getVariable(zKey);
			ValueEnumeration xe = xVariable.values();
			ValueEnumeration ye = yVariable.values();
			ValueEnumeration ze = zVariable.values();
			Point lastPos = null;
			Point nextPos = null;
			while (xe.hasMoreValues() && ye.hasMoreValues() && ze.hasMoreValues()) {
				nextPos = getScreenPoint(xe.nextDouble(), ye.nextDouble(), ze.nextDouble(), nextPos);
				if (lastPos != null && nextPos != null)
					g.drawLine(lastPos.x, lastPos.y, nextPos.x, nextPos.y);
				Point temp = lastPos;
				lastPos = nextPos;
				nextPos = temp;
			}
		}
		g.setColor(getForeground());
		super.drawData(g, shadeHandling);
	}
}
	
