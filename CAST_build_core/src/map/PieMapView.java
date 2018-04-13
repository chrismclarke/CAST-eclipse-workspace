package map;

import java.awt.*;

import dataView.*;


public class PieMapView extends ShadedCirclesMapView {
	
//	private String pieKey[];
	private NumVariable pieVar[];
	private Color pieColors[];
	private Color dimPieColors[];
	private Color darkPieColors[];
	
	public PieMapView(DataSet theData, XApplet applet, String regionKey) {
		super(theData, applet, regionKey);
	}
	
	public void setPieVars(String[] pieKey, Color[] pieColors) {
//		this.pieKey = pieKey;
		pieVar = new NumVariable[pieKey.length];
		for (int i=0 ; i<pieKey.length ; i++)
			pieVar[i] = (NumVariable)getVariable(pieKey[i]);
		
		this.pieColors = pieColors;
		dimPieColors = new Color[pieColors.length];
		darkPieColors = new Color[pieColors.length];
		for (int i=0 ; i<pieColors.length ; i++) {
			dimPieColors[i] = dimColor(pieColors[i], 0.5);
			darkPieColors[i] = darkenColor(pieColors[i], 0.5);
		}
		
		setFixedCircleColor(Color.black);			//		because drawCircles() does not draw circles otherwise
	}
	
	protected void drawOneCircle(Graphics g, RegionValue region, double ySize,
																		int radius, Variable fillVar, int selectedRegion,
																		RegionVariable regionVar, int index) {
		double y[] = new double[pieVar.length];
		double sumY = 0.0;
		for (int i=0 ; i<pieVar.length ; i++) {
			y[i] = pieVar[i].doubleValueAt(index);
			sumY += y[i];
		}
		if (Double.isNaN(sumY)) {
			region.drawCircle(g, radius, kUnknownFillColor, darkenColor(kUnknownFillColor, 0.5), regionVar);
			return;
		}
		for (int i=0 ; i<y.length ; i++)
			 y[i] /= sumY;
		
		Color c[] = (selectedRegion < 0 || index == selectedRegion) ? pieColors : dimPieColors;
		Color cOutline[] = (selectedRegion < 0 || index == selectedRegion) ? darkPieColors : pieColors;
	
		region.drawPie(g, radius, y, c, cOutline, regionVar);
	}
	
}
