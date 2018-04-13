package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class ScatterLinesView extends ScatterView {
//	static public final String SCATTER_LINES_PLOT = "scatterLinesPlot";
	
	private String line0Key, line1Key;
	private boolean showLine0 = false;
	private boolean showLine1 = false;
	
	public ScatterLinesView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String line0Key,
						String line1Key) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.line0Key = line0Key;
		this.line1Key = line1Key;
	}
	
	public void showLine(int index, boolean showNotHide) {
		if (index == 0) {
			if (showLine0 != showNotHide) {
				showLine0 = showNotHide;
				repaint();
			}
		}
		else  if (index == 1) {
			if (showLine1 != showNotHide) {
				showLine1 = showNotHide;
				repaint();
			}
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		g.setColor(getForeground());
		
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		if (showLine0) {
			g.setColor(Color.green);
			LinearModel model0 = (LinearModel)getVariable(line0Key);
			model0.drawMean(g, this, axis, yAxis);
		}
		if (showLine1) {
			g.setColor(Color.red);
			LinearModel model1 = (LinearModel)getVariable(line1Key);
			model1.drawMean(g, this, axis, yAxis);
		}
	}
}
	
