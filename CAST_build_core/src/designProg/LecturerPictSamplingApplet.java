package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;

import survey.*;


public class LecturerPictSamplingApplet extends XApplet {
	static final protected String SAMPLING_PARAM = "sampling";
	static final protected String POPN_INFO_PARAM = "popnInfo";
	static final protected String PICTURE_PARAM = "pictureType";
	static final private String VALUE_DISTN_PARAM = "valueDistn";
	static final private String MAX_MEAN_PARAM = "maxMean";
	
	private int sampleSize;
	private long samplingSeed, pictureSeed;
	
	protected int rows, cols, rowCycle, maxHorizOffset, maxVertOffset;
	protected SamplePictView theView;
	
	private XButton sampleButton;
	private XChoice popSampChoice;
	private PopSampProportionView sampPropn, sampMean, errorPropn, errorMean;
	private int currentChoice = 0;
	
	private XChoice displayTypeChoice;
	private int currentDisplayType = 0;
	
	private XPanel estimatePanel, propnEstimatePanel, meanEstimatePanel;
	private CardLayout estimatePanelLayout, propnEstimatePanelLayout,
																													meanEstimatePanelLayout;
	
	private XCheckbox propnCheck, meanCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 20));
			leftPanel.add("Center", displayPanel(data));
			leftPanel.add("South", controlPanel(data));
			
		add("Center", leftPanel);
			
		add("East", summaryPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			StringTokenizer sampleTok = new StringTokenizer(getParameter(SAMPLING_PARAM));
			sampleSize = Integer.parseInt(sampleTok.nextToken());
			samplingSeed = Long.parseLong(sampleTok.nextToken());
			pictureSeed = samplingSeed + 187365292l;
			long catPopnSeed = samplingSeed + 783947621l;
			long numPopnSeed = samplingSeed + 6300288367l;
		
		StringTokenizer popnTok = new StringTokenizer(getParameter(POPN_INFO_PARAM));
		rows = Integer.parseInt(popnTok.nextToken());
		cols = Integer.parseInt(popnTok.nextToken());
		rowCycle = Integer.parseInt(popnTok.nextToken());
		maxHorizOffset = Integer.parseInt(popnTok.nextToken());
		maxVertOffset = Integer.parseInt(popnTok.nextToken());
		double successProb = Double.parseDouble(popnTok.nextToken());
		
			CatVariable xVar = new CatVariable(getParameter(CAT_NAME_PARAM));
			xVar.readLabels(getParameter(CAT_LABELS_PARAM));
			
			Random generator = new Random(catPopnSeed);
			int popnSize = rows * cols;
			int values[] = new int[popnSize];
			for (int i=0 ; i<popnSize ; i++)
				values[i] = (generator.nextDouble() <= successProb) ? 0 : 1;
			xVar.setValues(values);
		
		data.addVariable("x", xVar);
		
			NumVariable yVar = new NumVariable(getParameter(VAR_NAME_PARAM));
			StringTokenizer st = new StringTokenizer(getParameter(VALUE_DISTN_PARAM));
			NumValue mean = new NumValue(st.nextToken());
			NumValue sd = new NumValue(st.nextToken());
			RandomNormal normGen = new RandomNormal(popnSize, mean.toDouble(), sd.toDouble(), 3.0);
			normGen.setSeed(numPopnSeed);
			double y[] = normGen.generate();
			int decimals = Math.max(mean.decimals, sd.decimals);
			double factor = Math.pow(10.0, decimals);
			for (int i=0 ; i<popnSize ; i++)
				y[i] = Math.rint(y[i] * factor) / factor;
			yVar.setValues(normGen.generate());
			yVar.setDecimals(decimals);
			
		data.addVariable("y", yVar);
		
		Flags selection = new Flags(popnSize);
		selection.setFlags(0, popnSize);
		data.setSelection(selection);			//		selects all items so that they are not dimmed
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			String pictType = getParameter(PICTURE_PARAM);
			if (pictType.equals("box"))
				theView = new SampleBoxView(data, this, sampleSize, samplingSeed, pictureSeed,
																					rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			else if (pictType.equals("case"))
				theView = new SampleCaseView(data, this, sampleSize, samplingSeed, pictureSeed,
																					rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			else if (pictType.equals("person"))
				theView = new SamplePictView(data, this, sampleSize,
									samplingSeed, pictureSeed, rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			else if (pictType.equals("african")) {
				theView = new SamplePictView(data, this, sampleSize,
									samplingSeed, pictureSeed, rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
				theView.setPeopleColor(SamplePictView.BLACK);
			}
			else
				theView = new SampleAppleView(data, this, sampleSize,
									samplingSeed, pictureSeed, rows, cols, rowCycle, maxHorizOffset, maxVertOffset);
			theView.doInitialisation(this);
			theView.clearSample();
			theView.setFont(getBigBoldFont());
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel propnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
			Value successVal = data.getCatVariable().getLabel(0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 30));
		
			String propnString = translate("Propn") + "(" + successVal.toString() + ")";
			propnCheck = new XCheckbox(propnString, this);
		
		thePanel.add(propnCheck);
			
			propnEstimatePanel = new XPanel();
			propnEstimatePanelLayout = new CardLayout();
			propnEstimatePanel.setLayout(propnEstimatePanelLayout);
			
			propnEstimatePanel.add("blank", new XPanel());
			
				XPanel valuePanel = new XPanel();
				valuePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 30));
				
					XPanel propnPanel = new XPanel();
					propnPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
					
						PopSampProportionView popPropn = new PopSampProportionView(data, this, PopSampProportionView.POPN);
						popPropn.setFont(getBigFont());
					propnPanel.add(popPropn);
					
						sampPropn = new PopSampProportionView(data, this, PopSampProportionView.SAMPLE);
						sampPropn.setFont(getBigFont());
						sampPropn.setEnabled(false);
					propnPanel.add(sampPropn);
				
				valuePanel.add(propnPanel);
				
					errorPropn = new PopSampProportionView(data, this, PopSampProportionView.ERROR);
					errorPropn.setFont(getBigBoldFont());
					errorPropn.setEnabled(false);
					errorPropn.setHighlight(true);
					errorPropn.setForeground(Color.red);
				valuePanel.add(errorPropn);
				
			propnEstimatePanel.add("propn", valuePanel);
			
			propnEstimatePanelLayout.show(propnEstimatePanel, "blank");
		
		thePanel.add(propnEstimatePanel);
		
		return thePanel;
	}
	
	private XPanel meanPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 30));
		
			String meanString = translate("Mean");
			meanCheck = new XCheckbox(meanString, this);
		
		thePanel.add(meanCheck);
			
			meanEstimatePanel = new XPanel();
			meanEstimatePanelLayout = new CardLayout();
			meanEstimatePanel.setLayout(meanEstimatePanelLayout);
			
			meanEstimatePanel.add("blank", new XPanel());
			
				XPanel valuePanel = new XPanel();
				valuePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 30));
				
					XPanel meanPanel = new XPanel();
					meanPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
					
						NumValue maxMean = new NumValue(getParameter(MAX_MEAN_PARAM));
						
						PopSampProportionView popPropn = new PopSampMeanView(data, this, PopSampProportionView.POPN, maxMean);
						popPropn.setFont(getBigFont());
					meanPanel.add(popPropn);
					
						sampMean = new PopSampMeanView(data, this, PopSampProportionView.SAMPLE, maxMean);
						sampMean.setFont(getBigFont());
						sampMean.setEnabled(false);
					meanPanel.add(sampMean);
				
				valuePanel.add(meanPanel);
				
					errorMean = new PopSampMeanView(data, this, PopSampProportionView.ERROR, maxMean);
					errorMean.setFont(getBigBoldFont());
					errorMean.setEnabled(false);
					errorMean.setHighlight(true);
					errorMean.setForeground(Color.red);
				valuePanel.add(errorMean);
				
			meanEstimatePanel.add("mean", valuePanel);
			
			meanEstimatePanelLayout.show(meanEstimatePanel, "blank");
		
		thePanel.add(meanEstimatePanel);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 60));
		
			displayTypeChoice = new XChoice(translate("Display") + ":" , XChoice.VERTICAL_LEFT, this);
			displayTypeChoice.addItem(translate("Pictures"));
			displayTypeChoice.addItem(data.getVariable("x").name);
			displayTypeChoice.addItem(data.getVariable("y").name);
			
		thePanel.add(displayTypeChoice);
		
			estimatePanel = new XPanel();
			estimatePanelLayout = new CardLayout();
			estimatePanel.setLayout(estimatePanelLayout);
			
			estimatePanel.add("blank", new XPanel());
			estimatePanel.add("propn", propnPanel(data));
			estimatePanel.add("mean", meanPanel(data));
			
			estimatePanelLayout.show(estimatePanel, "blank");
		
		thePanel.add(estimatePanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
		
			popSampChoice = new XChoice(this);
			popSampChoice.addItem(translate("Population"));
			popSampChoice.addItem(translate("Sample"));
//				popSampChoice.disable();
		thePanel.add(popSampChoice);
		
			sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected void setPopSamp(int popSampChoice) {
		if (currentChoice != popSampChoice) {
			theView.showPopNotSamp(popSampChoice == 0);
			currentChoice = popSampChoice;
		}
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			popSampChoice.select(1);
			currentChoice = 1;
			if (sampPropn != null)
				sampPropn.setEnabled(true);
			if (errorPropn != null)
				errorPropn.setEnabled(true);
			if (sampMean != null)
				sampMean.setEnabled(true);
			if (errorMean != null)
				errorMean.setEnabled(true);
			theView.takeSample();
			return true;
		}
		else if (target == popSampChoice) {
			int newChoice = popSampChoice.getSelectedIndex();
			setPopSamp(newChoice);
			return true;
		}
		else if (target == displayTypeChoice) {
			int newChoice = displayTypeChoice.getSelectedIndex();
			if (currentDisplayType != newChoice) {
				currentDisplayType = newChoice;
				int displayType = (newChoice == 0) ? SamplePictView.PICTURE : (newChoice == 1) ? SamplePictView.SYMBOL : SamplePictView.VALUE;
				theView.setDisplayType(displayType);
				
				String displayString = (newChoice == 0) ? "blank" : (newChoice == 1) ? "propn" : "mean";
				estimatePanelLayout.show(estimatePanel, displayString);
			}
			return true;
		}
		else if (target == propnCheck) {
			String displayString = propnCheck.getState() ? "propn" : "blank";
			propnEstimatePanelLayout.show(propnEstimatePanel, displayString);
		}
		else if (target == meanCheck) {
			String displayString = meanCheck.getState() ? "mean" : "blank";
			meanEstimatePanelLayout.show(meanEstimatePanel, displayString);
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}