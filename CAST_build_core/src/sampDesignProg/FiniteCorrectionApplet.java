package sampDesignProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;

import sampDesign.*;
//import randomStat.*;


class SampleSizeSlider extends XSlider {
	private int maxPopnSize;
	
	SampleSizeSlider(int popnSize, int startSamp, int maxPopnSize, XApplet applet) {
		super("1", String.valueOf(popnSize), applet.translate("Sample size") + " = ", 1, popnSize, startSamp, applet);
		this.maxPopnSize = maxPopnSize;
		setFont(applet.getStandardBoldFont());
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getValue(), 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return new NumValue(maxPopnSize, 0).stringWidth(g);
	}
}


public class FiniteCorrectionApplet extends XApplet {
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "randomPopn";
	static final private String SAMPLE_SEED_PARAM = "sampleSeed";
	static final private String INIT_SAMP_PARAM = "initSampSize";
	static final private String POPN_SIZE_PARAM = "popnSize";
	static final private String DECIMALS_PARAM = "decimals";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private XButton resetButton;
	private RepeatingButton sampleButton;
	private SampleSizeSlider sampleSizeSlider;
	private XChoice popnSizeChoice;
	private XCheckbox finiteCorrectionCheck;
	
	private JitterPlusNormalView meanView;
	
	private int decimals;
	private int popnSize[];
	private int initialPopnIndex;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																															ProportionLayout.TOTAL));
			leftPanel.add(ProportionLayout.TOP, sampleDisplayPanel(data));
			leftPanel.add(ProportionLayout.BOTTOM, summaryDisplayPanel(summaryData));
			
		add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																															ProportionLayout.TOTAL));
			rightPanel.add(ProportionLayout.TOP, controlPanel());
			rightPanel.add(ProportionLayout.BOTTOM, correctionPanel());
		add("East", rightPanel);
		
		add("South", popSampSizePanel(data));
		
		setNormalApprox(finiteCorrectionCheck.getState());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomNormal generator = new RandomNormal(randomInfo);
		
		StringTokenizer st = new StringTokenizer(getParameter(POPN_SIZE_PARAM));
		int nPopnSizes = 0;
		while (st.hasMoreTokens()) {
			st.nextToken();
			nPopnSizes ++;
		}
		
		popnSize = new int[nPopnSizes];
		st = new StringTokenizer(getParameter(POPN_SIZE_PARAM));
		for (int i=0 ; i<nPopnSizes ; i++) {
			String nextSizeString = st.nextToken();
			if (nextSizeString.charAt(0) == '*') {
				popnSize[i] = Integer.parseInt(nextSizeString.substring(1));
				initialPopnIndex = i;
			}
			else
				popnSize[i] = Integer.parseInt(nextSizeString);
		}
		
		FinitePopnSampleVariable y = new FinitePopnSampleVariable(getParameter(VAR_NAME_PARAM),
											Long.parseLong(getParameter(SAMPLE_SEED_PARAM)), generator, decimals);
											
		y.setSampleSize(Integer.parseInt(getParameter(INIT_SAMP_PARAM)));
		
		data.addVariable("y", y);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "y");
		
		summaryData.addVariable("mean", new MeanVariable(translate("Mean"), "y", decimals));
		
			NormalDistnVariable normal = new NormalDistnVariable("Normal");
		summaryData.addVariable("normal", normal);
		
		summaryData.setAccumulate(true);
		return summaryData;
	}

//---------------------------------------------------------------------

	
	private XPanel sampleDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis theAxis = new HorizAxis(this);
			theAxis.readNumLabels(getParameter(HORIZ_AXIS_PARAM));
			theAxis.setAxisName(translate("Sampled values"));
			
		thePanel.add("Bottom", theAxis);
		
			FiniteStackedView theView = new FiniteStackedView(data, this, theAxis, "y");
			
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel summaryDisplayPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis theAxis = new HorizAxis(this);
			theAxis.readNumLabels(getParameter(HORIZ_AXIS_PARAM));
			theAxis.setAxisName(translate("Sample mean"));
			
		thePanel.add("Bottom", theAxis);
		
			meanView = new JitterPlusNormalView(summaryData, this, theAxis, "normal", 1.0);
			
//			meanView.setShowDensity (JitterPlusNormalView.NO_DISTN);
			meanView.lockBackground(Color.white);
		thePanel.add("Center", meanView);
		
		return thePanel;
	}

	
	private XPanel controlPanel() {
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		buttonPanel.add(sampleButton);
		
			resetButton = new XButton(translate("Reset"), this);
		buttonPanel.add(resetButton);
		
		return buttonPanel;
	}

	
	private XPanel correctionPanel() {
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			finiteCorrectionCheck = new XCheckbox(translate("Finite population correction"), this);
		buttonPanel.add(finiteCorrectionCheck);
		
		return buttonPanel;
	}

	
	private XPanel popSampSizePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(50, 0));
		
			FinitePopnSampleVariable y = (FinitePopnSampleVariable)data.getVariable("y");
			sampleSizeSlider = new SampleSizeSlider(popnSize[initialPopnIndex], y.getSampleSize(),
															popnSize[popnSize.length - 1], this);
		thePanel.add("Center", sampleSizeSlider);
		
			XPanel popnSizePanel = new XPanel();
			popnSizePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
			XLabel theLabel = new XLabel(translate("Population size"), XLabel.LEFT, this);
			theLabel.setFont(getStandardBoldFont());
			popnSizePanel.add(theLabel);
			
				popnSizeChoice = new XChoice(this);
				for (int i=0 ; i<popnSize.length ; i++)
					popnSizeChoice.add(String.valueOf(popnSize[i]));
				popnSizeChoice.select(initialPopnIndex);
			
			popnSizePanel.add(popnSizeChoice);
		
		thePanel.add("East", popnSizePanel);
		
		return thePanel;
	}

//----------------------------------------------------------------
	
	private void clearSample() {
		summaryData.clearData();
		summaryData.variableChanged("mean");
//		meanView.setShowDensity (JitterPlusNormalView.NO_DISTN);
		
		FinitePopnSampleVariable y = (FinitePopnSampleVariable)data.getVariable("y");
		y.clearSample();
		
		data.variableChanged("y");
	}
	
	private void doTakeSample() {
//		meanView.setShowDensity (JitterPlusNormalView.CONTIN_DISTN);
		summaryData.takeSample();
	}
	
	private void setNormalApprox(boolean showFiniteCorrection) {
		FinitePopnSampleVariable y = (FinitePopnSampleVariable)data.getVariable("y");
		NumValue[] popnValues = y.getPopnValues();
		int popnSize = popnValues.length;
		int sampleSize = y.getSampleSize();
		
		double sy = 0.0;
		double syy = 0.0;
		for (int i=0 ; i<popnSize ; i++) {
			double v = popnValues[i].toDouble();
			sy += v;
			syy += v * v;
		}
		
		double mean = sy / popnSize;
		double var = (syy - sy * sy / popnSize) / popnSize;
		double meanVar = var / sampleSize;
		if (showFiniteCorrection)
			meanVar *= (popnSize - sampleSize) / (double)(popnSize - 1);
		
		NormalDistnVariable normal = (NormalDistnVariable)summaryData.getVariable("normal");
		normal.setMean(mean);
		normal.setSD(Math.sqrt(meanVar));
		
		summaryData.variableChanged("normal");
	}
	
	private void changeSampleSize(int newSampleSize) {
		clearSample();
		FinitePopnSampleVariable y = (FinitePopnSampleVariable)data.getVariable("y");
		y.setSampleSize(newSampleSize);
		setNormalApprox(finiteCorrectionCheck.getState());
	}
	
	private void changePopnSize(int newPopnSize) {
		clearSample();
		FinitePopnSampleVariable y = (FinitePopnSampleVariable)data.getVariable("y");
		y.generatePopnValues(newPopnSize);
		
		sampleSizeSlider.setMaxValue(String.valueOf(newPopnSize), newPopnSize);
		
		int oldSampleSize = y.getSampleSize();
		if (oldSampleSize > newPopnSize) {
			y.setSampleSize(newPopnSize);
			sampleSizeSlider.setValue(newPopnSize);
		}
		
		sampleSizeSlider.repaint();
		
		setNormalApprox(finiteCorrectionCheck.getState());
	}

	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			clearSample();
			return true;
		}
		else if (target == sampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == finiteCorrectionCheck) {
			setNormalApprox(finiteCorrectionCheck.getState());
			return true;
		}
		else if (target == sampleSizeSlider) {
			int newSampleSize = sampleSizeSlider.getValue();
			changeSampleSize(newSampleSize);
			return true;
		}
		else if (target == popnSizeChoice) {
			int newPopnSize = popnSize[popnSizeChoice.getSelectedIndex()];
			changePopnSize(newPopnSize);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}