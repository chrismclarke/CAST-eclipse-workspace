package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class PairingDotView extends DotPlotView {
	
	static final private Color kTopLineColor = new Color(0xDDDDDD);
	static final private Color kBandEvenColor = new Color(0xE8EFC3);
	static final private Color kBandOddColor = new Color(0xEECEE0);
	
	private int noInBlock;
	
	private boolean showPairBands = false;
	
	public PairingDotView(DataSet theData, XApplet applet, NumCatAxis theAxis, int noInBlock) {
		super(theData, applet, theAxis);
		this.noInBlock = noInBlock;
	}
	
	public void setShowPairBands(boolean showPairBands) {
		this.showPairBands = showPairBands;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		int horizPos = axis.numValToRawPosition(theVal.toDouble());
		Point p = translateToScreen(horizPos, 0, thePoint);
		
		if (showPairBands) {
			NumVariable yVar = getNumVariable();
			int rank = yVar.indexToRank(index);
			int group = rank % noInBlock;
			p.y = getSize().height * (noInBlock - group % noInBlock) / (noInBlock + 1);
		}
		else
			p.y = getSize().height / 2;
		
		return p;
	}
	
	protected void paintBackground(Graphics g) {
		if (showPairBands) {
			NumVariable yVar = getNumVariable();
			NumValue sortedX[] = yVar.getSortedData();
			
			g.setColor(kBandEvenColor);
			g.fillRect(0, 0, getSize().width, getSize().height);
			
			Point p = new Point(0, 0);
			g.setColor(kBandOddColor);
			
			for (int i=noInBlock-1 ; i+noInBlock<sortedX.length ; i+=2*noInBlock) {
				double lowShadeX = (sortedX[i].toDouble() + sortedX[i + 1].toDouble()) / 2;
				double nextBandStart = (i + noInBlock + 1 < sortedX.length) ? sortedX[i + noInBlock + 1].toDouble()
																									: 2 * axis.maxOnAxis - axis.minOnAxis;
				double highShadeX = (sortedX[i + noInBlock].toDouble() + nextBandStart) / 2;
				
				int lowXPos = axis.numValToRawPosition(lowShadeX);
				int highXPos = axis.numValToRawPosition(highShadeX);
				
				int px0 = translateToScreen(lowXPos, 0, p).x;
				int px1 = translateToScreen(highXPos, 0, p).x;
				
				g.fillRect(px0, 0, (px1 - px0), getSize().height);
			}
		}
		g.setColor(kTopLineColor);
		g.drawLine(0, 0, getSize().width, 0);
	}
	
	public void paintView(Graphics g) {
		paintBackground(g);
			
		super.paintView(g);
	}
}