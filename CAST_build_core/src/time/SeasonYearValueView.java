package time;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.*;


public class SeasonYearValueView extends ValueView {
	
	private SeasonTimeAxis axis;
	private String labelString;
	
	public SeasonYearValueView(DataSet data, XApplet applet, SeasonTimeAxis axis, String labelString) {
		super(data, applet);
		this.axis = axis;
		this.labelString = labelString;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		int maxValWidth = 0;
		FontMetrics fm = g.getFontMetrics();
		for (int i=0 ; i<getData().getSelection().getNoOfFlags() ; i++)
			maxValWidth = Math.max(maxValWidth, fm.stringWidth(axis.getSeasonString(i)));
		
		return maxValWidth;
	}
	
	protected String getValueString() {
		int selectedIndex = getData().getSelection().findSingleSetFlag();
		if (selectedIndex < 0)
			return null;
		else
			return axis.getSeasonString(selectedIndex);
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
