package ssqProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import imageUtils.*;

import ssq.*;

public class AnovaTableApplet extends XApplet {
	
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String INITIAL_R2_PARAM = "initialR2";
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String SHOW_PVALUE_PARAM = "showPValue";
	static final private String SHOW_R2_PARAM = "showR2";
	
	static final private Color kTableBackgroundColor = new Color(0xDAE4FF);
	
	private boolean xNumNotCat;
	private CoreModelDataSet data;
	private AnovaSummaryData summaryData;
	private NumValue maxSsq, maxMss, maxF, maxRSquared;
	
	private XChoice sampSizeChoice;
	private XButton sampleButton;
	private R2Slider rSlider;
	
	public void setupApplet() {
		xNumNotCat = (getParameter(X_LABELS_PARAM) == null);
		if (xNumNotCat)
			AnovaImages.loadRegnImages(this);
		else
			AnovaImages.loadGroupImages(this);
		
		data = readData();
		
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(10, 10));
		
//			XPanel topPanel = new XPanel();
//			topPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL,
//																ProportionLayout.TOTAL));
//		
//			topPanel.add(ProportionLayout.LEFT, dataDisplayPanel(data));
//		
//			topPanel.add(ProportionLayout.RIGHT, controlPanel(data, summaryData));
		
		add("Center", dataDisplayPanel(data));
		add("East", controlPanel(data, summaryData));
		
		add("South", bottomPanel(summaryData));
	}
	
	private CoreModelDataSet readData() {
		CoreModelDataSet data;
		if (xNumNotCat)
			data = new SimpleRegnDataSet(this);
		else
			data = new GroupsDataSet(this);
		
		data.addBasicComponents();
		
		return data;
	}
	
	private AnovaSummaryData getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		return new AnovaSummaryData(data, "error", BasicComponentVariable.kComponentKey,
																									maxSsq.decimals, maxRSquared.decimals);
	}
	
	private XPanel dataDisplayPanel(DataSet data) {
		DataWithComponentsPanel thePanel = new DataWithComponentsPanel(this);
		thePanel.setupPanel(data, "x", "y", "ls", null, DataWithComponentView.NO_COMPONENT_DISPLAY, this);
		return thePanel;
	}
	
	private XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 10));
			
			double initialR2 = Double.parseDouble(getParameter(INITIAL_R2_PARAM));
			rSlider = new R2Slider(this, data, "y", "y", summaryData, translate("Variation in Data"),
																																							initialR2);
			rSlider.updateForNewR2();
		thePanel.add(rSlider);
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																														VerticalLayout.CENTER, 4));
		
				XPanel sampSizePanel = new XPanel();
				sampSizePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
					XLabel sampSizeLabel = new XLabel(translate("Sample size") + ":", XLabel.LEFT, this);
					sampSizeLabel.setFont(getStandardBoldFont());
				sampSizePanel.add(sampSizeLabel);
					
					sampSizeChoice = data.dataSetChoice(this);
					
				sampSizePanel.add(sampSizeChoice);
			
			samplePanel.add(sampSizePanel);
		
//				XPanel takeSamplePanel = new XPanel();
//				takeSamplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
//					sampleButton = new XButton(translate("Take sample"), this);
//				takeSamplePanel.add(sampleButton);
//				
//			samplePanel.add(takeSamplePanel);
		
		thePanel.add(samplePanel);
		
		String showR2String = getParameter(SHOW_R2_PARAM);
		if (showR2String != null && showR2String.equals("true")) {
			XPanel r2Panel = new XPanel();
			r2Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			r2Panel.add(new OneValueImageView(summaryData, "rSquared", this,
															"xEquals/rSquared2.png", 23, maxRSquared));
			thePanel.add(r2Panel);
		}
		
		return thePanel;
	}
	
	private XPanel bottomPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel tablePanel = new InsetPanel(20, 5);
			tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
				String componentName[] = new String[3];
				for (int i=0 ; i<3 ; i++)
					componentName[i] = st.nextToken();
			
				String showPValueString = getParameter(SHOW_PVALUE_PARAM);
				int tableDisplay = (showPValueString == null || showPValueString.equals("true"))
																	? AnovaTableView.SSQ_F_PVALUE : AnovaTableView.SSQ_AND_F;
			
				AnovaTableView tableView = new AnovaTableView(summaryData, this,
											BasicComponentVariable.kComponentKey, maxSsq, maxMss, maxF, tableDisplay);
				tableView.setComponentNames(componentName);
			
			tablePanel.add(tableView);
		
			tablePanel.lockBackground(kTableBackgroundColor);
		thePanel.add(tablePanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampSizeChoice) {
			if (data.changeDataSet(sampSizeChoice.getSelectedIndex())) {
				data.variableChanged("rawY");
				summaryData.setSingleSummaryFromData();
				rSlider.updateForNewR2();
			}
			return true;
		}
		else if (target == sampleButton) {
			summaryData.takeSample();
			rSlider.updateForNewR2();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}