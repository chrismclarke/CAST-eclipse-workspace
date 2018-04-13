package stdError;

import java.awt.*;

import dataView.*;
import distn.*;
import images.*;


public class ErrorPropnMeanSdView extends DataView {
	
	static final public int COUNT = 0;
	static final public int PROPN = 1;
	static final public int ERROR = 2;
	
	static final private int kImageWidth = 266;
	static final private int kImageHeight = 109;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private String kCountImageName = "ci/errorCountMeanSD.png";
	static final private String kPropnImageName = "ci/errorPropnMeanSD.png";
	static final private String kErrorImageName = "ci/errorErrorMeanSD.png";
	
	static final private Point kCountMeanPos = new Point(120, 40);
	static final private Point kCountSDPos = new Point(191, 73);
	static final private Point kPropnMeanPos = new Point(110, 40);
	static final private Point kPropnSDPos = new Point(183, 73);
	static final private Point kErrorSDPos = new Point(224, 92);
	
	private String distnKey;
	
	private int statisticType;
	private int countDecimals, propnDecimals;
	
	private Image countImage, propnImage, errorImage;
	
	public ErrorPropnMeanSdView(DataSet theData, XApplet applet, String distnKey, int statisticType,
											int countDecimals, int propnDecimals) {
		super(theData, applet, new Insets(0,0,0,0));
		this.distnKey = distnKey;
		this.statisticType = statisticType;
		this.countDecimals = countDecimals;
		this.propnDecimals = propnDecimals;
		
		MediaTracker tracker = new MediaTracker(applet);
			countImage = CoreImageReader.getImage(kCountImageName);
			propnImage = CoreImageReader.getImage(kPropnImageName);
			errorImage = CoreImageReader.getImage(kErrorImageName);
		tracker.addImage(countImage, 0);
		tracker.addImage(propnImage, 0);
		tracker.addImage(errorImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	public void setStatisticType(int statisticType) {
		this.statisticType = statisticType;
	}
	
	public void paintView(Graphics g) {
		BinomialDistnVariable yDistn = (BinomialDistnVariable)getVariable(distnKey);
		int n = yDistn.getCount();
		double p = yDistn.getProb();
		
		switch (statisticType) {
			case COUNT:
				g.drawImage(countImage, 0, 0, this);
				NumValue yMean = new NumValue(n * p, countDecimals);
				NumValue ySD = new NumValue(Math.sqrt(n * p * (1 - p)), countDecimals);
				yMean.drawRight(g, kCountMeanPos.x, kCountMeanPos.y);
				ySD.drawRight(g, kCountSDPos.x, kCountSDPos.y);
				break;
			case PROPN:
				g.drawImage(propnImage, 0, 0, this);
				NumValue pMean = new NumValue(p, propnDecimals);
				NumValue pSD = new NumValue(Math.sqrt(p * (1 - p) / n), propnDecimals);
				pMean.drawRight(g, kPropnMeanPos.x, kPropnMeanPos.y);
				pSD.drawRight(g, kPropnSDPos.x, kPropnSDPos.y);
				break;
			case ERROR:
				g.drawImage(errorImage, 0, 0, this);
				NumValue eSD = new NumValue(Math.sqrt(p * (1 - p) / n), propnDecimals);
				eSD.drawRight(g, kErrorSDPos.x, kErrorSDPos.y);
				break;
		}
	}

//-----------------------------------------------------------------------------------

	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		NumValue maxErrorSD = new NumValue(1.0, propnDecimals);
		int maxErrorSDWidth = maxErrorSD.stringWidth(g);
		
		return new Dimension(Math.max(kImageWidth, kErrorSDPos.x + maxErrorSDWidth),
																															kImageHeight);
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}