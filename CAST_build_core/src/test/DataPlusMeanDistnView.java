package test;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import formula.*;
import images.*;


public class DataPlusMeanDistnView extends JitterPlusNormalView {
	static final private Color kMeanDensityColor = new Color(0xCCCCFF);
	static final private Color kDensityLabelColor = new Color(0x8888BB);
	static final private Color kMeanValueColor = new Color(0xCC0000);
	
	static final private int kQuestionBottomGap = 7;
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private String normalKey;
	private Image questionImage;
	
	public DataPlusMeanDistnView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																					String normalKey, double initialJittering) {
		super(theData, applet, theAxis, normalKey, initialJittering);
		setDensityColor(kMeanDensityColor);
		this.normalKey = normalKey;
		if (normalKey == null) {
			String fileName = "test/question.gif";
				questionImage = CoreImageReader.getImage(fileName);
			MediaTracker tracker = new MediaTracker(applet);
			tracker.addImage(questionImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		
		NumVariable yVar = getNumVariable();
		ValueEnumeration e = yVar.values();
		double sy = 0.0;
		while (e.hasMoreValues())
			sy += e.nextDouble();
		
		double mean = sy / yVar.noOfValues();
		
		if (normalKey == null) {
			int questionWidth = questionImage.getWidth(this);
			int questionHeight = questionImage.getHeight(this);
			
			int horizPos = axis.numValToRawPosition(mean);
			Point p = translateToScreen(horizPos, 0, null);
			
			g.drawImage(questionImage, p.x - questionWidth / 2,
																		getSize().height - questionHeight - kQuestionBottomGap,
																		questionWidth, questionHeight, this);
		}
		
		int baseline = g.getFontMetrics().getAscent() + 2;
		
		g.setColor(kDensityLabelColor);
		LabelValue kMeanDensity = new LabelValue(getApplet().translate("Distn of mean") + MText.expandText(" (H#sub0#)"));
		kMeanDensity.drawLeft(g, getSize().width - 2, baseline);
		
		g.setColor(kMeanValueColor);
		try {
			int horizPos = axis.numValToPosition(mean);
			Point p = translateToScreen(horizPos, 0, null);
			if (vertNotHoriz)
				g.drawLine(0, p.y, getSize().width, p.y);
			else
				g.drawLine(p.x, 0, p.x, getSize().height);
		} catch (AxisException ex) {
		}
		
		LabelValue kSampleMean = new LabelValue(getApplet().translate("Sample mean"));
		kSampleMean.drawRight(g, 2, baseline);
		
		g.setColor(getForeground());
	}
}