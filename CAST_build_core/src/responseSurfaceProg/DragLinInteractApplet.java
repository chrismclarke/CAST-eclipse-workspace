package responseSurfaceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class DragLinInteractApplet extends RotateApplet {
	static final private String START_MEAN_PARAM = "startMean";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
//	static final private String CONTOUR_AXIS_PARAM = "contourAxis";
	static final private String FIXED_CONTOURS_PARAM = "fixedContours";
	static final private String MAX_COEFFS_PARAM = "maxCoeffs";
	static final private String SHORT_NAMES_PARAM = "shortNames";
	
	static final private String kXKey[] = {"x", "z"};
	static final private String kHandleXKey[] = {"xHandle", "zHandle"};
	
	static final private Color kOrange = new Color(0xFF6600);
	static final private Color kDarkGreen = new Color(0x009900);
	static final private Color kKeyColor[] = {kDarkGreen, kOrange, Color.blue};
	
	static final private boolean kLinInteractShown[] = {true, true, true, false, false, true};
	
	private String xName, zName, yName;
	private DataSet data;
	
	private NumValue maxParam[];
	
	private ColourMap colourMap;
	
	private MultiLinearEqnView theEqn;
	private XButton lsButton;
	
	private XChoice displayTypeChoice;
	private int currentDisplayType = 0;
	
	private void addModel(DataSet data, String modelKey) {
		NumVariable yHandleVar = new NumVariable("yHandle");
		NumVariable xHandleVar = new NumVariable(kHandleXKey[0]);
		NumVariable zHandleVar = new NumVariable(kHandleXKey[1]);
		for (int i=0 ; i<6 ; i++) {
			yHandleVar.addValue(new NumValue(0.0, 0));
			xHandleVar.addValue(new NumValue(0.0, 0));
			zHandleVar.addValue(new NumValue(0.0, 0));
		}
		data.addVariable("yHandle", yHandleVar);
		data.addVariable("xHandle", xHandleVar);
		data.addVariable("zHandle", zHandleVar);
		
			maxParam = new NumValue[6];
			StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFFS_PARAM));
			for (int i=0 ; i<6 ; i++)
				maxParam[i] = new NumValue(st.nextToken());
		
			ResponseSurfaceModel model = new ResponseSurfaceModel("Model", data, kXKey);
			for (int i=0 ; i<6 ; i++)
				model.setParameter(i, new NumValue(0.0, maxParam[i].decimals));
		
		data.addVariable(modelKey, model);
	}
	
	private void addDataValues(DataSet data) {
			yName = getParameter(Y_VAR_NAME_PARAM);
			String yValues = getParameter(Y_VALUES_PARAM);
		data.addNumVariable("y", yName, yValues);
		
			xName = getParameter(X_VAR_NAME_PARAM);
			String xValues = getParameter(X_VALUES_PARAM);
		data.addNumVariable("x", xName, xValues);
		
			zName = getParameter(Z_VAR_NAME_PARAM);
			String zValues = getParameter(Z_VALUES_PARAM);
		data.addNumVariable("z", zName, zValues);
	}
	
	private void setupColorMap() {
		double[] keyValues = getKeyValues();
		colourMap = new ColourMap(kKeyColor, keyValues);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		addDataValues(data);
		addModel(data, "model");
		
		setupColorMap();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(SHORT_NAMES_PARAM));
			String shortYName = st.nextToken();
			String shortXName = st.nextToken();
			String shortZName = st.nextToken();
			String shortNames[] = {shortXName, shortZName, shortXName, shortZName, shortXName + shortZName};
			
			theEqn = new MultiLinearEqnView(data, this, "model", shortYName, shortNames, maxParam, maxParam);
			theEqn.setDrawParameters(kLinInteractShown);
		thePanel.add(theEqn);
		return thePanel;
	}
	
	private double[] getKeyValues() {
		double keyValue[] = new double[kKeyColor.length];
		StringTokenizer st = new StringTokenizer(getParameter(SHADE_CUTOFF_PARAM));
		for (int i=0 ; i<keyValue.length ; i++)
			keyValue[i] = Double.parseDouble(st.nextToken());
		return keyValue;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
/*		
			VertAxis scaleAxis = new VertAxis(this);
			scaleAxis.readNumLabels(getParameter(CONTOUR_AXIS_PARAM));
		thePanel.add("Left", scaleAxis);
		
			ContourControlView scaleView = new ContourControlView(data, null, this,
															scaleAxis, "model", colourMap, Double.NaN, DataView.BUFFERED);
		
		thePanel.add("LeftMargin", scaleView);
*/		
			D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
				double startY = Double.parseDouble(getParameter(START_MEAN_PARAM));
			DragResponseSurfaceView localView = new DragResponseSurfaceView(data, this, xAxis, yAxis, zAxis, "model",
												kXKey, "y", kHandleXKey[0], kHandleXKey[1], "yHandle");
			localView.resetModel(startY);
			localView.lockBackground(Color.white);
			localView.setAllowTerm(5, true);
			localView.setColourMap(colourMap);
//			localView.setContourControl(scaleView);
			localView.setCrossSize(DataView.LARGE_CROSS);
			
			StringTokenizer st = new StringTokenizer(getParameter(FIXED_CONTOURS_PARAM));
			double fixedContours[] = new double[st.countTokens()];
			for (int i=0 ; i<fixedContours.length ; i++)
				fixedContours[i] = Double.parseDouble(st.nextToken());
			localView.setFixedContours(fixedContours);
		
			theView = localView;
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			XPanel rotatePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
			
		thePanel.add(rotatePanel);
			
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
				
				XLabel displayLabel = new XLabel(translate("Display type") + ":", XLabel.CENTER, this);
				displayLabel.setFont(getStandardBoldFont());
			choicePanel.add(displayLabel);
				
				displayTypeChoice = new XChoice(this);
				displayTypeChoice.addItem(translate("Shaded surface"));
				displayTypeChoice.addItem(translate("Selected contours"));
				displayTypeChoice.addItem(translate("Grid"));
			choicePanel.add(displayTypeChoice);
			
		thePanel.add(choicePanel);
		
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			((DragResponseSurfaceView)theView).setDataLsModel();
			data.variableChanged("model");
			return true;
		}
		else if (target == displayTypeChoice) {
			int newChoice = displayTypeChoice.getSelectedIndex();
			if (newChoice != currentDisplayType) {
				currentDisplayType = newChoice;
				
				int displayType = (newChoice == 0) ? ResponseSurfaceView.SURFACE
													: (newChoice == 1) ? ResponseSurfaceView.CONTOURS
													: ResponseSurfaceView.GRID;
				((ResponseSurfaceView)theView).setDrawType(displayType);
				theView.repaint();
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