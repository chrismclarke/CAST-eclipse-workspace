package statisticProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import random.*;

import statistic.*;


public class DragTargetSsqApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String START_TARGET_PARAM = "startTarget";
	static final private String MAX_SUMMARY_PARAM = "maxSummary";
	
	static final private Color kSummaryColor = Color.red;
	static final private Color kTargetColor = new Color(0x006600);	//	dark green
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private DragTargetView theView;
	private DragValAxis theHorizAxis;
	private SsqDeviationsView summaryValue;
	
	private double popnMean;
	
	private XButton takeSampleButton, sampleMeanButton, popnMeanButton;
	
	public void setupApplet() {
		data = getData();
		summaryData = new SummaryDataSet(data, "y");
		summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 16));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		String normalString = getParameter(RANDOM_NORMAL_PARAM);
		StringTokenizer st = new StringTokenizer(normalString);
			st.nextToken();
			popnMean = Double.parseDouble(st.nextToken());
		RandomNormal generator = new RandomNormal(normalString);
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theHorizAxis = new DragValAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(data.getVariable("y").name);
			theHorizAxis.setValueColor(kTargetColor);
			NumValue startTarget = new NumValue(getParameter(START_TARGET_PARAM));
			try {
				theHorizAxis.setAxisVal(startTarget);
			} catch (AxisException e) {
			}
		thePanel.add("Bottom", theHorizAxis);
		
			theView = new DragTargetView(data, this, theHorizAxis);
			theHorizAxis.setView(theView);
			theView.setCrossSize(DataView.LARGE_CROSS);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
			
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 6));
				
				NumValue maxValue = new NumValue(getParameter(MAX_SUMMARY_PARAM));
				String summaryImage = "xEquals/ssqErrorRed.png";
				summaryValue = new SsqDeviationsView(data, this, summaryImage, 19, "y", maxValue, theView,
																																				SsqDeviationsView.SUM_SSQ);
				summaryValue.setForeground(kSummaryColor);
					DataView tempView[] = new DataView[1];
					tempView[0] = summaryValue;
				theHorizAxis.setOtherLinkedViews(tempView);
			leftPanel.add(summaryValue);
			
				takeSampleButton = new XButton(translate("Another sample"), this);
			leftPanel.add(takeSampleButton);
			
		thePanel.add(ProportionLayout.LEFT, leftPanel);	
			
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 6));
				
				sampleMeanButton = new XButton("Set k to Sample Mean", this);
			rightPanel.add(sampleMeanButton);
				
				popnMeanButton = new XButton("Set k to Population Mean", this);
			rightPanel.add(popnMeanButton);
			
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleMeanButton) {
			NumVariable y = (NumVariable)data.getVariable("y");
			
			ValueEnumeration ye = y.values();
			double sy = 0.0;
			while (ye.hasMoreValues())
				sy += ye.nextDouble();
			
			try {
				theHorizAxis.setAxisVal(sy / y.noOfValues());
			} catch (AxisException e) {
			}
			return true;
		}
		else if (target == popnMeanButton) {
			try {
				theHorizAxis.setAxisVal(popnMean);
			} catch (AxisException e) {
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