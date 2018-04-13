package randomisationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;

import randomisation.*;


abstract public class CoreStatDistanceApplet extends CoreSimApplet {
	static final private String Z_SUMMARY_AXIS_PARAM = "zAxis";
	
	private XChoice summaryDisplayChoice;
	private int currentSummaryDisplay = 0;
	
	protected int paramSdDecimals;
	protected NumValue nullParam, popnSd;
	protected NumValue actualParam;
	
	private MultiHorizAxis multiSummaryAxis = null;
	
	
	abstract protected double dataPanelPropn();
	
	protected void addPanels(XPanel samplePanel, XPanel summaryPanel) {
		if (multiSummaryAxis == null) {
			setLayout(new ProportionLayout(dataPanelPropn() + 0.05, 30, ProportionLayout.VERTICAL, ProportionLayout.TOTAL));
			add(ProportionLayout.TOP, samplePanel);
			add(ProportionLayout.BOTTOM, summaryPanel);
		}
		else {
			setLayout(new ProportionLayout(dataPanelPropn(), 30, ProportionLayout.VERTICAL, ProportionLayout.TOTAL));
			add(ProportionLayout.TOP, samplePanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 30));
				
				bottomPanel.add("North", zChoicePanel(summaryData));
				bottomPanel.add("Center", summaryPanel);
			add(ProportionLayout.BOTTOM, bottomPanel);
		}
	}
	
	
	abstract protected String lowerCaseParam();		//	lower case "mean" or "propn"
	
	
	protected XPanel zChoicePanel(DataSet summaryData) {
		XPanel choicePanel = new XPanel();
		choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			summaryDisplayChoice = new XChoice(translate("Summarise sample " + lowerCaseParam() + " with") + ":", null, XChoice.HORIZONTAL, this);
			summaryDisplayChoice.addItem(translate("Raw value"));
			summaryDisplayChoice.addItem(translate("'Statistical distance' from") + " " + nullParam);
		
		choicePanel.add(summaryDisplayChoice);
	
		return choicePanel;
	}
	
	
	abstract protected DataSet getData();
	abstract protected void addStatistic(SummaryDataSet summaryData);
	
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		addStatistic(summaryData);
		
		if (showTheory) {
			NormalDistnVariable theory = new NormalDistnVariable(translate("Theory"));
			theory.setMean(nullParam.toDouble());
			theory.setSD(popnSd.toDouble() / Math.sqrt(sampleSize));
				
			theory.setMinSelection(lowCutOff());
			theory.setMaxSelection(highCutOff());
			
			summaryData.addVariable("theory", theory);
		}
		
		return summaryData;
	}
	
	
	abstract protected XPanel modelInfoPanel(DataSet data);
	abstract protected XPanel sampleViewPanel(DataSet data);
	
	
	protected HorizAxis getSummaryAxis(DataSet data) {
		String zAxisString = getParameter(Z_SUMMARY_AXIS_PARAM);
		if (zAxisString ==  null)
			return super.getSummaryAxis(data);
		else {
			multiSummaryAxis = new MultiHorizAxis(this, 2);
			multiSummaryAxis.readNumLabels(getParameter(SUMMARY_AXIS_INFO_PARAM));
			multiSummaryAxis.readExtraNumLabels(zAxisString);
			multiSummaryAxis.setAxisName(summaryName(data));
			return multiSummaryAxis;
		}
	}
	
	
	abstract protected String summaryName(DataSet data);
	
	
	protected DataView getSummaryView(DataSet summaryData, HorizAxis summaryAxis) {
		MoreExtremeView theView = new MoreExtremeView(summaryData, this, summaryAxis, "stat",
																																				showTheory ? "theory" : null);
		theView.setShowDensity(false);
		theView.setCutOffs(lowCutOff(), highCutOff());
		return theView;
	}
	
	protected String getMenuTheoryString() {
		return translate("Normal distribution");
	}
	
	protected double lowCutOff() {
		double nullVal = nullParam.toDouble();
		double dataVal = actualParam.toDouble();
		return (tailType == LOW_TAIL) ? dataVal :
										(tailType == TWO_TAIL) ? nullVal - Math.abs(dataVal - nullVal)
										: Double.NEGATIVE_INFINITY;
	}
	
	protected double highCutOff() {
		double nullVal = nullParam.toDouble();
		double dataVal = actualParam.toDouble();
		return (tailType == LOW_TAIL) ? Double.POSITIVE_INFINITY :
										(tailType == TWO_TAIL) ? nullVal + Math.abs(dataVal - nullVal)
										: dataVal;
	}
	
	protected XPanel theoryParamPanel(DataSet summaryData) {
		XPanel titlePanel = new XPanel();
		titlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		titlePanel.add(new XLabel(translate("Distn of " + lowerCaseParam()) + ":", XLabel.LEFT, this));
		
			NumValue sdOfMean = new NumValue(popnSd.toDouble() / Math.sqrt(sampleSize), paramSdDecimals);
		
		titlePanel.add(new XLabel(translate("Normal") + " (" + nullParam.toString() + ", " + sdOfMean.toString() + ")", XLabel.LEFT, this));
		
		return titlePanel;
	}
	
	protected String extremePropnString() {
		if (tailType == LOW_TAIL)
			return translate("P(" + lowerCaseParam() + " below") + " " + actualParam + ")";
		else if (tailType == HIGH_TAIL)
			return translate("P(" + lowerCaseParam() + " above") + " " + actualParam + ")";
		else {
			StringTokenizer st = new StringTokenizer(translate("P(" + lowerCaseParam() + " as far as * from *)"), "*");
			return st.nextToken() + actualParam + st.nextToken() + nullParam + st.nextToken();
		}
	}
	
	protected String extremeProbString() {
		return extremePropnString();
	}
	
	protected void adjustSummaryDisplay() {
		((MoreExtremeView)summaryView).setShowDensity(currentResultsDisplay == 1);
		summaryView.repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == summaryDisplayChoice) {
			int newChoice = summaryDisplayChoice.getSelectedIndex();
			if (currentSummaryDisplay != newChoice) {
				currentSummaryDisplay = newChoice;
				multiSummaryAxis.setAlternateLabels(newChoice);
				StringTokenizer st = new StringTokenizer(translate("'Statistical distance' of " + lowerCaseParam() + " from * as z-value"), "*");
				multiSummaryAxis.setAxisName((newChoice == 0) ? summaryName(data) : (st.nextToken() + nullParam + st.nextToken()));
				multiSummaryAxis.repaint();
			}
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