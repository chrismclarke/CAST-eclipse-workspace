package twoGroup;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import images.*;


public class StackedWithArrowView extends StackedDotPlotView {
	
	static final private int kMaxWait = 30000;		//		30 seconds
	static final private int kArrowImageGap = 5;
	static final private int kImageTopGap = 5;
	
	static final private Color kArrowColor = Color.gray;
	
	private double arrowValue;
	private Image arrowNameImage;
	
	public StackedWithArrowView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																		String xKey, double arrowValue, String imageFile) {
		super(theData, applet, theAxis, null, false);
		this.arrowValue = arrowValue;
		this.arrowValue = arrowValue;
		setActiveCatVariable(xKey);
		
		MediaTracker tracker = new MediaTracker(applet);
			arrowNameImage = CoreImageReader.getImage(imageFile);
		tracker.addImage(arrowNameImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	protected void paintBackground(Graphics g) {
		g.setColor(kArrowColor);
		
		try {
			int arrowPos = axis.numValToPosition(arrowValue);
			int arrowHoriz = translateToScreen(arrowPos, 0, null).x;
			
			g.drawLine(arrowHoriz, 0, arrowHoriz, getSize().height);
			g.drawImage(arrowNameImage, arrowHoriz + kArrowImageGap, kImageTopGap, this);
		} catch (AxisException ex) {
		}
		
		g.setColor(getForeground());
	}
}