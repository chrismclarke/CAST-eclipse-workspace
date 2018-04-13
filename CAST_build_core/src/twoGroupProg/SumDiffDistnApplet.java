package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import distn.*;
import coreGraphics.*;
import coreVariables.*;
import coreSummaries.*;
import formula.*;

import twoGroup.*;


public class SumDiffDistnApplet extends XApplet {
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String START_MEAN_SD_PARAM = "startMeanSd";
	static final private String MAX_MEAN_PARAM_PARAM = "maxMeanParam";
	static final private String MAX_SUM_DIFF_SD_PARAM = "maxSumDiffSd";
	static final private String EQUAL_MEANS_PARAM = "equalMeans";
	static final private String EQUAL_SDS_PARAM = "equalSds";
	static final private String SHOW_SUM_PARAM = "showSum";
	
	static final private Color kMeanSdHeadingColor = new Color(0x660000);	//	dark red
	static final private Color kDensityColor = new Color(0xC0C0C0);	//	pale gray
	static final private Color kSumDiffLabelColor = new Color(0xFFAAAA);
	
	static final private Color kMeanSdBackground = new Color(0xDDDDEE);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private DragMeanSdView y1View, y2View;
	private StackedPlusNormalView sumDiffView;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	private XCheckbox equalMeansCheck, equalSdsCheck;
	private boolean meansAlwaysEqual, sdsAlwaysEqual;
	
	private XChoice sumDiffChoice;
	private int currentSumDiff = 0;
	
	private NumValue maxSumDiffSd;
	private NumValue maxMeanParam;
	
	private boolean showSum;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(data, summaryData);
		
		setLayout(new BorderLayout(10, 0));
		
		add("Center", leftPanel(summaryData));
		add("East", rightPanel(summaryData));
		add("South", samplingPanel());
		add("North", distnControlPanel());
	}
	
	private void generateInitialSample(DataSet data, SummaryDataSet summaryData) {
		setTheoryParameters(data, summaryData);
		summaryData.takeSample();
	}
	
	private DataSet getData() {
		String showSumString = getParameter(SHOW_SUM_PARAM);
		showSum = (showSumString == null) || showSumString.equals("true");
		
		StringTokenizer st = new StringTokenizer(getParameter(START_MEAN_SD_PARAM));
		NumValue startMean = new NumValue(st.nextToken());
		NumValue startSd = new NumValue(st.nextToken());
		
		maxMeanParam = new NumValue(getParameter(MAX_MEAN_PARAM_PARAM));
		int xParamDecimals = Math.max(startMean.decimals, startSd.decimals);
		
		DataSet data = new DataSet();
		
		RandomNormal y1Generator = new RandomNormal(1, 0.0, 1.0, 3.0);
		NumVariable y1Core = new NumSampleVariable("y1Core", y1Generator, 10);
		data.addVariable("y1Core", y1Core);
		
		RandomNormal y2Generator = new RandomNormal(1, 0.0, 1.0, 3.0);
		NumVariable y2Core = new NumSampleVariable("y2Core", y2Generator, 10);
		data.addVariable("y2Core", y2Core);
		
		data.addVariable("yy", new BiSampleVariable(data, "y1Core", "y2Core"));
		
		ScaledVariable y1 = new ScaledVariable(MText.expandText("Y#sub1#"), y1Core, "y1Core",
																			startMean.toDouble(), startSd.toDouble(), xParamDecimals);
		data.addVariable("y1", y1);
		
		ScaledVariable y2 = new ScaledVariable(MText.expandText("Y#sub2#"), y2Core, "y2Core",
																			startMean.toDouble(), startSd.toDouble(), xParamDecimals);
		data.addVariable("y2", y2);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "yy");
		
			MeanVariable x1 = new MeanVariable(MText.expandText("X#sub1#"), "y1", 10);
		summaryData.addVariable("x1", x1);
		
			MeanVariable x2 = new MeanVariable(MText.expandText("X#sub2#"), "y2", 10);
		summaryData.addVariable("x2", x2);
		
		if (showSum) {
			String sumName = MText.expandText("X#sub1# + X#sub2#");
			SumDiffVariable sum = new SumDiffVariable(sumName, summaryData, "x1", "x2", SumDiffVariable.SUM);
			summaryData.addVariable("sum", sum);
		}
		
			String diffName = MText.expandText("X#sub1# - X#sub2#");
			SumDiffVariable diff = new SumDiffVariable(diffName, summaryData, "x1", "x2", SumDiffVariable.DIFF);
		summaryData.addVariable("diff", diff);
		
			int meanDecimals = maxMeanParam.decimals;
		
			NormalDistnVariable x1Distn = new NormalDistnVariable("x1 model");
			x1Distn.setDecimals(meanDecimals);
		summaryData.addVariable("x1Distn", x1Distn);
		
			NormalDistnVariable x2Distn = new NormalDistnVariable("x2 model");
			x2Distn.setDecimals(meanDecimals);
		summaryData.addVariable("x2Distn", x2Distn);
		
			maxSumDiffSd = new NumValue(getParameter(MAX_SUM_DIFF_SD_PARAM));
			int sdDecimals = maxSumDiffSd.decimals;
		
		if (showSum) {
			NormalDistnVariable sumDistn = new NormalDistnVariable("sum theory");
			sumDistn.setDecimals(meanDecimals, sdDecimals);
			summaryData.addVariable("sumTheory", sumDistn);
		}
		
			NormalDistnVariable diffDistn = new NormalDistnVariable("diff theory");
			diffDistn.setDecimals(meanDecimals, sdDecimals);
		summaryData.addVariable("diffTheory", diffDistn);
		
		return summaryData;
	}
	
	private void setTheoryParameters(DataSet data, SummaryDataSet summaryData) {
		ScaledVariable y1 = (ScaledVariable)data.getVariable("y1");
		double y1Mean = y1.getParam(0);
		double y1Sd = y1.getParam(1);
		
		ScaledVariable y2 = (ScaledVariable)data.getVariable("y2");
		double y2Mean = y2.getParam(0);
		double y2Sd = y2.getParam(1);
		
		NormalDistnVariable x1Distn = (NormalDistnVariable)summaryData.getVariable("x1Distn");
		x1Distn.setMean(y1Mean);
		x1Distn.setSD(y1Sd);
		
		NormalDistnVariable x2Distn = (NormalDistnVariable)summaryData.getVariable("x2Distn");
		x2Distn.setMean(y2Mean);
		x2Distn.setSD(y2Sd);
		
		double sumDiffSd = Math.sqrt(y1Sd * y1Sd + y2Sd * y2Sd);
		
		if (showSum) {
			NormalDistnVariable sumDistn = (NormalDistnVariable)summaryData.getVariable("sumTheory");
			sumDistn.setMean(y1Mean + y2Mean);
			sumDistn.setSD(sumDiffSd);
		}
		
		NormalDistnVariable diffDistn = (NormalDistnVariable)summaryData.getVariable("diffTheory");
		diffDistn.setMean(y1Mean - y2Mean);
		diffDistn.setSD(sumDiffSd);
	}
	
	private XPanel dataPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			y1View = new DragMeanSdView(summaryData, this, theHorizAxis, "x1", "x1Distn",
																													true, true, maxMeanParam.decimals);
			y1View.lockBackground(Color.white);
			y1View.setDensityColor(kDensityColor);
			y1View.setViewBorder(new Insets(5, 0, 5, 0));
			y1View.setEqualMeans(true);
			y1View.setEqualSds(true);
		
			y2View = new DragMeanSdView(summaryData, this, theHorizAxis, "x2", "x2Distn",
																													true, true, maxMeanParam.decimals);
			y2View.lockBackground(Color.white);
			y2View.setDensityColor(kDensityColor);
			y2View.setViewBorder(new Insets(5, 0, 5, 0));
			y2View.setEqualMeans(true);
			y2View.setEqualSds(true);
			
			y1View.setLinkedView(y2View);
			y2View.setLinkedView(y1View);
			MultipleDataView dataView = new MultipleDataView(summaryData, this, y1View, y2View);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel sumDiffPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			String sumDiffTheoryKey = showSum ? "sumTheory" : "diffTheory";
			String sumDiffSampleKey = showSum ? "sum" : "diff";
			
			sumDiffView = new StackedPlusNormalView(summaryData, this, theHorizAxis, sumDiffTheoryKey);
			sumDiffView.setActiveNumVariable(sumDiffSampleKey);
			sumDiffView.lockBackground(Color.white);
			sumDiffView.setDensityColor(kDensityColor);
			sumDiffView.setViewBorder(new Insets(5, 0, 5, 0));
		
			Font f = getBigBoldFont();
			int biggerSize = (int)Math.round(2.0 * f.getSize());
			Font biggerFont = new Font(f.getName(), f.getStyle(), biggerSize);
			sumDiffView.setFont(biggerFont);
			setSumDiffLabel(summaryData, showSum);
			
		thePanel.add("Center", sumDiffView);
		
		return thePanel;
	}
	
	private XPanel leftPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 5, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, dataPanel(summaryData));
		
		thePanel.add(ProportionLayout.BOTTOM, sumDiffPanel(summaryData));
		
		return thePanel;
	}
	
	private void addParamHeadings(XPanel thePanel, GridBagLayout gbl, GridBagConstraints gbParam) {
		
		XLabel meanLabel = new XLabel(translate("Mean"), XLabel.CENTER, this);
		meanLabel.setFont(getStandardBoldFont());
		meanLabel.setForeground(kMeanSdHeadingColor);
		thePanel.add(meanLabel);
		gbParam.gridx = 1;
		gbl.setConstraints(meanLabel, gbParam);
		
		XLabel sdLabel = new XLabel(translate("St devn"), XLabel.CENTER, this);
		sdLabel.setFont(getStandardBoldFont());
		sdLabel.setForeground(kMeanSdHeadingColor);
		thePanel.add(sdLabel);
		gbParam.gridx = 2;
		gbl.setConstraints(sdLabel, gbParam);
		
		gbParam.gridy ++;
	}
	
	private void addParamRow(DataSet data, String valueKey, String distnKey, XPanel thePanel,
											GridBagLayout gbl, GridBagConstraints gbVarName, GridBagConstraints gbParam) {
		XLabel varLabel = new XLabel(data.getVariable(valueKey).name, XLabel.CENTER, this);
		thePanel.add(varLabel);
		gbl.setConstraints(varLabel, gbVarName);
		gbVarName.gridy ++;
		
//		NormalDistnVariable distnVar = (NormalDistnVariable)data.getVariable(distnKey);
		gbParam.gridx = 1;
		DistnMeanSdView meanView = new DistnMeanSdView(data, this, distnKey, DistnMeanSdView.MEAN,
																																						null, maxMeanParam);
		thePanel.add(meanView);
		gbl.setConstraints(meanView, gbParam);
		
		gbParam.gridx = 2;
		DistnMeanSdView sdView = new DistnMeanSdView(data, this, distnKey, DistnMeanSdView.SD,
																																						null, maxMeanParam);
		thePanel.add(sdView);
		gbl.setConstraints(sdView, gbParam);
		gbParam.gridy ++;
	}
	
	private XPanel summaryPanel(SummaryDataSet summaryData, String valueKey, String distnKey,
																															String value2Key, String distn2Key) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XPanel innerPanel = new InsetPanel(10, 5);
			GridBagLayout gbl = new GridBagLayout();
			innerPanel.setLayout(gbl);
		
			GridBagConstraints gbVarName = new GridBagConstraints();
			gbVarName.anchor = GridBagConstraints.CENTER;
			gbVarName.fill = GridBagConstraints.BOTH;
			gbVarName.gridheight = gbVarName.gridwidth = 1;
			gbVarName.gridx = 0;
			gbVarName.gridy = 1;
			gbVarName.insets = new Insets(2,2,2,2);
			gbVarName.ipadx = gbVarName.ipady = 0;
			gbVarName.weightx = gbVarName.weighty = 0.0;
 
			GridBagConstraints gbParam = new GridBagConstraints();
			gbParam.anchor = GridBagConstraints.CENTER;
			gbParam.fill = GridBagConstraints.NONE;
			gbParam.gridheight = gbParam.gridwidth = 1;
			gbParam.gridy = 0;
			gbParam.insets = new Insets(0,10,0,10);
			gbParam.ipadx = gbParam.ipady = 0;
			gbParam.weightx = gbParam.weighty = 0.0;
			
			addParamHeadings(innerPanel, gbl, gbParam);
			addParamRow(summaryData, valueKey, distnKey, innerPanel, gbl, gbVarName, gbParam);
			if (value2Key != null && distn2Key != null)
				addParamRow(summaryData, value2Key, distn2Key, innerPanel, gbl, gbVarName, gbParam);
		
			innerPanel.lockBackground(kMeanSdBackground);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel rightPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 5, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, summaryPanel(summaryData, "x1", "x1Distn", "x2", "x2Distn"));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 5));
			if (showSum) {
				sumDiffChoice = new XChoice(this);
				sumDiffChoice.addItem(translate("Sum"));
				sumDiffChoice.addItem(translate("Difference"));
				bottomPanel.add(sumDiffChoice);
				
				bottomPanel.add(summaryPanel(summaryData, "sum", "sumTheory", "diff", "diffTheory"));
			}
			else	
				bottomPanel.add(summaryPanel(summaryData, "diff", "diffTheory", null, null));
		
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		return thePanel;
	}
	
	private XPanel distnControlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			String equalMeansString = getParameter(EQUAL_MEANS_PARAM);
			if (equalMeansString.equals("check")) {
				equalMeansCheck = new XCheckbox(translate("Equal means"), this);
				equalMeansCheck.setState(true);
				thePanel.add(equalMeansCheck);
			}
			else {
				meansAlwaysEqual = equalMeansString.equals("true");
				y1View.setEqualMeans(meansAlwaysEqual);
				y2View.setEqualMeans(meansAlwaysEqual);
			}
			
			String equalSdsString = getParameter(EQUAL_SDS_PARAM);
			if (equalSdsString.equals("check")) {
				equalSdsCheck = new XCheckbox(translate("Equal st devns"), this);
				equalSdsCheck.setState(true);
				thePanel.add(equalSdsCheck);
			}
			else {
				sdsAlwaysEqual = equalSdsString.equals("true");
				y1View.setEqualSds(sdsAlwaysEqual);
				y2View.setEqualSds(sdsAlwaysEqual);
			}
		return thePanel;
	}
	
	private XPanel samplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			XPanel panel1 = new XPanel();
			panel1.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			panel1.add(takeSampleButton);
			
		thePanel.add("West", panel1);
			
			XPanel panel2 = new XPanel();
			panel2.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			panel2.add(accumulateCheck);
			
		thePanel.add("Center", panel2);
		return thePanel;
	}
	
	private void doTakeSample() {
		summaryData.takeSample();
	}
	
	private void setScaledDistns(DragMeanSdView theView, String yKey, String y2Key) {
		ScaledVariable yVar = (ScaledVariable)data.getVariable(yKey);
		double newMean = theView.getMean();
		double newSd = theView.getSd();
		yVar.setParam(0, newMean);
		yVar.setParam(1, newSd);
		
		ScaledVariable y2Var = (ScaledVariable)data.getVariable(y2Key);
		if (equalMeansCheck != null && equalMeansCheck.getState() || meansAlwaysEqual)
			y2Var.setParam(0, newMean);
		if (equalSdsCheck != null && equalSdsCheck.getState() || sdsAlwaysEqual)
			y2Var.setParam(1, newSd);
	}
	
	private void setSumDiffLabel(SummaryDataSet summaryData, boolean sumNotDiff) {
		String sumDiffKey = sumNotDiff ? "sum" : "diff";
		sumDiffView.setDistnLabel(new LabelValue(summaryData.getVariable(sumDiffKey).name), kSumDiffLabelColor);
	}
	
	private void changeSumDiff(boolean sumNotDiff) {
		setSumDiffLabel(summaryData, sumNotDiff);
		String sumDiffKey = sumNotDiff ? "sum" : "diff";
		String theoryKey = sumNotDiff ? "sumTheory" : "diffTheory";
		sumDiffView.setVariableKeys(sumDiffKey, theoryKey);
		sumDiffView.repaint();
	}
	
	public void notifyDataChange(DataView theView) {
							//		notification that mean or sd have been dragged
		if (theView == y1View)
			setScaledDistns(y1View, "y1", "y2");
		else
			setScaledDistns(y2View, "y2", "y1");
		setTheoryParameters(data, summaryData);
		summaryData.setSingleSummaryFromData();
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == equalMeansCheck) {
			boolean isEqual = equalMeansCheck.getState();
			y1View.setEqualMeans(isEqual);
			y2View.setEqualMeans(isEqual);
			if (isEqual)
				notifyDataChange(y1View);
			
			return true;
		}
		else if (target == equalSdsCheck) {
			boolean isEqual = equalSdsCheck.getState();
			y1View.setEqualSds(isEqual);
			y2View.setEqualSds(isEqual);
			if (isEqual)
				notifyDataChange(y1View);
			
			return true;
		}
		else if (target == sumDiffChoice) {
			int newChoice = sumDiffChoice.getSelectedIndex();
			if (newChoice != currentSumDiff) {
				currentSumDiff = newChoice;
				changeSumDiff(currentSumDiff == 0);
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