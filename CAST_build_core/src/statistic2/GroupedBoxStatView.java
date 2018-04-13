package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

import boxPlot.*;


public class GroupedBoxStatView extends GroupedBoxView {
	static private final int kLineExtra = 5;
	
	private CatCenterView centerStat = null;
	private CatSpreadView spreadStat = null;
	
	public GroupedBoxStatView(DataSet theData, XApplet applet, NumCatAxis theAxis, NumCatAxis groupAxis) {
		super(theData, applet, theAxis, groupAxis);
	}
	
	public void setLinkedStats(CatCenterView centerStat, CatSpreadView spreadStat) {
		this.centerStat = centerStat;
		this.spreadStat = spreadStat;
	}
	
	protected void paintGroupBackground(Graphics g, NumVariable variable, BoxInfo theBoxInfo,
																										int catNo) {
		Color oldColor = g.getColor();
		if (spreadStat != null) {
			g.setColor(Color.lightGray);
			SpreadLimits sl = spreadStat.findSpreadLimits(variable, theBoxInfo);
			
			try {
				int horizPos1 = axis.numValToPosition(sl.low);
				int horizPos2 = axis.numValToPosition(sl.high);
				int vertPos =  groupAxis.catValToPosition(catNo);
				Point p1 = translateToScreen(horizPos1, vertPos, null);
				Point p2 = translateToScreen(horizPos2, vertPos, null);
				
				int lineOffset = currentJitter / 2 + kLineExtra;
				g.fillRect(p1.x, p1.y - lineOffset, p2.x - p1.x, 2 * lineOffset);
			} catch (AxisException e) {
			}
		}
		
		if (centerStat != null) {
			g.setColor(Color.blue);
			
			try {
				int horizPos = axis.numValToPosition(centerStat.evaluateStat(variable, theBoxInfo));
				int vertPos =  groupAxis.catValToPosition(catNo);
				Point p = translateToScreen(horizPos, vertPos, null);
				
				int lineOffset = currentJitter / 2 + kLineExtra;
				g.drawLine(p.x, p.y - lineOffset, p.x, p.y + lineOffset - 1);
			} catch (AxisException e) {
			}
		}
		g.setColor(oldColor);
	}
}