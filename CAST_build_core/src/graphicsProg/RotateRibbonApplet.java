package graphicsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import cat.*;
import graphics.*;


public class RotateRibbonApplet extends RotateApplet {
	static final private String N_SERIES_PARAM = "nSeries";
	static final private String Y_NAME_PARAM = "yName";
	
	static final private String kDepthAxisInfo = "0.0 1.0 2 1";
	static final private String kSeriesAxisInfo = "0.15 0.85 2 1";
	
	static final private int kWidthSteps = 100;
	
	private String yKeys[];
	
	private XNoValueSlider ribbonWidthSlider;
	private int initialWidth;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		int nSeries = Integer.parseInt(getParameter(N_SERIES_PARAM));
		yKeys = new String[nSeries];
		LabelValue seriesLabels[] = new LabelValue[nSeries];
		for (int i=0 ; i<nSeries ; i++) {
			yKeys[i] = "y" + i;
			String seriesName = getParameter(VAR_NAME_PARAM + i);
			seriesLabels[i] = new LabelValue(seriesName);
			data.addNumVariable(yKeys[i], seriesName, getParameter(VALUES_PARAM + i));
		}
		initialWidth = (nSeries == 2 ? 60 : nSeries == 3 ? 80 : 100);
		
			CatVariable keyVar = new CatVariable(translate("Key"));
			keyVar.setLabels(seriesLabels);
		data.addVariable("key", keyVar);
		
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

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			D3Axis xAxis = new D3Axis("", D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(kDepthAxisInfo);
			xAxis.setShow(false);
			
			String yName = getParameter(Y_NAME_PARAM);
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			yAxis.setShow(false);

			D3Axis zAxis = new D3Axis("", D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(kSeriesAxisInfo);
			zAxis.setShow(false);
			
			double initialWidthPropn = initialWidth / (double)kWidthSteps;
			theView = new RibbonChartView(data, this, xAxis, yAxis,
											zAxis, "label", yKeys, CatKey3View.kCatColour, yName, initialWidthPropn);
			theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		ribbonWidthSlider = new XNoValueSlider(translate("Thin"), translate("Thick"), translate("Ribbon thickness"), 0, kWidthSteps, initialWidth, this);
		thePanel.add(ribbonWidthSlider);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
		CatKey3View keyView = new CatKey3View(data, this, "key");
		thePanel.add(keyView);
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
			rotatePanel.add(new RotateCustomButton(translate("Show 2D"), 90, 0, theView, this));
			rotatePanel.add(new RotateButton(RotateButton.XYZ_BLANK_ROTATE, theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(rotatePanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == ribbonWidthSlider) {
			double ribbonWidth = ribbonWidthSlider.getValue() / (double)kWidthSteps;
			((RibbonChartView)theView).setDepth(ribbonWidth);
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