package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import images.*;


public class MeanAndTargetView extends DotPlotView implements DataPlusDistnInterface {
	
	static final private String xBarFile = "symbols/redXBar.gif";
	static final private int kXBarWidth = 11;
	static final private int kXBarHeight = 14;
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private String normalKey;
	private double target;
	private Value targetName;
	
	private AccurateDistnArtist backgroundDrawer;
	private int densityType = CONTIN_DISTN;
	private LabelValue label = null;
	private Color labelColor;
	
	private Image xBarImage;
	
	public MeanAndTargetView(DataSet theData, XApplet applet, NumCatAxis theAxis, String normalKey,
									double target, Value targetName) {
		super(theData, applet, theAxis, 1.0);
		this.target = target;
		this.targetName = targetName;
		this.normalKey = normalKey;
		
		backgroundDrawer = new AccurateDistnArtist(normalKey, theData);
		backgroundDrawer.setDensityScaling(0.7);
		
		MediaTracker tracker = new MediaTracker(this);
			xBarImage = CoreImageReader.getImage(xBarFile);
		tracker.addImage(xBarImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	private void paintBackground(Graphics g) {
		if (densityType == CONTIN_DISTN && backgroundDrawer != null) {
			backgroundDrawer.paintDistn(g, this, axis);
			if (label != null) {
				g.setColor(labelColor);
				int ascent = g.getFontMetrics().getAscent();
				label.drawLeft(g, getSize().width - 2, ascent + 2);
				g.setColor(getForeground());
			}
		}
		
		g.setColor(Color.blue);
		int baseline = g.getFontMetrics().getAscent() + 2;
		
		try {
			int targetPos = axis.numValToPosition(target);
			int targetHoriz = translateToScreen(targetPos, 0, null).x;
			targetName.drawCentred(g, targetHoriz, baseline);
			g.drawLine(targetHoriz, baseline + 4, targetHoriz, getSize().height);
		} catch (AxisException e) {
		}
		
		g.setColor(Color.red);
		int imageTop = baseline + g.getFontMetrics().getDescent() + 2;
		NumVariable yVar = getNumVariable();
		ValueEnumeration ye = yVar.values();
		double sy = 0.0;
		while (ye.hasMoreValues())
			sy += ye.nextDouble();
		double mean = sy / yVar.noOfValues();
		
		try {
			int meanPos = axis.numValToPosition(mean);
			int meanHoriz = translateToScreen(meanPos, 0, null).x;
			g.drawImage(xBarImage, meanHoriz - kXBarWidth / 2, imageTop, this);
			g.drawLine(meanHoriz, imageTop + kXBarHeight + 2, meanHoriz, getSize().height);
		} catch (AxisException e) {
		}
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		paintBackground(g);
		super.paintView(g);
	}
	
	public void setShowDensity (int densityType) {
		this.densityType = densityType;
		repaint();
	}
	
	public void setDensityColor(Color c) {
		if (backgroundDrawer != null)
			backgroundDrawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		this.label = label;
		this.labelColor = labelColor;
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(normalKey))
			backgroundDrawer.resetDistn();
		super.doChangeVariable(g, key);
	}
}