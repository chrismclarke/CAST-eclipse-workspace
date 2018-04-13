package linModProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;

import linMod.*;


public class SlopeFitApplet extends SampleLSApplet {
	static final protected String SLOPE_AXIS_PARAM = "slopeAxis";
	static final protected String FITTED_DECIMALS_PARAM = "fittedDecimals";
	
//	static final private int kExtraSDDecimals = 3;
	
	static final private Color kPaleBlue = new Color(0x99CCFF);
	
	private RepeatingButton anotherDataSetButton;
	
	private int fittedDecimals;
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		fittedDecimals = Integer.parseInt(getParameter(FITTED_DECIMALS_PARAM));
		
		SlopeDistnVariable slopeDistn = new SlopeDistnVariable("slope", sourceData,
																						"x", "y", fittedDecimals);
		summaryData.addVariable("fittedSlope", slopeDistn);
		
		return summaryData;
	}
	
	private XPanel jitteredPanel(DataSet data, String paramKey, String theoryKey, String axisParam) {
		XPanel paramPanel = new XPanel();
		paramPanel.setLayout(new AxisLayout());
			
		HorizAxis horizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisParam);
		horizAxis.readNumLabels(labelInfo);
		NumVariable param = (NumVariable)data.getVariable(paramKey);
		horizAxis.setAxisName(param.name);
		paramPanel.add("Bottom", horizAxis);
		
		JitterPlusNormalView dataView = new JitterPlusNormalView(data, this, horizAxis, theoryKey, 0.0);
		dataView.lockBackground(Color.white);
		dataView.setActiveNumVariable(paramKey);
		dataView.setShowDensity(DataPlusDistnInterface.CONTIN_DISTN);
		dataView.setDensityColor(kPaleBlue);
		paramPanel.add("Center", dataView);
		
		return paramPanel;
	}
	
	protected DataView getSampleView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		SampleLineView theView = (SampleLineView)super.getSampleView(data, xAxis, yAxis);
		theView.setShowModel(false);
		return theView;
	}
	
	protected XPanel equationPanel(DataSet data) {		//		don't put equation under plots
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel bivarDisplayPanel(DataSet data, int viewType) {
		if (viewType == SAMPLE)
			return super.bivarDisplayPanel(data, viewType);
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(0, 30));
			
			thePanel.add("North", parameterPanel(data));
			
			thePanel.add("Center", jitteredPanel(data, "slope", "fittedSlope", SLOPE_AXIS_PARAM));
			return thePanel;
		}
	}
	
	private XPanel parameterPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(singleEquationPanel(data, LS_EQN));
		
		NumValue maxParam = new NumValue(slopeMax.toDouble(), fittedDecimals);
		thePanel.add(new SlopeDistnView(summaryData, this, "fittedSlope", maxParam, SlopeDistnView.SLOPE_MEAN));
		thePanel.add(new SlopeDistnView(summaryData, this, "fittedSlope", maxParam, SlopeDistnView.SLOPE_SD));
		return thePanel;
	}
	
	protected XPanel samplingControlPanel(DataSet summaryData, int topInset, int bottomInset) {
		XPanel thePanel = new InsetPanel(0, topInset, 0, bottomInset);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		anotherDataSetButton = new RepeatingButton("Another Data Set", this);
		thePanel.add(anotherDataSetButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == anotherDataSetButton) {
			SlopeDistnVariable slopeDistn = (SlopeDistnVariable)summaryData.getVariable("fittedSlope");
			slopeDistn.resetSource();
			summaryData.takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}