package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import imageGroups.*;
import random.RandomNormal;

import sampling.*;


public class PopSampStatsApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String SAMPLING_PARAM = "sampling";
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	
	protected DataSet data;
	protected XButton takeSampleButton;
	
	protected int sampleSize, summaryDecimals;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		TickCrossImages.loadCrossAndTick(this);
		data = getData();
		
		setLayout(new BorderLayout());
		add("South", controlPanel(data));
		add("Center", displayPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		NumVariable y = new NumVariable(getParameter(VAR_NAME_PARAM));
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		if (randomInfo != null) {
			RandomNormal generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			y.setValues(vals);
		}
		else
			y.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", y);
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLING_PARAM));
		sampleSize = Integer.parseInt(st.nextToken());
		long samplingSeed = Long.parseLong(st.nextToken());
		FreqVariable f = new FreqVariable("frequency", y.noOfValues(), samplingSeed);
		int intVal[] = new int[y.noOfValues()];
		f.setValues(intVal);
		data.addVariable("freq", f);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		summaryDecimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(12, 0));
		
		XPanel leftPanel = new XPanel();
		leftPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		leftPanel.add("Top", dotPlotPanel(data, "freq", true));			//		popn
		leftPanel.add("Bottom", dotPlotPanel(data, "freq", false));		//		sample
		thePanel.add("Center", leftPanel);
		
		XPanel rightPanel = new XPanel();
		rightPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		rightPanel.add("Top", summaryPanel(data, "freq", true));				//		popn
		rightPanel.add("Bottom", summaryPanel(data, "freq", false));		//		sample
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}
	
	protected XPanel dotPlotPanel(DataSet data, String freqKey, boolean popNotSamp) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		MeanSDDotPlotView theView = new MeanSDDotPlotView(data, this, theHorizAxis, freqKey, popNotSamp);
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data, String freqKey, boolean popNotSamp) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 5));
		
			XLabel title = new XLabel(popNotSamp ? translate("Popn parameters")
																					: translate("Sample statistics"), XLabel.LEFT, this);
			title.setFont(getStandardBoldFont());
		thePanel.add(title);
		thePanel.add(new SummaryView(data, this, "y", freqKey, SummaryView.MEAN, summaryDecimals, popNotSamp));
		thePanel.add(new SummaryView(data, this, "y", freqKey, SummaryView.SD, summaryDecimals, popNotSamp));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
		takeSampleButton = new XButton(translate("Take sample"), this);
		controlPanel.add(takeSampleButton);
		
		return controlPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			((FreqVariable)data.getVariable("freq")).sample(sampleSize, FreqVariable.WITHOUT_REPLACEMENT);
			data.variableChanged("freq");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}