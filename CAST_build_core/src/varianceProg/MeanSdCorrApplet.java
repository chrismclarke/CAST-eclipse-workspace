package varianceProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import distn.*;
import random.*;
import utils.*;
import coreGraphics.*;
import coreSummaries.*;
import imageGroups.*;
import imageUtils.*;
import corr.*;


public class MeanSdCorrApplet extends XApplet {
	static final private String NORMAL_PARAM = "random";
	static final private String MAX_SUMMARY_PARAM = "maxSummary";
	static final private String AXIS_PARAM = "horizAxis";
	static final private String SD_AXIS_PARAM = "sdAxis";
	static final private String MEAN_AXIS_PARAM = "meanAxis";
	static final private String MEAN_NAME_PARAM = "meanName";
	static final private String SD_NAME_PARAM = "sdName";
	
	static final private Color kNormalColor = new Color(0xDDDDFF);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private NumValue maxSummary;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(0.5, 10));
		
		add(ProportionLayout.LEFT, leftPanel(data, summaryData));
		add(ProportionLayout.RIGHT, rightPanel(summaryData));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			RandomNormal generator = new RandomNormal(getParameter(NORMAL_PARAM));
			NumSampleVariable yVar = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 9);
			
		data.addVariable("y", yVar);
		
			NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
			StringTokenizer st = new StringTokenizer(getParameter(NORMAL_PARAM));
			@SuppressWarnings("unused")
			int noOfValues = Integer.parseInt(st.nextToken());
			NumValue modelMean = new NumValue(st.nextToken());
			NumValue modelSD = new NumValue(st.nextToken());
			dataDistn.setParams(modelMean.toString() + " " + modelSD.toString());
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "y");
		
			maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
		
			SDVariable sdVar = new SDVariable(getParameter(SD_NAME_PARAM), "y", maxSummary.decimals);
		summaryData.addVariable("sd", sdVar);
		
		MeanVariable meanVar = new MeanVariable(getParameter(MEAN_NAME_PARAM), "y",
																															maxSummary.decimals);
		summaryData.addVariable("mean", meanVar);
		
		return summaryData;
	}
	
	private XPanel leftPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", dataPanel(data));
		
		thePanel.add("South", controlPanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_PARAM));
			theHorizAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			StackedPlusNormalView dataView = new StackedPlusNormalView(data, this, theHorizAxis, "model");
			dataView.setActiveNumVariable("y");
			dataView.setShowDensity(DataPlusDistnInterface.CONTIN_DISTN);
			dataView.setDensityColor(kNormalColor);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 30));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
				
				OneValueImageView meanView = new OneValueImageView(summaryData, "mean", this,
																"xEquals/sampMean.png", MeanSDImages.kParamAscent, maxSummary);
				meanView.setHighlightSelection(false);
				meanView.setForeground(Color.blue);
			
			topPanel.add(meanView);
			
				OneValueImageView sdView = new OneValueImageView(summaryData, "sd", this,
																"xEquals/sampSD.png", MeanSDImages.kParamAscent, maxSummary);
				sdView.setHighlightSelection(false);
				sdView.setForeground(Color.red);
			
			topPanel.add(sdView);
		
		thePanel.add(topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				
				takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			bottomPanel.add(takeSampleButton);
			
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			bottomPanel.add(accumulateCheck);
		
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
	
	private XPanel rightPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("Center", scatterPanel(summaryData));
		
			XPanel corrPanel = new XPanel();
			corrPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				CorrelationView rView = new CorrelationView(summaryData, "sd", "mean", CorrelationView.NO_FORMULA, this);
				rView.setFont(getBigFont());
				
			corrPanel.add(rView);
		
		thePanel.add("South", corrPanel);
		
		return thePanel;
	}
	
	private XPanel scatterPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(SD_AXIS_PARAM));
				vertAxis.setForeground(Color.red);
			plotPanel.add("Left", vertAxis);
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(MEAN_AXIS_PARAM));
				horizAxis.setAxisName(getParameter(MEAN_NAME_PARAM));
				horizAxis.setForeground(Color.blue);
			plotPanel.add("Bottom", horizAxis);
			
				ScatterView dataView = new ScatterView(summaryData, this, horizAxis, vertAxis, "mean", "sd");
				dataView.lockBackground(Color.white);
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
		
			XLabel topVarName = new XLabel(getParameter(SD_NAME_PARAM), XLabel.LEFT, this);
				topVarName.setForeground(Color.red);
		thePanel.add("North", topVarName);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}