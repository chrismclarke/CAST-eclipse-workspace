package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import survey.*;


public class PictSamplingApplet extends XApplet {
	static final protected String SAMPLING_PARAM = "sampling";
	static final protected String POPN_INFO_PARAM = "popnInfo";
	static final protected String PICTURE_PARAM = "pictureType";
	static final protected String PROPN_DISPLAY_PARAM = "propnDisplay";
	static final private String ALLOW_PROPN_PARAM = "allowPropn";
	
	protected int rows, cols, rowCycle, maxHorizOffset, maxVertOffset;
	protected SamplePictView theView;
	
	private XButton sampleButton;
	private XChoice popSampChoice;
	private PopSampProportionView sampPropn;
	private int currentChoice = 0;
	
	private XCheckbox symbolCheck;
	
	private long popnRandomSeed;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(20, 0));
		add("Center", displayPanel(data));
		add("East", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		StringTokenizer popnTok = new StringTokenizer(getParameter(POPN_INFO_PARAM));
		rows = Integer.parseInt(popnTok.nextToken());
		cols = Integer.parseInt(popnTok.nextToken());
		rowCycle = Integer.parseInt(popnTok.nextToken());
		maxHorizOffset = Integer.parseInt(popnTok.nextToken());
		maxVertOffset = Integer.parseInt(popnTok.nextToken());
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
			long samplingSeed = Long.parseLong(sampleTok.nextToken());
			
			String pictType = getParameter(PICTURE_PARAM);
			if (pictType.equals("box"))
				theView = new SampleBoxView(data, this, sampleSize, samplingSeed, popnRandomSeed,
																					rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			else if (pictType.equals("case"))
				theView = new SampleCaseView(data, this, sampleSize, samplingSeed, popnRandomSeed,
																					rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			else if (pictType.equals("person"))
				theView = new SamplePictView(data, this, sampleSize,
									samplingSeed, popnRandomSeed, rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			else if (pictType.equals("african")) {
				theView = new SamplePictView(data, this, sampleSize,
									samplingSeed, popnRandomSeed, rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
				theView.setPeopleColor(SamplePictView.BLACK);
			}
			else
				theView = new SampleAppleView(data, this, sampleSize,
									samplingSeed, popnRandomSeed, rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			theView.doInitialisation(this);
			theView.clearSample();
			theView.setFont(getBigBoldFont());
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel propnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
			Value successVal = data.getCatVariable().getLabel(0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
			XLabel propnLabel = new XLabel(translate("Propn") + "(" + successVal.toString() + ")", XLabel.CENTER, this);
			propnLabel.setFont(getBigBoldFont());
		thePanel.add(propnLabel);
		
			PopSampProportionView popPropn = new PopSampProportionView(data, this, PopSampProportionView.POPN);
			popPropn.setFont(getBigFont());
		thePanel.add(popPropn);
		
			sampPropn = new PopSampProportionView(data, this, PopSampProportionView.SAMPLE);
			sampPropn.setFont(getBigFont());
			sampPropn.setEnabled(false);
			sampPropn.setHighlight(true);
//			sampPropn.show(false);
		thePanel.add(sampPropn);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 40));
		
			String propnDisplayString = getParameter(PROPN_DISPLAY_PARAM);
		if (propnDisplayString != null && propnDisplayString.equals("yes"))
			mainPanel.add(propnPanel(data));
		
			XPanel actionPanel = new XPanel();
			actionPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
				popSampChoice = new XChoice(this);
				popSampChoice.addItem(translate("Population"));
				popSampChoice.addItem(translate("Sample"));
//				popSampChoice.disable();
			actionPanel.add(popSampChoice);
			
				sampleButton = new XButton(translate("Take sample"), this);
			actionPanel.add(sampleButton);
		
		mainPanel.add(actionPanel);
		
			String allowPropnDisplayString = getParameter(ALLOW_PROPN_PARAM);
			if (allowPropnDisplayString == null || allowPropnDisplayString.equals("true")) {
				symbolCheck = new XCheckbox(translate("Only show") + " " + data.getCatVariable().name, this);
				mainPanel.add(symbolCheck);
			}
		return mainPanel;
	}
	
	protected void setPopSamp(int popSampChoice) {
		if (currentChoice != popSampChoice) {
			theView.showPopNotSamp(popSampChoice == 0);
			currentChoice = popSampChoice;
		}
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
//		popSampChoice.enable();
		popSampChoice.select(1);
		currentChoice = 1;
		if (sampPropn != null)
			sampPropn.setEnabled(true);
		theView.takeSample();
		return true;
	}
	else if (target == popSampChoice) {
		int newChoice = popSampChoice.getSelectedIndex();
		setPopSamp(newChoice);
		return true;
	}
	else if (target == symbolCheck) {
		theView.setDisplayType(symbolCheck.getState() ? SamplePictView.SYMBOL : SamplePictView.PICTURE);
		return true;
	}
	return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}