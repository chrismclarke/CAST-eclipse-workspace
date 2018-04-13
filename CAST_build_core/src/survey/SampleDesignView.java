package survey;

import java.awt.*;
import java.util.*;

import dataView.*;


abstract public class SampleDesignView extends DataView {
	static final private int NORMAL_LINK = 0;
	static final private int NOT_SAMPLED_LINK = 1;
	static final private int DROPPED_LINK = 2;
	static final private int CHANGED_LINK = 3;
	
	static final private int TARGET = 0;
	static final private int COVERED = 1;
	static final private int SAMPLED = 2;
	static final private int RESPONDED = 3;
	static final private int MEASURED = 4;
	
	static final private int kNotSampledLength = 15;
	static final private int kDroppedLength = 20;
//	static final private int kLineGap = 2;
	static final private int kMarkLineGap = 2;
	static final private int kCoverageGap = 10;
	static final private int kSummaryLeftRight = 4;
	static final private int kSummaryTopBottom = 3;
	static final private int kSummaryGap = 3;			//		below summary box (to "Target")
	static final private int kSummaryNameGap = 5;	//		above and below summary heading
	
	private LabelValue kSamplingValue;
	
	private LabelValue kTargetValue, kRecordedValue;
	
	private boolean coverageErrors;
	private int noOfTargetVals, noCovered, sampleSize;
	protected NumValue maxSummary;
	protected String yKey;
	
	private boolean initialised = false;
	private boolean sampleSelected = false;
	
	protected boolean nonResponseErrors = false;
	protected boolean instrumentErrors = false;
	
	protected Dimension markSize;
	private int summaryAscent, summaryDescent, maxSummaryWidth;
	private int tableHeight, lineGap;
	private int[] colPos;
	private int headingAscent, headingDescent, headingLeading, headingHeight;
	
	private Value[][] values = new Value[5][];
	protected Random generator;
	
	public SampleDesignView(DataSet theData, XApplet applet, String yKey, int noCovered,
																		int sampleSize, NumValue maxSummary, long randomSeed) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		this.noCovered = noCovered;
			
		kSamplingValue = new LabelValue(applet.translate("Sampling"));
		
		kTargetValue = new LabelValue(applet.translate("Target"));
		kRecordedValue = new LabelValue(applet.translate("Recorded"));
		
		Variable v = (Variable)getVariable(yKey);
		noOfTargetVals = v.noOfValues();
		coverageErrors = noOfTargetVals > noCovered;
		this.sampleSize = sampleSize;
		generator = new Random(randomSeed);
		this.maxSummary = maxSummary;
		
		values[TARGET] = new Value[noOfTargetVals];
		ValueEnumeration e = v.values();
		int index = 0;
		while (e.hasMoreValues())
			values[TARGET][index ++] = e.nextValue();
		if (coverageErrors) {
			values[COVERED] = new Value[noOfTargetVals];
			for (int i=0 ; i<noCovered ; i++)
				values[COVERED][i] = values[TARGET][i];		//	remaining values are null
		}
		else
			values[COVERED] = values[TARGET];
	}

//----------------------------------------------------------------------
	
	abstract protected Dimension getMarkSize(Graphics g);
	
	abstract protected Color drawMark(Graphics g, Value v, int x, int y, boolean changed);
	
	abstract protected void generateResponses(Value[] sample, Value[] responses);
	
	abstract protected void generateMeasured(Value[] responses, Value[] measured);
	
	abstract protected NumValue summarise(Value[] values);
	
	abstract protected LabelValue summaryName();

//----------------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		markSize = getMarkSize(g);
		
		g.setFont(getApplet().getSmallBoldFont());
		FontMetrics fm = g.getFontMetrics();
		headingAscent = fm.getAscent();
		headingDescent = fm.getDescent();
		headingLeading = fm.getLeading();
		headingHeight = 2 * (headingAscent + headingDescent + headingLeading) + headingLeading;
		
		g.setFont(getApplet().getStandardFont());
		fm = g.getFontMetrics();
		summaryAscent = fm.getAscent();
		summaryDescent = fm.getDescent();
		maxSummaryWidth = maxSummary.stringWidth(g);
		
		int minTableHeight = values[TARGET].length * markSize.height;
		if (coverageErrors)
			minTableHeight += kCoverageGap;
		int summaryHeight = 3 * kSummaryNameGap + headingAscent + headingDescent
						+ 2 * (summaryAscent + summaryDescent + kSummaryTopBottom) + kSummaryGap;
		int maxTableHeight = getSize().height - headingHeight - summaryHeight;
		
		lineGap = (maxTableHeight - minTableHeight) / values[TARGET].length;
		tableHeight = lineGap + values[TARGET].length * (markSize.height + lineGap);
		if (coverageErrors)
			tableHeight += kCoverageGap;
		
		int summaryWidth = Math.max(Math.max(maxSummaryWidth + 2 * kSummaryLeftRight,
										kTargetValue.stringWidth(g)), kRecordedValue.stringWidth(g));
		
		int noOfColumns = 2;
		if (coverageErrors)
			noOfColumns ++;
		if (nonResponseErrors)
			noOfColumns ++;
		if (instrumentErrors)
			noOfColumns ++;
		
		int colOffset = Math.max(summaryWidth, markSize.width) / 2 + 1;
		int colSpacing = (getSize().width - 2 * colOffset) / (noOfColumns - 1);
		colPos = new int[5];
		colPos[TARGET] = colOffset;
		
		if (coverageErrors)
			colOffset += colSpacing;
		colPos[COVERED] = colOffset;
		
		colOffset += colSpacing;
		colPos[SAMPLED] = colOffset;
		
		if (nonResponseErrors)
			colOffset += colSpacing;
		colPos[RESPONDED] = colOffset;
		
		if (instrumentErrors)
			colOffset += colSpacing;
		colPos[MEASURED] = colOffset;
		
		initialised = true;
		return true;
	}
	
	public void takeSample() {
		if (values[SAMPLED] == null)
			values[SAMPLED] = new Value[noOfTargetVals];
		int sampleLeft = sampleSize;
		int popnLeft = noCovered;
		for (int i=0 ; i<noCovered ; i++) {
			if (sampleLeft > 0 && sampleLeft >= popnLeft * generator.nextDouble()) {
				values[SAMPLED][i] = values[COVERED][i];
				sampleLeft --;
			}
			else
				values[SAMPLED][i] = null;
			popnLeft --;
		}
		
		if (nonResponseErrors) {
			if (values[RESPONDED] == null)
				values[RESPONDED] = new Value[noOfTargetVals];
			generateResponses(values[SAMPLED], values[RESPONDED]);
		}
		else
			values[RESPONDED] = values[SAMPLED];
		
		if (instrumentErrors) {
			if (values[MEASURED] == null)
				values[MEASURED] = new Value[noOfTargetVals];
			generateMeasured(values[RESPONDED], values[MEASURED]);
		}
		else
			values[MEASURED] = values[RESPONDED];
		
		sampleSelected = true;
		repaint();
	}
	
	private void drawLink(Graphics g, int linkType, int startX, int endX, int y,
																						Color startColor) {
		startX += kMarkLineGap;
		endX -= kMarkLineGap;
		switch (linkType) {
			case NORMAL_LINK:
				g.setColor(startColor);
				g.drawLine(startX, y, endX, y);
				break;
			case NOT_SAMPLED_LINK:
				g.setColor(Color.red);
				g.drawLine(startX, y, startX + kNotSampledLength, y);
				break;
			case DROPPED_LINK:
				g.setColor(Color.red);
				g.drawLine(startX, y, startX + kDroppedLength, y);
				g.drawLine(startX + kDroppedLength + 1, y + 1, startX + kDroppedLength + 2, y + 1);
				g.drawLine(startX + kDroppedLength + 3, y + 2, startX + kDroppedLength + 3, y + 6);
				g.drawLine(startX + kDroppedLength, y + 3, startX + kDroppedLength + 1, y + 3);
				g.drawLine(startX + kDroppedLength + 5, y + 3, startX + kDroppedLength + 6, y + 3);
				g.drawLine(startX + kDroppedLength + 1, y + 4, startX + kDroppedLength + 5, y + 4);
				g.drawLine(startX + kDroppedLength + 2, y + 5, startX + kDroppedLength + 4, y + 5);
				break;
			case CHANGED_LINK:
				g.setColor(startColor);
				int centerX = (startX + endX) / 2;
				g.drawLine(startX, y, centerX - 6, y);
				g.drawLine(centerX - 2, y, centerX, y);
				g.drawLine(centerX + 1, y - 1, centerX + 1, y - 1);
				g.drawLine(centerX + 2, y - 2, centerX + 2, y - 3);
				g.drawLine(centerX - 2, y - 4, centerX + 1, y - 4);
				g.drawLine(centerX - 3, y - 3, centerX - 3, y - 3);
				g.setColor(Color.red);
				g.drawLine(centerX - 4, y - 2, centerX - 4, y + 1);
				g.drawLine(centerX - 3, y + 2, centerX - 3, y + 2);
				g.drawLine(centerX - 2, y + 3, centerX + 1, y + 3);
				g.drawLine(centerX + 2, y + 2, centerX + 2, y + 2);
				g.drawLine(centerX + 3, y + 1, centerX + 4, y + 1);
				g.drawLine(centerX + 5, y, endX, y);
		}
	}
	
	private void drawHeading(Graphics g) {
		g.setColor(Color.black);
		g.setFont(getApplet().getSmallBoldFont());
		int midBaseline = (headingHeight + headingAscent - headingDescent) / 2;
		int topBaseline = headingLeading + headingAscent;
		int bottomBaseline = topBaseline + headingLeading + headingAscent + headingDescent;
		if (colPos[TARGET] != colPos[COVERED]) {
			int midPos = (colPos[TARGET] + colPos[COVERED]) / 2;
			(new LabelValue(getApplet().translate("Coverage"))).drawCentred(g, midPos, midBaseline);
		}
		if (colPos[COVERED] != colPos[SAMPLED]) {
			int midPos = (colPos[COVERED] + colPos[SAMPLED]) / 2;
			kSamplingValue.drawCentred(g, midPos, midBaseline);
		}
		if (colPos[SAMPLED] != colPos[RESPONDED]) {
			int midPos = (colPos[SAMPLED] + colPos[RESPONDED]) / 2;
			StringTokenizer st= new StringTokenizer(getApplet().translate("Non-*response"), "*");
			LabelValue kNonValue = new LabelValue(st.nextToken());
			LabelValue kResponseValue = new LabelValue(st.nextToken());
			kNonValue.drawCentred(g, midPos, topBaseline);
			kResponseValue.drawCentred(g, midPos, bottomBaseline);
		}
		if (colPos[RESPONDED] != colPos[MEASURED]) {
			int midPos = (colPos[RESPONDED] + colPos[MEASURED]) / 2;
		
			StringTokenizer st= new StringTokenizer(getApplet().translate("Interviewer*/instrument"), "*");
			LabelValue kInterviewerValue = new LabelValue(st.nextToken());
			LabelValue kInstrumentValue = new LabelValue(st.nextToken());
			kInterviewerValue.drawCentred(g, midPos, topBaseline);
			kInstrumentValue.drawCentred(g, midPos, bottomBaseline);
		}
	}
	
	private void drawOneSummary(Graphics g, Value[] values, int boxCenter, int boxTop,
																										int columnType) {
		int boxLeft = boxCenter - maxSummaryWidth / 2 - kSummaryLeftRight;
		g.setColor(Color.white);
		g.fillRect(boxLeft + 1, boxTop + 1, maxSummaryWidth + 2 * kSummaryLeftRight - 2,
																	summaryAscent + 2 * kSummaryTopBottom - 2);
		g.setColor(Color.black);
		g.drawRect(boxLeft, boxTop, maxSummaryWidth + 2 * kSummaryLeftRight - 1,
																	summaryAscent + 2 * kSummaryTopBottom - 1);
		if (values != null) {
			NumValue summary = summarise(values);
//			System.err.print("col " + columnType + ": ");
//			for (int i=0 ; i<values.length ; i++)
//				System.err.print((values[i] == null) ? "-" : values[i].toString());
//			System.err.println(", summary: " + ((summary == null) ? "??" : summary.toString()));
			
			if (summary != null)
				summary.drawLeft(g, boxLeft + kSummaryLeftRight + maxSummaryWidth,
																	boxTop + kSummaryTopBottom + summaryAscent);
		}
		
		if (columnType == TARGET)
			kTargetValue.drawCentred(g, boxCenter,
										boxTop + 2 * summaryAscent + 2 * kSummaryTopBottom + kSummaryGap);
		else if (columnType == MEASURED)
			kRecordedValue.drawCentred(g, boxCenter,
										boxTop + 2 *summaryAscent + 2 * kSummaryTopBottom + kSummaryGap);
	}
	
	private void drawSummaries(Graphics g) {
		g.setFont(getApplet().getSmallBoldFont());
		g.setColor(Color.black);
		int headingBaseline = headingHeight + tableHeight + kSummaryNameGap + headingAscent;
		summaryName().drawCentred(g, getSize().width / 2, headingBaseline);
		
		g.setFont(getApplet().getStandardFont());
		int boxTop = headingBaseline + headingDescent + kSummaryNameGap;
		drawOneSummary(g, values[TARGET], colPos[TARGET], boxTop, TARGET);
		for (int i=COVERED ; i<=MEASURED ; i++) {
			Value[] vals = sampleSelected ? values[i] : null;
			drawOneSummary(g, vals, colPos[i], boxTop, i);
		}
	}
	
	private void drawValueRow(Graphics g, int index, int vertCenter) {
		Color markColor = drawMark(g, values[TARGET][index], colPos[TARGET], vertCenter, false);
		if (!sampleSelected)
			return;
		if (colPos[TARGET] != colPos[COVERED]) {
			int startX = colPos[TARGET] + markSize.width / 2;
			int endX = colPos[COVERED] - markSize.width / 2;
			int linkType = (index < noCovered) ? NORMAL_LINK : DROPPED_LINK;
			drawLink(g, linkType, startX, endX, vertCenter, markColor);
			if (index < noCovered)
				markColor = drawMark(g, values[COVERED][index], colPos[COVERED], vertCenter, false);
		}
		if (index >= noCovered)
			return;
		
		if (colPos[COVERED] != colPos[SAMPLED]) {
			int startX = colPos[COVERED] + markSize.width / 2;
			int endX = colPos[SAMPLED] - markSize.width / 2;
			boolean sampled = values[SAMPLED][index] != null;
			int linkType = sampled ? NORMAL_LINK : NOT_SAMPLED_LINK;
			drawLink(g, linkType, startX, endX, vertCenter, markColor);
			if (sampled)
				markColor = drawMark(g, values[SAMPLED][index], colPos[SAMPLED], vertCenter, false);
			else
				return;
		}
		
		if (colPos[SAMPLED] != colPos[RESPONDED]) {
			int startX = colPos[SAMPLED] + markSize.width / 2;
			int endX = colPos[RESPONDED] - markSize.width / 2;
			boolean notDropped = values[RESPONDED][index] != null;
			int linkType = notDropped ? NORMAL_LINK : DROPPED_LINK;
			drawLink(g, linkType, startX, endX, vertCenter, markColor);
			if (notDropped)
				markColor = drawMark(g, values[RESPONDED][index], colPos[RESPONDED], vertCenter, false);
			else
				return;
		}
		
		if (colPos[RESPONDED] != colPos[MEASURED]) {
			int startX = colPos[RESPONDED] + markSize.width / 2;
			int endX = colPos[MEASURED] - markSize.width / 2;
			boolean changed = values[MEASURED][index] != values[RESPONDED][index];
			int linkType = changed ? CHANGED_LINK : NORMAL_LINK;
			drawLink(g, linkType, startX, endX, vertCenter, markColor);
			markColor = drawMark(g, values[MEASURED][index], colPos[MEASURED], vertCenter, changed);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		drawHeading(g);
		
		g.setColor(Color.white);
		g.fillRect(0, headingHeight, getSize().width, tableHeight);
		
		int vertCenter = headingHeight + lineGap + markSize.height / 2;
		for (int i=0 ; i<noCovered ; i++) {
			drawValueRow(g, i, vertCenter);
			vertCenter += lineGap + markSize.height;
		}
		vertCenter += kCoverageGap;
		for (int i=noCovered ; i<noOfTargetVals ; i++) {
			drawValueRow(g, i, vertCenter);
			vertCenter += lineGap + markSize.height;
		}
		drawSummaries(g);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
