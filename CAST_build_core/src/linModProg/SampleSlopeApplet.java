package linModProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import models.*;


public class SampleSlopeApplet extends SampleLSApplet {
	static final protected String SLOPE_AXIS_PARAM = "slopeAxis";
	static final protected String INTERCEPT_AXIS_PARAM = "interceptAxis";
	
	static final private int INTERCEPT = 0;
	static final private int SLOPE = 1;
	
	static final private int kExtraSDDecimals = 3;
	
	static final private Color kPaleBlue = new Color(0x99CCFF);
	
	private DataPlusDistnInterface slopeView, interceptView;
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		NormalDistnVariable slopeDistn = new NormalDistnVariable("slope distn");
		summaryData.addVariable("slopeDistn", slopeDistn);
		
		NormalDistnVariable interceptDistn = new NormalDistnVariable("intercept distn");
		summaryData.addVariable("interceptDistn", interceptDistn);
		
		setTheoryParams(sourceData, summaryData);
		
		return summaryData;
	}
	
	protected void setTheoryParams(DataSet sourceData, SummaryDataSet summaryData) {
		LinearModel theory = (LinearModel)sourceData.getVariable("model");
		NumValue modelSlope = theory.getSlope();
		NumValue modelIntercept = theory.getIntercept();
		double modelSD = theory.evaluateSD().toDouble();
		
		NumVariable xVar = (NumVariable)sourceData.getVariable("x");
		ValueEnumeration xe = xVar.values();
		double sxx = 0.0;
		double sx = 0.0;
		int n = 0;
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			sx += x;
			sxx += x * x;
			n++;
		}
		double xMean = sx / n;
		sxx -= sx * xMean;
		NumValue interceptSD = new NumValue(Math.sqrt(1.0 / n + xMean * xMean / sxx) * modelSD,
																	modelIntercept.decimals + kExtraSDDecimals);
		NumValue slopeSD = new NumValue(modelSD / Math.sqrt(sxx),
																	modelSlope.decimals + kExtraSDDecimals);
		
		NormalDistnVariable slopeDistn = (NormalDistnVariable)summaryData.getVariable("slopeDistn");
		slopeDistn.setParams(modelSlope.toString() + " " + slopeSD.toString());
		
		NormalDistnVariable interceptDistn = (NormalDistnVariable)summaryData.getVariable("interceptDistn");
		interceptDistn.setParams(modelIntercept.toString() + " "
																				+ interceptSD.toString());
	}
	
	protected void repaintSummaries() {
		((DataView)slopeView).repaint();
		((DataView)interceptView).repaint();
	}
	
	protected DataPlusDistnInterface getDotDistnView(DataSet data, String paramKey, String theoryKey,
																											HorizAxis horizAxis) {
		return new StackedPlusNormalView(data, this, horizAxis, theoryKey);
	}
	
	protected XPanel jitteredPanel(DataSet data, String paramKey, String theoryKey,
												String axisParam, int densityDisplayType, int paramType) {
		XPanel paramPanel = new XPanel();
		paramPanel.setLayout(new AxisLayout());
			
		HorizAxis horizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisParam);
		horizAxis.readNumLabels(labelInfo);
		NumVariable param = (NumVariable)data.getVariable(paramKey);
		horizAxis.setAxisName(param.name);
		paramPanel.add("Bottom", horizAxis);
		
		DataPlusDistnInterface theView = getDotDistnView(data, paramKey, theoryKey, horizAxis);
		if (paramType == SLOPE)
			slopeView = theView;
		else
			interceptView = theView;
		theView.setShowDensity(densityDisplayType);
		theView.setDensityColor(kPaleBlue);
		
		DataView dataView = (DataView)theView;
		dataView.lockBackground(Color.white);
		dataView.setActiveNumVariable(paramKey);
		paramPanel.add("Center", dataView);
		
		return paramPanel;
	}
	
	protected XPanel stackedPanel(DataSet data, String paramKey, String theoryKey,
												String axisParam, int densityDisplayType, int paramType) {
		XPanel paramPanel = new XPanel();
		paramPanel.setLayout(new AxisLayout());
			
		HorizAxis horizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisParam);
		horizAxis.readNumLabels(labelInfo);
		NumVariable param = (NumVariable)data.getVariable(paramKey);
		horizAxis.setAxisName(param.name);
		paramPanel.add("Bottom", horizAxis);
		
		StackedPlusNormalView dataView = new StackedPlusNormalView(data, this, horizAxis, theoryKey);
		dataView.lockBackground(Color.white);
		dataView.setActiveNumVariable(paramKey);
		dataView.setShowDensity(densityDisplayType);
		dataView.setDensityColor(kPaleBlue);
		paramPanel.add("Center", dataView);
		
		if (paramType == SLOPE)
			slopeView = dataView;
		else
			interceptView = dataView;
		
		return paramPanel;
	}
	
	protected int initialTheoryDisplay() {
		return DataPlusDistnInterface.NO_DISTN;
	}
	
	protected XPanel bivarDisplayPanel(DataSet data, int viewType) {
		if (viewType == SAMPLE)
			return super.bivarDisplayPanel(data, viewType);
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
			thePanel.add(ProportionLayout.TOP, jitteredPanel(summaryData, "intercept", "interceptDistn",
												INTERCEPT_AXIS_PARAM, initialTheoryDisplay(), INTERCEPT));
			thePanel.add(ProportionLayout.BOTTOM, jitteredPanel(summaryData, "slope", "slopeDistn",
												SLOPE_AXIS_PARAM, initialTheoryDisplay(), SLOPE));
			return thePanel;
		}
	}
	
	public void setTheoryShow(boolean theoryShow) {
		interceptView.setShowDensity(theoryShow ? DataPlusDistnInterface.CONTIN_DISTN
																			: DataPlusDistnInterface.NO_DISTN);
		slopeView.setShowDensity(theoryShow ? DataPlusDistnInterface.CONTIN_DISTN
																			: DataPlusDistnInterface.NO_DISTN);
	}
}