package exerciseBivar;

import java.awt.*;

import dataView.*;
import axis.*;

import exerciseNumGraph.*;


public class FinishScatterView extends DragScatterView {
	
	private int numberToDrag;
	private boolean draggedCross[];
	
	public FinishScatterView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
	}
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<draggedCross.length ; i++)
			s += draggedCross[i] ? "t" : "f";
		
		return s + "*" + super.getStatus();
	}
	
	public void setStatus(String statusString) {
		for (int i=0 ; i<draggedCross.length ; i++)
			draggedCross[i] = statusString.charAt(i) == 't';
		int firstStarPos = statusString.indexOf("*");
		super.setStatus(statusString.substring(firstStarPos + 1));
	}
	
	public void setDragCount(int numberToDrag) {
		this.numberToDrag = numberToDrag;
		draggedCross = new boolean[numberToDrag];		//	all false
	}
	
	public boolean draggedAllCrosses() {
		for (int i=0 ; i<draggedCross.length ; i++)
			if (!draggedCross[i])
				return false;
		return true;
	}
	
	public void showCorrectCrosses() {
		super.showCorrectCrosses();
		for (int i=0 ; i<draggedCross.length ; i++)
			draggedCross[i] = true;
	}
	
//-----------------------------------------------------------------
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		showCrosses(numberToDrag);
	}
	
	private int findDragIndex(int index) {
		return index - yPos.length + numberToDrag;
	}
	
	protected Color getSymbolColor(int index) {
		int dragIndex = findDragIndex(index);
		if (dragIndex < 0)
			return super.getSymbolColor(index);
		else
			return FinishStackedCrossView.getDragColor(dragIndex);
	}
	
	protected void drawSymbol(Graphics g, Point p, int index) {
		int dragIndex = findDragIndex(index);
		if (dragIndex >= 0) {
			Point p0 = new Point(p);
			for (int i=0 ; i<3 ; i++) {
				p0.x = p.x - 1 + i;
				for (int j=0 ; j<3 ; j++) {			//	to make it bolder
					p0.y = p.y - 1 + j;
					drawCross(g, p0);
				}
			}
		}
		else
			drawCross(g, p);
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		ScatterPosInfo hitPos = (ScatterPosInfo)super.getInitialPosition(x, y);
		
		if (hitPos == null) {
			getData().clearSelection();
			return null;
		}
		else {
			int dragIndex = findDragIndex(hitPos.index);
			if (dragIndex >= 0)
				return hitPos;
			else {
				getData().setSelection(hitPos.index);
				return null;
			}
		}
	}
	
	protected void setDoneDrag(int index) {
		super.setDoneDrag(index);
		int dragIndex = findDragIndex(index);
		if (dragIndex >= 0)
			draggedCross[dragIndex] = true;
	}
	
}
	
