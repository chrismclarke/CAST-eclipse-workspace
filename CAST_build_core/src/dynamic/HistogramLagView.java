package dynamic;

import java.awt.*;

import dataView.*;
import axis.*;


public class HistogramLagView extends HistogramView {
//	static public final String HISTOGRAM_LAG_VIEW = "histogramLagView";
	
	static final private Color kDeathsColor = new Color(0xFF9999);
	static final private Color kBirthsColor = Color.blue;
	
	static final private int kKeySquareSize = 20;
	
	private LabelValue[] keyText;
	
	public HistogramLagView(DataSet theData, XApplet applet,
													String yKey, HorizAxis xAxis, VertAxis freqAxis, double unitClassWidth,
													double[] classBoundary, boolean[] groupWithNext, LabelValue[] keyText) {
		super(theData, applet, yKey, xAxis, freqAxis, unitClassWidth, classBoundary, groupWithNext);
		this.keyText = keyText;
		setFont(applet.getStandardBoldFont());
	}
	
	protected void drawOneBar(Graphics g, NumVariable yVar, int index, boolean needsScaling,
																															double scaling, Point p0, Point p1) {
		NumSeriesVariable ySeriesVar = (NumSeriesVariable)yVar;
		double seriesIndex = ySeriesVar.getSeriesIndex();
		if (needsScaling || seriesIndex < 1.0)
			super.drawOneBar(g, yVar, index, needsScaling, scaling, p0, p1);
		else {
			double thisY = yVar.doubleValueAt(index);
			
			if (index == 0) {
				fillOneBar(g, thisY, classBoundary[index], classBoundary[index + 1], kBirthsColor, p0, p1);
				outlineOneBar(g, thisY, classBoundary[index], classBoundary[index + 1], getForeground(), p0, p1);
			}
			else {
				NumSeriesValue lastVal = (NumSeriesValue)yVar.valueAt(index - 1);
				double lastY = lastVal.toDouble(seriesIndex - 1.0);
				
				if (needsScaling) {
					thisY *= scaling;
					lastY *= scaling;
				}
				if (lastY >= thisY) {
					fillOneBar(g, lastY, classBoundary[index], classBoundary[index + 1], kDeathsColor, p0, p1);
//					outlineOneBar(g, lastY, classBoundary[index], classBoundary[index + 1], kDarkRed, p0, p1);
					fillOneBar(g, thisY, classBoundary[index], classBoundary[index + 1], kCorrectFillColor, p0, p1);
					outlineOneBar(g, thisY, classBoundary[index], classBoundary[index + 1], getForeground(), p0, p1);
				}
				else {
					fillOneBar(g, thisY, classBoundary[index], classBoundary[index + 1], kBirthsColor, p0, p1);
					outlineOneBar(g, thisY, classBoundary[index], classBoundary[index + 1], getForeground(), p0, p1);
					fillOneBar(g, lastY, classBoundary[index], classBoundary[index + 1], kCorrectFillColor, p0, p1);
					outlineOneBar(g, lastY, classBoundary[index], classBoundary[index + 1], getForeground(), p0, p1);
				}
			}
		}
	}
	
	protected void drawKey(Graphics g) {
		drawCoreKey(g, keyText[0], kKeySquareSize, kBirthsColor, Color.black, 0);
		drawCoreKey(g, keyText[1], kKeySquareSize, kDeathsColor, Color.black, kKeySquareSize + 4);
	}
	
}
	
