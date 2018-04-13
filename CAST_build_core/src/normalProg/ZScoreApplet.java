package normalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreVariables.*;
import formula.*;


import normal.*;


public class ZScoreApplet extends XApplet {
	static final private String MODEL_PARAM_PARAM = "modelParams";
	static final private String X_SLIDER_PARAM = "xSlider";
	static final private String SLIDER_QN_PARAM = "sliderQn";
	
	static final private String kStdNormalDistn = "0 1";
	
	static final private Color kInnerColor = new Color(0x3366FF);
	static final private Color kTailColor = new Color(0xCCCCCC);
	
	static final protected Color kFormulaBackground = new Color(0xDDDDFF);
	
	static final private int kZDecimals = 2;
	
	protected NumValue mean, sd, maxX;
	
	protected DataSet data;
	
	private StdNormalView normView;
	
	protected ParameterSlider xSlider; 
	
	public void setupApplet() {
		readParameters();
		data = getData();
		
		setLayout(new BorderLayout(0, verticalGap()));
		add("North", sliderPanel());
		add("Center", displayPanel(data));
		add("South", formulaPanel(data));
		
		setValuesFromSlider(xSlider);
	}
	
	protected int verticalGap() {
		return 20;
	}
	
	protected NumValue getMaxX(NumValue minSliderValue, NumValue maxSliderValue) {
		return new NumValue((minSliderValue.toString().length()
																										> maxSliderValue.toString().length())
														? minSliderValue : maxSliderValue);
	}
	
	private void readParameters() {
		StringTokenizer st = new StringTokenizer(getParameter(MODEL_PARAM_PARAM));
		mean = new NumValue(st.nextToken());
		sd = new NumValue(st.nextToken());
		
		st = new StringTokenizer(getParameter(X_SLIDER_PARAM));
		NumValue minValue = new NumValue(st.nextToken());
		NumValue maxValue = new NumValue(st.nextToken());
		maxX = getMaxX(minValue, maxValue);
		NumValue startValue = new NumValue(st.nextToken());
		
		String paramName = getParameter(SLIDER_QN_PARAM);
		
		xSlider = new ParameterSlider(minValue, maxValue, startValue, paramName, this);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NormalDistnVariable zDistn = new NormalDistnVariable("zDistn");
			zDistn.setParams(kStdNormalDistn);
			zDistn.setMinSelection(0.0);
			zDistn.setMaxSelection(0.0);
		data.addVariable("zDistn", zDistn);
		
			NumVariable xVar = new NumVariable(getParameter(VAR_NAME_PARAM));
			xVar.addValue(new NumValue(mean.toDouble(), maxX.decimals));
		data.addVariable("x", xVar);
			
			ScaledVariable zVar = new ScaledVariable(translate("z-score"), xVar, "x", -mean.toDouble() / sd.toDouble(),
															1.0 / sd.toDouble(), kZDecimals);
		data.addVariable("z", zVar);
		
		data.setSelection(0);
		
		return data;
	}
	
	protected StdNormalView creatStdNormView(DataSet data, HorizAxis axis) {
		StdNormalView view = new StdNormalView(data, this, axis, "zDistn");
		view.setTopZValue(0.0);
		view.setDistnColors(kInnerColor, kTailColor);
		view.lockBackground(Color.white);
		return view;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			StdNormalAxis modelAxis = new StdNormalAxis(this);
			modelAxis.setupStdAxis(mean, sd);
		thePanel.add("Bottom", modelAxis);
		
			StdNormalAxis theHorizAxis = new StdNormalAxis(this);
			theHorizAxis.setupStdAxis();
		thePanel.add("Bottom", theHorizAxis);
		
			normView = creatStdNormView(data, theHorizAxis);
		thePanel.add("Center", normView);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			xSlider.setFont(getBigBoldFont());
		
		thePanel.add(xSlider);
		return thePanel;
	}
	
	protected XPanel formulaPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 4);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				ZFormulaPanel form2 = new ZFormulaPanel(data, "x", "z", maxX, mean, sd, bigContext);
			innerPanel.add(form2);
			
				StdProbFormulaPanel form1 = new StdProbFormulaPanel(data, "x", "z", maxX, bigContext);
				form1.setFont(getBigFont());
			innerPanel.add(form1);
		
			innerPanel.lockBackground(kFormulaBackground);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	protected void setValuesFromSlider(ParameterSlider xSlider) {
		double x = xSlider.getParameter().toDouble();
		double z = (x - mean.toDouble()) / sd.toDouble();
		setZValue(z);
		
		NumVariable xVar = (NumVariable)data.getVariable("x");
		NumValue x0 = (NumValue)xVar.valueAt(0);
		x0.setValue(x);
		
		data.valueChanged(0);
	}
	
	protected void setZValue(double z) {
		if (normView != null)
			normView.setTopZValue(z);
	}

	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			setValuesFromSlider(xSlider);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}