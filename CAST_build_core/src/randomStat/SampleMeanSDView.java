package randomStat;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import distn.*;


public class SampleMeanSDView extends ValueView {
//	static public final String MEAN_SD_VIEW = "meanSDValue";
	
	static final private String kZeroString = "0.000";
	static final private int kPropnDecimals = 3;
	static final private String kMaxCountString = "99999";
	static final private String kMaxSuccessMeanString = "999.99";
	
	static public final int MEAN = 0;
	static public final int SD = 1;
	static public final int PROPN_MEAN = 2;
	static public final int PROPN_SD = 3;
	static public final int COUNT = 4;
	static public final int SUCCESS_MEAN = 5;
	static public final int SUCCESS_SD = 6;
	
	static public final int SAMPLE_DISTN = 0;
	static public final int MEAN_DISTN = 1;
	
	private String distnKey;
	private int statistic;
	private int distnType;
	
	public SampleMeanSDView(DataSet theData, XApplet applet, String distnKey, int statistic, int distnType) {
		super(theData, applet);
		this.distnKey = distnKey;
		this.statistic = statistic;
		this.distnType = distnType;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		int width;
		switch (statistic) {
			case PROPN_SD:
				width = FitEstImages.kParam2Width;
				break;
			case SUCCESS_MEAN:
				width = FitEstImages.kSuccessMeanWidth;
				break;
			case SUCCESS_SD:
				width = FitEstImages.kSuccessSDWidth;
				break;
			default:
				width = FitEstImages.kParamWidth;
		}
		return width;
	}
	
	protected int getLabelAscent(Graphics g) {
		int height;
		switch (statistic) {
			case SUCCESS_MEAN:
			case SUCCESS_SD:
				height = FitEstImages.kSuccessAscent;
				break;
			default:
				height = FitEstImages.kParamAscent;
		}
		return height;
	}
	
	protected int getLabelDescent(Graphics g) {
		int height;
		switch (statistic) {
			case SUCCESS_MEAN:
			case SUCCESS_SD:
				height = FitEstImages.kSuccessDescent;
				break;
			default:
				height = FitEstImages.kParamDescent;
		}
		return height;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		Image label = null;
		switch (statistic) {
			case SD:
				label = (distnType == SAMPLE_DISTN) ? FitEstImages.fitSD : FitEstImages.estSD;
				break;
			case MEAN:
				label = (distnType == SAMPLE_DISTN) ? FitEstImages.fitMean : FitEstImages.estMean;
				break;
			case PROPN_MEAN:
				label = (distnType == SAMPLE_DISTN) ? FitEstImages.fitPropn : FitEstImages.estPropn;
				break;
			case PROPN_SD:
				label = FitEstImages.estPropnSD;
				break;
			case COUNT:
				label = FitEstImages.n;
				break;
			case SUCCESS_MEAN:
				label = FitEstImages.successMean;
				break;
			case SUCCESS_SD:
				label = FitEstImages.successSD;
				break;
		}
		g.drawImage(label, startHoriz, baseLine - getLabelBaseline(g), this);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		if (statistic == PROPN_MEAN || statistic == PROPN_SD)
			return g.getFontMetrics().stringWidth(kZeroString);
		else if (statistic == COUNT)
			return g.getFontMetrics().stringWidth(kMaxCountString);
		else if (statistic == SUCCESS_MEAN || statistic == SUCCESS_SD)
			return g.getFontMetrics().stringWidth(kMaxSuccessMeanString);
		else {
			DistnVariable y = (DistnVariable)getVariable(distnKey);
			if (statistic == MEAN || statistic == PROPN_MEAN)
				return y.getMean().stringWidth(g);
			else
				return y.getSD().stringWidth(g);
		}
	}
	
	protected String getValueString() {
		if (distnType == SAMPLE_DISTN && statistic == PROPN_MEAN) {
			CatDistnVariable y = (CatDistnVariable)getVariable(distnKey);
			double[] p = y.getProbs();
			NumValue p0 = new NumValue(p[0], kPropnDecimals);
			return p0.toString();
		}
		else if (statistic == PROPN_MEAN || statistic == PROPN_SD) {
			BinomialDistnVariable y = (BinomialDistnVariable)getVariable(distnKey);
			double p = y.getProb();
			int n = y.getCount();
			double value = (statistic == PROPN_MEAN) ? p : Math.sqrt(p * (1.0 - p) / n);
			return (new NumValue(value, kPropnDecimals)).toString();
		}
		else if (statistic == COUNT) {
			Variable y = (Variable)getVariable(distnKey);	//		actually a NumVariable or CatVariable
			return (new NumValue(y.noOfValues(), 0)).toString();
		}
		else {
			DistnVariable y = (DistnVariable)getVariable(distnKey);
			NumValue value = (statistic == MEAN || statistic == SUCCESS_MEAN) ? y.getMean() : y.getSD();
			return value.toString();
		}
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
