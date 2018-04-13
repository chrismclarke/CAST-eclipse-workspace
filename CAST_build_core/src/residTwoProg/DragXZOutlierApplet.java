package residTwoProg;

import java.awt.*;
import java.util.*;

import axis.*;
import utils.*;
import dataView.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;
import residTwo.*;


public class DragXZOutlierApplet extends RotateApplet {
	static final protected String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String DRAG_POINTS_PARAM = "dragPoints";
	
	protected DataSet data;
	
	private int[] dragIndex;
	
	private XChoice dragPointChoice;
	private int currentChoice = 0;
	private double originalYValue;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		data.addVariable("resid", new ResidValueVariable(translate("Residual"), data,
																							MultiRegnDataSet.xKeys, "y", "ls", 9));
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(DRAG_POINTS_PARAM), "#");
		int nPoints = Integer.parseInt(st.nextToken());
		dragIndex = new int[nPoints];
		
		dragPointChoice = new XChoice(this);
		
		for (int i=0 ; i<nPoints ; i++) {
			dragIndex[i] = Integer.parseInt(st.nextToken());
			dragPointChoice.addItem(st.nextToken());
		}
		data.setSelection(dragIndex[0]);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		originalYValue = yVar.doubleValueAt(dragIndex[0]);
		
			XLabel choiceLabel = new XLabel(translate("Drag point with") + ":", XLabel.LEFT, this);
			choiceLabel.setFont(getStandardBoldFont());
		thePanel.add(choiceLabel);
		
		thePanel.add(dragPointChoice);
		
		return thePanel;
	}
	
	protected void addRotateButtons(XPanel thePanel, Rotate3DView theView) {
		thePanel.add("East", RotateButton.createRotationPanel(theView, this,
																																		RotateButton.VERTICAL));
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(getParameter(Z_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			theView = new DragOutlierView(data, this, xAxis, yAxis, zAxis, "ls", MultiRegnDataSet.xKeys, "y");
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		addRotateButtons(thePanel, theView);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XLabel residLabel = new XLabel(translate("Residual"), XLabel.LEFT, this);
			residLabel.setFont(getStandardFont());
		thePanel.add("North", residLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new FixedSizeLayout(100, 0));
			
				XPanel dataPanel = new XPanel();
				dataPanel.setLayout(new AxisLayout());
			
					VertAxis vertAxis = new VertAxis(this);
					vertAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
				dataPanel.add("Left", vertAxis);
			
					ResidDotView residView = new ResidDotView(data, this, vertAxis);
					residView.setActiveNumVariable("resid");
					residView.lockBackground(Color.white);
					residView.setAllowDrag(false);
				
				dataPanel.add("Center", residView);
			innerPanel.add(dataPanel);
			
		thePanel.add("Center", innerPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dragPointChoice) {
			int newChoice = dragPointChoice.getSelectedIndex();
			if (newChoice != currentChoice) {
				NumVariable yVar = (NumVariable)data.getVariable("y");
				((NumValue)yVar.valueAt(dragIndex[currentChoice])).setValue(originalYValue);
				MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
				ls.updateLSParams("y");
				
				currentChoice = newChoice;
			
				originalYValue = yVar.doubleValueAt(dragIndex[newChoice]);
				
				data.variableChanged("ls", dragIndex[newChoice]);
			}
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