package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import distn.*;
import coreSummaries.*;
import imageUtils.*;

import cat.*;
import stdError.*;


public class ErrorDistnPApplet extends ErrorDistnApplet {
	static final public String DATA_NAME_PARAM = "dataName";

	static final private String RANDOM_BINOMIAL_PARAM = "random";
	static final private String COUNT_NAME_PARAM = "countName";
	static final private String COUNT_INFO_PARAM = "countAxis";
	static final private String PROPN_INFO_PARAM = "propnAxis";
	static final private String ERROR_INFO_PARAM = "errorAxis";
	
	static final private int kPiImageAscent = 13;
	static final private int kPiImageDescent = 4;
	static final private int kPiImageWidth = 26;
	
	static final private Color kTitleColor = new Color(0x990000);
	
	private int noOfValues;
	private NumValue modelProb;
	private MultiHorizAxis countPropnAxis;
	
	private XChoice propnChoice;
	private int currentPropnChoiceIndex = 0;
	
	private int decimals;
	
	public void init() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(10, 10));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																										ProportionLayout.TOTAL));
			
			dataPanel.add(ProportionLayout.LEFT, dataPanel(data));
			dataPanel.add(ProportionLayout.RIGHT, sampleControlPanel(summaryData));
		
		add("North", dataPanel);
		
		add("Center", summaryPanel(summaryData));
		
		add("East", displayControlPanel(summaryData));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		StringTokenizer st = new StringTokenizer(getParameter(RANDOM_BINOMIAL_PARAM));
		noOfValues = Integer.parseInt(st.nextToken());
		modelProb = new NumValue(st.nextToken());
		double prob[] = new double[2];
		prob[0] = modelProb.toDouble();
		prob[1] = 1.0 - modelProb.toDouble();
		
		RandomMultinomial generator = new RandomMultinomial(noOfValues, prob);
		CatVariable y = new CatSampleVariable(getParameter(VAR_NAME_PARAM), generator);
		y.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", y);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			CountVariable count = new CountVariable(getParameter(COUNT_NAME_PARAM), "y");
		
		summaryData.addVariable("count", count);
		
			decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		
			BinomialDistnVariable binomDistn = new BinomialDistnVariable("binom distn");
			binomDistn.setParams(getParameter(RANDOM_BINOMIAL_PARAM));
		summaryData.addVariable("binomDistn", binomDistn);
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
		
			String dataName = getParameter(DATA_NAME_PARAM);
			if (dataName != null) {
				XLabel title = new XLabel(dataName, XLabel.CENTER, this);
				title.setFont(getBigBoldFont());
				title.setForeground(kTitleColor);
				thePanel.add(title);
			}
			
		thePanel.add(new FreqTableView(data, this, "y", FreqTableView.NO_DRAG, decimals));
		
		return thePanel;
	}
	
	protected XPanel sampleControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
				
			XPanel piPanel = new XPanel();
			piPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
				FixedParamImageView piValueView = new FixedParamImageView(this, "xEquals/popnProp.png", kPiImageAscent, kPiImageDescent,
																																				kPiImageWidth, modelProb);
				piValueView.setForeground(Color.blue);
				piValueView.setFont(getStandardBoldFont());
			piPanel.add(piValueView);
			
		thePanel.add("North", piPanel);
		
		thePanel.add("Center", super.sampleControlPanel(summaryData));
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.55, 10, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, summaryDotPanel(summaryData, ErrorDistnPView.SUMMARY_COUNTS));
		thePanel.add(ProportionLayout.BOTTOM, summaryDotPanel(summaryData, ErrorDistnPView.SUMMARY_ERRORS));
		
		return thePanel;
	}
	
	protected XPanel summaryDotPanel(SummaryDataSet summaryData, int displayType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			MultiHorizAxis theHorizAxis = (displayType == ErrorDistnPView.SUMMARY_ERRORS) ? errorAxis()
																													: countAxis(summaryData);
		thePanel.add("Bottom", theHorizAxis);
		
			ErrorDistnPView dataView = new ErrorDistnPView(summaryData, this, theHorizAxis,
																													"count", "binomDistn", displayType);
			dataView.lockBackground(Color.white);
				Color mainColor = (displayType == ErrorDistnPView.SUMMARY_ERRORS) ? kErrorCrossColor
																															: kMeanCrossColor;
			dataView.setForeground(mainColor);
		thePanel.add("Center", dataView);
			
			if (displayType == ErrorDistnPView.SUMMARY_COUNTS) {
				meanView = dataView;
				countPropnAxis = theHorizAxis;
			}
			else
				errorView = dataView;
		
		return thePanel;
	}
	
	private MultiHorizAxis countAxis(SummaryDataSet summaryData) {
		MultiHorizAxis theHorizAxis = new MultiHorizAxis(this, 2);
		String labelInfo = getParameter(COUNT_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		labelInfo = getParameter(PROPN_INFO_PARAM);
		theHorizAxis.readExtraNumLabels(labelInfo);
		setCountPropnAxisName(theHorizAxis, ErrorDistnPView.SUMMARY_COUNTS);
		theHorizAxis.setForeground(kMeanCrossColor);
		return theHorizAxis;
	}
	
	private MultiHorizAxis errorAxis() {
		MultiHorizAxis theHorizAxis = new MultiHorizAxis(this, 2);
		String labelInfo = getParameter(COUNT_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		labelInfo = getParameter(ERROR_INFO_PARAM);
		theHorizAxis.readExtraNumLabels(labelInfo);
		theHorizAxis.setStartAlternate(1);
		theHorizAxis.setAxisName(translate("Error"));
		theHorizAxis.setForeground(kErrorCrossColor);
		return theHorizAxis;
	}
	
	private void setCountPropnAxisName(HorizAxis axis, int viewType) {
		Value successName = ((CatVariable)data.getVariable("y")).getLabel(0);
		String name = (viewType == ErrorDistnPView.SUMMARY_COUNTS) ? "Count of " : "Propn of ";
		axis.setAxisName(name + successName.toString());
	}
	
	protected XPanel displayControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.55, 10, ProportionLayout.VERTICAL,
																														ProportionLayout.TOTAL));
			XPanel countPropnPanel = new XPanel();
			countPropnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
				XLabel displayTypeLabel = new XLabel("Display:", XLabel.CENTER, this);
				displayTypeLabel.setFont(getStandardBoldFont());
			countPropnPanel.add(displayTypeLabel);
				
				propnChoice = new XChoice(this);
				propnChoice.addItem(translate("Counts"));
				propnChoice.addItem(translate("Proportions"));
			countPropnPanel.add(propnChoice);
			
		thePanel.add(ProportionLayout.TOP, countPropnPanel);
		
		thePanel.add(ProportionLayout.BOTTOM, super.displayControlPanel(summaryData));
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == propnChoice) {
			int newPropnChoiceIndex = propnChoice.getSelectedIndex();
			if (newPropnChoiceIndex != currentPropnChoiceIndex) {
				currentPropnChoiceIndex = newPropnChoiceIndex;
				
				int viewType = currentPropnChoiceIndex == 0
														? ErrorDistnPView.SUMMARY_COUNTS : ErrorDistnPView.SUMMARY_PROPNS;
				((ErrorDistnPView)meanView).setViewType(viewType);
				countPropnAxis.setAlternateLabels(currentPropnChoiceIndex == 0 ? 0 : 1);
				setCountPropnAxisName(countPropnAxis, viewType);
				countPropnAxis.repaint();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}