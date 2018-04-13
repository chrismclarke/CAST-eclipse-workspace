package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import inference.*;


public class EstimateSIntervalApplet extends SampleIntervalApplet {
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String K_VALUE_PARAM = "kValue";
	
	private XChoice sampleSizeChoice;
	private XCheckbox sFromSampleCheck;
	
	private int sampleSize[];
	private int currentSampleSizeIndex;
	
	public void setupApplet() {
		readSampleSizes();
		super.setupApplet();
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
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		int decimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		String kString = getParameter(K_VALUE_PARAM);
		double k = Double.parseDouble(kString);
		
			String knownPopnSDString = getParameter(POPN_SD_PARAM);
			MeanCIVariable zCI = new MeanCIVariable(getParameter(MEAN_NAME_PARAM), k,
										Double.parseDouble(knownPopnSDString), "y", decimals);
		summaryData.addVariable("ci", zCI);
		
		MeanCIVariable tCI = new MeanCIVariable(getParameter(MEAN_NAME_PARAM), k,
																						noOfValues - 1, "y", decimals);
		summaryData.addVariable("ci2", tCI);
		
		return summaryData;
	}
	
	protected boolean onlyShowSummaryScale() {
		return true;
	}
	
	protected XPanel sampleControlPanel(DataSet summaryData) {
		XPanel thePanel = new InsetPanel(0, 30, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 30));
		
			XPanel samplePanel = super.sampleControlPanel(summaryData);
			
				XPanel sampleSizePanel = new XPanel();
				sampleSizePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				
					XLabel nLabel = new XLabel(translate("Sample size") + " =", XLabel.RIGHT, this);
					nLabel.setFont(getStandardBoldFont());
				sampleSizePanel.add(nLabel);
				sampleSizePanel.add(sampleSizeChoice);
				
			samplePanel.add(sampleSizePanel);
		
		thePanel.add("Center", samplePanel);
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																													VerticalLayout.VERT_CENTER, 10));
			
				sFromSampleCheck = new XCheckbox(translate("Use sample sd"), this);
			controlPanel.add(sFromSampleCheck);
		
		thePanel.add("South", controlPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleSizeChoice) {
			int newSampleSizeIndex = sampleSizeChoice.getSelectedIndex();
			if (newSampleSizeIndex != currentSampleSizeIndex) {
				currentSampleSizeIndex = newSampleSizeIndex;
				summaryData.changeSampleSize(sampleSize[currentSampleSizeIndex]);
				summaryData.variableChanged("errorDistn");
			}
			return true;
		}
		else if (target == sFromSampleCheck) {
			String ciKey = sFromSampleCheck.getState() ? "ci2" : "ci";
			summaryView.setVariableKey(ciKey);
			ciValueView.setVariableKey(ciKey);
			coverage.setVariableKey(ciKey);
			
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