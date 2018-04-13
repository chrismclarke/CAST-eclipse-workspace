package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import distn.*;
import coreGraphics.*;
import coreVariables.*;

import stdError.*;


public class MeanMedErrorDistnApplet extends MeanErrorDistnApplet {
	static final private String MEDIAN_ERROR_NAME_PARAM = "medianErrorName";
	
	protected UnknownDotPlotView meanErrorView, medianErrorView;
	
	private XButton resetButton;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(0.7, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
		
			XPanel popSampPanel = new XPanel();
			popSampPanel.setLayout(new BorderLayout(0, 0));
		
			popSampPanel.add("West", popSampPanels(titlePanel("Population", kPopnColor, null), null,
										titlePanel("Sample", kSampColor, kSampleBackgroundColor), null));
		
			popSampPanel.add("Center", popSampPanels(populationPanel(data, kPopnColor), takeSamplePanel(),
													samplePanel(data, kSampColor, kSampleBackgroundColor),
													controlPanel(summaryData, "estimate", kSampleBackgroundColor)));
		
			popSampPanel.add("East", popSampPanels(populationParamPanel(data, kPopnColor), null,
												sampleEstimatePanel(summaryData, kSummaryColor, kSampleBackgroundColor), null));
		
		add(ProportionLayout.TOP, popSampPanel);
		add(ProportionLayout.BOTTOM, errorDistnsPanel(summaryData, "error", "errorDistn",
																														"medianError", "medianErrorDistn"));
		
		summaryData.setAccumulate(true);
		showPopulation(true);
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
			PercentileVariable medianVar = new PercentileVariable(translate("Median"), "y", 0.5,
																																				maxEstimate.decimals);
		summaryData.addVariable("median", medianVar);
			
			ScaledVariable medianError = new ScaledVariable(getParameter(MEDIAN_ERROR_NAME_PARAM),
															medianVar, "median", -modelMean.toDouble(), 1.0, maxEstimate.decimals);
		
		summaryData.addVariable("medianError", medianError);
		
			NormalDistnVariable medianErrorDistn = new NormalDistnVariable("Median error distn");
			medianErrorDistn.setMean(0.0);
			medianErrorDistn.setSD(1.253 * modelSD.toDouble() / Math.sqrt(noOfValues));
			
		summaryData.addVariable("medianErrorDistn", medianErrorDistn);
		
		return summaryData;
	}
	
	protected XPanel samplePanel(DataSet data, Color c, Color background) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, "y", getParameter(AXIS_INFO_PARAM));
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			StackedDotPlotView sampleView = new StackedDotPlotView(data, this, horizAxis);
			sampleView.setActiveNumVariable("y");
			sampleView.lockBackground(Color.white);
			sampleView.setForeground(c);
		thePanel.add("Center", sampleView);
		
		if (background != null)
			thePanel.lockBackground(background);
		
		return thePanel;
	}
	
	protected XPanel sampleEstimatePanel(SummaryDataSet summaryData, Color c, Color background) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
			XLabel estimateLabel = new XLabel(translate("Estimates from sample"), XLabel.CENTER, this);
			estimateLabel.setFont(getStandardBoldFont());
			estimateLabel.setForeground(c);
		thePanel.add(estimateLabel);
		
			OneValueView meanValueView = new OneValueView(summaryData, "estimate", this, maxEstimate);
			meanValueView.setLabel(translate("Mean"));
			meanValueView.setHighlightSelection(false);
			meanValueView.setForeground(c);
		thePanel.add(meanValueView);
		
			OneValueView medianValueView = new OneValueView(summaryData, "median", this, maxEstimate);
			medianValueView.setLabel(translate("Median"));
			medianValueView.setHighlightSelection(false);
			medianValueView.setForeground(c);
		thePanel.add(medianValueView);
		
		if (background != null)
			thePanel.lockBackground(background);
		
		return thePanel;
	}
	
	private XPanel errorDistnsPanel(SummaryDataSet summaryData, String meanErrorKey,
											String meanErrorDistnKey, String medianErrorKey, String medianErrorDistnKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																															ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, errorPanel(summaryData, meanErrorKey, kSummaryColor));
			meanErrorView = errorView;
		
		thePanel.add(ProportionLayout.RIGHT, errorPanel(summaryData, medianErrorKey, kSummaryColor));
			medianErrorView = errorView;
		
		return thePanel;
	}
	
	protected XPanel controlPanel(SummaryDataSet summaryData, String summaryKey, Color background) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		thePanel.setLayout(new EqualSpacingLayout());
		
			ArrowCanvas arrow1 = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow1.setForeground(kSampColor);
		thePanel.add(arrow1);
		
			XPanel countPanel = new XPanel();
			countPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				ValueCountView theCount = new ValueCountView(summaryData, this);
				theCount.setLabel(translate("Samples") + " =");
			countPanel.add(theCount);
		thePanel.add(countPanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				resetButton = new XButton(translate("Reset"), this);
			buttonPanel.add(resetButton);
		thePanel.add(buttonPanel);
		
			ArrowCanvas arrow2 = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow2.setForeground(kSampColor);
		thePanel.add(arrow2);
		
		if (background != null)
			thePanel.lockBackground(background);
		
		return thePanel;
	}
	
	protected void showPopulation(boolean showPopn) {
		((SimpleDistnView)popnView).setDistnKey(showPopn ? "model" : null, this);
		popnView.repaint();
		popnParamValueView.setValue(showPopn ? modelMean.toDouble() : Double.NaN);
		meanErrorView.setShowUnknown(!showPopn, this);
		medianErrorView.setShowUnknown(!showPopn, this);
	}

	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			summaryData.setSingleSummaryFromData();
//			summaryData.setAccumulate(false);
//			summaryData.setAccumulate(true);		//	removes all but the last summary
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
	
}