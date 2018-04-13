package indicatorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import glmAnovaProg.*;
import twoFactor.*;


public class Groups2ReorderApplet extends AnovaTableReorderApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String[] kXKey = {"x", "z"};
	
	private RotateDragFactorsView theView;
	
	private XButton spinButton;
	
	public void setupApplet() {
		readMaxSsq();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 5));
		
		add("Center", displayPanel(data));
		add("South", createTable(data));
		add("East", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
																										getParameter(X_LABELS_PARAM));
		data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
																										getParameter(Z_LABELS_PARAM));
		
			TwoFactorModel model = new TwoFactorModel("FactorModel", data, kXKey,
															TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, false, 0.0);
			model.setLSParams("y");
		data.addVariable("model", model);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xAxis.setCatScale(xVar);
		
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		D3Axis zAxis = new D3Axis(getParameter(Z_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		CatVariable zVar = (CatVariable)data.getVariable("z");
		zAxis.setCatScale(zVar);
		
		theView = new RotateDragFactorsView(data, this, xAxis, yAxis, zAxis, "x", "y", "z", "model");
		theView.setCrossColouring(RotateDragFactorsView.COLOURS);
		theView.setAllowDragParams(false);
		theView.lockBackground(Color.white);
		theView.setCrossSize(DataView.LARGE_CROSS);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 30));
		
			XPanel rotatePanel = RotateButton.createRotationPanel(theView, this,
																																	RotateButton.VERTICAL);
		
			spinButton = new XButton(translate("Spin"), this);
			rotatePanel.add(spinButton);
			
		thePanel.add(rotatePanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == spinButton) {
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