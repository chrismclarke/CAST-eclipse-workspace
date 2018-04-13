package linMod;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class RotateHistoPDFView extends RotateHistoView {
	
	static final public int HISTO = 0;
	static final public int PDF = 1;
	
	static final private int kQuadraticSteps = 20;
	
	private int displayType = HISTO;
	
	protected Normal3DArtist normalArtist;
	
	private Color meanColor = null;
	private String modelKey, modelCatXKey;
	
	public RotateHistoPDFView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String classInfoParam, String sortedXParam,
						double invPdfScaling, String modelKey, String modelCatXKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, classInfoParam, sortedXParam);
		normalArtist = new Normal3DArtist(this, modelKey, modelCatXKey, theData, yAxis, xAxis, true);
		normalArtist.setInverseScalingFactor(invPdfScaling);
		this.modelKey = modelKey;
		this.modelCatXKey = modelCatXKey;
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
	}
	
	public void setPopnMeanColor(Color meanColor) {
		this.meanColor = meanColor;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (displayType == HISTO)
			super.drawData(g, shadeHandling);
		else
			drawPDFs(g);
	}
	
	private void drawMeanLine(Graphics g) {
		Color oldColor = g.getColor();
		g.setColor(meanColor);
		CoreVariable coreModel = getVariable(modelKey);
		if (coreModel instanceof GroupsModelVariable) {
			GroupsModelVariable model = (GroupsModelVariable)coreModel;
			CatVariable xCatVar = (CatVariable)getVariable(modelCatXKey);
			int noOfCats = xCatVar.noOfCategories();
			Point p0 = null;
			Point p1 = null;
			for (int i=0 ; i<noOfCats ; i++) {
//				double xFract = (i + 0.5) / (noOfCats + 1);
				double mean = model.evaluateMean(xCatVar.getLabel(i));
				p0 = getScreenPoint(mean, 0.0, i - 0.25, p0);
				p1 = getScreenPoint(mean, 0.0, i + 0.25, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
		else {
			LinearModel model = (LinearModel)coreModel;
			double x0 = zAxis.getMinOnAxis();
			double x1 = zAxis.getMaxOnAxis();
			double y0 = model.evaluateMean(x0);
			
			Point p0 = getScreenPoint(y0, 0.0, x0, null);
			
			if (model instanceof QuadraticModel) {
				double xStep = (x1 - x0) / kQuadraticSteps;
				Point p1 = null;
				for (int i=1 ; i<=kQuadraticSteps ; i++) {
					x1 = x0 + i * xStep;
					double y1 = model.evaluateMean(x1);
					p1 = getScreenPoint(y1, 0.0, x1, p1);
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
					Point pTemp = p0;
					p0 = p1;
					p1 = pTemp;
				}
			}
			else {
				double y1 = model.evaluateMean(x1);
				Point p1 = getScreenPoint(y1, 0.0, x1, null);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
		g.setColor(oldColor);
	}
	
	private boolean fromAbove() {
		return map.getTheta2() < 180;
	}
	
	private void drawPDFs(Graphics g) {
		if (meanColor != null && fromAbove())
			drawMeanLine(g);
		
		CatVariable xCatVar = (modelCatXKey == null) ? null : (CatVariable)getVariable(modelCatXKey);
			
		if (map.xAxisBehind())
			if (xCatVar == null)
				for (int i=0 ; i<sortedX.length ; i++)
					normalArtist.paintDistn(g, sortedX[i]);
			else
				for (int i=0 ; i<xCatVar.noOfCategories() ; i++)
					normalArtist.paintDistn(g, xCatVar.getLabel(i));
		else
			if (xCatVar == null)
				for (int i=sortedX.length-1 ; i>=0 ; i--)
					normalArtist.paintDistn(g, sortedX[i]);
			else
				for (int i=xCatVar.noOfCategories()-1 ; i>=0 ; i--)
					normalArtist.paintDistn(g, xCatVar.getLabel(i));
		
		if (meanColor != null && !fromAbove())
			drawMeanLine(g);
	}
}
	
