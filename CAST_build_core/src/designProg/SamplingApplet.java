package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import survey.*;


public class SamplingApplet extends XApplet {
	static final protected String SAMPLING_PARAM = "sampling";
	static final protected String POPN_INFO_PARAM = "popnInfo";
	
	protected int rows, cols;
	protected CatSamplingView theView;
	
	private XButton sampleButton;
//	private XChoice popSampChoice;
	private PopSampProportionView sampPropn;
//	private int currentChoice = 0;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(20, 0));
		add("West", displayPanel(data));
		add("Center", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		StringTokenizer popnTok = new StringTokenizer(getParameter(POPN_INFO_PARAM));
		rows = Integer.parseInt(popnTok.nextToken());
		cols = Integer.parseInt(popnTok.nextToken());
		double successProb = Double.parseDouble(popnTok.nextToken());
		long seed = Long.parseLong(popnTok.nextToken());
		
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM));
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		
		Random generator = new Random(seed);
		int popnSize = rows * cols;
		int values[] = new int[popnSize];
		for (int i=0 ; i<popnSize ; i++)
			values[i] = (generator.nextDouble() <= successProb) ? 0 : 1;
		v.setValues(values);
		
		data.addVariable("y", v);
		
		Flags selection = new Flags(popnSize);
		selection.setFlags(0, popnSize);
		data.setSelection(selection);			//		selects all items so that they are not dimmed
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		StringTokenizer sampleTok = new StringTokenizer(getParameter(SAMPLING_PARAM));
		int sampleSize = Integer.parseInt(sampleTok.nextToken());
		long randomSeed = Long.parseLong(sampleTok.nextToken());
		
		theView = new CatSamplingView(data, this, sampleSize, randomSeed, rows, cols);
			theView.setFont(getStandardBoldFont());
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel topControlPanel(DataSet data) {
		XPanel proportionPanel = new XPanel();
		
		Value successVal = data.getCatVariable().getLabel(0);
		proportionPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			XLabel propnLabel = new XLabel(translate("Propn") + "(" + successVal.toString() + ")", XLabel.CENTER, this);
			propnLabel.setFont(getStandardBoldFont());
		proportionPanel.add(propnLabel);
		
		PopSampProportionView popPropn = new PopSampProportionView(data, this, PopSampProportionView.POPN);
		proportionPanel.add(popPropn);
		sampPropn = new PopSampProportionView(data, this, PopSampProportionView.SAMPLE);
		proportionPanel.add(sampPropn);
		sampPropn.setEnabled(false);
		
		return proportionPanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
		mainPanel.add(topControlPanel(data));
		
		XPanel actionPanel = new XPanel();
		actionPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
//		popSampChoice = new XChoice(this);
//		popSampChoice.addItem("Population");
//		popSampChoice.addItem("Sample");
//		popSampChoice.enable(false);
//		actionPanel.add(popSampChoice);
		
		sampleButton = new XButton(translate("Take sample"), this);
		actionPanel.add(sampleButton);
		
		mainPanel.add(actionPanel);
		return mainPanel;
	}
	
//	protected void setPopSamp(int popSampChoice) {
//		if (currentChoice != popSampChoice) {
//			theView.showPopNotSamp(popSampChoice == 0);
//			currentChoice = popSampChoice;
//		}
//	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			theView.takeSample();
//			popSampChoice.enable(true);
//			popSampChoice.select(1);
//			currentChoice = 1;
			if (sampPropn != null)
				sampPropn.setEnabled(true);
			return true;
		}
//		else if (target == popSampChoice) {
//			int newChoice = popSampChoice.getSelectedIndex();
//			setPopSamp(newChoice);
//			return true;
//		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}