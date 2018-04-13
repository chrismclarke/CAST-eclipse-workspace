package linModProg;

import java.awt.*;
import java.util.*;

import axis.*;
import utils.*;
import dataView.*;
import random.*;
import models.*;
import coreGraphics.*;
import regn.*;
import linMod.*;


public class DragRegnParamApplet extends ScatterApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String RANDOM_X_PARAM = "randomX";
	static final protected String INTERCEPT_PARAM = "interceptLimits";
	static final protected String SLOPE_PARAM = "slopeLimits";
	static final protected String SD_PARAM = "sdLimits";
	static final protected String DATA_NAMES_PARAM = "dataNames";
	
	@SuppressWarnings("unused")
	private NumValue intMin, intMax, intStart, slopeMin, slopeMax, slopeStart,
																						sdMin, sdMax, sdStart;
	
	private DataSet data;
	
	private XButton sampleButton, bestButton;
	private RandomNormal generator, xGenerator;
	
	private XChoice dataChoice;
	private int currentDataIndex = 0;
	
	private DragRegnParamView theView;
	
	public void setupApplet() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(INTERCEPT_PARAM));
		intMin = new NumValue(paramLimits.nextToken());
		intMax = new NumValue(paramLimits.nextToken());
		intStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(getParameter(SLOPE_PARAM));
		slopeMin = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		slopeStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(getParameter(SD_PARAM));
		sdMin = new NumValue(paramLimits.nextToken());
		sdMax = new NumValue(paramLimits.nextToken());
		sdStart = new NumValue(paramLimits.nextToken());
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x", intStart,
																								slopeStart, sdStart);
		yDistn.setSDDrag(true);
		data.addVariable("model", yDistn);
		
		String xValueString = getParameter(X_VALUES_PARAM);
		NumVariable xVar = new NumVariable(X_VAR_NAME_PARAM);
		if (xValueString == null)
			xGenerator = new RandomNormal(getParameter(RANDOM_X_PARAM));
		else
			xVar.readValues(xValueString);
		data.addVariable("x", xVar);
		
		String yValues = getParameter(Y_VALUES_PARAM);
		if (yValues == null) {
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			generator = new RandomNormal(randomInfo);
			
			NumVariable error = new NumVariable(translate("Error"));
			data.addVariable("error", error);
			
			ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																											data, "x", "error", "model", 10);
			data.addVariable("y", yData);
		}
		else
			data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), yValues);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		String dataNames = getParameter(DATA_NAMES_PARAM);
		if (dataNames != null) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(0, 0));
			
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
					dataChoice = new XChoice(translate("Data set") + ":", XChoice.HORIZONTAL, this);
					StringTokenizer st = new StringTokenizer(dataNames, "#");
					dataChoice.addItem(st.nextToken());
					dataChoice.addItem(st.nextToken());
				choicePanel.add(dataChoice);
				
			thePanel.add("North", choicePanel);
			thePanel.add("Center", super.topPanel(data));
			
			return thePanel;
		}
		else
			return super.topPanel(data);
	}
	
	private XPanel parameterPanel(DataSet data) {
		XPanel equationPanel = new XPanel();
		equationPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
		
			LinearEquationView eqnView = new LinearEquationView(data, this, "model", null, null,
																									intMin, intMax, slopeMin, slopeMax);
			eqnView.setFont(getBigBoldFont());
		equationPanel.add(eqnView);
		
			YSDView sdView = new YSDView(data, this, "model", sdMax);
			sdView.setFont(getBigBoldFont());
		equationPanel.add(sdView);
		return equationPanel;
	}
	
	private XPanel buttonPanel(DataSet data) {
		XPanel samplePanel = new XPanel();
		samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		NumVariable errorVar = (NumVariable)data.getVariable("error");
		if (errorVar == null) {
			bestButton = new XButton(translate("Best values"), this);
			samplePanel.add(bestButton);
		}
		else {
			sampleButton = new XButton(translate("Take sample"), this);
			samplePanel.add(sampleButton);
		}
		
		return samplePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, buttonPanel(data));
		
		thePanel.add(ProportionLayout.RIGHT, parameterPanel(data));
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new DragRegnParamView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		
		NumVariable errorVar = (NumVariable)data.getVariable("error");
		if (errorVar == null)
			theView.setGotSample(true);
		
		return theView;
	}
	
	private void takeSample() {
		double vals[] = generator.generate();
		((NumVariable)data.getVariable("error")).setValues(vals);
		if (xGenerator != null) {
			vals = xGenerator.generate();
			((NumVariable)data.getVariable("x")).setValues(vals);
		}
		theView.setGotSample(true);
		data.variableChanged("error");
	}
	
	private void setBestValues() {
		LinearModel model = (LinearModel)data.getVariable("model");
		model.updateLSParams("y");
		data.variableChanged("model");
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			takeSample();
			return true;
		}
		else if (target == bestButton) {
			setBestValues();
			return true;
		}
		else if (target == dataChoice) {
			int newChoice = dataChoice.getSelectedIndex();
			if (newChoice != currentDataIndex) {
				currentDataIndex = newChoice;
				String suffix = (newChoice == 0) ? "" : "2";
				
				theHorizAxis.setAxisName(getParameter(X_VAR_NAME_PARAM + suffix));
				theHorizAxis.repaint();
				
				yVariateName.setText(getParameter(Y_VAR_NAME_PARAM + suffix));
				yVariateName.invalidate();
				validate();
				
				NumVariable xVar = (NumVariable)data.getVariable("x");
				xVar.readValues(getParameter(X_VALUES_PARAM + suffix));
				NumVariable yVar = (NumVariable)data.getVariable("y");
				yVar.readValues(getParameter(Y_VALUES_PARAM + suffix));
				
				data.variableChanged("y");
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}