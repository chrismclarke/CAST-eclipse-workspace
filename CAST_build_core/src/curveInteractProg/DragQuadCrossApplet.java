package curveInteractProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multiRegn.*;


public class DragQuadCrossApplet extends DragQuadXZApplet {
	static final private String LONG_Y_NAME_PARAM = "longYVarName";
	static final private String LONG_X_NAME_PARAM = "longXVarName";
	static final private String LONG_Z_NAME_PARAM = "longZVarName";
	
	static final private String MAX_RSS_PARAM = "maxRss";
	
	static final protected String kXZKeys[] = {"x", "z"};
	
	static final private int kNLargeCross = 12;
	
	private XChoice residDisplayChoice;
	private int currentResidDisplay = 0;
	
	private XButton lsButton;
	
	protected DataSet readData() {
		data = super.readData();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM));
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(getParameter(LONG_X_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(getParameter(LONG_Y_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(getParameter(LONG_Z_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			DragResponseSurfaceView localView = new DragResponseSurfaceView(data, this, xAxis, yAxis, zAxis,
																	"model", kXZKeys, "y", kXZHandleKeys[0], kXZHandleKeys[1], kYHandleKey);
			localView.setHandlesForModel();
			localView.setColourMap(colourMap);
			localView.setDrawResids(false);
			localView.setSquaredResids(false);
			NumVariable yVar = (NumVariable)data.getVariable("y");
			if (yVar.noOfValues() < kNLargeCross)
				localView.setCrossSize(DataView.LARGE_CROSS);
			localView.lockBackground(Color.white);
			theView = localView;
			
			setLeastSquares();
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 2));
			
				XLabel residLabel = new XLabel(translate("Residuals") + ":", XLabel.LEFT, this);
				residLabel.setFont(getStandardBoldFont());
			
			choicePanel.add(residLabel);
				
				residDisplayChoice = new XChoice(this);
				residDisplayChoice.addItem(translate("Don't show"));
				residDisplayChoice.addItem(translate("Show as lines"));
				residDisplayChoice.addItem(translate("Show as squares"));
			choicePanel.add(residDisplayChoice);
		
		thePanel.add(choicePanel);
		
		thePanel.add(super.eastPanel(data));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		
		thePanel.add(new MultiResidSsqView(data, "y", "model", new NumValue(getParameter(MAX_RSS_PARAM)), this));
		
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
		
		return thePanel;
	}
	
	private void setLeastSquares() {
		((DragResponseSurfaceView)theView).setDataLsModel();
		((DragResponseSurfaceView)theView).setHandlesForModel();
		data.variableChanged("model");	
	}
	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			setLeastSquares();	
			return true;
		}
		else if (target == residDisplayChoice) {
			int newChoice = residDisplayChoice.getSelectedIndex();
			if (newChoice != currentResidDisplay) {
				currentResidDisplay = newChoice;
				DragResponseSurfaceView view = (DragResponseSurfaceView)theView;
				if (newChoice == 0) {
					view.setDrawResids(false);
				}
				else if (newChoice == 1) {
					view.setDrawResids(true);
					view.setSquaredResids(false);
				}
				else if (newChoice == 2) {
					view.setDrawResids(true);
					view.setSquaredResids(true);
				}
				view.repaint();
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