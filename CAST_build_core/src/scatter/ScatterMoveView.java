package scatter;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;


public class ScatterMoveView extends ScatterView {
//	static public final String SCATTER_MOVE_PLOT = "scatterMovePlot";
	
	static public final int kMaxFrame = 20;
	
	private String x2Key, y2Key;
	
	public ScatterMoveView(DataSet theData, XApplet applet, HorizAxis xAxis,
								VertAxis yAxis, String xKey, String yKey, String x2Key, String y2Key) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.x2Key = x2Key;
		this.y2Key = y2Key;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		try {
			double y = getY(index);
			double x = getX(index, theVal);
			int vertPos = yAxis.numValToPosition(y);
			int horizPos = axis.numValToPosition(x);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected double getX(int index, NumValue theVal) {
		NumVariable x2Variable = (NumVariable)getVariable(x2Key);
		double x2 = x2Variable.doubleValueAt(index);
		return (getCurrentFrame() * x2 + (kMaxFrame - getCurrentFrame()) * theVal.toDouble()) / kMaxFrame;
	}
	
	protected double getY(int index) {
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		double y = yVariable.doubleValueAt(index);
		NumVariable y2Variable = (NumVariable)getVariable(y2Key);
		double y2 = y2Variable.doubleValueAt(index);
		return (getCurrentFrame() * y2 + (kMaxFrame - getCurrentFrame()) * y) / kMaxFrame;
	}

//-----------------------------------------------------------------------------------
	
	public void doAnimation(XSlider controller) {
		animateFrames(1, kMaxFrame - 1, 8, controller);
	}
}
	
