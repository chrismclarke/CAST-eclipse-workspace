package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class ConditZApplet extends CoreRegnPlaneApplet {
	static final private String Z_LIMITS_PARAM = "zLimits";
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String ALLOW_BAND_PARAM = "allowBand";
	static final private String ALLOW_VAR_SELECTION_PARAM = "allowVarSelection";
	
	private boolean sliceZNotX;
	
	private NumValue sliderMin, sliderMax, sliderStart;
	private ParameterSlider valueSlider;
	
	private XCheckbox bandCheck;
	
	private ConditLinearEqnView conditEqn;
	private ConditRegn2DView conditScatterView;
	private XLabel zValueLabel;
	
	private XCheckbox[] paramCheck;
	private boolean[] paramInModel;
	
	protected void createYData(DataSet data, String yName) {
		super.createYData(data, yName);
		NumVariable yVar = (NumVariable)data.getVariable("y");
		if (yVar != null) {
			MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
			model.updateLSParams("y");
		}
		
		readSliderLimits();
	}
	
	private void readSliderLimits() {
		String zLimitsString = getParameter(Z_LIMITS_PARAM);
		sliceZNotX = (zLimitsString != null);
		
		if (sliceZNotX) {
			StringTokenizer st = new StringTokenizer(zLimitsString);
			sliderMin = new NumValue(st.nextToken());
			sliderMax = new NumValue(st.nextToken());
			sliderStart = new NumValue(st.nextToken());
		}
		else {
			StringTokenizer st = new StringTokenizer(getParameter(X_LIMITS_PARAM));
			sliderMin = new NumValue(st.nextToken());
			sliderMax = new NumValue(st.nextToken());
			sliderStart = new NumValue(st.nextToken());
		}
	}
	
	
	protected Rotate3DView getRotatingView(DataSet data, D3Axis yAxis, D3Axis xAxis, D3Axis zAxis) {
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ConditRegn3DView theView = new ConditRegn3DView(data, this, xAxis, yAxis, zAxis, "x",
												(yVar == null ? null : "y"), "z", "model", sliceZNotX, sliderStart.toDouble());
		return theView;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 35));
		
		thePanel.add(super.topPanel(data));
			theEqn.setLastDrawParameter(2);
			theEqn.setHighlightIndex(sliceZNotX ? 1 : 2);
			
			String allowBandString = getParameter(ALLOW_VAR_SELECTION_PARAM);
			if (allowBandString != null && allowBandString.equals("true"))
				thePanel.add(parameterPanel(data));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
			rotatePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
			rotatePanel.add(new RotateButton(sliceZNotX ? RotateButton.YX_ROTATE : RotateButton.YZ_ROTATE, theView, this));
		
		thePanel.add(rotatePanel);
		
		return thePanel;
	}
	
	private XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 4));
		
		XLabel heading = new XLabel(translate("Variables in Model"), XLabel.LEFT, this);
		heading.setFont(getStandardBoldFont());
		thePanel.add(heading);
		
		paramCheck = new XCheckbox[explanName.length];
		paramInModel = new boolean[explanName.length + 1];
		paramInModel[0] = true;
		for (int i=0 ; i<explanName.length ; i++) {
			paramCheck[i] = new XCheckbox(explanName[i], this);
			paramCheck[i].setState(true);
			paramInModel[i + 1] = true;
			thePanel.add(paramCheck[i]);
		}
		setBestParams();			//		paramInModel[] must be set before calling function
		if (theEqn != null)
			theEqn.setDrawParameters(paramInModel);
		return thePanel;
	}
	
	private void setBestParams() {
		double[] fixedB = new double[explanKey.length + 1];
		for (int i=0 ; i<fixedB.length ; i++)
			fixedB[i] = paramInModel[i] ? Double.NaN : 0.0;
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		model.updateLSParams("y", fixedB);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new FixedSizeLayout(50, 250));
			
			leftPanel.add(conditPanel(data));
		
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		
				valueSlider = new ParameterSlider(sliderMin, sliderMax, sliderStart,
													explanName[sliceZNotX ? 1 : 0], ParameterSlider.SHOW_MIN_MAX, this);
				valueSlider.setFont(getStandardFont());
				
			rightPanel.add(valueSlider);
			
				XPanel bottomRightPanel = new XPanel();
				bottomRightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
				
					conditEqn = new ConditLinearEqnView(data, this, "model", getShortYName(), getShortXNames(), minParam, maxParam);
					conditEqn.setCondit(sliceZNotX ? 1 : 0, sliderStart.toDouble());
					conditEqn.setForeground(Color.red);
					conditEqn.setFont(getStandardBoldFont());
			
				bottomRightPanel.add(conditEqn);
				
				String allowBandString = getParameter(ALLOW_BAND_PARAM);
				if (allowBandString == null || allowBandString.equals("true")) {
					bandCheck = new XCheckbox(translate("Show 95% band"), this);
					bottomRightPanel.add(bandCheck);
				}
			
			rightPanel.add(bottomRightPanel);
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		return thePanel;
	}
	
	private XPanel conditPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				StringTokenizer st = new StringTokenizer(translate("Mean of *, for"), "*");
				XLabel yNameLabel = new XLabel(st.nextToken() + yName + st.nextToken() + " "
										+ getShortXNames()[sliceZNotX ? 1 : 0] + " = ", XLabel.LEFT, this);
			topPanel.add(yNameLabel);
				
				zValueLabel = new XLabel(sliderStart.toString(), XLabel.LEFT, this);
			topPanel.add(zValueLabel);
		thePanel.add("North", topPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(sliceZNotX ? X_AXIS_INFO_PARAM : Z_AXIS_INFO_PARAM));
				horizAxis.setAxisName(explanName[sliceZNotX ? 0 : 1]);
			mainPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			mainPanel.add("Left", vertAxis);
			
				conditScatterView = new ConditRegn2DView(data, this, vertAxis, horizAxis, "model", sliceZNotX, sliderStart.toDouble());
				conditScatterView.lockBackground(Color.white);
			mainPanel.add("Center", conditScatterView);
		
		thePanel.add("Center", mainPanel);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (paramCheck != null)
			for (int i=0 ; i<paramCheck.length ; i++)
				if (target == paramCheck[i]) {
					paramInModel[i + 1] = paramCheck[i].getState();
					setBestParams();
					theEqn.setDrawParameters(paramInModel);
					data.variableChanged("model");
					
					return true;
				}
		
		if (target == valueSlider) {
			ConditRegn3DView conditView = (ConditRegn3DView)theView;
			conditView.setConditValue(valueSlider.getParameter().toDouble());
			conditScatterView.setConditValue(valueSlider.getParameter().toDouble());
			conditEqn.setCondit(sliceZNotX ? 1 : 0, valueSlider.getParameter().toDouble());
			zValueLabel.setText(valueSlider.getParameter().toString());
			return true;
		}
		else if (target == bandCheck) {
			ConditRegn3DView conditView = (ConditRegn3DView)theView;
			conditView.setShowBand(bandCheck.getState());
			conditScatterView.setShowBand(bandCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}