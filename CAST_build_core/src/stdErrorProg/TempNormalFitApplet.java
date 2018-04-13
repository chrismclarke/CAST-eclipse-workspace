package stdErrorProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.*;
import distn.*;
import coreGraphics.*;

import corr.*;
import stdError.*;


public class TempNormalFitApplet extends XApplet {
	static final private String DATA_AXIS_INFO_PARAM = "dataAxis";
	static final private String PERCENTILE_PARAM = "percentile";
	static final private String EXTRA_DECIMALS_PARAM = "extraDecimals";
	static final private String MAX_PERCENTILE_PARAM = "maxPercentile";
	static final private String SHOW_FIT_PARAM = "showFit";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private NumValue maxPercentile;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(30, 0));
		
		add("Center", dataPanel(data));
		add("South", controlPanel(data, summaryData));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		int n = 0;
		double sy = 0.0;
		double syy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			n ++;
			sy += y;
			syy += y * y;
		}
		double mean = sy / n;
		double sd = Math.sqrt((syy - sy * mean) / (n - 1));
		
		NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
		dataDistn.setMean(mean);
		dataDistn.setSD(sd);
		data.addVariable("fit", dataDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			double targetProb = Double.parseDouble(getParameter(PERCENTILE_PARAM));
			maxPercentile = new NumValue(getParameter(MAX_PERCENTILE_PARAM));
			PercentileVariable estimator = new PercentileVariable("Percentile", "y",
																										targetProb, maxPercentile.decimals);
			
		summaryData.addVariable("percentile", estimator);
		
		return summaryData;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(DATA_AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			Variable v = (Variable)data.getVariable("y");
			theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedPlusNormalView dataView = new StackedPlusNormalView(data, this, theHorizAxis, "fit",
																					StackedPlusNormalView.ACCURATE_STACK_ALGORITHM);
			dataView.setShowDensity(getParameter(SHOW_FIT_PARAM).equals("true")
											? StackedPlusNormalView.CONTIN_DISTN : StackedPlusNormalView.NO_DISTN);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			int extraMeanSdDecimals = Integer.parseInt(getParameter(EXTRA_DECIMALS_PARAM));
		
			MeanView biasValueView = new MeanView(data, "y", MeanView.GENERIC_TEXT_FORMULA, extraMeanSdDecimals, this);
		thePanel.add(biasValueView);
		
			StDevnView seValueView = new StDevnView(data, "y", MeanView.GENERIC_TEXT_FORMULA, extraMeanSdDecimals, this);
		thePanel.add(seValueView);
		
			OneValueView percentileView = new OneValueView(summaryData, "percentile", this, maxPercentile);
		thePanel.add(percentileView);
		
		return thePanel;
	}
}