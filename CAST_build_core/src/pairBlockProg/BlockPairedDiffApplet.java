package pairBlockProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import pairBlock.*;

public class BlockPairedDiffApplet extends XApplet {
	static final private String JITTER_PARAM = "jitter";
	static final private String CI_LABEL_NAME_PARAM = "ciLabelName";
	static final private String DIFF_AXIS_INFO_PARAM = "diffAxis";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String BLOCK_NAME_PARAM = "blockName";
	
	private CoreModelDataSet data;
	
	private MultiHorizAxis dataDiffAxis;
	private AnimateBlockDiffView theDotPlot;
	private PairedDifferenceCIView ciPlot;
	
	private XChoice blockDisplayChoice;
	private int currentBlockDisplay = 0;
	
	private XCheckbox showBlocksCheck;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, dataDisplayPanel(data));
		
		add(ProportionLayout.BOTTOM, ciPanel(data));
	}
	
	private CoreModelDataSet readData() {
		return new RemoveBlockDataSet(this, 0);
	}
	
	private String getDiffAxis(String normalLabelInfo, CoreModelDataSet data) {
																//	Shifts axis to give zero mean for treatment 1
		double sy = 0.0;
		int n = 0;
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		CatVariable treatVar = (CatVariable)data.getVariable("x");
		ValueEnumeration ye = yVar.values();
		ValueEnumeration te = treatVar.values();
		while (ye.hasMoreValues() && te.hasMoreValues()) {
			double y = ye.nextDouble();
			int treat = treatVar.labelIndex(te.nextValue());
			if (treat == 0) {
				sy += y;
				n ++;
			}
		}
		double mean = sy / n;
		
		StringTokenizer st = new StringTokenizer(normalLabelInfo);
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		st.nextToken();		//	ignore minLabel
		String stepString = st.nextToken();
		NumValue stepVal = new NumValue(stepString);
		
		double diffMin = - mean;
		double diffMax = diffMin + max - min;
		
		double lowLabel = 0.0;
		while (lowLabel - stepVal.toDouble() >= diffMin)
			lowLabel -= stepVal.toDouble();
		
		NumValue firstLabel = new NumValue(lowLabel, stepVal.decimals);
		
		return diffMin + " " + diffMax + " " + firstLabel.toString() + " " + stepString;
	}
	
	private XPanel dataDisplayPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel groupLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
		thePanel.add("North", groupLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				dataDiffAxis = new MultiHorizAxis(this, 2);
				dataDiffAxis.setChangeMinMax(false);
				String labelInfo = data.getYAxisInfo();
				dataDiffAxis.readNumLabels(labelInfo);
				dataDiffAxis.setAxisName(data.getVariable("y").name);
				dataDiffAxis.readExtraNumLabels(getDiffAxis(labelInfo, data));
			innerPanel.add("Bottom", dataDiffAxis);
			
				VertAxis groupAxis = new VertAxis(this);
				CatVariable groupVariable = (CatVariable)data.getVariable("x");
				groupAxis.setCatLabels(groupVariable);
			innerPanel.add("Left", groupAxis);
			
				double jitter = Double.parseDouble(getParameter(JITTER_PARAM));
				theDotPlot = new AnimateBlockDiffView(data, this, "x", "block", dataDiffAxis, groupAxis, jitter);
				theDotPlot.setActiveNumVariable("y");
				theDotPlot.setCrossSize(DataView.LARGE_CROSS);
				theDotPlot.setShowBlocks(false);
				theDotPlot.setDrawType(BlockDotPlotView.PROFILE_IN_BLOCKS);
				theDotPlot.lockBackground(Color.white);
			innerPanel.add("Center", theDotPlot);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	private XPanel ciPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				showBlocksCheck = new XCheckbox(translate("Show") + " " + getParameter(BLOCK_NAME_PARAM), this);
			choicePanel.add(showBlocksCheck);
			
				blockDisplayChoice = new XChoice(this);
				blockDisplayChoice.addItem(translate("Analysis ignoring differences between") + " " + getParameter(BLOCK_NAME_PARAM));
				blockDisplayChoice.addItem(translate("Correct analysis using paired differences"));
				blockDisplayChoice.disable();
			
			choicePanel.add(blockDisplayChoice);
			
		thePanel.add("North", choicePanel);
		
		thePanel.add("Center", ciDisplayPanel(data));
		
		return thePanel;
	}
	
	private CatVariable createDiffVariable(CatVariable treatVar) {
		int noOfCats = treatVar.noOfCategories();
		Value label[] = new LabelValue[noOfCats - 1];
		
		String treat0Name = treatVar.getLabel(0).toString();
		for (int i=0 ; i<noOfCats-1 ; i++)
			label[i] = new LabelValue(treatVar.getLabel(i + 1).toString() + " - " + treat0Name);
		
		CatVariable diffVar = new CatVariable("");
		diffVar.setLabels(label);
		return diffVar;
	}
	
	private XPanel ciDisplayPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel titleLabel = new XLabel(getParameter(CI_LABEL_NAME_PARAM), XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
		thePanel.add("North", titleLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis diffAxis = new HorizAxis(this);
				diffAxis.readNumLabels(getParameter(DIFF_AXIS_INFO_PARAM));
			innerPanel.add("Bottom", diffAxis);
			
				VertAxis groupAxis = new VertAxis(this);
				CatVariable groupVariable = (CatVariable)data.getVariable("x");
				groupAxis.setCatLabels(createDiffVariable(groupVariable));
			innerPanel.add("Left", groupAxis);
			
				int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
				ciPlot = new PairedDifferenceCIView(data, this, "y", "x", "block", diffAxis, groupAxis, decimals);
				ciPlot.setCiType(PairedDifferenceCIView.TWO_SAMPLE_CIS);
				ciPlot.lockBackground(Color.white);
			innerPanel.add("Center", ciPlot);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	public void finishedAnimation(boolean toDifferences) {
		blockDisplayChoice.enable();
		if (!toDifferences)
			showBlocksCheck.enable();
		ciPlot.setCiType(toDifferences ? PairedDifferenceCIView.PAIRED_CIS
																					: PairedDifferenceCIView.TWO_SAMPLE_CIS);
		ciPlot.repaint();
		
		dataDiffAxis.setAlternateLabels(toDifferences ? 1 : 0);
		if (toDifferences) {
			CatVariable treatVar = (CatVariable)data.getVariable("x");
			String diffName = "Differences from " + treatVar.getLabel(0).toString() + " within " + getParameter(BLOCK_NAME_PARAM);
			dataDiffAxis.setAxisName(diffName);
		}
		else
			dataDiffAxis.setAxisName(data.getVariable("y").name);
		dataDiffAxis.repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == showBlocksCheck) {
			boolean showBlocks = showBlocksCheck.getState();
			if (showBlocks)
				blockDisplayChoice.enable();
			else
				blockDisplayChoice.disable();
			
			theDotPlot.setShowBlocks(showBlocks);
			theDotPlot.repaint();
			
			return true;
		}
		else if (target == blockDisplayChoice) {
			int newBlockDisplay = blockDisplayChoice.getSelectedIndex();
			if (newBlockDisplay != currentBlockDisplay) {
				currentBlockDisplay = newBlockDisplay;
				blockDisplayChoice.disable();
				showBlocksCheck.disable();
				
				theDotPlot.animateDifferences(newBlockDisplay == 1);
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