package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import linMod.*;


public class ResidDistnApplet extends RotateApplet {
	static final protected String SORTED_X_PARAM = X_VALUES_PARAM;
	static final protected String ERROR_SD_AXIS_PARAM = "errorAxis";
	static final protected String RESID_ROTATION_PARAM = "initialResidRotation";
	
	static final protected String kDefaultZAxis = "0.0 1.0 2.0 1.0";
	
	private SimpleRegnDataSet regnData;
	
	private RotatePDFView residView;
	private XButton residRotateButton;
	
	protected DataSet readData() {
		regnData = new SimpleRegnDataSet(this);
		regnData.addVariable("residDistn", new ResidDistnModel("Resid distn", regnData, "x", "model"));
		return regnData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
			XLabel modelLabel = new XLabel(translate("Model"), XLabel.CENTER, this);
			modelLabel.setFont(getBigBoldFont());
		thePanel.add(ProportionLayout.LEFT, modelLabel);
		
			XLabel residLabel = new XLabel(translate("Distn of residuals"), XLabel.CENTER, this);
			residLabel.setFont(getBigBoldFont());
		thePanel.add(ProportionLayout.RIGHT, residLabel);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, dataPanel(data));
		thePanel.add(ProportionLayout.RIGHT, residPanel(data));
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		xAxis.setNumScale(regnData.getXAxisInfo());
		D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		yAxis.setNumScale(regnData.getYAxisInfo());
		D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(kDefaultZAxis);
		
		RotatePDFView localView = new RotatePDFView(data, this, xAxis, yAxis,
												zAxis, "model", "x", "y", getParameter(SORTED_X_PARAM));
		localView.setPopnMeanColor(Color.red);
		localView.lockBackground(Color.white);
		thePanel.add("Center", localView);
		
		theView = localView;
		return thePanel;
	}
	
	private XPanel residPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		xAxis.setNumScale(regnData.getXAxisInfo());
		D3Axis yAxis = new D3Axis(translate("Residual"), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		yAxis.setNumScale(getParameter(ERROR_SD_AXIS_PARAM));
		D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(kDefaultZAxis);
		
		residView = new RotatePDFView(data, this, xAxis, yAxis,
												zAxis, "residDistn", "x", "y", getParameter(SORTED_X_PARAM));
		residView.setPopnMeanColor(Color.red);
		residView.lockBackground(Color.white);
		
			StringTokenizer st = new StringTokenizer(getParameter(RESID_ROTATION_PARAM));
			int theta1 = Integer.parseInt(st.nextToken());
			int theta2 = Integer.parseInt(st.nextToken());
		residView.setInitialRotation(theta1, theta2);
		
		thePanel.add("Center", residView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			leftPanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			leftPanel.add(rotateButton);
		
		thePanel.add(ProportionLayout.LEFT, leftPanel);
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			rightPanel.add(new RotateButton(RotateButton.XYZ_ROTATE, residView, this));
				residRotateButton = new XButton(translate("Spin"), this);
			rightPanel.add(residRotateButton);
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
			
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == residRotateButton) {
			residView.startAutoRotation();
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