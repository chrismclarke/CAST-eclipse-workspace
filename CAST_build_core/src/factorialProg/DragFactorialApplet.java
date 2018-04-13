package factorialProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import factorial.*;


public class DragFactorialApplet extends RotateApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String START_MEAN_PARAM = "startMean";
	static final private String START_MODEL_PARAM = "startModel";
	static final private String MAX_INTERACT_PARAM = "maxInteract";
	static final private String SHOW_RESID_PARAM = "showResiduals";
	
	static final protected String X_LONG_NAME_PARAM = "xLongName";
	static final protected String Z_LONG_NAME_PARAM = "zLongName";
	static final protected String W_LONG_NAME_PARAM = "wLongName";
	
	private FactorialDataSet data;
	
	protected XButton lsButton;
	
	protected boolean hasY, hasModel, alwaysLS;
	
	protected DataSet readData() {
		int[][] startModel = null;
		String startModelString = getParameter(START_MODEL_PARAM);
		if (startModelString != null) {
			StringTokenizer st = new StringTokenizer(startModelString);
			if (st.countTokens() == 0)		//	need to distinguish between no model and model with no terms
				startModel = new int[0][];
			else {
				st = new StringTokenizer(startModelString, "+");
				startModel = new int[st.countTokens()][];
				for (int i=0 ; i<startModel.length ; i++) {
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "* ");
					startModel[i] = new int[st2.countTokens()];
					for (int j=0 ; j<startModel[i].length ; j++)
						startModel[i][j] = Integer.parseInt(st2.nextToken());
				}
			}
		}
		
		String maxInteractString = getParameter(MAX_INTERACT_PARAM);
		int maxInteract = (maxInteractString == null) ? -1 : Integer.parseInt(maxInteractString);
		data = new FactorialDataSet(maxInteract, startModel, this);
		
		MultiFactorModel model = (MultiFactorModel)data.getVariable("model");
		hasModel = model != null;
		if (hasModel) {
			String startMeanString = getParameter(START_MEAN_PARAM);
			alwaysLS = startMeanString.equals("ls");
			hasY = data.getVariable("y") != null;
			if (hasY && alwaysLS)
				model.updateLSParams("y");
			
			if (!alwaysLS) {
				double newMean = Double.parseDouble(startMeanString);
				model.setConstantMeans(newMean);
			}
		}
/*	
		int levels[] = new int[3];
		for (int i=0 ; i<2 ; i++)
			for (int j=0 ; j<2 ; j++)
				for (int k=0 ; k<2 ; k++) {
					levels[0] = i;
					levels[1] = j;
					levels[2] = k;
					System.out.println("mean[" + i + ", " + j + ", " + k + "] = " + model.evaluateMean(levels));
				}
*/
		
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
		
		String xKey = ((FactorialDataSet)data).getMainEffectKey(0);
		CatVariable xVar = (CatVariable)data.getVariable(xKey);
		String xName = getParameter(X_LONG_NAME_PARAM);
		if (xName == null)
			xName = xVar.name;
		D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setCatScale(xVar);
		
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		String zKey = ((FactorialDataSet)data).getMainEffectKey(1);
		CatVariable zVar = (CatVariable)data.getVariable(zKey);
		String zName = getParameter(Z_LONG_NAME_PARAM);
		if (zName == null)
			zName = zVar.name;
		D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setCatScale(zVar);
		
		String wKey = ((FactorialDataSet)data).getMainEffectKey(2);
		String wName = getParameter(W_LONG_NAME_PARAM);
		if (wName == null)
			wName = data.getVariable(wKey).name;
		theView = new DragThreeFactorView(data, this, xAxis, yAxis, zAxis, xKey, "y", zKey, wKey,
																										(hasModel ? "model" : null), wName);
		theView.setCrossSize(DataView.LARGE_CROSS);
		theView.lockBackground(Color.white);
		theView.setDrawData(data.getVariable("y") != null);
		((DragThreeFactorView)theView).setAllowDragParams(!alwaysLS);
		
		String showResidString = getParameter(SHOW_RESID_PARAM);
		if (showResidString != null && showResidString.equals("true"))
			((DragThreeFactorView)theView).setResidualDisplay(DragThreeFactorView.LINE_RESIDUALS);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		if (hasModel)
			return new FactorialEffectPanel(data, "model", alwaysLS, "y",
																					FactorialEffectPanel.VERTICAL, translate("Model terms"), this);
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			return thePanel;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel rotatePanel = RotateButton.createRotationPanel(theView, this, RotateButton.HORIZONTAL);
			rotateButton = new XButton(translate("Spin"), this);
		rotatePanel.add(rotateButton);
		
		if (hasY && hasModel && !alwaysLS) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			thePanel.add(rotatePanel);
			
				lsButton = new XButton(translate("Least squares"), this);
			thePanel.add(lsButton);
			
			return thePanel;
		}
		else
			return rotatePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			MultiFactorModel model = (MultiFactorModel)data.getVariable("model");
			model.updateLSParams("y");
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}