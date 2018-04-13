package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import sampling.*;
import survey.*;


public class SimulateSampleApplet extends XApplet implements RandomDigitProgInterface {
	static final protected String RANDOM_SEED_PARAM = "random";
	static final protected String POPN_INFO_PARAM = "popnInfo";
//	static final protected String ALLOW_FASTER_PARAM = "allowFaster";
	
	static final private int[] kMillisecPerFrame = {50, 47, 51, 49};
	static final private int[] kFastMillisecPerFrame = {5, 4, 5, 4};
	
	static final protected Color kPaleRed = new Color(0xFF66FF);
	static final protected Color kPaleOrange = new Color(0xFF9999);
	static final protected Color kPaleGreen = new Color(0xCCFFCC);
	
	protected DataSet data;
	
	protected RandomDigitsPanel theDigits;
	protected SamplePictView theView;
	
	protected XButton generateButton, resetButton;
	
	private XCheckbox fasterCheck;
	
	protected SimpleTextArea message;
	
	protected int rows, cols, rowCycle, maxHorizOffset, maxVertOffset;
	protected long popnSeed;
	
	private int[] millisecPerFrame = kMillisecPerFrame;
	
	public void setupApplet() {
		DigitImages.loadDigits(this);
		
		data = readData();
		
		setLayout(new BorderLayout(20, 8));
			
		add("Center", displayPanel(data));
			
		add("East", controlPanel());
		
			message = new SimpleTextArea(1);
			message.setBorders(6, 5);
			message.lockBackground(Color.white);
			message.setFont(getBigBoldFont());
		add("South", message);
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
		popnSeed = seed + 13749363L;
		
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM));
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		
		Random generator = new Random(seed);
		int popnSize = rows * cols;
		int values[] = new int[popnSize];
		for (int i=0 ; i<popnSize ; i++)
			values[i] = (generator.nextDouble() <= successProb) ? 0 : 1;
		v.setValues(values);
		
		data.addVariable("y", v);
		
		return data;
	}

//---------------------------------------------------------------------

	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			theView = new SamplePictView(data, this, 1, popnSeed, 0, rows, cols, rowCycle, maxHorizOffset,
																													maxVertOffset);
			theView.setDrawIndices(true);
			theView.showPopNotSamp(false);
			theView.doInitialisation(this);
		
		thePanel.add("Center", theView);
		return thePanel;
	}

	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 30));
		
			resetButton = new XButton(translate("Clear sample"), this);
		thePanel.add(resetButton);
		
			XPanel generatePanel = new XPanel();
			generatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
				generateButton = new XButton(translate("Random index"), this);
			generatePanel.add(generateButton);
		
				theDigits = new RandomDigitsPanel(this, this, RandomDigitsPanel.NO_DECIMALS, getParameter(RANDOM_SEED_PARAM));
			generatePanel.add(theDigits);
			
		thePanel.add(generatePanel);
		
//		String fasterString = getParameter(ALLOW_FASTER_PARAM);
//		if (fasterString != null && fasterString.equals("true")) {
			fasterCheck = new XCheckbox(translate("Faster"), this);
			thePanel.add(fasterCheck);
//		}
		
		return thePanel;
	}

//----------------------------------------------------------------
	
	public void noteNewValue(RandomDigitsPanel valuePanel) {
		int newIndex = valuePanel.getValue();
		if (newIndex >= rows * cols) {
			message.lockBackground(kPaleRed);
			message.setText(translate("Ignored: no item has index") + " " + newIndex);
		}
		else {
			boolean successful = theView.addToSample(newIndex);
			if (successful) {
				message.lockBackground(kPaleGreen);
				StringTokenizer st= new StringTokenizer(translate("Item * has been selected"), "*");
				message.setText(st.nextToken() + newIndex + st.nextToken());
			}
			else {
				message.lockBackground(kPaleOrange);
				StringTokenizer st= new StringTokenizer(translate("Ignored: item * was previously selected"), "*");
				message.setText(st.nextToken() + newIndex + st.nextToken());
			}
		}
		generateButton.enable();
	}
	
	public void noteClearedValue() {
		message.lockBackground(Color.white);
		message.setText("");
		theView.clearHighlight();
		generateButton.disable();
	}
	
	protected void clearSample() {
		message.lockBackground(Color.white);
		message.setText("");
		
		theView.clearSample();
		generateButton.enable();
	}

	
	private boolean localAction(Object target) {
		if (target == generateButton) {
			theDigits.animateNextDigits(millisecPerFrame);
			return true;
		}
		else if (target == resetButton) {
			clearSample();
			return true;
		}
		else if (target == fasterCheck) {
			millisecPerFrame = fasterCheck.getState() ? kFastMillisecPerFrame : kMillisecPerFrame;
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}