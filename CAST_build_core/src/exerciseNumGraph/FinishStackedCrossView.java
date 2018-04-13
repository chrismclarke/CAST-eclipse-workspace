package exerciseNumGraph;

import java.awt.*;

import dataView.*;


public class FinishStackedCrossView extends DragStackedCrossView {
	static final private Color kDragColor[] = {new Color(0x3333FF), new Color(0x008800), new Color(0x990000)};
	static public Color getDragColor(int index) {
		return kDragColor[index % kDragColor.length];
	}
	
	private int numberToDrag;
	private boolean draggedCross[];
	
	public FinishStackedCrossView(DataSet theData, XApplet applet, String yKey, String axisInfo) {
		super(theData, applet, yKey, axisInfo);
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
	
	private int findDragIndex(int index) {
		return index - stackIndex.length + numberToDrag;
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		showCrosses(numberToDrag);
	}
	
	protected void setSymbolRect(int axisPix, int offAxisPix, Rectangle r) {
		super.setSymbolRect(axisPix, offAxisPix, r);
		if (offAxisPix <= 0) {
			r.y = 10;
		}
	}
	
	protected Color getSymbolColor(int index) {
		int dragIndex = findDragIndex(index);
		if (dragIndex < 0)
			return super.getSymbolColor(index);
		else
			return getDragColor(dragIndex);
	}
	
	protected void drawSymbol(Graphics g, Rectangle r, Color crossColor, NumValue y, int index) {
		super.drawSymbol(g, r, crossColor, y, index);
		
		int dragIndex = findDragIndex(index);
		if (dragIndex >= 0)
			for (int i=0 ; i<2 ; i++)
				for (int j=0 ; j<2 ; j++) {			//	to make it bolder
					g.drawLine(r.x + 1 + i, r.y + j + 1, r.x + r.width + i - 3, r.y + r.height + j - 3);
					g.drawLine(r.x + 1 + i, r.y + r.height + j - 3, r.x + r.width + i - 3, r.y + j + 1);
				}
	}
	

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		IndexPosInfo hitPos = (IndexPosInfo)super.getInitialPosition(x, y);
		
		if (hitPos == null) {
			getData().clearSelection();
			return null;
		}
		else {
			int dragIndex = findDragIndex(hitPos.itemIndex);
			if (dragIndex >= 0)
				return hitPos;
			else {
				getData().setSelection(hitPos.itemIndex);
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
	
