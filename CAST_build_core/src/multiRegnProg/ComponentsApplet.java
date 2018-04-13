package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;
import ssq.*;


public class ComponentsApplet extends RotateApplet {
	static final private String MEAN_DECIMALS_PARAM = "meanDecimals";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String SSQ_NAMES_PARAM = "ssqNames";
	static final private String DOT_WIDTH_PARAM = "componentDotWidth";
	
	static final private NumValue kZero = new NumValue(0.0, 0);
	
	static final private Color kPlaneAColor = new Color(0x66CCFF);
	static final private Color kPlaneBColor = new Color(0xCCCCCC);
	
	static final protected NumValue kMaxR2 = new NumValue(1.0, 4);
	
	protected MultiRegnDataSet data;
	protected SummaryDataSet summaryData;
	
	protected D3Axis xAxis, yAxis, zAxis;
	
	protected NumValue maxSsq;
	
	protected ComponentPlotPanel componentPlot;
	private XChoice compChoice;
	private int currentCompIndex = 0;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		data.addBasicComponents();
		
		MultipleRegnModel meanModel = new MultipleRegnModel("mean", data, MultiRegnDataSet.xKeys);
		data.addVariable("mean", meanModel);
		setMeanModel(data);
		
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	protected void setMeanModel(DataSet data) {
		double sy = 0.0;
		int n = 0;
		ValueEnumeration ye = ((NumVariable)data.getVariable("y")).values();
		while (ye.hasMoreValues()) {
			sy += ye.nextDouble();
			n++;
		}
		MultipleRegnModel meanModel = (MultipleRegnModel)data.getVariable("mean");
		int meanDecimals = Integer.parseInt(getParameter(MEAN_DECIMALS_PARAM));
		meanModel.setParameter(0, new NumValue(sy / n, meanDecimals));
		meanModel.setParameter(1, kZero);
		meanModel.setParameter(2, kZero);
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
											BasicComponentVariable.kComponentKey, maxSsq.decimals, kMaxR2.decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
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
		xAxis = new D3Axis(xVar == null ? "x" : xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		
		CoreVariable yVar = data.getVariable("y");
		yAxis = new D3Axis(yVar == null ? "y" : yVar.name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		CoreVariable zVar = data.getVariable("z");
		zAxis = new D3Axis(zVar == null ? "z" : zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = new Rotate3DCrossPlanesView(data, this, xAxis, yAxis, zAxis, "mean", "ls",
																	kPlaneAColor, kPlaneBColor, BasicComponentVariable.kComponentColor);
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected boolean sdDisplayType() {
		return ComponentPlotPanel.NO_SD;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.add("Center", super.controlPanel(data));
		thePanel.add("East", componentChoicePanel(data));
		
		return thePanel;
	}
	
	protected XPanel componentChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

//********* must be created here instead of in eastPanel() since controlPanel() is called first
			componentPlot = new ComponentPlotPanel(data, ((MultiRegnDataSet)data).getSummaryAxisInfo(),
								BasicComponentVariable.kComponentKey, BasicComponentVariable.kComponentColor,
								getParameter(SSQ_NAMES_PARAM), ComponentPlotPanel.NOT_SELECTED,
								sdDisplayType(), ComponentPlotPanel.NO_HEADING, null, this);
//*********
			
			compChoice = componentPlot.createComponentChoice(this);
		thePanel.add(compChoice);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout (FlowLayout.LEFT, 0, 0));
			topPanel.add(new XLabel(translate("Component"), XLabel.LEFT, this));
			
		thePanel.add("North", topPanel);
			
			XPanel mainPanel = new XPanel();
				int dotPlotWidth = Integer.parseInt(getParameter(DOT_WIDTH_PARAM));
			mainPanel.setLayout(new FixedSizeLayout(dotPlotWidth, 300));
			
//********* must be created in controlPanel() since it is called first
//				componentPlot = new ComponentPlotPanel(data, getParameter(COMPONENT_AXIS_PARAM),
//									BasicComponentVariable.kComponentKey, BasicComponentVariable.kComponentColor,
//									getParameter(SSQ_NAMES_PARAM), ComponentPlotPanel.NOT_SELECTED,
//									sdDisplayType(), ComponentPlotPanel.NO_HEADING, null, this);
//*********
			
			mainPanel.add(componentPlot);
			
		thePanel.add("Center", mainPanel);
				
		return thePanel;
	}
	
	protected void changeComponentDisplayed(int index) {
		componentPlot.setComponent(index);
		((Rotate3DCrossPlanesView)theView).setComponentType(index);
		theView.repaint();
	}
	
	private boolean localAction(Object target) {
		if (target == compChoice) {
			if (compChoice.getSelectedIndex() != currentCompIndex) {
				currentCompIndex = compChoice.getSelectedIndex();
				changeComponentDisplayed(currentCompIndex);
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