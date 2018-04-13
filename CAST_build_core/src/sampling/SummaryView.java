package sampling;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import imageGroups.*;


public class SummaryView extends ValueView {
//	static public final String MEAN_VIEW = "meanValue";
	
	static public final boolean POPULATION = true;
	static public final boolean SAMPLE = false;
	
	static public final int MEAN = 0;
	static public final int SD = 1;
	static public final int PROPN = 2;
	static public final int COUNT = 3;
	
	static final private String kMaxCountString = "99999";
	
	private String variableKey, freqKey;
	private int decimals;
	private boolean popNotSamp;
	private int statistic;
	
	static public double evaluateStatistic(Variable yVar, FreqVariable fVar,
											Flags flags, boolean popNotSamp, int statistic) {
		if (fVar == null && popNotSamp)
			return Double.NaN;
		
		if (statistic == COUNT)
			return yVar.noOfValues();
		else if (statistic == PROPN) {
			int totalCount = 0;
			int successCount = 0;
			if (yVar instanceof CatVariable) {
				totalCount = yVar.noOfValues();
				successCount = ((CatVariable)yVar).getCounts()[0];
			}
			else {
				FlagEnumeration flagE = flags.getEnumeration();
				if (fVar == null || popNotSamp) {
					while (flagE.hasMoreFlags())
						if (flagE.nextFlag())
							successCount ++;
					totalCount = yVar.noOfValues();
				}
				else {
					ValueEnumeration fe = fVar.values();
					while (fe.hasMoreValues()) {
						int f = ((FreqValue)fe.nextValue()).intValue;
						boolean nextSel = flagE.nextFlag();
						if (f > 0) {
							totalCount += f;
							if (nextSel)
								successCount += f;
						}
					}
				}
			}
			return ((double)successCount) / totalCount;
		}
		
		double sum = 0.0;
		double sum2 = 0.0;
		int count = 0;
		if (fVar == null || popNotSamp) {
			ValueEnumeration ye = yVar.values();
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				sum += y;
				sum2 += y * y;
			}
			count = yVar.noOfValues();
		}
		else {
			ValueEnumeration ye = yVar.values();
			ValueEnumeration fe = fVar.values();
			while (ye.hasMoreValues() && fe.hasMoreValues()) {
				int f = ((FreqValue)fe.nextValue()).intValue;
				double y = ye.nextDouble();
				if (f > 0) {
					sum += y * f;
					sum2 += y * y * f;
					count += f;
				}
			}
		}
		
		if (count == 0 || (count == 1 && statistic == SD))
			return Double.NaN;
		return (statistic == MEAN) ? sum / count : Math.sqrt((sum2 - sum * sum / count) / (count - 1));
	}
	
	public SummaryView(DataSet theData, XApplet applet, String variableKey, String freqKey,
																	int statistic, int decimals, boolean popNotSamp) {
		super(theData, applet);
		this.variableKey = variableKey;
		this.freqKey = freqKey;
		this.decimals = decimals;
		this.popNotSamp = popNotSamp;
		this.statistic = statistic;
	}
	
	public void setPopNotSamp(boolean popNotSamp) {
		this.popNotSamp = popNotSamp;
		repaint();
	}
	
	public void setDecimals(int decimals) {
		this.decimals = decimals;
		resetSize();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return MeanSDImages.kParamWidth;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		CoreVariable y = getVariable(variableKey);
		if (y instanceof DistnVariable)
			return ((DistnVariable)y).getMean().stringWidth(g, decimals);
		else if (statistic == COUNT)
			return g.getFontMetrics().stringWidth(kMaxCountString);
		else if (statistic == PROPN)
			return new NumValue(1.0, decimals).stringWidth(g);
		else
			return ((NumVariable)y).getMaxAlignedWidth(g, decimals);
	}
	
	protected int getLabelAscent(Graphics g) {
		return MeanSDImages.kParamAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return MeanSDImages.kParamDescent;
	}
	
	public String getValueString() {
		CoreVariable y = getVariable(variableKey);
		if (y instanceof DistnVariable) {
			NumValue value = (statistic == MEAN) ? ((DistnVariable)y).getMean() : ((DistnVariable)y).getSD();
			return value.toString(decimals);
		}
		
		if (freqKey == null && popNotSamp)
			return "???";
		
		FreqVariable fVar = (freqKey == null) ? null :(FreqVariable)getVariable(freqKey);
		Variable yVar = (Variable)y;
		double value = evaluateStatistic(yVar, fVar, getSelection(), popNotSamp, statistic);
		if (Double.isNaN(value))
			return "";
		else if (statistic == COUNT)
			return (new NumValue(value, 0)).toString();
		else
			return (new NumValue(value, decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		Image label = null;
		switch (statistic) {
			case SD:
				label = popNotSamp ? MeanSDImages.popnSD : MeanSDImages.sampSD;
				break;
			case MEAN:
				label = popNotSamp ? MeanSDImages.popnMean : MeanSDImages.sampMean;
				break;
			case PROPN:
				label = popNotSamp ? MeanSDImages.popnProp : MeanSDImages.sampProp;
				break;
			case COUNT:
				label = MeanSDImages.sampN;
		}
		g.drawImage(label, startHoriz, baseLine - MeanSDImages.kParamAscent, this);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
