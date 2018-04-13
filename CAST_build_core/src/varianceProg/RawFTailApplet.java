package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import coreGraphics.*;
import coreVariables.*;
import models.*;
import formula.*;
import imageUtils.*;

import ssq.*;
import variance.*;


public class RawFTailApplet extends RawFDistnApplet {
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	
	private ParameterSlider meanSlider;
	
	protected double getFDisplayProportion() {
		return 0.45;
	}
	
	protected AnovaSummaryData getSummaryData(DataSet sourceData) {
		AnovaSummaryData summaryData = super.getSummaryData(sourceData);
		
		summaryData.setAccumulate(false);
		
		return summaryData;
	}
	
	protected XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 10));
		
			thePanel.add(ProportionLayout.RIGHT, new ImageCanvas("anova/zeroMeanHypoth.gif", this));
			
				StringTokenizer st = new StringTokenizer(getParameter(MEAN_LIMITS_PARAM));
				String minString = st.nextToken();
				String maxString = st.nextToken();
				int noOfSteps = Integer.parseInt(st.nextToken());
				ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
				double startValue = yVar.getParam(0);
				
				String mu = MText.expandText("#mu#");
				meanSlider = new ParameterSlider(new NumValue(minString), new NumValue(maxString),
											new NumValue(startValue), noOfSteps, mu, this);
			thePanel.add(ProportionLayout.LEFT, meanSlider);
			
		return thePanel;
	}
	
	protected AnovaTableView anovaTable(AnovaSummaryData summaryData) {
		AnovaTableView table = new AnovaTableView(summaryData, this,
									SimpleComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_F_PVALUE);
		table.setComponentNames(kComponentNames);
		return table;
	}
	
	protected DataView createSampleView(DataSet data, HorizAxis horizAxis) {
		return new MeanAndTargetView(data, this, horizAxis, "model", 0.0, new LabelValue("Target"));
	}
	
	protected DataPlusDistnInterface createFView(SummaryDataSet summaryData, HorizAxis horizAxis) {
		FView theView = new FView(summaryData, this, horizAxis, "fDistn", "F");
		double areaProportion = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
		theView.setAreaProportion(areaProportion);
		
		theView.lockBackground(Color.white);
			
		return theView;
	}
	
	private boolean localAction(Object target) {
		if (target == meanSlider) {
			double newMean = meanSlider.getParameter().toDouble();
			ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
			yVar.setParam(0, newMean);
			
			NormalDistnVariable model = (NormalDistnVariable)data.getVariable("model");
			model.setMean(newMean);
			
			data.variableChanged("model");
			data.variableChanged("y");
			summaryData.redoLastSummary();
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