package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class SequentialAddApplet extends RotateApplet {
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	
	private MultiRegnDataSet data;
	
	private String xVarName[] = new String[2];
	private String yVarName;
	
	private MultiLinearEqnView equationView;
	
	private XCheckbox xCheck, zCheck;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		findCoeffNames();
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		ls.updateLSParams("y", getConstraints());
			
		return data;
	}
	
	private void findCoeffNames() {
		xVarName[0] = data.getVariable("x").name;
		xVarName[1] = data.getVariable("z").name;
		yVarName = data.getVariable("y").name;
	}
	
	private double[] getConstraints() {
		double[] constraints = {Double.NaN, Double.NaN, Double.NaN};
		if (xCheck == null || !xCheck.getState())
			constraints[1] = 0.0;
		if (zCheck == null || !zCheck.getState())
			constraints[2] = 0.0;
		
		return constraints;
	}
	
	private boolean[] getParamShow() {
		boolean[] paramShow = {true, true, true};
		if (xCheck == null || !xCheck.getState())
			paramShow[1] = false;
		if (zCheck == null || !zCheck.getState())
			paramShow[2] = false;
		
		return paramShow;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(regnData.getXAxisInfo());
			D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(regnData.getYAxisInfo());
			D3Axis zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(regnData.getZAxisInfo());
			
			Model3ResidView view3D = new Model3ResidView(data, this, xAxis, yAxis, zAxis, "ls",
																																MultiRegnDataSet.xKeys, "y");
			view3D.setShowSelectedArrows(false);
			view3D.lockBackground(Color.white);
			theView = view3D;
			
		thePanel.add("Center", view3D);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
				topPanel.add(new RotateButton(RotateButton.YX_ROTATE, theView, this));
				topPanel.add(new RotateButton(RotateButton.YZ_ROTATE, theView, this));
				topPanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
		
			rotatePanel.add(topPanel);
			
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(rotatePanel);
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 4));
			
				XLabel checkLabel = new XLabel(translate("Variables in model") + ":", XLabel.LEFT, this);
				checkLabel.setFont(getStandardBoldFont());
			checkPanel.add(checkLabel);
				
				xCheck = new XCheckbox(xVarName[0], this);
			checkPanel.add(xCheck);
				zCheck = new XCheckbox(xVarName[1], this);
			checkPanel.add(zCheck);
		
		thePanel.add(checkPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		 
			NumValue maxCoeff[] = new NumValue[3];
			StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
			for (int i=0 ; i<3 ; i++)
				maxCoeff[i] = new NumValue(st.nextToken());
			
			equationView = new MultiLinearEqnView(data, this, "ls", yVarName, xVarName, maxCoeff, maxCoeff);
			equationView.setDrawParameters(getParamShow());
		thePanel.add(equationView);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == xCheck || target == zCheck) {
			MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
			ls.updateLSParams("y", getConstraints());
			
			equationView.setDrawParameters(getParamShow());
			
			data.variableChanged("ls");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}