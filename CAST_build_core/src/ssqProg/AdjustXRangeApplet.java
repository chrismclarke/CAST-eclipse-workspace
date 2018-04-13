package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import imageUtils.*;

import ssq.*;


public class AdjustXRangeApplet extends XApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final protected NumValue kMaxRSquared = new NumValue(1.0, 3);
	
	private VariableXRangeDataSet data;
	private AnovaSummaryData summaryData;
	private NumValue maxSsq;
	
	private XNoValueSlider xRangeSlider;
	private XButton sampleButton;
	
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	
	public void setupApplet() {
		AnovaImages.loadRegnImages(this);
		
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		
		data = readData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(10, 0));
		
			DataWithComponentsPanel dataPanel = new DataWithComponentsPanel(this);
			dataPanel.setupPanel(data, "x", "y", "ls", "model",
																					DataWithComponentView.NO_COMPONENT_DISPLAY, this);
		add("Center", dataPanel);
		
		add("East", anovaPanel(summaryData));
		add("South", controlPanel(data));
	}
	
	private VariableXRangeDataSet readData() {
		VariableXRangeDataSet data = new VariableXRangeDataSet(this);
		data.addBasicComponents();
		return data;
	}
	
	private AnovaSummaryData getSummaryData(DataSet data) {
		return new AnovaSummaryData(data, "error", BasicComponentVariable.kComponentKey,
																								maxSsq.decimals, kMaxRSquared.decimals);
	}
	
	private XPanel anovaPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			AnovaTableView theTable = new AnovaTableView(summaryData, this,
									BasicComponentVariable.kComponentKey, maxSsq, null, null, AnovaTableView.SSQ_ONLY);
		thePanel.add(theTable);
		
			OneValueImageView r2 = new OneValueImageView(summaryData, "rSquared",
																					this, "xEquals/rSquared.png", 14, kMaxRSquared);
			r2.setFont(getBigFont());
		thePanel.add(r2);
		return thePanel;
	}
	
	private XPanel controlPanel(VariableXRangeDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
			
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
				xRangeSlider = new XNoValueSlider(translate("wide"), translate("narrow"), translate("Spread of X"), 0, 99, 0, this);
			sliderPanel.add(xRangeSlider);
		
		thePanel.add("Center", sliderPanel);
		
			XPanel samplingPanel = new XPanel();
			samplingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				XPanel sampleSizePanel = new XPanel();
				sampleSizePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				sampleSizePanel.add(new XLabel(translate("Sample size"), XLabel.LEFT, this));
					
					sampleSizeChoice = data.dataSetChoice(this);
				sampleSizePanel.add(sampleSizeChoice);
				
			samplingPanel.add(sampleSizePanel);
			
				XPanel takeSamplePanel = new XPanel();
				takeSamplePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
					sampleButton = new XButton(translate("Another sample"), this);
				takeSamplePanel.add(sampleButton);
				
			samplingPanel.add(takeSamplePanel);
			
		thePanel.add("East", samplingPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == xRangeSlider) {
			data.setXRangePropn(1.0 - xRangeSlider.getValue() / (double)(xRangeSlider.getMaxValue() + 1));
			data.variableChanged("x");
			summaryData.setSingleSummaryFromData();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoiceIndex = sampleSizeChoice.getSelectedIndex();
			if (newChoiceIndex != currentSampleSizeIndex) {
				currentSampleSizeIndex = newChoiceIndex;
				data.changeDataSet(currentSampleSizeIndex);
				summaryData.setSingleSummaryFromData();
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