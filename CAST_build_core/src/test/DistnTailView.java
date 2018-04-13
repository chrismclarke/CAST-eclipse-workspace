package test;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DistnTailView extends DataView {
	static final public int TWO_TAIL = 0;
	static final public int LOW_TAIL = 1;
	static final public int HIGH_TAIL = 2;
	
	private BackgroundNormalArtist backgroundDrawer;
	private NumCatAxis axis;
	private int tailHighlight;
	private String statisticKey;
	
	private Value distnName = null;
	private Color distnNameColor = null;
	
	public DistnTailView(DataSet theData, XApplet applet, NumCatAxis axis, String normalKey,
									String statisticKey, int tailHighlight) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		backgroundDrawer = new BackgroundNormalArtist(normalKey, theData);
		this.axis = axis;
		this.tailHighlight = tailHighlight;
		this.statisticKey = statisticKey;
	}
	
	public void setDistnName(Value distnName, Color distnNameColor) {
		this.distnName = distnName;
		this.distnNameColor = distnNameColor;
	}
	
	public void paintView(Graphics g) {
		NumVariable stat = (NumVariable)getData().getVariable(statisticKey);
		double statistic = stat.doubleValueAt(0);
		
		switch (tailHighlight) {
			case TWO_TAIL:
				double absStatistic = Math.abs(statistic);
				backgroundDrawer.paintDistn(g, this, axis, -absStatistic, absStatistic);
				break;
			case LOW_TAIL:
				backgroundDrawer.paintDistn(g, this, axis, statistic, Double.POSITIVE_INFINITY);
				break;
			case HIGH_TAIL:
				backgroundDrawer.paintDistn(g, this, axis, Double.NEGATIVE_INFINITY, statistic);
				break;
		}
		
		try {
			int statPos = axis.numValToPosition(statistic) + getViewBorder().left;
			g.drawLine(statPos, 0, statPos, getSize().height - 1);
		} catch (AxisException e) {
		}
		
		if (distnName != null) {
			int ascent = g.getFontMetrics().getAscent();
			g.setColor(distnNameColor);
			distnName.drawLeft(g, getSize().width - 3, ascent + 2);
			g.setColor(getForeground());
		}
	}
	
	public void setDensityColor(Color c) {
		backgroundDrawer.setFillColor(c);
	}
	
	public void setHighlightColor(Color c) {
		backgroundDrawer.setHighlightColor(c);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}