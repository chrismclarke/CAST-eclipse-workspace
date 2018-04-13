package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class StackCumulativeView extends StackedDotPlotView {
	static final private Color kLowBackgroundColor = new Color(0xEEEEFF);
	static final private Color kCutoffColor = new Color(0x6666FF);
	static final private Color kCutoffTitleColor = new Color(0x000099);
	static final private String kValueBelowString = "values below";
	
	private NumValue cutoff = null;
	private int nBelowCutoff;
	
	public StackCumulativeView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey) {
		super(theData, applet, theAxis, null, false);
		setActiveNumVariable(yKey);
	}
	
	public void setCutoff(NumValue cutoff) {
		this.cutoff = cutoff;
		NumVariable yVar = getNumVariable();
		ValueEnumeration ye = yVar.values();
		nBelowCutoff = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			if (y < cutoff.toDouble())
				nBelowCutoff ++;
		}
		repaint();
	}
	
	public int getNBelowCutoff() {
		return nBelowCutoff;
	}
	
	private void drawTitleString(Graphics g) {
		g.setColor(kCutoffTitleColor);
		g.setFont(getApplet().getBigBigBoldFont());
		
		String titleString = nBelowCutoff + " " + getApplet().translate(kValueBelowString) + " " + cutoff.toString();
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int titleWidth = fm.stringWidth(titleString);
		g.drawString(titleString, (getWidth() - titleWidth) / 2, ascent + 2);
	}
	
	protected void paintBackground(Graphics g) {
		int cutoffPos = axis.numValToRawPosition(cutoff.toDouble());
		Point p = translateToScreen(cutoffPos, 0, null);
		g.setColor(kLowBackgroundColor);
		g.fillRect(0, 0, p.x, getSize().height + 1);
		g.setColor(kCutoffColor);
		g.drawLine(p.x, 0, p.x, getSize().height);
		
		drawTitleString(g);
		
		g.setColor(getForeground());
	}
}