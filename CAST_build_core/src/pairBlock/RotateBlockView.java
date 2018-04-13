package pairBlock;

import java.awt.*;

import dataView.*;
import graphics3D.*;

import exper.*;


public class RotateBlockView extends Rotate3DView {
	
	static final public int ALL_BLACK = 0;
	static final public int SHOW_COLOURS = 1;
	
	protected String lsKey;
	private int colouring = ALL_BLACK;
	
	public RotateBlockView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey, String lsKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		this.lsKey = lsKey;
	}
	
	public void setColouring(int colouring) {
		this.colouring = colouring;
	}
	
	protected Point getScreenPoint(double y, int x, int z, int nXCats, int nZCats, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		double yFract = yAxis.numValToPosition(y);
		double xFract = xAxis.catValToPosition(x, nXCats);
		double zFract = zAxis.catValToPosition(z, nZCats);
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	protected void setCrossColor(Graphics g, int xCat, int zCat, int index) {
		if (colouring == SHOW_COLOURS)
			g.setColor(TreatEffectSliderView.getBaseBarColor(zCat));
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (drawData) {
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			int nx = xVariable.noOfCategories();
			int nz = zVariable.noOfCategories();
			
			Point crossPos = null;
			
			int index = 0;
			ValueEnumeration ye = yVariable.values();
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				int xCat = xVariable.getItemCategory(index);
				int zCat = zVariable.getItemCategory(index);
				crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
				if (crossPos != null) {
					setCrossColor(g, xCat, zCat, index);
					drawCross(g, crossPos);
				}
				index ++;
			}
			g.setColor(getForeground());
		}
	}
	
	private Color getZColor(int z) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return TreatEffectSliderView.getShadedBarColor(z, 1.0 - rot);
	}
	
	private Color getXColor(int x) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return TreatEffectSliderView.getShadedBarColor(x, rot);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		if (colouring == ALL_BLACK)
			return null;		
		
//		MultipleRegnModel model = (MultipleRegnModel)getVariable(lsKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int nx = xVariable.noOfCategories();
		CatVariable zVariable = (CatVariable)getVariable(zKey);
		int nz = zVariable.noOfCategories();
		
//		Value[] cat = new Value[2];
		
		double mean[][] = new double[nx][nz];
//		for (int x=0 ; x<nx ; x++) {
//			cat[0] = xVariable.getLabel(x);
//			for (int z=0 ; z<nz ; z++) {
//				cat[1] = zVariable.getLabel(z);
//				mean[x][z] = model.evaluateMean(cat);
//			}
//		}
		
		int n[][] = new int[nx][nz];
		int index = 0;
		ValueEnumeration ye = yVariable.values();
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int xCat = xVariable.getItemCategory(index);
			int zCat = zVariable.getItemCategory(index);
			mean[xCat][zCat] += y;
			n[xCat][zCat] ++;
			index ++;
		}
		for (int x=0 ; x<nx ; x++)
			for (int z=0 ; z<nz ; z++)
				mean[x][z] /= n[x][z];
		
		Point p = null;
		Point q = null;
		
		for (int x=0 ; x<nx ; x++) {
			g.setColor(getXColor(x));
			for (int z=1 ; z<nz ; z++) {
				p = getScreenPoint(mean[x][z-1], x, z-1, nx, nz, p);
				q = getScreenPoint(mean[x][z], x, z, nx, nz, q);
				g.drawLine(p.x, p.y, q.x, q.y);
			}
		}
	
		for (int z=0 ; z<nz ; z++) {
			g.setColor(getZColor(z));
			for (int x=1 ; x<nx ; x++) {
				p = getScreenPoint(mean[x-1][z], x-1, z, nx, nz, p);
				q = getScreenPoint(mean[x][z], x, z, nx, nz, q);
				g.drawLine(p.x, p.y, q.x, q.y);
			}
		}
		
//		g.setColor(getForeground());
//		int oldCrossSize = getCrossSize();
//		setCrossSize(oldCrossSize - 1);
//		
//		for (int x=0 ; x<nx ; x++)
//			for (int z=0 ; z<nz ; z++) {
//				p = getScreenPoint(mean[x][z], x, z, nx, nz, p);
//				drawBlob(g, p);
//			}
//		setCrossSize(oldCrossSize);
	
		return null;
	}
}
	
