package varianceProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import formula.*;

import ssq.*;
import variance.*;


public class TwoGroupSsqApplet extends XApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	static final private String SD_LIMITS_PARAM = "sdLimits";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	
//	static final private int kParamAscent = 15;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private NumValue maxSsq, maxMsq, maxF, maxRSquared;
	
	private ParameterSlider meanSlider[] = new ParameterSlider[2];
	private ParameterSlider sdSlider[] = new ParameterSlider[2];
	
	private String minMean, maxMean, minSD, maxSD;
	private int nMeanSteps, nSDSteps;
	private double startMean, startSD;
	
	public void setupApplet() {
		readMeanSDLimits();
		
		data = readData();
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(20, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 0));
			
				XPanel mainPanel = new XPanel();
				mainPanel.setLayout(new ProportionLayout(0.15, 0));
				mainPanel.add(ProportionLayout.LEFT, new XPanel());
				
					XPanel subPanel = new XPanel();
					subPanel.setLayout(new ProportionLayout(0.82, 0));
					subPanel.add(ProportionLayout.LEFT, dataPanel(data));
					subPanel.add(ProportionLayout.RIGHT, new XPanel());
				
				mainPanel.add(ProportionLayout.RIGHT, subPanel);
			
			topPanel.add(ProportionLayout.LEFT, mainPanel);
			topPanel.add(ProportionLayout.RIGHT, anovaTablePanel(summaryData));
			
		add("Center", topPanel);
		add("South", controlPanel(summaryData));
	}
	
	private void readMeanSDLimits() {
		StringTokenizer st = new StringTokenizer(getParameter(MEAN_LIMITS_PARAM));
		minMean = st.nextToken();
		maxMean = st.nextToken();
		nMeanSteps = Integer.parseInt(st.nextToken());
		startMean = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(getParameter(SD_LIMITS_PARAM));
		minSD = st.nextToken();
		maxSD = st.nextToken();
		nSDSteps = Integer.parseInt(st.nextToken());
		startSD = Double.parseDouble(st.nextToken());
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("rawY", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("x", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
																															getParameter(CAT_LABELS_PARAM));
			FixedMeanSDVariable scaledVariable = new FixedMeanSDVariable(getParameter(VAR_NAME_PARAM),
																																						data, "rawY", "x");
			scaledVariable.setMean(startMean);
			scaledVariable.setSD(startSD);
		data.addVariable("y", scaledVariable);
		
			GroupsModelVariable lsFit = new GroupsModelVariable("ls", data, "x");
			lsFit.updateLSParams("y");
		data.addVariable ("ls", lsFit);
		
		for (int i=0 ; i<TwoGroupComponentVariable.kComponentKey.length ; i++) {
			String key = TwoGroupComponentVariable.kComponentKey[i];
			int type = TwoGroupComponentVariable.kComponentType[i];
			TwoGroupComponentVariable comp = new TwoGroupComponentVariable(key, data, "x", "y",
											"ls", type, 10);
			data.addVariable(key, comp);
		}
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		
		return new AnovaSummaryData(data, "error", TwoGroupComponentVariable.kComponentKey,
																								maxSsq.decimals, maxRSquared.decimals);
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			HorizAxis theGroupAxis = new HorizAxis(this);
			CatVariable xVariable = (CatVariable)data.getVariable("x");
			theGroupAxis.setCatLabels(xVariable);
		thePanel.add("Bottom", theGroupAxis);
		
			DataView theView = new TwoGroupSpreadDotView(data, this, yAxis, theGroupAxis, "y", "x");
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel anovaTablePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
																	TwoGroupComponentVariable.kComponentKey, maxSsq, maxMsq, maxF,
																	AnovaTableView.SSQ_ONLY);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			String componentName[] = new String[4];
			for (int i=0 ; i<4 ; i++)
				componentName[i] = st.nextToken();
			table.setComponentNames(componentName);
			table.setComponentColors(TwoGroupComponentVariable.kComponentColor);
			table.setFont(getBigFont());
		thePanel.add(table);
		
		return thePanel;
	}
	
	private XPanel oneGroupSliderPanel(int index, int withinIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			String yBar = MText.expandText("y#bar##sub" + index + "#");
			meanSlider[index] = new ParameterSlider(new NumValue(minMean), new NumValue(maxMean),
											new NumValue(startMean), nMeanSteps, yBar, this);
			meanSlider[index].setForeground(TwoGroupComponentVariable.kComponentColor[TwoGroupComponentVariable.BETWEEN_MEANS]);
		
		thePanel.add("Center", meanSlider[index]);
		
			String s = MText.expandText("s#sub" + index + "#");
			sdSlider[index] = new ParameterSlider(new NumValue(minSD), new NumValue(maxSD),
											new NumValue(startSD), nSDSteps, s, this);
			sdSlider[index].setForeground(TwoGroupComponentVariable.kComponentColor[withinIndex]);
		
		thePanel.add("South", sdSlider[index]);
		
		return thePanel;
	}
	
	private XPanel controlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
			
		thePanel.add(ProportionLayout.LEFT, oneGroupSliderPanel(0, TwoGroupComponentVariable.WITHIN_0));
		thePanel.add(ProportionLayout.RIGHT, oneGroupSliderPanel(1, TwoGroupComponentVariable.WITHIN_1));
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		for (int i=0 ; i<meanSlider.length ; i++)
			if (target == meanSlider[i] || target == sdSlider[i]) {
				double newMean = meanSlider[i].getParameter().toDouble();
				double newSD = sdSlider[i].getParameter().toDouble();
				FixedMeanSDVariable scaledVariable = (FixedMeanSDVariable)data.getVariable("y");
				scaledVariable.setMean(newMean, i);
				scaledVariable.setSD(newSD, i);
				GroupsModelVariable lsFit = (GroupsModelVariable)data.getVariable("ls");
				lsFit.updateLSParams("y");
				data.variableChanged("y");
				summaryData.setSingleSummaryFromData();
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}