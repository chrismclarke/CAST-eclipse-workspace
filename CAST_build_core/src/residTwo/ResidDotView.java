package residTwo;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ResidDotView extends DotPlotView {
	
//	static public final String RESID_DOTPLOT = "residDotPlot";
	
	private boolean allowDrag = true;
	
	public ResidDotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis, 1.0);
	}
	
	public void setAllowDrag(boolean allowDrag) {
		this.allowDrag = allowDrag;
	}

//-----------------------------------------------------------------------------------
	
	public void paintView(Graphics g) {
		g.setColor(Color.lightGray);
		
		try {
			int zeroPos = axis.numValToPosition(0.0);
			Point p = translateToScreen(zeroPos, 0, null);
			if (vertNotHoriz)
				g.drawLine(0, p.y, getSize().width, p.y);
			else
				g.drawLine(p.x, 0, p.x, getSize().height);
		} catch (AxisException e) {
		}
		
		g.setColor(getForeground());
		
		super.paintView(g);
	}
	
	protected boolean canDrag() {
		return allowDrag;
	}
}
	
