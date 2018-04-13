package multiRegn;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class ColoredLinearEqnView extends MultiLinearEqnView {
	
	static final private Color kXAxisColor = D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND];
	static final private Color kYAxisColor = D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND];
	static final public Color kZAxisColor = D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND];
	static final public Color kExtraColor = new Color(0x009900);
	
	private int selectedParam = -1;
	
	private Color paramColor[];
	
	public ColoredLinearEqnView(DataSet theData, XApplet applet, String modelKey, String yName, String[] xName,
								NumValue[] minParam, NumValue[] maxParam) {
		super(theData, applet, modelKey, yName, xName, minParam, maxParam);
		paramColor = new Color[minParam.length];
		paramColor[0] = kYAxisColor;
		paramColor[1] = kXAxisColor;
		paramColor[2] = kZAxisColor;
		for (int i=3 ; i<paramColor.length ; i++)
			paramColor[i] = kExtraColor;
	}
	
	public void setSelectedParam(int paramIndex) {
		selectedParam = paramIndex;
		repaint();
	}
	
	public void setParamColor(Color c, int paramIndex) {
		paramColor[paramIndex] = c;
	}
	
//------------------------------------------------------------
	
	protected boolean parameterSelected(int paramIndex) {
		return paramIndex == selectedParam;
	}
	
	protected Color getParamColor(int paramIndex) {
		return paramColor[paramIndex];
	}
}
