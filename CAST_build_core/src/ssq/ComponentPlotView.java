package ssq;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ComponentPlotView extends DotPlotView {
	static final private int kArrowWidth = 6;
	
	private ComponentPanelInterface panel;
	private boolean selected;
	
	private boolean showSD = false;
	
	public ComponentPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, double initialJittering,
					boolean selected, ComponentPanelInterface panel) {
		super(theData, applet, theAxis, initialJittering);
		this.panel = panel;
		this.selected = selected;
	}
	
	public void setShowSD(boolean showSD) {
		this.showSD = showSD;
	}
	
	public void setSelected(boolean selected) {
		if (this.selected != selected) {
			this.selected = selected;
			repaint();
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable v = getNumVariable();
		
		int zeroPos = axis.numValToRawPosition(0.0);
		int zeroOnScreen = translateToScreen(zeroPos, 0, null).y;
		g.setColor(Color.lightGray);
		g.drawLine(0, zeroOnScreen, getSize().width - 1, zeroOnScreen);
		
		if (selected) {
			g.setColor(Color.yellow);
			g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
			g.drawRect(1, 1, getSize().width - 3, getSize().height - 3);
			g.drawRect(2, 2, getSize().width - 5, getSize().height - 5);
		}
		g.setColor(getForeground());
		
		if (showSD) {
			ValueEnumeration e = v.values();
			double sx = 0.0;
			double sxx = 0.0;
			int n = 0;
			while (e.hasMoreValues()) {
				double x = e.nextDouble();
				if (!Double.isNaN(x)) {
					sx += x;
					sxx += x * x;
					n++;
				}
			}
			double m = sx / n;
			int df = n;
//			int df = n - 1;
//			if (v instanceof CoreComponentVariable)				//	draws arrow too big for 1df between means
//				df = ((CoreComponentVariable)v).getDF();
			double s = Math.sqrt((sxx - sx * m) / df);
			int lowPos = axis.numValToRawPosition(m - 2.0 * s);
			int lowOnScreen = translateToScreen(lowPos, 0, null).y;
			int highPos = axis.numValToRawPosition(m + 2.0 * s);
			int highOnScreen = translateToScreen(highPos, 0, null).y;
			int horizCenter = getSize().width - kArrowWidth - 2;
			
			g.drawLine(horizCenter, highOnScreen, horizCenter, lowOnScreen);
			if (lowOnScreen - highOnScreen > 3) {
				g.drawLine(horizCenter - 1, highOnScreen + 1, horizCenter - 1, lowOnScreen - 1);
				g.drawLine(horizCenter + 1, highOnScreen + 1, horizCenter + 1, lowOnScreen - 1);
				
				for (int i=2 ; i<Math.min(kArrowWidth, (lowOnScreen - highOnScreen - 1) / 2) ; i++) {
					g.drawLine(horizCenter - i, highOnScreen + i, horizCenter + i, highOnScreen + i);
					g.drawLine(horizCenter - i, lowOnScreen - i, horizCenter + i, lowOnScreen - i);
				}
			}
		}
		
		super.paintView(g);
	}
	
	protected int getMaxJitter() {
		return super.getMaxJitter() - 2 * kArrowWidth - 4 + getDisplayBorderAwayAxis();
	}

//-----------------------------------------------------------------------------------
	
	public void mousePressed(MouseEvent e) {
		if (panel != null)
			panel.actionComponentSelected(this);
		super.mousePressed(e);
	}
	
}
	
