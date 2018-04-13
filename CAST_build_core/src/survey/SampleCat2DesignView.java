package survey;

import java.awt.*;

import dataView.*;


public class SampleCat2DesignView extends SampleCatDesignView {
	
	
	public SampleCat2DesignView(DataSet theData, XApplet applet, String yKey, int noCovered,
															int sampleSize, NumValue maxSummary, long randomSeed) {
		super(theData, applet, yKey, noCovered, sampleSize, maxSummary, randomSeed);
	}

//----------------------------------------------------------------------
	
	protected Dimension getMarkSize(Graphics g) {
		g.setFont(getApplet().getTinyBoldFont());
		FontMetrics fm = g.getFontMetrics();
		int valueAscent = fm.getAscent();
		CatVariable y = (CatVariable)getVariable(yKey);
		int maxValueWidth = y.getMaxWidth(g);
		return new Dimension(maxValueWidth, valueAscent);
	}
	
	protected Color drawMark(Graphics g, Value v, int x, int y, boolean changed) {
		Color markColor = changed ? Color.red : (v == successVal) ? Color.blue : Color.black;
		g.setColor(markColor);
		v.drawCentred(g, x, y + markSize.height / 2);
		return markColor;
	}
}
