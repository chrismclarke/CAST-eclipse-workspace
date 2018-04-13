package simulation;

import java.awt.*;

import dataView.*;
import axis.*;


public class CumBoxedView extends BoxedDotPlotView {
	
	private NumCatAxis cumAxis;
	private String oneRandKey;
	
	public CumBoxedView(DataSet theData, XApplet applet, NumCatAxis axis, NumCatAxis cumAxis,
								String yKey, String oneRandKey, double step) {
		super(theData, applet, axis, yKey, step);
		
		this.cumAxis = cumAxis;
		this.oneRandKey = oneRandKey;
		
		boxFillColor = new Color(0xCCCCCC);			//		light gray
		boxOutlineColor = new Color(0x999999);		//		gray
		boxHighlightColor = Color.blue;
	}
	
	protected int getCellVert() {
		return 0;
	}
	
	protected int vertForFreq(int i, int cellVert) {
		int vertPos = 0;
		try {
			vertPos = cumAxis.numValToPosition(i);
		} catch (AxisException e) {
		}
		return vertPos;
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		g.setColor(getForeground());
		Point p1 = translateToScreen(0, 0, null);
		Point p2 = null;
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ve = yVar.values();
		int groupIndex = 0;
		int cumulative = 0;
		boolean firstPoint = true;
		
		while (ve.hasMoreValues()) {
			RepeatValue rv = ve.nextGroup();
			cumulative += rv.count;
			int horizIndex = groupIndexOnAxis[groupIndex];
			p2 = translateToScreen(horizPos[horizIndex + 1], vertForFreq(cumulative, 0), p2);
			
			if (firstPoint)
				firstPoint = false;
			else
				g.drawLine(p1.x, p1.y, p2.x, p1.y);
			g.drawLine(p2.x, p1.y, p2.x, p2.y);
			
			Point temp = p1;
			p1 = p2;
			p2 = temp;
			groupIndex++;
		}
		g.drawLine(p1.x, p1.y, getSize().width, p1.y);
		
		OneRandomVariable rand = (OneRandomVariable)getVariable(oneRandKey);
		double randValue = rand.getPseudo();
		if (!Double.isNaN(randValue))
			try {
				int noOfValues = yVar.noOfValues();
				double randOnAxis = randValue * noOfValues;
				p1 = translateToScreen(0, cumAxis.numValToPosition(randOnAxis), p1);
				
				ve = yVar.values();
				groupIndex = 0;
				cumulative = 0;
				while (ve.hasMoreValues()) {
					RepeatValue rv = ve.nextGroup();
					cumulative += rv.count;
					if (randOnAxis <= cumulative) {
						int horizIndex = groupIndexOnAxis[groupIndex];
						p2 = translateToScreen(horizPos[horizIndex + 1], 0, p2);
						g.setColor(Color.red);
						g.drawLine(p1.x, p1.y, p2.x, p1.y);
						g.drawLine(p2.x - 1, p1.y, p2.x - 4, p1.y - 3);
						g.drawLine(p2.x - 1, p1.y, p2.x - 4, p1.y + 3);
						
						g.drawLine(p2.x, p1.y, p2.x, p2.y);
						g.drawLine(p2.x, p2.y, p2.x - 3, p2.y - 3);
						g.drawLine(p2.x, p2.y, p2.x + 3, p2.y - 3);
						
						break;
					}
					
					groupIndex++;
				}
			} catch (AxisException e) {
			}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
