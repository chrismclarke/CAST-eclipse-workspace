package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import curveInteract.*;


public class QuadParamTestApplet extends ChooseTermsQuadApplet {
	static final private String MAX_TABLE_ENTRIES_PARAM = "maxTableEntries";
	static final private String PARAM_NAMES_PARAM = "paramNames";
	
	static final protected Color kTableBackground = new Color(0xDDDDEE);
	static final private Color kDimParamColor = new Color(0x999999);
	
	protected int paramDecimals[] = new int[6];
	
	private String paramName[] = new String[6];
	private Color paramColor[] = new Color[6];
	
	protected void readMinMaxParams() {
		super.readMinMaxParams();
		
		for (int i=0 ; i<6 ; i++)
			paramDecimals[i] = Math.max(minParam[i].decimals, maxParam[i].decimals);
		
		StringTokenizer st = new StringTokenizer(getParameter(PARAM_NAMES_PARAM));
		for (int i=0 ; i<6 ; i++) {
			paramName[i] = st.nextToken();
			if (paramName[i].equals("?"))
				paramName[i] = null;
		}
		for (int i=0 ; i<3 ; i++)
			paramColor[i] = kDimParamColor;
		for (int i=3 ; i<6 ; i++)
			if (paramName[i] != null)
				paramColor[i] = Color.black;
	}
	
	protected DataSet readData() {
		data = super.readData();
		
		double constraints[] = new double[6];
		for (int i=0 ; i<6 ; i++)
			constraints[i] = (paramName[i] == null) ? 0.0 : Double.NaN;
		
		ResponseSurfaceModel model = (ResponseSurfaceModel)data.getVariable("model");
		model.updateLSParams("y", constraints);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		
		boolean drawParam[] = new boolean[6];
		for (int i=0 ; i<6 ; i++)
			drawParam[i] = paramName[i] != null;
		equationView.setDrawParameters(drawParam);
			
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		((ResponseSurfaceView)theView).setDrawResids(false);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 6, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(20, 7);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
				NumValue maxParam = new NumValue(st.nextToken());
				NumValue maxSE = new NumValue(st.nextToken());
				NumValue maxT = new NumValue(st.nextToken());
				ParamTestsView testTable = new ParamTestsView(data, this,
												"model", "y", paramName, paramColor, maxParam, maxSE, maxT);
			innerPanel.add(testTable);
		
			innerPanel.lockBackground(kTableBackground);
		thePanel.add(innerPanel);
		
		return thePanel;
	}
}