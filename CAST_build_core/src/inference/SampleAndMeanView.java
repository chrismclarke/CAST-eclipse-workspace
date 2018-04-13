package inference;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import coreSummaries.*;
import images.*;
import imageGroups.*;


public class SampleAndMeanView extends StackedPlusNormalView {
//	static public final String SAMPLE_AND_MEAN_VIEW = "sampleAndMean";
	
	static public final boolean DRAW_MEAN_VALUE = true;
	static public final boolean NO_MEAN_VALUE = false;
	
	static final private Color kMeanValueColor = new Color(0x990000);
	static final private Color kIntervalColor = new Color(0xFF9999);
	
	static final private String kXBarEqualsFile = "xEquals/sampMeanRed.png";
	static final private String kXBarFile = "ci/sampMeanRed.png";
	
	static final private int kXBarAscent = MeanSDImages.kParamAscent;
	static final private int kXBarWidth = MeanSDImages.kParamWidth;
	static final private int kValueGap = 3;
	static final private int kLineImageGap = 5;
	static final private int kTopGap = 3;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private Image xBarImage;
	
	private int meanDecimals;
	private boolean drawMeanValue;
	
	private SummaryDataSet summaryData;
	private String ciKey;
	
	public SampleAndMeanView(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey,
															int meanDecimals, boolean drawMeanValue) {
		super(theData, applet, theAxis, distnKey);
		
		this.meanDecimals = meanDecimals;
		this.drawMeanValue = drawMeanValue;
		
		xBarImage = CoreImageReader.getImage(drawMeanValue ? kXBarEqualsFile : kXBarFile);
		MediaTracker tracker = new MediaTracker(applet);
		tracker.addImage(xBarImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
			
		if (distnKey == null)
			setShowDensity(SampleAndMeanView.NO_DISTN);
	}
	
	public void setDrawMeanValue(boolean drawMeanValue) {
		this.drawMeanValue = drawMeanValue;
	}
	
	public void setConfidenceInterval(SummaryDataSet summaryData, String ciKey) {
		this.summaryData = summaryData;
		this.ciKey = ciKey;
	}
	
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		
		if (summaryData != null) {
			MeanCIVariable ciVar = (MeanCIVariable)summaryData.getVariable(ciKey);
			int selectedIndex = summaryData.getSelection().findSingleSetFlag();
			if (selectedIndex >= 0) {
				IntervalValue ci = (IntervalValue)ciVar.valueAt(selectedIndex);
				double lowVal = ci.lowValue.toDouble();
				double highVal = ci.highValue.toDouble();
				
				int lowPos = axis.numValToRawPosition(lowVal);
				int highPos = axis.numValToRawPosition(highVal);
				
				Point lowPt = translateToScreen(lowPos, 0, null);
				Point highPt = translateToScreen(highPos, 0, null);
				
				g.setColor(kIntervalColor);
				g.fillRect(lowPt.x, getSize().height / 2, highPt.x - lowPt.x, getSize().height);
			}
		}
		
		NumValue sampMean = getSampleMean();
		if (Double.isNaN(sampMean.toDouble()))
			return;
		
		int horizPos = axis.numValToRawPosition(sampMean.toDouble());
		Point pSamp = translateToScreen(horizPos, 0, null);
		
		g.setColor(kMeanValueColor);
		g.drawLine(pSamp.x, 0, pSamp.x, getSize().height);
		
		g.drawImage(xBarImage, pSamp.x + kLineImageGap, kTopGap, this);
		
		if (drawMeanValue) {
			int baseline = kTopGap + kXBarAscent;
			int valueLeft = pSamp.x + kLineImageGap + kXBarWidth + kValueGap;
			
			sampMean.drawRight(g, valueLeft, baseline);
		}
		
		g.setColor(getForeground());
	}
	
	private NumValue getSampleMean() {
		NumVariable yVar = getNumVariable();
		
		ValueEnumeration e = yVar.values();
		double sy = 0.0;
		while (e.hasMoreValues())
			sy += e.nextDouble();
		
		return new NumValue(sy / yVar.noOfValues(), meanDecimals);
	}
}