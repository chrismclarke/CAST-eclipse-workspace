package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import random.*;
import utils.*;
import coreGraphics.*;
import coreVariables.*;
import models.*;

import ssq.*;
import variance.*;


public class RawFDistnApplet extends XApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String MAX_MSSQ_PARAM = "maxMeanSsq";
	static final private String MAX_F_PARAM = "maxF";
	static final private String MEAN_SD_PARAM = "meanSD";
	static final protected String F_AXIS_INFO_PARAM = "fAxis";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final protected String AREA_PROPN_PARAM = "areaProportion";
	
	static final private NumValue kMaxRSquared = new NumValue(1.0, 4);
	
	protected String kComponentNames[];
	
	protected DataSet data;
	protected AnovaSummaryData summaryData;
	protected NumValue maxSsq, maxMeanSsq, maxF;
	
	private int noOfValues;
	
	private RepeatingButton sampleButton;
	
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	private int sampleSize[];
	
	protected DataPlusDistnInterface fDistnView;
	
	public void setupApplet() {
		readMaxes();
		sampleSizeChoice = getSampleSizeChoice();
		
		kComponentNames = new String[3];
		kComponentNames[0] = translate("Total (about 0)");
		kComponentNames[1] = translate("Mean");
		kComponentNames[2] = translate("About mean");
		
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		adjustDF(summaryData);
		
		setLayout(new ProportionLayout(1.0 - getFDisplayProportion(), 10, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 5));
			topPanel.add("Center", dataPanel(data));
		
			topPanel.add("East", controlPanel());
			topPanel.add("South", anovaTable(summaryData));
			topPanel.add("North", sliderPanel(data));
		
		add(ProportionLayout.TOP, topPanel);
			
		add(ProportionLayout.BOTTOM, fDistnPanel(summaryData));
	}
	
	protected double getFDisplayProportion() {
		return 0.5;
	}
	
	
	private void readMaxes() {
		String s = getParameter(MAX_SSQ_PARAM);
		if (s !=  null)
			maxSsq = new NumValue(s);
		s = getParameter(MAX_MSSQ_PARAM);
		if (s !=  null)
			maxMeanSsq = new NumValue(s);
		s = getParameter(MAX_F_PARAM);
		if (s !=  null)
			maxF = new NumValue(s);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			RandomNormal generator = new RandomNormal(noOfValues, 0.0, 1.0, 3.0);
			NumVariable error = new NumSampleVariable("error", generator, 10);
		data.addVariable("error", error);
		
		ScaledVariable y = new ScaledVariable(getParameter(VAR_NAME_PARAM), error, "error",
																														getParameter(MEAN_SD_PARAM));
		data.addVariable("y", y);
		
			NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
			dataDistn.setParams(getParameter(MEAN_SD_PARAM));
		data.addVariable("model", dataDistn);
		
		for (int i=0 ; i<3 ; i++) {
			String componentKey = SimpleComponentVariable.kComponentKey[i];
			double target = dataDistn.getMean().toDouble();
			data.addVariable(componentKey, new SimpleComponentVariable(componentKey, data, "y",
													null, target, SimpleComponentVariable.kComponentType[i], 10));
		}
		return data;
	}
	
	
	protected AnovaSummaryData getSummaryData(DataSet sourceData) {
		AnovaSummaryData summaryData = new AnovaSummaryData(sourceData, "error",
								SimpleComponentVariable.kComponentKey, maxSsq.decimals, kMaxRSquared.decimals);
		
			String meanKey = SimpleComponentVariable.kComponentKey[1];
			String residKey = SimpleComponentVariable.kComponentKey[2];
			SsqRatioVariable f = new SsqRatioVariable("F ratio", meanKey,
																		residKey, maxF.decimals, SsqRatioVariable.MEAN_SSQ);
		summaryData.addVariable("F", f);
		
			FDistnVariable fDistn = new FDistnVariable("F distn", 1, 1);
		summaryData.addVariable("fDistn", fDistn);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private XChoice getSampleSizeChoice() {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int noOfSizes = st.countTokens();
		sampleSize = new int[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++) {
			String nextSize = st.nextToken();
			boolean isInitialSize = nextSize.startsWith("*");
			if (isInitialSize) {
				nextSize = nextSize.substring(1);
				currentSampleSizeIndex = i;
			}
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		noOfValues = sampleSize[currentSampleSizeIndex];
		
		XChoice choice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length; i++)
			choice.addItem("n = " + String.valueOf(sampleSize[i]));
		choice.select(currentSampleSizeIndex);
		return choice;
	}
	
	private void adjustDF(AnovaSummaryData summaryData) {
		SimpleComponentVariable residComponent = (SimpleComponentVariable)data.
																				getVariable(SimpleComponentVariable.kComponentKey[2]);
		int residDF = residComponent.getDF();
		
		SimpleComponentVariable meanComponent = (SimpleComponentVariable)data.
																				getVariable(SimpleComponentVariable.kComponentKey[1]);
		int meanDF = meanComponent.getDF();
		
		FDistnVariable fDistn  = (FDistnVariable)summaryData.getVariable("fDistn");
		fDistn.setDF(meanDF, residDF);
		
		if (fDistnView != null) {
			adjustFDistnLabel(fDistnView, meanDF, residDF);
			int selectedIndex = summaryData.getSelection().findSingleSetFlag();
			summaryData.variableChanged("fDistn", selectedIndex);
		}
	}
	
	protected XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			XLabel theLabel = new XLabel(translate("Sample data"), XLabel.LEFT, this);
			theLabel.setFont(getStandardBoldFont());
		thePanel.add(theLabel);
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
//			Variable v = (Variable)data.getVariable("y");
//			horizAxis.setAxisName(v.name);
		thePanel.add("Bottom", horizAxis);
		
			DataView dataView = createSampleView(data, horizAxis);
			
			dataView.setActiveNumVariable("y");
			dataView.lockBackground(Color.white);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	protected DataView createSampleView(DataSet data, HorizAxis horizAxis) {
		return new StackedPlusNormalView(data, this, horizAxis, "model");
	}
	
	private void adjustFDistnLabel(DataPlusDistnInterface fView, int explainedDF, int residDF) {
		fView.setDistnLabel(new LabelValue("F(" + explainedDF + ", " + residDF + " df)"),
																																									Color.gray);
	}
	
	private XPanel fDistnPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(F_AXIS_INFO_PARAM));
			horizAxis.setAxisName(translate("F ratio"));
		thePanel.add("Bottom", horizAxis);
		
			fDistnView = createFView(summaryData, horizAxis);
			
			FDistnVariable fDistn = (FDistnVariable)summaryData.getVariable("fDistn");
			adjustFDistnLabel(fDistnView, fDistn.getDF1(), fDistn.getDF2());
		
		thePanel.add("Center", (DataView)fDistnView);
		
		return thePanel;
	}
	
	protected DataPlusDistnInterface createFView(SummaryDataSet summaryData, HorizAxis horizAxis) {
		StackedPlusNormalView theView = new StackedPlusNormalView(summaryData, this,
								horizAxis, "fDistn", StackedPlusNormalView.ACCURATE_STACK_ALGORITHM);
		theView.setActiveNumVariable("F");
		double areaProportion = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
		theView.setAreaProportion(areaProportion);
		
		theView.lockBackground(Color.white);
			
		return theView;
	}
	
	protected AnovaTableView anovaTable(AnovaSummaryData summaryData) {
		AnovaTableView table = new AnovaTableView(summaryData, this,
									SimpleComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_AND_F);
		table.setComponentNames(kComponentNames);
		return table;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(sampleSizeChoice);
		
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSampleSizeIndex) {
				currentSampleSizeIndex = newChoice;
				noOfValues = sampleSize[currentSampleSizeIndex];
				summaryData.changeSampleSize(noOfValues);
				adjustDF(summaryData);
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