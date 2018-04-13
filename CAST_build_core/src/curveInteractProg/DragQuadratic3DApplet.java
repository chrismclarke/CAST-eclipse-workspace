package curveInteractProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import graphics3D.*;

import regnProg.*;
import curveInteract.*;


public class DragQuadratic3DApplet extends DragQuadraticApplet {
	static final protected String Z_AXIS_INFO_PARAM = "xSqrAxis";
	
//	static final private String[] kXKeys = {"x", "z"};
	static final private String[] kSwappedXKeys = {"z", "x"};
	
	private QuadraticModel3View d3View;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			NumVariable xVar = (NumVariable)data.getVariable("x");
		
			QuadraticVariable zVar = new QuadraticVariable(xVar.name + "-" + translate("squared"), xVar, 0, 0, 1, 9);
		data.addVariable("z", zVar);
		
			QuadraticModel model = (QuadraticModel)data.getVariable("model");
		
			MultipleRegnModel model2 = new MultipleRegnModel("model2", data, kSwappedXKeys);
			model2.setParameter(0, model.getIntercept());
			model2.setParameter(2, model.getSlope());
			model2.setParameter(1, model.getCurvature());
		data.addVariable("model2", model2);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 5));
			leftPanel.add("Center", super.displayPanel(data));
			leftPanel.add("South", quadEqnPanel(data));
		
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout());
			
				NumVariable xVar = (NumVariable)data.getVariable("x");
				D3Axis xAxis = new D3Axis(xVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);	//swapped X_AXIS and Z_AXIS
				xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
				NumVariable yVar = (NumVariable)data.getVariable("y");
				D3Axis yAxis = new D3Axis(yVar.name, D3Axis.Y_AXIS, D3Axis.Z_AXIS, this);	//swapped X_AXIS and Z_AXIS
				yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
				NumVariable zVar = (NumVariable)data.getVariable("z");
				D3Axis zAxis = new D3Axis(zVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);	//swapped X_AXIS and Z_AXIS
				zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
				
				d3View = new QuadraticModel3View(data, this, zAxis, yAxis, xAxis, "model2", kSwappedXKeys, "y");
				d3View.setSelectCrosses(false);
				d3View.lockBackground(Color.white);
			rightPanel.add("Center", d3View);
			
			rightPanel.add("South", RotateButton.createQuadXYRotationPanel(d3View, this));
			
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		return lsResidButtonPanel();
	}
	
	protected void showResiduals(boolean showNotHide) {
		super.showResiduals(showNotHide);
		d3View.setDrawResiduals(showNotHide);
		data.variableChanged("model2");
	}
	
	protected void setLSParameters() {
		super.setLSParameters();
		
		QuadraticModel model = (QuadraticModel)data.getVariable("model");
		MultipleRegnModel model2 = (MultipleRegnModel)data.getVariable("model2");
		model2.setParameter(0, model.getIntercept());
		model2.setParameter(2, model.getSlope());
		model2.setParameter(1, model.getCurvature());
		
		data.variableChanged("model2");
	}
}