package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import formula.*;


import percentile.*;


public class MultiDataPercentApplet extends XApplet {
	static final protected String MAX_VALUE_PARAM = "maxValue";
	
	static final private String kZeroOneAxis = "0 1 0.0 0.2";
	static final private int kAppletWidth = 444;
	
	static final private int kMaxYears = 100;
	static final private int kPredictDecimals = 1;
	
	static final private Color kPredictBackground = new Color(0xDDDDFF);
	
	protected MultiDataSet data;
	protected DataSet refData;
	
	private YearsSlider yearsSlider;
	private XChoice dataSetChoice;
	
	private PercentFormulaPanel formula;
	private PropnPredictFormulaPanel yearsFormula;
	private PropnPredict2FormulaPanel yearsFormula2;
	
	public void setupApplet() {
		data = getData();
		refData = getReferenceData(data);
		
		setLayout(new BorderLayout(0, 0));
		add("North", topPanel(data));
		add("Center", dataDisplayPanel(data, refData));
		add("South", propnCalcPanel(data, refData));
	}
	
	protected MultiDataSet getData() {
		return new MultiDataSet(this);
	}
	
	protected DataSet getReferenceData(MultiDataSet data) {
		DataSet refData = data.getReferenceData();
		
		NumVariable yVar = data.getNumVariable();
		int startYears = yVar.noOfValues();
		if (startYears > 100)
			startYears = 50;
		refData.addNumVariable("years", "Years", String.valueOf(startYears));
		
		return refData;
	}
	
	protected XPanel topPanel(MultiDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
				XLabel dataNameLabel = new XLabel(translate("Data set"), XLabel.LEFT, this);
				dataNameLabel.setFont(getStandardBoldFont());
			topPanel.add(dataNameLabel);
			
				dataSetChoice = data.getDataSetChoice(this);
			topPanel.add(dataSetChoice);
			
		thePanel.add("North", topPanel);
		
		thePanel.add("Center", data.getDescription(kAppletWidth, this));
		
			XLabel cumPropnLabel = new XLabel(translate("Cumulative proportion"), XLabel.LEFT, this);
			cumPropnLabel.setFont(getStandardFont());
			
		thePanel.add("South", cumPropnLabel);
		return thePanel;
	}
	
	protected XPanel dataDisplayPanel(MultiDataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			MultiHorizAxis horizAxis = data.getHorizYAxis(this);
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(kZeroOneAxis);
		thePanel.add("Left", vertAxis);
		
			CumFunctDotPlotView cumView = new CumFunctDotPlotView(data, this,
										horizAxis, referenceData, "ref", PropnRangeView.LESS_EQUAL, vertAxis);
		thePanel.add("Center", cumView);
		return thePanel;
	}
	
	protected XPanel propnCalcPanel(MultiDataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
				FormulaContext boldContext = new FormulaContext(Color.black, getStandardBoldFont(), this);
				formula = new PercentFormulaPanel(data, "y", "ref",
												referenceData, maxY, PropnRangeView.LESS_EQUAL, boldContext);
			topPanel.add(formula);
		
		thePanel.add("North", topPanel);
		
			XPanel centerPanel = new InsetPanel(6, 2, 6, 6);
			centerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																												VerticalLayout.VERT_CENTER, 10));
			
				XPanel sliderFormula1Panel = new XPanel();
				sliderFormula1Panel.setLayout(new BorderLayout(40, 0));
				
					NumVariable yVar = (NumVariable)data.getVariable("y");
					int startYears = yVar.noOfValues();
					if (startYears > 100)
						startYears = 50;
					yearsSlider = new YearsSlider(this, startYears, kMaxYears, data.getAllUnitsStrings());
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
										data.getAllUnitsStrings(), boldContext);
					innerBottomPanel.add(yearsFormula2);
					
					innerBottomPanel.lockBackground(Color.white);
				bottomPanel.add(innerBottomPanel);
				
			centerPanel.add(bottomPanel);
			
			centerPanel.lockBackground(kPredictBackground);
		thePanel.add("Center", centerPanel);
		return thePanel;
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
		else if (target == dataSetChoice) {
			int newDataIndex = dataSetChoice.getSelectedIndex();
			if (data.changeDataSet(newDataIndex)) {
				yearsSlider.setVersionIndex(newDataIndex);
				yearsFormula.reinitialise();
				yearsFormula2.setVersionIndex(newDataIndex);
				formula.reinitialise();
				validate();
				repaint();
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