package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import models.*;

import regn.*;
//import scatterProg.*;
import linMod.*;


public class SlopeErrorApplet extends ScatterApplet {
	static final protected String ERROR_AXIS_PARAM = "errorAxis";
	static final protected String MAX_PARAM = "maxParams";
	static final protected String FITTED_DECIMALS_PARAM = "fittedDecimals";
	static final protected String ERROR_NAME_PARAM = "errorName";
	
//	Only used to generate pictures to translate into GIFs
	
	static final private int kExtraSDDecimals = 3;
	
	static final private Color kPaleBlue = new Color(0x99CCFF);
	
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
		
		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																													ProportionLayout.TOTAL));
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout());
			leftPanel.add("Center", displayPanel(data));
			leftPanel.add("North", topPanel(data));
		
		add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 20));
			rightPanel.add("North", summaryPanel(data, summaryData));
			rightPanel.add("Center", errorDistnPanel(summaryData));
			
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
		
		SlopeDistnVariable errorDistn = new SlopeDistnVariable("slope", sourceData,
																						"x", "y", fittedDecimals, true);
		summaryData.addVariable("errorDistn", errorDistn);
		
		summaryData.addVariable("dummy", new NumVariable("Dummy"));		// for JitterPlusNormalView
		
		return summaryData;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		SampleLineView theView = new SampleLineView(data, this, theHorizAxis, theVertAxis, "x", "y", null);
		theView.setShowData(true);
		theView.setShowModel(false);
		return theView;
	}
	
	private XPanel errorDistnPanel(SummaryDataSet summaryData) {
		XPanel errorPanel = new XPanel();
		errorPanel.setLayout(new AxisLayout());
			
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(ERROR_AXIS_PARAM));
			horizAxis.setAxisName(getParameter(ERROR_NAME_PARAM));
		errorPanel.add("Bottom", horizAxis);
		
			JitterPlusNormalView dataView = new JitterPlusNormalView(summaryData, this, horizAxis, "errorDistn", 0.0);
			dataView.lockBackground(Color.white);
			dataView.setActiveNumVariable("dummy");
			dataView.setShowDensity(DataPlusDistnInterface.CONTIN_DISTN);
			dataView.setDensityColor(kPaleBlue);
		errorPanel.add("Center", dataView);
		
		return errorPanel;
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 20));
		
			LSEquationView lsLineEquation = new LSEquationView(data, this, "y", "x", intMax, slopeMax);
			lsLineEquation.setShowData(true);
			lsLineEquation.setForeground(Color.blue);
			
		thePanel.add(lsLineEquation);
		
		thePanel.add(new ErrorSdEstView(data, this, "resid", maxErrorSD));
		
			NumValue maxParam = new NumValue(slopeMax.toDouble(), fittedDecimals);
		thePanel.add(new SlopeDistnView(summaryData, this, "errorDistn", maxParam, SlopeDistnView.SLOPE_SD));
		
		return thePanel;
	}
}