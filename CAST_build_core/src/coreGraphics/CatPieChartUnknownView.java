package coreGraphics;

import java.awt.*;

import dataView.*;
import imageGroups.*;


public class CatPieChartUnknownView extends CatPieChartView {

	public CatPieChartUnknownView(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, catKey);
	}
	
	public void setVariableKey(String catKey, XApplet applet) {
		super.setVariableKey(catKey, applet);
		if (catKey == null)
			TickCrossImages.loadCrossAndTick(applet);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		if (catKey == null) {
			g.setColor(Color.gray);
			g.drawOval(left, top, 2 * radius, 2 * radius);
			
			int qnHoriz = getSize().width / 2 - TickCrossIcon.kAnswerSize;
			int qnVert = (getSize().height - TickCrossIcon.kAnswerSize) / 2;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
			qnHoriz += TickCrossIcon.kAnswerSize;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
			return;
		}
		else
			super.paintView(g);
	}
}