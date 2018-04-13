package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import imageGroups.*;
import coreGraphics.*;


public class UnknownDotPlotView extends StackedDotPlotView {
	
	private boolean showUnknown;
	
	public UnknownDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	public void setShowUnknown(boolean showUnknown, XApplet applet) {
		this.showUnknown = showUnknown;
		if (showUnknown)
			TickCrossImages.loadCrossAndTick(applet);
		repaint();
	}
	
	public void paintView(Graphics g) {
		if (showUnknown) {
			int qnHoriz = (getSize().width - 3 * TickCrossIcon.kAnswerSize) / 2;
			int qnVert = (getSize().height - TickCrossIcon.kAnswerSize) / 2;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
			qnHoriz += 2 * TickCrossIcon.kAnswerSize;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
		}
		else
			super.paintView(g);
	}
}