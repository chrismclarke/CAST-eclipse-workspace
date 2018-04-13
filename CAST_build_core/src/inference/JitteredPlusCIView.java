package inference;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import coreSummaries.*;


public class JitteredPlusCIView extends JitterPlusNormalView {
	
//	private double tValue;
	private String ciKey;
	
	public JitteredPlusCIView(DataSet theData, XApplet applet, NumCatAxis theAxis, String normalKey,
									String ciKey, double tValue) {
		super(theData, applet, theAxis, normalKey, 0.0);
//		this.tValue = tValue;
		this.ciKey = ciKey;
	}
	
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		
		MeanCIVariable ci = (MeanCIVariable)getVariable(ciKey);
		IntervalValue ciValue = (IntervalValue)ci.valueAt(0);
		
		int lowPos = axis.numValToRawPosition(ciValue.lowValue.toDouble());
		int highPos = axis.numValToRawPosition(ciValue.highValue.toDouble());
		
		g.setColor(Color.red);
		Point lowPoint = translateToScreen(lowPos, 5, null);
		Point highPoint = translateToScreen(highPos + 1, 8, null);
		g.fillRect(lowPoint.x, highPoint.y, highPoint.x - lowPoint.x, lowPoint.y - highPoint.y);
		
		lowPoint = translateToScreen(lowPos, 3, lowPoint);
		highPoint = translateToScreen(lowPos + 1, 10, highPoint);
		g.fillRect(lowPoint.x, highPoint.y, highPoint.x - lowPoint.x, lowPoint.y - highPoint.y);
		
		lowPoint = translateToScreen(highPos, 3, lowPoint);
		highPoint = translateToScreen(highPos + 1, 10, highPoint);
		g.fillRect(lowPoint.x, highPoint.y, highPoint.x - lowPoint.x, lowPoint.y - highPoint.y);
		
		g.setColor(getForeground());
	}
	
	protected boolean canDrag() {
		return false;
	}
}