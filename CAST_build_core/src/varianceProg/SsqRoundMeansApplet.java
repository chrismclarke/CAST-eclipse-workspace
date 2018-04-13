package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import distn.*;
import models.*;
import coreGraphics.*;
import imageUtils.*;

import variance.*;


public class SsqRoundMeansApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SUMMARY_AXIS_INFO_PARAM = "summaryAxis";
	static final private String RANDOM_PARAM = "random";
	static final private String DATA_DECIMALS_PARAM = "dataDecimals";
	static final private String MAX_SUMMARY_PARAM = "maxSummary";
	static final private String SSQ_NAME_PARAM = "ssqName";
	
	static final private String kSsqAboutMuFile = "xEquals/ssqAboutMu.png";
	static final private String kSsqAboutXbarFile = "xEquals/ssqAboutXbar.png";
	static final private int kSsqImageAscent = 19;
	
	static final private Color kSsqAboutMuColor = new Color(0x006600);
	static final private Color kSsqAboutXbarColor = new Color(0x000099);
	static final private Color kDistnTextColor = new Color(0x555555);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private NumValue maxSummary;
	
	private StackedPlusNormalView dataView;
	private JitterPlusNormalView summaryView;
	private HorizAxis ssqAxis;
	
	private OneValueImageView ssqRoundMuValue, ssqRoundXbarValue;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	private XChoice popSampChoice;
	
	private int noOfValues;
	
	private boolean roundPopnNotSample = true;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new ProportionLayout(0.45, 10, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, topPanel(data, summaryData));
		add(ProportionLayout.BOTTOM, bottomPanel(summaryData));
		
		generateInitialSample(summaryData);
	}
	
	private void generateInitialSample(SummaryDataSet summaryData) {
		setSummaryTheoryParameters(summaryData, "theory");
		summaryData.takeSample();
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			RandomNormal generator = new RandomNormal(getParameter(RANDOM_PARAM));
			int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
			NumVariable yVar = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, decimals);
		data.addVariable("y", yVar);
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_PARAM));
			noOfValues = Integer.parseInt(st.nextToken());
			String meanSDString = st.nextToken() + " " + st.nextToken();
			NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
			dataDistn.setParams(meanSDString);
		data.addVariable("model", dataDistn);
		
			double popnMean = dataDistn.getMean().toDouble();
			String roundMuKey = SimpleComponentVariable.kComponentKey[0];
		data.addVariable(roundMuKey, new SimpleComponentVariable(roundMuKey, data, "y",
													null, popnMean, SimpleComponentVariable.FROM_TARGET, decimals));
		
			String roundXbarKey = SimpleComponentVariable.kComponentKey[2];
		data.addVariable(roundXbarKey, new SimpleComponentVariable(roundXbarKey, data, "y",
													null, popnMean, SimpleComponentVariable.FROM_MEAN, decimals));
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
			
			String roundMuKey = SimpleComponentVariable.kComponentKey[0];
			SsqVariable ssqPopn = new SsqVariable("ssq round mu", sourceData,
																					roundMuKey, maxSummary.decimals, SsqVariable.SSQ);
																															
		summaryData.addVariable(roundMuKey, ssqPopn);
			
			String roundXbarKey = SimpleComponentVariable.kComponentKey[2];
			SsqVariable ssqSample = new SsqVariable("ssq round xBar", sourceData,
																					roundXbarKey, maxSummary.decimals, SsqVariable.SSQ);
																															
		summaryData.addVariable(roundXbarKey, ssqSample);
		
		GammaDistnVariable varianceDistn = new GammaDistnVariable(translate("Chi-squared"));
		summaryData.addVariable("theory", varianceDistn);
		
		return summaryData;
	}
	
	private void setSummaryTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
		GammaDistnVariable varianceDistn = (GammaDistnVariable)summaryData.getVariable(theoryKey);
		
		int df = noOfValues;
		if (!roundPopnNotSample)
			df --;
		NormalDistnVariable model = (NormalDistnVariable)data.getVariable("model");
		double sd = model.getSD().toDouble();
		
		varianceDistn.setShape(df * 0.5);
		varianceDistn.setScale(2.0 * sd * sd);
		
		summaryView.setDistnLabel(new LabelValue("chi-squared (" + df + " df)"), kDistnTextColor);
	}
	
	private XPanel topPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
		thePanel.add("Center", dataPanel(data, "y", "model"));
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																													VerticalLayout.VERT_CENTER, 10));
			
				String roundMuKey = SimpleComponentVariable.kComponentKey[0];
				ssqRoundMuValue = new OneValueImageView(summaryData, roundMuKey,
											this, kSsqAboutMuFile, kSsqImageAscent, maxSummary);
				ssqRoundMuValue.setForeground(kSsqAboutMuColor);
				ssqRoundMuValue.setHighlightSelection(true);
			controlPanel.add(ssqRoundMuValue);
			
				String roundXbarKey = SimpleComponentVariable.kComponentKey[2];
				ssqRoundXbarValue = new OneValueImageView(summaryData, roundXbarKey,
											this, kSsqAboutXbarFile, kSsqImageAscent, maxSummary);
				ssqRoundXbarValue.setForeground(kSsqAboutXbarColor);
				ssqRoundXbarValue.setHighlightSelection(false);
			controlPanel.add(ssqRoundXbarValue);
			
		thePanel.add("East", controlPanel);
		
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			Variable v = (Variable)data.getVariable(variableKey);
			theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
			dataView = new StackedPlusNormalView(data, this, theHorizAxis, modelKey);
			dataView.setActiveNumVariable(variableKey);
			dataView.lockBackground(Color.white);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel bottomPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			String roundMuKey = SimpleComponentVariable.kComponentKey[0];
		thePanel.add("Center", ssqPanel(summaryData, roundMuKey, "theory"));
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				popSampChoice = new	XChoice(this);
				popSampChoice.addItem("Ssq about population mean");
				popSampChoice.addItem("Ssq about sample mean");
			controlPanel.add(popSampChoice);
			
		thePanel.add("North", controlPanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
				
				takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			buttonPanel.add(takeSampleButton);
			
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			buttonPanel.add(accumulateCheck);
		
		thePanel.add("East", buttonPanel);
		
		return thePanel;
	}
	
	private XPanel ssqPanel(SummaryDataSet summaryData, String ssqKey, String theoryKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			ssqAxis = new HorizAxis(this);
			ssqAxis.readNumLabels(getParameter(SUMMARY_AXIS_INFO_PARAM));
			ssqAxis.setAxisName(getParameter(SSQ_NAME_PARAM));
			ssqAxis.setForeground(kSsqAboutMuColor);
		thePanel.add("Bottom", ssqAxis);
		
			summaryView = new JitterPlusNormalView(summaryData, this, ssqAxis, theoryKey, 1.0);
			summaryView.setActiveNumVariable(ssqKey);
			summaryView.setForeground(kSsqAboutMuColor);
			summaryView.lockBackground(Color.white);
		
		thePanel.add("Center", summaryView);
		
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
		else if (target == popSampChoice) {
			boolean newPopNotSamp = (popSampChoice.getSelectedIndex() == 0);
			if (newPopNotSamp != roundPopnNotSample) {
				roundPopnNotSample = newPopNotSamp;
				
				Color newColor = roundPopnNotSample ? kSsqAboutMuColor : kSsqAboutXbarColor;
				ssqAxis.setForeground(newColor);
				ssqAxis.repaint();
				summaryView.setForeground(newColor);
				
				setSummaryTheoryParameters(summaryData, "theory");
				String ssqKey = SimpleComponentVariable.kComponentKey[roundPopnNotSample ? 0 : 2];
				summaryView.setActiveNumVariable(ssqKey);
				summaryView.repaint();
				ssqRoundMuValue.setHighlightSelection(roundPopnNotSample);
				ssqRoundMuValue.repaint();
				ssqRoundXbarValue.setHighlightSelection(!roundPopnNotSample);
				ssqRoundXbarValue.repaint();
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