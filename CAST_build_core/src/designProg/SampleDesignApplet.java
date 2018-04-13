package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import survey.*;


public class SampleDesignApplet extends XApplet {
	static final private String SAMPLING_PARAM = "sampling";
	static final private String MAX_SUMMARY_PARAM = "maxSummary";
	static final private String RESPONSE_PROB_PARAM = "responseProb";
	static final private String CHANGE_PROB_PARAM = "changeProb";
	
	private boolean numNotCat;
	private SampleDesignView theView;
	
	private XButton sampleButton;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(10, 0));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String catName = getParameter(CAT_NAME_PARAM);
		numNotCat = catName == null;
		if (numNotCat)
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		else
			data.addCatVariable("y", catName, getParameter(CAT_VALUES_PARAM),
																				getParameter(CAT_LABELS_PARAM));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		StringTokenizer sampleTok = new StringTokenizer(getParameter(SAMPLING_PARAM));
		int noCovered = Integer.parseInt(sampleTok.nextToken());
		int sampleSize = Integer.parseInt(sampleTok.nextToken());
		long randomSeed = Long.parseLong(sampleTok.nextToken());
		
		NumValue maxSummaryValue = new NumValue(getParameter(MAX_SUMMARY_PARAM));
		String responseProbString = getParameter(RESPONSE_PROB_PARAM);
		String changeProbString = getParameter(CHANGE_PROB_PARAM);
		
		if (numNotCat) {
			SampleNumDesignView numView = new SampleNumDesignView(data, this, "y", noCovered, sampleSize,
																																	maxSummaryValue, randomSeed);
			if (responseProbString != null) {
				StringTokenizer st = new StringTokenizer(responseProbString);
				double p0 = Double.parseDouble(st.nextToken());
				double y0 = Double.parseDouble(st.nextToken());
				double p1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				numView.setResponseProbs(p0, y0, p1, y1);
			}
			if (changeProbString != null) {
				StringTokenizer st = new StringTokenizer(changeProbString);
				double p0 = Double.parseDouble(st.nextToken());
				double y0 = Double.parseDouble(st.nextToken());
				double p1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				double changeMean = Double.parseDouble(st.nextToken());
				double changeSD = Double.parseDouble(st.nextToken());
				numView.setChangeProbs(p0, y0, p1, y1, changeMean, changeSD);
			}
			theView = numView;
		}
		else {
			SampleCatDesignView catView = new SampleCat2DesignView(data, this, "y", noCovered, sampleSize,
							maxSummaryValue, randomSeed);
			if (responseProbString != null) {
				StringTokenizer st = new StringTokenizer(responseProbString);
				double successProb = Double.parseDouble(st.nextToken());
				double failureProb = Double.parseDouble(st.nextToken());
				catView.setResponseProbs(successProb, failureProb);
			}
			if (changeProbString != null) {
				StringTokenizer st = new StringTokenizer(changeProbString);
				double successProb = Double.parseDouble(st.nextToken());
				double failureProb = Double.parseDouble(st.nextToken());
				catView.setChangeProbs(successProb, failureProb);
			}
			theView = catView;
		}
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			theView.takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}