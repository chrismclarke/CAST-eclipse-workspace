package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import random.*;
import models.*;

import twoGroup.*;
import bivarCat.*;


public class PropnDiffApplet extends CoreDiffApplet {
	static final private String PROPN_DECIMALS_PARAM = "propnDecimals";
	static final private String COMMON_PROB_PARAM = "commonProb";
	
	static final private Color kDarkGreen = new Color(0x006600);
	
	private XCheckbox proportionCheck;
	private ParameterSlider sliders[];
	private ParameterSlider commonSlider;
	private ContinFitView theView;
	
	public void setupApplet() {
		super.setupApplet();
		
		setShowTheory(true);
	}
	
	protected CoreModelDataSet readData() {
		return new ContinTableDataSet(this);
	}
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		CatVariable xVar = (CatVariable)sourceData.getVariable("x");
		TwoGroupPropnSummary diff = new TwoGroupPropnSummary(xVar.getLabel(1).toString()
									+ " " + translate("minus") + " " + xVar.getLabel(0).toString(),
									sourceData.getSummaryDecimals(), TwoGroupPropnSummary.DIFFERENCE);
		summaryData.addVariable("diff", diff);
		
		TwoGroupPropnSummary group2 = new TwoGroupPropnSummary("Group 2 propn",
									sourceData.getSummaryDecimals(), TwoGroupPropnSummary.PROPN2);
		summaryData.addVariable("group2", group2);
		
		TwoGroupPropnSummary group1 = new TwoGroupPropnSummary("Group 1 propn",
									sourceData.getSummaryDecimals(), TwoGroupPropnSummary.PROPN1);
		summaryData.addVariable("group1", group1);
		
		summaryData.takeSample();
		
		NormalDistnVariable diffTheory = new NormalDistnVariable("diffTheory");
		diffTheory.setDecimals(sourceData.getSummaryDecimals());
		summaryData.addVariable("diffTheory", diffTheory);
		
		NormalDistnVariable group2Theory = new NormalDistnVariable("group2Theory");
		group2Theory.setDecimals(sourceData.getSummaryDecimals());
		summaryData.addVariable("group2Theory", group2Theory);
		
		NormalDistnVariable group1Theory = new NormalDistnVariable("group1Theory");
		group1Theory.setDecimals(sourceData.getSummaryDecimals());
		summaryData.addVariable("group1Theory", group1Theory);
		
		setTheoryParams(sourceData, summaryData);
		
//		summaryData.clearData();
//		CatSampleVariable y = (CatSampleVariable)data.getVariable("y");
//		y.clearData();
		
		return summaryData;
	}
	
	protected void setTheoryParams(CoreModelDataSet data, SummaryDataSet summaryData) {
		NormalDistnVariable diffTheory = (NormalDistnVariable)summaryData.getVariable("diffTheory");
		NormalDistnVariable group2Theory = (NormalDistnVariable)summaryData.getVariable("group2Theory");
		NormalDistnVariable group1Theory = (NormalDistnVariable)summaryData.getVariable("group1Theory");
		
		double[] probs = getZeroProbs(data);
		double p1 = probs[0];
		double p2 = probs[1];
		
		CatSampleVariable y = (CatSampleVariable)data.getVariable("y");
		RandomProductMulti generator = (RandomProductMulti)y.getGenerator();
		int[] sampleSizes = generator.getSampleSizes();
		int n1 = sampleSizes[0];
		int n2 = sampleSizes[1];
		
		diffTheory.setMean(p2 - p1);
		diffTheory.setSD(Math.sqrt(p1 * (1.0 - p1) / n1 + p2 * (1.0 - p2) / n2));
		
		group1Theory.setMean(p1);
		group1Theory.setSD(Math.sqrt(p1 * (1.0 - p1) / n1));
		
		group2Theory.setMean(p2);
		group2Theory.setSD(Math.sqrt(p2 * (1.0 - p2) / n2));
	}
	
	protected XPanel displayPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		thePanel.add("North", sliderPanel(data));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.66667, 10, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
			mainPanel.add(ProportionLayout.TOP, super.displayPanel(data, summaryData));
			mainPanel.add(ProportionLayout.BOTTOM, differencePlotPanel(summaryData,
																						"diff", "diffTheory"));
		thePanel.add("Center", mainPanel);
		return thePanel;
	}
	
	private double[] getZeroProbs(CoreModelDataSet data) {
		CatSampleVariable y = (CatSampleVariable)data.getVariable("y");
		int ny = y.noOfCategories();
		RandomProductMulti generator = (RandomProductMulti)y.getGenerator();
		
		double[] probs = generator.getProbs();
		int nx = probs.length / ny;
		double[] zeroProbs = new double[nx];
		for (int i=0 ; i<nx ; i++)
			zeroProbs[i] = probs[ny * i];
		return zeroProbs;
	}
	
	protected double getLeftProportion() {
		return 0.5;
	}
	
	protected XPanel dataPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			int proportionDecimals = Integer.parseInt(getParameter(PROPN_DECIMALS_PARAM));
			theView = new ContinFitView(data, this, "x", "y", "model", proportionDecimals, data.getSummaryDecimals(),
																															groupMainColor);
//			theView.setDisplayType(TwoWayView.XMAIN, TwoWayView.PROPN_IN_X, false);
		
		thePanel.add(theView);
		
			XPanel propnPanel = new XPanel();
			propnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				proportionCheck = new XCheckbox(translate("Proportions"), this);
//				proportionCheck.setState(true);
			propnPanel.add(proportionCheck);
		
		thePanel.add(propnPanel);
		
		return thePanel;
	}
	
	private XPanel sliderPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		
		CatVariable y = (CatVariable)data.getVariable("y");
		String successString = "P(" + y.getLabel(0).toString() + ")";
		double[] probs = getZeroProbs(data);
		
		String commonProbString = getParameter(COMMON_PROB_PARAM);
		if (commonProbString != null && commonProbString.equals("true")) {
			thePanel.setLayout(new ProportionLayout(0.25, 0));
			thePanel.add(ProportionLayout.LEFT, new XPanel());
			
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new ProportionLayout(0.667, 0));
				rightPanel.add(ProportionLayout.RIGHT, new XPanel());
				
					commonSlider = new ParameterSlider(new NumValue(0.0, 2), new NumValue(1.0, 2),
							new NumValue(probs[0], 2), successString, this);		//	assumes initial p(success) are all same
				rightPanel.add(ProportionLayout.LEFT, commonSlider);
			
			thePanel.add(ProportionLayout.RIGHT, rightPanel);
		}
		else {
			thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
			CatVariable x = (CatVariable)data.getVariable("x");
			
			int nx = x.noOfCategories();
			sliders = new ParameterSlider[nx];
			
			for (int i=0 ; i<nx ; i++) {
				sliders[i] = new ParameterSlider(new NumValue(0.0, 2), new NumValue(1.0, 2),
							new NumValue(probs[i], 2), x.getLabel(i).toString() + ": " + successString, this);
				sliders[i].setFont(getStandardFont());
				if (i == 0) {
					sliders[i].setSliderColor(XSlider.GREEN);
					sliders[i].setForeground(kDarkGreen);
				}
				else
					sliders[i].setForeground(Color.blue);
				thePanel.add((i==0) ? ProportionLayout.LEFT : ProportionLayout.RIGHT, sliders[i]);
			}
		}
		
		return thePanel;
	}
	
	protected String getSummaryYAxisInfo(CoreModelDataSet data) {
		return "-0.05 1.05 0.0 0.2";
	}
	
	protected XPanel summaryPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.TOP, groupPlotPanel(data, summaryData, "group1",
																									"group1Theory", 0));
		
		thePanel.add(ProportionLayout.BOTTOM, groupPlotPanel(data, summaryData, "group2",
																									"group2Theory", 1));
		
		return thePanel;
	}
	
	private void updateZeroProb(double[] p, int group, double p0, CatSampleVariable y) {
		int ny = y.noOfCategories();
		double oldProbLeft = 1.0 - p[group * ny];
		p[group * ny] = p0;
		
		double newProbLeft = 1.0 - p0;
		double scaling = newProbLeft / oldProbLeft;
		
		if (oldProbLeft <= 0.0) {
			double py = newProbLeft / (ny - 1);
			for (int j=1 ; j<ny ; j++)
				p[group * ny + j] = py;
		}
		else
			for (int j=1 ; j<ny ; j++)
				p[group * ny + j] = p[group * ny + j] * scaling;
	}
	
	private void resetSummaries(double[] p, CatSampleVariable y) {
		summaryData.clearData();
		y.clearData();
		
		setTheoryParams(data, summaryData);
		summaryData.variableChanged("group1");
		summaryData.variableChanged("group2");
		summaryData.variableChanged("diff");
		CatDistnVariable model = (CatDistnVariable)data.getVariable("model");
		model.setProbs(p);
		data.variableChanged("y");
	}

	
	private boolean localAction(Object target) {
		if (target == proportionCheck) {
			theView.setDisplayType(TwoWayView.XMAIN,
					proportionCheck.getState() ? TwoWayView.PROPN_IN_X : TwoWayView.COUNT, false);
			return true;
		}
		else if (target == commonSlider) {
			double newProb = commonSlider.getParameter().toDouble();
			CatVariable x = (CatVariable)data.getVariable("x");
			int nx = x.noOfCategories();
			CatSampleVariable y = (CatSampleVariable)data.getVariable("y");
			RandomProductMulti generator = (RandomProductMulti)y.getGenerator();
			double[] p = generator.getProbs();

			for (int i=0 ; i<nx ; i++)
				updateZeroProb(p, i, newProb, y);

			resetSummaries(p, y);

			return true;
		}
		else
			for (int i=0 ; i<sliders.length ; i++)
				if (target == sliders[i]) {
					double newProb = sliders[i].getParameter().toDouble();
					CatSampleVariable y = (CatSampleVariable)data.getVariable("y");
					RandomProductMulti generator = (RandomProductMulti)y.getGenerator();
					double[] p = generator.getProbs();

					updateZeroProb(p, i, newProb, y);
					resetSummaries(p, y);

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
	
/*
	private double oneMinus(double x) {
		double result = 1.0 - x;
		if (Double.isInfinite(result))
			result = 0.0;
//		System.out.println("x = " + x + ", result = " + result);
		return result;
	}
*/
}