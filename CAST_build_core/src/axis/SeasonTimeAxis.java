package axis;

import java.util.*;
import java.awt.*;
import dataView.*;


public class SeasonTimeAxis extends TimeAxis {
	private int noOfSeasons, firstValSeason, cycleOneIndex, seasonPrintCycle;
	private String cyclePrefix;
	private Value seasonName[];
	private int yearLabelStep = 1;
	
	public SeasonTimeAxis(XApplet applet, int noOfVals) {
		super(applet, noOfVals);
	}
	
	public void setTimeScale(int noOfSeasons, Value seasonName[], int firstValSeason,
										int seasonPrintCycle, int cycleOneIndex, String cyclePrefix) {
		this.noOfSeasons = noOfSeasons;
		this.firstValSeason = firstValSeason;
		this.cycleOneIndex = cycleOneIndex;
		this.seasonPrintCycle = seasonPrintCycle;
		this.cyclePrefix = cyclePrefix;
		this.seasonName = seasonName;
	}
	
	public int getNoOfSeasons() {
		return noOfSeasons;
	}
	
	public int getFirstValSeason() {
		return firstValSeason;
	}
	
	public Value[] getSeasonNames() {
		return seasonName;
	}
	
	public String getSeasonString(int index) {
		int season = (index + firstValSeason) % noOfSeasons;
		int cycle = (index + firstValSeason) / noOfSeasons;
		
		return seasonName[season].toString() + " " + (cycleOneIndex + cycle);
	}
	
	public void setTimeScale(String seasonNameInfo, String labelInfo) {
		try {
			StringTokenizer seasons = new StringTokenizer(seasonNameInfo);
			noOfSeasons = Integer.parseInt(seasons.nextToken());
			seasonName = new LabelValue[noOfSeasons];
			for (int i=0 ; i<noOfSeasons ; i++)
				seasonName[i] = new LabelValue(seasons.nextToken());
			
			StringTokenizer labels = new StringTokenizer(labelInfo);
			firstValSeason = Integer.parseInt(labels.nextToken());
			if (firstValSeason < 0 || firstValSeason >= noOfSeasons)
				throw new Exception("");
			
			seasonPrintCycle = Integer.parseInt(labels.nextToken());
			if (seasonPrintCycle < 0 || seasonPrintCycle > noOfSeasons)
				throw new Exception("");
			
			cycleOneIndex = Integer.parseInt(labels.nextToken());
			if (labels.hasMoreTokens()) {
				cyclePrefix = labels.nextToken();
				try {
					int tempYearLabelStep = Integer.parseInt(cyclePrefix);
					cyclePrefix = null;
					yearLabelStep = tempYearLabelStep;
				} catch (NumberFormatException e) {
				}
			}
			else
				cyclePrefix = null;
		} catch (Exception e) {
			System.err.println("Badly formatted time label specification: " + labelInfo);
			seasonPrintCycle = 0;
			cycleOneIndex = 0;
		}
		repaint();
	}
	
	public void findAxisWidth() {					//		must be called AFTER findLengthInfo()
		super.findAxisWidth();
		if (seasonPrintCycle > 0)
			axisWidth += (kLongTickLength - kShortTickLength) + fontHeight;	//	for season names
		axisWidth += fontHeight;					//		for cycle number
	}
	
	private int lastCycleIndex() {
		return cycleOneIndex + (noOfVals + firstValSeason - 1) / noOfSeasons;
	}
	
	private String getCycleLabel(int cycleIndex) {
		if (cyclePrefix == null)
			return Integer.toString(cycleIndex);
		else
			return cyclePrefix + Integer.toString(cycleIndex);
	}
	
	private int cycleLabelPos(int cycleIndex) throws AxisException {
		int cycleStartIndex = (cycleIndex - cycleOneIndex) * noOfSeasons - firstValSeason;
		if ((noOfSeasons & 1) == 0)		//		even no of seasons
			return timePositionBefore(cycleStartIndex + noOfSeasons / 2);
		else									//		odd no of seasons
			return timePosition(cycleStartIndex + noOfSeasons / 2);
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		Graphics g = getGraphics();
		setFontInfo(g);
		
		lowBorder = minLowBorder;
		highBorder = minHighBorder;
		
		int firstCycleIndex = cycleOneIndex;
		int lastCycleIndex = lastCycleIndex();
		LabelValue firstCycleLabel = new LabelValue(getCycleLabel(firstCycleIndex));
		LabelValue lastCycleLabel = new LabelValue(getCycleLabel(lastCycleIndex));
		int firstLabelWidth = firstCycleLabel.stringWidth(g);
		int lastLabelWidth = lastCycleLabel.stringWidth(g);
		
		boolean needsCheck = true;
		while (needsCheck) {
			axisLength = availableLength - lowBorder - highBorder;
			if (axisLength < kMinHorizAxisLength)
				break;
			try {
				lowBorderUsed = Math.max(0, firstLabelWidth / 2 - cycleLabelPos(firstCycleIndex));
			} catch (AxisException e) {
				lowBorderUsed = 0;
			}
			try {
				highBorderUsed = Math.max(0, cycleLabelPos(lastCycleIndex) + lastLabelWidth / 2 - axisLength);
			} catch (AxisException e) {
				highBorderUsed = 0;
			}
			
			needsCheck = false;
			if (lowBorderUsed + kLeftRightGap > lowBorder) {
				lowBorder = lowBorderUsed + kLeftRightGap;
				needsCheck = true;
			}
			if (highBorderUsed + kLeftRightGap > highBorder) {
				highBorder = highBorderUsed + kLeftRightGap;
				needsCheck = true;
			}
		}
	}
	
	protected void drawShortTick(Graphics g, int index) {
		if (yearLabelStep == 1)		//	don't draw month ticks
			super.drawShortTick(g, index);
	}
	
	public void corePaint(Graphics g) {
		Color oldColor = g.getColor();
//		g.setColor(Color.lightGray);
		g.setColor(Color.red);
		
		int lineBottom = ((seasonPrintCycle > 0) ? (kLongTickLength + fontHeight)
												: kShortTickLength) + fontHeight + 1;
		int redTickBottom = (yearLabelStep > 1)	? kShortTickLength + 3 : lineBottom;
		if (yearLabelStep > 1)
			lineBottom += 3;					//		short ticks between years and no month ticks
		
		for (int i=0 ; i<lastCycleIndex()-cycleOneIndex ; i++)
			try {
				int itemIndex = (noOfSeasons - firstValSeason) + i * noOfSeasons;
				int horiz = lowBorderUsed + timePositionBefore(itemIndex);
				g.drawLine(horiz, 0, horiz, redTickBottom);
			}
			catch (AxisException e) {
			}
		
		g.setColor(oldColor);
		super.corePaint(g);
		
		if (seasonPrintCycle > 0)
			for (int index=0 ; index<noOfVals ; index++) {
				int season = (index + firstValSeason) % noOfSeasons;
				if (season % seasonPrintCycle == 0)
					drawLabelledTick(g, index, seasonName[season]);
			}
		
		int cycleBaseline = lineBottom - descent;
		for (int cycleIndex=cycleOneIndex ; cycleIndex<=lastCycleIndex() ; cycleIndex+=yearLabelStep) {
			try {
				int horiz = lowBorderUsed + cycleLabelPos(cycleIndex);
				LabelValue cycleLabel = new LabelValue(getCycleLabel(cycleIndex));
				cycleLabel.drawCentred(g, horiz, cycleBaseline);
			} catch (AxisException e) {
			}
		}
	}
}