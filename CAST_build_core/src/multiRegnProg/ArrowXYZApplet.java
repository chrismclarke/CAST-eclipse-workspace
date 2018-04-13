package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class ArrowXYZApplet extends RotateApplet {
	static final private Color kXAxisColor = D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND];
	static final private Color kYAxisColor = D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND];
	static final private Color kZAxisColor = D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND];
	
	protected String explanKey[] = {"x", "z"};
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		((NumVariable)data.getVariable("y")).setMaxDecimals();
		((NumVariable)data.getVariable("x")).setMaxDecimals();
		((NumVariable)data.getVariable("z")).setMaxDecimals();
							//		needed to make OneValueView always show same decimals
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(getParameter(Z_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			theView = new Dot3ArrowView(data, this, xAxis, yAxis, zAxis, explanKey, "y");
			
			int noOfValues = ((NumVariable)data.getVariable("y")).noOfValues();
			if (noOfValues > 50)
				theView.setCrossSize(DataView.SMALL_CROSS);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			OneValueView xView = new OneValueView(data, explanKey[0], this);
			xView.setForeground(kXAxisColor);
		thePanel.add(xView);
		
			OneValueView zView = new OneValueView(data, explanKey[1], this);
			zView.setForeground(kZAxisColor);
		thePanel.add(zView);
		
			OneValueView yView = new OneValueView(data, "y", this);
			yView.setForeground(kYAxisColor);
		thePanel.add(yView);
		
		return thePanel;
	}
}