package percentile;

import java.awt.*;

import dataView.*;
import valueList.*;


public class PropnRangeView extends ValueView implements PropnConstants {
//	static public final String PROPN_RANGE_VIEW = "propnRangeValue";
	
	static private final NumValue kMaxPropn = new NumValue(1.0, 3);
	static private final NumValue kMaxPercent = new NumValue(100.0, 1);
	static private final NumValue kMaxMean = new NumValue(99.0, 2);
	
	static public boolean doComparison(double y, double ref, int comparison) {
		return (comparison == LESS_THAN && y < ref) || (comparison == LESS_EQUAL && y <= ref)
					|| (comparison == GREATER_THAN && y > ref) || (comparison == GREATER_EQUAL && y >= ref);
	}
	
	static private NumValue findMaxValue(DataSet data, String yKey, int displayType) {
		switch (displayType) {
			case COUNT:
				NumVariable yVar = (NumVariable)data.getVariable(yKey);
				return new NumValue(yVar.noOfValues(), 0);
			case PROPORTION:
				return kMaxPropn;
			case PERCENTAGE:
				return kMaxPercent;
			case GEOM_MEAN:
				return kMaxMean;
			case SCALED:
			default:
				return null;
		}
	}
	
	private LabelValue label = null;
	private boolean highlightSelection = false;
	
	private DataSet refData;
	private String yKey, refKey, scaleKey;
	private int comparison, displayType;
	private NumValue maxValue;
	
	private int cumEvaluateType = PercentileInfo.STEP;
	
	public PropnRangeView(DataSet theData, XApplet applet, String yKey, String refKey,
														DataSet refData, int comparison, int displayType, NumValue maxValue) {
		super(theData, applet);
		this.yKey = yKey;
		this.refKey = refKey;
		this.refData = refData;
		this.comparison = comparison;
		this.displayType = displayType;
		this.maxValue = maxValue;
	}
	
	public PropnRangeView(DataSet theData, XApplet applet, String yKey, String refKey,
																						DataSet refData, int comparison, int displayType) {
		this(theData, applet, yKey, refKey, refData, comparison, displayType,
																							findMaxValue(theData, yKey, displayType));
	}
	
	public PropnRangeView(DataSet theData, XApplet applet, String yKey, String refKey,
															DataSet refData, int comparison, String scaleKey, NumValue maxScaled) {
		this(theData, applet, yKey, refKey, refData, comparison, SCALED, maxScaled);
		this.scaleKey = scaleKey;
	}
	
	public void setLabel(LabelValue label) {
		this.label = label;
	}
	
	public void setHighlightSelection(boolean highlightSelection) {
		this.highlightSelection = highlightSelection;
	}
	
	public void setComparison(int comparison) {
		this.comparison = comparison;
		repaint();
	}
	
	public void setCumEvaluateType(int cumEvaluateType) {
		this.cumEvaluateType = cumEvaluateType;
		repaint();
	}
	
	public void setMaxValue(NumValue maxValue) {
		this.maxValue = maxValue;
		initialised = false;
		invalidate();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return (label == null) ? 0 : label.stringWidth(g);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable referenceVar = (NumVariable)refData.getVariable(refKey);
		double ref = referenceVar.doubleValueAt(0);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		double result;
		
		if (displayType == PERCENTILE) {
			NumValue sortedData[] = yVar.getSortedData();
			result = PercentileInfo.evaluatePercentile(sortedData, ref, cumEvaluateType);
		}
		else {
			int n = yVar.noOfValues();
			ValueEnumeration ye = yVar.values();
			int count = 0;
			
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				if (doComparison(y, ref, comparison))
					count ++;
			}
			
			switch (displayType) {
				case COUNT:
					result = count;
					break;
				case PROPORTION:
					result = count / (double)n;
					break;
				case PERCENTAGE:
					result = (count * 100) / (double)n;
					break;
				case GEOM_MEAN:
					result = (double)n / count;
					break;
				case SCALED:
				default:
					NumVariable scaleVar = (NumVariable)refData.getVariable(scaleKey);
					double scale = scaleVar.doubleValueAt(0);
					result = (scale * count) / n;
			}
		}
		return new NumValue(result, maxValue.decimals).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (label != null)
			label.drawRight(g, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return highlightSelection;
	}
}
