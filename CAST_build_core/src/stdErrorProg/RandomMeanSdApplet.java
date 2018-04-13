package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;

import corr.*;
import sampling.*;


public class RandomMeanSdApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final protected String RANDOM_PARAM = "random";
	static final protected String DECIMALS_PARAM = "decimals";
	static final protected String UNITS_PARAM = "units";
	static final protected String MAX_MEAN_SD_PARAM = "maxMeanSd";
	static final protected String SAMPLE_BUTTON_PARAM = "sampleButton";
	static final protected String SAMPLE_SIZE_PARAM = "sampleSize";
	
	static final private Color kMeanSdBackground = new Color(0xFFEEBB);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected XButton takeSampleButton;
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	private int sampleSize[];
	
	private String buttonStart, buttonEnd;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(30, 20));
		
		add("Center", dataPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		RandomNormal generator = new RandomNormal(getParameter(RANDOM_PARAM));
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		return new SummaryDataSet(sourceData, "y");
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(50, 0);
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		Variable v = (Variable)data.getVariable("y");
		theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
			MeanSDDotPlotView theView = new MeanSDDotPlotView(data, this, theHorizAxis);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 20));
		
			XPanel meanSdPanel = new XPanel();
			meanSdPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			meanSdPanel.add(meanSdPanel(data));
			
		thePanel.add(meanSdPanel);
		
			XPanel sampPanel = new XPanel();
			sampPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
			
				String sampleSizeString = getParameter(SAMPLE_SIZE_PARAM);
				if (sampleSizeString == null)
					takeSampleButton = new XButton(getParameter(SAMPLE_BUTTON_PARAM), this);
				else {
					StringTokenizer st = new StringTokenizer(sampleSizeString);
					sampleSize = new int[st.countTokens()];
					
					sampleSizeChoice = new XChoice(translate("No of values") + " =", XChoice.HORIZONTAL, this);
					for (int i=0 ; i<sampleSize.length ; i++) {
						sampleSize[i] = Integer.parseInt(st.nextToken());
						sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
					}
					
					sampPanel.add(sampleSizeChoice);
					
					st = new StringTokenizer(getParameter(SAMPLE_BUTTON_PARAM), "*");
					buttonStart = st.nextToken();
					buttonEnd = st.nextToken();
					takeSampleButton = new XButton(buttonStart + sampleSize[0] + buttonEnd, this);
				}
			sampPanel.add(takeSampleButton);
			
		thePanel.add(sampPanel);
		
		return thePanel;
	}
	
	private XPanel meanSdPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(5, 2);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
			NumValue maxMeanSd = new NumValue(getParameter(MAX_MEAN_SD_PARAM));
		
			MeanView meanView = new MeanView(data, "y", MeanView.GENERIC_TEXT_FORMULA, 0, this);
			meanView.setLabel(translate("Mean"));
			meanView.setForeground(Color.blue);
			String unitsString = getParameter(UNITS_PARAM);
			if (unitsString != null)
				meanView.setUnitsString(unitsString);
			meanView.setMaxValue(maxMeanSd);
		thePanel.add(meanView);
		
			StDevnView sdView = new StDevnView(data, "y", MeanView.GENERIC_TEXT_FORMULA, 0, this);
			sdView.setLabel(translate("Standard devn"));
			sdView.setForeground(Color.red);
			sdView.setUnitsString(unitsString);
			sdView.setMaxValue(maxMeanSd);
		thePanel.add(sdView);
		
		thePanel.lockBackground(kMeanSdBackground);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSampleSizeIndex) {
				currentSampleSizeIndex = newChoice;
				summaryData.changeSampleSize(sampleSize[newChoice]);
				takeSampleButton.setText(buttonStart + sampleSize[newChoice] + buttonEnd);
				takeSampleButton.invalidate();
				validate();
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