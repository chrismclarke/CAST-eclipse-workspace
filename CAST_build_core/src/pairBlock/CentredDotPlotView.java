package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class CentredDotPlotView extends DotPlotView {
	
	private NumCatAxis diffLabelAxis;
	private PairedDotPlotView pairedView;
	private int groupCentre;
	
	public CentredDotPlotView(DataSet theData, XApplet applet,
								NumCatAxis theAxis, NumCatAxis diffLabelAxis, PairedDotPlotView pairedView,
								double initialJittering) {
		super(theData, applet, theAxis, initialJittering);
		this.diffLabelAxis = diffLabelAxis;
		this.pairedView = pairedView;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point p = super.getScreenPoint(index, theVal, thePoint);
		if (p != null)
			if (vertNotHoriz)
				p.x += groupCentre - currentJitter / 2;
			else
				p.y -= groupCentre - currentJitter / 2;
		return p;
	}
	
	protected void checkJittering() {
		jittering = pairedView.getJittering();
		initialiseJittering();
	}
	
	public void paintView(Graphics g) {
		groupCentre = diffLabelAxis.catValToPosition(0);
		super.paintView(g);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		groupCentre = diffLabelAxis.catValToPosition(0);
		
		return super.getPosition(x, y);
	}
}
	
