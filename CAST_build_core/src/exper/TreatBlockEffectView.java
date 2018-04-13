package exper;

import java.awt.*;

import dataView.*;


public class TreatBlockEffectView extends TreatmentEffectView {
//	static final public String TREAT_BLOCK_EFFECT_VIEW = "treatBlockEffect";
	
	static final private String kPlusMinusString = "\u00B1";
	static final private int kPlusMinusGap = 3;
	
	private String blockKey;
	private int plusMinusBoxLeft;
	
	public TreatBlockEffectView(DataSet theData, XApplet applet, String treatmentKey, String responseKey,
						NumValue maxMean) {
		super(theData, applet, treatmentKey, responseKey, maxMean, SHOW_INTERVAL);
	}
	
	public void setBlockKey(String blockKey) {
		this.blockKey = blockKey;
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			plusMinusBoxLeft = meanBoxLeft + statisticBoxWidth + 2 * kPlusMinusGap + plusMinusStringWidth;
			maxLeft = Math.max(maxLeft, plusMinusBoxLeft + statisticBoxWidth);
			
			return true;
		}
		return false;
	}
	
	private boolean balancedDesign(CatVariable treat, CatVariable block, int noOfTreats, int noOfBlocks) {
		int count[][] = treat.getCounts(block);
		int targetCount = treat.noOfValues() / noOfTreats / noOfBlocks;
		for (int i=0 ; i<count.length ; i++)
			for (int j=0 ; j<count[i].length ; j++)
				if (count[i][j] != targetCount)
					return false;
		return true;
	}
	
	protected double getSDEstimate(int[] count, double[] sum, double[] sum2) {
		if (blockKey == null)
			return super.getSDEstimate(count, sum, sum2);
		else {
			CatVariable treat = (CatVariable)getVariable(treatmentKey);
			int noOfTreats = treat.noOfCategories();
			CatVariable block = (CatVariable)getVariable(blockKey);
			int noOfBlocks = block.noOfCategories();
			if (balancedDesign(treat, block, noOfTreats, noOfBlocks)) {
				NumVariable resp = (NumVariable)getVariable(responseKey);
				int noOfReps = treat.noOfValues() / (noOfTreats * noOfBlocks);
				
				double sb[] = new double[noOfBlocks];
				ValueEnumeration re = resp.values();
				ValueEnumeration be = block.values();
				while (be.hasMoreValues()) {
					int bl = block.labelIndex(be.nextValue());
					double nextVal = re.nextDouble();
					sb[bl] += nextVal;
				}
				
				double ss = 0.0;
				double s = 0.0;
				double sst = 0.0;
				for (int i=0 ; i<noOfTreats ; i++) {
					ss += sum2[i];
					s += sum[i];
					sst += sum[i] * sum[i];
				}
				double ssb = 0.0;
				for (int i=0 ; i<noOfBlocks ; i++)
					ssb += sb[i] * sb[i];
				
				ss += (s * s / (noOfTreats * noOfBlocks * noOfReps) - sst / (noOfBlocks * noOfReps)
																					- ssb / (noOfTreats * noOfReps));
				int df = treat.noOfValues() - noOfTreats - noOfBlocks + 1;
				
				return Math.sqrt(ss / df);
			}
			else
				return Double.NaN;
		}
	}
	
	protected int getSDDF(int[] count) {
		if (blockKey == null)
			return super.getSDDF(count);
		else {										//		assumes a balanced design
			CatVariable treat = (CatVariable)getVariable(treatmentKey);
			int noOfTreats = treat.noOfCategories();
			CatVariable block = (CatVariable)getVariable(blockKey);
			int noOfBlocks = block.noOfCategories();
			return treat.noOfValues() - noOfTreats - noOfBlocks + 1;
		}
	}
	
	protected void drawTreatmentRow(Graphics g, int baseline, int index,
											CatVariable treat, NumVariable resp, int count,
											double sum, double sd, int df, LabelValue rowLabel) {
		super.drawTreatmentRow(g, baseline, index, treat, resp, count, sum, sd, df, rowLabel);
		
		g.drawString(kPlusMinusString, plusMinusBoxLeft - kPlusMinusGap - plusMinusStringWidth,
																										baseline);
		
		g.drawRect(plusMinusBoxLeft, baseline - ascent - kBoxTopBottom,
																		statisticBoxWidth - 1, boxHeight - 1);
		g.setColor(Color.white);
		g.fillRect(plusMinusBoxLeft + 1, baseline - ascent - kBoxTopBottom + 1,
																		statisticBoxWidth - 2, boxHeight - 2);
		g.setColor(Color.black);
		if (showResponse && count > 0 && !Double.isNaN(sd)) {
			NumValue plusMinusVal = new NumValue(sd / Math.sqrt(count), maxMean.decimals);
			plusMinusVal.drawLeft(g, plusMinusBoxLeft + statisticBoxWidth - kBoxLeftRight,
																											baseline);
		}
	}
	
	public Dimension getMinimumSize() {
		Dimension size = super.getMinimumSize();
		size.width = maxLeft;
		
		return size;
	}
}
	
