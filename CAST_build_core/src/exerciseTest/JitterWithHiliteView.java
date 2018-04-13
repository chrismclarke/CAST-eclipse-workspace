package exerciseTest;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class JitterWithHiliteView extends DotPlotView implements BackgroundHiliteInterface {
	private boolean hiliteBackground = false;
	
	protected double highlightVal = Double.NEGATIVE_INFINITY;
	protected int highlightSide = LOW_HIGHLIGHT;
	private Color highlightBackgroundColor = kPaleRedColor;
	
	public JitterWithHiliteView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey) {
		super(theData, applet, theAxis, 1.0);
		setActiveNumVariable(yKey);
//		addComponentListener(this);
	}
	
	public void setCrossHighlight(double highlightVal, int highlightSide) {
		this.highlightVal = highlightVal;
		this.highlightSide = highlightSide;
	}
	
	public void setHighlightBackground(boolean hiliteBackground) {
		this.hiliteBackground = hiliteBackground;
	}
	
	public void setHighlightColor(Color highlightBackgroundColor) {
		this.highlightBackgroundColor = highlightBackgroundColor;
	}
	
	protected int groupIndex(int itemIndex) {
		NumVariable variable = getNumVariable();
		double value = variable.doubleValueAt(itemIndex);
		
		return ((value <= highlightVal) == (highlightSide == LOW_HIGHLIGHT)) ? 2 : 0;
	}
	
	protected void drawBackground(Graphics g) {
		if (hiliteBackground) {
			int borderPos = axis.numValToRawPosition(highlightVal);
			Point p = translateToScreen(borderPos, 0, null);
			
			g.setColor(highlightBackgroundColor);
			if (highlightSide == LOW_HIGHLIGHT)
				g.fillRect(0, 0, p.x, getSize().height);
			else
				g.fillRect(p.x, 0, getSize().width, getSize().height);
			g.setColor(Color.red);
			g.drawLine(p.x, 0, p.x, getSize().height);
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		super.paintView(g);
	}
}