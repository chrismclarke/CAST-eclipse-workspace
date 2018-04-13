package randomStatProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;



public class MeanDistnTheoryApplet extends MeanDistnApplet {
	
	private SimpleDistnView summaryDistnView;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new ProportionLayout(0.55, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 0));
			topPanel.add("West", titlePanel(translate("Population"), kPopnColor));
			topPanel.add("South", takeSamplePanel());
			topPanel.add("Center", populationPanel(data, "model", kPopnColor));
		
		add(ProportionLayout.TOP, topPanel);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			bottomPanel.add("West", titlePanel(translate("Summary"), kSummaryColor));
			bottomPanel.add("Center", meanTheoryPanel(summaryData, "theory", kSummaryColor));
	
		add(ProportionLayout.BOTTOM, bottomPanel);
		
		setTheoryParameters(summaryData, "theory");			//	to set st devn of mean on distn
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
		super.setTheoryParameters(summaryData, theoryKey);
		if (summaryDistnView != null) {
			DistnVariable meanDistn = (DistnVariable)summaryData.getVariable(theoryKey);
			NumValue sd = meanDistn.getSD();
			LabelValue label = new LabelValue(translate("st devn") + " = " + sd.toString());
			
			summaryDistnView.setLabel(label, Color.gray);
		}
	}
	
	private XPanel meanTheoryPanel(SummaryDataSet summaryData, String modelKey, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(summaryData, modelKey);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			summaryDistnView = new SimpleDistnView(summaryData, this, horizAxis, modelKey, null, null,
																																					SimpleDistnView.STACK_ALGORITHM);
			summaryDistnView.setDensityScaling(0.9);
			summaryDistnView.lockBackground(Color.white);
			summaryDistnView.setForeground(c);
		thePanel.add("Center", summaryDistnView);
		
		return thePanel;
	}
	
	protected XPanel takeSamplePanel() {
		XPanel thePanel = new InsetPanel(100, 0);
		thePanel.setLayout(new BorderLayout(12, 0));
		
			ArrowCanvas arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
		thePanel.add("West", arrow);
		
		if (sampleSize != null)
			thePanel.add("Center", sampleSizeSliderPanel());
		
			arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
		thePanel.add("East", arrow);
		
		return thePanel;
	}
	
}