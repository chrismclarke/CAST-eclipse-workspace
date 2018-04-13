package bivarCat;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class D3BarChartView extends Rotate3DView {
//	static public final String BARCHART_3D_PLOT = "barChart3DPlot";
	
	private int counts[][];
	
	public D3BarChartView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
																						D3Axis zAxis, String xKey, String zKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, null, zKey);
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			CatVariable xVariable = (CatVariable)getVariable(xKey);
			CatVariable zVariable = (CatVariable)getVariable(zKey);
			
			counts = xVariable.getCounts((Variable)zVariable);
			return true;
		}
		else
			return false;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		g.setColor(Color.blue);
		Point barTop = null;
		Point barBottom = null;
		int xNoOfCats = counts.length;
		int zNoOfCats = counts[0].length;
		for (int ix=0 ; ix<xNoOfCats ; ix++)
			for (int iz=0 ; iz<zNoOfCats ; iz++) {
				double xFract = xAxis.catValToPosition(ix, xNoOfCats);
				double zFract = zAxis.catValToPosition(iz, zNoOfCats);
				barBottom = translateToScreen(map.mapH3DGraph(0.0, xFract, zFract),
													map.mapV3DGraph(0.0, xFract, zFract), barBottom);
				
				double yFract = yAxis.numValToPosition(counts[ix][iz]);
				barTop = translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
													map.mapV3DGraph(yFract, xFract, zFract), barTop);
				g.fillRect(barTop.x - 1, barTop.y, 3, barBottom.y - barTop.y);
			}
	}
}
	
