package map;

import java.awt.*;

import dataView.*;

import dynamic.*;


public class ShadedCirclesMapView extends ShadedMapView {
//	static public final String SHADED_CIRCLES_MAP = "shadedCirclesMap";
	
	static final protected Color kUnknownFillColor = new Color(0xAAAAAA);
	static final private Color kUnknownRadiusColor = new Color(0x777777);
	
	private Color fixedFillColor = null;
	
	private String circleFillKey = null;
	private double minFillY, maxFillY;
	private Color circleFillColors[];		//		only for num variables
	
	private String circleSizeKey = null;
	private double maxCircleY;
	private int maxScreenRadius;
	
	public ShadedCirclesMapView(DataSet theData, XApplet applet, String regionKey) {
		super(theData, applet, regionKey, null, 0);
	}
	
	public void setCircleSizeVariable(String circleSizeKey, int maxCircleRadius) {
		this.circleSizeKey = circleSizeKey;
		if (circleSizeKey == null)
			return;
		NumVariable yVar = (NumVariable)getVariable(circleSizeKey);
		maxCircleY = 0.0;
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues()) {
			NumValue y = (NumValue)ye.nextValue();
			if (y instanceof NumSeriesValue) {
				NumSeriesValue ySeries = (NumSeriesValue)y;
				for (int i=0 ; i<ySeries.seriesLength() ; i++) {
					double yDouble = ySeries.toDouble(i);
					if (!Double.isNaN(yDouble))
						maxCircleY = Math.max(maxCircleY, yDouble);
				}
			}
			else {
				double yDouble = y.toDouble();
				if (!Double.isNaN(yDouble))
					maxCircleY = Math.max(maxCircleY, yDouble);
			}
		}
		setMaxCircleRadius(maxCircleRadius);
	}
	
	public void setMaxCircleRadius(int maxScreenRadius) {
		this.maxScreenRadius = maxScreenRadius;
	}
	
	public void setFixedCircleColor(Color fixedFillColor) {		//	null for no circles
		this.fixedFillColor = fixedFillColor;
		circleFillKey = null;
	}
	
	public void setNumVarCircleColor(String circleFillKey, double minFillY, double maxFillY,
																																			Color[] numFillColors) {
		this.circleFillKey = circleFillKey;
		this.minFillY = minFillY;
		this.maxFillY = maxFillY;
		circleFillColors = numFillColors;
		fixedFillColor = null;
	}
	
	public void setCatVarCircleColor(String circleFillKey, Color[] catFillColors) {
		this.circleFillKey = circleFillKey;
		circleFillColors = catFillColors;
		fixedFillColor = null;
	}
	
	protected Color getRegionColor(CatVariable yCatVar, NumVariable yNumVar, CatVariable conditVar,
																												int index) {
		Color regionColor = super.getRegionColor(yCatVar, yNumVar, conditVar, index);
		if (circleSizeKey !=  null && (fixedFillColor != null || circleFillKey != null))
			regionColor = dimColor(regionColor, 0.5);
		return regionColor;
	}
	
	private Color getCircleFillColor(Variable fillVar, int index) {
		if (fillVar == null)
			return fixedFillColor;
		else if (fillVar instanceof NumVariable) {
			double fillY = ((NumVariable)fillVar).doubleValueAt(index);
			if (Double.isNaN(fillY))
				return kUnknownFillColor;
			else {
				double fillPropn = (fillY - minFillY) / (maxFillY - minFillY);
				return NumKeyView.mixColors(fillPropn, circleFillColors);
			}
		}
		else {
			int catIndex = ((CatVariable)fillVar).getItemCategory(index);
			return circleFillColors[catIndex];
		}
	}
	
	protected void drawOneCircle(Graphics g, RegionValue region, double ySize,
																		int radius, Variable fillVar, int selectedRegion,
																		RegionVariable regionVar, int index) {
		Color fillColor = getCircleFillColor(fillVar, index);
		fillColor = (selectedRegion < 0 || index == selectedRegion) ? fillColor
																													: dimColor(fillColor, 0.5);
		Color outlineColor = darkenColor(fillColor, 0.5);
	
		region.drawCircle(g, radius, fillColor, outlineColor, regionVar);
	}
	
	protected void drawCircles(Graphics g, RegionVariable regionVar, int selectedRegion,
																													int maxCircleRadius) {
		if (circleSizeKey == null || fixedFillColor == null && circleFillKey == null)
			return;
		
		NumVariable ySizeVar = (NumVariable)getVariable(circleSizeKey);
		int sortedSizeIndex[] = ySizeVar.getSortedIndex();
		
		Variable fillVar = (circleFillKey == null) ? null : (Variable)getVariable(circleFillKey);
		
		for (int i=sortedSizeIndex.length-1 ; i>=0 ; i--) {
			int index = sortedSizeIndex[i];
			RegionValue nextR = (RegionValue)regionVar.valueAt(index);
			double ySize = ySizeVar.doubleValueAt(index);
			
			if (Double.isNaN(ySize)) 
				nextR.drawUnknown(g, kUnknownRadiusColor, regionVar);
			else {
				int radius = (int)Math.round(Math.sqrt(ySize / maxCircleY) * maxCircleRadius);
				
				drawOneCircle(g, nextR, ySize, radius, fillVar, selectedRegion, regionVar, index);
			}
		}
	}
	
	public void paintView(Graphics g) {
		RegionVariable regionVar = (RegionVariable)getVariable(regionKey);
		regionVar.scaleGraphicsToFit(g, kLeftRightBorder, kTopBottomBorder,
									getSize().width - 2 * kLeftRightBorder, getSize().height - 2 * kTopBottomBorder);
		int maxCircleRadius = regionVar.rescaleRadius(maxScreenRadius);
		
		int selectedRegion = getSelection().findSingleSetFlag();
		
		fillRegions(g, regionVar, selectedRegion);
		
		drawCircles(g, regionVar, selectedRegion, maxCircleRadius);
		
		regionVar.restoreGraphicScaling(g);
		
					//	The first time a map it displayed, its coordinates are those of its
					//	parent but on the second paint(), it displays properly.
					//	A fix is to call repaint() the first time it is drawn.
		if (firstTime) {
			firstTime = false;
			repaint();
		}
	}
	
}
