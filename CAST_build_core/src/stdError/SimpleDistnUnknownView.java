package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import imageGroups.*;
import coreGraphics.*;


public class SimpleDistnUnknownView extends SimpleDistnView {
	
	public SimpleDistnUnknownView(DataSet theData, XApplet applet, NumCatAxis theAxis,
								String distnKey, LabelValue label, Color labelColor, int algorithm) {
		super(theData, applet, theAxis, distnKey, label, labelColor, algorithm);
	}
	
	public SimpleDistnUnknownView(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey) {
		super(theData, applet, theAxis, distnKey);
	}
	
	public void setDistnKey(String distnKey, XApplet applet) {
		super.setDistnKey(distnKey,  applet);
		if (distnKey == null)
			TickCrossImages.loadCrossAndTick(applet);
	}
	
	public void paintView(Graphics g) {
		if (distnKey == null) {
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