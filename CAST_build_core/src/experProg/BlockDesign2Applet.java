package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.OneValueView;
import random.RandomNormal;

import exper.*;


public class BlockDesign2Applet extends XApplet {
	static final private String TREAT_EFFECT_PARAM = "treatEffect";
	static final protected String FIELD_PICT_PARAM = "fieldPict";
	static final protected String TREAT_PICT_PARAM = "treatPict";
	static final private String RESPONSE_NAME_PARAM = "responseName";
	static final private String MAX_RESPONSE_PARAM = "maxResponse";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String RANDOMISATION_PARAM = "randomisation";
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	
	static final private String BLOCK_PARAM = "blockInfo";
	static final private String BLOCK_EFFECT_PARAM = "blockEffect";
	static final private String BLOCK_NAME_PARAM = "blockName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabel";
	static final private String NO_BLOCK_IMAGE_PARAM = "noBlockPict";
	static final private String BLOCK_IMAGES_PARAM = "blockPicts";
	static final private String TREAT_IMAGES_PARAM = "treatPicts";
	
	static final private int RANDOMISED = 0;
	static final private int BLOCKED = 1;
	
	private DataSet data;
	
	private double treatmentEffect[];
	private double blockEffect[];
	private int plotsPerTreat[];
	
	private RandomNormal generator;
	private Random randomisator;
	
	private int rows, cols, rowsPerBlock;
	private int design = RANDOMISED;
	
	private XButton sampleButton;
	private XChoice designChoice;
	private XCheckbox showBlocksCheck;
	
	private BlockPlotsView designView;
	private BlockTreatDotView responseView;
	
	private NumValue maxResponse;
	
	public void setupApplet() {
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		
		readEffects();
		
		data = readData();
		
		initRandomisation();
		
		setLayout(new BorderLayout(10, 0));
		
		add("West", designPanel(data));
		add("Center", dataDisplayPanel(data));
		add("East", controlPanel(data));
	}
	
	private void readEffects() {
		StringTokenizer st = new StringTokenizer(getParameter(TREAT_EFFECT_PARAM));
		treatmentEffect = new double[st.countTokens()];
		for (int i=0 ; i<treatmentEffect.length ; i++)
			treatmentEffect[i] = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(getParameter(BLOCK_EFFECT_PARAM));
		blockEffect = new double[st.countTokens()];
		for (int i=0 ; i<blockEffect.length ; i++)
			blockEffect[i] = Double.parseDouble(st.nextToken());
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			CatVariable treatmentVar = new CatVariable(getParameter(CAT_NAME_PARAM));
			treatmentVar.readLabels(getParameter(CAT_LABELS_PARAM));
			treatmentVar.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("treatment", treatmentVar);
		
			NumVariable response = new NumVariable(getParameter(RESPONSE_NAME_PARAM));
			double val[] = new double[treatmentVar.noOfValues()];
			response.setValues(val);
			maxResponse = new NumValue(getParameter(MAX_RESPONSE_PARAM));
			response.setDecimals(maxResponse.decimals);
		data.addVariable("response", response);
		
		data.addCatVariable("block", getParameter(BLOCK_NAME_PARAM),
								getParameter(BLOCK_VALUES_PARAM), getParameter(BLOCK_LABELS_PARAM));
		
		return data;
	}
	
	private void initRandomisation() {
		CatVariable treat = (CatVariable)data.getVariable("treatment");
		int noOfTreats = treat.noOfCategories();
		StringTokenizer st2 = new StringTokenizer(getParameter(RANDOMISATION_PARAM));
		plotsPerTreat = new int[noOfTreats];
		for (int i=0 ; i<noOfTreats ; i++)
			plotsPerTreat[i] = Integer.parseInt(st2.nextToken());
		
		long seed = Long.parseLong(st2.nextToken());
		randomisator = new Random(seed);
	}
	
	private XPanel designPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
		
		String blockInfo = getParameter(BLOCK_PARAM);
		StringTokenizer st = new StringTokenizer(blockInfo);
		rows = Integer.parseInt(st.nextToken());
		cols = Integer.parseInt(st.nextToken());
		rowsPerBlock = Integer.parseInt(st.nextToken());
		long permutationSeed = Long.parseLong(st.nextToken());
		
		designView = new BlockPlotsView(data, this,
							getParameter(NO_BLOCK_IMAGE_PARAM), getParameter(BLOCK_IMAGES_PARAM),
							getParameter(TREAT_IMAGES_PARAM), "block", "treatment", "response",
							rows, cols, rowsPerBlock, permutationSeed);
		designView.setShowBlocks(false);
		designView.lockBackground(Color.white);
		thePanel.add(designView);
		
		thePanel.add(new OneValueView(data, "response", this, maxResponse));
		return thePanel;
	}
	
	private XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis responseAxis = new VertAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			responseAxis.readNumLabels(labelInfo);
		thePanel.add("Left", responseAxis);
		
			CatVariable treat = (CatVariable)data.getVariable("treatment");
			HorizAxis treatAxis = new HorizAxis(this);
			treatAxis.setCatLabels(treat);
			treatAxis.setAxisName(treat.name);
		thePanel.add("Bottom", treatAxis);
		
			responseView = new BlockTreatDotView(data, this, responseAxis, treatAxis, "response", "treatment", "block");
			responseView.lockBackground(Color.white);
		thePanel.add("Center", responseView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																	VerticalLayout.VERT_CENTER, 10));
		
			sampleButton = new XButton(translate("Conduct experiment"), this);
		thePanel.add(sampleButton);
		
			CatVariable block = (CatVariable)data.getVariable("block");
			showBlocksCheck = new XCheckbox("Show " + block.name, this);
		thePanel.add(showBlocksCheck);
		
			designChoice = new XChoice(this);
			designChoice.addItem("Randomised design");
			designChoice.addItem("Block design");
			designChoice.select(0);
		thePanel.add(designChoice);
		
		return thePanel;
	}
	
	private void generateResponses() {
		switch (design) {
			case RANDOMISED:
				randomiseTreatments(0, plotsPerTreat);
				break;
			case BLOCKED:
				int plotsPerTreatInBlock[] = new int[plotsPerTreat.length];
				int noOfBlocks = blockEffect.length;
				int plotsPerBlock = cols * rowsPerBlock;
				int startIndex = 0;
				for (int i=0 ; i<noOfBlocks ; i++) {
					for (int j=0 ; j<plotsPerTreat.length ; j++)
						plotsPerTreatInBlock[j] = plotsPerTreat[j] / noOfBlocks;
					randomiseTreatments(startIndex, plotsPerTreatInBlock);
					startIndex += plotsPerBlock;
				}
		}
		
		double vals[] = generator.generate();
		NumVariable response = (NumVariable)data.getVariable("response");
		CatVariable block = (CatVariable)data.getVariable("block");
		CatVariable treat = (CatVariable)data.getVariable("treatment");
		
		ValueEnumeration re = response.values();
		ValueEnumeration be = block.values();
		ValueEnumeration te = treat.values();
		
		int index = 0;
		while (re.hasMoreValues() && be.hasMoreValues() && te.hasMoreValues()) {
			LabelValue t = (LabelValue)te.nextValue();
			LabelValue b = (LabelValue)be.nextValue();
			double value = vals[index];
			value += treatmentEffect[treat.labelIndex(t)];
			int blockIndex = block.labelIndex(b);
			value += blockEffect[blockIndex];
			
			NumValue r = (NumValue)re.nextValue();
			r.setValue(Math.rint(value));
			
			index ++;
		}
		
		data.variableChanged("response");
	}
	
	private void randomiseTreatments(int startPlot, int[] treatCount) {
		CatVariable treat = (CatVariable)data.getVariable("treatment");
		int noOfTreats = treat.noOfCategories();
		
		int totalLeft = 0;
		int sampLeft[] = new int[noOfTreats];
		for (int i=0 ; i<noOfTreats ; i++) {
			sampLeft[i] = treatCount[i];
			totalLeft += treatCount[i];
		}
		int endPlot = startPlot + totalLeft;
		
		for (int i=startPlot ; i<endPlot ; i++) {
			double index = totalLeft * randomisator.nextDouble();
			int cumulative = 0;
			for (int j=0 ; j<noOfTreats ; j++) {
				cumulative += sampLeft[j];
				if (index < cumulative) {
					treat.setValueAt(treat.getLabel(j), i);
					sampLeft[j] --;
					break;
				}
			}
			totalLeft --;
		}
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			generateResponses();
			responseView.setShowData(true);
			return true;
		}
		else if (target == showBlocksCheck) {
			boolean showBlocks = showBlocksCheck.getState();
			responseView.setShowBlocks(showBlocks);
			designView.setShowBlocks(showBlocks);
			return true;
		}
		else if (target == designChoice) {
			int oldDesign = design;
			design = designChoice.getSelectedIndex();
			if (oldDesign != design) {
				responseView.setShowData(false);
				data.variableChanged("treatment");
				if (design != RANDOMISED && oldDesign == RANDOMISED) {
					designView.animateFrames(1, BlockPlotsView.kBlockedFrame - 1, 5, null);
					showBlocksCheck.setState(true);
					showBlocksCheck.disable();
					responseView.setShowBlocks(true);
					designView.setShowBlocks(true);
				}
				else if (design == RANDOMISED && oldDesign != RANDOMISED) {
					designView.animateFrames(BlockPlotsView.kBlockedFrame - 1,
																	1 - BlockPlotsView.kBlockedFrame, 5, null);
					showBlocksCheck.enable();
				}
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