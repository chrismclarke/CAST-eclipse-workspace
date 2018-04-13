package axis;

import java.awt.*;

import dataView.*;


public class TransformVertAxis extends VertAxis implements TransAxisInterface {
	static final private int kPowerBorder = 2;

	public TransformVertAxis(XApplet applet) {
		super(applet);
	}
	
	public void findAxisWidth() {
		super.findAxisWidth();
		
		setValHeight();
		int valWidth = maxPowerWidth(getGraphics());
		axisWidth = Math.max(axisWidth, valWidth + 2 * kPowerBorder);
	}
	
	protected int getTopExtraHeight() {
		return valHeight + 2 * kPowerBorder;
	}
	
	public void corePaint(Graphics g) {
		if (!labels.isEmpty()) {
			int transPos = getSize().height - 1 - (lowBorderUsed + findDragPos());
			if (selectedVal) {
				g.setColor(Color.yellow);
				g.fillRect(0, transPos - 2, getSize().width, 5);
			}
			g.setColor(Color.red);
			g.drawLine(0, transPos, getSize().width - 1, transPos);
			
			drawPower(g, kPowerBorder, kPowerBorder);
			g.setColor(getForeground());
		}
		super.corePaint(g);
	}
}