package percentile;

import java.awt.*;

import dataView.*;
import axis.*;


public class CumFunctDotPlotView extends CumDotBoxPlotView {
//	static public final String CUM_FUNCT_DOT_PLOT = "cumFunctDot";
	
	static final private Color kHiliteCumBackground = new Color(0xFFFFBB);
	static final private Color kHiliteCumDotBackground = new Color(0xD2D8B0);
	
	protected int cumEvaluateType = PercentileInfo.STEP;
	
	protected VertAxis cumProbAxis;
	
	public CumFunctDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis,
													DataSet refData, String refKey, int inequality, VertAxis cumProbAxis) {
																							//	inequality as defined in PropnConstants
		super(theData, applet, theAxis, refData, refKey, inequality);
		this.cumProbAxis = cumProbAxis;
	}
	
	public void setCumEvaluateType(int cumEvaluateType) {
		this.cumEvaluateType = cumEvaluateType;
		repaint();
	}
	
	protected Color getTopHiliteBackground() {
		return kHiliteCumBackground;
	}
	
	protected Color getBottomHiliteBackground() {
		return kHiliteCumDotBackground;
	}
	
	protected void drawCumArrow(Graphics g, NumValue[] sortedData, double ref, int refHorizPos) {
		double refCum = 0.0;
		int n = sortedData.length;
		for (int i=0 ; i<n ; i++) {
			double x = sortedData[i].toDouble();
			if (x < ref)
				refCum = (i + 1) / (double)n;
		}
		g.setColor(Color.red);
		int refCumPos = cumProbAxis.numValToRawPosition(refCum);
		Point p = translateToScreen(refHorizPos, refCumPos, null);
		g.drawLine(0, p.y, p.x, p.y);
		for (int i=1 ; i<5 ; i++)
			g.drawLine(i, p.y - i, i, p.y + i);
	}
	
	protected void drawTopPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		NumValue sortedData[] = getNumVariable().getSortedData();
		
		g.setColor(Color.gray);
		
		Point p0 = null;
		Point p1 = null;
		
		int zeroPos = cumProbAxis.numValToRawPosition(0.0);
		int onePos = cumProbAxis.numValToRawPosition(1.0);
		int xMinPos = axis.numValToRawPosition(axis.minOnAxis);
		int xMaxPos = axis.numValToRawPosition(axis.maxOnAxis);
		p0 = translateToScreen(xMinPos, zeroPos, p0);
		p1 = translateToScreen(xMaxPos, zeroPos, p1);
		g.drawLine(0, p0.y, getSize().width, p1.y);
		p0 = translateToScreen(xMinPos, onePos, p0);
		p1 = translateToScreen(xMaxPos, onePos, p1);
		g.drawLine(0, p0.y, getSize().width, p1.y);
		
		g.setColor(Color.blue);
		int n = sortedData.length;
		if (cumEvaluateType == PercentileInfo.STEP) {
			p1 = translateToScreen(0, zeroPos, p1);
			p1.x = 0;
			double cum = 0.0;
			for (int i=0 ; i<n ; i++) {
				double x = sortedData[i].toDouble();
				int xPos = axis.numValToRawPosition(x);
				int cumPos = cumProbAxis.numValToRawPosition(cum);
				p0 = translateToScreen(xPos, cumPos, p0);
				g.drawLine(p1.x, p1.y, p0.x, p0.y);
				cum = (i + 1) / (double)n;
				cumPos = cumProbAxis.numValToRawPosition(cum);
				p1 = translateToScreen(xPos, cumPos, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			
			p0 = translateToScreen(xMaxPos, onePos, p0);
			g.drawLine(p1.x, p1.y, getSize().width, p0.y);
		}
		else {
			double x = sortedData[0].toDouble();
			int xPos = axis.numValToRawPosition(x);
			int cumPos = cumProbAxis.numValToRawPosition(0.0);
			p0 = translateToScreen(xPos, cumPos, p0);
			g.drawLine(0, p0.y, p0.x, p0.y);
			
			double cum = 1.0 / (n + 1);
			cumPos = cumProbAxis.numValToRawPosition(cum);
			p1 = translateToScreen(xPos, cumPos, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			for (int i=1 ; i<n ; i++) {
				Point pTemp = p1; p1 = p0 ; p0 = pTemp;
				x = sortedData[i].toDouble();
				xPos = axis.numValToRawPosition(x);
				cum = (i + 1.0) / (n + 1);
				cumPos = cumProbAxis.numValToRawPosition(cum);
				p1 = translateToScreen(xPos, cumPos, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			
			cumPos = cumProbAxis.numValToRawPosition(1.0);
			p0 = translateToScreen(xPos, cumPos, p0);
			g.drawLine(p1.x, p1.y, p0.x, p0.y);
			
			g.drawLine(p0.x, p0.y, getSize().width, p0.y);
		}
		
		if (allowDrag)
			drawCumArrow(g, sortedData, ref, refHorizPos);
	}
	
}
	
