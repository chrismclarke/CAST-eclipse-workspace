package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class ScatterGroupsView extends ScatterView {
	static final private Color kOverallLineColor = new Color(0x999999);
	
	private boolean colouredCats = true;
	private String overallModelKey = null;
	private String[] catModelKey = null;
	
	public ScatterGroupsView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
	}
	
	public void setColouredCats(boolean colouredCats) {
		this.colouredCats = colouredCats;
	}
	
	public void setModels(String overallModelKey, String[] catModelKey) {
		this.overallModelKey = overallModelKey;
		this.catModelKey = catModelKey;
	}
	
	protected int groupIndex(int itemIndex) {
		return colouredCats ? getCatVariable().getItemCategory(itemIndex) : 0;
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		if (colouredCats && catModelKey != null)
			for (int i=0 ; i<catModelKey.length ; i++) {
				LinearModel model = (LinearModel)getVariable(catModelKey[i]);
				g.setColor(getCrossColor(i));
				model.drawMean(g, this, axis, yAxis);
			}
		else if (!colouredCats && overallModelKey != null) {
			LinearModel model = (LinearModel)getVariable(overallModelKey);
			g.setColor(kOverallLineColor);
			model.drawMean(g, this, axis, yAxis);
		}
		
		g.setColor(getForeground());
	}
}
	
