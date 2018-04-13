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

public class ClusterSampleApplet extends XApplet {
	static final private String VERT_AXIS_PARAM = "vertAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String MEAN_PARAM = "mean";
//	static final private String CLUSTER_SIZE_PARAM = "clusterSize";
	static final private String CLUSTER_CORR_PARAM = "clusterCorr";
	static final private String CLUSTER_SAMPLE_PARAM = "clustersPerSample";
	static final private String MEAN_DISTN_PARAM = "meanDistn";
	
	static final private int kMaxSampleSize = 1000;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private ExpandingTimeAxis timeAxis;
	private JitterPlusNormalView meanDotPlot;
	
	private XButton sampleButton, resetButton;
	private ParameterSlider clusterSlider;
	
	private int decimals;
	private NumValue minCorr, maxCorr, startCorr;
	
	private boolean usesFixedMeanDistn;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(20, 0));
		
		add("Center", displayPanel(data, summaryData));
		add("South", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomNormal generator = new RandomNormal(randomInfo);
		double mean = Double.parseDouble(getParameter(MEAN_PARAM));
		
		StringTokenizer st = new StringTokenizer(getParameter(CLUSTER_CORR_PARAM));
		minCorr = new NumValue(st.nextToken());
		maxCorr = new NumValue(st.nextToken());
		startCorr = new NumValue(st.nextToken());
		
		ClusterSampleVariable y = new ClusterSampleVariable(getParameter(VAR_NAME_PARAM),
																				generator, mean, 1.0 - startCorr.toDouble(), decimals);
		String clusterString = getParameter(CLUSTER_SAMPLE_PARAM);
		if (clusterString != null)
			y.setClustersPerSample(Integer.parseInt(clusterString));
		data.addVariable("y", y);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "y");
		
		summaryData.addVariable("clusterMean", new ClusterMeanVariable("Cluster mean", "y", decimals));
		
			NormalDistnVariable normal = new NormalDistnVariable("Normal");
			String normalDistnString = getParameter(MEAN_DISTN_PARAM);
			usesFixedMeanDistn = normalDistnString != null;
			if (usesFixedMeanDistn)
				normal.setParams(normalDistnString);
			
		summaryData.addVariable("normal", normal);
		
		summaryData.setAccumulate(true);
		return summaryData;
	}

//---------------------------------------------------------------------

	
	private XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel headerPanel = new XPanel();
			headerPanel.setLayout(new BorderLayout());
				CoreVariable y = data.getVariable("y");
				XLabel valueLabel = new XLabel(y.name, XLabel.LEFT, this);
				valueLabel.setFont(getBigBoldFont());
				XLabel meanLabel = new XLabel(translate("Means"), XLabel.RIGHT, this);
				meanLabel.setFont(getBigBoldFont());
			headerPanel.add("Center", valueLabel);
			headerPanel.add("East", meanLabel);
		
		thePanel.add("North", headerPanel);
		
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new AxisLayout());
			
				timeAxis = new ExpandingTimeAxis(this, 10, kMaxSampleSize);
				timeAxis.setAxisName(translate("Samples"));
				
			graphPanel.add("Bottom", timeAxis);
			
			
				VertAxis vertAxis = new VertAxis(this);
				String labelInfo = getParameter(VERT_AXIS_PARAM);
				vertAxis.readNumLabels(labelInfo);
			graphPanel.add("Left", vertAxis);
			
				ClusterSampleView theView = new ClusterSampleView(data, this, timeAxis, vertAxis, "y", summaryData);
			
				theView.lockBackground(Color.white);
			graphPanel.add("Center", theView);
			
				meanDotPlot = new ClusterPlusNormalView(summaryData, this, vertAxis, "normal", 1.0, data, "y", usesFixedMeanDistn);
				meanDotPlot.setMinDisplayWidth(50);
				meanDotPlot.lockBackground(new Color(0xEEEEEE));
			graphPanel.add("RightMargin", meanDotPlot);
		
		thePanel.add("Center", graphPanel);
		
		return thePanel;
	}

	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			clusterSlider = new ParameterSlider(minCorr, maxCorr, startCorr,
																									translate("Correlation within sample"), this);
		thePanel.add("Center", clusterSlider);
			
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XPanel innerButtonPanel = new XPanel();
				innerButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
					
					sampleButton = new XButton(translate("Take sample"), this);
				innerButtonPanel.add(sampleButton);
				
					resetButton = new XButton(translate("Reset"), this);
				innerButtonPanel.add(resetButton);
				
			buttonPanel.add(innerButtonPanel);
		
		thePanel.add("East", buttonPanel);
		
		return thePanel;
	}

//----------------------------------------------------------------
	
	private void clearSample() {
		summaryData.clearData();
		summaryData.variableChanged("clusterMean");
		
		ClusterSampleVariable y = (ClusterSampleVariable)data.getVariable("y");
		y.clearSample();
		
		timeAxis.setNoOfValues(0);
		data.variableChanged("y");
	}
	
	protected void doTakeSample() {
		ClusterSampleVariable y = (ClusterSampleVariable)data.getVariable("y");
		int noOfSamples = y.getNoOfClusters() / y.getClustersPerSample();
		timeAxis.setNoOfValues(noOfSamples + 1);
		
		summaryData.takeSample();
	}
	
	protected void setClusterCorr() {
		double corr = clusterSlider.getParameter().toDouble();
		ClusterSampleVariable y = (ClusterSampleVariable)data.getVariable("y");
		y.setProportion(1.0 - corr);
		data.variableChanged("y");
		
		ClusterMeanVariable means = (ClusterMeanVariable)summaryData.getVariable("clusterMean");
		means.recalculateMeans(data);
		
		meanDotPlot.repaint();
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
		else if (target == clusterSlider) {
			setClusterCorr();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}