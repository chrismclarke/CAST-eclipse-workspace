package varianceProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import distn.*;
import models.*;
import formula.*;
import imageUtils.*;

import ssq.*;
import variance.*;


public class TwoGroupFApplet extends XApplet {
	static final protected String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final protected String SUMMARY_AXIS_PARAM = "summaryAxis";
	static final protected String DF_PARAM = "df";
	
	private GroupsDataSet data;
	private SummaryDataSet summaryData;
	protected NumValue maxSsq, maxMsq, maxF, maxRSquared;
	
	protected int numerDF, denomDF;
	
	private XCheckbox accumulateCheck;
	private RepeatingButton sampleButton;
	
	public void setupApplet() {
		AnovaImages.loadGroupImages(this);
		
		data = readData();
		
		summaryData = getSummaryData(data);
		if (data.getVariable("error") == null)
			summaryData.setSingleSummaryFromData();
		else
			summaryData.takeSample();
		
		setLayout(new ProportionLayout(1.0 - getFProportion(), 20, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		
		add(ProportionLayout.TOP, topPanel(data, summaryData));
		add(ProportionLayout.BOTTOM, bottomPanel(summaryData));
	}
	
	protected GroupsDataSet readData() {
		GroupsDataSet data = new GroupsDataSet(this);
		
		TwoGroupComponentVariable.addComponentsToData(data, "x", "y", "ls");
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
												TwoGroupComponentVariable.kComponentKey, maxSsq.decimals,
												maxRSquared.decimals);
		
			String var0Key = TwoGroupComponentVariable.kComponentKey[TwoGroupComponentVariable.WITHIN_0];
			SsqVariable mss = new SsqVariable("Group 1 var", data, var0Key, maxMsq.decimals, SsqVariable.MEAN_SSQ);
		summaryData.addVariable("var0", mss);
		
			String var1Key = TwoGroupComponentVariable.kComponentKey[TwoGroupComponentVariable.WITHIN_1];
			mss = new SsqVariable("Group 2 var", data, var1Key, maxMsq.decimals, SsqVariable.MEAN_SSQ);
		summaryData.addVariable("var1", mss);
		
			SsqRatioVariable f = new SsqRatioVariable("F ratio", var1Key,
										var0Key, maxF.decimals, SsqRatioVariable.MEAN_SSQ);
		summaryData.addVariable("F", f);

		st = new StringTokenizer(getParameter(DF_PARAM));
		numerDF = Integer.parseInt(st.nextToken());
		denomDF = Integer.parseInt(st.nextToken());
		
		summaryData.addVariable("fDistn", new FDistnVariable("F distn", numerDF, denomDF));
		
		return summaryData;
	}
	
	protected double getFProportion() {
		return 0.5;
	}
	
	private XPanel topPanel(GroupsDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		thePanel.add("Center", dataPanel(data));
		thePanel.add("East", summaryValuePanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel bottomPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		thePanel.add("Center", fDistnPanel(summaryData));
		thePanel.add("East", controlPanel(summaryData));
		
		return thePanel;
	}
	
	protected XPanel dataPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis yAxis = new HorizAxis(this);
			String labelInfo = data.getYAxisInfo();
			yAxis.readNumLabels(labelInfo);
			yAxis.setAxisName(data.getVariable("y").name);
		thePanel.add("Bottom", yAxis);
		
			VertAxis theGroupAxis = new VertAxis(this);
			CatVariable groupVariable = data.getCatVariable();
			theGroupAxis.setCatLabels(groupVariable);
		thePanel.add("Left", theGroupAxis);
		
			DataView theView = new TwoGroupSpreadDotView(data, this, yAxis, theGroupAxis, "y", "x");
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel fDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis ssqAxis = new HorizAxis(this);
			ssqAxis.readNumLabels(getParameter(SUMMARY_AXIS_PARAM));
			ssqAxis.setAxisName(translate("F ratio"));
		thePanel.add("Bottom", ssqAxis);
		
			StackedPlusNormalView theView = new StackedPlusNormalView(summaryData, this, ssqAxis, "fDistn",
																								StackedPlusNormalView.STACK_ALGORITHM);
			theView.setActiveNumVariable("F");
			theView.setDistnLabel(new LabelValue("F(" + numerDF + ", " + denomDF + ")"), Color.gray);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel summaryValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			String var2Key = "var1";
			Color var2Color = TwoGroupComponentVariable.kComponentColor[TwoGroupComponentVariable.WITHIN_1];
			OneValueImageView var2View = new OneValueImageView(summaryData, var2Key,
													this, "xEquals/s2Sqr.png", 17, maxMsq);
			var2View.setForeground(var2Color);
			var2View.setHighlightSelection(false);
		
		thePanel.add(var2View);
		
			String var1Key = "var0";
			Color var1Color = TwoGroupComponentVariable.kComponentColor[TwoGroupComponentVariable.WITHIN_0];
			OneValueImageView var1View = new OneValueImageView(summaryData, var1Key,
													this, "xEquals/s1Sqr.png", 17, maxMsq);
			var1View.setForeground(var1Color);
			var1View.setHighlightSelection(false);
		
		thePanel.add(var1View);
		
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
		thePanel.add(new FCalcPanel(summaryData, var2Key, var1Key,
																				"F", maxMsq, maxF, var2Color, var1Color, stdContext));
		return thePanel;
	}
	
	protected XPanel controlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
			
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		
		thePanel.add(sampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
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