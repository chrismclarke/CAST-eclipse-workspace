package exerciseNormalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;
import formula.*;


import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;


public class NormalApproxProbApplet extends CoreBinomialProbApplet {
	static final private NumValue kOne = new NumValue(1, 0);
	static final private NumValue kHalf = new NumValue(0.5, 1);
	
	private NormalLookupPanel zLookupPanel;
	
	private BinomTemplatePanel xMeanTemplate, xSdTemplate;
	private ZTemplatePanel zTemplate;
	
	
//-----------------------------------------------------------
		
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("maxMeanSd", "string");
	}
	
	public String getAxisInfo() {
		return null;		//	standard normal distn axis is default
	}
	
	private NumValue getMaxMean() {
		StringTokenizer st = new StringTokenizer(getStringParam("maxMeanSd"), " ");
		return new NumValue(st.nextToken());
	}
	
	private NumValue getMaxSd() {
		StringTokenizer st = new StringTokenizer(getStringParam("maxMeanSd"), " ");
		st.nextToken();
		return new NumValue(st.nextToken());
	}
	
	
	protected Dimension getMinMax(String minMaxString, int total) {
		double p = getPSuccess().toDouble();
		int n = getNTrials();
		
		double mean = n * p;
		double sd = Math.sqrt(n * p * (1 - p));
		
		double minCum = kMinCum;
		double maxCum = 1 - kMinCum;
		StringTokenizer st = new StringTokenizer(minMaxString, ":");
		if (st.hasMoreTokens())
			minCum = Double.parseDouble(st.nextToken());
		if (st.hasMoreTokens())
			maxCum = Double.parseDouble(st.nextToken());
		
		double zMin = NormalTable.quantile(minCum);
		double zMax = NormalTable.quantile(maxCum);
		
		double xMin = mean + zMin * sd;
		double xMax = mean + zMax * sd;
		
		return new Dimension((int)Math.round(Math.ceil(xMin)), (int)Math.round(Math.floor(xMax)));
	}
	
//-----------------------------------------------------------
	
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NormalDistnVariable normalDistn = new NormalDistnVariable("Normal");
														//	default is N(0,1)
		data.addVariable("z", normalDistn);
		
		return data;
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("North", templatePanel());
		
			zLookupPanel = new NormalLookupPanel(data, "z", this, CoreLookupPanel.HIGH_ONLY);
			registerStatusItem("zDrag", zLookupPanel);
		thePanel.add("Center", zLookupPanel);
		
		return thePanel;
	}
	
	private XPanel templatePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				xMeanTemplate = new BinomTemplatePanel(MText.expandText("#mu# ="), BinomTemplatePanel.X_MEAN, getMaxMean(), stdContext);
				registerStatusItem("xMeanTemplate", xMeanTemplate);
			topPanel.add(xMeanTemplate);
			
				xSdTemplate = new BinomTemplatePanel(MText.expandText("#sigma# ="), BinomTemplatePanel.X_SD, getMaxSd(), stdContext);
				registerStatusItem("xSdTemplate", xSdTemplate);
			topPanel.add(xSdTemplate);
		thePanel.add(topPanel);
		
			XPanel bottomPanel = new InsetPanel(20, 7);
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				zTemplate = new ZTemplatePanel(MText.expandText("z ="), 4, stdContext);
				registerStatusItem("zTemplate", zTemplate);
			bottomPanel.add(zTemplate);
			
			bottomPanel.lockBackground(kWorkingBackground);
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		xMeanTemplate.setValues(kOne, kHalf, kHalf);
		xMeanTemplate.changeMaxValue(getMaxMean());
		
		xSdTemplate.setValues(kOne, kHalf, kHalf);
		xSdTemplate.changeMaxValue(getMaxSd());
		
		zLookupPanel.resetPanel();
		
		data.variableChanged("z");
		
		zTemplate.setValues(kOne, kOne, kOne);
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NormalDistnVariable zDistn = (NormalDistnVariable)data.getVariable("z");
		zDistn.setMinSelection(Double.NEGATIVE_INFINITY);
		zDistn.setMaxSelection(0.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		DiscreteIntervalLimits limits = getLimits();
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("#bullet#  Find the parameters, #mu# and #sigma#, of the normal approximation.\n#bullet#  Translate the endpoint of the interval into a z-score.\n#bullet#  Drag over the normal density (or type the z-score into the text-edit box).\n#bullet#  Then type the probability into the text-edit box.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a probability into the answer box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("Probabilities cannot be less than zero or more than one.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("#bullet#  This binomial distribution of X is approximately normal with\n");
				insertNormalParams(messagePanel);
				
				messagePanel.insertText("#bullet#  X being '" + limits + "' is equivalent to (X ");
				int first = limits.getFirst();
				int last = limits.getLast();
				String sign = (first <= 0) ? "< " : "> ";
				double limit = (first <= 0) ? (last + 0.5) : (first - 0.5);
				messagePanel.insertText(sign + new NumValue(limit, 1) + ")");
				
				messagePanel.insertText("\n#bullet#  This translates into Z " + sign + zTemplate.getResult());
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Correct!\n");
				messagePanel.insertText("You have correctly evaluated the probability from the normal approximation.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("Your answer is as close as could be expected by dragging on the diagram. However you should be able to get the probability correct to 4 decimal places by typing the z-value in the text-edit box above the normal density curve.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("#bullet#  Translate '" + limits + "' into an equivalent inequality with cut-off ending in '.5'");
				messagePanel.insertText("\n#bullet#  Find the parameters of the best normal approximation using the formulae\n");
				insertNormalParams(messagePanel);
				messagePanel.insertText("#bullet#  Find the z-score for the cut-off with ");
				messagePanel.insertFormula(MStandardFormulae.zFormula(this));
				messagePanel.insertText("\n#bullet#  Use the z-score to find the probability from the normal density.");
				break;
		}
	}
	
	private void insertNormalParams(MessagePanel messagePanel) {
		messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
		messagePanel.insertText("#mu# = n#pi#  and  ");
		messagePanel.insertFormula(MStandardFormulae.xSdFormula(this));
		messagePanel.insertText("\n");
		messagePanel.setAlignment(MessagePanel.LEFT_ALIGN);
	}
	
	protected int getMessageHeight() {
		return 160;
	}
	
//-----------------------------------------------------------
	
	protected double evaluateProbability(DiscreteIntervalLimits limits) {
		double p = getPSuccess().toDouble();
		int n = getNTrials();
		
		double mean = n * p;
		double sd = Math.sqrt(n * p * (1 - p));
		
		double first = limits.getFirst() - 0.5;
		double last = limits.getLast() + 0.5;
		
		double z1 = (first - mean) / sd;
		double z2 = (last - mean) / sd;
		
		return NormalTable.cumulative(z2) - NormalTable.cumulative(z1);
	}
	
	protected boolean isCorrect(double attempt, double correct, DiscreteIntervalLimits limits) {
		double p = getPSuccess().toDouble();
		int n = getNTrials();
		
		double mean = n * p;
		double sd = Math.sqrt(n * p * (1 - p));
		
		double first = limits.getFirst() - 0.5;
		double last = limits.getLast() + 0.5;
		
		double z0 = (first < 0.0) ? Double.NEGATIVE_INFINITY : (first - mean) / sd;
		double z1 = (last > n) ? Double.POSITIVE_INFINITY : (last - mean) / sd;
		double pLow = NormalTable.cumulative(z1 - 0.001) - NormalTable.cumulative(z0 + 0.001) - 0.00006;
		double pHigh = NormalTable.cumulative(z1 + 0.001) - NormalTable.cumulative(z0 - 0.001) + 0.00006;
																			//	adjustment of 0.0001 is needed because pHigh may round up to the correct answer with
																			//	the correct answer still not between pLow and pHigh
		System.out.println("z0 = " + z0 + ", z1 = " + z1 + ", pLow = " + pLow + ", pHigh = " + pHigh + ", attempt = " + attempt);
		return attempt >= pLow && attempt <= pHigh;
	}
	
	protected boolean isClose(double attempt, double correct, DiscreteIntervalLimits limits) {
		double p = getPSuccess().toDouble();
		int n = getNTrials();
		
		double mean = n * p;
		double sd = Math.sqrt(n * p * (1 - p));
		
		double first = limits.getFirst() - 0.5;
		double last = limits.getLast() + 0.5;
		double x = (first < 0.0) ? last : first;
		
		double z = (x - mean) / sd;
		
		double pixError = zLookupPanel.getPixError(new NumValue(z), null);
		
		return Math.abs(correct - attempt) < pixError;
	}
	
	
	protected void showCorrectWorking() {
		DiscreteIntervalLimits limits = getLimits();
		double prob = evaluateProbability(limits);
		NumValue probValue = new NumValue(prob, 4);
		
		resultPanel.showAnswer(probValue);
		
		NumValue pVal = getPSuccess();
		NumValue qVal = new NumValue(1 - pVal.toDouble(), pVal.decimals);
		double p = pVal.toDouble();
		int n = getNTrials();
		NumValue nVal = new NumValue(n, 0);
		
		xMeanTemplate.setValues(nVal, pVal, qVal);
		xSdTemplate.setValues(nVal, pVal, qVal);
		
		double mean = n * p;
		double sd = Math.sqrt(n * p * (1 - p));
		NumValue meanVal = new NumValue(mean, getMaxMean().decimals);
		NumValue sdVal = new NumValue(sd, getMaxSd().decimals);
		
		double first = limits.getFirst() - 0.5;
		double last = limits.getLast() + 0.5;
		
		double x = (first > 0) ? first : last;
		NumValue xVal = new NumValue(x, 1);
		zTemplate.setValues(xVal, meanVal, sdVal);
		double z = (x - mean) / sd;
		NumValue zVal = new NumValue(z, 3);
		
		zLookupPanel.showAnswer(NumValue.NEG_INFINITY_VALUE, zVal);
	}
}