package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;
import graphics3D.*;


import ssq.*;
import ssqProg.*;
import multiRegn.*;

public class SeqComponentsApplet extends Components2Applet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String COMPONENT_DECIMALS_PARAM = "componentDecimals";
	
	static final protected Color kModel0Color = new Color(0xCCCCCC);
	static final protected Color kModel1Color = new Color(0x66CCFF);
	static final protected Color kModel2Color = new Color(0xFFCCCC);
	
	static final protected Color kDarkRed = new Color(0x990000);
	
	static final private double[] kNoVarConstraints = {Double.NaN, 0.0, 0.0};
	
	static final private int kR2Decimals = 3;
	
	protected SeqComponentPlanesView theView;
	protected NumValue maxSsq;
	
	private XPanel eqnsPanel;
	private CardLayout eqnsPanelLayout;
	private ComponentEqnPanel ssqXZEquation, ssqZXEquation;
	
	private XChoice orderChoice;
	private int currentOrder = 0;
	
	protected String kExplainedString, kAfterString;
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(translate("Explained by * after"), "*");
		kExplainedString = st.nextToken();
		kAfterString = st.nextToken() + " ";
		
		super.setupApplet();
	}
	
	private NumValue[] copyParams(MultipleRegnModel ls) {
		int nParam = ls.noOfParameters();
		NumValue paramCopy[] = new NumValue[nParam];
		for (int i=0 ; i<nParam ; i++)
			paramCopy[i] = new NumValue(ls.getParameter(i));
		return paramCopy;
	}
	
	protected CoreModelDataSet getData() {
		MultiRegnDataSet data = new MultiRegnDataSet(this);
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		
		MultipleRegnModel ls0 = new MultipleRegnModel("mean only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
		ls0.updateLSParams("y", kNoVarConstraints);
		data.addVariable("ls0", ls0);
		
		MultipleRegnModel lsX = new MultipleRegnModel("X only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
		lsX.updateLSParams("y", data.getXOnlyConstraints());
		data.addVariable("lsX", lsX);
		
		MultipleRegnModel lsZ = new MultipleRegnModel("Z only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
		lsZ.updateLSParams("y", data.getZOnlyConstraints());
		data.addVariable("lsZ", lsZ);
		
		int componentDecimals = Integer.parseInt(getParameter(COMPONENT_DECIMALS_PARAM));
		SeqXZComponentVariable.addComponentsToData(data, "x", "z", "y", "lsX", "lsZ", "ls", componentDecimals);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
										SeqXZComponentVariable.kAllComponentKeys, maxSsq.decimals, kR2Decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", new XPanel());
		
			XPanel orderPanel = new XPanel();
			orderPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel orderLabel = new XLabel(translate("Fitting order") + ":", XLabel.LEFT, this);
				orderLabel.setFont(getStandardBoldFont());
				orderLabel.setForeground(kDarkRed);
			
			orderPanel.add(orderLabel);
				MultiRegnDataSet regnData = (MultiRegnDataSet)data;
				orderChoice = new XChoice(this);
				orderChoice.addItem(regnData.getXVarName() + " " + translate("then") + " " + regnData.getZVarName());
				orderChoice.addItem(regnData.getZVarName() + " " + translate("then") + " " + regnData.getXVarName());
			orderPanel.add(orderChoice);
			
		thePanel.add("West", orderPanel);
	
			XPanel compPanel = new XPanel();
			compPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel compLabel = new XLabel(translate("Component") + ":", XLabel.LEFT, this);
				compLabel.setFont(getStandardBoldFont());
				compLabel.setForeground(kDarkRed);
			
			compPanel.add(compLabel);
			compPanel.add(componentChoicePanel());
			
		thePanel.add("East", compPanel);
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new InsetPanel(0, 0, 40, 0);
		thePanel.setLayout(new BorderLayout(10, 0));
		
			D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(regnData.getXAxisInfo());
			D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(regnData.getYAxisInfo());
			D3Axis zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(regnData.getZAxisInfo());
			
			theView = new SeqComponentPlanesView(data, this, xAxis, yAxis, zAxis, "ls0", "lsX", "ls",
																kModel0Color, kModel1Color, kModel2Color, component3DColors());
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		thePanel.add("East", RotateButton.createRotationPanel(theView, this,
																																	RotateButton.VERTICAL));
		
		return thePanel;
	}
	
	protected String[] componentKeys() {
		return (orderChoice == null || orderChoice.getSelectedIndex() == 0)
							? SeqXZComponentVariable.kXZComponentKey : SeqXZComponentVariable.kZXComponentKey;
	}
	
	protected Color[] componentColors() {
		return SeqXZComponentVariable.kComponentColor;
	}
	
	protected Color[] component3DColors() {
		return SeqXZComponentVariable.kComponentColor;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", super.rightPanel(data));
			componentPlot.getView().setShowSD(true);
			
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		eqnsPanel = new InsetPanel(0, 20, 0, 0);
		eqnsPanelLayout = new CardLayout();
		eqnsPanel.setLayout(eqnsPanelLayout);
		
		AnovaImages.loadXZImages(this);
		
			XPanel xzPanel = new XPanel();
			xzPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				ssqXZEquation = new ComponentEqnPanel(summaryData, SeqXZComponentVariable.kXZComponentKey, 
								maxSsq, AnovaImages.xThenZSsqs, componentColors(), AnovaImages.kXZSsqWidth,
								AnovaImages.kXZSsqHeight, bigContext);
			xzPanel.add(ssqXZEquation);
		
		eqnsPanel.add("XZ", xzPanel);
		
			XPanel zxPanel = new XPanel();
			zxPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			
				ssqZXEquation = new ComponentEqnPanel(summaryData, SeqXZComponentVariable.kZXComponentKey, 
								maxSsq, AnovaImages.zThenXSsqs, componentColors(), AnovaImages.kXZSsqWidth,
								AnovaImages.kXZSsqHeight, bigContext);
			zxPanel.add(ssqZXEquation);
		
		eqnsPanel.add("ZX", zxPanel);
		
		eqnsPanelLayout.show(eqnsPanel, "XZ");
		
		return eqnsPanel;
	}
	
	protected void highlightEquation(int newComponentIndex) {
		super.highlightEquation(newComponentIndex);
		ssqXZEquation.highlightComponent(newComponentIndex);
		ssqZXEquation.highlightComponent(newComponentIndex);
	}
	
	protected void changeComponentSelected(int newComponentIndex) {
		super.changeComponentSelected(newComponentIndex);
		
		theView.setComponentType(newComponentIndex);
		theView.repaint();
	}
	
	private void changeXZOrder(int newOrder) {
		if (newOrder == 0) {
			theView.setModelKeys("ls0", "lsX", "ls");
			compChoice.changeItem(1, kExplainedString + "X");
			compChoice.changeItem(2, kExplainedString + "Z" + kAfterString + "X");
			eqnsPanelLayout.show(eqnsPanel, "XZ");
		}
		else {
			theView.setModelKeys("ls0", "lsZ", "ls");
			compChoice.changeItem(1, kExplainedString + "Z");
			compChoice.changeItem(2, kExplainedString + "X" + kAfterString + "Z");
			eqnsPanelLayout.show(eqnsPanel, "ZX");
		}
		
		componentPlot.setComponentKeys(componentKeys(), "", compChoice.getSelectedIndex());
	}

	
	private boolean localAction(Object target) {
		if (target == orderChoice) {
			if (orderChoice.getSelectedIndex() != currentOrder) {
				currentOrder = orderChoice.getSelectedIndex();
				changeXZOrder(currentOrder);
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