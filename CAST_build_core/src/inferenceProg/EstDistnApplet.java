package inferenceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;
import imageGroups.*;

import randomStat.*;
//import sampling.*;


abstract public class EstDistnApplet extends XApplet {
	static final protected String MEAN_NAME_PARAM = "meanName";
	static final protected String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	static final protected Color kPaleBlueColor = new Color(0x99CCFF);
	
	protected SampleMeanSDView dataMeanView, dataSDView, estMeanView, estSDView, dataCountView;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected DataView summaryView, dataView;
	protected HorizAxis dataAxis, summaryAxis;
	
	private XCheckbox showTheoryCheck;
	
	protected String varName[];
	protected String meanName[];
	protected int displayDecimals[];
	protected String axisInfo[];
	
	private XChoice dataSetChoice;
	protected int dataSetSelection = 0;
	
	protected double dataMean, dataSD;
	
	public void setupApplet() {
		FitEstImages.loadFitEst(this);
		MeanSDImages.loadMeanSD(this);
		
		data = getData();
		summaryData = getSummaryData(data);
		setSummaryInfo(summaryData, "mean", "theory", data, "y", displayDecimals[0], meanName[0]);
		
		setLayout(new BorderLayout());
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		dataPanel.add(ProportionLayout.TOP, dataPanel(data, "y"));
		dataPanel.add(ProportionLayout.BOTTOM, summaryPanel(summaryData, "mean", "theory"));
		
		add("East", paramPanel(data, "fit", summaryData, "theory"));
		
		add("Center", dataPanel);
		add("South", controlPanel());
	}
	
	protected int readCoreData() {
		int noOfDataSets = 0;
		while (true) {
			String suffix = (noOfDataSets == 0) ? "" : String.valueOf(noOfDataSets+1);
			String temp = getParameter(VAR_NAME_PARAM + suffix);
			if (temp == null) {
//				System.out.println("Didn't find: " + VAR_NAME_PARAM + suffix);
				break;
			}
			else {
//				System.out.println("Found: " + VAR_NAME_PARAM + suffix);
				noOfDataSets ++;
			}
		}
		varName = new String[noOfDataSets];
		axisInfo = new String[noOfDataSets];
		displayDecimals = new int[noOfDataSets];
		meanName = new String[noOfDataSets];
		for (int i=0 ; i<noOfDataSets ; i++) {
			String suffix = (i == 0) ? "" : String.valueOf(i+1);
			varName[i] = getParameter(VAR_NAME_PARAM + suffix);
			axisInfo[i] = getParameter(AXIS_INFO_PARAM + suffix);
			displayDecimals[i] = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM + suffix));
			meanName[i] = getParameter(MEAN_NAME_PARAM + suffix);
		}
		return noOfDataSets;
	}
	
	abstract protected DataSet getData();
	
	abstract protected void setEstimatedDistn(DataSet data, String varKey, String estKey,
																										int decimals);
	
	abstract protected SummaryDataSet getSummaryData(DataSet sourceData);
	
	abstract protected void setSummaryInfo(SummaryDataSet summaryData, String sumValueKey,
						String sumTheoryKey, DataSet sourceData, String sourceVarKey, int decimals,
						String summaryName);
	
	abstract protected int getMeanType();
	abstract protected int getSDType();
	
	private XPanel paramPanel(DataSet data, String fitKey, SummaryDataSet summaryData,
																								String meanEstKey) {
		int meanType = getMeanType();
		int sdType = getSDType();
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		XPanel fitPanel = new XPanel();
		fitPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
		
		if (sdType != SampleMeanSDView.SD)  {
			dataCountView = new SampleMeanSDView(data, this, "y", SampleMeanSDView.COUNT, SampleMeanSDView.SAMPLE_DISTN);
			fitPanel.add(dataCountView);
		}
		
		dataMeanView = new SampleMeanSDView(data, this, fitKey, meanType, SampleMeanSDView.SAMPLE_DISTN);
		fitPanel.add(dataMeanView);
		
		if (sdType == SampleMeanSDView.SD) {
			dataSDView = new SampleMeanSDView(data, this, fitKey, sdType, SampleMeanSDView.SAMPLE_DISTN);
			dataSDView.show(false);
			fitPanel.add(dataSDView);
		}
		
		XPanel meanEstPanel = new XPanel();
		meanEstPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
		
		estMeanView = new SampleMeanSDView(summaryData, this, meanEstKey, meanType,
											SampleMeanSDView.MEAN_DISTN);
		estMeanView.show(false);
		meanEstPanel.add(estMeanView);
		
		estSDView = new SampleMeanSDView(summaryData, this, meanEstKey, sdType, SampleMeanSDView.MEAN_DISTN);
		estSDView.show(false);
		meanEstPanel.add(estSDView);
		
		thePanel.add(ProportionLayout.TOP, fitPanel);
		thePanel.add(ProportionLayout.BOTTOM, meanEstPanel);
		
		return thePanel;
	}
	
	abstract protected XPanel dataPanel(DataSet data, String variableKey);
	
	abstract protected DataPlusDistnInterface getSummaryView(SummaryDataSet summaryData,
																			String variableKey, String modelKey);
	
	protected XPanel summaryPanel(SummaryDataSet summaryData, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		summaryAxis = getAxis(summaryData, variableKey);
		thePanel.add("Bottom", summaryAxis);
		
		DataPlusDistnInterface tempView = getSummaryView(summaryData, variableKey, modelKey);
		summaryView = (DataView)tempView;
		thePanel.add("Center", summaryView);
		summaryView.lockBackground(Color.white);
		
		tempView.setShowDensity(DataPlusDistnInterface.NO_DISTN);
		tempView.setDensityColor(kPaleBlueColor);
		
		return thePanel;
	}
	
	protected HorizAxis getAxis(DataSet data, String variableKey) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	private void setTheoryShow(boolean theoryShow) {
		((DataPlusDistnInterface)summaryView).setShowDensity(theoryShow ? DataPlusDistnInterface.CONTIN_DISTN : DataPlusDistnInterface.NO_DISTN);
		if (dataSDView != null)
			dataSDView.show(theoryShow);
		estMeanView.show(theoryShow);
		estSDView.show(theoryShow);
	}
	
	abstract protected String checkBoxName();
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		showTheoryCheck = new XCheckbox(checkBoxName(), this);
		thePanel.add(showTheoryCheck);
		thePanel.add(dataChoicePanel());
		
		return thePanel;
	}
	
	protected XPanel dataChoicePanel() {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		if (varName.length > 1) {
			dataSetChoice = new XChoice(this);
			for (int i=0 ; i<varName.length ; i++)
				dataSetChoice.addItem(varName[i]);
			controlPanel.add(dataSetChoice);
		}
		return controlPanel;
	}
	
	abstract protected void changeDataSet(int newChoice);
	
	
	private boolean localAction(Object target) {
		if (target == showTheoryCheck) {
			setTheoryShow(showTheoryCheck.getState());
			return true;
		}
		else if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != dataSetSelection)
				changeDataSet(newChoice);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}