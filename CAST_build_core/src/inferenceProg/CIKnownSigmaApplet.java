package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import valueList.*;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;
import imageUtils.*;

import inference.*;


public class CIKnownSigmaApplet extends XApplet {
	static final private String DATA_INFO_PARAM = "dataAxis";
	static final private String ERROR_INFO_PARAM = "errorAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String SE_DECIMALS_PARAM = "seDecimals";
	static final private String MAX_PLUSMINUS_PARAM = "maxPlusMinus";
	
//	static final private Color kDarkRed = new Color(0x990000);
	static final private Color kCIBackground = new Color(0xDDDDEE);
	
	static final private int kSDImageAscent = 13;
	static final private int kSDImageDescent = 4;
	static final private int kSDImageWidth = 26;
	
	private RandomNormal generator;
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private RepeatingButton takeSampleButton;
	private XChoice sampleSizeChoice;
	
	private int sampleSize[];
	private int currentSampleSizeIndex;
	
	private NumValue modelSD;
	private int seDecimals;
	
	public void setupApplet() {
		readSampleSizes();
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
				dataPanel.add(ProportionLayout.TOP, dataPanel(data, summaryData));
				dataPanel.add(ProportionLayout.BOTTOM, errorDistnPanel(summaryData));
		
		add("Center", dataPanel);
				
		add("South", ciPanel(summaryData));
	}
	
	private void readSampleSizes() {
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		sampleSize = new int[st.countTokens()];
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length ; i++) {
			String nextSize = st.nextToken();
			if (nextSize.indexOf("*") == 0) {
				nextSize = nextSize.substring(1);
				currentSampleSizeIndex = i;
			}
			sampleSizeChoice.addItem(nextSize);
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		sampleSizeChoice.select(currentSampleSizeIndex);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		generator = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM));
		generator.setSampleSize(sampleSize[currentSampleSizeIndex]);
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			NormalDistnVariable errorDistn = new NormalDistnVariable(translate("Distn of errors"));
			seDecimals = Integer.parseInt(getParameter(SE_DECIMALS_PARAM));
			errorDistn.setDecimals(seDecimals);
		summaryData.addVariable("errorDistn", errorDistn);
			readModelSD();
			fixErrorSD(summaryData);
		
		summaryData.addVariable("mean", new MeanVariable("mean", "y", seDecimals));
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_NORMAL_PARAM));
				st.nextToken();		//	count
				st.nextToken();		//	mean
				double popnSd = Double.parseDouble(st.nextToken());
		summaryData.addVariable("ci", new MeanCIVariable("95% confidence interval", 1.96,
																																	popnSd, "y", seDecimals));
		
		return summaryData;
	}
	
	private void readModelSD() {
		StringTokenizer st = new StringTokenizer(getParameter(RANDOM_NORMAL_PARAM));
		st.nextToken();
		st.nextToken();
		modelSD = new NumValue(st.nextToken());
	}
	
	private void fixErrorSD(SummaryDataSet summaryData) {
		NormalDistnVariable errorDistn = (NormalDistnVariable)summaryData.getVariable("errorDistn");
		double errorSD = modelSD.toDouble() / Math.sqrt(sampleSize[currentSampleSizeIndex]);
		errorDistn.setSD(errorSD);
		errorDistn.setMinSelection(-1.96 * errorSD);
		errorDistn.setMaxSelection(1.96 * errorSD);
	}
	
	private HorizAxis getAxis(DataSet data, String variableKey, String axisInfoParam) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisInfoParam);
		theHorizAxis.readNumLabels(labelInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	private XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("Center", dotPlotPanel(data));
		thePanel.add("East", samplingPanel(data, summaryData));
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(data, "y", DATA_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			SampleAndMeanView dataView = new SampleAndMeanView(data, this, theHorizAxis, null,
																					seDecimals, SampleAndMeanView.DRAW_MEAN_VALUE);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel samplingPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(new FixedParamImageView(this, "xEquals/popnSD2.png", kSDImageAscent, kSDImageDescent,
																																			kSDImageWidth, modelSD));
		
//			OneValueImageView meanView = new OneValueImageView(summaryData, "mean", null,
//							this, "xEquals/sampMeanRed.png", MeanSDImages.kParamAscent, DataView.BUFFERED);
//			meanView.setHighlightSelection(false);
//			meanView.setForeground(kDarkRed);
//		thePanel.add(meanView);
		
			XPanel sampleSizePanel = new XPanel();
			sampleSizePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			
				XLabel nLabel = new XLabel("n =", XLabel.RIGHT, this);
				nLabel.setFont(getStandardBoldFont());
			sampleSizePanel.add(nLabel);
			sampleSizePanel.add(sampleSizeChoice);
			
		thePanel.add(sampleSizePanel);
		
			takeSampleButton = new RepeatingButton(translate("Another sample"), this);
			
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	private XPanel errorDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("Center", errorDensityPanel(summaryData));
		thePanel.add("West", sePanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel errorDensityPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(ERROR_INFO_PARAM));		
			CoreVariable errorDistn = summaryData.getVariable("errorDistn");
			theHorizAxis.setAxisName(errorDistn.name);
		thePanel.add("Bottom", theHorizAxis);
		
			VertAxis theProbAxis = new VertAxis(this);
			String labelInfo = getParameter(PROB_AXIS_PARAM);
			theProbAxis.readNumLabels(labelInfo);
			theProbAxis.setShowUnlabelledAxis(false);
		thePanel.add("Left", theProbAxis);
		
			DistnDensityView errorDistnView = new DistnDensityView(summaryData, this, theHorizAxis,
													theProbAxis, "errorDistn", DistnDensityView.NO_SHOW_MEANSD, DistnDensityView.NO_DRAG);
			errorDistnView.lockBackground(Color.white);
		thePanel.add("Center", errorDistnView);
		
		return thePanel;
	}
	
	private XPanel sePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		thePanel.add(new SigmaKValueView(summaryData, this, "errorDistn",
										SigmaKValueView.SE_MEAN, new NumValue(getParameter(MAX_PLUSMINUS_PARAM))));
		
		return thePanel;
	}
	
	private XPanel ciPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(20, 6);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
				
				SigmaKValueView ciLevelView = new SigmaKValueView(summaryData, this, "errorDistn",
										SigmaKValueView.CONF_LEVEL, new NumValue(getParameter(MAX_PLUSMINUS_PARAM)));
				ciLevelView.setFont(getBigFont());
			innerPanel.add(ciLevelView);
				
				XPanel mainCIPanel = new XPanel();
				mainCIPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
					
					SigmaKValueView ciPlusMinusView = new SigmaKValueView(summaryData, this, "errorDistn",
										SigmaKValueView.CONF_INTERVAL, new NumValue(getParameter(MAX_PLUSMINUS_PARAM)));
					ciPlusMinusView.setFont(getBigFont());
				mainCIPanel.add(ciPlusMinusView);
					
					OneValueView ciValueView = new OneValueView(summaryData, "ci", this);
					ciValueView.setLabel(" = ");
					ciValueView.setFont(getBigFont());
				mainCIPanel.add(ciValueView);
				
			innerPanel.add(mainCIPanel);
			
			innerPanel.lockBackground(kCIBackground);
		thePanel.add(innerPanel);
			
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newSampleSizeIndex = sampleSizeChoice.getSelectedIndex();
			if (newSampleSizeIndex != currentSampleSizeIndex) {
				currentSampleSizeIndex = newSampleSizeIndex;
				fixErrorSD(summaryData);
				summaryData.changeSampleSize(sampleSize[currentSampleSizeIndex]);
				summaryData.variableChanged("errorDistn");
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