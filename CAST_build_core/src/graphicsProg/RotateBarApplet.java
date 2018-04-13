package graphicsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import graphics.*;


public class RotateBarApplet extends RotateApplet {
	static final private String kDepthAxisInfo = "0.0 1.0 2 1";
	static final private String kZAxisInfo = "0.15 0.85 2 1";
	
	static final private int kDiameterSteps = 100;
	static final private int kMinDiameter = 10;
	static final private int kInitialDiameter = 60;
	
	protected String yName;
	protected XNoValueSlider barDiameterSlider;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		yName = getParameter(VAR_NAME_PARAM);
		data.addNumVariable("y", yName, getParameter(VALUES_PARAM));
		
			LabelVariable labelVar = new LabelVariable(getParameter(LABEL_NAME_PARAM));
			labelVar.readValues(getParameter(LABELS_PARAM));
		data.addVariable("label", labelVar);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected BarChartRotateView getBarChartView(DataSet theData, D3Axis xAxis, D3Axis yAxis,
												D3Axis zAxis, String labelKey, String yName, double initialDiameter) {
		return new BarChartRotateView(theData, this, xAxis, yAxis,
															zAxis, labelKey, "y", Color.blue, yName, initialDiameter);
	}
	
	protected String getXAxisInfo() {
		return kDepthAxisInfo;
	}
	
	protected String getZAxisInfo() {
		return kZAxisInfo;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			D3Axis xAxis = new D3Axis("", D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getXAxisInfo());
			xAxis.setShow(false);
			
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			yAxis.setShow(false);

			D3Axis zAxis = new D3Axis("", D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getZAxisInfo());
			zAxis.setShow(false);
			
			double initialDiameter = kInitialDiameter / (double)kDiameterSteps;
			theView = getBarChartView(data, xAxis, yAxis, zAxis, "label", yName, initialDiameter);
			theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(80, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		barDiameterSlider = new XNoValueSlider(translate("Narrow"), translate("Wide"), translate("Bar width"), kMinDiameter, kDiameterSteps, kInitialDiameter, this);
		thePanel.add(barDiameterSlider);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		thePanel.add(new RotateCustomButton(translate("Show 2D"), 90, 0, theView, this));
		thePanel.add(new RotateButton(RotateButton.XYZ_BLANK_ROTATE, theView, this));
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == barDiameterSlider) {
			double barDiameter = barDiameterSlider.getValue() / (double)kDiameterSteps;
			((BarChartRotateView)theView).setDepth(barDiameter);
			theView.repaint();
			
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