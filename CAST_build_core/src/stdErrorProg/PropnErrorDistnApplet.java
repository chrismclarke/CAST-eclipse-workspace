package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;
import coreVariables.*;
import imageUtils.*;


public class PropnErrorDistnApplet extends MeanErrorDistnApplet {

	private NumValue modelProb;
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			RandomMultinomial generator = new RandomMultinomial(getParameter(RANDOM_PARAM));
			CatVariable y = new CatSampleVariable(getParameter(CAT_NAME_PARAM), generator, Variable.USES_REPEATS);
			y.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", y);
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_PARAM));
			noOfValues = Integer.parseInt(st.nextToken());
			
			CatDistnVariable popnDistn = new CatDistnVariable(getParameter(CAT_NAME_PARAM));
			popnDistn.readLabels(getParameter(CAT_LABELS_PARAM));
			modelProb = new NumValue(st.nextToken());
			double probs[] = new double[2];
			probs[0] = modelProb.toDouble();
			probs[1] = 1.0 - probs[0];
			popnDistn.setProbs(probs);
			
		data.addVariable("model", popnDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			maxEstimate = new NumValue(getParameter(MAX_ESTIMATE_PARAM));
			PropnVariable propnVar = new PropnVariable(translate("Proportion"), "y", maxEstimate.decimals);
		summaryData.addVariable("estimate", propnVar);
			
			ScaledVariable error = new ScaledVariable(translate("Error"), propnVar,
																				"estimate", -modelProb.toDouble(), 1.0, maxEstimate.decimals);
		
		summaryData.addVariable("error", error);
		
			NormalDistnVariable errorDistn = new NormalDistnVariable("Error distn");
			errorDistn.setMean(0.0);
			errorDistn.setSD(Math.sqrt(maxEstimate.toDouble() * (1.0 - maxEstimate.toDouble()) / noOfValues));
			
		summaryData.addVariable("errorDistn", errorDistn);
		
			NumVariable unknownVar = new NumVariable("dummy");
			unknownVar.readValues("?");
		summaryData.addVariable("unknown", unknownVar);
		
		return summaryData;
	}
	
	protected XPanel populationPanel(DataSet data, Color c) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 8);
		thePanel.setLayout(new BorderLayout(10, 0));
		
			CatPieChartView localView = new CatPieChartView(data, this, "model");
			localView.setVariableKey(null, this);
			localView.setForeground(c);
			popnView = localView;
		thePanel.add("Center", popnView);
		
			XPanel keyPanel = new InsetPanel(0, 0, 40, 0);
			keyPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 0));
																											
				CatVariableKey key = new CatVariableKey(data, this, "model");
				key.setFont(getStandardBoldFont());
				key.setForeground(c);
				
			keyPanel.add(key);
			
		thePanel.add("East", keyPanel);
		return thePanel;
	}
	
	protected FixedValueImageView getParameterView(DataSet data) {
		return new FixedValueImageView("xEquals/piBlack.png", 8, modelProb, Double.NaN, this);
	}
	
	protected XPanel samplePanel(DataSet data, Color c, Color background) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 8);
		thePanel.setLayout(new BorderLayout(10, 0));
		
			CatPieChartView sampleView = new CatPieChartView(data, this, "y");
			sampleView.setForeground(c);
		
		thePanel.add("Center", sampleView);
		
			XPanel keyPanel = new InsetPanel(0, 0, 40, 0);
			keyPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 0));
				CatVariableKey key = new CatVariableKey(data, this, "y");
				key.setFont(getStandardBoldFont());
				key.setForeground(c);
				
			keyPanel.add(key);
			
		thePanel.add("East", keyPanel);
		
		if (background != null)
			thePanel.lockBackground(background);
		return thePanel;
	}
	
	protected OneValueImageView getEstimateView(SummaryDataSet summaryData) {
		return new OneValueImageView(summaryData, "estimate", this, "xEquals/piHatBlue.png", 18, maxEstimate);
	}
	
	protected OneValueImageView getErrorView(SummaryDataSet summaryData) {
		return new OneValueImageView(summaryData, "unknown", this, "xEquals/errorOfPropnRed.png", 13, maxEstimate);
	}
	
	protected void showPopulation(boolean showPopn) {
		((CatPieChartView)popnView).setVariableKey(showPopn ? "model" : null, this);
		popnView.repaint();
		popnParamValueView.setValue(showPopn ? modelProb.toDouble() : Double.NaN);
		errorValueView.setVariableKey(showPopn ? "error" : "unknown");
		errorView.setShowUnknown(!showPopn, this);
	}
}