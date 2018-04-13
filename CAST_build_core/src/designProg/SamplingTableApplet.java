package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import survey.*;


public class SamplingTableApplet extends XApplet {
	static final private String SAMPLING_PARAM = "sampling";
	
	private SamplingTableView theView;
	
	private XButton sampleButton;
	private XChoice sampleSizeChoice;
	private int sampleSize[];
	private int currentSampleIndex = 0;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(20, 0));
		add("Center", displayPanel(data));
		add("East", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		v.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("y", v);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		StringTokenizer sampleTok = new StringTokenizer(getParameter(SAMPLING_PARAM));
		int noOfSampleSizes = sampleTok.countTokens() - 1;
		sampleSize = new int[noOfSampleSizes];
		for (int i=0 ; i<noOfSampleSizes ; i++)
			sampleSize[i] = Integer.parseInt(sampleTok.nextToken());
		long randomSeed = Long.parseLong(sampleTok.nextToken());
		currentSampleIndex = noOfSampleSizes / 2;
		
		theView = new SamplingTableView(data, this, sampleSize[currentSampleIndex], randomSeed);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
		XPanel sampleSizePanel = new XPanel();
		
		sampleSizePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		sampleSizePanel.add(new XLabel(translate("Sample size") + ":", XLabel.CENTER, this));
		
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length ; i++)
			sampleSizeChoice.addItem(Integer.toString(sampleSize[i]));
		sampleSizeChoice.select(currentSampleIndex);
		sampleSizePanel.add(sampleSizeChoice);
		
		mainPanel.add(sampleSizePanel);
		
		sampleButton = new XButton(translate("Take sample"), this);
		mainPanel.add(sampleButton);
		
		return mainPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			theView.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (currentSampleIndex != newChoice) {
				theView.setSampleSize(sampleSize[newChoice]);;
				currentSampleIndex = newChoice;
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