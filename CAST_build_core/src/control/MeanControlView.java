package control;

import java.awt.*;

import dataView.*;
import axis.*;


public class MeanControlView extends ControlView {
//	static public final String MEAN_CONTROL_PLOT = "MeanControlPlot";
	
	static final public int POOLED_S_LIMITS = 0;
	static final public int R_BAR_LIMITS = 1;
	
	static final private double kA2[] = {Double.NaN, Double.NaN, 1.880, 1.023, 0.729, 0.577, 0.483, 0.419, 0.373, 0.337, 0.308};
	
	private String kTrainingString, kProductionString;
	
	protected DataSet rawData;
	protected boolean showSummaries = true;		//		so that axis is changed on initialisation
	protected int noOfTrainingSamples;
	
	private int limitType = POOLED_S_LIMITS;
	
	public MeanControlView(DataSet meanData, XApplet applet, TimeAxis timeAxis, ControlLimitAxis numAxis,
										int problemFlags, DataSet rawData, int noOfTrainingSamples) {
		super(meanData, applet, timeAxis, numAxis, problemFlags);
		this.rawData = rawData;
		this.noOfTrainingSamples = noOfTrainingSamples;
			
		kTrainingString = applet.translate("Training");
		kProductionString = applet.translate("Production");
		
		setShowSummaries(false);
	}
	
	public void setLimitType(int limitType) {
		this.limitType = limitType;
	}
	
	public void setShowSummaries(boolean showSummaries) {
		if (showSummaries != this.showSummaries) {
			this.showSummaries = showSummaries;
			NumVariable rawVariable = rawData.getNumVariable();
			GroupMeanVariable meanVariable = (GroupMeanVariable)getNumVariable();
			int noInGroup = meanVariable.getNoInGroup();
			
			ValueEnumeration e = rawVariable.values();
			double total = 0.0;
			double rss = 0.0;
			double rangeTotal = 0.0;
			for (int group=0 ; group<noOfTrainingSamples ; group++) {
				double sx = 0.0;
				double sxx = 0.0;
				double min = Double.NaN;
				double max = Double.NaN;
				for (int i=0 ; i<noInGroup ; i++) {
					double nextVal = e.nextDouble();
					if (Double.isNaN(min))
						min = max = nextVal;
					else {
						min = Math.min(min, nextVal);
						max = Math.max(max, nextVal);
					}
					sx += nextVal;
					sxx += nextVal * nextVal;
				}
				total += sx;
				rss += (sxx - sx * sx / noInGroup);
				rangeTotal += (max - min);
			}
			double centre = total / (noInGroup * noOfTrainingSamples);
			double var = rss / (noOfTrainingSamples * (noInGroup - 1));
			double rBar = rangeTotal / noOfTrainingSamples;
			
			double plusMinus;
			if (showSummaries)
				if (limitType == POOLED_S_LIMITS)
					plusMinus = 3.0 * Math.sqrt(var / noInGroup);
				else
					plusMinus = rBar * kA2[noInGroup];
			else
					plusMinus = 3.0 * Math.sqrt(var);
			
			int decimals = meanVariable.getMaxDecimals();
			getNumAxis().setControlLimits(new NumValue(centre - plusMinus, decimals),
													new NumValue(centre, decimals),
													new NumValue(centre + plusMinus, decimals));
			repaint();
		}
	}
	
	protected void drawBackground(Graphics g) {
		int trainingEndPos = getScreenBefore(noOfTrainingSamples, null).x;
		int trainingStartPos = getScreenBefore(0, null).x;
		if (getCurrentFrame() > noOfTrainingSamples) {
			g.setColor(kShadeColor);
			g.fillRect(trainingStartPos, 0, (trainingEndPos - trainingStartPos), getSize().height);
		}
		
		g.setColor(kCentreLineColor);
		try {
			int centre = getScreenPoint(0, getNumAxis().getCentre(), null).y;
			g.drawLine(0, centre, getSize().width - 1, centre);
		} catch (Exception e) {
		}
		try {
			int lowLimit = getScreenPoint(0, getNumAxis().getLowerLimit(), null).y;
			g.drawLine(0, lowLimit, getSize().width - 1, lowLimit);
		} catch (Exception e) {
		}
		try {
			int highLimit = getScreenPoint(0, getNumAxis().getUpperLimit(), null).y;
			g.drawLine(0, highLimit, getSize().width - 1, highLimit);
		} catch (Exception e) {
		}
		
		g.setColor(Color.red);
		g.drawLine(trainingEndPos, 0, trainingEndPos, getSize().height  - 1);
		FontMetrics fm = g.getFontMetrics();
		int descent = fm.getDescent();
		int width = fm.stringWidth(kTrainingString);
		int textStart = Math.max(trainingStartPos, (trainingEndPos + trainingStartPos - width) / 2);
		g.drawString(kTrainingString, textStart, getSize().height - descent - 3);
		
		int productionEndPos = getScreenBefore(((GroupMeanVariable)getNumVariable()).noOfValues(), null).x;
		width = fm.stringWidth(kProductionString);
		textStart = Math.min(productionEndPos - width,
														(trainingEndPos + productionEndPos - width) / 2);
		g.drawString(kProductionString, textStart, getSize().height - descent - 3);
	}
	
	protected Point getRawScreenPoint(int index, double theVal, int noInGroup, Point thePoint) {
		try {
			int vertPos = getNumAxis().numValToPosition(theVal);
			int horizPos = getTimeAxis().timePosition(index / noInGroup);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected void drawRawValues(Graphics g, NumVariable rawVariable, int noInGroup) {
		Point thePoint = null;
		if (showSummaries) {
			ValueEnumeration e = rawVariable.values();
			g.setColor(Color.gray);
			int index = 0;
			while (e.hasMoreValues()) {
				if (index >= getCurrentFrame() * noInGroup)
					break;
				double nextVal = e.nextDouble();
				thePoint = getRawScreenPoint(index, nextVal, noInGroup, thePoint);
				if (thePoint != null)
					drawCross(g, thePoint);
				index ++;
			}
		}
		else {
			g.setColor(kCrossColor);
			ControlledEnumeration e = new ControlledEnumeration(rawVariable, getNumAxis(), getProblemFlags(), getApplet());
			int index = 0;
			while (e.hasMoreValues()) {
				if (index >= getCurrentFrame() * noInGroup)
					break;
				double nextVal = e.nextDouble();
				thePoint = getRawScreenPoint(index, nextVal, noInGroup, thePoint);
				if (thePoint != null) {
					if (e.getControlProblem() != null) {
						g.setColor(kBlobColor);
						drawBlob(g, thePoint);
						g.setColor(kCrossColor);
					}
					else
						drawCross(g, thePoint);
				}
				index ++;
			}
		}
	}
	
	public void paintView(Graphics g) {
		GroupMeanVariable meanVariable = (GroupMeanVariable)getNumVariable();
		
		drawBackground(g);
		
		drawRawValues(g, rawData.getNumVariable(), meanVariable.getNoInGroup());
		
		if (showSummaries) {
			joinValues(g, meanVariable);
			drawValues(g, meanVariable);
		}
	}
}
	
