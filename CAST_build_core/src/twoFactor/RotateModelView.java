package twoFactor;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class RotateModelView extends Rotate3DView {
	
//	static final private Color k00Color = Color.blue;
//	static final private Color k10Color = Color.red;
//	static final private Color k01Color = new Color(0x009900);
	
	static final private int kDistnWidth = 20;
	
	private String modelKey;
	
	public RotateModelView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
											D3Axis zAxis, String xKey, String yKey, String zKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		setCrossSize(LARGE_CROSS);
		this.modelKey = modelKey;
	}
	
	protected Point getScreenPoint(double y, int x, int z, int nx, int nz, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		double yFract = yAxis.numValToPosition(y);
		double xFract = (x + 0.5) / nx;
		double zFract = (z + 0.5) / nz;
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		g.setColor(getForeground());
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		int nx = xVar.noOfCategories();
		int nz = zVar.noOfCategories();
		
		Point crossPos = null;
		
		int index = 0;
		ValueEnumeration ye = yVar.values();
		g.setColor(getForeground());
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int xCat = xVar.getItemCategory(index);
			int zCat = zVar.getItemCategory(index);
			crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
			if (crossPos != null)
				drawCross(g, crossPos);
			index ++;
		}
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		TwoFactorModel model = (TwoFactorModel)getVariable(modelKey);
		double sd = model.evaluateSD().toDouble();
		double threeSD = 3.0 * sd;
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		int nx = xVar.noOfCategories();
		int nz = zVar.noOfCategories();
		
		Point p = null;
		Point q = null;
		
		int startX, endX, xStep, startZ, endZ, zStep;
		if (map.xAxisBehind()) {
			startZ = 0;
			endZ = nz;
			zStep = 1;
		}
		else {
			endZ = -1;
			startZ = nz - 1;
			zStep = -1;
		}
		if (map.zAxisBehind()) {
			startX = 0;
			endX = nx;
			xStep = 1;
		}
		else {
			endX = -1;
			startX = nx - 1;
			xStep = -1;
		}
		
		for (int x=startX ; x!=endX ; x+=xStep)
			for (int z=startZ ; z!=endZ ; z+=zStep) {
				double mean = model.evaluateMean(x, z);
				p = getScreenPoint(mean - threeSD, x, z, nx, nz, p);
				q = getScreenPoint(mean + threeSD, x, z, nx, nz, q);
				g.setColor(getDistnColor(x, z, nx, nz));
				drawDistn(g, p, q);
			}
		
		return null;
	}
	
	private Color getDistnColor(int x, int z, int nx, int nz) {
		float pRed = x / (nx - 1.0f);
		float pGreen = z / (nz - 1.0f);
		float pBlue = Math.max(0, 1 - pRed - pGreen);
		
		pGreen *= 0.75;		//	to darken it
		
		return new Color(pRed, pGreen, pBlue);
	}
	
	private void drawDistn(Graphics g, Point lowPos, Point highPos) {
		int highY = Math.max(highPos.y, lowPos.y);
		int lowY = Math.min(highPos.y, lowPos.y);
		int distnHt = highY - lowY;
		if (distnHt == 0)
			g.drawLine(lowPos.x, lowY, lowPos.x + kDistnWidth, lowY);
		else {
			double mid = (lowY + highY) * 0.5;
			double sd = distnHt / 6.0;
			for (int pos=lowY ; pos<=highY ;  pos++) {
				double z = (pos - mid) / sd;
				double density = Math.exp(-0.5 * z * z);
				int width = (int)Math.round(density * kDistnWidth);
				g.drawLine(lowPos.x, pos, lowPos.x + width, pos);
			}
		}
	}
	
}
	
