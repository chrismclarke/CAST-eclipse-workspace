package simulationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import random.*;

import simulation.*;


class BSlider extends XSlider {
	public BSlider(XApplet applet) {
		super(applet.translate("Short tail"), applet.translate("Long tail"), null, -99, 100, 0, applet);
							//		if b == bMax, the density is not drawn at all
							//		(bug drawing density, but it is easier to avoid than to fix :-(
	}
	
	protected Value translateValue(int val) {
		return new NumValue(0.0, 2);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return 10;
	}
}


public class RandomNormPlusCumApplet extends XApplet {
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String NORMAL_DISTN_PARAM = "normalDistn";
	static final private String DISTN_NAME_PARAM = "distnName";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String HORIZ_AXIS_NAME_PARAM = "horizAxisName";
	static final private String VALUE_AXIS_INFO_PARAM = "valueAxis";
	static final private String CUM_AXIS_INFO_PARAM = "cumAxis";
	
	private NormalPlusDistnVariable distn;
	
	private XButton takeSampleButton, normalDistnButton;
	private XSlider bSlider;
	private XChoice sampleSizeChoice;
	
	private int sampleSize[];
	private int currentSizeIndex = 0;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
//	private NumValue maxValue;
	
	public void setupApplet() {
		readSampleSizes();
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(20, 0));
		
		add("Center", displayPanel(data, summaryData));
		
		add("South", controlPanel());
	}
	
	private void readSampleSizes() {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int noOfSizes = st.countTokens();
		sampleSize = new int[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++) {
			String nextSize = st.nextToken();
			boolean isInitialSize = nextSize.startsWith("*");
			if (isInitialSize) {
				nextSize = nextSize.substring(1);
				currentSizeIndex = i;
			}
			sampleSize[i] = Integer.parseInt(nextSize);
		}
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		StringTokenizer st = new StringTokenizer(getParameter(NORMAL_DISTN_PARAM));
		double mean = Double.parseDouble(st.nextToken());
		double sd = Double.parseDouble(st.nextToken());
		
		distn = new NormalPlusDistnVariable(getParameter(DISTN_NAME_PARAM));
		distn.setMean(mean);
		distn.setSD(sd);
		data.addVariable("distn", distn);
		
		st = new StringTokenizer(getParameter(DECIMALS_PARAM));
		int pseudoDecimals = Integer.parseInt(st.nextToken());
		RandomRectangular generator = new RandomRectangular(sampleSize[currentSizeIndex], 0.001, 0.999);
		NumSampleVariable pseudo = new NumSampleVariable("Pseudo", generator, pseudoDecimals);
		data.addVariable("pseudo", pseudo);
		
		int valueDecimals = Integer.parseInt(st.nextToken());
		DistnFunctionVariable randomVal = new DistnFunctionVariable("Value", data,
																		"pseudo", "distn", valueDecimals);
		data.addVariable("randomVal", randomVal);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "pseudo");
		summaryData.takeSample();
		selectLowestValue();
		
		return summaryData;
	}
	
	private XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(VALUE_AXIS_INFO_PARAM));
			xAxis.setAxisName(getParameter(HORIZ_AXIS_NAME_PARAM));
		thePanel.add("Bottom", xAxis);
		
			VertAxis probAxis = new VertAxis(this);
			probAxis.readNumLabels(getParameter(CUM_AXIS_INFO_PARAM));
		thePanel.add("Left", probAxis);
			
			CumulativePlotView theView = new CumulativePlotView(data, this, xAxis, probAxis, "distn",
																															"pseudo", "randomVal");
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		DotPlotView randomRectPlot = new DotPlotView(data, this, probAxis, 1.0);
		randomRectPlot.setActiveNumVariable("pseudo");
		thePanel.add("LeftMargin", randomRectPlot);
		
		DotPlotView randomNormPlot = new DotPlotView(data, this, xAxis, 1.0);
		randomNormPlot.setActiveNumVariable("randomVal");
		thePanel.add("BottomMargin", randomNormPlot);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				takeSampleButton = new XButton(translate("Take sample"), this);
			samplePanel.add(takeSampleButton);
			
		thePanel.add("West", samplePanel);
		
			bSlider = new BSlider(this);
		
		thePanel.add("Center", bSlider);
		
			XPanel sampSizePanel = new XPanel();
			sampSizePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				
				normalDistnButton = new XButton(translate("Normal distn"), this);
			
			sampSizePanel.add(normalDistnButton);
				
				sampleSizeChoice = new XChoice(this);
				for (int i=0 ; i<sampleSize.length; i++)
					sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
				sampleSizeChoice.select(currentSizeIndex);
			
			sampSizePanel.add(sampleSizeChoice);
		
		thePanel.add("East", sampSizePanel);
			
		return thePanel;
	}
	
	private void changeSampleSize(int newChoice) {
		currentSizeIndex = newChoice;
		int noOfValues = sampleSize[currentSizeIndex];
		
		summaryData.changeSampleSize(noOfValues);
		selectLowestValue();
	}
	
	private void selectLowestValue() {
		NumVariable rectVar = (NumVariable)data.getVariable("pseudo");
		int[] sortedIndex = rectVar.getSortedIndex();
		data.setSelection(sortedIndex[0]);
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			selectLowestValue();
			return true;
		}
		else if (target == normalDistnButton) {
			bSlider.setValue(0);
			return true;
		}
		else if (target == bSlider) {
			int bIndex = bSlider.getValue();
			
			double bNormal = distn.getBNormal();
			double bMin = distn.getBMin();
			double bMax = distn.getBMax();
			
			double newB = bNormal - ((bIndex < 0) ? (bIndex / 100.0 * (bMax - bNormal))
															: (bIndex / 100.0 * (bNormal - bMin)));
			distn.setB(newB);
			data.variableChanged("distn");
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSizeIndex)
				changeSampleSize(newChoice);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}