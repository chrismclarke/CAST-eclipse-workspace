package pairBlockProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import models.*;
import formula.*;
import multiRegn.*;
import ssq.*;
import pairBlock.*;

public class ResidComponent2Applet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_DECIMALS_PARAM = "componentDecimals";
	
	static final private int kAllComponents[] = {1, 2, 3};
	static final private int kTreatResidComponents[] = {2, 3};
	static final private int kResidComponent[] = {3};
	
	protected TwoTreatDataSet data;
	private SummaryDataSet summaryData;
	
	private XLabel treatBlockLabel;
	private MultiVertAxis treatBlockAxis;
	protected BlockTreatComponent2View componentPlot;
	
	@SuppressWarnings("unused")
	private NumValue maxSsq, maxMss, maxF, maxRSquared;
	protected ComponentEqnPanel ssqEquation;
	
	private XCheckbox blockEffectCheck, treatGroupingCheck, treatEffectCheck;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", dataPanel(data));
		
		add("South", controlPanel(data, summaryData));
	}
	
	private NumValue[] copyParams(MultipleRegnModel ls) {
		int nParam = ls.noOfParameters();
		NumValue paramCopy[] = new NumValue[nParam];
		for (int i=0 ; i<nParam ; i++)
			paramCopy[i] = new NumValue(ls.getParameter(i));
		return paramCopy;
	}
	
	private TwoTreatDataSet readData() {
		TwoTreatDataSet data = new TwoTreatDataSet(this);
		
			MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		
			MultipleRegnModel lsX = new MultipleRegnModel("X only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
			lsX.updateLSParams("y", data.getXOnlyConstraints());
		data.addVariable("lsX", lsX);
		
			MultipleRegnModel lsZ = new MultipleRegnModel("Z only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
			lsZ.updateLSParams("y", data.getZOnlyConstraints());
		data.addVariable("lsZ", lsZ);
		
		int componentDecimals = Integer.parseInt(getParameter(COMPONENT_DECIMALS_PARAM));
		SeqXZComponentVariable.addComponentsToData(data, "x", "z", "y", "lsX", "lsZ", "ls",
																																componentDecimals);
		
		NumVariable rawY = (NumVariable)data.getVariable("y");
		RemoveBlockVariable adjY = new RemoveBlockVariable(rawY.name, data, "y",
																																				"x", "z", -1);
		data.addVariable("adjustedY", adjY);
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
							SeqXZComponentVariable.kZXComponentKey, maxSsq.decimals, maxRSquared.decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	
	protected XPanel dataPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			treatBlockLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
			treatBlockLabel.setFont(getStandardBoldFont());
		thePanel.add("North", treatBlockLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis yAxis = new HorizAxis(this);
				String labelInfo = data.getYAxisInfo();
				yAxis.readNumLabels(labelInfo);
				yAxis.setAxisName(data.getYVarName());
			innerPanel.add("Bottom", yAxis);
			
				treatBlockAxis = new MultiVertAxis(this, 3);
				CatVariable blockVariable = (CatVariable)data.getVariable("x");
				treatBlockAxis.setCatLabels(blockVariable);
				
				treatBlockAxis.readExtraCatLabels(new CatVariable(""));		//	no labels
				
				CatVariable treatVariable = (CatVariable)data.getVariable("z");
				treatBlockAxis.readExtraCatLabels(treatVariable);
			innerPanel.add("Left", treatBlockAxis);
			
				componentPlot = getDataView(data, yAxis, treatBlockAxis);
				componentPlot.setCrossSize(DataView.LARGE_CROSS);
				componentPlot.lockBackground(Color.white);
			innerPanel.add("Center", componentPlot);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	protected BlockTreatComponent2View getDataView(MultiRegnDataSet data, HorizAxis yAxis,
																														MultiVertAxis treatBlockAxis) {
		return new BlockTreatComponent2View(data, this, yAxis, treatBlockAxis,
																																	"adjustedY", "x", "z");
	}
	
	
	private XPanel checkPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(30, 8, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
		
			blockEffectCheck = new XCheckbox(translate("Remove block effect"), this);
		thePanel.add(blockEffectCheck);
	
			treatGroupingCheck = new XCheckbox(translate("Group by treatments"), this);
			treatGroupingCheck.disable();
		thePanel.add(treatGroupingCheck);
	
			treatEffectCheck = new XCheckbox(translate("Remove treatment effect"), this);
			treatEffectCheck.disable();
		thePanel.add(treatEffectCheck);
		
		return thePanel;
	}
	
	protected ComponentEqnPanel ssqEquationPanel(MultiRegnDataSet data, int startHilite) {
		AnovaImages.loadBlockImages(this);
	
		FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
		ssqEquation = new ComponentEqnPanel(summaryData, SeqXZComponentVariable.kZXComponentKey, 
						maxSsq, AnovaImages.blockSsqs, SeqXZComponentVariable.kComponentColor,
						AnovaImages.kBlockSsqWidth, AnovaImages.kBlockSsqHeight, bigContext);
		ssqEquation.highlightComponent(startHilite);
		
		return ssqEquation;
	}
	
	
	protected XPanel controlPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 8));
		
		thePanel.add("Center", checkPanel(data, summaryData));
		
		thePanel.add("East", new CatVariableKey(data, this, "z"));
		
			XPanel ssqPanel = new XPanel();
			ssqPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			ssqPanel.add(ssqEquationPanel(data, ComponentEqnPanel.combineComponents(kAllComponents)));
			
		thePanel.add("South", ssqPanel);
		
		return thePanel;
	}
	
	public void checkEnabling() {
		BlockTreatComponent2View c = componentPlot;
		int transitionType = componentPlot.getTransitionType();
		int transitionStage = componentPlot.getTransitionStage();
		
		checkControlEnabling(c, transitionType, transitionStage);
		
		if (transitionType == BlockTreatComponent2View.ADD_REMOVE_BLOCK || transitionType == BlockTreatComponent2View.BLOCK_TO_TREAT && transitionStage == BlockTreatComponent2View.START)
			treatBlockAxis.setAlternateLabels(0);
		else if (transitionType == BlockTreatComponent2View.ADD_REMOVE_TREAT || transitionType == BlockTreatComponent2View.BLOCK_TO_TREAT && transitionStage == BlockTreatComponent2View.END)
			treatBlockAxis.setAlternateLabels(2);
		else
			treatBlockAxis.setAlternateLabels(1);
		
		if (transitionType == BlockTreatComponent2View.BLOCK_TO_TREAT) {
			if (transitionStage == BlockTreatComponent2View.START)
				treatBlockLabel.setText(data.getVariable("x").name);
			else if (transitionStage == BlockTreatComponent2View.END)
				treatBlockLabel.setText(data.getVariable("z").name);
			else
				treatBlockLabel.setText("");
		}
	}
	
	protected void checkControlEnabling(BlockTreatComponent2View c, int transitionType,
																																	int transitionStage) {
		if (transitionStage == BlockTreatComponent2View.MIDDLE) {
			blockEffectCheck.disable();
			treatGroupingCheck.disable();
			treatEffectCheck.disable();
		}
		else if (transitionStage == BlockTreatComponent2View.START) {
			blockEffectCheck.setEnabled(transitionType == BlockTreatComponent2View.ADD_REMOVE_BLOCK
																	|| transitionType == BlockTreatComponent2View.BLOCK_TO_TREAT);
			treatGroupingCheck.setEnabled(transitionType == BlockTreatComponent2View.BLOCK_TO_TREAT
																	|| transitionType == BlockTreatComponent2View.ADD_REMOVE_TREAT);
			treatEffectCheck.setEnabled(transitionType == BlockTreatComponent2View.ADD_REMOVE_TREAT);
		}
		else {		//		transitionStage == END
			blockEffectCheck.setEnabled(transitionType == BlockTreatComponent2View.ADD_REMOVE_BLOCK);
			treatGroupingCheck.setEnabled(transitionType == BlockTreatComponent2View.ADD_REMOVE_BLOCK
																	|| transitionType == BlockTreatComponent2View.BLOCK_TO_TREAT);
			treatEffectCheck.setEnabled(transitionType == BlockTreatComponent2View.BLOCK_TO_TREAT
																	|| transitionType == BlockTreatComponent2View.ADD_REMOVE_TREAT);
		}
	}
	
	protected void frameChanged(DataView theView) {
		checkEnabling();
	}

	
	private boolean localAction(Object target) {
		if (target == blockEffectCheck) {
			int newComponents[] = blockEffectCheck.getState() ? kTreatResidComponents : kAllComponents;
			ssqEquation.highlightComponent(ComponentEqnPanel.combineComponents(newComponents));
			
			componentPlot.animateTransition(BlockTreatComponent2View.ADD_REMOVE_BLOCK);
			
			checkEnabling();
			return true;
		}
		else if (target == treatGroupingCheck) {
			componentPlot.animateTransition(BlockTreatComponent2View.BLOCK_TO_TREAT);
			
			checkEnabling();
			return true;
		}
		else if (target == treatEffectCheck) {
			int newComponents[] = treatEffectCheck.getState() ? kResidComponent : kTreatResidComponents;
			ssqEquation.highlightComponent(ComponentEqnPanel.combineComponents(newComponents));
			
			componentPlot.animateTransition(BlockTreatComponent2View.ADD_REMOVE_TREAT);
			
			checkEnabling();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}