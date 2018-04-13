package simulation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class CumulativePlotView extends DataView {
	static final private Color kLightGray = new Color(0xCCCCCC);
	
	private String distnKey, rectKey, valueKey;
	private NumCatAxis xAxis, probAxis;
	
	private BackgroundNormalArtist densityDrawer;
	private DistnInfo cumDistnInfo;
	
	private int cumX[];
	private int cumY[];
	private int cumPointsUsed;
	
	public CumulativePlotView(DataSet theData, XApplet applet, NumCatAxis xAxis, NumCatAxis probAxis,
							String distnKey, String rectKey, String valueKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.distnKey = distnKey;
		this.rectKey = rectKey;
		this.valueKey = valueKey;
		this.xAxis = xAxis;
		this.probAxis = probAxis;
		
		updateCumDistnInfo();
		
		densityDrawer = new BackgroundNormalArtist(distnKey, theData);
		densityDrawer.setMaxDensityFactor(2.0);			//		max density ht = half of view ht
		densityDrawer.setFillColor(kLightGray);
		densityDrawer.setHighlightColor(kLightGray);
	}
	
	private boolean updateCumDistnInfo() {
		ContinDistnVariable distn = (ContinDistnVariable)getVariable(distnKey);
		if (cumDistnInfo ==  null || !cumDistnInfo.sameParams(distn)) {
			if (distn instanceof NormalPlusDistnVariable)
				cumDistnInfo = new CumNormalPlusDistnInfo((NormalPlusDistnVariable)distn);
			else if (distn instanceof NormalDistnVariable)
				cumDistnInfo = new CumNormalDistnInfo();
			else
				return false;
			return true;
		}
		return false;
	}
	
	private void addPointToPoly(int x, int y) {
		cumX[cumPointsUsed] = x;
		cumY[cumPointsUsed ++] = y;
	}
	
	public void paintView(Graphics g) {
		densityDrawer.paintDistn(g, this, xAxis);
		
		ContinDistnVariable distn = (ContinDistnVariable)getVariable(distnKey);
		boolean updatePts = updateCumDistnInfo();
		
		DistnPoints zp0 = cumDistnInfo.getLowPoints();
		DistnPoints zp1 = cumDistnInfo.getHighPoints();
		int nLowPoints = (zp0 == null) ? 0 : zp0.elementsUsed;
		int nHighPoints = (zp1 == null) ? 0 : zp1.elementsUsed;
		int nPoints = nLowPoints + nHighPoints;
		
		if (cumX == null || cumX.length != nPoints || updatePts) {
			if (cumX == null || cumX.length != nPoints) {
				cumX = new int[nPoints];
				cumY = new int[nPoints];
			}
			
			Point p = null;
			cumPointsUsed = 0;
			
			for (int i=nLowPoints-1 ; i>=0 ; i--) {
				double x = distn.zToX(zp0.z[i]);
				int xPos = xAxis.numValToRawPosition(x);
				int yPos = probAxis.numValToRawPosition(zp0.d[i]);
				p = translateToScreen(xPos, yPos, p);
				addPointToPoly(p.x, p.y);
			}
			for (int i=1 ; i<nHighPoints ; i++) {
				double x = distn.zToX(zp1.z[i]);
				int xPos = xAxis.numValToRawPosition(x);
				int yPos = probAxis.numValToRawPosition(zp1.d[i]);
				p = translateToScreen(xPos, yPos, p);
				addPointToPoly(p.x, p.y);
			}
		}
		
		g.setColor(getForeground());
		for (int i=1 ; i<cumPointsUsed ; i++)
			g.drawLine(cumX[i-1], cumY[i-1], cumX[i], cumY[i]);
		
		
		NumVariable rectVar = (NumVariable)getVariable(rectKey);
		NumVariable normalVar = (NumVariable)getVariable(valueKey);
		
		NumValue[] sortedRect = rectVar.getSortedData();
		int[] sortedIndex = rectVar.getSortedIndex();
		for (int i=0 ; i<sortedRect.length ; i++) {
			double rectValue = sortedRect[i].toDouble();
			double normalValue = normalVar.doubleValueAt(sortedIndex[i]);
			
			Point p1 = translateToScreen(0, probAxis.numValToRawPosition(rectValue), null);
			Point p2 = translateToScreen(xAxis.numValToRawPosition(normalValue), 0, null);
			
			if (i == 0)
				 g.setColor(Color.red);
			g.drawLine(p1.x, p1.y, p2.x, p1.y);
			g.drawLine(p2.x, p1.y, p2.x, p2.y);
			
			if (i == 0) {
				g.drawLine(p2.x, p2.y, p2.x - 3, p2.y - 3);
				g.drawLine(p2.x, p2.y, p2.x + 3, p2.y - 3);
				g.setColor(getForeground());
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
