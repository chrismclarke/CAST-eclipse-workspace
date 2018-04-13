package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import valueList.*;
import distn.*;
import utils.*;
import qnUtils.*;
import coreGraphics.*;
import coreSummaries.*;
import models.*;

import test.*;
import ssq.*;
import variance.*;


public class FandTApplet extends XApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String F_AXIS_INFO_PARAM = "fAxis";
	static final private String T_AXIS_INFO_PARAM = "tAxis";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String TARGET_PARAM = "target";
	static final private String TARGET_NAME_PARAM = "targetName";
	
	static final protected NumValue kMaxRSquared = new NumValue(1.0, 4);
	
	protected String componentNames[] = {"Total (about ", translate("Mean"), "About mean"};
	
	private DataSet data;
	private AnovaSummaryData summaryData;
	protected NumValue maxSsq, maxMeanSsq, maxF, maxT;
	
	private NumValue target;
	
	public void setupApplet() {
		readMaxes();
		
		data = getData();
		summaryData = getSummaryData(data);
		setupComponentNames();
		
		setLayout(new ProportionLayout(1.0 - getFDisplayProportion(), 10, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 5));
				
				XPanel topTopPanel = new XPanel();
				topTopPanel.setLayout(new ProportionLayout(0.6, 0, ProportionLayout.HORIZONTAL,
																																ProportionLayout.TOTAL));
				topTopPanel.add(ProportionLayout.LEFT, dataPanel(data));
				topTopPanel.add(ProportionLayout.RIGHT, summaryValuePanel(summaryData));
			
			topPanel.add("Center", topTopPanel);
		
			topPanel.add("South", anovaTable(summaryData));
		
		add(ProportionLayout.TOP, topPanel);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																																ProportionLayout.TOTAL));
			bottomPanel.add(ProportionLayout.LEFT, tDistnPanel(summaryData));
			bottomPanel.add(ProportionLayout.RIGHT, fDistnPanel(summaryData));
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	protected void setupComponentNames() {
		componentNames[0] += target.toString() + ")";
	}
	
	protected double getFDisplayProportion() {
		return 0.35;
	}
	
	
	private void readMaxes() {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMeanSsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxT = new NumValue(st.nextToken());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		target = new NumValue(getParameter(TARGET_PARAM));
		
		for (int i=0 ; i<3 ; i++) {
			String componentKey = SimpleComponentVariable.kComponentKey[i];
			data.addVariable(componentKey, new SimpleComponentVariable(componentKey, data, "y",
													null, target.toDouble(), SimpleComponentVariable.kComponentType[i], 10));
		}
		return data;
	}
	
	
	protected AnovaSummaryData getSummaryData(DataSet sourceData) {
		AnovaSummaryData summaryData = new AnovaSummaryData(sourceData, "y",
								SimpleComponentVariable.kComponentKey, maxSsq.decimals, kMaxRSquared.decimals);
		
			String meanKey = SimpleComponentVariable.kComponentKey[1];
			String residKey = SimpleComponentVariable.kComponentKey[2];
			SsqRatioVariable f = new SsqRatioVariable("F ratio", meanKey,
																		residKey, maxF.decimals, SsqRatioVariable.MEAN_SSQ);
		summaryData.addVariable("F", f);
		
			int nValues = ((NumVariable)sourceData.getVariable("y")).noOfValues();
			FDistnVariable fDistn = new FDistnVariable("F distn", 1, nValues - 1);
		summaryData.addVariable("fDistn", fDistn);
		
			MeanVariable mean = new MeanVariable(translate("Mean"), "y", 10);
		summaryData.addVariable("mean", mean);
		
			HypothesisTest tTest = new UnivarHypothesisTest(sourceData, "y", target,
																				HypothesisTest.HA_NOT_EQUAL, HypothesisTest.MEAN, this);
			StatisticValueVariable tValue = new StatisticValueVariable(translate("t statistic"), tTest, maxT.decimals);
		summaryData.addVariable("t", tValue);
		
			TDistnVariable tDistn = new TDistnVariable(translate("t distn"), nValues - 1);
		summaryData.addVariable("tDistn", tDistn);
		
		summaryData.setSingleSummaryFromData();
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
//			Variable v = (Variable)data.getVariable("y");
//			horizAxis.setAxisName(v.name);
		thePanel.add("Bottom", horizAxis);
		
			DataView dataView = new MeanTargetDataView(data, this, horizAxis, target.toDouble(),
																										new LabelValue(getParameter(TARGET_NAME_PARAM)));
			dataView.setActiveNumVariable("y");
			dataView.lockBackground(Color.white);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel fDistnPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(F_AXIS_INFO_PARAM));
//			horizAxis.setAxisName("F ratio");
		thePanel.add("Bottom", horizAxis);
		
			AccurateTailAreaView fDistnView = new AccurateTailAreaView(summaryData, this, horizAxis, "fDistn");
			fDistnView.setActiveNumVariable("F");
			fDistnView.setDensityScaling(2.5);
			fDistnView.lockBackground(Color.white);
			
		thePanel.add("Center", fDistnView);
		
		return thePanel;
	}
	
	private XPanel tDistnPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(T_AXIS_INFO_PARAM));
//			horizAxis.setAxisName(translate("t statistic"));
		thePanel.add("Bottom", horizAxis);
		
			AccurateTailAreaView tDistnView = new AccurateTailAreaView(summaryData, this, horizAxis, "tDistn");
			tDistnView.setTailType(AccurateTailAreaView.TWO_TAILED);
			tDistnView.setActiveNumVariable("t");
			tDistnView.setValueLabel(new LabelValue("t"));
			tDistnView.setDensityScaling(0.75);
			tDistnView.lockBackground(Color.white);
			
		thePanel.add("Center", tDistnView);
		
		return thePanel;
	}
	
	protected AnovaTableView anovaTable(AnovaSummaryData summaryData) {
		AnovaTableView table = new AnovaTableView(summaryData, this,
									SimpleComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_F_PVALUE);
		table.setComponentNames(componentNames);
		return table;
	}
	
	protected XPanel summaryValuePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(new OneValueView(summaryData, "t", this, maxT));
		thePanel.add(new OneValueView(summaryData, "mean", this));
		
		return thePanel;
	}
}