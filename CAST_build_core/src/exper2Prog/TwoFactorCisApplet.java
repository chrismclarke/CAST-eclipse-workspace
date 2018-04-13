package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;
import graphics3D.*;

import multivarProg.*;
import twoFactor.*;
import indicator.*;


public class TwoFactorCisApplet extends RotateApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
//	static final private String X_MODEL_TERMS_PARAM = "xModelTerms";
	
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
//	static final private String Z_MODEL_TERMS_PARAM = "zModelTerms";
	
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final private String MAX_PLUS_MINUS_PARAM = "maxPlusMinus";
	
	static final private String kXZKeys[] = {"x", "z"};
	
	private NumValue maxParam[];
	private NumValue maxPlusMinus[];
	
//	private DataSet data;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
			CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			String xLabels = MText.expandText(getParameter(X_LABELS_PARAM));
			xVar.readLabels(xLabels);
			xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
			CatVariable zVar = new CatVariable(getParameter(Z_VAR_NAME_PARAM));
			String zLabels = MText.expandText(getParameter(Z_LABELS_PARAM));
			zVar.readLabels(zLabels);
			zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		
			int nx = xVar.noOfCategories();
			int nz = zVar.noOfCategories();
		
			maxParam = new NumValue[3];		//	first for intercept
			maxPlusMinus = new NumValue[3];
			
			StringTokenizer stMax = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
			StringTokenizer stMaxPlusMin = new StringTokenizer(getParameter(MAX_PLUS_MINUS_PARAM));
			for (int i=0 ; i<3 ; i++) {
				maxParam[i] = new NumValue(stMax.nextToken());
				maxPlusMinus[i] = new NumValue(stMaxPlusMin.nextToken());
			}
			
			int[] paramDecimals = new int[nx + nz + 1];
			paramDecimals[0] = maxParam[0].decimals;
			for (int i=1 ; i<=nx ; i++)
				paramDecimals[i] = maxParam[1].decimals;
			for (int i=nx+1 ; i<=nx+nz ; i++)
				paramDecimals[i] = maxParam[2].decimals;
			
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, kXZKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
			TwoFactorModel lsFactorModel = new TwoFactorModel("lsFactor", data, kXZKeys,
																TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, false, 0.0);
			lsFactorModel.setLSParams("y");
			
		data.addVariable("lsFactor", lsFactorModel);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			int termsPerColumn[] = {3};
//			int termsPerColumn[] = {2, 1};
		TermEstimatesView paramView = new TermEstimatesView(data, this, kXZKeys, "ls", maxParam, maxPlusMinus, termsPerColumn);
		paramView.setFont(getBigFont());
		thePanel.add(paramView);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xAxis.setCatScale(xVar);
		
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		D3Axis zAxis = new D3Axis(getParameter(Z_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		CatVariable zVar = (CatVariable)data.getVariable("z");
		zAxis.setCatScale(zVar);
		
		RotateDragFactorsView localView = new RotateDragFactorsView(data, this, xAxis, yAxis, zAxis,
																										"x", "y", "z", "lsFactor");
		localView.setAllowDragParams(false);
		localView.setCrossColouring(RotateDragFactorsView.COLOURS);
		theView = localView;
		theView.lockBackground(Color.white);
		theView.setCrossSize(DataView.LARGE_CROSS);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
}