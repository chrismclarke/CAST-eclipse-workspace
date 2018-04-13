package twoFactor;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class RotateDiffModelsView extends RotateDragFactorsView {
	static final private Color kDiffColor = Color.red;
	
	private String model2Key;
	private boolean showFirstModel = true;
	
	public RotateDiffModelsView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey, String model1Key, String model2Key) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey, model1Key);
		this.model2Key = model2Key;
		setAllowDragParams(false);
		setShowResiduals(false);
		setDrawGridBlobs(false);
	}
	
	public void setShowFirstModel(boolean showFirstModel) {
		this.showFirstModel = showFirstModel;
	}
	
	protected String getGridModelKey() {
		if (showFirstModel)
			return super.getGridModelKey();
		else
			return model2Key;
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		String model1Key = super.getGridModelKey();
		
		TwoFactorModel model1 = (TwoFactorModel)getVariable(model1Key);
		TwoFactorModel model2 = (TwoFactorModel)getVariable(model2Key);
		
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int nx = xVariable.noOfCategories();
		CatVariable zVariable = (CatVariable)getVariable(zKey);
		int nz = zVariable.noOfCategories();
		
		Point p = null;
		Point q = null;
		
		g.setColor(kDiffColor);
		
		for (int x=0 ; x<nx ; x++) {
			for (int z=0 ; z<nz ; z++) {
				p = getScreenPoint(model1.evaluateMean(x, z), x, z, nx, nz, p);
				q = getScreenPoint(model2.evaluateMean(x, z), x, z, nx, nz, q);
				for (int i=-1 ; i<=1 ; i++)
					g.drawLine(p.x + i, p.y, q.x + i, q.y);
			}
		}
		
		super.drawShadeRegion(g);
		
		return null;
	}
}
	
