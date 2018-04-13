package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import exper.*;


public class Identifiability2Applet extends IdentifiabilityApplet {
	static final private String INITIAL_ROTATION_PARAM = "initialRotation";
	
	static final private String effect2Keys[] = {"treat1", "treat2"};
	
	private RotateTwoFactorView theView;
	
	protected String[] getEffectKeys() {
		return effect2Keys;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(2, 2));
		thePanel.add("Center", threeDPanel(data));
			
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
			buttonPanel.add(RotateButton.createRotationPanel(theView, this));
//			buttonPanel.add(namePanel(data));
		thePanel.add("South", buttonPanel);
		
		String initialRotationString = getParameter(INITIAL_ROTATION_PARAM);
		if (initialRotationString != null) {
			StringTokenizer theAngles = new StringTokenizer(initialRotationString);
			int roundDens = Integer.parseInt(theAngles.nextToken());
			int ofDens = Integer.parseInt(theAngles.nextToken());
			theView.rotateTo(roundDens, ofDens);
		}
		return thePanel;
	}
	
	private XPanel threeDPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			CatVariable xVar = (CatVariable)data.getVariable("treat1");
			D3Axis xAxis = new D3Axis(xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setCatScale(xVar);
			
			D3Axis yAxis = new D3Axis(data.getVariable("model").name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(RESPONSE_AXIS_INFO_PARAM));
			
			CatVariable zVar = (CatVariable)data.getVariable("treat2");
			D3Axis zAxis = new D3Axis(zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setCatScale(zVar);
			
			String yVarKey = "response";											//	for RotateTwoFactorLSApplet
			if (data.getVariable(yVarKey) == null)
				yVarKey = null;
			
			theView = new RotateTwoFactorView(data, this, xAxis, yAxis, zAxis, "treat1", yVarKey, "treat2", "model");
			theView.setDrawData(true);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 20));
		
		thePanel.add(basePanel(data));
		
			zeroEffectButton = new XButton[2][];
			zeroMeanButton = new XButton[2];
		
			String title = data.getVariable("treat1").name + " effects";
		thePanel.add(effectPanel(data, "treat1", 0, title));
		
			title = data.getVariable("treat2").name + " effects";
		thePanel.add(effectPanel(data, "treat2", 1, title));
		
		return thePanel;
	}
	
}