package residTwo;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import coreGraphics.*;


public class YChangeScatterView extends ScatterView {
//	static public final String Y_CHANGE_SCATTER_PLOT = "yChangeScatter";
	
	static protected final int kFinalFrame = 40;
	static private final int kFrameRate = 10;
	
	protected VertAxis y2Axis;
	private String y2Key;
	
	protected boolean yOneToTwo = true;
	private XChoice yVarChoice = null;
	
	private NumVariable colorVar = null;
	private ColoredXZView linked3DView;
	
	public YChangeScatterView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, VertAxis y2Axis, String xKey, String yKey,
										String y2Key, ColoredXZView linked3DView) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.y2Axis = y2Axis;
		this.y2Key = y2Key;
		this.linked3DView = linked3DView;
	}
	
	public void setColorKey(String colorKey) {
		colorVar = (colorKey == null) ? null : (NumVariable)getVariable(colorKey);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		int currentFrame = getCurrentFrame();
		if (!yOneToTwo)
			currentFrame = kFinalFrame - currentFrame;
		
		int vertPos = 0;
		int vertPos2 = 0;
		if (currentFrame != kFinalFrame) {
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			NumValue yVal = (NumValue)(yVariable.valueAt(index));
			vertPos = yAxis.numValToRawPosition(yVal.toDouble());
		}
		if (currentFrame != 0) {
			NumVariable y2Variable = (NumVariable)getVariable(y2Key);
			NumValue y2Val = (NumValue)(y2Variable.valueAt(index));
			vertPos2 = y2Axis.numValToRawPosition(y2Val.toDouble());
		}
		
		if (currentFrame == kFinalFrame)
			vertPos = vertPos2;
		else if (currentFrame > 0)
			vertPos = (vertPos2 * currentFrame + (kFinalFrame - currentFrame) * vertPos) / kFinalFrame;
		
		int horizPos = axis.numValToRawPosition(theVal.toDouble());
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected void fiddleColor(Graphics g, int itemIndex) {
		if (colorVar != null) {
			Color pointColor = linked3DView.getPointColor(colorVar.doubleValueAt(itemIndex));
			if (pointColor != null)
				g.setColor(pointColor);
		}
	}
	
	protected void drawBackground(Graphics g) {
		boolean showingResid = yOneToTwo && (getCurrentFrame() == kFinalFrame)
																							|| !yOneToTwo && (getCurrentFrame() == 0);
		if (showingResid) {
			g.setColor(Color.lightGray);
			
			int zeroPos = y2Axis.numValToRawPosition(0.0);
			Point p = translateToScreen(0, zeroPos, null);
			g.drawLine(0, p.y, getSize().width, p.y);
			g.setColor(getForeground());
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
		if (getCurrentFrame() == kFinalFrame && yVarChoice != null) {
			yVarChoice.enable();
			yVarChoice = null;
		}
	}
	
	public void animateChange(boolean yOneToTwo, XChoice yVarChoice) {
		yVarChoice.disable();
		this.yVarChoice = yVarChoice;
		this.yOneToTwo = yOneToTwo;
		animateFrames(0, kFinalFrame, kFrameRate, null);
	}
	
	public void suddenChange(boolean yOneToTwo) {
		if (getCurrentFrame() > 0 && this.yOneToTwo != yOneToTwo) {
			this.yOneToTwo = yOneToTwo;
			setFrame(kFinalFrame);		//	will be redrawn by changeVariables() later
		}
	}
}
	
