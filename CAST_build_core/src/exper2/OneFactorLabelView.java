package exper2;

import java.awt.*;

import dataView.*;
import axis.*;


public class OneFactorLabelView extends CoreOneFactorView {
	static final private Color kParamColor = new Color(0xFF6600);
	
	private String levelParamName[];
	
	public OneFactorLabelView(DataSet theData, XApplet applet, NumCatAxis xAxis,
												NumCatAxis yAxis, String xKey, String yKey, String modelKey,
												String[] levelParamName) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, modelKey);
		this.levelParamName = levelParamName;
	}
	
	protected void drawOneMean(int levelIndex, Point p0, Point p1, Graphics g) {
		super.drawOneMean(levelIndex, p0, p1, g);
		
		if (levelParamName != null) {
			FontMetrics fm = g.getFontMetrics();
			g.setColor(kParamColor);
			
			int baseline = p0.y + (fm.getAscent() - fm.getDescent()) / 2;
			int startX = p0.x - fm.stringWidth(levelParamName[levelIndex]);
			g.drawString(levelParamName[levelIndex], startX, baseline);
		}
	}
}
	
