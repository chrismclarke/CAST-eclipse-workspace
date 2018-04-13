package pairBlockProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import qnUtils.*;
import valueList.*;
import coreVariables.*;
import coreSummaries.*;
import imageUtils.*;

import test.*;
import exerciseSD.*;
import inference.*;
import imageGroups.*;

import pairBlock.*;


public class PairedInferenceApplet extends XApplet {
	static final private String RAW_AXIS_INFO_PARAM = "rawAxis";
	static final private String DIFF_AXIS_INFO_PARAM = "diffAxis";
	static final private String RESPONSE_NAME_PARAM = "responseName";
	static final private String X1_NAME_PARAM = "x1Name";
	static final private String X2_NAME_PARAM = "x2Name";
	static final private String DIFF_NAME_PARAM = "diffName";
	static final private String X1_VALUES_PARAM = "x1Values";
	static final private String X2_VALUES_PARAM = "x2Values";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String ALTERNATIVE_PARAM = "alternative";
	static final private String FORMULA_GIF_PARAM = "formulaGif";
	static final private String T_AXIS_INFO_PARAM = "tAxis";
	static final private String MEAN_SD_DECIMALS_PARAM = "meanSdDecimals";
	static final private String CI_DECIMALS_PARAM = "ciDecimals";
	
	static final private Color kDarkGreenColor = new Color(0x006600);
	static final private Color kResultBackground = new Color(0xCCCCFF);
	static final private Color kInferenceBackground = new Color(0xFFEEBB);
	static final private Color kLightGray = new Color(0x999999);
	
	static final private String kCiFormulaGif = "ci/pairedCiFormula.png";
	static final private String kCiFormulaGif2 = "ci/pairedCiFormula2.png";
	static final private int kCiImageAscent = 19;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private HypothesisTest test;
	private int testTail;
	
	private PairedDotPlotView pairedView;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		data = getData();
		
		test = readTestInfo(data, "diff");
		
		summaryData = getSummaryData(data);
		
		setLayout(new ProportionLayout(0.55, 5, ProportionLayout.VERTICAL));
			
			XPanel dataDisplayPanel = new XPanel();
			dataDisplayPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
		
			dataDisplayPanel.add(ProportionLayout.TOP, rawDataPanel(data));
			dataDisplayPanel.add(ProportionLayout.BOTTOM, diffDataPanel(data));
			
		add(ProportionLayout.TOP, dataDisplayPanel);
			
			XPanel inferencePanel = new InsetPanel(0, 10);
			inferencePanel.setLayout(new BorderLayout(20, 0));
			
			inferencePanel.add("West", ciPanel(data, summaryData));
			inferencePanel.add("Center", testPanel(data, summaryData));
			
			inferencePanel.lockBackground(kInferenceBackground);
		
		add(ProportionLayout.BOTTOM, inferencePanel);
	}
	
	private DataSet getData() {
		DataSet theData = new DataSet();
		
			NumVariable x1Var = new NumVariable(getParameter(X1_NAME_PARAM));
			x1Var.readValues(getParameter(X1_VALUES_PARAM));
		theData.addVariable("x1", x1Var);
		
			NumVariable x2Var = new NumVariable(getParameter(X2_NAME_PARAM));
			x2Var.readValues(getParameter(X2_VALUES_PARAM));
		theData.addVariable("x2", x2Var);
		
			SumDiffVariable diffVar = new SumDiffVariable(getParameter(DIFF_NAME_PARAM), theData, 
																														"x2", "x1", SumDiffVariable.DIFF);
		theData.addVariable("diff", diffVar);
		
		return theData;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet theSummaryData = new SummaryDataSet(data, "y");
		
		StringTokenizer st = new StringTokenizer(getParameter(SUMMARY_DECIMALS_PARAM));
		int zDecimals = Integer.parseInt(st.nextToken());
		int pValueDecimals = Integer.parseInt(st.nextToken());
		
			StatisticValueVariable zValue = new StatisticValueVariable(translate("t statistic"), test, zDecimals);
		theSummaryData.addVariable("z", zValue);
		
			PValueVariable pValue = new PValueVariable(translate("p-value") + " = ", test, pValueDecimals);
		theSummaryData.addVariable("pValue", pValue);
		
			NumVariable yVar = (NumVariable)data.getVariable("x1");
			int n = yVar.noOfValues();
			TDistnVariable tDistn = new TDistnVariable(translate("t distn"), n - 1);
		theSummaryData.addVariable("zDistn", tDistn);
		
			int decimals = Integer.parseInt(getParameter(CI_DECIMALS_PARAM));
			MeanCIVariable ci = new MeanCIVariable(translate("Mean"), 1.0, n - 1, "diff", decimals);
			ci.setTFromLevel(0.95);
		theSummaryData.addVariable("ci", ci);
		
		theSummaryData.setSingleSummaryFromData();
		return theSummaryData;
	}
	
	private HypothesisTest readTestInfo(DataSet data, String yKey) {
		String tailString = getParameter(ALTERNATIVE_PARAM);
		if (tailString.equals("low"))
			testTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			testTail = HypothesisTest.HA_HIGH;
		else
			testTail = HypothesisTest.HA_NOT_EQUAL;
		
		return new UnivarHypothesisTest(data, yKey, new NumValue(0, 0), testTail,
																							HypothesisTest.PAIRED_DIFF_MEAN, this);
	}
	
	private XPanel rawDataPanel(DataSet data) {
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			String labelInfo = getParameter(RAW_AXIS_INFO_PARAM);
			horizAxis.readNumLabels(labelInfo);
			horizAxis.setAxisName(getParameter(RESPONSE_NAME_PARAM));
		
		dataPanel.add("Bottom", horizAxis);
		
			CatVariable tempVar = new CatVariable("");
			tempVar.readLabels(getParameter(X1_NAME_PARAM) + " " + getParameter(X2_NAME_PARAM));
			
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.setCatLabels(tempVar);
		
		dataPanel.add("Left", vertAxis);
		
			pairedView = new PairedDotPlotView(data, this, "x1", "x2", horizAxis, vertAxis, 1.0);
			pairedView.setRetainLastSelection(true);
			pairedView.setShowPairing(true);
			pairedView.lockBackground(Color.white);
			
		dataPanel.add("Center", pairedView);
		
		return dataPanel;
	}
	
	private XPanel diffDataPanel(DataSet data) {
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			String labelInfo = getParameter(DIFF_AXIS_INFO_PARAM);
			horizAxis.readNumLabels(labelInfo);
			horizAxis.setAxisName(getParameter(DIFF_NAME_PARAM));
		
		plotPanel.add("Bottom", horizAxis);
		
			int meanSdDecimals = Integer.parseInt(getParameter(MEAN_SD_DECIMALS_PARAM));
			StackMeanSdView dataView = new StackMeanSdView(data, this, horizAxis, "diff", meanSdDecimals);
			
			dataView.setXChar('d');
			dataView.setRetainLastSelection(true);
			dataView.lockBackground(Color.white);
			
		plotPanel.add("Center", dataView);
	
		return plotPanel;
	}
	
	private XPanel ciPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 30));
		
			XLabel heading = new XLabel(translate("95% Confidence Interval"), XLabel.LEFT, this);
			heading.setFont(getBigBoldFont());
			heading.setForeground(Color.blue);
		thePanel.add(heading);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 3));
		
				NumVariable yVar = (NumVariable)data.getVariable("x1");
				int df = yVar.noOfValues() - 1;
				FixedValueView dfView = new FixedValueView(translate("Degrees of freedom"), new NumValue(df, 0), df, this);
				dfView.addEqualsSign();
			topPanel.add(dfView);
			
				double tValue = TTable.quantile(0.975, df);
				FixedValueView tView = new FixedValueView(translate("t-value"), new NumValue(tValue, 3), tValue, this);
				tView.addEqualsSign();
			topPanel.add(tView);
			
		thePanel.add(topPanel);
		
			XPanel resultPanel = new InsetPanel(10, 4);
			resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				String testGifName = getParameter(FORMULA_GIF_PARAM);
				String ciGifName = kCiFormulaGif;
				if (testGifName.charAt(testGifName.length() - 1) == '2')
					ciGifName = kCiFormulaGif2;		//	(messy)  if testGif ends in '2', so should ciGif. (This is a hack to allow both to use 'g' instead of 'n' as sample size)
				OneValueImageView ciView = new OneValueImageView(summaryData, "ci", this, ciGifName, kCiImageAscent);
				ciView.addEqualsSign();
			resultPanel.add(ciView);
			resultPanel.lockBackground(kResultBackground);
			
		thePanel.add(resultPanel);
		
		return thePanel;
	}
	
	private XPanel testPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel tCalcPanel = new XPanel();
			tCalcPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 15));
				
				HypothesisView hypoth = new HypothesisView(test, this);
			tCalcPanel.add(hypoth);
		
				String formulaGif = getParameter(FORMULA_GIF_PARAM) + ".gif";
				OneValueImageView zCalculator = new OneValueImageView(summaryData, "z", this, formulaGif, 25);
			tCalcPanel.add(zCalculator);
		
		thePanel.add("North", tCalcPanel);
		
		thePanel.add("Center", tDistnPanel(summaryData));
			
			XPanel pValuePanel = new XPanel();
			pValuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
				XPanel resultPanel = new InsetPanel(10, 4);
				resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					OneValueView pValueView = new OneValueView(summaryData, "pValue", this);
					pValueView.setForeground(kDarkGreenColor);
				resultPanel.add(pValueView);
				resultPanel.lockBackground(kResultBackground);
			
			pValuePanel.add(resultPanel);
			
		thePanel.add("South", pValuePanel);
		
		return thePanel;
	}
	
	private XPanel tDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(T_AXIS_INFO_PARAM));
//			CoreVariable v = summaryData.getVariable("z");
//			theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
			int tail = (testTail == HypothesisTest.HA_LOW) ? DistnTailView.LOW_TAIL
							: (testTail == HypothesisTest.HA_HIGH) ? DistnTailView.HIGH_TAIL
							: DistnTailView.TWO_TAIL;
			DistnTailView tView = new DistnTailView(summaryData, this, theHorizAxis, "zDistn", "z", tail);
			TDistnVariable tDistn = (TDistnVariable)summaryData.getVariable("zDistn");
			int df = tDistn.getDF();
			tView.setDistnName(new LabelValue(translate("t distn") + " (" + df + "df)"), kLightGray);
			
			tView.lockBackground(Color.white);
			tView.setForeground(Color.red);
			tView.setHighlightColor(kDarkGreenColor);
		
		thePanel.add("Center", tView);
		
		return thePanel;
	}
}