package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import twoFactor.*;
import indicator.*;


public class Rotate2FactorDragApplet extends RotateApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	
	static final private String INIT_MEAN_PARAM = "initialMean";
	static final private String MAX_PARAM_PARAM = "maxParam";
	
	static final private String SEPARATE_X_PARAM = "separateX";
	static final private String SEPARATE_Z_PARAM = "separateZ";
	
	static final private String kXZKeys[] = {"x", "z"};
	
	private DataSet data;
	
	private XButton lsButton;
	
	protected DataSet readData() {
		data = new DataSet();
		
		String yValuesString = getParameter(Y_VALUES_PARAM);
		if (yValuesString != null)
			data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), yValuesString);
		
		data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
													getParameter(X_LABELS_PARAM));
		data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
													getParameter(Z_LABELS_PARAM));
		
		double initMean = Double.parseDouble(getParameter(INIT_MEAN_PARAM));
		
		TwoFactorModel model = new TwoFactorModel(getParameter(Y_VAR_NAME_PARAM), data, kXZKeys,
															TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, false, initMean);
		
		data.addVariable("model", model);
		setLeastSquares();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
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
		
		RotateDragFactorsView localView = new RotateDragFactorsView(data, this, xAxis, yAxis, zAxis, "x", "y", "z", "model");
		NumValue maxIntercept = new NumValue(new StringTokenizer(getParameter(MAX_PARAM_PARAM)).nextToken());
		localView.setShowBaselineOffsetArrows(true, maxIntercept.decimals);
		localView.setCrossColouring(RotateDragFactorsView.COLOURS);
		theView = localView;
		theView.lockBackground(Color.white);
		theView.setCrossSize(DataView.LARGE_CROSS);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(20, 10, 20, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
			NumValue maxParam[] = new NumValue[3];
			for (int i=0 ; i<3 ; i++)
				maxParam[i] = new NumValue(st.nextToken());
		thePanel.add(new FactorEstimatesView(data, this, "x", "z", "model", maxParam));
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(7, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 60));
			
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
			rotatePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
			
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
			
				String separateXString = getParameter(SEPARATE_X_PARAM);
			if (separateXString != null)
				rotatePanel.add(new RotateCustomButton(separateXString, theView, this));
				
				String separateZString = getParameter(SEPARATE_Z_PARAM);
			if (separateZString != null)
				rotatePanel.add(new RotateCustomButton(separateZString, theView, this));
		
		thePanel.add(rotatePanel);
			
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
			
		return thePanel;
	}
	
	private void setLeastSquares() {
		TwoFactorModel model = (TwoFactorModel)data.getVariable("model");
		model.updateLSParams("y");
	}
	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			setLeastSquares();
			data.variableChanged("model");
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