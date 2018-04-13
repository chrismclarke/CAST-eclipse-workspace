package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import coreGraphics.*;
import models.*;

import ssq.*;


public class GroupSsqDistnApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final protected String CHI2_AXIS_INFO_PARAM = "chi2Axis";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final protected String AREA_PROPN_PARAM = "areaProportion";
	
	protected String kComponentNames[];
	
	protected GroupsDataSet data;
	protected AnovaSummaryData summaryData;
	protected NumValue maxSsq, maxMsq, maxF, maxRSquared;
	
	private HorizAxis groupAxis;
	private VertAxis yAxis;
	
//	private int noOfValues;
	
	private RepeatingButton sampleButton;
	
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	private int sampleSize[];
	
	private XChoice nGroupsChoice;
	
	protected StackedPlusNormalView chi2DistnView;
	
	public void setupApplet() {
		kComponentNames = new String[3];
		kComponentNames[0] = translate("Total");
		kComponentNames[1] = translate("Between groups");
		kComponentNames[2] = translate("Within groups");
		
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(0.65, 10, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 5));
			topPanel.add("Center", dataPanel(data));
		
			topPanel.add("East", controlPanel(data));
			topPanel.add("South", anovaTable(summaryData));
		
		add(ProportionLayout.TOP, topPanel);
			
		add(ProportionLayout.BOTTOM, chi2DistnPanel(summaryData));
	}
	
	private GroupsDataSet getData() {
		GroupsDataSet data = new GroupsDataSet(this);
		data.addBasicComponents();
		return data;
	}
	
	
	protected AnovaSummaryData getSummaryData(DataSet sourceData) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		
		AnovaSummaryData summaryData = new AnovaSummaryData(sourceData, "error",
								BasicComponentVariable.kComponentKey, maxSsq.decimals, maxRSquared.decimals);
		
			Chi2DistnVariable chi2Distn = new Chi2DistnVariable("Chi-squared distn");
		summaryData.addVariable("chi2Distn", chi2Distn);
		
			adjustDF(summaryData);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private void adjustDF(AnovaSummaryData summaryData) {
		BasicComponentVariable betweenComponent = (BasicComponentVariable)data.
																				getVariable(BasicComponentVariable.kComponentKey[1]);
		int betweenDF = betweenComponent.getDF();
		
		Chi2DistnVariable chi2Distn  = (Chi2DistnVariable)summaryData.getVariable("chi2Distn");
		chi2Distn.setDF(betweenDF);
		
		if (chi2DistnView != null) {
			adjustDistnLabel(chi2DistnView, betweenDF);
			int selectedIndex = summaryData.getSelection().findSingleSetFlag();
			summaryData.variableChanged("chi2Distn", selectedIndex);
		}
	}
	
	private void adjustDistnLabel(DataPlusDistnInterface chi2View, int betweenDF) {
		chi2View.setDistnLabel(new LabelValue(translate("Chi-squared") + "(" + betweenDF + " df)"), Color.gray);
	}
	
	private XPanel dataPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			yAxis = new VertAxis(this);
			groupAxis = new HorizAxis(this);
			updateAxes();
			
		thePanel.add("Left", yAxis);
		thePanel.add("Bottom", groupAxis);
		
			DataView dataView = new DataWithComponentView(data, this, groupAxis, yAxis,
														"x", "y", "ls", "model", BasicComponentVariable.EXPLAINED);
			
			dataView.setActiveNumVariable("y");
			dataView.lockBackground(Color.white);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private void updateAxes() {
		yAxis.readNumLabels(data.getYAxisInfo());
		yAxis.repaint();
		
		CatVariable xVar = (CatVariable)data.getVariable("x");
		groupAxis.setCatLabels(xVar);
		groupAxis.repaint();
	}
	
	private XPanel chi2DistnPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(CHI2_AXIS_INFO_PARAM));
			horizAxis.setAxisName(translate("Between groups sum of squares"));
		thePanel.add("Bottom", horizAxis);
		
			chi2DistnView = new StackedPlusNormalView(summaryData, this,
									horizAxis, "chi2Distn", StackedPlusNormalView.ACCURATE_STACK_ALGORITHM);
			chi2DistnView.setActiveNumVariable(BasicComponentVariable.kComponentKey[1]);
			double areaProportion = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
			chi2DistnView.setAreaProportion(areaProportion);
			
			chi2DistnView.lockBackground(Color.white);
			
			Chi2DistnVariable chi2Distn = (Chi2DistnVariable)summaryData.getVariable("chi2Distn");
			adjustDistnLabel(chi2DistnView, chi2Distn.getDF());
		
		thePanel.add("Center", chi2DistnView);
		
		return thePanel;
	}
	
	protected AnovaTableView anovaTable(AnovaSummaryData summaryData) {
		AnovaTableView table = new AnovaTableView(summaryData, this,
									BasicComponentVariable.kComponentKey, maxSsq, maxMsq, maxF,
									AnovaTableView.SSQ_AND_MSSQ);
		table.setComponentNames(kComponentNames);
		table.setFont(getBigFont());
		return table;
	}
	
	private XChoice getSampleSizeChoice() {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int noOfSizes = st.countTokens();
		sampleSize = new int[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++) {
			String nextSize = st.nextToken();
			boolean isInitialSize = nextSize.startsWith("*");
			if (isInitialSize) {
				nextSize = nextSize.substring(1);
				currentSampleSizeIndex = i;
			}
			sampleSize[i] = Integer.parseInt(nextSize);
		}
//		noOfValues = sampleSize[currentSampleSizeIndex];
		
		XChoice choice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length; i++)
			choice.addItem(String.valueOf(sampleSize[i] + " " + translate("per group")));
		choice.select(currentSampleSizeIndex);
		return choice;
	}
	
	private XPanel controlPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		nGroupsChoice = data.dataSetChoice(this);
		thePanel.add(nGroupsChoice);
		
		sampleSizeChoice = getSampleSizeChoice();
		thePanel.add(sampleSizeChoice);
		
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		return thePanel;
	}
	
	private void changeNoPerGroup(int nPerGroup) {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int newCounts[] = new int[xVar.noOfCategories()];
		for (int i=0 ; i<newCounts.length ; i++)
			newCounts[i] = nPerGroup;
		xVar.setCounts(newCounts);
		summaryData.changeSampleSize(newCounts.length * nPerGroup);
//		summaryData.takeSample();
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSampleSizeIndex) {
				currentSampleSizeIndex = newChoice;
				changeNoPerGroup(sampleSize[currentSampleSizeIndex]);
			}
			return true;
		}
		else if (target == nGroupsChoice) {
			int newChoice = nGroupsChoice.getSelectedIndex();
			if (data.changeDataSet(newChoice)) {
				changeNoPerGroup(sampleSize[currentSampleSizeIndex]);
				updateAxes();
				data.variableChanged("x");
				
				adjustDF(summaryData);
				summaryData.variableChanged(BasicComponentVariable.kComponentKey[1], 0);
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