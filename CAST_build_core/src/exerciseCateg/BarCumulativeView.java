package exerciseCateg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import distribution.*;


public class BarCumulativeView extends CoreCumulativeView implements StatusInterface {
//	static public final String BAR_CUM_VIEW = "barCum";
		
	private boolean[] selectedBars = null;
	
	private int dragIndex = -1;
	
	public BarCumulativeView(DataSet theData, XApplet applet,
												String yKey, HorizAxis valAxis, VertAxis leftAxis, VertAxis cumAxis) {
		super(theData, applet, yKey, valAxis, leftAxis, cumAxis);
	}
	
	
	public String getStatus() {
		return String.valueOf(dragIndex);
	}
	
	public void setStatus(String statusString) {
		selectedBars = null;
		dragIndex = Integer.parseInt(statusString);
		repaint();
	}

//-------------------------------------------------------------------
	
	protected int[] getCounts() {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		return yVar.getCounts();
	}
	
	public void setSelectedBars(boolean[] selectedBars) {
		this.selectedBars = selectedBars;
		int nSelected = 0;
		int firstSelected = Integer.MAX_VALUE;
		int lastSelected = Integer.MIN_VALUE;
		int lastUnselected = Integer.MIN_VALUE;
		for (int i=0 ; i<selectedBars.length ; i++)
			if (selectedBars[i]) {
				nSelected ++;
				firstSelected = Math.min(firstSelected, i);
				lastSelected = Math.max(lastSelected, i);
			}
			else
				lastUnselected = i;
		if (nSelected == 1)
			dragIndex = firstSelected;
		else if (nSelected == selectedBars.length - 1)
			dragIndex = lastUnselected;
			
		else if (firstSelected == 0)
			dragIndex = lastSelected;
		else
			dragIndex = firstSelected - 1;
	}

//-------------------------------------------------------------------
	
	private void drawBar(Graphics g, Point pTop, int halfWidth, int barIndex) {
		int width = 2 * halfWidth;
		int height = getSize().height - pTop.y;
		if (height > 1) {
			boolean selected = selectedBars != null && selectedBars[barIndex];
			boolean dragged = barIndex == dragIndex;
			g.setColor(selected ? kBarHiliteColour : dragged ? kBarFillColour : kBarDimFillColour);
			g.fillRect(pTop.x - halfWidth, pTop.y, width, height);
			
			Color barOutlineColor = kBarDimOutlineColour;
			if ((selectedBars != null && selectedBars[barIndex]) || dragged)
				barOutlineColor = kBarOutlineColour;
			g.setColor(barOutlineColor);
			g.drawRect(pTop.x - halfWidth, pTop.y, width, height);
		}
		else if (selectedBars != null && selectedBars[barIndex]) {
			g.setColor(kBarHiliteColour);
			g.fillOval(pTop.x - 8, pTop.y - kArrowHead - 18, 16, 16);
		}
	}
	
	public void paintView(Graphics g) {
		int count[] = getCounts();
		int total = 0;
		for (int i=0 ; i<count.length ; i++)
			total += count[i];

		drawBackground(g, total);
		
		Point p = null;
		int halfBarWidth = getHalfBarWidth();
		for (int i=0 ; i<count.length ; i++) {
			int barHt = leftAxis.numValToRawPosition(count[i]);
			int xPos = valAxis.catValToPosition(i);			
			p = translateToScreen(xPos, barHt, p);
			
			drawBar(g, p, halfBarWidth, i);
		}
		
		drawCumulative(g, 1.0);
		
		if (dragIndex >= 0) {
			int cumCount = 0;
			for (int i=0 ; i<=dragIndex ; i++)
				cumCount += count[i];
			drawPropns(g, dragIndex, count[dragIndex], cumCount);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(yKey)) {
			selectedBars = null;
			dragIndex = -1;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || x >= getSize().width || y < 0 || y >= getSize().height)
			return null;
		
		Point p = translateFromScreen(x, y, null);
		int xCat = valAxis.positionToCatVal(p.x);
		return new DiscreteDragInfo(xCat);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		DiscreteDragInfo dragPos = (DiscreteDragInfo)startInfo;
		dragIndex = dragPos.xValue;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			dragIndex = -1;
		else {
			DiscreteDragInfo dragPos = (DiscreteDragInfo)toPos;
			dragIndex = dragPos.xValue;
		}
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
//		dragIndex = -1;
		repaint();
	}

}
	
