package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import images.*;


public class Chi2View extends MarginalDataView implements DataPlusDistnInterface {
	static final private int kMinDensityWidth = 60;
	
	static final private String muFile = "xEquals/popnMean.png";
	static final private String muInfiniteFile = "anova/infiniteMean.gif";
	static final private int kMaxWait = 30000;		//		30 seconds
	static final private int kMuAscent = 13;
	static final private int kMuWidth = 31;
	static final private int kMuInfiniteWidth = 46;
	
	static final private int kInfMeanCenter = 80;
	static final private int kInfArrowLength = 50;
	static final private int kMeanArrowHead = 5;
	
	static final public int UPPER_TAIL = 0;
	static final public int TWO_TAILED = 1;
	
	static final private int kTopGap = 2;
	static final private int kFLabelGap = 2;
	static final private int kFArrowVertGap = 2;
	static final private int kFArrowHorizGap = 4;
	static final private int kArrowBottomSpace = 15;
	static final private int kArrowHead = 6;
	static final private int kHorizArrowLength = 14;
	
	private String distnKey, valueKey;
	private BackgroundArtistInterface distnDrawer;
	private Font bigFont;
	
	private int densityType = CONTIN_DISTN;
	protected String labelPrefix = null;
	protected String labelSuffix = null;
	private Color labelColor;
	private Color arrowColor = Color.red;
	
	private Image muImage, muInfiniteImage;
	
	private int tailType = UPPER_TAIL;
	private boolean showValueArrow = true;
	
	private LabelValue fLabel = new LabelValue("F");		//		Value label (initially written for F distn)
	
	public Chi2View(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey, String valueKey) {
		super(theData, applet, new Insets(0, 0, 0, 0), theAxis);
		this.distnKey = distnKey;
		this.valueKey = valueKey;
//		distnDrawer = new BackgroundNormalArtist(distnKey, theData);
//		distnDrawer.setMaxDensityFactor(1.3);			//	a bit more space at top
		AccurateDistn2Artist tempDrawer = new AccurateDistn2Artist(distnKey, theData);
//		tempDrawer.setFillColor(kDensityColor);
		distnDrawer = tempDrawer;
		
		bigFont = applet.getBigFont();
		
		if (valueKey == null) {
			MediaTracker tracker = new MediaTracker(this);
				muImage = CoreImageReader.getImage(muFile);
				muInfiniteImage = CoreImageReader.getImage(muInfiniteFile);
			tracker.addImage(muImage, 0);
			tracker.addImage(muInfiniteImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public Chi2View(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey) {
		this(theData, applet, theAxis, distnKey, null);
	}
	
	public void setTailType(int tailType) {
		this.tailType = tailType;
	}
	
	public void setShowValueArrow(boolean showValueArrow) {
		this.showValueArrow = showValueArrow;
	}
	
	public void setValueLabel(LabelValue fLabel) {
		this.fLabel = fLabel;
	}
	
	public void setDistnColors(Color fillColor, Color hiliteColor) {
		distnDrawer.setFillColor(fillColor);
		distnDrawer.setHighlightColor(hiliteColor);
	}
	
	public void setArrowColor(Color arrowColor) {
		this.arrowColor = arrowColor;
	}
	
	public void setAreaProportion(double areaProportion) {
		if (distnDrawer instanceof AccurateDistn2Artist)
			((AccurateDistn2Artist)distnDrawer).setAreaProportion(areaProportion);
	}
	
	private void drawDistnMean(Graphics g, ContinDistnVariable distn) {
		try {
			NumValue mean = distn.getMean();
			if (distn instanceof Chi2DistnVariable)
				mean.decimals = 0;
			g.setColor(Color.blue);
			
			if (Double.isInfinite(mean.toDouble())) {		//	for F distn only
				g.drawLine(getSize().width - kInfArrowLength, kInfMeanCenter,
																										getSize().width, kInfMeanCenter);
				g.drawLine(getSize().width, kInfMeanCenter, getSize().width - kMeanArrowHead,
																														kInfMeanCenter - kMeanArrowHead);
				g.drawLine(getSize().width, kInfMeanCenter, getSize().width - kMeanArrowHead,
																														kInfMeanCenter + kMeanArrowHead);
				
				g.drawImage(muInfiniteImage, getSize().width - kInfArrowLength - kMuInfiniteWidth - 4,
																						kInfMeanCenter - kMuAscent / 2, this);
			}
			else {
				int meanPos = axis.numValToPosition(mean.toDouble());
				int meanHoriz = translateToScreen(meanPos, 0, null).x;
				g.drawLine(meanHoriz, 0, meanHoriz, getSize().height);
				
				g.setFont(bigFont);
				int baseline = 3 + Math.max(kMuAscent, g.getFontMetrics().getAscent());
				g.drawImage(muImage, meanHoriz + 3, baseline - kMuAscent, this);
				mean.drawRight(g, meanHoriz + kMuWidth, baseline);
			}
		} catch (AxisException e) {
		}
	}
	
	public void drawDistnTail(Graphics g, ContinDistnVariable dist) {
		FontMetrics fm = g.getFontMetrics();
		
		int labelBaseline = fm.getAscent() + kTopGap;
		
		NumVariable fVar = (NumVariable)getVariable(valueKey);
		double f = fVar.doubleValueAt(0);
		try {
			int fPos = axis.numValToPosition(f);
			int fHoriz = translateToScreen(fPos, 0, null).x;
			
			double lowF = Double.NEGATIVE_INFINITY;
			double highF = f;
			
			if (tailType == TWO_TAILED) {
				double cumProb = dist.getCumulativeProb(f);
				double otherQuantile = dist.getQuantile(1.0 - cumProb);
				lowF = Math.min(f, otherQuantile);
				highF = Math.max(f, otherQuantile);
			}
			
			distnDrawer.paintDistn(g, this, axis, lowF, highF);
			
			if (showValueArrow) {
				g.setColor(arrowColor);
				int fBaseline = labelBaseline;
				if (fLabel != null) {
					fBaseline += fm.getAscent() + fm.getDescent() + kFLabelGap;
					fLabel.drawCentred(g, fHoriz, fBaseline);
				}
				
				int arrowTop = fBaseline + kFArrowVertGap;
				int arrowBottom = getSize().height - kArrowBottomSpace;
				g.drawLine(fHoriz, arrowTop, fHoriz, arrowBottom);
				g.drawLine(fHoriz - 1, arrowTop, fHoriz - 1, arrowBottom - 1);
				g.drawLine(fHoriz + 1, arrowTop, fHoriz + 1, arrowBottom - 1);
				for (int i=2 ; i<kArrowHead ; i++)
					g.drawLine(fHoriz - i, arrowBottom - i, fHoriz + i, arrowBottom - i);
			}
		} catch (AxisException ex) {
			distnDrawer.paintDistn(g, this, axis, axis.minOnAxis, axis.maxOnAxis);
			
			if (showValueArrow) {
				g.setColor(arrowColor);
				int fVertCenter = (getSize().height + labelBaseline) / 2;
				int fBaseline = fVertCenter + (fm.getAscent() - fm.getDescent()) / 2;
				
				int arrowRight = getSize().width - 2;
				int arrowLeft = arrowRight - kHorizArrowLength;
				int fRight = arrowLeft - kFArrowHorizGap;
				
				if (fLabel != null)
					fLabel.drawLeft(g, fRight, fBaseline);
				g.drawLine(arrowLeft, fVertCenter, arrowRight, fVertCenter);
				g.drawLine(arrowLeft, fVertCenter - 1, arrowRight - 1, fVertCenter - 1);
				g.drawLine(arrowLeft, fVertCenter + 1, arrowRight - 1, fVertCenter + 1);
				for (int i=2 ; i<kArrowHead ; i++)
					g.drawLine(arrowRight - i, fVertCenter - i, arrowRight - i, fVertCenter + i);
			}
		}
	}
	
	public void paintView(Graphics g) {
		Font stdFont = g.getFont();
		g.setFont(bigFont);
		
		if (densityType == CONTIN_DISTN && distnDrawer != null) {
			ContinDistnVariable distn = (ContinDistnVariable)getVariable(distnKey);
			if (labelPrefix != null) {
				g.setColor(labelColor);
				int ascent = g.getFontMetrics().getAscent();
				LabelValue label = getLabel(distn);
				label.drawLeft(g, getSize().width - 2, ascent + 2);
				g.setColor(getForeground());
			}
			
			if (valueKey == null) {
				drawDistnMean(g, distn);
				distnDrawer.paintDistn(g, this, axis);
			}
			else
				drawDistnTail(g, distn);
		}
		g.setFont(stdFont);
	}
	
	protected LabelValue getLabel(ContinDistnVariable distn) {
		if (labelSuffix == null)
			return new LabelValue(labelPrefix);
		else {
			Chi2DistnVariable chi2Distn = (Chi2DistnVariable)distn;
			int df = chi2Distn.getDF();
			return new LabelValue(labelPrefix + df + labelSuffix);
		}
	}
	
	public void setShowDensity (int densityType) {
		this.densityType = densityType;
		repaint();
	}
	
	public void setDensityColor(Color c) {
		distnDrawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		int nIndex = label.label.indexOf("#");
		if (nIndex < 0) {
			labelPrefix = label.label;
			labelSuffix = null;
		}
		else {
			labelPrefix = label.label.substring(0, nIndex);
			labelSuffix = label.label.substring(nIndex+1);
		}
		this.labelColor = labelColor;
	}

//-----------------------------------------------------------------------------------

	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(distnKey))
			distnDrawer.resetDistn();
		super.doChangeVariable(g, key);
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	public int minDisplayWidth() {
		return kMinDensityWidth;
	}
}