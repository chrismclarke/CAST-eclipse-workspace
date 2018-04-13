package inference;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import imageGroups.TickCrossImages;


public class StackedDotUnknownView extends StackedDotPlotView {
	
	public StackedDotUnknownView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																										String freqKey, boolean popNotSamp) {
		super(theData, applet, theAxis, freqKey, popNotSamp);
	}
	
	public StackedDotUnknownView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	public void paintView(Graphics g) {
		if (popNotSamp && freqKey == null) {
			Point questionCentre = translateToScreen(getDisplayHeight() / 2,
																					getDisplayWidth() / 2, null);
			
			g.drawImage(TickCrossImages.question,
									questionCentre.x - TickCrossImages.question.getWidth(this) / 2,
									questionCentre.y - TickCrossImages.question.getHeight(this) / 2, this);
		}
		else
			super.paintView(g);
	}
}