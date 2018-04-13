package ssq;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import images.*;



public class SsqStackedView extends StackedPlusNormalView {
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static private String[] asArray(String s) {
		
		if (s == null)
			return null;
		String temp[] = new String[1];
		temp[0] = s;
		return temp;
	}
	
	private String distnKey;
	private Color sampleMeanColor;
	
	private Image currentMeanImage = null;
	private Image meanImages[] = null;
	
	public SsqStackedView(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey,
									String[] meanGifs, Color sampleMeanColor) {
		super(theData, applet, theAxis, distnKey);
		this.distnKey = distnKey;
		this.sampleMeanColor = sampleMeanColor;
		setChi2Df(applet);
		
		if (meanGifs != null) {
			MediaTracker tracker = new MediaTracker(this);
			meanImages = new Image[meanGifs.length];
			for (int i=0 ; i<meanGifs.length ; i++) {
				meanImages[i] = CoreImageReader.getImage(meanGifs[i]);
				tracker.addImage(meanImages[i], 0);
			}
			currentMeanImage = meanImages[0];
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public SsqStackedView(DataSet theData, XApplet applet, NumCatAxis theAxis,
														String distnKey, String meanGif, Color sampleMeanColor) {
		this(theData, applet, theAxis, distnKey, asArray(meanGif), sampleMeanColor);
	}
	
	public void setChi2Df() {
		setChi2Df(getApplet());
	}
	
	private void setChi2Df(XApplet applet) {
		GammaDistnVariable chi2 = (GammaDistnVariable)getVariable(distnKey);
		int df = (int)Math.round(chi2.getShape().toDouble() * 2.0);
		setDistnLabel(new LabelValue(applet.translate("Chi-squared") + "(" + df + " df)"), Color.gray);
	}
	
	public void setSsqType(String ssqKey, int imageIndex, Color ssqColor, Color meanColor) {
		setActiveNumVariable(ssqKey);
		setForeground(ssqColor);
		sampleMeanColor = meanColor;
		currentMeanImage = meanImages[imageIndex];
	} 
	
	protected void paintBackground(Graphics g) {
		if (currentMeanImage != null) {
			GammaDistnVariable chi2 = (GammaDistnVariable)getVariable(distnKey);
			double mean = chi2.getMean().toDouble();
			
			g.setColor(Color.black);
			try {
				int meanPos = axis.numValToPosition(mean);
				Point p  = translateToScreen(meanPos, 0, null);
				g.drawLine(p.x, 0, p.x, getSize().height);
				
				g.drawImage(currentMeanImage, p.x + 3, 3, this);
			} catch (AxisException e) {
			}
			
			g.setColor(getForeground());
		}
		
		super.paintBackground(g);
		
		g.setColor(sampleMeanColor);
		try {
			int meanPos = axis.numValToPosition(getSampleMean());
			Point p  = translateToScreen(meanPos, 0, null);
			g.drawLine(p.x, 20, p.x, getSize().height);
		} catch (AxisException e) {
		}
		
		g.setColor(getForeground());
	}
	
	private double getSampleMean() {
		ValueEnumeration ye = getNumVariable().values();
		int n=0;
		double sy = 0.0;
		while (ye.hasMoreValues()) {
			n++;
			sy += ye.nextDouble();
		}
		return sy / n;
	}
}