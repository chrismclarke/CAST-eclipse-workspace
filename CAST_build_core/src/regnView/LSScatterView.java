package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class LSScatterView extends ScatterView {
//	static public final String LS_SCATTER_PLOT = "lsScatterPlot";
	
	protected String lineKey;
	
	public LSScatterView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
		setRetainLastSelection(true);
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		LinearModel model = (LinearModel)getVariable(lineKey);
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
		g.setColor(getForeground());
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (theAxis == yAxis || theAxis == axis) {
			LinearModel model = (LinearModel)getVariable(lineKey);
			model.updateLSParams(yKey);
			getData().variableChanged(lineKey);
		}
		reinitialiseAfterTransform();
		repaint();
	}
}
	
