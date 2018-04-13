package residTwo;

import java.awt.*;

import dataView.*;
import axis.*;


public class PartialResidScatterView extends YChangeScatterView {
//	static public final String PARTIAL_RESID_PLOT = "partialResidPlot";
	
	private double partialSlope;
	
	public PartialResidScatterView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, VertAxis y2Axis, String xKey,
										String yKey, String y2Key, ColoredXZView linked3DView) {
		super(theData, applet, xAxis, yAxis, y2Axis, xKey, yKey, y2Key, linked3DView);
	}
	
	public void setPartialSlope(double partialSlope) {
		this.partialSlope = partialSlope;
	}
	
	private Point getLinePoint(double x) {
		int currentFrame = getCurrentFrame();
		if (!yOneToTwo)
			currentFrame = kFinalFrame - currentFrame;
		
		int vertPos = 0;
		int vertPos2 = 0;
		if (currentFrame != kFinalFrame)
			vertPos = yAxis.numValToRawPosition(0.0);
		if (currentFrame != 0)
			vertPos2 = y2Axis.numValToRawPosition(partialSlope * x);
		
		if (currentFrame == kFinalFrame)
			vertPos = vertPos2;
		else if (currentFrame > 0)
			vertPos = (vertPos2 * currentFrame + (kFinalFrame - currentFrame) * vertPos) / kFinalFrame;
		
		int horizPos = axis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, null);
	}
	
	protected void drawBackground(Graphics g) {
		g.setColor(Color.lightGray);
		
		double lowX = axis.minOnAxis;
		double highX = axis.maxOnAxis;
		double slop = (highX - lowX) * 0.1;
		lowX -= slop;
		highX += slop;
		
		Point p0 = getLinePoint(lowX);
		Point p1 = getLinePoint(highX);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		g.setColor(getForeground());
	}
}
	
