package exper;

import java.awt.*;

import dataView.*;
import distn.TTable;


public class TreatmentEffectView extends DataView {
//	static final public String TREATMENT_EFFECT_VIEW = "treatmentEffect";
	
	static final public boolean SHOW_INTERVAL = true;
	static final public boolean HIDE_INTERVAL = false;
	
	static final protected int kBoxTopBottom = 4;
	static final protected int kBoxLeftRight = 6;
	static final private int kBoxVertGap = 6;
	static final private int kBoxMinHorizGap = 30;
	static final private int kHeadingMinHorizGap = 20;
	static final private int kTextBoxHorizGap = 10;
	static final private int kTopBottomSlop = 3;
	static final private int kLeftRightSlop = 3;
	static final private int kMinEffectOffset = 40;
	
	static final private String kCountString = "No of plots";
	static final private String kMeanString = "Mean ";
	static final private String kEffectString = " effect";
	static final private String kPlusMinusString = "\u00B1";
	
	protected String treatmentKey, responseKey;
	protected NumValue maxMean;
	private boolean showInterval;
	
	private boolean initialised = false;
	protected boolean showResponse = false;
	protected int ascent, descent, boxHeight;
	private int countStringWidth, meanStringWidth, countBoxLeft, countBoxWidth,
							effectStringWidth, effectLineWidth;
	protected int meanBoxLeft, statisticBoxWidth, plusMinusStringWidth, maxLeft;
	
	public TreatmentEffectView(DataSet theData, XApplet applet, String treatmentKey, String responseKey,
						NumValue maxMean, boolean showInterval) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.treatmentKey = treatmentKey;
		this.responseKey = responseKey;
		this.maxMean = maxMean;
		this.showInterval = showInterval;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			boxHeight = ascent + 2 * kBoxTopBottom;
			
			CatVariable treat = (CatVariable)getVariable(treatmentKey);
			NumVariable resp = (NumVariable)getVariable(responseKey);
			
			int treatWidth = 0;
			for (int i=0 ; i<treat.noOfCategories() ; i++)
				treatWidth = Math.max(treatWidth, treat.getLabel(i).stringWidth(g));
			countStringWidth = fm.stringWidth(kCountString);
			meanStringWidth = fm.stringWidth(kMeanString + resp.name);
			
			countBoxLeft = kLeftRightSlop + treatWidth + kTextBoxHorizGap;
			countBoxWidth = fm.stringWidth("99") + 2 * kBoxLeftRight;
			
			statisticBoxWidth = maxMean.stringWidth(g) + 2 * kBoxLeftRight;
			int betweenBoxCenters = Math.max((countBoxWidth + statisticBoxWidth) / 2 + kBoxMinHorizGap,
										(countStringWidth + meanStringWidth) / 2 + kHeadingMinHorizGap);
			meanBoxLeft = countBoxLeft + (countBoxWidth - statisticBoxWidth) / 2 + betweenBoxCenters;
			maxLeft = meanBoxLeft + Math.max(statisticBoxWidth, (statisticBoxWidth + meanStringWidth) / 2);
			
			plusMinusStringWidth = fm.stringWidth(kPlusMinusString);
			
			if (treat.noOfCategories() == 2) {
				effectStringWidth = fm.stringWidth(treat.getLabel(0).toString() + kEffectString);
				effectLineWidth = effectStringWidth + kTextBoxHorizGap + statisticBoxWidth;
				
				if (showInterval)
					effectLineWidth += 2 * kTextBoxHorizGap + statisticBoxWidth + plusMinusStringWidth;
				
				maxLeft = Math.max(maxLeft, kMinEffectOffset + effectLineWidth) + kLeftRightSlop;
			}
			
			initialised = true;
			return true;
		}
		return false;
	}
	
	protected double getSDEstimate(int[] count, double[] sum, double[] sum2) {
		double sxx = 0.0;
		int df = 0;
		for (int i=0 ; i<sum.length ; i++)
			if (count[i] > 1) {
				sxx += (sum2[i] - sum[i] * sum[i] / count[i]);
				df += (count[i] - 1);
			}
		return Math.sqrt(sxx / df);
	}
	
	protected int getSDDF(int[] count) {
		int df = 0;
		for (int i=0 ; i<count.length ; i++)
			if (count[i] > 1)
				df += (count[i] - 1);
		return df;
	}
	
	protected void drawTreatmentRow(Graphics g, int baseline, int index,
										CatVariable treat, NumVariable resp, int count, double sum,
										double sd, int df, Value rowLabel) {
		rowLabel.drawLeft(g, countBoxLeft - kTextBoxHorizGap, baseline);
		g.drawRect(countBoxLeft, baseline - ascent - kBoxTopBottom, countBoxWidth - 1, boxHeight - 1);
		g.setColor(Color.white);
		g.fillRect(countBoxLeft + 1, baseline - ascent - kBoxTopBottom + 1, countBoxWidth - 2, boxHeight - 2);
		g.setColor(Color.black);
		NumValue countVal = new NumValue(count, 0);
		countVal.drawLeft(g, countBoxLeft + countBoxWidth - kBoxLeftRight, baseline);
		
		g.drawRect(meanBoxLeft, baseline - ascent - kBoxTopBottom, statisticBoxWidth - 1, boxHeight - 1);
		g.setColor(Color.white);
		g.fillRect(meanBoxLeft + 1, baseline - ascent - kBoxTopBottom + 1, statisticBoxWidth - 2, boxHeight - 2);
		g.setColor(Color.black);
		if (showResponse && count > 0) {
			NumValue meanVal = new NumValue(sum / count, maxMean.decimals);
			meanVal.drawLeft(g, meanBoxLeft + statisticBoxWidth - kBoxLeftRight, baseline);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		CatVariable treat = (CatVariable)getVariable(treatmentKey);
		int noOfTreats = treat.noOfCategories();
		NumVariable resp = (NumVariable)getVariable(responseKey);
		
		int baseline = kTopBottomSlop + ascent;
		
		int countTextLeft = countBoxLeft + (countBoxWidth - countStringWidth) / 2;
		g.drawString(kCountString, countTextLeft, baseline);
		int meanTextLeft = meanBoxLeft + (statisticBoxWidth - meanStringWidth) / 2;
		g.drawString(kMeanString + resp.name, meanTextLeft, baseline);
		
		baseline += descent + kBoxVertGap + kBoxTopBottom + ascent;
		Value rowLabel[] = new LabelValue[noOfTreats];
		for (int i=0 ; i<noOfTreats ; i++)
			rowLabel[i] = treat.getLabel(i);
		
		ValueEnumeration re = resp.values();
		ValueEnumeration te = treat.values();
		int count[] = new int[noOfTreats];
		double sum[] = new double[noOfTreats];
		double sum2[] = new double[noOfTreats];
		while (te.hasMoreValues()) {
			int cat = treat.labelIndex(te.nextValue());
			count[cat] ++;
			if (showResponse) {
				double nextVal = re.nextDouble();
				sum[cat] += nextVal;
				sum2[cat] += nextVal * nextVal;
			}
		}
		
		double sd = showResponse ? getSDEstimate(count, sum, sum2) : 0.0;
		int df = getSDDF(count);
		
		for (int i=0 ; i<noOfTreats ; i++) {
			drawTreatmentRow(g, baseline, i, treat, resp, count[i], sum[i], sd, df, rowLabel[i]);
			
			baseline += (boxHeight + kBoxVertGap);
		}
		
		if (noOfTreats == 2) {
			int left = maxLeft - effectLineWidth - kLeftRightSlop;
			g.drawString(rowLabel[0].toString() + kEffectString, left, baseline);
			left += (effectStringWidth + kTextBoxHorizGap);
			g.drawRect(left, baseline - ascent - kBoxTopBottom, statisticBoxWidth - 1, boxHeight - 1);
			g.setColor(Color.white);
			g.fillRect(left + 1, baseline - ascent - kBoxTopBottom + 1, statisticBoxWidth - 2, boxHeight - 2);
			g.setColor(Color.black);
			if (showResponse && count[0] > 0 && count[1] > 0) {
				NumValue diffVal = new NumValue(sum[0] / count[0] - sum[1] / count[1], maxMean.decimals);
				diffVal.drawLeft(g, left + statisticBoxWidth - kBoxLeftRight, baseline);
			}
			
			if (showInterval) {
				left += statisticBoxWidth + kTextBoxHorizGap;
				g.drawString(kPlusMinusString, left, baseline);
				left += plusMinusStringWidth + kTextBoxHorizGap;
				
				g.drawRect(left, baseline - ascent - kBoxTopBottom, statisticBoxWidth - 1, boxHeight - 1);
				g.setColor(Color.white);
				g.fillRect(left + 1, baseline - ascent - kBoxTopBottom + 1, statisticBoxWidth - 2, boxHeight - 2);
				g.setColor(Color.black);
				if (showResponse && count[0] > 0 && count[1] > 0) {
					double sDiff = Math.sqrt(sd * (1.0 / count[0] + 1.0 / count[1]));
					double tVal = TTable.quantile(0.975, df);
					NumValue sdVal = new NumValue(sDiff * tVal, maxMean.decimals);
					sdVal.drawLeft(g, left + statisticBoxWidth - kBoxLeftRight, baseline);
				}
			}
		}
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		CatVariable treat = (CatVariable)getVariable(treatmentKey);
		int noOfTreats = treat.noOfCategories();
		int height = ascent + descent + noOfTreats * (boxHeight + kBoxVertGap) + 2 * kTopBottomSlop;
		if (noOfTreats == 2)
			height += boxHeight + kBoxVertGap;
		
		return new Dimension(maxLeft, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(treatmentKey)) {
			showResponse = false;
			repaint();
		}
		else if (key.equals(responseKey)) {
			showResponse = true;
			repaint();
		}
	}
	
	protected void doChangeSelection(Graphics g) {
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
