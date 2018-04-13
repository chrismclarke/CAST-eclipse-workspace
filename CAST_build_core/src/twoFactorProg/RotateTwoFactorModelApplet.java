package twoFactorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import twoFactor.*;


public class RotateTwoFactorModelApplet extends RotateApplet {
	
	private SummaryDataSet summaryData;
	
	private XButton sampleButton;
	
	protected DataSet readData() {
		TwoFactorDataSet data = new TwoFactorDataSet(this);
		
		summaryData = new SummaryDataSet(data, "error");
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		TwoFactorDataSet factorData = (TwoFactorDataSet)data;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(factorData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			CatVariable xCat = (CatVariable)factorData.getVariable("x");
			xAxis.setCatScale(xCat);
		
			D3Axis yAxis = new D3Axis(factorData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(factorData.getYAxisInfo());
			
			D3Axis zAxis = new D3Axis(factorData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			CatVariable zCat = (CatVariable)data.getVariable("z");
			zAxis.setCatScale(zCat);
			
			theView = new RotateModelView(data, this, xAxis, yAxis, zAxis, "x", "y", "z", "model");
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}