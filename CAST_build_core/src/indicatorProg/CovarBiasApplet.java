package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import axis.*;
import random.*;

import indicator.*;


public class CovarBiasApplet extends XApplet {
//	static final private String DATA_NAMES_PARAM = "dataNames";
	static final private String THEORY_PARAMS_PARAM = "theoryParams";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
//	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VAR_NAME_PARAM = "xVarName";
//	static final private String X_VALUES_PARAM = "xValues";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
//	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String Y_AXIS_PARAM = "yAxis";
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final private String N_VALUES_PARAM = "nValues";
	static final private String SLIDER_NAMES_PARAM = "sliderNames";
	static final private String LS_WITH_COVAR_PARAM = "lsWithCovar";
	
	static final private String[] kXKeys = {"x", "z"};
	
	static final private double[] kBiasedConstraints = {Double.NaN, 0.0, Double.NaN};
	
	private NumValue maxParam[];
	private int paramDecimals[];
	
	private int nValues;
	private double errorSd;
	private double[] sortedX;
	private DataSet data;
	
	private HorizAxis xAxis;
	private SimpleParallelLinesView theView;
	
	private XPanel controlPanel;
	private CardLayout controlPanelLayout;
	
	private boolean lsWithCovar = false;
	private XCheckbox useCovarCheck, extraVarCheck;
	private XButton sampleButton;
	private XChoice dataChoice;
	private int currentDataIndex = 0;
	private XNoValueSlider orthogonalSlider;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 15));
			
		add("Center", displayPanel(data));
		add("North", topPanel(data));
		add("South", controlPanel(data));
		add("East", rightPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
			maxParam = new NumValue[kXKeys.length + 1];
			for (int i=0 ; i<=kXKeys.length ; i++)
				maxParam[i] = new NumValue(st.nextToken());
			
			paramDecimals = new int[kXKeys.length + 1];
			for (int i=0 ; i<=kXKeys.length ; i++)
				paramDecimals[i] = maxParam[i].decimals;
		
		nValues = Integer.parseInt(getParameter(N_VALUES_PARAM));
		
//			NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
//		data.addVariable("y", yVar);
		
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			sortedX = getSortedX(xVar);
		data.addVariable("x", xVar);
		
		CatVariable zVar = new CatVariable(getParameter(Z_VAR_NAME_PARAM));
		zVar.readLabels(getParameter(Z_LABELS_PARAM));
		data.addVariable("z", zVar);
		
			NumValue startParams[] = new NumValue[maxParam.length];
			for (int i=0 ; i<startParams.length ; i++)
				startParams[i] = new NumValue(maxParam[i]);
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, kXKeys, startParams);
		data.addVariable("ls", lsModel);
		
			MultipleRegnModel model = new MultipleRegnModel("model", data, kXKeys,
																												getParameter(THEORY_PARAMS_PARAM));
			errorSd = model.evaluateSD().toDouble();
			model.setSD(0.0);
		data.addVariable("model", model);
		
			RandomNormal errorGen = new RandomNormal(nValues, 0.0, 1.0, 3.0);
			NumSampleVariable errorVar = new NumSampleVariable(translate("Error"), errorGen, 9);
			errorVar.generateNextSample();
		data.addVariable("error", errorVar);
		
			ResponseVariable yVar = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),data,
																					kXKeys, "error", "model", 9);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private double[] getSortedX(NumVariable xVar) {
		xAxis = new HorizAxis(this);
		xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		xAxis.setAxisName(xVar.name);
		
		double middleX = (xAxis.minOnAxis + xAxis.maxOnAxis) / 2;
		RandomRectangular xGenerator = new RandomRectangular(nValues / 2, xAxis.minOnAxis,
																																							middleX);
		xGenerator.setNeatening(0.3);
		double lowX[] = xGenerator.generate();
		sortValues(lowX);
		xGenerator = new RandomRectangular(nValues / 2, middleX, xAxis.maxOnAxis);
		xGenerator.setNeatening(0.3);
		double highX[] = xGenerator.generate();
		sortValues(highX);
		
		double[] x = new double[nValues];
		System.arraycopy(lowX, 0, x, 0, lowX.length);
		System.arraycopy(highX, 0, x, lowX.length, highX.length);
		
		return x;
	}
	
	private void sortValues(double[] x) {
		for (int i=1 ; i<x.length ; i++)
			for (int j=i-1 ; j>=0 ; j--)
				if (x[j] > x[j + 1]) {
					double temp = x[j];
					x[j] = x[j + 1];
					x[j + 1] = temp;
				}
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
				
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
			dataPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			dataPanel.add("Left", yAxis);
			
				theView = new SimpleParallelLinesView(data, this, xAxis, yAxis,
																						kXKeys, null, "model", paramDecimals);
				theView.setFont(getBigBoldFont());
				theView.lockBackground(Color.white);
				
			dataPanel.add("Center", theView);
		
		thePanel.add("Center", dataPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
				yLabel.setFont(yAxis.getFont());
			topPanel.add(yLabel);
		
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		controlPanel = new XPanel();
			controlPanelLayout = new CardLayout();
		controlPanel.setLayout(controlPanelLayout);
		
		controlPanel.add("Model", new XPanel());
		controlPanel.add("Slider", sliderPanel(data));
		controlPanel.add("Sample", samplePanel(data));
		
		return controlPanel;
	}
	
	private XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(70, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			StringTokenizer st = new StringTokenizer(getParameter(SLIDER_NAMES_PARAM), "#");
			String titleText = st.nextToken();
			orthogonalSlider = new XNoValueSlider(st.nextToken(), st.nextToken(), titleText,
													0, nValues / 2, nValues / 4, this);
		thePanel.add("Center", orthogonalSlider);
		
		return thePanel;
	}
	
	private XPanel samplePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			sampleButton = new XButton(translate("Repeat experiment"), this);
			
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			dataChoice = new XChoice(translate("Display"), XChoice.HORIZONTAL, this);
			dataChoice.addItem(translate("Theory"));
			dataChoice.addItem(translate("Artificial data"));
			dataChoice.addItem(translate("Randomised experiment"));
		
		thePanel.add(dataChoice);
		
		return thePanel;
	}
	
	
	private XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.CENTER, 50));
		
			XPanel keyPanel = new XPanel();
			keyPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 0));
			
				XLabel zLabel = new XLabel(data.getVariable("z").name, XLabel.LEFT, this);
				zLabel.setFont(getBigFont());
			keyPanel.add(zLabel);
				
				CatKey zKey = new CatKey(data, "z", this, CatKey.VERT);
				zKey.setFont(getBigFont());
			keyPanel.add(zKey);
			
		thePanel.add(keyPanel);
		
		String lsString = getParameter(LS_WITH_COVAR_PARAM);
		if (lsString.equals("check")) {
			useCovarCheck = new XCheckbox(translate("LS with covariate"), this);
			useCovarCheck.disable();
			thePanel.add(useCovarCheck);
		}
		lsWithCovar = lsString.equals("always");
		
		extraVarCheck = new XCheckbox(translate("Extra variation"), this);
		extraVarCheck.disable();
		thePanel.add(extraVarCheck);
		
		return thePanel;
	}
	
	private void updateZValues() {
		CatVariable zVar = (CatVariable)data.getVariable("z");
		int index = orthogonalSlider.getValue();
		int zVal[] = new int[nValues];
		for (int i=0 ; i<nValues/2 ; i++) {
			if (i % 2 == 0) {							//	even
				if (index < nValues / 4)		//	all get treatment at low index
					zVal[i] = 1;
				else {
					int highTreat = (index - nValues / 4) * 2;
					zVal[i] = i < highTreat ? 0 : 1;
				}
			}
			else {												//	odd
				if (index >= nValues / 4)
					zVal[i] = 0;							//	none get treatment at high index
				else {
					int highTreat = (nValues / 4 - index) * 2;
					zVal[i] = i < highTreat ? 1 : 0;
				}
			}
		}
		int inverseIndex = nValues / 2 - index;
		for (int i=0 ; i<nValues/2 ; i++) {
			if (i % 2 == 0) {							//	even
				if (inverseIndex < nValues / 4)		//	none get treatment at low inverseIndex
					zVal[nValues - i - 1] = 1;
				else {
					int highTreat = (inverseIndex - nValues / 4) * 2;
					zVal[nValues - i - 1] = i < highTreat ? 0 : 1;
				}
			}
			else {												//	odd
				if (inverseIndex >= nValues / 4)
					zVal[nValues - i - 1] = 0;							//	all get treatment at high inverseIndex
				else {
					int highTreat = (nValues / 4 - inverseIndex) * 2;
					zVal[nValues - i - 1] = i < highTreat ? 1 : 0;
				}
			}
		}
		zVar.setValues(zVal);
					
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		if (lsWithCovar)
			ls.updateLSParams("y");
		else
			ls.updateLSParams("y", kBiasedConstraints);
		
		theView.setDataAndModel("y", kXKeys, "ls");
		theView.repaint();
	}
	
	private void showRandomSample() {
		RandomRectangular xGenerator = new RandomRectangular(nValues, xAxis.minOnAxis,
																																		xAxis.maxOnAxis);
		xGenerator.setNeatening(0.3);
		double x[] = xGenerator.generate();
		NumVariable xVar = (NumVariable)data.getVariable("x");
		xVar.setValues(x);
		
		int z[] = new int[x.length];
		for (int i=0 ; i<x.length/2 ; i++)
			z[i] = 1;
		
		CatVariable zVar = (CatVariable)data.getVariable("z");
		zVar.setValues(z);
		
		NumSampleVariable errorVar = (NumSampleVariable)data.getVariable("error");
		errorVar.generateNextSample();
		
		updateLS();
	}
	
	private void updateLS() {
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		if (lsWithCovar)
			ls.updateLSParams("y");
		else
			ls.updateLSParams("y", kBiasedConstraints);
	}
	
	private boolean localAction(Object target) {
		if (target == dataChoice) {
			int newChoice = dataChoice.getSelectedIndex();
			if (currentDataIndex != newChoice) {
				currentDataIndex = newChoice;
				if (newChoice == 0)
					theView.setDataAndModel(null, kXKeys, "model");
				else if (newChoice == 1) {
					NumVariable xVar = (NumVariable)data.getVariable("x");
					xVar.setValues(sortedX);
					
					MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
					model.setSD(newChoice == 1 ? 0.0 : errorSd);
					updateZValues();
				}
				else {
					showRandomSample();
					theView.setDataAndModel("y", kXKeys, "ls");
				}
				theView.repaint();
				
				controlPanelLayout.show(controlPanel, (newChoice == 0) ? "Model"
																					: (newChoice == 2) ? "Sample" : "Slider");
//				orthogonalSlider.setEnabled(newChoice != 0);
				if (useCovarCheck != null)
					useCovarCheck.setEnabled(newChoice != 0);
				
				extraVarCheck.setEnabled(newChoice != 0);
			}
			return true;
		}
		else if (target == useCovarCheck) {
			lsWithCovar = useCovarCheck.getState();
			if (dataChoice.getSelectedIndex() > 0) {
				updateLS();
						
//				theView.setDataAndModel("y", kXKeys, "ls");
				theView.repaint();
			}
			
			return true;
		}
		else if (target == orthogonalSlider) {
			if (dataChoice.getSelectedIndex() > 0)
				updateZValues();
			return true;
		}
		else if (target == sampleButton) {
			showRandomSample();
			theView.repaint();
			return true;
		}
		else if (target == extraVarCheck) {
			MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
			model.setSD(extraVarCheck.getState() ? errorSd : 0.0);
			updateLS();
			
			theView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}