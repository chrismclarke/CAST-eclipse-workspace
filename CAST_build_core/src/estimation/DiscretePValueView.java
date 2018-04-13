package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import formula.*;


public class DiscretePValueView extends DataView {
	static final private Color kDistnColor = new Color(0x999999);
//	static final private Color kHighlightColor = new Color(0x0000CC);
	static final private Color kSignificantColor = new Color(0xBB0000);
	static final private Color kNonSignificantColor = new Color(0x008800);
	static final private Color kSignificantBackground = new Color(0xFFDDDD);
	
	private String distnKey;
	private int observedX;
	private NumValue sigLevel;
	
	private NumCatAxis probAxis;
	private NumCatAxis xAxis;
	
	public DiscretePValueView(DataSet theData, XApplet applet, String distnKey, NumCatAxis probAxis,
															NumCatAxis xAxis, int observedX, NumValue sigLevel) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.distnKey = distnKey;
		this.probAxis = probAxis;
		this.xAxis = xAxis;
		this.observedX = observedX;
		this.sigLevel = sigLevel;
	}
	
	public void setObservedX(int observedX) {
		this.observedX = observedX;
		repaint();
	}
	
	private void drawTitleString(Graphics g) {
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		double lowProb = y.getCumulativeProb(observedX + 0.5);
		double highProb = 1 - y.getCumulativeProb(observedX - 0.5);
		double pValue = Math.min(lowProb, highProb) * 2;
		if (pValue > 1.0)
			pValue = 1.0;
		
		g.setColor(pValue < sigLevel.toDouble() ? kSignificantColor : kNonSignificantColor);
		
		String titleString = getApplet().translate("p-value") + " = 2" + MText.expandText("#times#") + "P(X";
		titleString += MText.expandText((lowProb <= highProb) ? "#le#" : "#ge#");
		
		titleString += observedX + ") = " + new NumValue(pValue, sigLevel.decimals).toString();
		
		g.setFont(getApplet().getBigBigBoldFont());
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int titleWidth = fm.stringWidth(titleString);
		g.drawString(titleString, (getWidth() - titleWidth) / 2, ascent + 2);
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		double lowProb = y.getCumulativeProb(observedX + 0.5);
		double highProb = 1 - y.getCumulativeProb(observedX - 0.5);
		double pValue = Math.min(lowProb, highProb) * 2;
		
		if (pValue < sigLevel.toDouble()) {
			g.setColor(kSignificantBackground);
			if (lowProb < highProb) {
				int xPos = xAxis.numValToRawPosition(observedX + 0.5);
				Point px = translateToScreen(xPos, 0, null);
				g.fillRect(0, 0, px.x, px.y + 1);
			}
			else {
				int xPos = xAxis.numValToRawPosition(observedX - 0.5);
				Point px = translateToScreen(xPos, 0, null);
				g.fillRect(px.x, 0, getSize().width, px.y + 1);
			}
		}
		
		int barSpacing = 0;
		try {
			int x0Pos = xAxis.numValToPosition(0.0);
			int x1Pos = xAxis.numValToPosition(1.0);
			barSpacing = x1Pos - x0Pos;
		} catch (AxisException e) {
			int x0Pos = xAxis.numValToRawPosition(xAxis.minOnAxis);
			int x1Pos = xAxis.numValToRawPosition(xAxis.minOnAxis + 1.0);
			barSpacing = x1Pos - x0Pos;
		}
		
		int halfBarWidth = (barSpacing >= 20) ? 2
								: (barSpacing >= 10) ? 1
								: 0;
		
		double maxY = xAxis.maxOnAxis;
		double probFactor = y.getProbFactor();
		Point topLeft = null;
		for (int i=0 ; i<=maxY ; i++)
			try {
				int xPos = xAxis.numValToPosition(i);
				double prob = y.getScaledProb(i) * probFactor;
				int probPos = probAxis.numValToPosition(prob);
				topLeft = translateToScreen(xPos, probPos, topLeft);
				
				if (lowProb < highProb && i <= observedX || lowProb >= highProb && i >= observedX) {
					g.setColor(pValue < sigLevel.toDouble() ? kSignificantColor : kNonSignificantColor);
				}
				else
					g.setColor(kDistnColor);
				
				g.fillRect(topLeft.x - halfBarWidth, topLeft.y + 1, 2 * halfBarWidth + 1,
																						getSize().height - topLeft.y - 1);
			} catch (AxisException e) {
			}
			
		drawTitleString(g);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}