package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DataRectangularView extends DataView {
	
	static final private Color kDistnColor = new Color(0x999999);
	static final private Color kDataColor = new Color(0xCC0000);
	
	static final private int kArrowHeight = 50;
	static final private int kArrowHead = 5;
	
	private String distnKey, dataKey;
	private HorizAxis horizAxis;
	private VertAxis densityAxis;
	
	public DataRectangularView(DataSet theData, XApplet applet, String distnKey, String dataKey,
												HorizAxis horizAxis, VertAxis densityAxis) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.distnKey = distnKey;
		this.dataKey = dataKey;
		this.horizAxis = horizAxis;
		this.densityAxis = densityAxis;
	}
	
	public void paintView(Graphics g) {
		RectangularDistnVariable distnVar = (RectangularDistnVariable)getVariable(distnKey);
		double distnMax = distnVar.getMax().toDouble();
		double density = 1 / distnMax;
		
		try {
			int minPos = horizAxis.numValToPosition(0.0);
			int maxPos = horizAxis.numValToPosition(distnMax);
			int densityPos = densityAxis.numValToPosition(density);
			
			Point p0 = translateToScreen(minPos, densityPos, null);
			Point p1 = translateToScreen(maxPos, -1, null);
			
			g.setColor(kDistnColor);
			g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
		
			g.setColor(kDataColor);
			NumVariable xVar = (NumVariable)getVariable(dataKey);
			ValueEnumeration xe = xVar.values();
			while (xe.hasMoreValues()) {
				double x = xe.nextDouble();
				
				int xPos = horizAxis.numValToPosition(x);
				Point pBottom = translateToScreen(xPos, -1, null);
				if (x <= distnMax) {
					Point pTop = translateToScreen(xPos, densityPos, null);
					g.fillRect(pTop.x - 1, pTop.y, 3, p1.y - pTop.y);
				}
				else {
					g.drawLine(pBottom.x, pBottom.y, pBottom.x, pBottom.y - kArrowHeight);
					g.drawLine(pBottom.x, pBottom.y, pBottom.x - kArrowHead, pBottom.y - kArrowHead);
					g.drawLine(pBottom.x, pBottom.y, pBottom.x + kArrowHead, pBottom.y - kArrowHead);
				}
			}
		} catch (AxisException e) {
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}