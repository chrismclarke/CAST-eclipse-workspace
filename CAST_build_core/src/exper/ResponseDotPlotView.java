package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ResponseDotPlotView extends DotPlotView {
//	static public final String RESPONSE_DOTPLOT = "responseDotPlot";
	
	private String catKey, yKey;
	private CatVariable groupVariable;
	private boolean showResponse = false;
	
	public ResponseDotPlotView(DataSet theData, XApplet applet,
						NumCatAxis theAxis, String catKey, String yKey, double initialJittering) {
		super(theData, applet, theAxis, initialJittering);
		groupVariable = (CatVariable)theData.getVariable(catKey);
		this.catKey = catKey;
		this.yKey = yKey;
		setActiveNumVariable(yKey);
	}
	
	protected int groupIndex(int itemIndex) {
		return groupVariable.getItemCategory(itemIndex);
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(catKey)) {
			showResponse = false;
			repaint();
		}
		else if (key.equals(yKey)) {
			showResponse = true;
			repaint();
		}
	}
	
	public void paintView(Graphics g) {
		if (showResponse)
			super.paintView(g);
	}
}
	
