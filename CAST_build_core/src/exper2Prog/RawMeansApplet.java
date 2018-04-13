package exper2Prog;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import random.*;
import models.*;

import exper2.*;

public class RawMeansApplet extends XApplet {
	static final private String Y_NAME_PARAM = "yVarName";
	static final private String Y_MODEL_PARAM = "yModel";
	static final private String Y_DECIMALS_PARAM = "yDecimals";
	static final private String BLOCK_NAME_PARAM = "blockVarName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	static final protected String SHORT_BLOCK_LABELS_PARAM = "blockShortLabels";
	static final private String TREAT_NAME_PARAM = "treatVarName";
	static final private String TREAT_VALUES_PARAM = "treatValues";
	static final private String TREAT_LABELS_PARAM = "treatLabels";
	
	static final private String MAX_MEAN_PARAM = "maxMean";
	static final protected String BLOCK_N_MAX_EXTRA_PARAM = "blockNMaxExtra";
	static final private String Y_AXIS_PARAM = "yAxis";
	static final private String DATA_NAMES_PARAM = "dataNames";
	
	static final private String[] kXKeys = {"block", "treat"};
	static final private String[] kBlockKey = {"block"};
	static final private String[] kTreatKey = {"treat"};
	
	static final protected int kSliderMax = 100;
	
	static final protected Color kParamBackgroundColor = new Color(0xD6E1FF);
	
	protected DataSet data;
	private double blockNBase;
	
	protected NumValue maxMean;
	protected double maxExtra;
	protected XNoValueSlider blockNSlider;
	private XChoice dataChoice;
	private int currentDataIndex = 0;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 0));
			
			String dataNamesString = getParameter(DATA_NAMES_PARAM);
			if (dataNamesString != null) {
				dataChoice = new XChoice(this);
				StringTokenizer st = new StringTokenizer(dataNamesString, "#");
				while (st.hasMoreTokens())
					dataChoice.addItem(st.nextToken());
				
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				choicePanel.add(dataChoice);
				topPanel.add("North", choicePanel);
			}
			
			topPanel.add("Center", dataTablePanel(data));
			topPanel.add("South", sliderPanel(data));
			topPanel.add("East", meansPanel(data));
		add("North", topPanel);
		
		add("Center", displayPanel(data));
	}
	
	private int[] intArray(int val, int rep) {
		int result[] = new int[rep];
		for (int i=0 ; i<rep ; i++)
			result[i] = val;
		return result;
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			CatVariable blockVar = new CatVariable(getParameter(BLOCK_NAME_PARAM));
			blockVar.readLabels(getParameter(BLOCK_LABELS_PARAM));
			blockVar.readValues(getParameter(BLOCK_VALUES_PARAM));
		data.addVariable("block", blockVar);
		
		data.addCatVariable("treat", getParameter(TREAT_NAME_PARAM),
											getParameter(TREAT_VALUES_PARAM), getParameter(TREAT_LABELS_PARAM));
		
			int n = blockVar.noOfValues();
			RandomNormal errorGen = new RandomNormal(n, 0, 1, 4);
			NumSampleVariable errorVar = new NumSampleVariable(translate("Error"), errorGen, 9);
			errorVar.generateNextSample();
		data.addVariable("error", errorVar);
		
			MultipleRegnModel model = new MultipleRegnModel("Model", data, kXKeys,
																															getParameter(Y_MODEL_PARAM));
			blockNBase = model.getParameter(blockVar.noOfCategories() - 1).toDouble();
		data.addVariable("model", model);
		
			int yDecimals = Integer.parseInt(getParameter(Y_DECIMALS_PARAM));
			ResponseVariable yVar = new ResponseVariable(getParameter(Y_NAME_PARAM), data, kXKeys,
																			"error", "model", yDecimals);
		data.addVariable("y", yVar);
		
			maxMean = new NumValue(getParameter(MAX_MEAN_PARAM));
			
			MultipleRegnModel lsBlock = new MultipleRegnModel("LS_block", data, kBlockKey);
			lsBlock.setLSParams("y", intArray(maxMean.decimals, lsBlock.noOfParameters()), 9);
		data.addVariable("lsBlock", lsBlock);
		
			MultipleRegnModel lsTreat = new MultipleRegnModel("LS_treat", data, kTreatKey);
			lsTreat.setLSParams("y", intArray(maxMean.decimals, lsTreat.noOfParameters()), 9);
		data.addVariable("lsTreat", lsTreat);
		
			MultipleRegnModel lsAll = new MultipleRegnModel("LS_all", data, kXKeys);
			lsAll.setLSParams("y", intArray(maxMean.decimals, lsAll.noOfParameters()), 9);
		data.addVariable("lsAll", lsAll);
		
		return data;
	}
	
	protected XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(140, 20, 140, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			maxExtra = Double.parseDouble(getParameter(BLOCK_N_MAX_EXTRA_PARAM));
			blockNSlider = new XNoValueSlider("0", String.valueOf(maxExtra), translate("Addition to last block"),
																								0, kSliderMax, 0, this);
		
		thePanel.add("Center", blockNSlider);		
		return thePanel;
	}
	
	private XPanel dataTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			IncompleteBlockView theView = new IncompleteBlockView(data, this, "y", "block", "treat");
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel meansPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XPanel innerPanel = new InsetPanel(8, 4);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																													VerticalLayout.VERT_CENTER, 3));
			
				XLabel label = new XLabel(translate("Raw means"), XLabel.CENTER, this);
				label.setFont(getStandardBoldFont());
			innerPanel.add(label);
			
				XPanel meansPanel = new XPanel();
				meansPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT,
																														VerticalLayout.VERT_CENTER, 3));
				
				CatVariable treatVar = (CatVariable)data.getVariable("treat");
				for (int i=0 ; i<treatVar.noOfCategories() ; i++) {
					RawMeanView theView = new RawMeanView(data, this, "treat", i,
																														"lsTreat", maxMean);
					meansPanel.add(theView);
				}
			innerPanel.add(meansPanel);
			innerPanel.lockBackground(kParamBackgroundColor);
		
		thePanel.add(innerPanel);
			
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 6));
		
			CatVariable dummyBlockVar = new CatVariable("");
			dummyBlockVar.readLabels(getParameter(SHORT_BLOCK_LABELS_PARAM));
		thePanel.add(ProportionLayout.LEFT, meansDisplayPanel(data, "block", "lsBlock",
																dummyBlockVar, "Left", XLabel.LEFT, translate("Block means")));
			
			CatVariable treatVar = (CatVariable)data.getVariable("treat");
		thePanel.add(ProportionLayout.RIGHT, meansDisplayPanel(data, "treat", "lsTreat",
																	treatVar, "Right", XLabel.RIGHT, translate("Treatment means")));
		return thePanel;
	}
	
	protected XPanel meansDisplayPanel(DataSet data, String catKey, String lsKey,
													CatVariable catVar, String vertAxisPosition, int labelSide,
													String vertAxisTitle) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel yLabel = new XLabel(vertAxisTitle, labelSide, this);
			yLabel.setForeground(Color.red);
			yLabel.setFont(getStandardBoldFont());
		thePanel.add("North", yLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			innerPanel.add(vertAxisPosition, yAxis);
			
				HorizAxis treatAxis = new HorizAxis(this);
				treatAxis.setCatLabels(catVar);
			innerPanel.add("Bottom", treatAxis);
			
				CoreOneFactorView theView = getView(data, treatAxis, yAxis, catKey, "y",
																																				lsKey, labelSide);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
			innerPanel.add("Center", theView);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	protected CoreOneFactorView getView(DataSet data, HorizAxis treatAxis, VertAxis yAxis,
																			String xKey, String yKey, String lsKey, int labelSide) {
		return new CoreOneFactorView(data, this, treatAxis, yAxis, xKey, "y", lsKey);
	}
	
	private void updateLSParams() {
		MultipleRegnModel lsBlock = (MultipleRegnModel)data.getVariable("lsBlock");
		lsBlock.updateLSParams("y");
		
		MultipleRegnModel lsTreat = (MultipleRegnModel)data.getVariable("lsTreat");
		lsTreat.updateLSParams("y");
		
		MultipleRegnModel lsAll = (MultipleRegnModel)data.getVariable("lsAll");
		lsAll.updateLSParams("y");
		
		data.variableChanged("y");
	}
	
	private void updateForSlider() {
		double extra = blockNSlider.getValue() * maxExtra / kSliderMax;
		
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		CatVariable blockVar = (CatVariable)data.getVariable("block");
		int lastBlockIndex = blockVar.noOfCategories() - 1;
		
		model.setParameter(lastBlockIndex, blockNBase + extra);
		
		updateLSParams();
	}
	
	private boolean localAction(Object target) {
		if (target == blockNSlider) {
			updateForSlider();
			
			return true;
		}
		else if (target == dataChoice) {
			int newChoice = dataChoice.getSelectedIndex();
			if (newChoice != currentDataIndex) {
				currentDataIndex = newChoice;
				CatVariable treatVar = (CatVariable)data.getVariable("treat");
				String valueParam = TREAT_VALUES_PARAM;
				if (newChoice > 0)
					valueParam += (newChoice + 1);
				treatVar.readValues(getParameter(valueParam));
				
				CatVariable blockVar = (CatVariable)data.getVariable("block");
				valueParam = BLOCK_VALUES_PARAM;
				if (newChoice > 0)
					valueParam += (newChoice + 1);
				blockVar.readValues(getParameter(valueParam));
				
				NumSampleVariable errorVar = (NumSampleVariable)data.getVariable("error");
				errorVar.setSampleSize(blockVar.noOfValues());
				errorVar.generateNextSample();
				
				blockNSlider.setValue(0);
				updateForSlider();
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