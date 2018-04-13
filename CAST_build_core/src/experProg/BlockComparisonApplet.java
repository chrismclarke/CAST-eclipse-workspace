package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import random.RandomNormal;
import coreSummaries.*;

//import randomisation.*;
import exper.*;


public class BlockComparisonApplet extends XApplet {
	static final private String BLOCK_EFFECT_PARAM = "blockEffect";
	static final private String BLOCK_NAME_PARAM = "blockName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	
	static final private String TREAT_EFFECT_PARAM = "treatEffect";
	static final private String TREAT_NAME_PARAM = "treatName";
	static final private String TREAT_VALUES_PARAM = "treatValues";
	static final private String TREAT_LABELS_PARAM = "treatLabels";
	static final private String TREAT_IMAGES_PARAM = "treatPicts";
	
	static final private String CONSTANT_PARAM = "constant";
	static final private String VARIANCES_PARAM = "variances";
	static final private String RESPONSE_NAME_PARAM = "responseName";
	static final private String RESPONSE_AXIS_INFO_PARAM = "responseAxis";
	static final private String BLOCK_PARAM = "blockInfo";
	static final private String DECIMALS_PARAM = "decimals";
	
	static final private String DIFF_NAME_PARAM = "diffName";
	static final private String EFFECT_NAME_PARAM = "effectName";
	static final private String DIFF_AXIS_INFO_PARAM = "diffAxis";
	static final private String BLOCK_EXTREMES_PARAM = "blockEffectExtremes";
	static final private int kBlockEffectSteps = 100;
	
	static final protected Color kEffectsBackground = new Color(0xFFEEBB);
	
	private String kDesignName[];
	
	private DataSet data[] = new DataSet[2];
	private SummaryDataSet summaryData[] = new SummaryDataSet[2];
	
	private double treatmentEffect[];
	private double blockEffect[];
	private double effects[][] = new double[2][];
	private NumValue maxTreatEffect, minTreatEffect;
	private String fixedTreatEffectName = null;
	
	private int rows, cols, rowsPerBlock;
	
	private XButton sampleButton[] = new XButton[2];
	private XNoValueSlider blockEffectSlider;
	private ParameterSlider treatEffectSlider;
	private XChoice blockEffectChoice;
	
	private double blockPlusErrorVar, minBlockPropn, maxBlockPropn, startBlockPropn;
	
	public void setupApplet() {
		kDesignName = new String[2];
		kDesignName[0] = translate("Completely randomised");
		kDesignName[1] = translate("Randomised block");
		
		readEffects();
		data[0] = readData(0);
			CatVariable blockVar = (CatVariable)data[0].getVariable("block");
			int blockSize = blockVar.noOfValues() / blockVar.noOfCategories();
		data[1] = readData(blockSize);
		
		for (int i=0 ; i<2 ; i++) {
			summaryData[i] = getSummaryData(data[i]);
			summaryData[i].takeSample();
		}
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel experPanel = new XPanel();
			experPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
			
			experPanel.add(ProportionLayout.TOP, getExperPanel(0));
			experPanel.add(ProportionLayout.BOTTOM, getExperPanel(1));
		
		add("Center", experPanel);
		add("North", sliderPanel(data[0]));
	}
	
	private void readEffects() {
		StringTokenizer st = new StringTokenizer(getParameter(TREAT_EFFECT_PARAM), "#");
		treatmentEffect = new double[2];
		treatmentEffect[0] = 0.0;
		treatmentEffect[1] = 1.0;
		maxTreatEffect = new NumValue(st.nextToken());
		minTreatEffect = new NumValue(0.0, maxTreatEffect.decimals);
		if (st.hasMoreTokens())
			fixedTreatEffectName = st.nextToken();
		
		st = new StringTokenizer(getParameter(BLOCK_EFFECT_PARAM));
		blockEffect = new double[st.countTokens()];
		double sx = 0.0;
		double sxx = 0.0;
		int n = blockEffect.length;
		for (int i=0 ; i<n ; i++) {
			double x = Double.parseDouble(st.nextToken());
			blockEffect[i] = x;
			sx += x;
			sxx += x * x;
		}
		double meanEffect = sx / n;
		double sdEffect = Math.sqrt((sxx - sx * sx / n) / n);
		for (int i=0 ; i<n ; i++)
			blockEffect[i] = (blockEffect[i] - meanEffect) / sdEffect;
		
		effects[0] = blockEffect;
		effects[1] = treatmentEffect;
		
		st = new StringTokenizer(getParameter(VARIANCES_PARAM));
		blockPlusErrorVar = Double.parseDouble(st.nextToken());
		minBlockPropn = Double.parseDouble(st.nextToken());
		maxBlockPropn = Double.parseDouble(st.nextToken());
		startBlockPropn = Double.parseDouble(st.nextToken());
	}
	
	private DataSet readData(int randomiseBlockSize) {
		DataSet data = new DataSet();
		
			CatVariable blockVar = new CatVariable(getParameter(BLOCK_NAME_PARAM));
			blockVar.readLabels(getParameter(BLOCK_LABELS_PARAM));
			blockVar.readValues(getParameter(BLOCK_VALUES_PARAM));
		data.addVariable("block", blockVar);
		
			RandomisedCatVariable treatmentVar = new RandomisedCatVariable(getParameter(TREAT_NAME_PARAM),
																																				randomiseBlockSize);
			treatmentVar.readLabels(getParameter(TREAT_LABELS_PARAM));
			treatmentVar.readValues(getParameter(TREAT_VALUES_PARAM));
		data.addVariable("treat", treatmentVar);
		
			int nValues = blockVar.noOfValues();
			RandomNormal generator = new RandomNormal(nValues, 0.0, 1.0, 3.0);
			NumSampleVariable error = new NumSampleVariable("error", generator, 10);
			error.setSampleSize(generator.getSampleSize());
		data.addVariable("error", error);
		
			String effectKeys[] = {"block", "treat"};
			double constant = Double.parseDouble(getParameter(CONSTANT_PARAM));
			FactorResponseVariable response = new FactorResponseVariable(getParameter(RESPONSE_NAME_PARAM),
											data, constant, effectKeys, effects, "error");
			setTreatmentEffect(response, maxTreatEffect.toDouble());
			setBlockErrorVariance(response, startBlockPropn);
		data.addVariable("response", response);
		
		data.addVariable("biSamp", new BiSampleVariable(data, "error", "treat"));
		
		return data;
	}
	
	private void setTreatmentEffect(FactorResponseVariable response, double treatEffect) {
		response.setFactorEffectScaling(1, treatEffect);
	}
	
	private void setBlockErrorVariance(FactorResponseVariable response, double blockPropn) {
		response.setErrorEffectScaling(Math.sqrt(0.07 * blockPlusErrorVar));
//		response.setErrorEffectScaling(Math.sqrt((1.0 - blockPropn) * blockPlusErrorVar));
		response.setFactorEffectScaling(0, Math.sqrt(blockPropn * blockPlusErrorVar));
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "biSamp");
			
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		summaryData.addVariable("diff", new DiffSummaryVariable(getParameter(DIFF_NAME_PARAM),
																									"response", "treat", decimals));
		summaryData.setAccumulate(true);
		return summaryData;
	}
	
	private XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(3, 6);
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
				
			String blockExtremesString = getParameter(BLOCK_EXTREMES_PARAM);
			if (blockExtremesString == null) {
				int sliderStart = (int)Math.rint(kBlockEffectSteps * (startBlockPropn - minBlockPropn)
																													/ (maxBlockPropn - minBlockPropn));
				CatVariable blockVar = (CatVariable)data.getVariable("block");
				blockEffectSlider = new XNoValueSlider(translate("None"), translate("Strong"),
														blockVar.name + " " + translate("effect"), 0,
																										kBlockEffectSteps, sliderStart, this);
				blockEffectSlider.setFont(getStandardBoldFont());
				leftPanel.add(blockEffectSlider);
			}
			else {
				blockEffectChoice = new XChoice(this);
				StringTokenizer st = new StringTokenizer(blockExtremesString, "#");
				blockEffectChoice.addItem(st.nextToken());
				blockEffectChoice.addItem(st.nextToken());
				leftPanel.add(blockEffectChoice);
			}
			
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			if (fixedTreatEffectName == null) {
				treatEffectSlider = new ParameterSlider(minTreatEffect, maxTreatEffect, maxTreatEffect,
														getParameter(EFFECT_NAME_PARAM), ParameterSlider.SHOW_MIN_MAX, this);
				treatEffectSlider.setFont(getStandardBoldFont());
				rightPanel.add(treatEffectSlider);
			}
			else {
				XLabel effectLabel = new XLabel(fixedTreatEffectName, XLabel.CENTER, this);
				effectLabel.setFont(getStandardBoldFont());
				rightPanel.add(effectLabel);
			}
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		thePanel.lockBackground(kEffectsBackground);
		return thePanel;
	}
	
	private XPanel designPanel(DataSet data, String designName) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 4));
		
			XLabel designLabel = new XLabel(designName, XLabel.LEFT, this);
			designLabel.setFont(getBigBoldFont());
			
		thePanel.add(designLabel);
		
			String blockInfo = getParameter(BLOCK_PARAM);
			StringTokenizer st = new StringTokenizer(blockInfo);
			rows = Integer.parseInt(st.nextToken());
			cols = Integer.parseInt(st.nextToken());
			rowsPerBlock = Integer.parseInt(st.nextToken());
			
			BlockAndTreatView designView = new BlockAndTreatView(data, this, getParameter(TREAT_IMAGES_PARAM), "treat",
								rows, cols, rowsPerBlock);
			designView.lockBackground(Color.white);
		thePanel.add(designView);
		return thePanel;
	}
	
	private XPanel responsePlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(RESPONSE_AXIS_INFO_PARAM));
			plotPanel.add("Left", yAxis);
			
				HorizAxis treatAxis = new HorizAxis(this);
				treatAxis.setCatLabels((CatVariable)data.getVariable("treat"));
				
			plotPanel.add("Bottom", treatAxis);
			
				TreatDotView yView = new TreatDotView(data, this, yAxis, treatAxis, "response", "treat", 1.0);
				yView.lockBackground(Color.white);
			plotPanel.add("Center", yView);
		
		thePanel.add("Center", plotPanel);
		
			NumVariable respVar = (NumVariable)data.getVariable("response");
			XLabel respLabel = new XLabel(respVar.name, XLabel.LEFT, this);
			respLabel.setFont(yAxis.getFont());
		thePanel.add("North", respLabel);
		
		return thePanel;
	}
	
	private XPanel samplePanel(int designIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																	VerticalLayout.VERT_CENTER, 10));
		
			sampleButton[designIndex] = new RepeatingButton(translate("Repeat"), this);
		thePanel.add(sampleButton[designIndex]);
		
		return thePanel;
	}
	
	private XPanel summaryDisplayPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis diffAxis = new VertAxis(this);
				String labelInfo = getParameter(DIFF_AXIS_INFO_PARAM);
				diffAxis.readNumLabels(labelInfo);
			plotPanel.add("Left", diffAxis);
			
				DotPlotView diffView = new DotPlotView(summaryData, this, diffAxis, 1.0);
				diffView.lockBackground(Color.white);
				diffView.setActiveNumVariable("diff");
			plotPanel.add("Center", diffView);
		
		thePanel.add("Center", plotPanel);
		
			NumVariable diffVar = (NumVariable)summaryData.getVariable("diff");
			XLabel diffLabel = new XLabel(diffVar.name, XLabel.LEFT, this);
			diffLabel.setFont(diffAxis.getFont());
		thePanel.add("North", diffLabel);
		
		return thePanel;
	}
	
	private XPanel getExperPanel(int expIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(5, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(5, 0));
			leftPanel.add("West", designPanel(data[expIndex], kDesignName[expIndex]));
			leftPanel.add("Center", samplePanel(expIndex));
		
		thePanel.add("West", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.6, 5, ProportionLayout.HORIZONTAL,
																											ProportionLayout.TOTAL));
			rightPanel.add(ProportionLayout.LEFT, responsePlotPanel(data[expIndex]));
			rightPanel.add(ProportionLayout.RIGHT, summaryDisplayPanel(summaryData[expIndex]));
			
			
		thePanel.add("Center", rightPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<2 ; i++)
			if (target == sampleButton[i]) {
				summaryData[i].takeSample();
				return true;
			}
		if (target == blockEffectSlider) {
			double blockPropn = minBlockPropn + blockEffectSlider.getValue()
																	* (maxBlockPropn - minBlockPropn) / kBlockEffectSteps;
			for (int i=0 ; i<2 ; i++) {
				FactorResponseVariable resp = (FactorResponseVariable)data[i].getVariable("response");
				setBlockErrorVariance(resp, blockPropn);
				data[i].variableChanged("response");
				summaryData[i].setSingleSummaryFromData();
			}
		}
		else if (target == treatEffectSlider) {
			double treatEffect = treatEffectSlider.getParameter().toDouble();
			for (int i=0 ; i<2 ; i++) {
				FactorResponseVariable resp = (FactorResponseVariable)data[i].getVariable("response");
				setTreatmentEffect(resp, treatEffect);
				data[i].variableChanged("response");
				summaryData[i].setSingleSummaryFromData();
			}
		}
		else if (target == blockEffectChoice) {
			int newChoice = blockEffectChoice.getSelectedIndex();
			double blockPropn = (newChoice == 0) ? maxBlockPropn : minBlockPropn;
			for (int i=0 ; i<2 ; i++) {
				FactorResponseVariable resp = (FactorResponseVariable)data[i].getVariable("response");
				setBlockErrorVariance(resp, blockPropn);
				data[i].variableChanged("response");
				summaryData[i].setSingleSummaryFromData();
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}