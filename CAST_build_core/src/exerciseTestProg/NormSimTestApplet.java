package exerciseTestProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;
import valueList.*;
import formula.*;
import coreVariables.*;
import coreSummaries.*;
import coreGraphics.*;

import exerciseTest.*;


public class NormSimTestApplet extends CoreNormalApplet {
	static final private double kEps = 0.00001;
	static final private NumValue kZero = new NumValue(0, 2);
	static final private NumValue kOne = new NumValue(1, 2);
	
	static final private int kMinAxisLabels = 4;
	
//	private RandomNormal normalGenerator;
//	private NumSampleStatus sampleStatus;
	
	private SummaryDataSet summaryData;
	
	private HorizAxis meanAxis, yAxis;
	private CutoffPanel cutoffPanel;
	
	private OneValueView meanValueView;
	
	private ParameterSlider meanSlider, sdSlider;
	
	private XButton sampleButton;
	
	private boolean disableDisplayUpdate = false;
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("minSimulations", "int");
		registerParameter("defaultSimulations", "int");
	}
	
	private int getMinSimulations() {
		return getIntParam("minSimulations");
	}
	
	private int getDefaultSimulations() {
		return getIntParam("defaultSimulations");
	}
	
//-----------------------------------------------------------
	
	private String getYAxis() {
		double sd = getSliderSd().toDouble();
		double mean = getSliderMean().toDouble();
		
		return NumCatAxis.neatNumAxisLabels(mean - 3.5 * sd, mean + 3.5 * sd, kMinAxisLabels);
	}
	
	private String getMeanAxis() {
		double sd = getSliderSd().toDouble() / Math.sqrt(getSampleSize());
		double mean = getSliderMean().toDouble();
		
		double actualMean = getObservedMean().toDouble();
		
		double lowLimit = Math.min(mean - 3.5 * sd, actualMean);
		double highLimit = Math.max(mean + 3.5 * sd, actualMean);
		
		return NumCatAxis.neatNumAxisLabels(lowLimit, highLimit, kMinAxisLabels);
	}
	
	private NumValue getLowSliderMean() {
		NumValue nullMean = getNullMean();
		NumValue observedMean = getObservedMean();
		int decimals = Math.max(nullMean.decimals, observedMean.decimals);
		
		double factor = 1.0;
		for (int i=0 ; i<decimals ; i++)
			factor *= 10;
		
		if (Math.round(nullMean.toDouble() * factor) == Math.round(observedMean.toDouble() * factor))
			return new NumValue(nullMean.toDouble() - 2 / factor, decimals);
		else
			return new NumValue(Math.min(nullMean.toDouble(), observedMean.toDouble()), decimals);
	}
	
	private NumValue getHighSliderMean() {
		NumValue nullMean = getNullMean();
		NumValue observedMean = getObservedMean();
		int decimals = Math.max(nullMean.decimals, observedMean.decimals);
		
		double factor = 1.0;
		for (int i=0 ; i<decimals ; i++)
			factor *= 10;
		
		if (Math.round(nullMean.toDouble() * factor) == Math.round(observedMean.toDouble() * factor))
			return new NumValue(nullMean.toDouble() + 4 / factor, decimals);
		else
			return new NumValue(Math.max(nullMean.toDouble(), observedMean.toDouble()), decimals);
	}
	
	private NumValue getDefaultSliderMean() {
		NumValue nullMean = getNullMean();
		NumValue observedMean = getObservedMean();
		int decimals = Math.max(nullMean.decimals, observedMean.decimals);
		
		double factor = 1.0;
		for (int i=0 ; i<decimals ; i++)
			factor *= 10;
		
		double startVal = Math.rint((nullMean.toDouble() + observedMean.toDouble()) / 2 * factor) / factor;
		return new NumValue(startVal, decimals);
	}
	
	private NumValue getSliderMean() {
		return meanSlider.getParameter();
	}
	
	private NumValue getLowSliderSd() {			//		assumes that n = 4,9,16,25 and sigma/root(n) has same decimals as sigma
		NumValue sd = getSd();
		int decimals = sd.decimals;
		
		return new NumValue(sd.toDouble() / 5, decimals);
	}
	
	private NumValue getHighSliderSd() {
		NumValue sd = getSd();
		int decimals = sd.decimals;
		
		return new NumValue(sd.toDouble(), decimals);
	}
	
	private NumValue getDefaultSliderSd() {
		NumValue sd = getSd();
		int decimals = sd.decimals;
		
		return new NumValue(sd.toDouble() / 2, decimals);
	}
	
	private NumValue getSliderSd() {
		return sdSlider.getParameter();
	}
		
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			
			RandomNormal normalGenerator = new RandomNormal(1, 0.0, 1.0, 3.0);
			NumVariable z = new NumSampleVariable("Z", normalGenerator, 9);
		data.addVariable("z", z);
			
			ScaledVariable yVar = new ScaledVariable("Y", z, "z", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		
		summaryData = new SummaryDataSet(data, "z");
		summaryData.setAccumulate(true);
		
			MeanVariable meanVar = new MeanVariable(translate("Mean"), "y", 9);
		summaryData.addVariable("mean", meanVar);
		
		return data;
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
		
			XPanel samplePanel = new InsetPanel(0, 6, 0, 0);
			samplePanel.setLayout(new BorderLayout(0, 0));
			
			samplePanel.add("West", sliderPanel());
			
				XPanel rightSamplePanel = new XPanel();
				rightSamplePanel.setLayout(new BorderLayout(0, 0));
				
				rightSamplePanel.add("West", takeSamplePanel());
				rightSamplePanel.add("Center", sampleDataPanel(data));
				
			samplePanel.add("Center", rightSamplePanel);
			
			samplePanel.lockBackground(kWorkingBackground);
			
		thePanel.add(ProportionLayout.TOP, samplePanel);
		
			XPanel meanPanel = new InsetPanel(0, 6, 8, 0);
			meanPanel.setLayout(new BorderLayout(10, 0));
			
				cutoffPanel = new CutoffPanel(summaryData, "mean", CutoffPanel.VERTICAL, this);
				cutoffPanel.setDiscrete(false);
				cutoffPanel.setVariableName("X#bar#");
			meanPanel.add("West", cutoffPanel);
			
			meanPanel.add("Center", empiricalDistnPanel(data));
		
		thePanel.add(ProportionLayout.BOTTOM, meanPanel);
		
		return thePanel;
	}
		
//-----------------------------------------------------------
	
	protected String getPValueLabel() {
		return MText.expandText("Propn as 'extreme' (H#sub0#)");
	}
	
	protected String getPValuesPropnsString() {
		return translate("Proportions");
	}
	
	protected String getPvalueLongName() {
		return "proportion of simulated means as extreme as " + getObservedMean();
	}
		
//-----------------------------------------------------------
	
	private XPanel takeSamplePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
			meanValueView = new OneValueView(summaryData, "mean", this);
		thePanel.add(meanValueView);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(150, 100));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
			
				String mu = MText.expandText("#mu#");
				meanSlider = new ParameterSlider(kZero, kOne, kZero, mu, ParameterSlider.NO_SHOW_MIN_MAX, this);
				registerStatusItem("mean", meanSlider);
			
			innerPanel.add(meanSlider);
			
				String sigma = MText.expandText("#sigma#");
				sdSlider = new ParameterSlider(kZero, kOne, kZero, sigma, ParameterSlider.NO_SHOW_MIN_MAX, this);
				registerStatusItem("sd", sdSlider);
			
			innerPanel.add(sdSlider);
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private XPanel sampleDataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new FixedSizeLayout(0, 80));
			
				XPanel plotPanel = new XPanel();
				plotPanel.setLayout(new AxisLayout());
					
					yAxis = new HorizAxis(this);
					yAxis.setFont(getSmallFont());
				plotPanel.add("Bottom", yAxis);
				
					DotPlotView sampleView = new DotPlotView(data, this, yAxis, 1.0);
					sampleView.setActiveNumVariable("y");
					sampleView.setCrossSize(DataView.SMALL_CROSS);
					sampleView.lockBackground(Color.white);
				plotPanel.add("Center", sampleView);
			
			innerPanel.add(plotPanel);
			
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel empiricalDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			meanAxis = new HorizAxis(this);
		thePanel.add("Bottom", meanAxis);
		
			JitterWithHiliteView meanSimulationView = new JitterWithHiliteView(summaryData, this, meanAxis, "mean");
			meanSimulationView.setRetainLastSelection(true);
			meanSimulationView.lockBackground(Color.white);
			cutoffPanel.setLinkedView(meanSimulationView);
		thePanel.add("Center", meanSimulationView);
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		disableDisplayUpdate = true;		//	do disable slider change events in action() -- they reset selection and flash
		NumValue startMean = getDefaultSliderMean();
		meanSlider.changeLimits(getLowSliderMean(), getHighSliderMean(), startMean);
		
		NumValue startSd = getDefaultSliderSd();
		sdSlider.changeLimits(getLowSliderSd(), getHighSliderSd(), startSd);
		disableDisplayUpdate = false;
		
		yAxis.readNumLabels(getYAxis());
		yAxis.invalidate();
		
		meanAxis.readNumLabels(getMeanAxis());
		String varName = getVarName();
		if (varName.length() > 0)
			meanAxis.setAxisName("Distn of " + varName);
		meanAxis.invalidate();
		
		cutoffPanel.clearCutoff();
		
		meanValueView.reset(getHighSliderMean());
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		SampleInterface zVar = (SampleInterface)data.getVariable("z");
		zVar.setSampleSize(getSampleSize());
		
		clearSample();
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		yVar.setScale(getDefaultSliderMean().toDouble(), getDefaultSliderSd().toDouble(), 9);
		
		MeanVariable meanVar = (MeanVariable)summaryData.getVariable("mean");
		meanVar.setDecimals(getMeanDecimals());
		summaryData.variableChanged("mean");
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all 3 steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		messagePanel.insertText("#bullet#  Use the sliders to set the H#sub0# values of the normal mean and sd, then simulate at least " + getMinSimulations() + " samples.\n");
		messagePanel.insertText("#bullet#  Type the proportion of samples with as 'extreme' a mean as that observed based on the simulation. (Hint: type a value for the 'cutoff'.)\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify your conclusion from the test.");
	}
	
	private boolean sliderMeanError() {
		NumValue nullMean = getNullMean();
		NumValue attemptMean = getSliderMean();
		double maxMeanError = (getHighSliderMean().toDouble() - getLowSliderMean().toDouble()) * kEps;
		return Math.abs(attemptMean.toDouble() - nullMean.toDouble()) > maxMeanError;
	}
	
	private boolean sliderSdError() {
		double sigma = getSd().toDouble();
		double attemptSd = getSliderSd().toDouble();
		double maxSdError = sigma * kEps;
		return Math.abs(attemptSd - sigma) > maxSdError;
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		NumValue attemptMean = getSliderMean();
		NumValue nullMean = getNullMean();
		
		messagePanel.insertRedHeading("There is an error in the the simulation!\n");
		if (sliderMeanError()) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertRedText("\nThe null hypothesis mean is " + nullMean + " but the '#mu#' slider is set to " + attemptMean + ".");
		}
		else if (sliderSdError()) {
			messagePanel.insertText("(You have correctly specified the hypotheses and set the #mu# slider.)\n");
			messagePanel.insertRedText("However the #sigma# slider should be set to the population standard deviation, " + getSd() + ".");
		}
		else {
			int minSimulations = getMinSimulations();
			NumVariable meanVar = (NumVariable)summaryData.getVariable("mean");
			int n = meanVar.noOfValues();
			if (n < minSimulations) {
				messagePanel.insertText("(You have correctly specified the hypotheses and set the #mu# and #sigma# sliders.)\n");
				messagePanel.insertText("You have not performed enough repeats of the simulation. You should perform at least " + minSimulations + " repetitions but have only done " + n + ".");
			}
			else {
				messagePanel.insertText("(You have correctly specified the hypotheses and performed the simulation.)\n");
				
				NumValue attemptCutoff = cutoffPanel.getCutoff();
				double maxMeanError = (getHighSliderMean().toDouble() - getLowSliderMean().toDouble()) * kEps;
				if ((attemptCutoff.toDouble() - getObservedMean().toDouble()) > maxMeanError)
					messagePanel.insertText("However you have not used the correct cutoff value to find which counts are more 'extreme' than what was observed.");
				else
					messagePanel.insertText("You have not correctly found the proportion of more 'extreme' counts from the simulation.");
			}
		}
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		NumValue nullMean = getNullMean();
		NumValue observedMean = getObservedMean();
		messagePanel.insertText("The H#sub0# value for the mean is #mu# = " + nullMean + ". ");
		
		NumVariable meanVar = (NumVariable)summaryData.getVariable("mean");
		int n = meanVar.noOfValues();
		messagePanel.insertText("The diagram shows results from " + n + " simulations with this parameter. ");
		
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The more 'extreme' samples are those whose mean is ");
				messagePanel.insertBoldText("less than or equal to ");
				messagePanel.insertText(observedMean + ".");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The more 'extreme' samples are those whose mean is ");
				messagePanel.insertBoldText("greater than or equal to ");
				messagePanel.insertText(observedMean + ".");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The more 'extreme' samples are those whose those whose mean is ");
				messagePanel.insertBoldText("at least as extreme as ");
				messagePanel.insertText(observedMean + ". This is ");
				messagePanel.insertBoldText("twice");
				messagePanel.insertText(" the proportion with mean ");
				messagePanel.insertText((cumulativeProbability(observedMean.toDouble()) < 0.5 ? "#le# " : "#ge# ") + observedMean + ".");
				break;
		}
	}	
//-----------------------------------------------------------
	
	
	protected double cumulativeProbability(double cutoff) {
		NumVariable meanVar = (NumVariable)summaryData.getVariable("mean");
		int n = meanVar.noOfValues();
		return countBelow(cutoff) / (double)n;
	}
	
	protected boolean correctModelSetup() {
		int minSimulations = getMinSimulations();
		NumVariable meanVar = (NumVariable)summaryData.getVariable("mean");
		int n = meanVar.noOfValues();
		if (n < minSimulations)
			return false;
		
		return !sliderMeanError() && !sliderSdError();
	}
	
	private int countBelow(double cutoff) {
		NumVariable meanVar = (NumVariable)summaryData.getVariable("mean");
		int n = meanVar.noOfValues();
		int x = 0;
		for (int i=0 ; i<n ; i++) {
			double mean = meanVar.doubleValueAt(i);
			if (mean <= cutoff)
				x ++;
		}
		return x;
	}
	
	private void clearSample() {
		NumVariable zVar = (NumVariable)data.getVariable("z");
		zVar.clearData();
		data.variableChanged("z");
		
		summaryData.clearData();
		summaryData.variableChanged("mean");
		
		if (cutoffPanel != null)
			cutoffPanel.clearCutoff();
	}
	
	protected void showCorrectWorking() {
		NumValue correctMean = getNullMean();
		NumValue correctSd = getSd();
		
		if (sliderMeanError() || sliderSdError()) {
			meanSlider.setParameter(correctMean.toDouble(), false);
			sdSlider.setParameter(correctSd.toDouble(), false);
					//	postEvent=false so noteChangedWorking() is not called (it would set result to ANS_UNCHECKED)
			
			
			ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
			yVar.setScale(correctMean.toDouble(), correctSd.toDouble(), 9);
			
			clearSample();
			
			meanAxis.readNumLabels(getMeanAxis());
			meanAxis.invalidate();
		}
		
		NumVariable meanVar = (NumVariable)summaryData.getVariable("mean");
		if (meanVar.noOfValues() < getMinSimulations()) {
			int simCount = getDefaultSimulations();
			for (int i=meanVar.noOfValues() ; i<simCount ; i++)
				summaryData.takeSample();
		}
		
		NumValue observedMean = getObservedMean();
		int tail = getTail();
		if (tail == TAIL_LOW || tail == TAIL_LOW_EQ)
			cutoffPanel.setCorrectCutoff(observedMean, CutoffPanel.LOW_TAIL);
		else
			cutoffPanel.setCorrectCutoff(observedMean, CutoffPanel.HIGH_TAIL);
		
		showCorrectAnswer();
	}
	
//-----------------------------------------------------------
	
	private boolean localAction(Object target) {
		if ((target == meanSlider || target == sdSlider) && !disableDisplayUpdate) {
			ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
			NumValue newMean = meanSlider.getParameter();
			NumValue newSd = sdSlider.getParameter();
			yVar.setScale(newMean.toDouble(), newSd.toDouble(), 9);
			
			meanAxis.readNumLabels(getMeanAxis());
			meanAxis.invalidate();
			
			yAxis.readNumLabels(getYAxis());
			yAxis.invalidate();
				
			cutoffPanel.clearCutoff();
			
			clearSample();
			data.variableChanged("mean");
			noteChangedWorking();
			return true;
		}
		else if (target == sampleButton) {
			summaryData.takeSample();
			noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}