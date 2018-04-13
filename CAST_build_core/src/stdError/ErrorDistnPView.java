package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import images.*;


public class ErrorDistnPView extends DiscretePlusBinomView {
	
	static public final int SUMMARY_COUNTS = 1;
	static public final int SUMMARY_PROPNS = 2;
	static public final int SUMMARY_ERRORS = 3;
	
	static final private Color kPopDensityColor = new Color(0xEEEEFF);
	static final private Color kDensityLabelColor = new Color(0x8888BB);
	static final private Color kMeanValueColor = new Color(0xCC0000);
	
	static final private String kSampleXFile = "symbols/redNP.gif";
	static final private String kPopMeanFile = "symbols/purpleNPi.gif";
	static final private String kPFile = "symbols/redP.gif";
	static final private String kPiFile = "symbols/purplePi.gif";
	
	static final private int kXImageWidth = 45;
//	static final private int kImageHeight = 14;
	static final private int kPImageWidth = 10;
	static final private int kLineImageGap = 5;
	static final private int kTopGap = 3;
	static final private int kArrowSize = 4;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private LabelValue kCountDistn = new LabelValue("Distn of x");
	static final private LabelValue kPropnDistn = new LabelValue("Distn of p");
	static final private LabelValue kErrorDistn = new LabelValue("Distn of error");
	
	private LabelValue kError;
	
	
	private String binomKey;
	private int viewType;
	
	static private Image sampleXImage, popMeanImage, pImage, piImage;
	
	public ErrorDistnPView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																									String xKey, String binomKey, int viewType) {
		super(theData, applet, theAxis, xKey, binomKey);
		kError = new LabelValue(applet.translate("Error"));
		setDensityColor(kPopDensityColor);
		this.binomKey = binomKey;
		setViewType(viewType);
		setShowDensity(NO_DISTN);
		
		if (sampleXImage == null || popMeanImage == null) {
				sampleXImage = CoreImageReader.getImage(kSampleXFile);
				popMeanImage = CoreImageReader.getImage(kPopMeanFile);
				pImage = CoreImageReader.getImage(kPFile);
				piImage = CoreImageReader.getImage(kPiFile);
			MediaTracker tracker = new MediaTracker(applet);
			tracker.addImage(sampleXImage, 0);
			tracker.addImage(popMeanImage, 0);
			tracker.addImage(pImage, 0);
			tracker.addImage(piImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void setViewType(int viewType) {
		this.viewType = viewType;
		switch (viewType) {
			case SUMMARY_COUNTS:
				setDistnLabel(kCountDistn, kDensityLabelColor);
				break;
				
			case SUMMARY_PROPNS:
				setDistnLabel(kPropnDistn, kDensityLabelColor);
				break;
				
			case SUMMARY_ERRORS:
			default:
				setDistnLabel(kErrorDistn, kDensityLabelColor);
		}
		repaint();
	}
	
	public void drawBackground(Graphics g) {
		super.drawBackground(g);
		
		double sampMean = getSampleMean();
		if (Double.isNaN(sampMean))
			return;
		
		Point pPop = null;
		if (binomKey != null) {
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
			int imageTop = kTopGap + ((viewType == SUMMARY_COUNTS) ? 0 : g.getFontMetrics().getAscent());
			Image popImage = (viewType == SUMMARY_PROPNS) ? piImage : popMeanImage;
			Image sampImage = (viewType == SUMMARY_PROPNS) ? pImage : sampleXImage;
			int imageWidth = (viewType == SUMMARY_PROPNS) ? kPImageWidth : kXImageWidth;
			
			if (pPop != null && pSamp.x < pPop.x) {
				g.drawImage(sampImage, pSamp.x - kLineImageGap - imageWidth, imageTop, this);
				g.drawImage(popImage, pPop.x + kLineImageGap, imageTop, this);
			}
			else {
				g.drawImage(sampImage, pSamp.x + kLineImageGap, imageTop, this);
				if (pPop != null)
					g.drawImage(popImage, pPop.x - kLineImageGap - imageWidth, imageTop, this);
			}
		
			g.setColor(Color.blue);
			if (pPop != null && viewType == SUMMARY_PROPNS)
				kError.drawCentred(g, (pPop.x + pSamp.x) / 2, kTopGap + g.getFontMetrics().getAscent());
		}
		
		if (pPop != null) {
			int lineVert = kTopGap + kArrowSize;
			if (viewType == SUMMARY_PROPNS)
				lineVert += g.getFontMetrics().getAscent();
			
			g.drawLine(pPop.x, lineVert, pSamp.x, lineVert);
			
			int sign = (pPop.x > pSamp.x) ? 1 : -1;
			g.drawLine(pSamp.x, lineVert, pSamp.x + sign * kArrowSize, lineVert + sign * kArrowSize);
			g.drawLine(pSamp.x, lineVert, pSamp.x + sign * kArrowSize, lineVert - sign * kArrowSize);
		}
		
		g.setColor(getForeground());
	}
	
	private double getSampleMean() {
		NumVariable xVar = getNumVariable();
		
		int selIndex = getSelection().findSingleSetFlag();
		if (selIndex >= 0)
			return xVar.doubleValueAt(selIndex);
		else
			return Double.NaN;
	}
	
	private double getPopnMean() {
		DistnVariable popVar = (DistnVariable)getData().getVariable(binomKey);
		return popVar.getMean().toDouble();
	}

}