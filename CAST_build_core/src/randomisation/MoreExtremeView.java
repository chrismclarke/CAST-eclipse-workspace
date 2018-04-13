package randomisation;

import java.awt.*;

import coreGraphics.*;
import dataView.*;
import axis.*;


public class MoreExtremeView extends DotPlotView {
	
	static final private Color kMoreExtremeColour = new Color(0xFF9999);
	static final private Color kLessExtremeColour = new Color(0xFF3333);
	
	static final private Color kPaleRedColor = new Color(0xFFEEEE);
	
	private double lowCutOff, highCutOff;
	private BackgroundNormalArtist backgroundDrawer;
	private boolean drawNormal = false;
	
	private LabelValue label = null;
	private Color labelColor;
	
	public MoreExtremeView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey,
																																								String distnKey) {
		super(theData, applet, theAxis, 1.0);
		setActiveNumVariable(yKey);
		if (distnKey != null) {
			backgroundDrawer = new BackgroundNormalArtist(distnKey, theData);
			backgroundDrawer.setFillColor(kMoreExtremeColour);
			backgroundDrawer.setHighlightColor(kLessExtremeColour);
		}
	}
	
	public void setCutOffs(double lowCutOff, double highCutOff) {
		this.lowCutOff = lowCutOff;
		this.highCutOff = highCutOff;
	}
	
	public void setDensityColor(Color c) {
		if (backgroundDrawer != null)
			backgroundDrawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		this.label = label;
		this.labelColor = labelColor;
	}
	
	public void setShowDensity(boolean drawTheory) {
		drawNormal = drawTheory && (backgroundDrawer != null);
	}
	
	public void drawBackground(Graphics g) {
		if (!Double.isInfinite(lowCutOff)) {
			int borderPos = axis.numValToRawPosition(lowCutOff);
			Point p = translateToScreen(borderPos, 0, null);
			
			g.setColor(kPaleRedColor);
			g.fillRect(0, 0, p.x, getSize().height);
			g.setColor(Color.red);
			g.drawLine(p.x, 0, p.x, getSize().height);
		}
		
		if (!Double.isInfinite(highCutOff)) {
			int borderPos = axis.numValToRawPosition(highCutOff);
			Point p = translateToScreen(borderPos, 0, null);
			
			g.setColor(kPaleRedColor);
			g.fillRect(p.x, 0, getSize().width, getSize().height);
			g.setColor(Color.red);
			g.drawLine(p.x, 0, p.x, getSize().height);
		}
		
		if (drawNormal) {
			backgroundDrawer.paintDistn(g, this, axis, lowCutOff, highCutOff);
			if (label != null) {
				g.setColor(labelColor);
				int ascent = g.getFontMetrics().getAscent();
				label.drawLeft(g, getSize().width - 2, ascent + 2);
				g.setColor(getForeground());
			}
		}
	}
	
	protected int groupIndex(int itemIndex) {
		NumVariable variable = getNumVariable();
		double value = variable.doubleValueAt(itemIndex);
		
		return ((value <= lowCutOff) || (value >= highCutOff)) ? 2 : 0;
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		if (!drawNormal)
			super.paintView(g);
	}
}