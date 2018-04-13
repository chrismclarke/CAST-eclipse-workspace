package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import coreGraphics.*;


public class SampleMean2Applet extends SampleMeanApplet {
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	private XChoice sampleSizeChoice;
	
	private int sampleSize[];
	private int currentSizeIndex = 0;
	
	protected void generateInitialSample(SummaryDataSet summaryData) {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int noOfSizes = st.countTokens();
		sampleSize = new int[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++) {
			String nextSize = st.nextToken();
			boolean isInitialSize = nextSize.startsWith("*");
			if (isInitialSize) {
				nextSize = nextSize.substring(1);
				currentSizeIndex = i;
			}
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		noOfValues = sampleSize[currentSizeIndex];
		setTheoryParameters(summaryData, "theory");
		summaryData.changeSampleSize(sampleSize[currentSizeIndex]);
	}
	
	protected XPanel topControlPanel(DataSet data, SummaryDataSet summaryData, String summaryKey) {
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		XPanel sampleSizePanel = new XPanel();
		sampleSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		sampleSizePanel.add(new XLabel(translate("Sample size") + ":", XLabel.LEFT, this));
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length; i++)
			sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
		sampleSizeChoice.select(currentSizeIndex);
		
		sampleSizePanel.add(sampleSizeChoice);
		topPanel.add(sampleSizePanel);
		
		takeSampleButton = new RepeatingButton(getSampleButtonText(), this);
		topPanel.add(takeSampleButton);
		return topPanel;
	}
	
	public void setTheoryShow(boolean theoryShow) {
		summaryView.setShowDensity(theoryShow ? DataPlusDistnInterface.CONTIN_DISTN : DataPlusDistnInterface.NO_DISTN);
	}
	
	protected void changeSampleSize(int newChoice) {
		currentSizeIndex = newChoice;
		noOfValues = sampleSize[currentSizeIndex];
		setTheoryParameters(summaryData, "theory");
		summaryData.changeSampleSize(noOfValues);
		
	}

	
	private boolean localAction(Object target) {
		if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSizeIndex)
				changeSampleSize(newChoice);
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