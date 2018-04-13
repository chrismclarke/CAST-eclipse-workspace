package twoFactorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import glmAnova.*;
import glmAnovaProg.*;
import twoFactor.*;


public class FactorAnovaReorderApplet extends AnovaTableReorderApplet {
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	private RotateEstimatesView theView;
	
	private XButton rotateButton;
	
	public void setupApplet() {
		readMaxSsq();
		
		data = readData();
		
		setLayout(new BorderLayout(0, 8));
		
		add("Center", displayPanel(data));
		
		add("East", eastPanel(data));
				
			AnovaReorderTableView table = createTable(data);
		add("South", table);
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
			
								//	need duplicate LS model of type TwoFactorModel for RotateEstimatesView
			String xKeys[] = getXKeys();
			TwoFactorModel lsFit = new TwoFactorModel("least squares", data, xKeys,
																TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, false, 0.0);
			lsFit.setLSParams("y");
		data.addVariable("ls", lsFit);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			String xKeys[] = getXKeys();
		
		CatVariable xVar = (CatVariable)data.getVariable(xKeys[0]);
		D3Axis xAxis = new D3Axis(xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setCatScale(xVar);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		D3Axis yAxis = new D3Axis(yVar.name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		CatVariable zVar = (CatVariable)data.getVariable(xKeys[1]);
		D3Axis zAxis = new D3Axis(zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setCatScale(zVar);
		
		theView = new RotateEstimatesView(data, this, xAxis, yAxis, zAxis, "X", "y", "Z", "ls",
														RotateEstimatesView.MODEL);
		theView.setCrossSize(DataView.LARGE_CROSS);
		theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == rotateButton) {
			theView.startAutoRotation();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}