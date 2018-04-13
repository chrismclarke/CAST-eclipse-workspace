package survey;

import java.awt.*;

import dataView.*;
import random.RandomHypergeometric;


public class SamplingTableView extends DataView {
	static final private int kTopBorder = 2;
	static final private int kSummaryLeftRight = 6;
	static final private int kSummaryTopBottom = 5;
	static final private int kHeadingTableGap = 3;
	static final private int kTableTopBottomBorder = 5;
	static final private int kTableLeftRightBorder = 8;
	static final private int kSuccessFailureGap = 4;
	static final private int kPopSampGap = 16;
	static final private int kTableTotalGap = 3;
	static final private int kTotalPropnGap = 40;
	static final private int kLabelTableGap = 5;
	
	static final private int kPropnDecimals = 3;
	static final private String kZeroString = "0.000";
	
	private RandomHypergeometric generator;
	private int sampleSize, populationSize;
//	private boolean sampleSelected = false;
	private int popnCounts[];
	private int sampCounts[];
	
	private String successString, failureString, propnString;
	
	private LabelValue kPopulationName, kSampleName;
	private String kProportionString;
	
	public SamplingTableView(DataSet theData, XApplet applet, int sampleSize, long randomSeed) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		kPopulationName = new LabelValue(applet.translate("Population"));
		kSampleName = new LabelValue(applet.translate("Sample"));
		kProportionString = applet.translate("Propn");
		
		this.sampleSize = sampleSize;
		CatVariable v = getCatVariable();
		popnCounts = v.getCounts();
		populationSize = popnCounts[0] + popnCounts[1];
		generator = new RandomHypergeometric(1, populationSize, popnCounts[0], sampleSize);
		generator.setSeed(randomSeed);
		
		successString = v.getLabel(0).toString();
		failureString = v.getLabel(1).toString();
		propnString = kProportionString + " (" + successString + ")";
	}
	
	public void takeSample() {
		if (sampCounts == null)
			sampCounts = new int[2];
		sampCounts[0] = generator.generateOne();
		sampCounts[1] = sampleSize - sampCounts[0];
		repaint();
	}
	
	public void setSampleSize(int n) {
		sampleSize = n;
		generator.setN(n);
		sampCounts = null;
		repaint();
	}
	
	private void drawColumn(Graphics g, int[] counts, int total, int valueRight,
									int successBaseline, int failureBaseline, int totalBaseline,
									int summaryBaseline, int propnBoxWidth, int ascent) {
		if (counts != null) {
			new NumCommaValue(counts[0], 0).drawLeft(g, valueRight, successBaseline);
			new NumCommaValue(counts[1], 0).drawLeft(g, valueRight, failureBaseline);
		}
		new NumCommaValue(total, 0).drawLeft(g, valueRight, totalBaseline);
		
		Color oldColor = g.getColor();
		g.setColor(Color.black);
		int boxLeft = valueRight - propnBoxWidth + kSummaryLeftRight;
		int boxTop = summaryBaseline - kSummaryTopBottom - ascent;
		g.drawRect(boxLeft, boxTop, propnBoxWidth - 1, 2 * kSummaryTopBottom + ascent - 1);
		g.setColor(Color.white);
		g.fillRect(boxLeft + 1, boxTop + 1, propnBoxWidth - 2,
																			2 * kSummaryTopBottom + ascent - 2); 
		g.setColor(oldColor);
		if (counts != null)
			new NumValue(counts[0] / (double)total, kPropnDecimals) .drawLeft(g, valueRight,
																										summaryBaseline);
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = getGraphics().getFontMetrics();
		int labelColWidth = Math.max(fm.stringWidth(successString),
								 Math.max(fm.stringWidth(failureString), fm.stringWidth(propnString)));
		
		int propnBoxWidth = fm.stringWidth(kZeroString) + 2 * kSummaryLeftRight;
		
		int popnLabelWidth = kPopulationName.stringWidth(g);
		int sampLabelWidth = kSampleName.stringWidth(g);
		int popnColWidth = Math.max(popnLabelWidth,
								Math.max(new NumCommaValue(populationSize, 0).stringWidth(g),propnBoxWidth));
		int sampColWidth = Math.max(sampLabelWidth,
								Math.max(new NumCommaValue(sampleSize, 0).stringWidth(g),propnBoxWidth));
		
		int tableLeft = labelColWidth + kLabelTableGap;
		int popnRight = tableLeft + kTableLeftRightBorder + popnColWidth;
		int sampRight = popnRight + kPopSampGap + sampColWidth;
		int tableRight = sampRight + kTableLeftRightBorder;
		
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int headingBaseline = kTopBorder + ascent;
		int tableTop = headingBaseline + descent + kHeadingTableGap;
		int successBaseline = tableTop + kTableTopBottomBorder + ascent;
		int failureBaseline = successBaseline + kSuccessFailureGap + ascent + descent;
		int tableBottom = failureBaseline + descent + kTableTopBottomBorder;
		int totalBaseline = tableBottom + kTableTotalGap + ascent;
		int summaryBaseline = totalBaseline + kTotalPropnGap + kSummaryTopBottom + ascent;
		
		int horizOffset = (getSize().width - tableRight) / 2;
		int vertOffset = (getSize().height - summaryBaseline - descent - kSummaryTopBottom) / 2;
		
		g.setColor(Color.white);
		g.fillRect(tableLeft + horizOffset, tableTop + vertOffset, tableRight - tableLeft,
																						tableBottom - tableTop);
		g.setColor(getForeground());
		
		g.drawString(successString, horizOffset, successBaseline + vertOffset);
		g.drawString(failureString, horizOffset, failureBaseline + vertOffset);
		g.drawString(propnString, horizOffset, summaryBaseline + vertOffset);
		
		kPopulationName.drawLeft(g, popnRight + horizOffset, headingBaseline + vertOffset);
		kSampleName.drawLeft(g, sampRight + horizOffset, headingBaseline + vertOffset);
		
		g.setColor(Color.blue);
		drawColumn(g, popnCounts, populationSize, popnRight + horizOffset,
							successBaseline + vertOffset, failureBaseline + vertOffset,
							totalBaseline + vertOffset, summaryBaseline + vertOffset,
							propnBoxWidth, ascent);
		g.setColor(Color.red);
		drawColumn(g, sampCounts, sampleSize, sampRight + horizOffset,
							successBaseline + vertOffset, failureBaseline + vertOffset,
							totalBaseline + vertOffset, summaryBaseline + vertOffset,
							propnBoxWidth, ascent);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
