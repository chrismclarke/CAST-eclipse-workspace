package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import imageGroups.*;


public class ScatterUnknownView extends ScatterView {
	
	public ScatterUnknownView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
	}
	
	public void paintView(Graphics g) {
		if (yKey == null) {		//		only required to support UnknownRelnApplet
			int qnHoriz = getSize().width / 2;
			int qnVert = getSize().height / 2;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
			qnHoriz -= TickCrossIcon.kAnswerSize;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
			qnVert -= TickCrossIcon.kAnswerSize;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
			qnHoriz += TickCrossIcon.kAnswerSize;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
		}
		else
			super.paintView(g);
	}
}
	
