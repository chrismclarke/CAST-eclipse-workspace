package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import distn.*;
import coreSummaries.*;
import coreVariables.*;
import imageUtils.*;

import stdError.*;


public class MeanErrorDistnApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "dataAxis";
	static final protected String RANDOM_PARAM = "random";
	static final protected String MAX_ESTIMATE_PARAM = "maxEstimate";
	static final private String ERROR_NAME_PARAM = "errorName";
	static final private String ERROR_AXIS_INFO_PARAM = "errorAxis";
	
	static final protected Color kPopnColor = Color.black;
	static final protected Color kSampColor = new Color(0x0000BB);
	static final protected Color kSummaryColor = new Color(0x990000);
	
	static final protected Color kSampleBackgroundColor = new Color(0xE3EAFF);
//	static final protected Color kSampleBackgroundColor = new Color(0xEED6C9);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected NumValue modelMean, modelSD;
	protected int noOfValues;
	
	protected NumValue maxEstimate;
	
	protected DataView popnView;
	protected ParamAndStatView sampleView;
	protected UnknownDotPlotView errorView;
	
	protected FixedValueImageView popnParamValueView;
	private OneValueImageView sampStatValueView;
	protected OneValueImageView errorValueView;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox popnPeekCheck;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 0));
		
		add("West", spacedPanels(titlePanel("Population", kPopnColor, null), null,
										titlePanel("Sample", kSampColor, kSampleBackgroundColor), null,
										titlePanel("Error", kSummaryColor, null)));			//	translate() called by titlePanel()
		
		add("Center", spacedPanels(populationPanel(data, kPopnColor), takeSamplePanel(),
													samplePanel(data, kSampColor, kSampleBackgroundColor),
													controlPanel(summaryData, "estimate", kSampleBackgroundColor),
													errorPanel(summaryData, "error", kSummaryColor)));		//	translate() called by titlePanel()
		
		add("East", spacedPanels(populationParamPanel(data, kPopnColor), null,
												sampleEstimatePanel(summaryData, kSampColor, kSampleBackgroundColor),
												null, errorValuePanel(summaryData, kSummaryColor)));		//	translate() called by titlePanel()
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			RandomContinuous generator = new RandomNormal(getParameter(RANDOM_PARAM));
			NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 9);
		data.addVariable("y", y);
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_PARAM));
			noOfValues = Integer.parseInt(st.nextToken());
			
			NormalDistnVariable popnDistn = new NormalDistnVariable(getParameter(VAR_NAME_PARAM));
			modelMean = new NumValue(st.nextToken());
			modelSD = new NumValue(st.nextToken());
			popnDistn.setParams(modelMean.toString() + " " + modelSD.toString());
			
		data.addVariable("model", popnDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			maxEstimate = new NumValue(getParameter(MAX_ESTIMATE_PARAM));
			MeanVariable meanVar = new MeanVariable(translate("Mean"), "y", maxEstimate.decimals);
		summaryData.addVariable("estimate", meanVar);
			
			ScaledVariable error = new ScaledVariable(getParameter(ERROR_NAME_PARAM), meanVar,
																	"estimate", -modelMean.toDouble(), 1.0, maxEstimate.decimals);
		
		summaryData.addVariable("error", error);
		
			NormalDistnVariable errorDistn = new NormalDistnVariable("Error distn");
			errorDistn.setMean(0.0);
			errorDistn.setSD(modelSD.toDouble() / Math.sqrt(noOfValues));
			
		summaryData.addVariable("errorDistn", errorDistn);
		
			NumVariable unknownVar = new NumVariable("dummy");
			unknownVar.readValues("?");
		summaryData.addVariable("unknown", unknownVar);
		
		return summaryData;
	}
	
	protected XPanel popSampPanels(XPanel popnPanel, XPanel takeSamplePanel, XPanel samplePanel,
																														XPanel popnPeekPanel) {
			XPanel topTwoPanel = new XPanel();
			topTwoPanel.setLayout(new ProportionLayout(0.5, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
				XPanel topPanel;
				if (takeSamplePanel == null)
					topPanel = popnPanel;
				else {
					topPanel = new XPanel();
					topPanel.setLayout(new BorderLayout(0, 0));
					topPanel.add("Center", popnPanel);
					topPanel.add("South", takeSamplePanel);
				}
		
			topTwoPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel middlePanel = new InsetPanel(0, 5);
				middlePanel.setLayout(new BorderLayout(0, 0));
				middlePanel.add("Center", samplePanel);
				
				if (popnPeekPanel != null)
					middlePanel.add("South", popnPeekPanel);
		
				middlePanel.lockBackground(kSampleBackgroundColor);
			topTwoPanel.add(ProportionLayout.BOTTOM, middlePanel);
		return topTwoPanel;
	}
	
	private XPanel spacedPanels(XPanel popnPanel, XPanel takeSamplePanel, XPanel samplePanel,
																							XPanel popnPeekPanel, XPanel errorPanel) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.7, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
		
		thePanel.add(ProportionLayout.TOP, popSampPanels(popnPanel, takeSamplePanel, samplePanel,
																																			popnPeekPanel));
	
		thePanel.add(ProportionLayout.BOTTOM, errorPanel);
		return thePanel;
	}
	
	protected XPanel titlePanel(String title, Color c, Color background) {
		StringTokenizer st = new StringTokenizer(translate(title), "*");
		
		XPanel thePanel = new InsetPanel(0, 0, 10, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_TOP, 0));
			
		while (st.hasMoreTokens()) {
			XLabel titleLabel = new XLabel(st.nextToken(), XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
			titleLabel.setForeground(c);
			thePanel.add(titleLabel);
		}
		
		if (background != null)
			thePanel.lockBackground(background);
		return thePanel;
	}
	
	protected HorizAxis getAxis(DataSet data, String variableKey, String axisInfo) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(axisInfo);
		CoreVariable v = data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	protected XPanel populationPanel(DataSet data, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, "model", getParameter(AXIS_INFO_PARAM));
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			SimpleDistnUnknownView tempView = new SimpleDistnUnknownView(data, this, horizAxis, "model");
			tempView.setDistnKey(null, this);
				String distnString = translate("normal") + " (" + modelMean.toString() + ", " + modelSD.toString() + ")";
			tempView.setLabel(new LabelValue(distnString), Color.gray);
			tempView.setDensityScaling(0.9);
			tempView.lockBackground(Color.white);
			tempView.setForeground(c);
		thePanel.add("Center", tempView);
			popnView = tempView;
		
		return thePanel;
	}
	
	protected FixedValueImageView getParameterView(DataSet data) {
		return new FixedValueImageView("xEquals/muBlack.png", 8, modelMean, Double.NaN, this);
	}
	
	protected XPanel populationParamPanel(DataSet data, Color c) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XLabel paramLabel = new XLabel(translate("Target parameter"), XLabel.CENTER, this);
			paramLabel.setFont(getStandardBoldFont());
			paramLabel.setForeground(c);
		thePanel.add(paramLabel);
		
			popnParamValueView = getParameterView(data);
			popnParamValueView.setForeground(c);
		thePanel.add(popnParamValueView);
		
		return thePanel;
	}
	
	protected XPanel samplePanel(DataSet data, Color c, Color background) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, "y", getParameter(AXIS_INFO_PARAM));
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			sampleView = new ParamAndStatView(data, this, horizAxis, "model", null, summaryData,
																	"estimate", "error", null, translate("Target"), translate("Estimate (sample mean)"));
			sampleView.setActiveNumVariable("y");
			sampleView.setShowDensity (ParamAndStatView.NO_DISTN);
			sampleView.lockBackground(Color.white);
			sampleView.setForeground(c);
		thePanel.add("Center", sampleView);
		
		if (background != null)
			thePanel.lockBackground(background);
		
		return thePanel;
	}
	
	protected OneValueImageView getEstimateView(SummaryDataSet summaryData) {
		return new OneValueImageView(summaryData, "estimate", this, "xEquals/muHatBlue.png", 18, maxEstimate);
	}
	
	protected XPanel sampleEstimatePanel(SummaryDataSet summaryData, Color c, Color background) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XLabel estimateLabel = new XLabel(translate("Estimate from sample"), XLabel.CENTER, this);
			estimateLabel.setFont(getStandardBoldFont());
			estimateLabel.setForeground(c);
		thePanel.add(estimateLabel);
		
			sampStatValueView = getEstimateView(summaryData);
			sampStatValueView.setHighlightSelection(false);
			sampStatValueView.setForeground(c);
		thePanel.add(sampStatValueView);
		
		if (background != null)
			thePanel.lockBackground(background);
		
		return thePanel;
	}
	
	protected XPanel errorPanel(SummaryDataSet summaryData, String errorKey, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(ERROR_AXIS_INFO_PARAM));
			horizAxis.setAxisName(summaryData.getVariable(errorKey).name);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			errorView = new UnknownDotPlotView(summaryData, this, horizAxis);
			errorView.setActiveNumVariable(errorKey);
			errorView.setShowUnknown(true, this);
			errorView.lockBackground(Color.white);
			errorView.setForeground(c);
		thePanel.add("Center", errorView);
		
		return thePanel;
	}
	
	protected OneValueImageView getErrorView(SummaryDataSet summaryData) {
		return new OneValueImageView(summaryData, "unknown", this, "xEquals/errorOfMeanRed.png", 13, maxEstimate);
	}
	
	protected XPanel errorValuePanel(SummaryDataSet summaryData, Color c) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
			XLabel errorLabel = new XLabel(getParameter(ERROR_NAME_PARAM), XLabel.CENTER, this);
			errorLabel.setFont(getStandardBoldFont());
			errorLabel.setForeground(c);
		thePanel.add(errorLabel);
			
			errorValueView = getErrorView(summaryData);
			errorValueView.setHighlightSelection(false);
			errorValueView.setForeground(c);
		thePanel.add(errorValueView);
		
		return thePanel;
	}
	
	protected XPanel takeSamplePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			ArrowCanvas arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
		thePanel.add(arrow);
		
			takeSampleButton = new RepeatingButton(translate("Another sample"), this);
		thePanel.add(takeSampleButton);
		
			arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
		thePanel.add(arrow);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(SummaryDataSet summaryData, String summaryKey, Color background) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			ArrowCanvas arrow1 = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow1.setForeground(kSampColor);
		thePanel.add(arrow1);
		
			popnPeekCheck = new XCheckbox(translate("Peek at population"), this);
		thePanel.add(popnPeekCheck);
		
			ArrowCanvas arrow2 = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow2.setForeground(kSampColor);
		thePanel.add(arrow2);
		
		if (background != null)
			thePanel.lockBackground(background);
		
		return thePanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
	}
	
	protected void showPopulation(boolean showPopn) {
		((SimpleDistnUnknownView)popnView).setDistnKey(showPopn ? "model" : null, this);
		popnView.repaint();
		popnParamValueView.setValue(showPopn ? modelMean.toDouble() : Double.NaN);
		sampleView.setTargetValue(showPopn ? modelMean : null);
		sampleView.repaint();
		errorValueView.setVariableKey(showPopn ? "error" : "unknown");
		errorView.setShowUnknown(!showPopn, this);
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == popnPeekCheck) {
			boolean showPopn = popnPeekCheck.getState();
			summaryData.setAccumulate(showPopn);
			showPopulation(showPopn);
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}