package exper;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class RotateTwoFactorView extends Rotate3DView {
//	static public final String ROTATE_2_FACTOR_PLOT = "rotateTwoFactor";
	
	static final protected Color kPinkColor = new Color(0xFF6699);
	
	static final public int NO_SLICE = 0;
	static final public int TREAT1_SLICE = 1;
	static final public int TREAT2_SLICE = 2;
	
	static final public int NO_RESIDUALS = 0;
	static final public int LINE_RESIDUALS = 1;
	static final public int SQR_RESIDUALS = 2;
	
	static final public int ALL_BLACK = 0;
	static final public int X_COLOURS = 1;
	static final public int Z_COLOURS = 2;
	
	protected String modelKey, wKey;
	protected int residualDisplay = NO_RESIDUALS;
	private int crossColouring = ALL_BLACK;
	
	private int sliceVariable = NO_SLICE;
	private int treat1Slice = 0;
	private int treat2Slice = 0;
	
	private double zCatToNum[];			//	used if z-axis is numerical
	
	public RotateTwoFactorView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey, String wKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		this.modelKey = modelKey;
		this.wKey = wKey;
	}
	
	public RotateTwoFactorView(DataSet theData, XApplet applet,
								D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String xKey,
								String yKey, String zKey, String modelKey) {
		this(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey, null, modelKey);
	}
	
	public RotateTwoFactorView(DataSet theData, XApplet applet,
								D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String xKey,
								String yKey, String zKey, String modelKey, double[] zCatToNum) {
		this(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey, null, modelKey);
		this.zCatToNum = zCatToNum;
	}
	
	public void setResidualDisplay(int residualDisplay) {
		this.residualDisplay = residualDisplay;
	}
	
	public void setSliceVariable(int sliceVariable) {
		if (this.sliceVariable != sliceVariable) {
			this.sliceVariable = sliceVariable;
			repaint();
		}
	}
	
	public void setSlice(int slice) {
		if (sliceVariable == TREAT1_SLICE)
			treat1Slice = slice;
		else
			treat2Slice = slice;
		repaint();
	}
	
	public void setCrossColouring(int crossColouring) {
		this.crossColouring = crossColouring;
	}
	
	protected Point getScreenPoint(double y, int x, int z, int nXCats, int nZCats, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		double yFract = yAxis.numValToPosition(y);
		double xFract = xAxis.catValToPosition(x, nXCats);
		double zFract = (zCatToNum == null) ? zAxis.catValToPosition(z, nZCats)
																				: zAxis.numValToPosition(zCatToNum[z]);
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	protected void setCrossColor(Graphics g, int xCat, int zCat, int index) {
		if (crossColouring != ALL_BLACK) {
			if (crossColouring == X_COLOURS)
				g.setColor(TreatEffectSliderView.getBaseBarColor(xCat));
			else
				g.setColor(TreatEffectSliderView.getBaseBarColor(zCat));
		}
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (drawData) {
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			CatVariable wVariable = null;
			if (wKey != null)
				wVariable = (CatVariable)getVariable(wKey);
			int nx = xVariable.noOfCategories();
			int nz = zVariable.noOfCategories();
			
			Point crossPos = null;
			
			if (residualDisplay != NO_RESIDUALS) {
				g.setColor(Color.red);
				FactorsModel model = (FactorsModel)getVariable(modelKey);
				int[] cat = new int[wKey == null ? 2 : 3];
				int index = 0;
				ValueEnumeration ye = yVariable.values();
				Point meanPos = null;
				while (ye.hasMoreValues()) {
					double y = ye.nextDouble();
					int xCat = cat[0] = xVariable.getItemCategory(index);
					int zCat = cat[1] = zVariable.getItemCategory(index);
					if (wKey != null)
						cat[2] = wVariable.getItemCategory(index);
					double mean = model.evaluateMean(cat);
					crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
					meanPos = getScreenPoint(mean, xCat, zCat, nx, nz, meanPos);
					if (crossPos != null && meanPos != null)
						g.drawLine(meanPos.x, meanPos.y, crossPos.x, crossPos.y);
					index ++;
				}
				g.setColor(getForeground());
			}
			
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
		
		return TreatEffectSliderView.getShadedBarColor((zCatToNum == null) ? z : -1, 1.0 - rot);
	}
	
	private Color getXColor(int x) {
		double rot = map.getTheta1() % 180;
		if (rot > 90)
			rot = 180 - rot;
		rot /= 90;
		
		return TreatEffectSliderView.getShadedBarColor(x, rot);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		FactorsModel model = (FactorsModel)getVariable(modelKey);
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int nx = xVariable.noOfCategories();
		CatVariable zVariable = (CatVariable)getVariable(zKey);
		int nz = zVariable.noOfCategories();
		
		int[] cat = new int[2];
		
		double mean[][] = new double[nx][nz];
		for (int x=0 ; x<nx ; x++) {
			cat[0] = x;
			for (int z=0 ; z<nz ; z++) {
				cat[1] = z;
				mean[x][z] = model.evaluateMean(cat);
			}
		}
		Point p = null;
		Point q = null;
		
		if (sliceVariable != TREAT2_SLICE)
			for (int x=0 ; x<nx ; x++) {
				if (sliceVariable == NO_SLICE || treat1Slice == x) {
					g.setColor(getXColor(x));
					for (int z=1 ; z<nz ; z++) {
						p = getScreenPoint(mean[x][z-1], x, z-1, nx, nz, p);
						q = getScreenPoint(mean[x][z], x, z, nx, nz, q);
						g.drawLine(p.x, p.y, q.x, q.y);
					}
				}
			}
		
		if (sliceVariable != TREAT1_SLICE)
			for (int z=0 ; z<nz ; z++) {
				if (sliceVariable == NO_SLICE || treat2Slice == z) {
					g.setColor(getZColor(z));
					for (int x=1 ; x<nx ; x++) {
						p = getScreenPoint(mean[x-1][z], x-1, z, nx, nz, p);
						q = getScreenPoint(mean[x][z], x, z, nx, nz, q);
						g.drawLine(p.x, p.y, q.x, q.y);
					}
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
				crossPos = getScreenPoint(y, xCat, zCat, nx, nz, crossPos);
				meanPos = getScreenPoint(mean[xCat][zCat], xCat, zCat, nx, nz, meanPos);
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
			for (int z=0 ; z<nz ; z++) {
				p = getScreenPoint(mean[x][z], x, z, nx, nz, p);
				drawBlob(g, p);
			}
		setCrossSize(oldCrossSize);
		return null;
	}
}
	
