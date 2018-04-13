package pairBlockProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import valueList.*;

import ssq.*;
import pairBlock.*;


public class MeanBlockComparisonApplet extends MeanBlockCisApplet {
	static final private String DECIMALS_PARAM = "decimals";
	static final private String MAX_INTERVAL_PARAM = "maxInterval";
	static final private String SHORT_X_LABELS_PARAM = "shortXLabels";
	static final private String SHORT_X_VAR_NAME_PARAM = "shortXVarName";
	static final private String X_VALUES_PARAM = "xValues";
	
	static final private Color kBackgroundColor = new Color(0xEEEEEE);
	
	private GroupMeansDataSet meanData;
	private SliceMeanView sliceView;
	
	public void setupApplet() {
		data = readData();
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		meanData = new GroupMeansDataSet(data, "y", "x", "shortX", decimals,
																							GroupMeansDataSet.SORT_HIGH_FIRST);
		CoreVariable meanVar = meanData.getVariable("mean");
		meanVar.name = translate("Mean");
		
		setLayout(new BorderLayout(20, 10));
	
		add("Center", displayPanel(data));
		add("East", meanListPanel(meanData));
		add("South", controlPanel());
	}
	
	protected TwoTreatDataSet readData() {
		TwoTreatDataSet data = super.readData();
		
			String shortXVarName = getParameter(SHORT_X_VAR_NAME_PARAM);
			if (shortXVarName == null)
				shortXVarName = data.getXVarName();
			CatVariable shortXVar = new CatVariable(shortXVarName);
			shortXVar.readLabels(getParameter(SHORT_X_LABELS_PARAM));
			shortXVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("shortX", shortXVar);
		
		return data;
	}
	
	protected XPanel titlePanel(TwoTreatDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel titleLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
//			XLabel titleLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
		
		thePanel.add("West", titleLabel);
		thePanel.add("Center", new XPanel());
		
//			XLabel meansLabel = new XLabel(translate("Means"), XLabel.RIGHT, this);
//			meansLabel.setFont(getStandardBoldFont());
//		
//		thePanel.add("East", meansLabel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(TwoTreatDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
		thePanel.add("North", titlePanel(data));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				NumCatAxis treatAxis = new VertAxis(this);
//				NumCatAxis treatAxis = new HorizAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
				treatAxis.setCatLabels(xVar);
//				treatAxis.setAxisName(data.getXVarName());
			
//			dataPanel.add("Bottom", treatAxis);
			dataPanel.add("Left", treatAxis);
			
//				NumCatAxis numAxis = new VertAxis(this);
				NumCatAxis numAxis = new HorizAxis(this);
				numAxis.readNumLabels(data.getYAxisInfo());
				numAxis.setAxisName(data.getYVarName());
			
//			dataPanel.add("Left", numAxis);
			dataPanel.add("Bottom", numAxis);
			
				double jitter = Double.parseDouble(getParameter(JITTER_PARAM));
				dataView = new TreatBlockCIView(data, this, "x", "z", "lsX", numAxis, treatAxis, jitter);
				dataView.setActiveNumVariable("y");
				dataView.setCrossSize(DataView.LARGE_CROSS);
				dataView.setShowBlocks(false);
				dataView.setCIType(TreatBlockCIView.MEAN_ONLY);
				dataView.lockBackground(Color.white);
				
			dataPanel.add("Center", dataView);
			
			addMarginView(dataPanel, numAxis);
		
		thePanel.add("Center", dataPanel);
		
		return thePanel;
	}
	
	protected void addMarginView(XPanel dataPanel, NumCatAxis numAxis) {
		double minStartSelection = 0.75 * numAxis.minOnAxis + 0.25 * numAxis.maxOnAxis;
		double maxStartSelection = 0.25 * numAxis.minOnAxis + 0.75 * numAxis.maxOnAxis;
		
		sliceView = new SliceMeanView(meanData, this, numAxis, minStartSelection, maxStartSelection,
																																	"mean", "shortGroupName");
		sliceView.lockBackground(kBackgroundColor);
		dataPanel.add((numAxis instanceof VertAxis) ? "RightMargin" : "BottomMargin", sliceView);
		
		meanData.setSelection("mean", minStartSelection, maxStartSelection);
		
		changeBlockDisplay(false);
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
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
			leftPanel.add(super.controlPanel());
		
		thePanel.add(leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
				XLabel rangeLabel = new XLabel(translate("Max difference (95% prob)"), XLabel.LEFT, this);
				rangeLabel.setFont(getStandardBoldFont());
			rightPanel.add(rangeLabel);
			
				NumValue maxValue = new NumValue(getParameter(MAX_INTERVAL_PARAM));
				MultiComparisonRangeView rangeView = new MultiComparisonRangeView(meanData, this, maxValue);
				rangeView.setLabel(null);
			rightPanel.add(rangeView);
		
		thePanel.add(rightPanel);
		
		return thePanel;
	}
	
	protected void changeBlockDisplay(boolean useBlocks) {
		dataView.setShowBlocks(useBlocks);
		dataView.repaint();
		
		int residDf = dataView.getResidDf(useBlocks);
		double residSd = Math.sqrt(dataView.getResidSsq(useBlocks) / residDf);
		
			CatVariable groups = (CatVariable)data.getVariable("x");
		int nMeans = groups.noOfCategories();
		int nPerGroup = groups.noOfValues() / nMeans;
		
		double intervalWidth = StudentizedRangeTable.quantile(0.95, residDf, nMeans)
																											* residSd * Math.sqrt(1.0 / nPerGroup);
		
		double oldCenter = 0.5 * (meanData.getMinSelection() + meanData.getMaxSelection());
		double minSelection = oldCenter - intervalWidth * 0.5;
		double maxSelection = oldCenter + intervalWidth * 0.5;
		sliceView.setSelection(minSelection, maxSelection);
		meanData.setSelection("mean", minSelection, maxSelection);
		
		meanData.valueChanged(0);
	}
}