package curveInteractProg;

import java.awt.*;

import dataView.*;
import graphics3D.*;

import multiRegn.*;
import curveInteract.*;


public class DragQuadGridApplet extends DragQuadXZApplet {
	
	protected void setupColorMap() {
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
			
			String explanName[] = new String[5];
			explanName[0] = explanName[2] = xVarName;
			explanName[1] = explanName[3] = zVarName;
			explanName[4] = xVarName + zVarName;		//	for interaction, but unused here
			equationView = new MultiLinearEqnView(data, this, "model", yVarName, explanName, minParam, maxParam);
			
			equationView.setSquaredExplan(2, true);
			equationView.setSquaredExplan(3, true);
			equationView.setLastDrawParameter(3);		//	never show interaction term
			
		thePanel.add(equationView);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(xVarName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yVarName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zVarName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			DragQuadGridView localView = new DragQuadGridView(data, this, xAxis, yAxis, zAxis, "model");
								
			String xCurveType = getParameter(QUAD_X_PARAM);
			quadXAlwaysOn = xCurveType.equals("on");
			localView.setAllowXCurvature(quadXAlwaysOn);
			
			String zCurveType = getParameter(QUAD_Z_PARAM);
			quadZAlwaysOn = zCurveType.equals("on");
			localView.setAllowZCurvature(quadZAlwaysOn);
			
			localView.lockBackground(Color.white);
			theView = localView;
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
			if (equationView != null)
				setEquationParamDraw();
			
		return thePanel;
	}
}