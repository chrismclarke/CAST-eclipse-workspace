package sampDesignProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import distn.*;
import coreGraphics.*;

import sampDesign.*;


class Stratum1SampleSlider extends XSlider {
	private int totalSampleSize;
	
	Stratum1SampleSlider(int totalSampleSize, int startStratum1, XApplet applet) {
		super("1", String.valueOf(totalSampleSize-1), applet.translate("Sample sizes") + ":  ", 1, totalSampleSize-1,
																														startStratum1, applet);
		this.totalSampleSize = totalSampleSize;
		setFont(applet.getStandardBoldFont());
	}
	
	protected Value translateValue(int val) {
		int nStrat1 = getValue();
		int nStrat2 = totalSampleSize - nStrat1;
		return new LabelValue(String.valueOf(nStrat1) + " / " + String.valueOf(nStrat2));
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(String.valueOf(totalSampleSize) + "/"
																						+ String.valueOf(totalSampleSize) + " sampled");
	}
}



public class StratifiedSampleApplet extends XApplet {
	static final private String VERT_AXIS_PARAM = "vertAxis";
	static final private String DECIMALS_PARAM = "decimals";
	
	static final private String RANDOM_NORMAL_PARAM = "randomNormal";
//	static final private String STRATUM_SIZE_PARAM = "stratumSize";
	
	static final private String RANDOM_SEED_PARAM = "sampleSeed";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	static final private String TITLE_PARAM = "title";
	static final private String SHOW_SLIDER_PARAM = "showSlider";
	static final private String MEAN_NAME_PARAM = "meanName";
	
	private DataSet data;
	private SummaryDataSet summaryData1, summaryData2;
	
	private StratifiedSampleView firstStratifiedView = null;
	
	private XButton sampleButton, resetButton;
	private Stratum1SampleSlider stratumSlider;
	
	private int decimals;
	
	public void setupApplet() {
		data = getData();
		summaryData1 = getSummaryData(data, "y1");
		summaryData2 = getSummaryData(data, "y2");
		
		setLayout(new BorderLayout());
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																														ProportionLayout.TOTAL));
			topPanel.add(ProportionLayout.LEFT, displayPanel(data, summaryData1, "y1", getParameter(TITLE_PARAM + "1")));
			topPanel.add(ProportionLayout.RIGHT, displayPanel(data, summaryData2, "y2", getParameter(TITLE_PARAM + "2")));
		
		add("Center", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																														ProportionLayout.TOTAL));
			bottomPanel.add(ProportionLayout.LEFT, samplingPanel());
			bottomPanel.add(ProportionLayout.RIGHT, sliderPanel(data));
		
		add("South", bottomPanel);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		CatVariable dummyCat = new CatVariable("dummy");	//	to help create stratum axis
		dummyCat.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("dummy", dummyCat);
		int nStrata = dummyCat.noOfCategories();
		
		decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		long samplingSeed = Long.parseLong(getParameter(RANDOM_SEED_PARAM));
		
		RandomContinuous[] popnGenerator = new RandomContinuous[nStrata];
		for (int i=0 ; i<nStrata ; i++)
			popnGenerator[i] = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM + (i+1)));
		
		StratifiedSampleVariable y1 = new StratifiedSampleVariable(getParameter(VAR_NAME_PARAM),
																						samplingSeed, popnGenerator, decimals);
		int sampleSize[] = readSampleSizes(SAMPLE_SIZE_PARAM, nStrata);
		if (sampleSize.length > 1)
			y1.setSampleSizes(sampleSize);
		else
			y1.setSampleSize(sampleSize[0]);
		data.addVariable("y1", y1);
		NumValue[][] popnValues = y1.getPopnValues();
		
		sampleSize = readSampleSizes(SAMPLE_SIZE_PARAM + "2", nStrata);
		StratifiedSampleVariable y2 = new StratifiedSampleVariable(getParameter(VAR_NAME_PARAM),
																						samplingSeed + 23423845L, popnValues);
		y2.setSampleSizes(sampleSize);
		data.addVariable("y2", y2);
		
		return data;
	}
	
	private int[] readSampleSizes(String paramName, int nStrata) {
		StringTokenizer st = new StringTokenizer(getParameter(paramName));
		int nSizes=0;
		while (st.hasMoreTokens()) {
			st.nextToken();
			nSizes ++;
		}
		
		int sampleSize[] = new int[nSizes];
		st = new StringTokenizer(getParameter(paramName));
		for (int i=0 ; i<nSizes ; i++)
			sampleSize[i] = Integer.parseInt(st.nextToken());
		return sampleSize;
	}
	
	private SummaryDataSet getSummaryData(DataSet data, String yKey) {
		SummaryDataSet summaryData = new SummaryDataSet(data, yKey);
		
		summaryData.addVariable("pred", new StratifiedMeanVariable("Prediction", yKey, decimals));
			
		NormalDistnVariable normal = new NormalDistnVariable("Normal");
		StratifiedSampleVariable y = (StratifiedSampleVariable)data.getVariable(yKey);
		normal.setMean(y.getMeanOfPred());
		normal.setSD(y.getSDOfPred());
		summaryData.addVariable("normal", normal);
		
		summaryData.setAccumulate(true);
		return summaryData;
	}

//---------------------------------------------------------------------

	
	private XPanel displayPanel(DataSet data, SummaryDataSet summaryData, String yKey, String title) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel headerPanel = new XPanel();
			headerPanel.setLayout(new BorderLayout());
			
				XLabel titleLabel = new XLabel(title, XLabel.CENTER, this);
				titleLabel.setFont(getBigBoldFont());
			headerPanel.add("North", titleLabel);
			
				CoreVariable y = data.getVariable(yKey);
				XLabel valueLabel = new XLabel(y.name, XLabel.LEFT, this);
				String meanName = getParameter(MEAN_NAME_PARAM);
				if (meanName == null)
					meanName = "Predicted mean";
				XLabel meanLabel = new XLabel(meanName, XLabel.RIGHT, this);
				meanLabel.setFont(getStandardBoldFont());
			headerPanel.add("Center", valueLabel);
			headerPanel.add("East", meanLabel);
		
		thePanel.add("North", headerPanel);
		
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new AxisLayout());
			
				HorizAxis stratumAxis = new HorizAxis(this);
				stratumAxis.setCatLabels((CatVariable)data.getVariable("dummy"));
				
			graphPanel.add("Bottom", stratumAxis);
			
			
				VertAxis vertAxis = new VertAxis(this);
				String labelInfo = getParameter(VERT_AXIS_PARAM);
				vertAxis.readNumLabels(labelInfo);
			graphPanel.add("Left", vertAxis);
			
				StratifiedSampleView theView = new StratifiedSampleView(data, this, stratumAxis,
																																vertAxis, yKey, firstStratifiedView);
				if (firstStratifiedView == null)
					firstStratifiedView = theView;
				else
					firstStratifiedView.setLinkedView(theView);
				
				theView.lockBackground(Color.white);
			graphPanel.add("Center", theView);
			
				JitterPlusNormalView predDotPlot = new JitterPlusNormalView(summaryData, this, vertAxis,
																																										"normal", 1.0);
//				StackedPlusNormalView predDotPlot = new StackedPlusNormalView(summaryData, null, this, vertAxis,
//																																"normal", BufferedCanvas.BUFFERED);
				predDotPlot.setMinDisplayWidth(50);
//				predDotPlot.setMinDisplayWidth(100);
//				predDotPlot.setCrossSize(DataView.SMALL_CROSS);
				predDotPlot.lockBackground(new Color(0xEEEEEE));
			graphPanel.add("RightMargin", predDotPlot);
		
		thePanel.add("Center", graphPanel);
		
		return thePanel;
	}

	
	private XPanel samplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
					
			sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
			resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
		
		return thePanel;
	}

	
	private XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		String showParamString = getParameter(SHOW_SLIDER_PARAM);
		if (showParamString != null && showParamString.equals("true")) {
				StratifiedSampleVariable y = (StratifiedSampleVariable)data.getVariable("y2");
				int[] sampleSize = y.getSampleSizes();
			if (sampleSize != null) {
					int nTotal = 0;
					for (int i=0 ; i<sampleSize.length ; i++)
						nTotal += sampleSize[i];
					stratumSlider = new Stratum1SampleSlider(nTotal, sampleSize[0], this);
				thePanel.add("Center", stratumSlider);
			}
		}
		
		return thePanel;
	}

//----------------------------------------------------------------
	
	private void clearSample() {
		summaryData1.clearData();
		summaryData1.variableChanged("pred");
		summaryData2.clearData();
		summaryData2.variableChanged("pred");
		
		StratifiedSampleVariable y1 = (StratifiedSampleVariable)data.getVariable("y1");
		y1.clearSample();
		data.variableChanged("y1");
		
		StratifiedSampleVariable y2 = (StratifiedSampleVariable)data.getVariable("y2");
		y2.clearSample();
		data.variableChanged("y2");
	}
	
	protected void doTakeSample() {
		summaryData1.takeSample();
		summaryData2.takeSample();
	}
	
	private void changeSampleSizes(int n1, int n2) {
		int[] sampleSize = new int[2];
		sampleSize[0] = n1;
		sampleSize[1] = n2;
		StratifiedSampleVariable y = (StratifiedSampleVariable)data.getVariable("y2");
		y.setSampleSizes(sampleSize);
	
		NormalDistnVariable normal = (NormalDistnVariable)summaryData2.getVariable("normal");
		normal.setMean(y.getMeanOfPred());
		normal.setSD(y.getSDOfPred());
		clearSample();
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
		else if (target == stratumSlider) {
			int n1 = stratumSlider.getValue();
			int n2 = stratumSlider.getMaxValue() + 1 - n1;
			changeSampleSizes(n1, n2);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}