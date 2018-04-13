package responseSurfaceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;


public class DragQuadSurfaceApplet extends QuadSurfaceApplet {
	private double startY;
	
	private XCheckbox x2Check, z2Check, xzCheck;
	
	private XButton resetButton;
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		
		boolean[] showParameter = {true, true, true, false, false, false};
		theEqn.setDrawParameters(showParameter);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			startY = (yAxis.getMaxOnAxis() + yAxis.getMinOnAxis()) / 2.0;
			DragResponseSurfaceView localView = new DragResponseSurfaceView(data, this, xAxis, yAxis,
																							zAxis, "model", explanKey, "y");
			localView.resetModel(startY);
			localView.lockBackground(Color.white);
			localView.setColourMap(colourMap);
			theView = localView;
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(50, 0);
		thePanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 1));
			
				x2Check = new XCheckbox(translate("Quadratic term in") + " " + getParameter(X_VAR_NAME_PARAM), this);
			leftPanel.add(x2Check);
			
				z2Check = new XCheckbox(translate("Quadratic term in") + " " + getParameter(Z_VAR_NAME_PARAM), this);
			leftPanel.add(z2Check);
			
				xzCheck = new XCheckbox(translate("Interaction term"), this);
			leftPanel.add(xzCheck);
		
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				resetButton = new XButton(translate("Reset"), this);
			rightPanel.add(resetButton);
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
			
		return thePanel;
	}
	
	private void checkEquation() {
		boolean[] showParameter = {true, true, true, x2Check.getState(), z2Check.getState(), xzCheck.getState()};
		theEqn.setDrawParameters(showParameter);
	}

	
	private boolean localAction(Object target) {
		DragResponseSurfaceView dragView = (DragResponseSurfaceView)theView;
		
		if (target == x2Check) {
			dragView.setAllowTerm(3, x2Check.getState());
			checkEquation();
			data.variableChanged("model");
			return true;
		}
		else if (target == z2Check) {
			dragView.setAllowTerm(4, z2Check.getState());
			checkEquation();
			data.variableChanged("model");
			return true;
		}
		else if (target == xzCheck) {
			dragView.setAllowTerm(5, xzCheck.getState());
			checkEquation();
			data.variableChanged("model");
			return true;
		}
		else if (target == resetButton) {
			x2Check.setState(false);
			z2Check.setState(false);
			xzCheck.setState(false);
			
			dragView.resetModel(startY);
			
			checkEquation();
			data.variableChanged("model");
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