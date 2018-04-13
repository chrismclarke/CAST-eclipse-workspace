package varianceProg;

import java.awt.*;

import axis.*;
import dataView.*;
import random.*;
import utils.*;
import coreGraphics.*;
import coreSummaries.*;
import imageGroups.*;
import imageUtils.*;

import variance.*;


public class Chi2CICalcApplet extends XApplet {
	static final private String AXIS_PARAM = "horizAxis";
	static final private String NORMAL_PARAM = "normal";
	static final private String MAX_SUMMARY_PARAM = "maxSummary";
	
	static final private int kSampleSize[] = {10, 50};
	static final private String kTemplateFile[] = {"anova/ci9dfTemplate.gif",
																									"anova/ci49dfTemplate.gif"};
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private NumValue maxSummary;
	
	private Chi2CalculationView calcView;
	
	protected XButton takeSampleButton;
	private XChoice sampleSizeChoice;
	
	private int sampleSizeIndex = 0;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(10, 20));
		
		add("Center", displayPanel(data));
		add("East", controlPanel(summaryData));
		
			calcView = new Chi2CalculationView(summaryData, this, kTemplateFile, "sd", "ci");
			calcView.setFont(getBigBoldFont());
		add("South", calcView);
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		RandomNormal generator = new RandomNormal(getParameter(NORMAL_PARAM));
		generator.setSampleSize(kSampleSize[0]);
		NumSampleVariable yVar = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 9);
		
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "y");
		
			maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
		
			SDVariable sdVar = new SDVariable("sd", "y", maxSummary.decimals);
		summaryData.addVariable("sd", sdVar);
		
		MeanVariable meanVar = new MeanVariable("mean", "y", maxSummary.decimals);
		summaryData.addVariable("mean", meanVar);
		
			CiForSDVariable ciVar = new CiForSDVariable("ci", "y", maxSummary.decimals);
		summaryData.addVariable("ci", ciVar);
		
		return summaryData;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_PARAM));
			String axisName = getParameter(VAR_NAME_PARAM);
			if (axisName != null)
				theHorizAxis.setAxisName(axisName);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedDotPlotView theView = new StackedDotPlotView(data, this, theHorizAxis);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
				
//				OneValueImageView meanView = new OneValueImageView(summaryData, "mean", null, this,
//								"xEquals/sampMean.png", MeanSDImages.kParamAscent, maxSummary, DataView.BUFFERED);
//				meanView.setHighlightSelection(false);
//				meanView.setForeground(Color.blue);
//			
//			leftPanel.add(meanView);
			
				OneValueImageView sdView = new OneValueImageView(summaryData, "sd", this,
																	"xEquals/sampSDBlue.png", MeanSDImages.kParamAscent, maxSummary);
				sdView.setHighlightSelection(false);
				sdView.setForeground(Color.blue);
				sdView.setFont(getBigBoldFont());
			
			leftPanel.add(sdView);
			
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
				sampleSizeChoice = new XChoice(this);
				for (int i=0 ; i<kSampleSize.length; i++)
					sampleSizeChoice.addItem("n = " + String.valueOf(kSampleSize[i]));
			rightPanel.add(sampleSizeChoice);
			
				takeSampleButton = new XButton(translate("Take sample"), this);
			rightPanel.add(takeSampleButton);
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != sampleSizeIndex) {
				sampleSizeIndex = newChoice;
				calcView.setCurrentTemplate(sampleSizeIndex);
				summaryData.changeSampleSize(kSampleSize[sampleSizeIndex]);
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