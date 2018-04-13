package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.OneValueView;
import exper.*;


public class BlockDesignApplet extends ExperDesignApplet {
	static final private String BLOCK_PARAM = "blockInfo";
	static final private String BLOCK_EFFECT_PARAM = "blockEffect";
	static final private String BLOCK_NAME_PARAM = "blockName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabel";
	static final private String BLOCK_IMAGES_PARAM = "blockPicts";
	static final private String TREAT_IMAGES_PARAM = "treatPicts";
	static final private String LAST_BLOCK_PARAM = "lastBlock";
	
	static final private int RANDOMISED = 0;
	static final private int BLOCKED = 1;
//	static final private int BAD = 2;
	
	private double blockEffect[];
	private int rows, cols, rowsPerBlock;
	private XChoice designChoice;
	private int design = RANDOMISED;
	private BlockPlotsView theView;
	private TreatBlockEffectView effectView;
//	private XCheckbox blockExtraCheck;
	@SuppressWarnings("unused")
	private double lastBlockExtra;
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(BLOCK_EFFECT_PARAM));
		blockEffect = new double[st.countTokens()];
		for (int i=0 ; i<blockEffect.length ; i++)
			blockEffect[i] = Double.parseDouble(st.nextToken());
			
		lastBlockExtra = Double.parseDouble(getParameter(LAST_BLOCK_PARAM));
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		NumVariable resp = (NumVariable)data.getVariable("response");
		resp.setDecimals(0);
		NumValue val0 = (NumValue)resp.valueAt(0);
		val0.setValue(maxMean.toDouble());			//		so that value list will be sized to hold big enough value
		
		data.addCatVariable("block", getParameter(BLOCK_NAME_PARAM),
								getParameter(BLOCK_VALUES_PARAM), getParameter(BLOCK_LABELS_PARAM));
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
		
		String blockInfo = getParameter(BLOCK_PARAM);
		StringTokenizer st = new StringTokenizer(blockInfo);
		rows = Integer.parseInt(st.nextToken());
		cols = Integer.parseInt(st.nextToken());
		rowsPerBlock = Integer.parseInt(st.nextToken());
		long permutationSeed = Long.parseLong(st.nextToken());
		
		theView = new BlockPlotsView(data, this, null, getParameter(BLOCK_IMAGES_PARAM), getParameter(TREAT_IMAGES_PARAM),
							"block", "treatment", "response", rows, cols, rowsPerBlock, permutationSeed);
		theView.lockBackground(Color.white);
		thePanel.add(theView);
		
		thePanel.add(new OneValueView(data, "response", this));
		return thePanel;
	}
	
	protected TreatmentEffectView getEffectView(DataSet data) {
		effectView = new TreatBlockEffectView(data, this, "treatment", "response", maxMean);
		return effectView;
	}
	
	protected TreatmentBiasView getBiasView(DataSet data) {
		return null;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		
		designChoice = new XChoice(this);
		designChoice.addItem("Randomised design");
		designChoice.addItem("Block design");
//		designChoice.addItem("Confounded design");
		designChoice.select(0);
		thePanel.add(designChoice);
		
//		CatVariable block = (CatVariable)data.getVariable("block");
//		blockExtraCheck = new XCheckbox("Increase "
//										+ block.getLabel(block.noOfCategories() - 1).toString(), this);
//		thePanel.add(blockExtraCheck);
		
		return thePanel;
	}
	
	protected void generateResponses() {
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
				break;
//			case BAD:
//				CatVariable treat = (CatVariable)data.getVariable("treatment");
//				CatVariable block = (CatVariable)data.getVariable("block");
//				for (int i=0 ; i<treat.noOfValues() ; i++) {
//					int blockIndex = block.getItemCategory(i);
//					int treatIndex = Math.min(blockIndex, treatmentEffect.length);
//					treat.setValueAt(treat.getLabel(treatIndex), i);
//				}
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
//			if (blockExtraCheck.getState() && blockIndex == block.noOfCategories() - 1)
//				value += lastBlockExtra;
			
			NumValue r = (NumValue)re.nextValue();
			r.setValue(Math.rint(value));
			
			index ++;
		}
		
		data.variableChanged("response");
	}
	
	private boolean localAction(Object target) {
		if (target == designChoice) {
			int oldDesign = design;
			design = designChoice.getSelectedIndex();
			if (oldDesign != design) {
				data.variableChanged("treatment");
				if (design != RANDOMISED && oldDesign == RANDOMISED) {
					theView.animateFrames(1, BlockPlotsView.kBlockedFrame - 1, 5, null);
					effectView.setBlockKey("block");
				}
				else if (design == RANDOMISED && oldDesign != RANDOMISED) {
					theView.animateFrames(BlockPlotsView.kBlockedFrame - 1,
							1 - BlockPlotsView.kBlockedFrame, 5, null);
					effectView.setBlockKey(null);
				}
			}
			return true;
		}
		//	else if (evt.target == blockExtraCheck) {
		//		data.variableChanged("treatment");
		//		return true;
		//	}
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