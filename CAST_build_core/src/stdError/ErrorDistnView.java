package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import images.*;


public class ErrorDistnView extends JitterPlusNormalView {
	
	static public final int SAMPLE_AND_MEAN = 0;
	static public final int SUMMARY_MEANS = 1;
	static public final int SUMMARY_ERRORS = 2;
	
	static final private Color kPopDensityColor = new Color(0xEEEEFF);
	static final private Color kDensityLabelColor = new Color(0x8888BB);
	static final private Color kMeanValueColor = new Color(0xCC0000);
	
	static final private String kXBarFile = "symbols/redXBar.gif";
	static final private String kMuFile = "symbols/purpleMu.gif";
	
	static final private int kImageWidth = 11;
//	static final private int kImageHeight = 14;
	static final private int kLineImageGap = 5;
	static final private int kTopGap = 3;
	static final private int kArrowSize = 4;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private LabelValue kMeanDistn = new LabelValue("Distn of mean");
	static final private LabelValue kErrorDistn = new LabelValue("Distn of error");
	
	private LabelValue kPopulation, kError;
	
	
	private String normalKey;
	private int viewType;
	
	static private Image xBarImage, muImage;
	
	public ErrorDistnView(DataSet theData, XApplet applet, NumCatAxis theAxis, String normalKey,
									double initialJittering, int viewType) {
		super(theData, applet, theAxis, normalKey, initialJittering);
		kError = new LabelValue(applet.translate("Error"));
		kPopulation = new LabelValue(applet.translate("Population"));
		
		setDensityColor(kPopDensityColor);
		this.normalKey = normalKey;
		this.viewType = viewType;
		switch (viewType) {
			case SAMPLE_AND_MEAN:
				setDistnLabel(kPopulation, kDensityLabelColor);
				setShowDensity(CONTIN_DISTN);
				break;
			
			case SUMMARY_MEANS:
				setDistnLabel(kMeanDistn, kDensityLabelColor);
				setShowDensity(NO_DISTN);
				break;
				
			case SUMMARY_ERRORS:
			default:
				setDistnLabel(kErrorDistn, kDensityLabelColor);
				setShowDensity(NO_DISTN);
		}
		
		if (xBarImage == null || muImage == null) {
				xBarImage = CoreImageReader.getImage(kXBarFile);
				muImage = CoreImageReader.getImage(kMuFile);
			MediaTracker tracker = new MediaTracker(applet);
			tracker.addImage(xBarImage, 0);
			tracker.addImage(muImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		
		double sampMean = getSampleMean();
		if (Double.isNaN(sampMean))
			return;
		
		Point pPop = null;
		if (normalKey != null) {
			double popMean = getPopnMean();
			
			int horizPos = axis.numValToRawPosition(popMean);
			pPop = translateToScreen(horizPos, 0, null);
			
			g.setColor(kDensityLabelColor);
			g.drawLine(pPop.x, 0, pPop.x, getSize().height);
		}
		
		int horizPos = axis.numValToRawPosition(sampMean);
		Point pSamp = translateToScreen(horizPos, 0, null);
		
		g.setColor(kMeanValueColor);
		g.drawLine(pSamp.x, 0, pSamp.x, getSize().height);
		
		if (viewType != SUMMARY_ERRORS) {
			int imageTop = (viewType == SAMPLE_AND_MEAN) ? kTopGap
																					: kTopGap + g.getFontMetrics().getAscent();
			
			if (pPop != null && pSamp.x < pPop.x) {
				g.drawImage(xBarImage, pSamp.x - kLineImageGap - kImageWidth, imageTop, this);
				g.drawImage(muImage, pPop.x + kLineImageGap, imageTop, this);
			}
			else {
				g.drawImage(xBarImage, pSamp.x + kLineImageGap, imageTop, this);
				if (pPop != null)
					g.drawImage(muImage, pPop.x - kLineImageGap - kImageWidth, imageTop, this);
			}
		}
		
		g.setColor(Color.blue);
		if (pPop != null && viewType == SUMMARY_MEANS)
			kError.drawCentred(g, (pPop.x + pSamp.x) / 2, kTopGap + g.getFontMetrics().getAscent());
		
		if (pPop != null && viewType != SAMPLE_AND_MEAN) {
			int lineVert = kTopGap + kArrowSize;
			if (viewType == SUMMARY_MEANS)
				lineVert += g.getFontMetrics().getAscent();
			
			g.drawLine(pPop.x, lineVert, pSamp.x, lineVert);
			
			int sign = (pPop.x > pSamp.x) ? 1 : -1;
			g.drawLine(pSamp.x, lineVert, pSamp.x + sign * kArrowSize, lineVert + sign * kArrowSize);
			g.drawLine(pSamp.x, lineVert, pSamp.x + sign * kArrowSize, lineVert - sign * kArrowSize);
		}
		
		g.setColor(getForeground());
	}
	
	private double getSampleMean() {
		NumVariable yVar = getNumVariable();
		switch (viewType) {
			case SAMPLE_AND_MEAN:
				ValueEnumeration e = yVar.values();
				double sy = 0.0;
				while (e.hasMoreValues())
					sy += e.nextDouble();
				
				return sy / yVar.noOfValues();
			
			case SUMMARY_MEANS:
			case SUMMARY_ERRORS:
				int selIndex = getSelection().findSingleSetFlag();
				if (selIndex >= 0)
					return yVar.doubleValueAt(selIndex);
			default:
				return Double.NaN;
		}
	}
	
	private double getPopnMean() {
		DistnVariable popVar = (DistnVariable)getData().getVariable(normalKey);
		return popVar.getMean().toDouble();
	}

}