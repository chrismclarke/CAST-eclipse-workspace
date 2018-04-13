package exerciseCateg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;

import exerciseCategProg.*;


public class ParetoReorderView extends CoreCumulativeView implements StatusInterface {
//	static public final String PARETO_REORDER_VIEW = "paretoReorder";
	
	private int perm[];
	
	private int dragIndex = -1;
	private int selectedIndex = -1;	
	private int hitOffset, dragDest;
	
	public ParetoReorderView(DataSet theData, XApplet applet,
												String yKey, HorizAxis valAxis, VertAxis leftAxis, VertAxis rightAxis) {
		super(theData, applet, yKey, valAxis, leftAxis, rightAxis);
	}
	
	public String getStatus() {
		String s = selectedIndex + "*";
		for (int i=0 ; i<perm.length ; i++)
			s += perm[i] + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString, "*");
		selectedIndex = Integer.parseInt(st.nextToken());
		
		st = new StringTokenizer(st.nextToken());
		for (int i=0 ; i<perm.length ; i++)
			perm[i] = Integer.parseInt(st.nextToken());
		
		repaint();
	}
	
	public void setPermutation(int[] perm) {
		this.perm = perm;
	}
	
	public int[] getCounts() {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int baseCounts[] = yVar.getCounts();
		int permCounts[] = new int[baseCounts.length];
		for (int i=0 ; i<baseCounts.length ; i++)
			permCounts[i] = baseCounts[perm[i]];
		return permCounts;
	}

//-------------------------------------------------------------------
	
	private void drawBar(Graphics g, Point pTop, int halfWidth, int barIndex) {
		int width = 2 * halfWidth;
		int height = getSize().height - pTop.y;
		boolean selected = barIndex == selectedIndex;
		boolean dragged = barIndex == dragIndex;
		
		if (height > 1) {
			g.setColor(selected ? kBarHiliteColour : dragged ? kBarFillColour : kBarDimFillColour);
			g.fillRect(pTop.x - halfWidth, pTop.y, width, height);
			
			Color barOutlineColor = kBarDimOutlineColour;
			if (selected || dragged)
				barOutlineColor = kBarOutlineColour;
			g.setColor(barOutlineColor);
			g.drawRect(pTop.x - halfWidth, pTop.y, width, height);
		}
		else if (selected) {
			g.setColor(kBarHiliteColour);
			g.fillOval(pTop.x - 8, pTop.y - kArrowHead - 18, 16, 16);
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g, 1.0);
		
		int count[] = getCounts();
		int total = 0;
		for (int i=0 ; i<count.length ; i++)
			total += count[i];
		
		Point p = null;
		int halfBarWidth = getHalfBarWidth();
		int nCats = valAxis.noOfCats;
		for (int i=0 ; i<count.length ; i++) {
			int barHt = leftAxis.numValToRawPosition(count[i] / (double)total);
			int xPos;
			if (dragIndex < 0)
				xPos = valAxis.catValToPosition(i);
			else if (dragIndex == i)
				xPos = Math.max(valAxis.catValToPosition(0), Math.min(valAxis.catValToPosition(nCats - 1), dragDest));
			else {
				double destIndex = Math.max(0.0, Math.min(nCats - 1, (dragDest * nCats) / (double)valAxis.axisLength - 0.5));
//				System.out.println("dragIndex = " + dragIndex + ", destIndex = " + destIndex + ", nCats = "
//																+ nCats + ", dragDest = " + dragDest + ", axisLength = " + valAxis.axisLength);
				if (destIndex < dragIndex) {
					int lowAffected = (int)Math.round(Math.floor(destIndex));
					if (i < lowAffected || i > dragIndex)
						xPos = valAxis.catValToPosition(i);
					else if (i > lowAffected)
						xPos = valAxis.catValToPosition(i + 1);
					else {
						double fraction = destIndex - lowAffected;
						xPos = (int)Math.round(fraction * valAxis.catValToPosition(i)
																									+ (1.0 - fraction) * valAxis.catValToPosition(i + 1));
					}
				}
				else {
					int highAffected = (int)Math.round(Math.ceil(destIndex));
					if (i < dragIndex || i > highAffected)
						xPos = valAxis.catValToPosition(i);
					else if (i < highAffected)
						xPos = valAxis.catValToPosition(i - 1);
					else {
						double fraction = highAffected - destIndex;
						xPos = (int)Math.round(fraction * valAxis.catValToPosition(i)
																													+ (1.0 - fraction) * valAxis.catValToPosition(i - 1));
					}
				}
			}
			p = translateToScreen(xPos, barHt, p);
			
			drawBar(g, p, halfBarWidth, i);
		}
		
		if (dragIndex < 0)
			drawCumulative(g, 1.0 / total);
		
		if (selectedIndex >= 0 && dragIndex < 0) {
			int cumCount = 0;
			for (int i=0 ; i<=selectedIndex ; i++)
				cumCount += count[i];
			drawPropns(g, selectedIndex, count[selectedIndex] / (double)total, cumCount / (double)total);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(yKey)) {
			dragIndex = selectedIndex = -1;
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
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (x < 0 || x >= getSize().width || y < 0 || y >= getSize().height)
			return null;
		
		Point p = translateFromScreen(x, y, null);
		int xCat = valAxis.positionToCatVal(p.x);
		return new HorizDragPosInfo(x, xCat, valAxis.catValToPosition(xCat) - x);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || x >= getSize().width || y < 0 || y >= getSize().height)
			return null;
		
		return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		selectedIndex = dragPos.index;
		hitOffset = dragPos.hitOffset;
		dragDest = dragPos.x + hitOffset;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			valAxis.setForeground(Color.black);
			valAxis.repaint();
			dragIndex = -1;
		}
		else {
			if (dragIndex < 0) {
				valAxis.setForeground(Color.gray);
				valAxis.repaint();
				dragIndex = selectedIndex;
			}
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			dragDest = dragPos.x + hitOffset;
		}
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragIndex >= 0) {
			int destIndex = valAxis.positionToCatVal(dragDest);
			moveBar(dragIndex, destIndex);
			selectedIndex = destIndex;
			valAxis.setForeground(Color.black);
			valAxis.repaint();
			dragIndex = -1;
		}
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	private void moveBar(int fromIndex, int toIndex) {
		int fromPerm = perm[fromIndex];
		if (fromIndex > toIndex) {
			for (int i=fromIndex ; i>toIndex ; i--)
				perm[i] = perm[i - 1];
			perm[toIndex] = fromPerm;
		}
		else if (fromIndex < toIndex) {
			for (int i=fromIndex ; i<toIndex ; i++)
				perm[i] = perm[i + 1];
			perm[toIndex] = fromPerm;
		}
		
		if (fromIndex != toIndex) {
			reorderAxis();
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	public void sortIntoOrder(int criticalIndex) {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int counts[] = yVar.getCounts();
		boolean changed = false;
		for (int i=1 ; i<counts.length ; i++) {
			for (int j=i-1 ; j>=0 ; j--)
				if (counts[perm[j]] < counts[perm[j + 1]]) {
					int temp = perm[j];
					perm[j] = perm[j + 1];
					perm[j + 1] = temp;
					changed = true;
				}
		}
		
		if (changed)
			reorderAxis();
		
		selectedIndex = -1;
		for (int i=0 ; i<counts.length ; i++)
			if (perm[i] == criticalIndex)
				selectedIndex = perm[i];
		repaint();
	}
	
	private void reorderAxis() {
		ParetoReorderApplet paretoApplet = (ParetoReorderApplet)getApplet();
		CatVariable yVar = (CatVariable)getVariable(yKey);
		paretoApplet.permuteAxisLabels(yVar, perm);
	}

}
	
