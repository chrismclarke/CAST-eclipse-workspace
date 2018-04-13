package inference;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.ValueView;
import formula.*;
import images.*;


public class SigmaKValueView extends ValueView {
	
	static public final int SE_MEAN = 0;
	static public final int CONF_LEVEL = 1;
	static public final int CONF_INTERVAL = 2;
	
	static final private int kTextImageGap = 4;
	
	static final private String kSEImageFile = "ci/seMean.png";
	static final private int kSEImageAscent = 18;
	static final private int kSEImageDescent = 13;
	
	static final private String kLevelImageFile = "ci/plusMinus.png";
	static final private int kLevelImageAscent = 10;
	static final private int kLevelImageDescent = 0;
	
	static final private String kCIImageFile = "ci/xBarPlusMinus.png";
	static final private int kCIImageAscent = 13;
	static final private int kCIImageDescent = 4;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private String distnKey;
	
	private String imageFile;
	private double k;
	private LabelValue labelStartText;
	private String labelEndText;
	private NumValue maxValue;
	
	private Image labelImage;
	private int imageAscent, imageDescent;
	
	public SigmaKValueView(DataSet theData, XApplet applet, String distnKey, int valueType, NumValue maxValue) {
		super(theData, applet);
		this.distnKey = distnKey;
		
		switch (valueType) {
			case SE_MEAN:
				k = 1.0;
				labelStartText = null;
				labelEndText = null;
				imageFile = kSEImageFile;
				imageAscent = kSEImageAscent;
				imageDescent = kSEImageDescent;
				break;
			case CONF_LEVEL:
				k = 1.96;
				labelStartText = new LabelValue("P(" + applet.translate("error is between") + " ");
				labelEndText = ") = 0.95";
				imageFile = kLevelImageFile;
				imageAscent = kLevelImageAscent;
				imageDescent = kLevelImageDescent;
				break;
			case CONF_INTERVAL:
				k = 1.96;
				StringTokenizer st = new StringTokenizer(applet.translate("95% CI for * is"), "*");
				labelStartText = new LabelValue(st.nextToken() + MText.expandText("#mu#") + " is ");
				labelEndText = null;
				imageFile = kCIImageFile;
				imageAscent = kCIImageAscent;
				imageDescent = kCIImageDescent;
		}
		
		this.maxValue = maxValue;
		
		labelImage = CoreImageReader.getImage(imageFile);
		MediaTracker tracker = new MediaTracker(applet);
		tracker.addImage(labelImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		
		if (labelEndText != null)
			setUnitsString(labelEndText);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		int width = labelImage.getWidth(this);
		if (labelStartText != null)
			width += kTextImageGap + labelStartText.stringWidth(g);
		return width;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		DistnVariable distn = (DistnVariable)getVariable(distnKey);
		NumValue kSD = distn.getSD();
		if (k != 0.0)
			kSD = new NumValue(kSD.toDouble() * k, kSD.decimals);
		return kSD.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (labelStartText != null) {
			labelStartText.drawRight(g, startHoriz, baseLine);
			startHoriz +=labelStartText.stringWidth(g) + kTextImageGap;
		}
		g.drawImage(labelImage, startHoriz, baseLine - imageAscent, this);
	}
	
	protected boolean highlightValue() {
		return false;
	}
	
	protected int getLabelAscent(Graphics g) {
		return Math.max(super.getLabelAscent(g), imageAscent);
	}
	
	protected int getLabelDescent(Graphics g) {
		return Math.max(super.getLabelDescent(g), imageDescent);
	}
}
