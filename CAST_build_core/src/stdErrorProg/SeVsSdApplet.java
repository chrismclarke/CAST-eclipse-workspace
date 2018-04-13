package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import distn.*;
import coreGraphics.*;

import stdError.*;


public class SeVsSdApplet extends XApplet {
	static final private String DATA_INFO_PARAM = "dataAxis";
	static final private String ERROR_INFO_PARAM = "errorAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String MAX_SD_PARAM = "maxSD";
	static final private String SE_DECIMALS_PARAM = "seDecimals";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String MODEL_DISTN_NAME_PARAM = "modelDistnName";
	
	static final protected Color kErrorCrossColor = new Color(0x000099);
	static final protected Color kMeanCrossColor = new Color(0x660000);
	
	static final protected Color kParamStatBackground = new Color(0xEDF2FF);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private NumValue maxSD;
	
	private int sampleSize[];
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex;
	
	private XButton takeSampleButton;
	
//	private int noOfValues;
	private double modelSD;
	
	public void setupApplet() {
		readSampleSizes();
		
		data = getData();
		summaryData = getSummaryData(data);
		
		summaryData.takeSample();
		setErrorDistn(summaryData);
		setEstErrorDistn(data, summaryData);
		
		setLayout(new BorderLayout());
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																									ProportionLayout.TOTAL));
				dataPanel.add(ProportionLayout.LEFT, dataPanel(data));
				dataPanel.add(ProportionLayout.RIGHT, errorDistnPanel(summaryData));
		
		add("Center", dataPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 10));
			
			bottomPanel.add(summaryPanel(data, summaryData));
			bottomPanel.add(sampleControlPanel(summaryData));
		
		add("South", bottomPanel);
	}
	
	private void readSampleSizes() {
		sampleSizeChoice = new XChoice(this);
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		int noOfSampleSizes = st.countTokens();
		sampleSize = new int[noOfSampleSizes];
		currentSampleSizeIndex = 0;
		for (int i=0 ; i<noOfSampleSizes ; i++) {
			String s = st.nextToken();
			if (s.charAt(0) == '*') {
				currentSampleSizeIndex = i;
				s = s.substring(1);
			}
			sampleSize[i] = Integer.parseInt(s);
			sampleSizeChoice.addItem(s);
		}
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_NORMAL_PARAM));
			double modelMean = Double.parseDouble(st.nextToken());
			modelSD = Double.parseDouble(st.nextToken());
			double truncation = Double.parseDouble(st.nextToken());
			
			RandomNormal generator = new RandomNormal(sampleSize[currentSampleSizeIndex],
																									modelMean, modelSD, truncation);
			NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
			NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
			maxSD = new NumValue(getParameter(MAX_SD_PARAM));
			dataDistn.setMean(modelMean);
			dataDistn.setSD(modelSD);
			dataDistn.setDecimals(0, maxSD.decimals);
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			NormalDistnVariable errorDistn = new NormalDistnVariable("Error distn");
			int seDecimals = Integer.parseInt(getParameter(SE_DECIMALS_PARAM));
			errorDistn.setMean(0.0);
			errorDistn.setDecimals(0, seDecimals);
		summaryData.addVariable("errorDistn", errorDistn);
		
			NormalDistnVariable estErrorDistn = new NormalDistnVariable("Est error distn");
			estErrorDistn.setMean(0.0);
			estErrorDistn.setDecimals(0, seDecimals);
		summaryData.addVariable("estErrorDistn", estErrorDistn);
		
		return summaryData;
	}
	
	private void setErrorDistn(SummaryDataSet summaryData) {
		NormalDistnVariable errorDistn = (NormalDistnVariable)summaryData.getVariable("errorDistn");
		double errorSD = modelSD / Math.sqrt(sampleSize[currentSampleSizeIndex]);
		
		errorDistn.setSD(errorSD);
		
		summaryData.variableChanged("errorDistn");
	}
	
	private void setEstErrorDistn(DataSet data, SummaryDataSet summaryData) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		int n = 0;
		double sy = 0.0;
		double syy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			n++;
			sy += y;
			syy += y * y;
		}
		double estModelSD = Math.sqrt((syy - sy * sy / n) / (n - 1));
		
		NormalDistnVariable estErrorDistn = (NormalDistnVariable)summaryData.getVariable("estErrorDistn");
		double estErrorSD = estModelSD / Math.sqrt(sampleSize[currentSampleSizeIndex]);
		
		estErrorDistn.setSD(estErrorSD);
		
		summaryData.variableChanged("estErrorDistn");
	}
	
	private HorizAxis getAxis(DataSet data, String variableKey, String axisInfoParam) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisInfoParam);
		theHorizAxis.readNumLabels(labelInfo);
		CoreVariable v = (CoreVariable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(data, "y", DATA_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedPlusNormalView dataView = new StackedPlusNormalView(data, this, theHorizAxis, "model");
			dataView.setActiveNumVariable("y");
			dataView.lockBackground(Color.white);
			dataView.setDistnLabel(new LabelValue(getParameter(MODEL_DISTN_NAME_PARAM)), Color.lightGray);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel errorDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(summaryData, "errorDistn", ERROR_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			NormalPlusNormalView errorDistnView = new NormalPlusNormalView(summaryData,
																		this, theHorizAxis, "errorDistn", "estErrorDistn");
			errorDistnView.lockBackground(Color.white);
		thePanel.add("Center", errorDistnView);
		
		return thePanel;
	}
	
	private XPanel sampleControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
			takeSampleButton = new XButton(translate("Take sample"), this);
			
		thePanel.add(takeSampleButton);
		
			XPanel sampleSizePanel = new XPanel();
			sampleSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			
				XLabel sampleSizeLabel = new XLabel(translate("Sample size") + ", n =", XLabel.LEFT, this);
				sampleSizeLabel.setFont(getStandardBoldFont());
			sampleSizePanel.add(sampleSizeLabel);
			sampleSizePanel.add(sampleSizeChoice);
			
		thePanel.add(sampleSizePanel);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(12, 5);
		thePanel.setLayout(new GridBagLayout());
		
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.NONE;
			
			Insets topInsets = new Insets(0, 10, 0, 10);
			Insets leftInsets = new Insets(10, 0, 10, 10);
			Insets spaceAround = new Insets(10, 10, 10, 10);
			
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.gridx = 1;
			c.gridy = 0;
			c.insets = topInsets;
//			c.ipady = 10;
//			c.ipadx = 20;
			XLabel sdLabel = new XLabel("St devn of data", XLabel.CENTER, this);
			sdLabel.setFont(getStandardBoldFont());
		thePanel.add(sdLabel, c);
		
			c.gridx = 2;
			XLabel seLabel = new XLabel(translate("Standard error"), XLabel.CENTER, this);
			seLabel.setFont(getStandardBoldFont());
			seLabel.setForeground(Color.red);
		thePanel.add(seLabel, c);
			
			c.anchor = GridBagConstraints.EAST;
			c.weightx = 0.0;
			c.weighty = 1.0;
			c.gridx = 0;
			c.gridy = 1;
			c.insets = leftInsets;
//			c.ipadx = 0;
			XLabel theoryLabel = new XLabel(translate("Theory"), XLabel.LEFT, this);
			theoryLabel.setFont(getStandardBoldFont());
		thePanel.add(theoryLabel, c);
		
			c.gridy = 2;
			XLabel sampleLabel = new XLabel("From sample", XLabel.LEFT, this);
			sampleLabel.setFont(getStandardBoldFont());
		thePanel.add(sampleLabel, c);
			
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.gridx = 1;
			c.gridy = 1;
			c.insets = spaceAround;
//			c.ipadx = 20;
			StDevnValueView sdView = new StDevnValueView(data, this, "model", true, maxSD);
			thePanel.add(sdView, c);
																																
			c.gridy = 2;
			StDevnValueView sdEstView = new StDevnValueView(data, this, "y", false, maxSD);
		thePanel.add(sdEstView, c);
		
			c.gridx = 2;
			c.gridy = 1;
			StdErrorValueView seView = new StdErrorValueView(summaryData, this,
																																	"errorDistn", false);
			thePanel.add(seView, c);
																																
			c.gridy = 2;
			StdErrorValueView seEstView = new StdErrorValueView(summaryData, this,
																																	"estErrorDistn", true);
		thePanel.add(seEstView, c);
		
		thePanel.lockBackground(kParamStatBackground);
		return thePanel;
	}
	
	private void doTakeSample() {
		summaryData.takeSample();
		setEstErrorDistn(data, summaryData);
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (currentSampleSizeIndex != newChoice) {
				currentSampleSizeIndex = newChoice;
				summaryData.changeSampleSize(sampleSize[newChoice]);
				setErrorDistn(summaryData);
				setEstErrorDistn(data, summaryData);
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