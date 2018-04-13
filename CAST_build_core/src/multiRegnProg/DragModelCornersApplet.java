package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import valueList.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class DragModelCornersApplet extends RotateApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String MIN_VAR_ROTATION_PARAM = "minVarRotation";
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	static final private String FIXED_DRAG_PARAM = "fixedDrag";
								//		remove // in RotateMap.setAngles() to see angles in console
	
	static final private NumValue kMaxR2 = new NumValue(1.0, 4);
	static final private String kResidKeyArray[] = {"resid"};
	
	static final private Color kRssBackground = new Color(0xDDDDEE);
	
	private MultiRegnDataSet data;
	private SummaryDataSet summaryData;
	private NumValue maxSsq;
	
	private XButton minVarButton, lsButton;
	private XChoice dragTypeChoice;
	private int currentDragType = 0;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		MultipleRegnModel lsModel = (MultipleRegnModel)data.getVariable("ls");
		MultipleRegnModel dragModel = new MultipleRegnModel(data.getYVarName(), data,
																														MultiRegnDataSet.xKeys);
		for (int i=0 ; i<3 ; i++)
			dragModel.setParameter(i, lsModel.getParameter(i));
		data.addVariable("dragModel", dragModel);
		
		
		int decimals = data.getResponseDecimals();
			BasicComponentVariable residComp = new BasicComponentVariable("resid", data,
																						MultiRegnDataSet.xKeys, "y", "dragModel",
																						BasicComponentVariable.RESIDUAL, decimals);
		data.addVariable("resid", residComp);
		
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
																	kResidKeyArray, maxSsq.decimals, kMaxR2.decimals);
		
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
		
		MultiRegnDataSet multiData = (MultiRegnDataSet)data;
		
		D3Axis xAxis = new D3Axis(multiData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(multiData.getXAxisInfo());
		
		D3Axis yAxis = new D3Axis(multiData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(multiData.getYAxisInfo());
		
		D3Axis zAxis = new D3Axis(multiData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(multiData.getZAxisInfo());
		
		theView = new DragPlaneHandlesView(data, this, xAxis, yAxis, zAxis, "dragModel",
								MultiRegnDataSet.xKeys, "y", summaryData);
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
		String dragType = getParameter(FIXED_DRAG_PARAM);
		if (dragType == null) {
			XPanel dragTypePanel = new XPanel();
			dragTypePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				XLabel dragLabel = new XLabel(translate("Drag") + ":", XLabel.LEFT, this);
			dragTypePanel.add(dragLabel);
			
				dragTypeChoice = new XChoice(this);
				dragTypeChoice.addItem(translate("Edges"));
				dragTypeChoice.addItem(translate("Corners"));
			dragTypePanel.add(dragTypeChoice);
		
			thePanel.add(dragTypePanel);
		}
		else
			((DragPlaneHandlesView)theView).setDragEdgeNotCorner(dragType.equals("edges"));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
			rotatePanel.add(new RotateButton(RotateButton.XZ_ROTATE, theView, this));
			rotatePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
			String rotationString = getParameter(MIN_VAR_ROTATION_PARAM);
			if (rotationString != null) {
				minVarButton = new XButton(translate("Show min variability"), this);
				rotatePanel.add(minVarButton);
			}
		
		thePanel.add(rotatePanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 7, 0, 0);
		thePanel.setLayout(new BorderLayout(30, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
				String yName = data.getVariable("y").name;
				String xName[] = new String[2];
				xName[0] =	data.getVariable("x").name;
				xName[1] =	data.getVariable("z").name;
				StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
				NumValue maxParam[] = new NumValue[3];
				for (int i=0 ; i<3 ; i++)
					maxParam[i] = new NumValue(st.nextToken());
			
			mainPanel.add(new MultiLinearEqnView(data, this, "dragModel", yName, xName, maxParam, maxParam));
				
				XPanel rssPanel = new InsetPanel(10, 5);
				rssPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					OneValueView rssView = new OneValueView(summaryData, "resid", this, maxSsq);
					rssView.setLabel(translate("Residual sum of squares") + " = ");
					rssView.setFont(getBigFont());
					
				rssPanel.add(rssView);
			
				rssPanel.lockBackground(kRssBackground);
			mainPanel.add(rssPanel);
		
		thePanel.add("West", mainPanel);
		
			XPanel lsPanel = new XPanel();
			lsPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
				
				lsButton = new XButton(translate("Least squares"), this);
			lsPanel.add(lsButton);
			
		thePanel.add("Center", lsPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == minVarButton) {
			StringTokenizer st = new StringTokenizer(getParameter(MIN_VAR_ROTATION_PARAM));
			double roundDens = Double.parseDouble(st.nextToken());
			double ofDens = Double.parseDouble(st.nextToken());
			theView.animateRotateTo(roundDens, ofDens);
			return true;
		}
		else if (target == lsButton) {
			MultipleRegnModel dragModel = (MultipleRegnModel)data.getVariable("dragModel");
			dragModel.updateLSParams("y");
			data.variableChanged("dragModel");
			summaryData.setSingleSummaryFromData();
			
			return true;
		}
		else if (target == dragTypeChoice) {
			int newChoice = dragTypeChoice.getSelectedIndex();
			if (currentDragType != newChoice) {
				currentDragType = newChoice;
				((DragPlaneHandlesView)theView).setDragEdgeNotCorner(currentDragType == 0);
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