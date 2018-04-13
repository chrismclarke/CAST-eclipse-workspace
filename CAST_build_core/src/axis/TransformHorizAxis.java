package axis;

import java.awt.*;

import dataView.*;


public class TransformHorizAxis extends HorizAxis implements TransAxisInterface {
	private String kPowerEqualsString;
	
	public TransformHorizAxis(XApplet applet) {
		super(applet);
		kPowerEqualsString = applet.translate("Transform") + " = ";
	}
	
	public void findAxisWidth() {
		super.findAxisWidth();
		
		setValHeight();
		axisWidth += valHeight;
	}
	
	public void corePaint(Graphics g) {
		int transPos = lowBorderUsed + findDragPos();
		if (selectedVal) {
			g.setColor(Color.yellow);
			g.fillRect(transPos - 2, 0, 5, getSize().height - valHeight);
		}
		g.setColor(Color.red);
		g.drawLine(transPos, 0, transPos, getSize().height - valHeight - 1);
		
		int powerEqualsWidth = g.getFontMetrics().stringWidth(kPowerEqualsString);
		int valWidth = powerWidth(g);
		int powerStart = transPos - valWidth / 2 - powerEqualsWidth;
		if (powerStart < 0)
			powerStart = 0;
		if (powerStart + valWidth + powerEqualsWidth >= getSize().width)
			powerStart = getSize().width - valWidth - powerEqualsWidth;
		
		g.drawString(kPowerEqualsString, powerStart, getSize().height - 2 - descent);
		
		drawPower(g, powerStart + powerEqualsWidth, getSize().height - valHeight);
		
		g.setColor(getForeground());
		super.corePaint(g);
	}
}