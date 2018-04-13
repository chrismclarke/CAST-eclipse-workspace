package simulation;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;



public class RandomCatGeneratorView extends DotPlotView {
	static final private Color kPink = new Color(0xFF99CC);
	static final private Color kPaleBlue = new Color(0x6699FF);
	static final private Color[] probBackground = {kPaleBlue, kPink, Color.yellow};
	
	static public Color getBackgroundColor(int colorIndex) {
		return probBackground[colorIndex];
	}
	
	public RandomCatGeneratorView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis, 1.0);
		setCrossSize(LARGE_CROSS);
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		PseudoRandCatVariable catProbVar = (PseudoRandCatVariable)getCatVariable();
		double cumProb[] = catProbVar.getCumProbs();
		
		Flags selection = getSelection();
		if (variable.noOfValues() > 0)
			selection.setFlag(variable.noOfValues() - 1);
		
		Point topLeftPoint = null;
		Point bottomRightPoint = null;
		
		try {
			int endPos = axis.numValToPosition(1.0);
			int startPos = 0;
			topLeftPoint = translateToScreen(endPos, -getDisplayBorderNearAxis() - 1, topLeftPoint);
			bottomRightPoint = translateToScreen(startPos, getDisplayWidth() + getDisplayBorderAwayAxis(),
																									bottomRightPoint);
			
			g.setColor(Color.white);
			
			int lowX = Math.min(topLeftPoint.x, bottomRightPoint.x);
			int highX = Math.max(topLeftPoint.x, bottomRightPoint.x);
			int lowY = Math.min(topLeftPoint.y, bottomRightPoint.y);
			int highY = Math.max(topLeftPoint.y, bottomRightPoint.y);
			g.fillRect(lowX, lowY, highX-lowX, highY-lowY);
		} catch (AxisException ex) {
		}
		
		for (int i=0 ; i<cumProb.length ; i++)
			try {
				int endPos = axis.numValToPosition(cumProb[i]);
				int startPos = (i == 0) ? 0 : axis.numValToPosition(cumProb[i - 1]);
				topLeftPoint = translateToScreen(endPos, -getDisplayBorderNearAxis() - 1, topLeftPoint);
				bottomRightPoint = translateToScreen(startPos, getDisplayWidth() + getDisplayBorderAwayAxis(),
																										bottomRightPoint);
				
				g.setColor(probBackground[i]);
				int lowX = Math.min(topLeftPoint.x, bottomRightPoint.x);
				int highX = Math.max(topLeftPoint.x, bottomRightPoint.x);
				int lowY = Math.min(topLeftPoint.y, bottomRightPoint.y);
				int highY = Math.max(topLeftPoint.y, bottomRightPoint.y);
				g.fillRect(lowX, lowY, highX-lowX, highY-lowY);
			} catch (AxisException ex) {
			}
		
		if (variable.noOfValues() > 0) {
			g.setColor(Color.red);
			
			Point lastPoint = getScreenPoint(variable.noOfValues() - 1,
											(NumValue)variable.valueAt(variable.noOfValues() - 1), null);
			if (vertNotHoriz)
				g.drawLine(0, lastPoint.y, getSize().width, lastPoint.y);
			else
				g.drawLine(lastPoint.x, 0, lastPoint.x, getSize().height);
			
			g.setColor(getForeground());
		}
		
		super.paintView(g);
	}
}