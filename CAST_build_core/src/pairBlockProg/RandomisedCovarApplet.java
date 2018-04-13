package pairBlockProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import random.*;
import coreGraphics.*;
import coreVariables.*;
import coreSummaries.*;
import models.*;

import pairBlock.*;


public class RandomisedCovarApplet extends XApplet {
	static final private String RESPONSE_NAME_PARAM = "responseName";
	static final private String N_VALUES_PARAM = "nValues";
	static final private String COVAR_NAME_PARAM = "covarName";
	static final private String COVAR_MIN_MAX_PARAM = "covarMinMax";
	static final private String COVAR_REGN_PARAM = "covarRegnParams";
	static final private String FACTOR_NAME_PARAM = "factorName";
	static final private String FACTOR_LABELS_PARAM = "factorLabels";
	static final private String ERROR_SD_PARAM = "errorSd";
	static final private String EFFECT_NAME_PARAM = "effectName";
	
	static final private String FACTOR_EFFECT_PARAM = "factorEffect";
	
	static final private String RESPONSE_AXIS_PARAM = "responseAxis";
	static final private String COVAR_AXIS_PARAM = "covarAxis";
	static final private String EFFECT_AXIS_PARAM = "effectAxis";
	
	static final private Color kCovarBackground = new Color(0xF5F5F5);
	static final private Color kMiddleDiffBackground = new Color(0xEEEEFF);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	private double covarMid, covarHalfRange;
	private int nLevels;
	
	protected PairedCovarView theView;
	protected PairingDotView covarView;
	
	private XNoValueSlider covarSpreadSlider;
	private XButton takeSampleButton;
	protected XCheckbox accumulateCheck;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(16, 4));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout((nLevels == 2) ? 0.73 : 0.6, 20));
			
			mainPanel.add(ProportionLayout.LEFT, displayPanel(data));
			mainPanel.add(ProportionLayout.RIGHT, summaryPanel(data, summaryData));
		
		add("Center", mainPanel);
		add("South", controlPanel());
		
		summaryData.takeSample();
		adjustCovarSpread();
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
			
			int n = Integer.parseInt(getParameter(N_VALUES_PARAM));
			RandomRectangular baseGenerator = new RandomRectangular(n, 0, 1);
			baseGenerator.setNeatening(0.4);
			NumVariable baseCovar = new NumVariable("BaseCovar");
			baseCovar.setValues(baseGenerator.generate());
		
		data.addVariable("baseCovar", baseCovar);
		
			ScaledVariable covar = new ScaledVariable(getParameter(COVAR_NAME_PARAM), baseCovar, "baseCovar", 0.0, 1.0, 9);
		data.addVariable("covar", covar);
			
			FactorAllocationVariable factor = new FactorAllocationVariable(getParameter(FACTOR_NAME_PARAM),
																													baseCovar, getParameter(FACTOR_LABELS_PARAM));
			nLevels = factor.noOfCategories();
		data.addVariable("factor", factor);
			
			double errorSd = Double.parseDouble(getParameter(ERROR_SD_PARAM));
			RandomNormal errorGenerator = new RandomNormal(n, 0.0, errorSd, 3.0);
			NumSampleVariable error = new NumSampleVariable("error", errorGenerator, 9);
		
		data.addVariable("error", error);
		
		data.addVariable("random", new BiSampleVariable(data, "error", "factor"));
		
		if (nLevels == 2) {
			data.addVariable("model", new LinearModel("Model", data, "covar", getParameter(COVAR_REGN_PARAM)));
			
				double factorEffect = Double.parseDouble(getParameter(FACTOR_EFFECT_PARAM));
			data.addVariable("y", new ResponseVariable(getParameter(RESPONSE_NAME_PARAM), data,
															"covar", "error", "model", "factor", factorEffect, 9));
		}
		else {
			String xKeys[] = {"covar", "factor"};
			String params = getParameter(COVAR_REGN_PARAM) + " " + getParameter(FACTOR_EFFECT_PARAM) + " 1";
			data.addVariable("model", new MultipleRegnModel("Model", data, xKeys, params));
			
			data.addVariable("y", new ResponseVariable(getParameter(RESPONSE_NAME_PARAM), data,
																												xKeys, "error", "model", 9));
		}
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "random");
		CatVariable factor = (CatVariable)sourceData.getVariable("factor");
		
		if (nLevels == 2)
			summaryData.addVariable("diffMeans", new DiffSummaryVariable(getParameter(EFFECT_NAME_PARAM), "y", "factor", 9));
		else
			for (int i=0 ; i<nLevels-1 ; i++)
				for (int j=i+1 ; j<nLevels ; j++) {
					String diffName = factor.getLabel(j) + " - " + factor.getLabel(i);
					DiffSummaryVariable dv = new DiffSummaryVariable(diffName, "y", "factor", 9);
					dv.setDiffIndices(j, i);
					String diffKey = ("diff" + i) + j;
					summaryData.addVariable(diffKey, dv);
				}
		
		return summaryData;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(COVAR_MIN_MAX_PARAM));
			double minCovar = Double.parseDouble(st.nextToken());
			double maxCovar = Double.parseDouble(st.nextToken());
			covarMid = (minCovar + maxCovar) / 2;
			covarHalfRange = (maxCovar - minCovar) / 2;
		
		thePanel.add("Center", leftControlPanel());
													
		thePanel.add("East", samplingPanel());
		
		return thePanel;
	}
	
	protected XPanel leftControlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
			covarSpreadSlider = new XNoValueSlider(translate("High"), translate("Low"), translate("Spread of") + " " + getParameter(COVAR_NAME_PARAM),
																																														0, 100, 0, this);
		thePanel.add(covarSpreadSlider);
		return thePanel;
	}
	
	private XPanel samplingPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
	
			takeSampleButton = new RepeatingButton(translate("Conduct experiment"), this);
		thePanel.add(takeSampleButton);
	
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
				
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(RESPONSE_AXIS_PARAM));
			mainPanel.add("Left", yAxis);
			
				HorizAxis covarAxis = new HorizAxis(this);
				covarAxis.readNumLabels(getParameter(COVAR_AXIS_PARAM));
				covarAxis.setAxisName(getParameter(COVAR_NAME_PARAM));
			mainPanel.add("Bottom", covarAxis);
			
				theView = new PairedCovarView(data, this, "y", "covar", "factor", yAxis, covarAxis);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
				
			mainPanel.add("Center", theView);
				
				covarView = new PairingDotView(data, this, covarAxis, nLevels);
				covarView.setActiveNumVariable("covar");
				covarView.setCrossSize(DataView.LARGE_CROSS);
				covarView.lockBackground(kCovarBackground);
			mainPanel.add("BottomMargin", covarView);
			
				FactorMeansView meansView = new FactorMeansView(data, this, yAxis, "y", "factor");
				meansView.lockBackground(kCovarBackground);
			mainPanel.add("RightMargin", meansView);
		
		thePanel.add("Center", mainPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
			
			XLabel yVariateName = new XLabel(getParameter(RESPONSE_NAME_PARAM), XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			topPanel.add("Center", yVariateName);
			
			XLabel meansName = new XLabel(translate("Means"), XLabel.RIGHT, this);
			meansName.setFont(yAxis.getFont());
			topPanel.add("East", meansName);

		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData) {
		if (nLevels == 2)
			return summary1Panel(summaryData);
		else
			return summary3Panel(summaryData);
	}
	
	private XPanel summary1Panel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
				
				VertAxis effectAxis = new VertAxis(this);
				effectAxis.readNumLabels(getParameter(EFFECT_AXIS_PARAM));
			mainPanel.add("Left", effectAxis);
			
				DotPlotView theView = new DotPlotView(summaryData, this, effectAxis, 1.0);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
				
			mainPanel.add("Center", theView);
		
		thePanel.add("Center", mainPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			XLabel effectName = new XLabel(getParameter(EFFECT_NAME_PARAM), XLabel.LEFT, this);
			effectName.setFont(effectAxis.getFont());
			topPanel.add(effectName);

		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel summary3Panel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
				
				VertAxis effectAxis = new VertAxis(this);
				effectAxis.readNumLabels(getParameter(EFFECT_AXIS_PARAM));
			mainPanel.add("Left", effectAxis);
			
				DotPlotView view10 = new DotPlotView(summaryData, this, effectAxis, 1.0);
				view10.setActiveNumVariable("diff01");
				view10.setCrossSize(DataView.LARGE_CROSS);
				view10.lockBackground(Color.white);
			
				DotPlotView view20 = new DotPlotView(summaryData, this, effectAxis, 1.0);
				view20.setActiveNumVariable("diff02");
				view20.setCrossSize(DataView.LARGE_CROSS);
				view20.lockBackground(kMiddleDiffBackground);
			
				DotPlotView view21 = new DotPlotView(summaryData, this, effectAxis, 1.0);
				view21.setActiveNumVariable("diff12");
				view21.setCrossSize(DataView.LARGE_CROSS);
				view21.lockBackground(Color.white);
				
				MultipleDataView theView = new MultipleDataView(summaryData, this, view10,
																					view20, view21, MultipleDataView.HORIZONTAL);
				
			mainPanel.add("Center", theView);
				
				HorizAxis diffAxis = new HorizAxis(this);
				String diffNames = "#" + summaryData.getVariable("diff01").name + "# #"
																			 + summaryData.getVariable("diff02").name + "# #"
																			 + summaryData.getVariable("diff12").name + "#";
				CatVariable tempVar = new CatVariable(null);
				tempVar.readLabels(diffNames);
				diffAxis.setCatLabels(tempVar);
				
			mainPanel.add("Bottom", diffAxis);
		
		thePanel.add("Center", mainPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			XLabel effectName = new XLabel(getParameter(EFFECT_NAME_PARAM), XLabel.LEFT, this);
			effectName.setFont(effectAxis.getFont());
			topPanel.add(effectName);

		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private void adjustCovarSpread() {
		double p;
		if (covarSpreadSlider == null)
			p = 1.0;
		else
			p = 1 - 0.95 * covarSpreadSlider.getValue() / (double)covarSpreadSlider.getMaxValue();
		double min = covarMid - p * covarHalfRange;
		double range = 2 * p * covarHalfRange;
		
		ScaledVariable covar = (ScaledVariable)data.getVariable("covar");
		covar.setScale(min, range, 9);
		data.variableChanged("covar");
		
		summaryData.setSingleSummaryFromData();
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == covarSpreadSlider) {
			adjustCovarSpread();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}