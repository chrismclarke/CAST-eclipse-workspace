package residTwoProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import valueList.*;
import graphics3D.*;

import resid.*;
import multiRegn.*;
import residTwo.*;


public class LeverageGraphApplet extends XApplet {
	static final private String MAX_FIT_VAR_PARAM = "maxFitVar";
	static final private String LEVERAGE_AXIS_PARAM = "leverageAxis";
	
	static final private Color kValueBackground = new Color(0xDDDDEE);
	
//	static final private String kCoeffKey[] = {"b0", "b1", "b2"};
	
	private MultiRegnDataSet data;
	private SummaryDataSet summaryData;
	private NumValue maxFitVar;
	
	private DragAddPointView d3View;
	private LeverageGraphView leverageView;
	
	private XButton rotateButton, rotateLeverageButton;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(0, 5));
		
			XPanel mainPanel = new XPanel();
		
			mainPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																										ProportionLayout.TOTAL));
			
			mainPanel.add(ProportionLayout.LEFT, dataPanel(data, summaryData));
			
			mainPanel.add(ProportionLayout.RIGHT, rightPanel(data, summaryData));
		
		add("Center", mainPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				XPanel innerBottomPanel = new InsetPanel(10, 6);
				innerBottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				
					OneValueView xValueView = new OneValueView(data, "x", this);
				innerBottomPanel.add(xValueView);
				
					OneValueView zValueView = new OneValueView(data, "z", this);
				innerBottomPanel.add(zValueView);
				
					OneValueView fitValueView = new OneValueView(data, "varFit", this, maxFitVar);
				innerBottomPanel.add(fitValueView);
				
				innerBottomPanel.lockBackground(kValueBackground);
			bottomPanel.add(innerBottomPanel);
		
		add("South", bottomPanel);
	}
	
	private MultiRegnDataSet readData() {
		MultiRegnDataSet data = new MultiRegnDataSet(this);
		
			NumVariable xVar = (NumVariable)data.getVariable("x");
			int n = xVar.noOfValues();
			((NumValue)xVar.valueAt(n - 1)).decimals = ((NumValue)xVar.valueAt(0)).decimals;	//	sets decimals for NaN
			NumVariable zVar = (NumVariable)data.getVariable("z");
			((NumValue)zVar.valueAt(n - 1)).decimals = ((NumValue)zVar.valueAt(0)).decimals;
		
			maxFitVar = new NumValue(getParameter(MAX_FIT_VAR_PARAM));
			LeverageValueVariable fitVar = new LeverageValueVariable(translate("Var(prediction)"), data,
									MultiRegnDataSet.xKeys, "model", LeverageValueVariable.LEVERAGE,
									maxFitVar.decimals);		//	last value of x and z are NaN except when dragging
			MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
			double errorSD = model.evaluateSD().toDouble();
			fitVar.setScaleFactor(errorSD * errorSD);
		
		data.addVariable("varFit", fitVar);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(MultiRegnDataSet data) {
		return null;
	}
	
	public void setCoeffDistns(DataSet data, SummaryDataSet summaryData) {
	}
	
	private XPanel dataPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
			
			D3Axis xAxis = new D3Axis(data.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(data.getXAxisInfo());
			
			D3Axis yAxis = new D3Axis(data.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(data.getYAxisInfo());
			
			D3Axis zAxis = new D3Axis(data.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(data.getZAxisInfo());
			
			d3View = new DragAddPointView(data, this, xAxis, yAxis, zAxis,
																						"model", MultiRegnDataSet.xKeys, summaryData);
			d3View.lockBackground(Color.white);
		
		thePanel.add("Center", d3View);
		thePanel.add("South", rotateDataPanel(data));
		
		return thePanel;
	}
	
	private XPanel rotateDataPanel(MultiRegnDataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(d3View, this);
		
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		
		return thePanel;
	}
	
	private XPanel rotateLeveragePanel(MultiRegnDataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(leverageView, this);
		
			rotateLeverageButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateLeverageButton);
		
		return thePanel;
	}
	
	protected XPanel rightPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
			
			D3Axis xAxis = new D3Axis(data.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(data.getXAxisInfo());
			
			D3Axis yAxis = new D3Axis(translate("Var(prediction)"), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(LEVERAGE_AXIS_PARAM));
			
			D3Axis zAxis = new D3Axis(data.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(data.getZAxisInfo());
			
			leverageView = new LeverageGraphView(data, this, xAxis, yAxis, zAxis, "x", "z", "model", d3View);
			leverageView.lockBackground(Color.white);
		
		thePanel.add("Center", leverageView);
			
		thePanel.add("South", rotateLeveragePanel(data));
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == rotateButton) {
			d3View.startAutoRotation();
			return true;
		}
		else if (target == rotateLeverageButton) {
			leverageView.startAutoRotation();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}

