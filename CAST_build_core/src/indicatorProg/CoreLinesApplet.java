package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import axis.*;

import indicator.*;


abstract public class CoreLinesApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String Y_AXIS_PARAM = "yAxis";
	static final private String INIT_MODEL_PARAM = "initModelParams";
	static final protected String SHORT_NAMES_PARAM = "shortNames";
	static final private String SHOW_COEFFS_PARAM = "showCoeffs";
	
	static final protected Color kEqnBackgroundColor = new Color(0xD6E1FF);
	
	static final private String[] kXDataKey = {"x", "z"};
	static final private String[] kXHandleKey = {"xHandle", "zHandle"};
	static final protected String kYDataKey = "y";
	static final protected String kYHandleKey = "yHandle";
	
	protected DataSet data;
	
	private boolean showCoeffs;
	
	protected HorizAxis xAxis;
	protected VertAxis yAxis;
	protected DragParallelLinesView theView;
	
	protected int paramDecimals[];
	
	private XButton lsButton;
	private XCheckbox showResidCheck;
	
	public void setupApplet() {
		data = readData();
		
		String showCoeffsString = getParameter(SHOW_COEFFS_PARAM);
		showCoeffs = (showCoeffsString == null) || showCoeffsString.equals("true");
		
		createAxes(data);			//	must be done before handle variables are created
		
		createHandleVars(data);
		
		setLayout(new BorderLayout(10, 5));
			
		add("Center", displayPanel(data));
		if (showCoeffs)
			add("South", equationPanel(data));
		add("East", controlPanel(data));
	}
	
	protected String[] getXDataKeys() {
		return kXDataKey;
	}
	
	protected String[] getXHandleKeys() {
		return kXHandleKey;
	}
	
	protected void addExplanVariables(DataSet data) {
		String xKey = getXDataKeys()[0];
		String zKey = getXDataKeys()[1];
		String xLabels = getParameter(X_LABELS_PARAM);
		if (xLabels == null)
			data.addNumVariable(xKey, getParameter(X_VAR_NAME_PARAM),
																												getParameter(X_VALUES_PARAM));
		else
			data.addCatVariable(xKey, getParameter(X_VAR_NAME_PARAM),
																									getParameter(X_VALUES_PARAM), xLabels);
		data.addCatVariable(zKey, getParameter(Z_VAR_NAME_PARAM),
																getParameter(Z_VALUES_PARAM), getParameter(Z_LABELS_PARAM));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable(kYDataKey, getParameter(Y_VAR_NAME_PARAM),
																												getParameter(Y_VALUES_PARAM));
		addExplanVariables(data);
		
			MultipleRegnModel model = new MultipleRegnModel("Model", data, getXDataKeys(),
																													getParameter(INIT_MODEL_PARAM));
			int noOfParams = model.noOfParameters();
			paramDecimals = new int[noOfParams];
			for (int i=0 ; i<noOfParams ; i++)
				paramDecimals[i] = model.getParameter(i).decimals;
		data.addVariable("model", model);
		
		return data;
	}
	
	protected void fillXArray(Value[] x, Value xVal, Value zVal, Variable xVar, CatVariable zVar) {
		x[0] = xVal;
		x[1] = zVal;		//	subclass adds interactions
	}
	
	private CatVariable createCatCopyVariable(CatVariable dataVar) {
		CatVariable zVar = new CatVariable(dataVar.name);
		
			Value[] labels = new LabelValue[dataVar.noOfCategories()];
			for (int i=0 ; i<labels.length ; i++)
				labels[i] = dataVar.getLabel(i);
		zVar.setLabels(labels);			//		must be identical records to dataVar
		return zVar;
	}
	
	
	abstract protected void addBaselineHandles(Variable xVar, NumVariable yVar, CatVariable zVar,
												MultipleRegnModel model);
	
	
	abstract protected void addGroupHandles(Variable xVar, NumVariable yVar, CatVariable zVar,
												MultipleRegnModel model);
	
	
	protected void createHandleVars(DataSet data) {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		
		NumVariable yVar = new NumVariable("YHandle");
		
			CoreVariable xDataVar = data.getVariable(getXDataKeys()[0]);
		Variable xVar;
		if (xDataVar instanceof NumVariable)
			xVar = new NumVariable("XHandle");
		else
			xVar = createCatCopyVariable((CatVariable)xDataVar);
			
			CatVariable zDataVar = (CatVariable)data.getVariable(getXDataKeys()[1]);
		CatVariable zVar = createCatCopyVariable(zDataVar);
		
		addBaselineHandles(xVar, yVar, zVar, model);
		
		addGroupHandles(xVar, yVar, zVar, model);
		
		data.addVariable(getXHandleKeys()[0], xVar);
		data.addVariable(getXHandleKeys()[1], zVar);
		data.addVariable(kYHandleKey, yVar);
	}
	
	
	private void createAxes(DataSet data) {
		xAxis = new HorizAxis(this);
			CoreVariable xVar = data.getVariable(kXDataKey[0]);
			if (xVar instanceof CatVariable)
				xAxis.setCatLabels((CatVariable)xVar);
			else
				xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		
		yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
	}
	
	
	abstract protected DragParallelLinesView getLinesView(DataSet data);
	
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
				
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				xAxis.setAxisName(data.getVariable(getXDataKeys()[0]).name);
			dataPanel.add("Bottom", xAxis);
			
			dataPanel.add("Left", yAxis);
			
			theView = getLinesView(data);
			
				StringTokenizer st = new StringTokenizer(getParameter(SHORT_NAMES_PARAM));
				String yName = st.nextToken();
				String xName = st.nextToken();
				theView.setYXNames(yName, xName);
				
				theView.setShowCoeffs(showCoeffs);
			
				theView.lockBackground(Color.white);
			dataPanel.add("Center", theView);
		
		thePanel.add("Center", dataPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				XLabel yLabel = new XLabel(data.getVariable(kYDataKey).name, XLabel.LEFT, this);
				yLabel.setFont(yAxis.getFont());
			topPanel.add(yLabel);
		
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	
	abstract protected XPanel equationPanel(DataSet data);
	
	protected void addInteractionCheck(XPanel thePanel) {
		//		only for subclass with interaction
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			XPanel keyPanel = new XPanel();
			keyPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 0));
			
				XLabel zLabel = new XLabel(data.getVariable(getXDataKeys()[1]).name, XLabel.LEFT, this);
				zLabel.setFont(getBigFont());
			keyPanel.add(zLabel);
				
				CatKey zKey = new CatKey(data, getXDataKeys()[1], this, CatKey.VERT);
				zKey.setFont(getBigFont());
			keyPanel.add(zKey);
			
		thePanel.add(keyPanel);
		
		addInteractionCheck(thePanel);
		
			showResidCheck = new XCheckbox(translate("Show residuals"), this);
		thePanel.add(showResidCheck);
		
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
		
		return thePanel;
	}
	
	protected void updateHandleY(MultipleRegnModel model) {
		Variable xVariable = (Variable)data.getVariable(getXHandleKeys()[0]);
		CatVariable zVariable = (CatVariable)data.getVariable(getXHandleKeys()[1]);
		NumVariable yVariable = (NumVariable)data.getVariable(kYHandleKey);
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		ValueEnumeration ze = zVariable.values();
		Value[] x = new Value[getXDataKeys().length];
		while (xe.hasMoreValues() && ze.hasMoreValues() && ye.hasMoreValues()) {
			fillXArray(x, xe.nextValue(), ze.nextValue(), xVariable, zVariable);
			NumValue y = (NumValue)ye.nextValue();
			if (!Double.isNaN(y.toDouble()))
				y.setValue(model.evaluateMean(x));
		}
	}
	
	protected double[] getConstraints() {
		return null;
	}
	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
			model.setXKey(getXDataKeys());
			model.setLSParams(kYDataKey, getConstraints(), paramDecimals, 0);
			updateHandleY(model);
			data.variableChanged("model");
			
			return true;
		}
		else if (target == showResidCheck) {
			theView.setDrawResiduals(showResidCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}