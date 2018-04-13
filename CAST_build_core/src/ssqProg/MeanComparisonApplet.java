package ssqProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import models.*;
import valueList.*;

import ssq.*;


public class MeanComparisonApplet extends MeanCisApplet {
	static final private String DECIMALS_PARAM = "decimals";
	static final private String MAX_INTERVAL_PARAM = "maxInterval";
	
	static final private Color kBackgroundColor = new Color(0xEEEEEE);
	
	private GroupMeansDataSet meanData;
	private SliceMeanView sliceView;
	
	private XChoice intervalChoice;
	private int currentInterval = 0;
	
	public void setupApplet() {
		data = getData();
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		meanData = new GroupMeansDataSet(data, "y", "x", null, decimals,
																							GroupMeansDataSet.SORT_HIGH_FIRST);
		CoreVariable meanVar = meanData.getVariable("mean");
		meanVar.name = translate("Mean");
		
		setLayout(new BorderLayout(20, 10));
	
		add("Center", displayPanel(data, false));
		add("East", meanListPanel(meanData));
		add("South", controlPanel(data));
	}
	
	protected void addMarginView(XPanel dataPanel, NumCatAxis numAxis) {
		double minStartSelection = 0.75 * numAxis.minOnAxis + 0.25 * numAxis.maxOnAxis;
		double maxStartSelection = 0.25 * numAxis.minOnAxis + 0.75 * numAxis.maxOnAxis;
		
		sliceView = new SliceMeanView(meanData, this, numAxis, minStartSelection, maxStartSelection,
																						"mean", "groupName");
		sliceView.lockBackground(kBackgroundColor);
		dataPanel.add((numAxis instanceof VertAxis) ? "RightMargin" : "BottomMargin", sliceView);
		
		meanData.setSelection("mean", minStartSelection, maxStartSelection);
		
		setIntervalType(true);
	}
	
	
	protected XPanel titlePanel(GroupsDataSet data, boolean yIsHoriz) {
		XPanel thePanel = super.titlePanel(data, yIsHoriz);
		
			XLabel meansLabel = new XLabel(translate("Means"), XLabel.RIGHT, this);
			meansLabel.setFont(getStandardBoldFont());
		
		thePanel.add("East", meansLabel);
		
		return thePanel;
	}
	
	private XPanel meanListPanel(GroupMeansDataSet meanData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			FullValueList listPanel = new FullValueList(meanData, this, FullValueList.HEADING);
			listPanel.addVariableToList("groupName");
			listPanel.addVariableToList("mean");
			listPanel.setCanSelectRows(false);
		
		thePanel.add(listPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
				intervalChoice = new XChoice(this);
				intervalChoice.addItem(translate("Pairwise comparisons"));
				intervalChoice.addItem(translate("Multiple comparisons"));
			
			leftPanel.add(intervalChoice);
		
		thePanel.add(leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
				XLabel rangeLabel = new XLabel(translate("Max difference (5% level)"), XLabel.LEFT, this);
				rangeLabel.setFont(getStandardBoldFont());
			rightPanel.add(rangeLabel);
			
				NumValue maxValue = new NumValue(getParameter(MAX_INTERVAL_PARAM));
				MultiComparisonRangeView rangeView = new MultiComparisonRangeView(meanData, this, maxValue);
				rangeView.setLabel(null);
			rightPanel.add(rangeView);
		
		thePanel.add(rightPanel);
		
		return thePanel;
	}
	
	private void setIntervalType(boolean isPairwise) {
		int pooledDf = dataView.getPooledDf();
		double sPooled = dataView.getPooledSd(pooledDf);
		
			CatVariable groups = (CatVariable)data.getVariable("x");
		int nMeans = groups.noOfCategories();
		int nPerGroup = groups.noOfValues() / nMeans;
		
		double intervalWidth;
		if (isPairwise)
			intervalWidth = TTable.quantile(0.975, pooledDf) * sPooled * Math.sqrt(2.0 / nPerGroup);
		else
			intervalWidth = StudentizedRangeTable.quantile(0.95, pooledDf, nMeans)
																											* sPooled * Math.sqrt(1.0 / nPerGroup);
		
		double oldCenter = 0.5 * (meanData.getMinSelection() + meanData.getMaxSelection());
		double minSelection = oldCenter - intervalWidth * 0.5;
		double maxSelection = oldCenter + intervalWidth * 0.5;
		sliceView.setSelection(minSelection, maxSelection);
		meanData.setSelection("mean", minSelection, maxSelection);
		
		meanData.valueChanged(0);
	}

	
	private boolean localAction(Object target) {
		if (target == intervalChoice) {
			int newInterval = intervalChoice.getSelectedIndex();
			if (newInterval != currentInterval) {
				currentInterval = newInterval;
				setIntervalType(newInterval == 0);
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