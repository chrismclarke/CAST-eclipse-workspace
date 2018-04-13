package dynamic;

import java.awt.*;

import dataView.*;


public class PyramidTraceCohortView extends PyramidView {
//	static public final String PYRAMID_TRACE_COHORT_VIEW = "pyramidTraceCohort";
	
	private double baseYearIndex = 0.0;
	private boolean showCurrentYear = false;
	
	public PyramidTraceCohortView(DataSet theData, XApplet applet,
										String leftKey, String rightKey, int classWidth, int freqMax, int axisMax,
										int axisStep, LabelValue freqLabel) {
		super(theData, applet, leftKey, rightKey, classWidth, freqMax, axisMax, axisStep, freqLabel);
	}
	
	public void setBaseYearIndex(double baseYearIndex) {
		this.baseYearIndex = baseYearIndex;
	}
	
	public void setShowCurrentYear(boolean showCurrentYear) {
		this.showCurrentYear = showCurrentYear;
	}
	
	protected void drawHistogramBars(Graphics g, NumVariable freqVar, NumVariable otherVar, int axisHoriz,
												int freqAxisWidth, int bottom, int ageAxisLength, int direction, Color fillColor,
												Color fillBorderColor) {
		NumSeriesVariable freqSeriesVar = (NumSeriesVariable)freqVar;
		double oldSeriesIndex = freqSeriesVar.getSeriesIndex();
		freqSeriesVar.setSeriesIndex(baseYearIndex);
		
		double yearShift = classWidth * (oldSeriesIndex - baseYearIndex);
		int shiftedBottom = bottom - (int)Math.round(ageAxisLength * (double)yearShift / 100);
		
		super.drawHistogramBars(g, freqVar, otherVar, axisHoriz, freqAxisWidth, shiftedBottom, ageAxisLength,
															direction, fillBorderColor, fillBorderColor);
		
		freqSeriesVar.setSeriesIndex(oldSeriesIndex);
		
		if (showCurrentYear)
			super.drawHistogramBars(g, freqVar, otherVar, axisHoriz, freqAxisWidth, bottom, ageAxisLength,
															direction, fillColor, fillBorderColor);
	}
	
}
	
