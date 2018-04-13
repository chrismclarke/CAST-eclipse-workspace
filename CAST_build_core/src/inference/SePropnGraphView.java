package inference;

import java.awt.*;

import dataView.*;
import axis.*;


public class SePropnGraphView extends DataView {
//	static public final String SE_PROPN_GRAPH = "sePropnGraph";
	
	static final private int kArrowSize = 3;
	
	private String xKey;
	private HorizAxis pAxis;
	private VertAxis seAxis;
	
	private int pPos[];
	private int sePos[];
//	private int pointsUsed;
	
	private boolean initialised = false;
	
	public SePropnGraphView(DataSet theData, XApplet applet, String xKey, HorizAxis pAxis, VertAxis seAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.pAxis = pAxis;
		this.seAxis = seAxis;
		
		CatVariable x = (CatVariable)getVariable(xKey);
		int n = x.noOfValues();
		pPos = new int[n + 1];
		sePos = new int[n + 1];
//		pointsUsed = 0;
	}
	
/*
	private void addPointToPoly(int x, int y) {
		pPos[pointsUsed] = x;
		sePos[pointsUsed ++] = y;
	}
*/
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		CatVariable x = (CatVariable)getVariable(xKey);
		int n = x.noOfValues();
		
		for (int i=0 ; i<=n ; i++)
			try {
				double p = i / (double)n;
				pPos[i] = pAxis.numValToPosition(p);
				sePos[i] = seAxis.numValToPosition(Math.sqrt(p * (1 - p) / n));
			} catch (AxisException e) {
			}
		
		initialised = true;
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int n = xVar.noOfValues();
		int counts[] = xVar.getCounts();
		int x = counts[0];
		
		Point p1 = translateToScreen(pPos[0], sePos[0], null);
		Point p0 = null;
		for (int i=1 ; i<=n ; i++) {
			Point temp = translateToScreen(pPos[i], sePos[i], p0);
			p0 = p1;
			p1 = temp;
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		
		g.setColor(Color.red);
		p0 = translateToScreen(pPos[x], sePos[x], p0);
		g.drawLine(p0.x, p0.y, p0.x, getSize().height);
		g.drawLine(p0.x, p0.y, 0, p0.y);
		g.drawLine(0, p0.y, kArrowSize, p0.y + kArrowSize);
		g.drawLine(0, p0.y, kArrowSize, p0.y - kArrowSize);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
