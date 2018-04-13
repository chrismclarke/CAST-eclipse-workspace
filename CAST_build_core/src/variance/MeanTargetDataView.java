package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import images.*;


public class MeanTargetDataView extends StackedDotPlotView {
	
	static final private String xBarFile = "symbols/redXBar.gif";
	static final private int kXBarWidth = 11;
	static final private int kXBarHeight = 14;
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private double target;
	private Value targetName;
	
	private Image xBarImage;
	
	public MeanTargetDataView(DataSet theData, XApplet applet, NumCatAxis theAxis,
									double target, Value targetName) {
		super(theData, applet, theAxis, null, false);
		this.target = target;
		this.targetName = targetName;
		
		MediaTracker tracker = new MediaTracker(this);
			xBarImage = CoreImageReader.getImage(xBarFile);
		tracker.addImage(xBarImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	protected void paintBackground(Graphics g) {
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
}