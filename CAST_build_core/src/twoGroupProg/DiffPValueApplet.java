package twoGroupProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import distn.*;
import qnUtils.*;
import valueList.*;
import models.*;
import coreSummaries.*;
import imageUtils.*;

import test.*;
import twoGroup.*;

public class DiffPValueApplet extends XApplet {
	static final private String MAX_STATS_PARAM = "maxSummary";
	static final private String T_AXIS_PARAM = "tAxis";
	static final private String ALTERNATIVE_PARAM = "alternative";
	
	static final private NumValue kZero = new NumValue(0.0, 0);
	static final private Color kDarkGreenColor = new Color(0x006600);
	
	private RepeatingButton takeSampleButton;
	
	private DiffHypothesisTest test;
	private NumValue maxT, maxPValue;
	private int df;
	private int testTail;
	
	private CoreModelDataSet data;
	private SummaryDataSet summaryData;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new ProportionLayout(0.4, 3));
		add(ProportionLayout.LEFT, leftPanel(data));
		add(ProportionLayout.RIGHT, rightPanel(summaryData));
	}
	
	private CoreModelDataSet getData() {
		return new GroupsDataSet(this);
	}
	
	private SummaryDataSet getSummaryData(CoreModelDataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
			String tailString = getParameter(ALTERNATIVE_PARAM);
			if (tailString.equals("low"))
				testTail = HypothesisTest.HA_LOW;
			else if (tailString.equals("high"))
				testTail = HypothesisTest.HA_HIGH;
			else
				testTail = HypothesisTest.HA_NOT_EQUAL;
			test = new DiffHypothesisTest(sourceData, kZero, testTail,
																			DiffHypothesisTest.DIFF_MEAN_IMAGE, this);
			StringTokenizer st = new StringTokenizer(getParameter(MAX_STATS_PARAM));
			maxT = new NumValue(st.nextToken());
			maxPValue = new NumValue(st.nextToken());
		
		summaryData.addVariable("tDiff", new StatisticValueVariable(translate("t statistic"),
																															test, maxT.decimals));
		summaryData.addVariable("pValue", new PValueVariable(translate("p-value") + " =",
																															test, maxPValue.decimals));
		
		summaryData.takeSample();
		
			GroupsDataSet anovaData = (GroupsDataSet)sourceData;
			int n1 = anovaData.getN(0);
			int n2 = anovaData.getN(1);
			df = Math.min(n1, n2) - 1;
			TDistnVariable tDistn = new TDistnVariable(translate("t distn"), df);
		summaryData.addVariable("tDistn", tDistn);
		
		return summaryData;
	}
	
	private XPanel leftPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		thePanel.add("Center", dataPanel(data));
		thePanel.add("South", controlPanel());
		
		return thePanel;
	}
	
	private XPanel dataPanel(CoreModelDataSet data) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
				
				VertAxis theNumAxis = new VertAxis(this);
				theNumAxis.readNumLabels(anovaData.getYAxisInfo());
			dataPanel.add("Left", theNumAxis);
			
				HorizAxis theGroupAxis = new HorizAxis(this);
				CatVariable groupVariable = anovaData.getCatVariable();
				theGroupAxis.setCatLabels(groupVariable);
				theGroupAxis.setAxisName(anovaData.getXVarName());
			dataPanel.add("Bottom", theGroupAxis);
			
				VerticalDotView theView = new VerticalDotView(anovaData, this, theNumAxis, theGroupAxis, "y", "x", "model", 0.4);
				theView.setMeanDisplay(VerticalDotView.MEAN_CHANGE);
				theView.setShow50PercentBand(true);
				theView.lockBackground(Color.white);
			dataPanel.add("Center", theView);
		
		thePanel.add("Center", dataPanel);
		
			XLabel responseNameLabel = new XLabel(anovaData.getYVarName(), XLabel.LEFT, this);
			responseNameLabel.setFont(theNumAxis.getFont());
		thePanel.add("North", responseNameLabel);
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		return thePanel;
	}
	
	private XPanel rightPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("North", summaryPanel(summaryData));
		thePanel.add("Center", tDistnPanel(summaryData));
		thePanel.add("South", pValuePanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																								VerticalLayout.VERT_CENTER, 20));
		thePanel.add(new HypothesisView(test, HypothesisView.VERTICAL, this));
		
			OneValueImageView tView = new OneValueImageView(summaryData, "tDiff", this, "xEquals/tForDiffMeans.png", 31, maxT);
			tView.setFont(getBigFont());
		thePanel.add(tView);
		
		return thePanel;
	}
	
	private XPanel pValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			OneValueView pView = new OneValueView(summaryData, "pValue", this, maxPValue);
			pView.setForeground(kDarkGreenColor);
			pView.setFont(getBigFont());
		thePanel.add(pView);
		
		return thePanel;
	}
	
	private XPanel tDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(T_AXIS_PARAM));
			CoreVariable v = summaryData.getVariable("tDiff");
			theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
			int tail = (testTail == HypothesisTest.HA_LOW) ? DistnTailView.LOW_TAIL
						: (testTail == HypothesisTest.HA_HIGH) ? DistnTailView.HIGH_TAIL
						: DistnTailView.TWO_TAIL;
			DistnTailView tView = new DistnTailView(summaryData, this, theHorizAxis, "tDistn", "tDiff", tail);
			tView.lockBackground(Color.white);
			tView.setForeground(Color.red);
			tView.setHighlightColor(kDarkGreenColor);
			tView.setDistnName(new LabelValue("t(" + df + " df)"), Color.lightGray);
		thePanel.add("Center", tView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}