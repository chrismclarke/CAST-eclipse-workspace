package graphicsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import cat.*;
import graphics.*;


public class RotatePieApplet extends RotateApplet {
	static final private String kHeightAxisInfo = "0.0 1.0 2 1";
	static final private String kCircleAxisInfo = "-1.0 1.0 2 1";
	static final private int kThicknessSteps = 100;
	static final private int kInitialThickness = 30;
	
	private XNoValueSlider thicknessSlider;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable yVar = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		yVar.readLabels(getParameter(CAT_LABELS_PARAM));
		yVar.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			D3Axis xAxis = new D3Axis("", D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(kCircleAxisInfo);
			xAxis.setShow(false);
			D3Axis yAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(kHeightAxisInfo);
			yAxis.setShow(false);
			D3Axis zAxis = new D3Axis("", D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(kCircleAxisInfo);
			zAxis.setShow(false);
			
			double initialThickness = kInitialThickness / (double)kThicknessSteps;
			theView = new RotatePieView(data, this, xAxis, yAxis,
											zAxis, "y", CatKey3View.kCatColour, initialThickness);
//			theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(70, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		thicknessSlider = new XNoValueSlider(translate("Thin"), translate("Thick"), translate("Pie thickness"), 0, kThicknessSteps, kInitialThickness, this);
		thePanel.add(thicknessSlider);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
		CatKey3View keyView = new CatKey3View(data, this, "y");
		thePanel.add(keyView);
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
			rotatePanel.add(new RotateCustomButton(translate("Show 2D"), 90, 90, theView, this));
			rotatePanel.add(new RotateButton(RotateButton.XYZ_BLANK_ROTATE, theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(rotatePanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == thicknessSlider) {
			double thicknessPropn = thicknessSlider.getValue() / (double)kThicknessSteps;
			((RotatePieView)theView).setThickness(thicknessPropn);
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