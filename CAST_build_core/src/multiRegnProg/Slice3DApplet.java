package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;
import ssq.*;
import multivar.*;


public class Slice3DApplet extends RotateApplet {
	static final private String MAX_R_PARAM = "maxR";
	static final private String INIT_SELECT_PARAM = "select";
	
	private MultiRegnDataSet data;
	
	private R2Slider corrXZSlider;
	private XButton takeSampleButton;
	
	protected DataSet readData() {
		data = new AdjustXZCorrDataSet(this);
		
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
		
		CoreVariable xVar = data.getVariable("x");
		D3Axis xAxis = new D3Axis(xVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		
		CoreVariable yVar = data.getVariable("y");
		D3Axis yAxis = new D3Axis(yVar.name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		CoreVariable zVar = data.getVariable("z");
		D3Axis zAxis = new D3Axis(zVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = new Rotate3DView(data, this, zAxis, yAxis, xAxis, "z", "y", "x");
												//	Swapped x & z to make z remain unchanged when correl changed
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(120, 100));
		thePanel.add(slicePanel(data));
		return thePanel;
	}
	
	private XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			rotatePanel.add(RotateButton.createRotationPanel(theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(rotatePanel);
		
			AdjustXZCorrDataSet data2 = (AdjustXZCorrDataSet)data;
			double initialR2 = data2.getInitialXZR2();
			String maxRString = getParameter(MAX_R_PARAM);
			double maxR = Double.parseDouble(maxRString);
			double maxR2 = maxR * maxR;
			corrXZSlider = new R2Slider(this, data, "z", "y", null, translate("Correl(X, Z)"),
																											"0.0", maxRString, initialR2, maxR2);
		thePanel.add(corrXZSlider);
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				takeSampleButton = new XButton(translate("Take sample"), this);
			samplePanel.add(takeSampleButton);
		
		thePanel.add(samplePanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(100, 250));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(40, 0));
			
			mainPanel.add("West", sliderPanel(data));
			mainPanel.add("Center", createPlotPanel(data));
		
		thePanel.add(mainPanel);
		return thePanel;
	}
	
	private XPanel createPlotPanel(DataSet tempData) {
												//	Swapped x & z to make z remain unchanged when correl changed
		MultiRegnDataSet data = (MultiRegnDataSet)tempData;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(data.getZAxisInfo());
				NumVariable zVar = (NumVariable)data.getVariable("z");
				horizAxis.setAxisName(zVar.name);
			plotPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(data.getYAxisInfo());
			plotPanel.add("Left", vertAxis);
			
				ScatterSliceView theView = new ScatterSliceView(data, this, horizAxis, vertAxis, "z", "y");
				theView.setForeground(Color.red);
				theView.lockBackground(Color.white);
			plotPanel.add("Center", theView);
		
		thePanel.add("Center", plotPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				NumVariable yVar = (NumVariable)data.getVariable("y");
				XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
				yVariateName.setFont(vertAxis.getFont());
			topPanel.add(yVariateName);
			
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel slicePanel(DataSet tempData) {
												//	Swapped x & z to make z remain unchanged when correl changed
		MultiRegnDataSet data = (MultiRegnDataSet)tempData;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dotPlotPanel(data));
		XLabel zName = new XLabel(data.getXVarName(), XLabel.LEFT, this);
		thePanel.add("North", zName);
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(MultiRegnDataSet data) {
												//	Swapped x & z to make z remain unchanged when correl changed
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis xAxis = new VertAxis(this);
			xAxis.readNumLabels(data.getXAxisInfo());
		thePanel.add("Left", xAxis);
		
			double minSel = 0.0;
			double maxSel = 1.0;
			StringTokenizer theLabels = new StringTokenizer(getParameter(INIT_SELECT_PARAM));
			try {
				minSel = Double.parseDouble(theLabels.nextToken());
				maxSel = Double.parseDouble(theLabels.nextToken());
			} catch (Exception e) {
			}
			data.setSelection("x", minSel, maxSel);
			
			SliceDotPlotView sliceView = new SliceDotPlotView(data, this, xAxis, 1.0, minSel, maxSel, "x");
			sliceView.lockBackground(Color.white);
		thePanel.add("Center", sliceView);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			NumSampleVariable error = (NumSampleVariable)data.getVariable("error");
			error.generateNextSample();
			data.valueChanged(-1);		//		Don't change selection
			
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