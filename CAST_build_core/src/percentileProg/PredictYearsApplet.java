package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import formula.*;


import percentile.*;


public class PredictYearsApplet extends DragCumPercentApplet {
	static final private int kMaxYears = 100;
	static final private int kPredictDecimals = 1;
	
	static final private Color kPredictBackground = new Color(0xDDDDFF);
	
	private SimplePropnFormulaPanel yearsFormula;
	private PropnPredict2FormulaPanel yearsFormula2;
	private YearsSlider yearsSlider;
	
	protected DataSet getReferenceData(DataSet data) {
		DataSet refData = super.getReferenceData(data);
		
		NumVariable yVar = data.getNumVariable();
		int n = yVar.noOfValues();
		refData.addNumVariable("years", "Years", String.valueOf(n));
		return refData;
	}
	
	protected XPanel topPanel(DataSet data) {
		return propnSidePanel(data);
	}
	
	protected VertAxis getVertAxis() {
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(kZeroOneAxis);
		vertAxis.setForeground(Color.red);
		return vertAxis;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
				FormulaContext boldContext = new FormulaContext(Color.black, getStandardBoldFont(), this);
				formula = new PercentFormulaPanel(data, "y", "ref", referenceData, maxY,
														PropnRangeView.LESS_EQUAL, boldContext);
			topPanel.add(formula);
		
		thePanel.add("North", topPanel);
		
			XPanel centerPanel = new InsetPanel(6, 2, 6, 6);
			centerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																												VerticalLayout.VERT_CENTER, 10));
			
				XPanel sliderFormula1Panel = new XPanel();
				sliderFormula1Panel.setLayout(new BorderLayout(40, 0));
				
					NumVariable yVar = (NumVariable)data.getVariable("y");
					int startYears = yVar.noOfValues();
					yearsSlider = new YearsSlider(this, startYears, kMaxYears, translate("years"));
					yearsSlider.setFont(getStandardBoldFont());
				sliderFormula1Panel.add("Center", yearsSlider);
				
					yearsFormula = new PropnPredictFormulaPanel(data, "y", "ref", referenceData, maxY,
													PropnRangeView.LESS_EQUAL, "years", new NumValue(kMaxYears, kPredictDecimals),
													boldContext);
				sliderFormula1Panel.add("East", yearsFormula);
				
			centerPanel.add(sliderFormula1Panel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					XPanel innerBottomPanel = new InsetPanel(6, 2);
					innerBottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
						
						yearsFormula2 = new PropnPredict2FormulaPanel(data, "y", "ref", referenceData, maxY,
													PropnRangeView.LESS_EQUAL, "years", new NumValue(kMaxYears, kPredictDecimals),
													"years", boldContext);
					innerBottomPanel.add(yearsFormula2);
					
					innerBottomPanel.lockBackground(Color.white);
				bottomPanel.add(innerBottomPanel);
				
			centerPanel.add(bottomPanel);
			
			centerPanel.lockBackground(kPredictBackground);
		thePanel.add("Center", centerPanel);
		return thePanel;
	}
	
	protected void changeInequality(int inequality) {
		super.changeInequality(inequality);
		yearsFormula.setInequality(inequality);
		yearsFormula2.setInequality(inequality);
	}

	
	private boolean localAction(Object target) {
		if (target == yearsSlider) {
			int years = yearsSlider.getYears();
			NumVariable yearsVar = (NumVariable)refData.getVariable("years");
			NumValue year0 = (NumValue)yearsVar.valueAt(0);
			year0.setValue(years);
			refData.valueChanged(0);
			data.variableChanged("y");
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