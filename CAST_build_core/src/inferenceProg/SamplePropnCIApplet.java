package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import inference.*;
import random.RandomMultinomial;
import cat.*;


public class SamplePropnCIApplet extends XApplet {
	static final private String CAT_PROB_PARAM = "catProb";
	static final private String SAMPLING_PARAM = "sampling";
	static final private String SUMMARY_AXIS_INFO_PARAM = "summaryAxis";
	static final protected String PROPN_NAME_PARAM = "propnName";
	static final protected String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final protected String PROB_AXIS_PARAM = "probAxis";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
//	private BarPlusProbView dataView;
	private IntervalView summaryView;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private HorizAxis summaryAxis;
	
	private NumValue modelPropn;
	private int decimals;
	
	public void setupApplet() {
		decimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		data = getData();
		summaryData = getSummaryData(data);
		
		summaryData.takeSample();
		
//		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
//																							ProportionLayout.TOTAL));
//		add(ProportionLayout.LEFT, samplePanel(data, "y", "model", summaryData));
//		add(ProportionLayout.RIGHT, summaryPanel(summaryData, "ci", modelPropn));
		
		setLayout(new BorderLayout(10, 0));
		add("West", samplePanel(data, "y", "model", summaryData));
		add("Center", summaryPanel(summaryData, "ci", modelPropn));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		CatDistnVariable dv = new CatDistnVariable(translate("Popn"));
		dv.readLabels(getParameter(CAT_LABELS_PARAM));
		dv.setParams(getParameter(CAT_PROB_PARAM));
		data.addVariable("model", dv);
		
		double p[] = dv.getProbs();
		modelPropn = new NumValue(p[0], decimals);
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLING_PARAM));
		int sampleSize = Integer.parseInt(st.nextToken());
		long samplingSeed = Long.parseLong(st.nextToken());
		RandomMultinomial generator = new RandomMultinomial(sampleSize, dv.getProbs());
		generator.setSeed(samplingSeed);
		
		CatSampleVariable sv = new CatSampleVariable(getParameter(CAT_NAME_PARAM), generator);
		sv.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", sv);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		PropnCIVariable ci = new PropnCIVariable(getParameter(PROPN_NAME_PARAM), 2.0, "y", decimals);
		summaryData.addVariable("ci", ci);
		
		return summaryData;
	}
	
	private XPanel samplePanel(DataSet data, String variableKey, String modelKey, DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("North", dataTablePanel(data, variableKey));
		thePanel.add("Center", dataPieView(data, variableKey));
		thePanel.add("South", sampleControlPanel(summaryData));
		
		return thePanel;
	}
	
	private DataView dataPieView(DataSet data, String variableKey) {
		return new PieView(data, this, variableKey, CatDataView.SELECT_ONE);
	}
	
	private XPanel dataTablePanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
			CatVariable v = (CatVariable)data.getVariable(variableKey);
			XLabel varName = new XLabel(v.name, XLabel.CENTER, this);
			varName.setFont(getStandardBoldFont());
		thePanel.add(varName);
		
			XPanel tablePanel = new InsetPanel(4, 3, 4, 3);
			tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			tablePanel.lockBackground(Color.white);
				FreqTableView tableView = new FreqTableView(data, this, variableKey, CatDataView.SELECT_ONE, decimals);
				tableView.lockBackground(Color.white);
				
			tablePanel.add(tableView);
		
		thePanel.add(tablePanel);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet summaryData, String variableKey, NumValue targetMean) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("Center", summaryDataPanel(summaryData, variableKey, targetMean));
		thePanel.add("South", summaryControlPanel(summaryData, variableKey));
		
		return thePanel;
	}
	
	private XPanel summaryDataPanel(DataSet summaryData, String variableKey, NumValue targetMean) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		summaryAxis = new HorizAxis(this);
		summaryAxis.readNumLabels(getParameter(SUMMARY_AXIS_INFO_PARAM));
		
		thePanel.add("Bottom", summaryAxis);
		
		summaryView = new IntervalView(summaryData, this, summaryAxis, variableKey, targetMean);
		thePanel.add("Center", summaryView);
		summaryView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel sampleControlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		ValueCountView theCount = new ValueCountView(summaryData, this);
		theCount.setLabel(translate("No of samples") + " =");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	private XPanel summaryControlPanel(DataSet summaryData, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 15));
		
			OneValueView ciValueView = new OneValueView(summaryData, variableKey, this);
			ciValueView.setFont(getBigFont());
		thePanel.add(ciValueView);
		
			CoverageValueView coverage = new CoverageValueView(summaryData, variableKey, this, modelPropn);
			coverage.setFont(getBigFont());
		thePanel.add(coverage);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}