package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;

import twoGroup.*;
import bivarCat.*;


public class PDiffSampleApplet extends XApplet {
	static final private String PROPN_DECIMALS_PARAM = "propnDecimals";
	
	static final private Color groupMainColor[] = {new Color(0x006600), Color.blue};
	
	private ContinTableDataSet data;
	
	private XCheckbox proportionCheck;
	private RepeatingButton takeSampleButton;
	
	private ParameterSlider sliders[];
	private ContinFitView theView;
	
	private NumValue maxDiff;
	
	public void setupApplet() {
		data = readData();
		maxDiff = new NumValue(-1.0, Integer.parseInt(getParameter(PROPN_DECIMALS_PARAM)));
		
		setLayout(new BorderLayout(30, 4));
		
		add("Center", leftPanel(data));
		
		add("East", rightPanel(data));
	}
	
	private ContinTableDataSet readData() {
		ContinTableDataSet theData = new ContinTableDataSet(this);
		CatSampleVariable y = (CatSampleVariable)theData.getVariable("y");
		y.generateNextSample();
		return theData;
	}
	
	private XPanel leftPanel(ContinTableDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.8, 5, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
			
				theView = new ContinFitView(data, this, "x", "y", "model", maxDiff.decimals,
																						data.getSummaryDecimals(), groupMainColor);
			
			mainPanel.add(ProportionLayout.TOP, theView);
			
				XPanel propnPanel = new XPanel();
				propnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					proportionCheck = new XCheckbox(translate("Proportions"), this);
				propnPanel.add(proportionCheck);
			
			mainPanel.add(ProportionLayout.BOTTOM, propnPanel);
		thePanel.add("Center", mainPanel);
		
		thePanel.add("South", sliderPanel(data));
		
		return thePanel;
	}
	
	private double[] getZeroProbs(ContinTableDataSet data) {
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
	
	private XPanel sliderPanel(ContinTableDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
		
		CatVariable y = (CatVariable)data.getVariable("y");
		String successString = ": P(" + y.getLabel(0).toString() + ")";
		CatVariable x = (CatVariable)data.getVariable("x");
		
		int nx = x.noOfCategories();
		sliders = new ParameterSlider[nx];
		
		double[] probs = getZeroProbs(data);
		
		for (int i=0 ; i<nx ; i++) {
			sliders[i] = new ParameterSlider(new NumValue(0.0, 2), new NumValue(1.0, 2),
						new NumValue(probs[i], 2), x.getLabel(i).toString() + successString, this);
			sliders[i].setFont(getStandardFont());
			if (i == 0)
				sliders[i].setSliderColor(XSlider.GREEN);
			sliders[i].setForeground(groupMainColor[i]);
			thePanel.add(sliders[i]);
		}
		
		return thePanel;
	}
	
	protected XPanel rightPanel(ContinTableDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 0, ProportionLayout.VERTICAL,
																						ProportionLayout.TOTAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 5));
			topPanel.add(new GroupSummaryView(data, this, GroupSummaryView.P, 0, maxDiff.toString(), maxDiff.decimals));
			
			topPanel.add(new GroupSummaryView(data, this, GroupSummaryView.P, 1, maxDiff.toString(), maxDiff.decimals));
			
			GroupSummary2View diff = new GroupSummary2View(data, this, GroupSummary2View.P_DIFF, maxDiff.toString(), maxDiff.decimals);
			diff.setForeground(Color.red);
			topPanel.add(diff);
			
		thePanel.add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 5));
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			bottomPanel.add(takeSampleButton);
		
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			CatSampleVariable y = (CatSampleVariable)data.getVariable("y");
			y.generateNextSample();
			data.variableChanged("y");
			return true;
		}
		else if (target == proportionCheck) {
			theView.setDisplayType(TwoWayView.XMAIN,
						proportionCheck.getState() ? TwoWayView.PROPN_IN_X : TwoWayView.COUNT, false);
			return true;
		}
		else
			for (int i=0 ; i<sliders.length ; i++)
				if (target == sliders[i]) {
					double newProb = sliders[i].getParameter().toDouble();
					CatDistnVariable model = (CatDistnVariable)data.getVariable("model");
					CatSampleVariable y = (CatSampleVariable)data.getVariable("y");
					int ny = y.noOfCategories();
					RandomProductMulti generator = (RandomProductMulti)y.getGenerator();
					double[] p = generator.getProbs();
					double oldProbLeft = 1.0 - p[i * ny];
					p[i * ny] = newProb;
					
					double newProbLeft = 1.0 - newProb;
					double scaling = newProbLeft / oldProbLeft;
//					System.out.println("newProb = " + newProb + ", newProbLeft = " + newProbLeft + ", oldProbLeft = " + oldProbLeft + ", scaling = " + scaling);
					if (oldProbLeft <= 0.0) {
						double py = newProbLeft / (ny - 1);
						for (int j=1 ; j<ny ; j++)
							p[i * ny + j] = py;
					}
					else
						for (int j=1 ; j<ny ; j++)
							p[i * ny + j] = p[i * ny + j] * scaling;
					y.clearData();
					model.setProbs(p);
					data.variableChanged("y");
					
					break;
				}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}