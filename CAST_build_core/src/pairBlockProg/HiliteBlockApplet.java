package pairBlockProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import valueList.*;

import ssq.*;
import pairBlock.*;

public class HiliteBlockApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String BLOCK_VAR_NAME_PARAM = "blockVarName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	static final private String JITTER_PARAM = "jitter";
	
	static final private String IGNORE_BLOCK_PARAM = "ignoreBlockText";
	static final private String SHOW_BLOCK_PARAM = "showBlockText";
	
	private CoreModelDataSet data;
	private AnovaSummaryData summaryData;
	
	private BlockDotPlotView theDotPlot;
	
	private XChoice blockDisplayChoice;
	private int currentBlockDisplay = 0;
	
	private XPanel anovaPanel;
	private CardLayout anovaPanelLayout;
	
	private NumValue maxSsq, maxMss, maxF, maxRSquared;
	
	public void setupApplet() {
		data = readData();
		
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(10, 10));
		
		add("Center", dataDisplayPanel(data));
		
		add("South", controlPanel(summaryData));
	}
	
	private CoreModelDataSet readData() {
		CoreModelDataSet data = new GroupsDataSet(this);	//	ignore blocks for anova
		
		data.addCatVariable("z", getParameter(BLOCK_VAR_NAME_PARAM),
								getParameter(BLOCK_VALUES_PARAM), getParameter(BLOCK_LABELS_PARAM));
		
		data.addBasicComponents();
		
		return data;
	}
	
	private AnovaSummaryData getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		AnovaSummaryData summaryData =  new AnovaSummaryData(data, "error",
						BasicComponentVariable.kComponentKey, maxSsq.decimals, maxRSquared.decimals);
		
		summaryData.addVariable("pValue", new BlockedPValueVariable(translate("p-value") + " =", data, "y", "x",
																																							"z", 4));
		return summaryData;
	}
	
	private XPanel dataDisplayPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel groupLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
		thePanel.add("North", groupLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis responseAxis = new HorizAxis(this);
				String labelInfo = data.getYAxisInfo();
				responseAxis.readNumLabels(labelInfo);
				responseAxis.setAxisName(data.getVariable("y").name);
			innerPanel.add("Bottom", responseAxis);
			
				VertAxis groupAxis = new VertAxis(this);
				CatVariable groupVariable = (CatVariable)data.getVariable("x");
				groupAxis.setCatLabels(groupVariable);
			innerPanel.add("Left", groupAxis);
			
				double jitter = Double.parseDouble(getParameter(JITTER_PARAM));
				theDotPlot = new BlockDotPlotView(data, this, "x", "z", responseAxis, groupAxis, jitter);
				theDotPlot.setActiveNumVariable("y");
				theDotPlot.setCrossSize(DataView.LARGE_CROSS);
				theDotPlot.setShowBlocks(false);
				theDotPlot.lockBackground(Color.white);
			innerPanel.add("Center", theDotPlot);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 16));
			
			anovaPanel = new XPanel();
			anovaPanelLayout = new CardLayout();
			anovaPanel.setLayout(anovaPanelLayout);
			
			anovaPanel.add("anova", anovaTablePanel(summaryData));
			anovaPanel.add("blank", blockTextPanel());
			
		thePanel.add("Center", anovaPanel);
				
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
					blockDisplayChoice = new XChoice(this);
					blockDisplayChoice.addItem(translate("Analysis ignoring blocks"));
					blockDisplayChoice.addItem(translate("Correct analysis taking account of blocks"));
					
				choicePanel.add(blockDisplayChoice);
				
			thePanel.add("North", choicePanel);
		
		return thePanel;
	}
	
	private XPanel blockTextPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XTextArea messageArea = new XTextArea(getParameter(SHOW_BLOCK_PARAM), 0, 430, this);
			messageArea.lockBackground(Color.white);
		
		thePanel.add("North", messageArea);
		
			XPanel pValPanel = new XPanel();
			pValPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																														VerticalLayout.VERT_CENTER, 3));
																														
				XLabel testLabel = new XLabel("Test for equal treatment means", XLabel.LEFT, this);
				testLabel.setFont(getBigBoldFont());
			pValPanel.add(testLabel);
			
				OneValueView pValView = new OneValueView(summaryData, "pValue", this, new NumValue(1.0, 9));
				pValView.setFont(getBigFont());
			pValPanel.add(pValView);
			
		thePanel.add("Center", pValPanel);
		
		return thePanel;
	}
	
	private XPanel anovaTablePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			XTextArea messageArea = new XTextArea(getParameter(IGNORE_BLOCK_PARAM), 0, 430, this);
			messageArea.lockBackground(Color.white);
		
		thePanel.add("North", messageArea);
		
			XPanel anovaPanel = new XPanel();
			anovaPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
				String componentName[] = new String[3];
				for (int i=0 ; i<3 ; i++)
					componentName[i] = st.nextToken();
			
				AnovaTableView tableView = new AnovaTableView(summaryData, this,
								BasicComponentVariable.kComponentKey, maxSsq, maxMss, maxF, AnovaTableView.SSQ_F_PVALUE);
				tableView.setComponentNames(componentName);
			
			anovaPanel.add(tableView);
		
		thePanel.add("Center", anovaPanel);
		
		return thePanel;
	}
	

	
	private boolean localAction(Object target) {
		if (target == blockDisplayChoice) {
			int newBlockDisplay = blockDisplayChoice.getSelectedIndex();
			if (newBlockDisplay != currentBlockDisplay) {
				currentBlockDisplay = newBlockDisplay;
				boolean showBlocks = newBlockDisplay == 1;
				
				theDotPlot.setShowBlocks(showBlocks);
				theDotPlot.repaint();
				
				anovaPanelLayout.show(anovaPanel, showBlocks ? "blank" : "anova");
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