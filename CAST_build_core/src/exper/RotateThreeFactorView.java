package exper;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class RotateThreeFactorView extends RotateTwoFactorView {
//	static public final String ROTATE_3_FACTOR_PLOT = "rotateThreeFactor";
	
	public RotateThreeFactorView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey, String wKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey, wKey, modelKey);
	}
	
	private Color getZColor(int w) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return TreatEffectSliderView.getShadedBarColor(w, 1.0 - rot);
	}
	
	private Color getXColor(int w) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return TreatEffectSliderView.getShadedBarColor(w, rot);
	}
	
	private Color getWColor() {
		double rot = map.getTheta1() % 90;
		if (rot > 45)
			rot = 90 - rot;
		rot /= 45;
		
		return TreatEffectSliderView.getShadedBarColor(-1, rot);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int nx = xVariable.noOfCategories();
		CatVariable zVariable = (CatVariable)getVariable(zKey);
		int nz = zVariable.noOfCategories();
		CatVariable wVariable = (CatVariable)getVariable(wKey);
		int nw = wVariable.noOfCategories();
		
		Value[] cat = new Value[3];
		
		double mean[][][] = new double[nx][nz][nw];
		for (int x=0 ; x<nx ; x++) {
			cat[0] = xVariable.getLabel(x);
			for (int z=0 ; z<nz ; z++) {
				cat[1] = zVariable.getLabel(z);
				for (int w=0 ; w<nw ; w++) {
					cat[2] = wVariable.getLabel(w);
					mean[x][z][w] = model.evaluateMean(cat);
				}
			}
		}
		Point p = null;
		Point q = null;
		
		for (int w=0 ; w<nw ; w++)
			for (int x=0 ; x<nx ; x++) {
				g.setColor(getXColor(w));
				for (int z=1 ; z<nz ; z++) {
					p = getScreenPoint(mean[x][z-1][w], x, z-1, nx, nz, p);
					q = getScreenPoint(mean[x][z][w], x, z, nx, nz, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
		
		for (int w=0 ; w<nw ; w++)
			for (int z=0 ; z<nz ; z++) {
				g.setColor(getZColor(w));
				for (int x=1 ; x<nx ; x++) {
					p = getScreenPoint(mean[x-1][z][w], x-1, z, nx, nz, p);
					q = getScreenPoint(mean[x][z][w], x, z, nx, nz, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
		
		for (int x=0 ; x<nx ; x++)
			for (int z=0 ; z<nz ; z++) {
				g.setColor(getWColor());
				for (int w=1 ; w<nw ; w++) {
					p = getScreenPoint(mean[x][z][w-1], x, z, nx, nz, p);
					q = getScreenPoint(mean[x][z][w], x, z, nx, nz, q);
					g.drawLine(p.x, p.y, q.x, q.y);
				}
			}
			
		if (residualDisplay == SQR_RESIDUALS) {
			g.setColor(kPinkColor);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			int index = 0;
			ValueEnumeration ye = yVariable.values();
			Point crossPos = null;
			Point meanPos = null;
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				int xCat = xVariable.getItemCategory(index);
				int zCat = zVariable.getItemCategory(index);
				int wCat = wVariable.getItemCategory(index);
				crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
				meanPos = getScreenPoint(mean[xCat][zCat][wCat], xCat, zCat, nx, nz, meanPos);
				if (crossPos != null && meanPos != null) {
					int top = Math.min(meanPos.y, crossPos.y);
					int height = Math.max(meanPos.y, crossPos.y) - top;
					g.fillRect(meanPos.x, top, height, height);
				}
				index ++;
			}
			g.setColor(getForeground());
		}
		
		g.setColor(getForeground());
		int oldCrossSize = getCrossSize();
		setCrossSize(oldCrossSize - 1);
		
		for (int x=0 ; x<nx ; x++)
			for (int z=0 ; z<nz ; z++)
				for (int w=0 ; w<nw ; w++) {
					p = getScreenPoint(mean[x][z][w], x, z, nx, nz, p);
					drawBlob(g, p);
				}
		setCrossSize(oldCrossSize);
		return null;
	}
	
	protected void setCrossColor(Graphics g, int xCat, int zCat, int index) {
		CatVariable wVariable = (CatVariable)getVariable(wKey);
		int layerCat = wVariable.getItemCategory(index);
		g.setColor(TreatEffectSliderView.getBaseBarColor(layerCat));
	}
}
	
