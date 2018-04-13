package map;

import java.awt.*;

import dataView.*;


public class ShadedMapView extends DataView {
//	static public final String SHADED_MAP = "shadedMap";
	
	static public final int kLeftRightBorder = 10;
	static public final int kTopBottomBorder = 10;
	
	static public final double kDimProportion = 0.8;
	
	static final private Color kUnknownColor = Color.lightGray;
	
	protected String regionKey;
	private String yKey;
	private double minY, maxY;
	private Color catColors[];		//		only for cat variables
	private Color numColors[];		//		only for num variables
	
	private String conditKey;
	private int conditIndex;
	
	protected boolean firstTime = true;
					//	The first time a map it displayed, its coordinates are those of its
					//	parent but on the second paint(), it displays properly.
					//	A fix is to call repaint() the first time it is drawn.
	
	public ShadedMapView(DataSet theData, XApplet applet, String regionKey, String conditKey, int conditIndex) {
		super(theData, applet, null);
		this.regionKey = regionKey;
		this.conditKey = conditKey;
		this.conditIndex = conditIndex;
		repaint();
	}
	
	public ShadedMapView(DataSet theData, XApplet applet, String regionKey) {
		this(theData, applet, regionKey, null, 0);
	}
	
	public void setNumDisplayKey(String yKey, double minY, double maxY, Color[] numColors) {
		this.yKey = yKey;
		this.minY = minY;
		this.maxY = maxY;
		this.numColors = numColors;
		repaint();
	}
	
	public void setCatDisplayKey(String yKey, Color[] catColors) {
		this.yKey = yKey;
		this.catColors = catColors;
		repaint();
	}
	
	public void setCondit(String conditKey, int conditIndex) {
		this.conditKey = conditKey;
		this.conditIndex = conditIndex;
		repaint();
	}
	
	protected Color getRegionColor(CatVariable yCatVar, NumVariable yNumVar, CatVariable conditVar,
																												int index) {
		Color regionColor;
		if (yCatVar != null) {
			int nextY = yCatVar.getItemCategory(index);
			if (conditVar == null || conditVar.getItemCategory(index) == conditIndex)
				regionColor = catColors[nextY];
			else
				regionColor = kUnknownColor;
		}
		else {
			double nextY = yNumVar.doubleValueAt(index);
			if (Double.isNaN(nextY) || (conditVar != null && conditVar.getItemCategory(index) != conditIndex))
				regionColor = kUnknownColor;
			else
				regionColor = NumKeyView.mixColors((nextY - minY) / (maxY - minY), numColors);
		}
		return regionColor;
	}
	
	protected void fillRegions(Graphics g, RegionVariable regionVar, int selectedRegion) {
		CatVariable yCatVar = null;
		NumVariable yNumVar = null;
		
		Variable coreVar = (Variable)getVariable(yKey);
		if(coreVar instanceof CatVariable)
			yCatVar = (CatVariable)coreVar;
		else
			yNumVar = (NumVariable)coreVar;
		
		CatVariable conditVar = (conditKey == null) ? null : (CatVariable)getVariable(conditKey);
		
		for (int i=0 ; i<regionVar.noOfValues() ; i++) {
			RegionValue nextR = (RegionValue)regionVar.valueAt(i);
			Color regionColor = getRegionColor(yCatVar, yNumVar, conditVar, i);
			
			if (selectedRegion >= 0 && selectedRegion != i)
				regionColor = dimColor(regionColor, kDimProportion);
			g.setColor(regionColor);
			
			nextR.fillInMap(g, regionVar);
		}
	}
	
	public void paintView(Graphics g) {
		RegionVariable regionVar = (RegionVariable)getVariable(regionKey);
		regionVar.scaleGraphicsToFit(g, kLeftRightBorder, kTopBottomBorder,
									getSize().width - 2 * kLeftRightBorder, getSize().height - 2 * kTopBottomBorder);
		
		int selectedRegion = getSelection().findSingleSetFlag();
		
		fillRegions(g, regionVar, selectedRegion);
		
		regionVar.restoreGraphicScaling(g);
		
					//	The first time a map it displayed, its coordinates are those of its
					//	parent but on the second paint(), it displays properly.
					//	A fix is to call repaint() the first time it is drawn.
		if (firstTime) {
			firstTime = false;
			repaint();
		}
	}


//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y < kTopBottomBorder || y >= getSize().height - kTopBottomBorder
													|| x < kLeftRightBorder || x >= getSize().width - kLeftRightBorder)
			return null;
		
		RegionVariable regionVar = (RegionVariable)getVariable(regionKey);
		
		int hitIndex = regionVar.findHit(x, y, kLeftRightBorder, kTopBottomBorder,
									getSize().width - 2 * kLeftRightBorder, getSize().height - 2 * kTopBottomBorder);
		
		if (hitIndex >= 0)
			return new IndexPosInfo(hitIndex);
		else
			return null;
	}


//-----------------------------------------------------------------------------------
	
	public Dimension getPreferredSize() {		//		Mainly for use with MapLayout
		RegionVariable regionVar = (RegionVariable)getVariable(regionKey);
		Rectangle sourceBounds = regionVar.getRegionBounds();
		firstTime = true;			//	To cause a repaint() the first time map is drawn
		return new Dimension(sourceBounds.width, sourceBounds.height);
	}
}
