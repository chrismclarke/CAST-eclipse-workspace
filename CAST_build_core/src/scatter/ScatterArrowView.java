package scatter;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ScatterArrowView extends ScatterView {
//	static public final String SCATTER_ARROW_PLOT = "scatterArrowPlot";
	
	static final public Color kHorizAxisColor = Color.blue;
	static final public Color kVertAxisColor = new Color(0x009900);
	static final public Color kZeroColor = new Color(0xEEEEEE);
	
	
	private Color xColor, yColor;
	
	private boolean joinPoints = false;
	private boolean drawXZero = false, drawYZero = false;
	
	public ScatterArrowView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		yColor = kVertAxisColor;
		xColor = kHorizAxisColor;
	}
	
	public void setAxisColors(Color xColor, Color yColor) {
		this.yColor = yColor;
		this.xColor = xColor;
	}
	
	public void setJoinPoints(boolean joinPoints) {
		this.joinPoints = joinPoints;
	}
	
	public void setDrawXZero(boolean drawXZero) {
		this.drawXZero = drawXZero;
	}
	
	public void setDrawYZero(boolean drawYZero) {
		this.drawYZero = drawYZero;
	}
	
	public void paintView(Graphics g) {
		Point p1 = null;
		g.setColor(kZeroColor);
		if (drawXZero) {
			int xZeroPos = axis.numValToRawPosition(0.0);
			p1 = translateToScreen(xZeroPos, 0, p1);
			g.drawLine(p1.x, 0, p1.x, getSize().height);
		}
		if (drawYZero) {
			int yZeroPos = yAxis.numValToRawPosition(0.0);
			p1 = translateToScreen(0, yZeroPos, p1);
			g.drawLine(0, p1.y, getSize().width, p1.y);
		}
		g.setColor(getForeground());
		
		NumVariable variable = getNumVariable();
		ValueEnumeration e = variable.values();
		int index = 0;
		
		if (joinPoints) {
			Point p2 = null;
			g.setColor(Color.gray);
			while (e.hasMoreValues()) {
				Point pTemp = p1;
				p1 = p2;
				p2 = pTemp;
				NumValue nextVal = (NumValue)e.nextValue();
				p1 = getScreenPoint(index, nextVal, p1);
				if (p1 != null && p2 != null)
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				index++;
			}
			e = variable.values();
			index = 0;
		}
		
		FlagEnumeration fe = getSelection().getEnumeration();
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				p1 = getScreenPoint(index, nextVal, p1);
				if (p1 != null) {
					g.setColor(xColor);
					g.drawLine(p1.x, p1.y - 1, p1.x, getSize().height - 1);
					g.drawLine(p1.x - 1, p1.y - 1, p1.x - 1, getSize().height - 2);
					g.drawLine(p1.x + 1, p1.y - 1, p1.x + 1, getSize().height - 2);
					for (int i=2 ; i<=4 ; i++)
						g.drawLine(p1.x - i, getSize().height - i - 1, p1.x + i, getSize().height - i - 1);
					
					g.setColor(yColor);
					g.drawLine(0, p1.y, p1.x + 1, p1.y);
					g.drawLine(1, p1.y - 1, p1.x + 1, p1.y - 1);
					g.drawLine(1, p1.y + 1, p1.x + 1, p1.y + 1);
					for (int i=2 ; i<=4 ; i++)
						g.drawLine(i, p1.y - i, i, p1.y + i);
					
					g.setColor(getForeground());
				}
			}
			index++;
		}
		super.paintView(g);
	}
}
	
