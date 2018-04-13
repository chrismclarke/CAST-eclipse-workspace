package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import qnUtils.*;
import models.*;

import linMod.*;


public class SlopeTestApplet extends ScatterApplet {
	static final protected String ERROR_AXIS_PARAM = "errorAxis";
	static final protected String MAX_PARAM = "maxParams";
	static final protected String FITTED_DECIMALS_PARAM = "fittedDecimals";
	
//	Only used to generate pictures to translate into GIFs
	
	static final private int kExtraSDDecimals = 3;
	
//	static final private Color kPaleBlue = new Color(0x99CCFF);
	
	private int fittedDecimals;
	protected NumValue intMax, slopeMax, maxErrorSD;
	
	public void setupApplet() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(MAX_PARAM));
		intMax = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		maxErrorSD = new NumValue(paramLimits.nextToken());
		
		String labelAxesParam = getParameter(LABEL_AXES_PARAM);
		labelAxes = labelAxesParam != null && labelAxesParam.equals("true");
		
		DataSet data = readData();
		SummaryDataSet summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
		DataSet tData = tDistnData(data, summaryData);
		
		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																													ProportionLayout.TOTAL));
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout());
			leftPanel.add("Center", displayPanel(data));
			leftPanel.add("North", topPanel(data));
			leftPanel.add("South", lsLinePanel(data, summaryData));
		
		add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 60));
			
			rightPanel.add("North", summaryPanel(data, summaryData));
			rightPanel.add("Center", tCalcPanel(tData));
			rightPanel.add("South", pValuePanel(tData));
			
		add(ProportionLayout.RIGHT, rightPanel);
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
			LinearModel lsLine = new LinearModel("lsLine", data, "x");
			lsLine.setLSParams("y", intMax.decimals, slopeMax.decimals,
																													intMax.decimals + kExtraSDDecimals);
		data.addVariable("lsLine", lsLine);
			ResidValueVariable resid = new ResidValueVariable("Resid", data, "x", "y",
																												"lsLine", intMax.decimals);
		data.addVariable("resid", resid);
		
		TDistnVariable tDist = new TDistnVariable("T", resid.noOfValues() - 2);
		data.addVariable("tDistn", tDist);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		SlopeInterceptVariable slope = new SlopeInterceptVariable("slope", "x", "y",
															slopeMax.decimals, SlopeInterceptVariable.SLOPE);
		SlopeInterceptVariable intercept = new SlopeInterceptVariable("intercept", "x", "y",
															intMax.decimals, SlopeInterceptVariable.INTERCEPT);
		
		summaryData.addVariable("slope", slope);
		summaryData.addVariable("intercept", intercept);
		
		fittedDecimals = Integer.parseInt(getParameter(FITTED_DECIMALS_PARAM));
		
		SlopeDistnVariable slopeDistn = new SlopeDistnVariable("slope", sourceData,
																											"x", "y", fittedDecimals);
		summaryData.addVariable("slopeDistn", slopeDistn);
		
		return summaryData;
	}
	
	protected DataSet tDistnData(DataSet data, SummaryDataSet summaryData) {
		DataSet tData = new DataSet();
		
		int nValues = ((NumVariable)data.getVariable("y")).noOfValues();
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)summaryData.getVariable("slopeDistn");
		double tValue = slopeDistn.getMean().toDouble() / slopeDistn.getSD().toDouble();
		
		TDistnVariable tDist = new TDistnVariable("T", nValues - 2);
		tDist.setMinSelection(-Math.abs(tValue));
		tDist.setMaxSelection(Math.abs(tValue));
		
		tData.addVariable("tDistn", tDist);
		
		return tData;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		SampleLineView theView = new SampleLineView(data, this, theHorizAxis, theVertAxis, "x", "y", null);
		theView.setShowData(true);
		theView.setShowModel(false);
		return theView;
	}
	
	private XPanel lsLinePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			LSEquationView lsLineEquation = new LSEquationView(data, this, "y", "x", intMax, slopeMax);
			lsLineEquation.setShowData(true);
			lsLineEquation.setForeground(Color.blue);
			
		thePanel.add(lsLineEquation);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			NumValue maxParam = new NumValue(slopeMax.toDouble(), fittedDecimals);
		thePanel.add(new SlopeDistnView(summaryData, this, "slopeDistn", maxParam, SlopeDistnView.SLOPE_SD));
		
		return thePanel;
	}
	
	private XPanel tCalcPanel(DataSet tData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
//		theHorizAxis.lockBackground(bgColor);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theProbAxis = new VertAxis(this);
//		theProbAxis.lockBackground(bgColor);
		thePanel.add("Left", theProbAxis);
		theProbAxis.show(false);
		
		theHorizAxis.readNumLabels("-5 5 -4 2");
		theProbAxis.readNumLabels("0 0.5 7 0.1");
		
		DistnDensityView theView = new DistnDensityView(tData, this, theHorizAxis,
													theProbAxis, "tDistn", DistnDensityView.NO_SHOW_MEANSD, DistnDensityView.NO_DRAG);
		
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel pValuePanel(DataSet tData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		ZProbView pView = new ZProbView(tData, "tDistn", this, ZProbView.BETWEEN);
		thePanel.add(pView);
		return thePanel;
	}
}