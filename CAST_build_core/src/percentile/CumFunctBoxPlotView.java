package percentile;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class CumFunctBoxPlotView extends CumFunctDotPlotView implements BoxPlotConstants {
//	static public final String CUM_FUNCT_BOX_PLOT = "cumFunctBox";
	
	static final private Color kGreen = new Color(0x009900);
	
	public CumFunctBoxPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis,
										DataSet refData, String refKey, int inequality, VertAxis cumProbAxis) {
																							//	inequality as defined in PropnRangeView
		super(theData, applet, theAxis, refData, refKey, inequality, cumProbAxis);
	}
	
	private void drawLineToGraph(Graphics g, double propn, double x) {
		int propnPos = cumProbAxis.numValToRawPosition(propn);
		int xPos = axis.numValToRawPosition(x);
		Point p = translateToScreen(xPos, propnPos, null);
		g.drawLine(0, p.y, p.x, p.y);
		g.drawLine(p.x, p.y, p.x, getSize().height - getViewBorder().bottom);
	}
	
	protected void drawTopPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		NumValue sortedData[] = getNumVariable().getSortedData();
		boxInfo.initialiseBox(sortedData, true, axis);
		
		g.setColor(kGreen);
		drawLineToGraph(g, 0.25, boxInfo.boxVal[LOW_QUART]);
		drawLineToGraph(g, 0.5, boxInfo.boxVal[MEDIAN]);
		drawLineToGraph(g, 0.75, boxInfo.boxVal[HIGH_QUART]);
		
		super.drawTopPanel(g, yVar, ref, refHorizPos);
	}
	
	private void drawArrowToBox(Graphics g, BoxInfo boxInfo, int boxPos) {
		Point p = translateToScreen(boxPos, boxInfo.boxBottom + boxInfo.getBoxHeight(), null);
		p.y -= 2;
		g.drawLine(p.x, getSize().height - getViewBorder().bottom, p.x, p.y);
		for (int i=1 ; i<5 ; i++)
			g.drawLine(p.x - i, p.y - i, p.x + i, p.y - i);
	}
	
	protected void drawBottomPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		NumValue sortedData[] = getNumVariable().getSortedData();
		
		boxInfo.initialiseBox(sortedData, true, axis);
		boxInfo.vertMidLine = -getViewBorder().bottom / 2;
		boxInfo.boxBottom = boxInfo.vertMidLine - boxInfo.getBoxHeight() / 2;
		
		g.setColor(getForeground());
		boxInfo.drawBoxPlot(g, this, yVar.getSortedData(), axis);
		
		g.setColor(kGreen);
		drawArrowToBox(g, boxInfo, boxInfo.boxPos[LOW_QUART]);
		drawArrowToBox(g, boxInfo, boxInfo.boxPos[MEDIAN]);
		drawArrowToBox(g, boxInfo, boxInfo.boxPos[HIGH_QUART]);
		
	}
	
}
	
