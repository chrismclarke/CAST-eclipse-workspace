package mixtureProg;

import java.awt.*;

import dataView.*;
import graphics3D.*;

import multivarProg.*;
import mixture.*;


public class TriangleConstraintApplet extends RotateApplet {
	private String kPercentString;
	
	public void setupApplet() {
		kPercentString = translate("Proportion of") + " ";
		super.setupApplet();
	}
		
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			String xName = getParameter(X_VAR_NAME_PARAM);
			D3Axis xAxis = new D3Axis(kPercentString + xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			String yName = getParameter(Y_VAR_NAME_PARAM);
			D3Axis yAxis = new D3Axis(kPercentString + yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			String zName = getParameter(Z_VAR_NAME_PARAM);
			D3Axis zAxis = new D3Axis(kPercentString + zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			theView = new TriangleConstraintView(data, this, xAxis, yAxis, zAxis, xName, yName, zName);
			theView.lockBackground(Color.white);
			theView.setBigHitRadius();
			theView.setCrossSize(DataView.LARGE_CROSS);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
}